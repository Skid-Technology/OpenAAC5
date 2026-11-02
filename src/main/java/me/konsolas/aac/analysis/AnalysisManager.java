package me.konsolas.aac.analysis;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.AACCustomFeature;
import me.konsolas.aac.api.AACCustomFeatureProvider;
import me.konsolas.aac.database.PlayerDatabase;
import me.konsolas.aac.player.PlayerLoginInfo;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class AnalysisManager implements PluginMessageListener {

    private final ExecutorService executor = Executors.newSingleThreadExecutor(result -> new Thread(result, "AAC Report Generator Thread"));

    private CompletableFuture<PlayerAnalysisReport> pendingAnalysis;

    private final AAC plugin;

    private final long historyMillis;

    private final int yellowScore;

    private final int redScore;

    private final Map<String, AnalysisActionRule> actionRules = new HashMap();

    private final FeatureRegistry featureRegistry;

    private final List<AACCustomFeatureProvider> customFeatureProviders = new ArrayList();

    private final Set<Player> alertRecipients = Collections.synchronizedSet(new LinkedHashSet());

    private final Map<UUID, PlayerAnalysisReport> onlineReports = new ConcurrentHashMap();

    private final Cache reportCache;

    public AnalysisManager(AAC plugin) {
        this.plugin = plugin;
        this.featureRegistry = new FeatureRegistry(plugin);
        double doubleValue = plugin.getConfig().getDouble("analysis.period");
        long longValue = plugin.getConfig().getLong("analysis.history");
        String string = plugin.getConfig().getString("analysis.history_time_unit");
        long value;
        try {
            value = TimeUnit.MILLISECONDS.convert(longValue, TimeUnit.valueOf(string));
        } catch (IllegalArgumentException illegalArgumentException) {
            plugin.getLogger().severe("Failed to parse history_time_unit: " + illegalArgumentException.getMessage());
            plugin.getLogger().severe("Using 1 DAY instead");
            value = TimeUnit.MILLISECONDS.convert(1L, TimeUnit.DAYS);
        }
        this.historyMillis = value;
        this.yellowScore = plugin.getConfig().getInt("analysis.yellow_score");
        this.redScore = plugin.getConfig().getInt("analysis.red_score");
        if (plugin.getConfig().getBoolean("analysis.clear_stale_entries")) {
            try {
                PlayerDatabase database = plugin.getDatabase();
                long value2 = System.currentTimeMillis() - this.historyMillis;
                int value3 = database.deleteEventsBefore(value2);
                if (value3 == 0) {
                    plugin.getLogger().info("No stale entries to remove");
                } else if (value3 == 1) {
                    plugin.getLogger().info("Removed 1 stale entry from the database");
                } else {
                    plugin.getLogger().info("Removed " + value3 + " stale entries from the database");
                }
            } catch (SQLException sQLException) {
                plugin.getLogger().log(Level.WARNING, "Failed to clear stale entries from the database", (Throwable) sQLException);
            }
        }
        ConfigurationSection configurationSection = plugin.getConfig().getConfigurationSection("actions");
        if (configurationSection != null) {
            for (String text : configurationSection.getKeys(false)) {
                ConfigurationSection configurationSection2 = configurationSection.getConfigurationSection(text);
                if (configurationSection2 != null) {
                    this.actionRules.put(text, new AnalysisActionRule(configurationSection2));
                } else {
                    plugin.getLogger().warning("Unexpected key " + text + " in config.yml:actions");
                }
            }
        } else {
            plugin.getLogger().warning("config.yml missing actions section. Consider regenerating your config.");
        }
        this.reportCache = CacheBuilder.newBuilder().maximumSize(100L).expireAfterAccess(10L, TimeUnit.MINUTES).build();
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::tick, 100L, (long) (doubleValue * 20.0));
    }

    public void updatePlayerWithAlertRecipients(Player player) {
        this.alertRecipients.add(player);
    }

    private void tick() {
        this.alertRecipients.removeIf(result -> !result.isOnline());
        if (!this.alertRecipients.isEmpty() && (this.pendingAnalysis == null || this.pendingAnalysis.isDone())) {
            Player player;
            synchronized (this.alertRecipients) {
                player = (Player) this.alertRecipients.iterator().next();
                this.alertRecipients.remove(player);
            }
            PlayerAnalysisReport report = this.getReport(player);
            RiskLevel riskLevel = report == null ? RiskLevel.NEUTRAL : report.getRiskLevel();
            this.pendingAnalysis = this.analyzeAsync(player).whenComplete((result2, result3) -> {
                if (result2 != null && result2.getRiskLevel() != riskLevel && player.isOnline()) {
                    if (this.plugin.isDatabaseEnabled()) {
                        this.updatePlayerWithPlugin(player);
                    }
                    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> this.plugin.getCommandExecutor().updatePlayer(player));
                }
            }).whenComplete((result4, result2) -> {
                if (result4 != null) {
                    for (Entry entry : this.actionRules.entrySet()) {
                        List<String> values = ((AnalysisActionRule) entry.getValue()).matchingCommands(result4);
                        if (values != null) {
                            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                                if (player.isOnline()) {
                                    for (String text : values) {
                                        text = this.plugin.resolvePlaceholders(player, text);
                                        this.plugin.getLogger().info("actions." + (String) entry.getKey() + ": " + text);
                                        this.plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), text);
                                    }
                                }
                            }, ((AnalysisActionRule) entry.getValue()).getDelayTicks());
                        }
                    }
                }
            });
        }
    }

    public PlayerAnalysisReport getReport(OfflinePlayer offlinePlayer) {
        return offlinePlayer.isOnline() ? (PlayerAnalysisReport) this.onlineReports.get(offlinePlayer.getUniqueId()) : (PlayerAnalysisReport) this.reportCache.getIfPresent(offlinePlayer.getUniqueId());
    }

    public void registerFeatureProvider(AACCustomFeatureProvider aACCustomFeatureProvider) {
        this.customFeatureProviders.add(aACCustomFeatureProvider);
    }

    public CompletableFuture<PlayerAnalysisReport> analyzeAsync(OfflinePlayer offlinePlayer) {
        CompletableFuture<PlayerAnalysisReport> completableFuture = new CompletableFuture();
        this.executor.execute(() -> {
            try {
                AAC aacPlugin = this.plugin;
                UUID uniqueId = offlinePlayer.getUniqueId();
                PlayerLoginInfo playerLoginInfo = aacPlugin.findPlayerLoginInfo(uniqueId);
                if (playerLoginInfo == null) {
                    PlayerDatabase database = this.plugin.getDatabase();
                    UUID uniqueId2 = offlinePlayer.getUniqueId();
                    UUID uUID = uniqueId2;
                    playerLoginInfo = database.findPlayerLoginInfo(uUID);
                }
                completableFuture.complete(this.analyze(offlinePlayer, playerLoginInfo));
            } catch (SQLException sQLException) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to analyse player " + offlinePlayer.getName(), (Throwable) sQLException);
                completableFuture.completeExceptionally(sQLException);
            }
        });
        return completableFuture.whenComplete((result, result2) -> {
            if (result != null) {
                if (offlinePlayer.isOnline()) {
                    this.onlineReports.put(offlinePlayer.getUniqueId(), result);
                } else {
                    this.reportCache.put(offlinePlayer.getUniqueId(), result);
                }
            }
        });
    }

    private PlayerAnalysisReport analyze(OfflinePlayer offlinePlayer, PlayerLoginInfo playerLoginInfo) throws SQLException {
        Instant instant = Instant.now();
        PlayerDatabase database = this.plugin.getDatabase();
        long value = System.currentTimeMillis() - this.historyMillis;
        UUID uniqueId = offlinePlayer.getUniqueId();
        UUID uUID = uniqueId;
        long value2 = value;
        List values = database.getEventsSince(value2, uUID);
        List<FeatureResult> values2;
        if (values.isEmpty()) {
            values2 = new ArrayList();
        } else {
            values2 = this.featureRegistry.analyze(values, playerLoginInfo);
        }
        for (AACCustomFeatureProvider aACCustomFeatureProvider : this.customFeatureProviders) {
            Collection featuresAsync = aACCustomFeatureProvider.getFeaturesAsync(offlinePlayer);
            if (featuresAsync != null) {
                ((Collection<me.konsolas.aac.api.AACCustomFeature>) aACCustomFeatureProvider.getFeaturesAsync(offlinePlayer)).stream().map(result -> {
                    String text = "(API) " + result.getInfo();
                    double score = result.getScore();
                    Map tooltip = result.getTooltip();
                    String text2 = text;
                    return FeatureResult.create(text2, tooltip, score);
                }).filter(Objects::nonNull).forEach(values2::add);
            }
        }
        double value3 = values2.stream().mapToDouble(FeatureResult::getScore).sum();
        RiskLevel riskLevel = value3 > this.redScore ? RiskLevel.HIGH : (value3 > this.yellowScore ? RiskLevel.SUSPICIOUS : RiskLevel.NEUTRAL);
        return new PlayerAnalysisReport(offlinePlayer, riskLevel, instant, value3, (List) values2);
    }

    public void evaluateUpdatePlayer(Player player) {
        this.analyzeAsync(player);
    }

    public void updatePlayer(Player player) {
        if (this.onlineReports.containsKey(player.getUniqueId())) {
            this.reportCache.put(player.getUniqueId(), (PlayerAnalysisReport) this.onlineReports.remove(player.getUniqueId()));
        }
        this.alertRecipients.remove(player);
    }

    private void updatePlayerWithPlugin(Player player) {
        ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();
        byteArrayDataOutput.writeUTF("Forward");
        byteArrayDataOutput.writeUTF("ONLINE");
        byteArrayDataOutput.writeUTF("AACAlerts");
        UUID uniqueId = player.getUniqueId();
        this.plugin.getLogger().info("Sending UUID " + uniqueId);
        byteArrayDataOutput.writeShort(16);
        byteArrayDataOutput.writeLong(uniqueId.getMostSignificantBits());
        byteArrayDataOutput.writeLong(uniqueId.getLeastSignificantBits());
        player.sendPluginMessage(this.plugin, "BungeeCord", byteArrayDataOutput.toByteArray());
    }

    public void onPluginMessageReceived(String text, Player player, byte[] values) {
        if (text.equals("BungeeCord")) {
            ByteArrayDataInput byteArrayDataInput = ByteStreams.newDataInput(values);
            String text2 = byteArrayDataInput.readUTF();
            if (text2.equals("AACAlerts")) {
                short value = byteArrayDataInput.readShort();
                if (value != 16) {
                    this.plugin.getLogger().warning("Invalid bungee message: len = " + value);
                    return;
                }
                long value2 = byteArrayDataInput.readLong();
                long value3 = byteArrayDataInput.readLong();
                UUID uUID = new UUID(value2, value3);
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uUID);
                if (offlinePlayer.getName() == null || offlinePlayer.getName().equals("null")) {
                    this.plugin.getLogger().warning("Invalid bungee message: uuid = " + uUID);
                    return;
                }
                CompletableFuture<PlayerAnalysisReport> completableFuture;
                if (this.plugin.getAnalysisManager().getReport(offlinePlayer) == null) {
                    completableFuture = this.plugin.getAnalysisManager().analyzeAsync(offlinePlayer);
                } else {
                    completableFuture = CompletableFuture.completedFuture(this.plugin.getAnalysisManager().getReport(offlinePlayer));
                }
                completableFuture.whenComplete((result, result2) -> this.plugin.getCommandExecutor().updatePlayer(offlinePlayer));
            }
        }
    }
}
