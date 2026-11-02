package me.konsolas.aac.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.AACExemption;
import me.konsolas.aac.checks.PlayerChecks;
import me.konsolas.aac.compat.integration.OptionalIntegrations;
import me.konsolas.aac.debug.DebugManager;
import me.konsolas.aac.timings.AACListener;
import me.konsolas.aac.util.math.MinecraftMath;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

public class PacketDispatcher {

    private static final AACExemption CREATIVE_MODE = new AACExemption("The player is in creative mode");

    private static final AACExemption SPECTATOR_MODE = new AACExemption("The player is in spectator mode");

    private static final AACExemption SLEEPING = new AACExemption("The player is sleeping");

    private static final AACExemption DEAD_PLAYER = new AACExemption("The player is dead");

    private static final AACExemption BYPASS_PERMISSION = new AACExemption("The player had the aac.bypass permission when they joined");

    private static final AACExemption BEDROCK_CLIENT = new AACExemption("The player is connected from Bedrock via GeyserMC");

    public static final AACExemption COMMAND_EXEMPTION = new AACExemption("The player has been exempted by the command /aac exempt");

    private final AAC plugin;

    private final Map playerChecks = new ConcurrentHashMap();

    private final Map exemptions = new ConcurrentHashMap();

    private final Cache pistonPushes;

    private final Cache slimePushes;

    private final boolean floodgateAvailable;

    private final Map pendingTeleportIds;

    private final Map teleportOrigins = new ConcurrentHashMap();

    private long tick = 0L;

    public PacketDispatcher(AAC plugin) {
        this.plugin = plugin;
        AACListener aACListener = new AACListener(this);
        plugin.getServer().getPluginManager().registerEvents(aACListener, plugin);
        ProtocolLibrary.getProtocolManager().addPacketListener(aACListener);
        ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
        ProtocolVersion version = ProtocolVersion.V1_9;
        this.pendingTeleportIds = serverVersion.isAtLeast(version) ? new ConcurrentHashMap() : null;
        long value = (long) (plugin.getConfig().getDouble("checks.move.piston_wait_time") * 1000.0);
        this.pistonPushes = CacheBuilder.newBuilder().expireAfterWrite(value, TimeUnit.MILLISECONDS).build();
        this.slimePushes = CacheBuilder.newBuilder().expireAfterWrite(value, TimeUnit.MILLISECONDS).build();
        Plugin plugin2 = Bukkit.getPluginManager().getPlugin("floodgate");
        if (this.floodgateAvailable = plugin2 != null) {
            Bukkit.getLogger().info("[AAC5] Detected floodgate " + plugin2.getDescription().getVersion());
        }
    }

    public AAC getPlugin() {
        return this.plugin;
    }

    public void addExemption(Player player, AACExemption aACExemption) {
        ((Set) this.exemptions.get(player.getUniqueId())).add(aACExemption);
    }

    public void removeExemption(Player player, AACExemption aACExemption) {
        ((Set) this.exemptions.get(player.getUniqueId())).remove(aACExemption);
    }

    public boolean shouldCheck(Player player) {
        Set values = (Set) this.exemptions.get(player.getUniqueId());
        if (values == null) {
            this.initializePlayer(player);
            values = (Set) this.exemptions.get(player.getUniqueId());
        }
        if (player.getGameMode() == GameMode.CREATIVE) {
            values.add(CREATIVE_MODE);
        } else {
            values.remove(CREATIVE_MODE);
        }
        if (player.getGameMode() == GameMode.SPECTATOR) {
            values.add(SPECTATOR_MODE);
        } else {
            values.remove(SPECTATOR_MODE);
        }
        if (player.isSleeping()) {
            values.add(SLEEPING);
        } else {
            values.remove(SLEEPING);
        }
        if (player.isDead()) {
            values.add(DEAD_PLAYER);
        } else {
            values.remove(DEAD_PLAYER);
        }
        if (!values.isEmpty()) {
            if (this.playerChecks.remove(player.getUniqueId()) != null && this.getPlugin().getDebugManager().isDebugging(player)) {
                this.getPlugin().getDebugManager().log(player, "dispatch: now exempt");
            }
            return false;
        } else {
            if (!this.playerChecks.containsKey(player.getUniqueId())) {
                if (this.getPlugin().getDebugManager().isDebugging(player)) {
                    this.getPlugin().getDebugManager().log(player, "dispatch: now checking");
                }
                Map entries = this.playerChecks;
                UUID uniqueId = player.getUniqueId();
                AAC aacPlugin = this.plugin;
                entries.put(uniqueId, new PlayerChecks(aacPlugin, player));
            }
            return true;
        }
    }

    public Set<AACExemption> getExemptions(Player player) {
        return Collections.unmodifiableSet((Set) this.exemptions.get(player.getUniqueId()));
    }

    public void tick() {
        this.tick++;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.shouldCheck(player)) {
                PlayerChecks playerChecks2 = (PlayerChecks) this.playerChecks.get(player.getUniqueId());
                long value = this.tick;
                playerChecks2.updateTime(value);
            }
        }
    }

    public void handleEvent(Player player, Event event) {
        UUID uniqueId = player.getUniqueId();
        if (this.playerChecks.containsKey(uniqueId) && ((Set) this.exemptions.get(uniqueId)).isEmpty()) {
            ((PlayerChecks) this.playerChecks.get(player.getUniqueId())).onEvent(event);
        }
    }

    public void handlePacket(PacketEvent event) {
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();
        PacketContainer packet = event.getPacket();
        PacketType type = packet.getType();
        if (this.pendingTeleportIds != null && type == PacketReader.TELEPORT_ACCEPT) {
            int value = (Integer) packet.getIntegers().read(0);
            boolean matches = this.pendingTeleportIds.remove(uniqueId, value);
            DebugManager debugManager = this.getPlugin().getDebugManager();
            if (debugManager.isDebugging(player)) {
                this.getPlugin().getDebugManager().log(player, "dispatch: confirmed teleport " + value + ": " + matches);
            }
        } else if (PacketReader.checkPacketReader(type) || type == PacketReader.VEHICLE_MOVE) {
            Location location = (Location) this.teleportOrigins.get(uniqueId);
            if (location != null) {
                Location location2 = null;
                if (type == Client.POSITION_LOOK) {
                    World world = location.getWorld();
                    double value2 = (Double) packet.getDoubles().read(0);
                    double value3 = (Double) packet.getDoubles().read(1);
                    double value4 = (Double) packet.getDoubles().read(2);
                    float value5 = (Float) packet.getFloat().read(0) % 360.0F;
                    float value6 = (Float) packet.getFloat().read(1);
                    float value7 = -90.0F;
                    float value8 = value6;
                    location2 = new Location(world, value2, value3, value4, value5, MinecraftMath.clamp(value8, value7, (float) 90.0F));
                    if (this.pendingTeleportIds != null && !this.pendingTeleportIds.containsKey(uniqueId) || this.pendingTeleportIds == null && location2.distanceSquared(location) == 0.0) {
                        if (this.getPlugin().getDebugManager().isDebugging(player)) {
                            this.getPlugin().getDebugManager().updatePlayer(player, "dispatch: accepted teleport: %.4f %.4f %.4f (pos: %.4f %.4f %.4f)", new Object[] { location2.getX(), location2.getY(), location2.getZ(), location.getX(), location.getY(), location.getZ() });
                        }
                        this.teleportOrigins.remove(uniqueId);
                        PlayerChecks playerChecks2 = (PlayerChecks) this.playerChecks.get(uniqueId);
                        if (playerChecks2 != null) {
                            playerChecks2.updateLocationWithMovementCheck(location);
                        }
                        return;
                    }
                }
                if (this.teleportOrigins.containsKey(uniqueId)) {
                    if (this.getPlugin().getDebugManager().isDebugging(player)) {
                        String text = this.pendingTeleportIds != null ? "unconfirmed" : "unresolved";
                        if (location2 != null) {
                            this.getPlugin().getDebugManager().updatePlayer(event.getPlayer(), "dispatch: " + text + " teleport: %.4f,%.4f,%.4f (pos: %.4f %.4f %.4f)", new Object[] { location2.getX(), location2.getY(), location2.getZ(), location.getX(), location.getY(), location.getZ() });
                        }
                    }
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (type == Server.POSITION) {
            Location location3 = player.getLocation();
            this.teleportOrigins.put(uniqueId, location3);
            PlayerChecks playerChecks3 = (PlayerChecks) this.playerChecks.get(uniqueId);
            if (playerChecks3 != null) {
                playerChecks3.updateLocation(location3);
            }
            Object result;
            if (this.pendingTeleportIds != null) {
                result = (Integer) packet.getIntegers().read(0);
                this.pendingTeleportIds.put(event.getPlayer().getUniqueId(), result);
            } else {
                result = null;
            }
            DebugManager debugManager2 = this.getPlugin().getDebugManager();
            Player player2 = event.getPlayer();
            Player player3 = player2;
            if (debugManager2.isDebugging(player3)) {
                assert location3.getWorld() != null;
                this.getPlugin().getDebugManager().updatePlayer(event.getPlayer(), "dispatch: teleported to " + location3.getWorld().getName() + ": %.4f,%.4f,%.4f " + (result != null ? result : ""), new Object[] { location3.getX(), location3.getY(), location3.getZ() });
            }
        }
        if (this.playerChecks.containsKey(uniqueId) && ((Set) this.exemptions.get(uniqueId)).isEmpty()) {
            ((PlayerChecks) this.playerChecks.get(event.getPlayer().getUniqueId())).handlePacket(event);
        }
    }

    public Cache getPistonPushes() {
        return this.pistonPushes;
    }

    public Cache getSlimePushes() {
        return this.slimePushes;
    }

    public void initializePlayer(Player player) {
        this.exemptions.put(player.getUniqueId(), ConcurrentHashMap.newKeySet());
        if (player.hasPermission("aac.bypass")) {
            ((Set) this.exemptions.get(player.getUniqueId())).add(BYPASS_PERMISSION);
        }
        if (this.floodgateAvailable && OptionalIntegrations.isFloodgatePlayer(player.getUniqueId())) {
            ((Set) this.exemptions.get(player.getUniqueId())).add(BEDROCK_CLIENT);
        }
    }

    public void removePlayer(Player player) {
        this.exemptions.remove(player.getUniqueId());
        this.playerChecks.remove(player.getUniqueId());
    }
}
