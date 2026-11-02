package me.konsolas.aac;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.clip.placeholderapi.PlaceholderAPI;
import me.konsolas.aac.analysis.AnalysisManager;
import me.konsolas.aac.api.AACAPI;
import me.konsolas.aac.api.internal.AACApiImplementation;
import me.konsolas.aac.command.AACCommandExecutor;
import me.konsolas.aac.database.PlayerDatabase;
import me.konsolas.aac.debug.DebugManager;
import me.konsolas.aac.metrics.MetricsLite;
import me.konsolas.aac.player.PlayerLoginInfo;
import me.konsolas.aac.protocol.PacketDispatcher;
import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.timings.CheckTask;
import me.konsolas.aac.timings.EntityTracker;
import me.konsolas.aac.timings.TPSCounter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class AAC extends JavaPlugin implements Listener {

    private TPSCounter tpsCounter;

    private final Map<UUID, PlayerLoginInfo> loginInfo = new ConcurrentHashMap();

    private boolean databaseEnabled;

    private PacketDispatcher packetDispatcher;

    private BiFunction<Player, String, String> placeholderResolver;

    private AnalysisManager analysisManager;

    private AACAPI api;

    private EntityTracker entityTracker;

    private CheckTask checkTask;

    private DebugManager debugManager;

    private final Map<UUID, ProtocolVersion> protocolVersions = new ConcurrentHashMap();

    private AACCommandExecutor commandExecutor;

    private PlayerDatabase database;

    public void loadLoginInfo(UUID uUID, InetAddress inetAddress) {
        try {
            this.loginInfo.put(uUID, this.database.findPlayerLoginInfo(uUID, inetAddress));
        } catch (SQLException sQLException) {
            this.getLogger().log(Level.SEVERE, "failed to retrieve login info for " + uUID, (Throwable) sQLException);
        }
    }

    public PlayerDatabase getDatabase() {
        return this.database;
    }

    public String resolvePlaceholders(Player player, String text) {
        return (String) this.placeholderResolver.apply(player, text);
    }

    public PacketDispatcher getPacketDispatcher() {
        return this.packetDispatcher;
    }

    public DebugManager getDebugManager() {
        return this.debugManager;
    }

    private void updatePlayer(Player player, boolean matches) {
        if (matches) {
            this.packetDispatcher.addExemption(player, PacketDispatcher.COMMAND_EXEMPTION);
        } else {
            this.packetDispatcher.removeExemption(player, PacketDispatcher.COMMAND_EXEMPTION);
        }
    }

    public PlayerLoginInfo findPlayerLoginInfo(UUID uUID) {
        return (PlayerLoginInfo) this.loginInfo.get(uUID);
    }

    public TPSCounter getTpsCounter() {
        return this.tpsCounter;
    }

    public EntityTracker getEntityTracker() {
        return this.entityTracker;
    }

    public AACCommandExecutor getCommandExecutor() {
        return this.commandExecutor;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitEvent(PlayerQuitEvent playerQuitEvent) {
        this.packetDispatcher.removePlayer(playerQuitEvent.getPlayer());
        AnalysisManager analysisManager2 = this.analysisManager;
        Player player = playerQuitEvent.getPlayer();
        Player player2 = player;
        analysisManager2.updatePlayer(player2);
        this.loginInfo.remove(playerQuitEvent.getPlayer().getUniqueId());
        this.protocolVersions.remove(playerQuitEvent.getPlayer().getUniqueId());
    }

    public boolean isDatabaseEnabled() {
        return this.databaseEnabled;
    }

    public AnalysisManager getAnalysisManager() {
        return this.analysisManager;
    }

    public boolean toggleExemption(Player player) {
        UUID uniqueId = player.getUniqueId();
        this.loginInfo.put(uniqueId, ((PlayerLoginInfo) this.loginInfo.get(uniqueId)).copy());
        boolean exempt = ((PlayerLoginInfo) this.loginInfo.get(uniqueId)).isExempt();
        try {
            this.database.setExempt(uniqueId, exempt);
        } catch (SQLException sQLException) {
            this.getLogger().log(Level.WARNING, "could not save exemption", (Throwable) sQLException);
        }
        this.updatePlayer(player, exempt);
        return exempt;
    }

    public ProtocolVersion getProtocolVersion(Player player) {
        return this.protocolVersions.computeIfAbsent(player.getUniqueId(), result -> {
            ProtocolVersion version = ProtocolVersion.detectPlayerVersion(player);
            if (version != null) {
                this.getLogger().info("Detected " + version + " for " + player.getName());
            } else {
                this.getLogger().warning("Unresolved version for " + player.getName() + " check your ViaVersion/ProtocolSupport build");
            }
            return version;
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinEvent(PlayerJoinEvent playerJoinEvent) {
        PacketDispatcher packetDispatcher2 = this.packetDispatcher;
        Player player = playerJoinEvent.getPlayer();
        Player player2 = player;
        packetDispatcher2.initializePlayer(player2);
        this.analysisManager.evaluateUpdatePlayer(playerJoinEvent.getPlayer());
        UUID uniqueId = playerJoinEvent.getPlayer().getUniqueId();
        if (this.findPlayerLoginInfo(uniqueId) == null) {
            this.getServer().getScheduler().runTaskAsynchronously(this, () -> {
                InetAddress address = Objects.requireNonNull(playerJoinEvent.getPlayer().getAddress()).getAddress();
                UUID uniqueId2 = playerJoinEvent.getPlayer().getUniqueId();
                this.loadLoginInfo(uniqueId2, address);
                Player player3 = playerJoinEvent.getPlayer();
                UUID uniqueId3 = playerJoinEvent.getPlayer().getUniqueId();
                boolean exempt = this.findPlayerLoginInfo(uniqueId3).isExempt();
                Player player4 = player3;
                this.updatePlayer(player4, exempt);
            });
        } else {
            Player player5 = playerJoinEvent.getPlayer();
            UUID uniqueId4 = playerJoinEvent.getPlayer().getUniqueId();
            boolean exempt2 = this.findPlayerLoginInfo(uniqueId4).isExempt();
            Player player6 = player5;
            this.updatePlayer(player6, exempt2);
        }
    }

    public void onEnable() {
        this.saveDefaultConfig();
        this.reloadConfig();
        try {
            if (this.getConfig().getBoolean("mysql.enabled")) {
                String string = this.getConfig().getString("mysql.host");
                int intValue = this.getConfig().getInt("mysql.port");
                String string2 = this.getConfig().getString("mysql.database");
                String string3 = this.getConfig().getString("mysql.username");
                String string4 = this.getConfig().getString("mysql.password");
                this.database = PlayerDatabase.create(this, string, intValue, string2, string3, string4);
            } else {
                String string5 = this.getConfig().getString("sqlite_target");
                this.database = PlayerDatabase.create(this, string5);
            }
        } catch (SQLException sQLException) {
            throw new RuntimeException(sQLException);
        }
        this.tpsCounter = new TPSCounter(this);
        this.entityTracker = new EntityTracker(this);
        this.checkTask = new CheckTask(this);
        this.packetDispatcher = new PacketDispatcher(this);
        this.analysisManager = new AnalysisManager(this);
        this.commandExecutor = new AACCommandExecutor(this);
        Objects.requireNonNull(this.getCommand("aac")).setExecutor(this.commandExecutor);
        Objects.requireNonNull(this.getCommand("aac")).setTabCompleter(this.commandExecutor);
        this.getServer().getPluginManager().registerEvents(this, this);
        if (!this.getServer().getOnlinePlayers().isEmpty()) {
            Logger logger = this.getLogger();
            Logger logger2 = logger;
            logger2.warning((String) "Attempting to handle server reload");
            for (Player player : this.getServer().getOnlinePlayers()) {
                this.getServer().getScheduler().runTaskAsynchronously(this, () -> {
                    InetAddress address = Objects.requireNonNull(player.getAddress()).getAddress();
                    UUID uniqueId = player.getUniqueId();
                    this.loadLoginInfo(uniqueId, address);
                });
                this.packetDispatcher.initializePlayer(player);
                this.analysisManager.evaluateUpdatePlayer(player);
            }
            logger = this.getLogger();
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringBuilder2 = stringBuilder;
            stringBuilder = stringBuilder2.append((String) "Initialised ").append(this.getServer().getOnlinePlayers().size());
            StringBuilder stringBuilder3 = stringBuilder;
            StringBuilder stringBuilder4 = stringBuilder3.append((String) " players");
            String text = stringBuilder4.toString();
            Logger logger3 = logger;
            logger3.warning(text);
        }
        this.databaseEnabled = this.getConfig().getBoolean("bungeecord");
        if (this.databaseEnabled) {
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this.analysisManager);
            Logger logger4 = this.getLogger();
            Logger logger5 = logger4;
            logger5.info((String) "Registered BungeeCord plugin channels");
        }
        this.api = new AACApiImplementation(this);
        this.getServer().getServicesManager().register(AACAPI.class, this.api, this, ServicePriority.Normal);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Logger logger6 = this.getLogger();
            Logger logger7 = logger6;
            logger7.info((String) "Hooking into PlaceholderAPI");
            this.placeholderResolver = (result, result2) -> PlaceholderAPI.setPlaceholders(result, result2.replace("%player_name%", result.getName()));
        } else {
            this.placeholderResolver = (result, result2) -> result2.replace("%player_name%", result.getName());
        }
        this.debugManager = new DebugManager(this);
        new MetricsLite(this, 2267);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent asyncPlayerPreLoginEvent) {
        InetAddress address = asyncPlayerPreLoginEvent.getAddress();
        UUID uniqueId = asyncPlayerPreLoginEvent.getUniqueId();
        this.loadLoginInfo(uniqueId, address);
    }
}
