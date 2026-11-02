package me.konsolas.aac.checks.combat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.checks.BaseCheck;
import me.konsolas.aac.checks.ViolationBuffer;
import me.konsolas.aac.compat.NativeAccess;
import me.konsolas.aac.compat.NativeAccessProvider;
import me.konsolas.aac.debug.DebugManager;
import me.konsolas.aac.movement.PlayerPose;
import me.konsolas.aac.protocol.PacketReader;
import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.timings.EntityTracker;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import me.konsolas.aac.util.time.ExpiringCounter;
import me.konsolas.aac.world.BlockRayTraceResult;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class HitboxCheck extends BaseCheck {

    private static final Vector previousEyePosition = new Vector(0.1, 0.1, 0.1);

    private static final Vector eyePosition = new Vector(0.13, 0.13, 0.13);

    private static final Vector lookDirection = new Vector(0.5, 0.5, 0.5);

    private final BlockingQueue<TeleportState> pendingAttacks;

    private Location lastLocation;

    private final ExpiringCounter attackTimer;

    private final ViolationBuffer hitboxBuffer;

    private final boolean checkBlocks;

    public HitboxCheck(AAC aacPlugin, Player player) {
        super(aacPlugin, player, ("hitbox"));
        this.pendingAttacks = new ArrayBlockingQueue(10);
        this.attackTimer = new ExpiringCounter(100L);
        this.checkBlocks = this.getConfiguration().getBoolean("check_blocks");
        ConfigurationSection configuration = this.getConfiguration();
        ConfigurationSection section = configuration;
        this.hitboxBuffer = ViolationBuffer.create(section, (String) "vl");
    }

    public void handlePacket(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        PacketType type = packet.getType();
        if (PacketReader.checkPacketReader(type)) {
            if (this.lastLocation == null || this.lastLocation.getWorld() != this.getPlayer().getWorld()) {
                this.lastLocation = this.getPlayer().getLocation();
            }
            Location location = this.lastLocation;
            this.lastLocation = PacketReader.findLocation(packet, location);
            if (this.getPlayer().getVehicle() != null) {
                Location location2 = this.getPlayer().getLocation();
                this.lastLocation.setX(location2.getX());
                this.lastLocation.setY(location2.getY());
                this.lastLocation.setZ(location2.getZ());
            }
            for (TeleportState teleportState : this.pendingAttacks) {
                Location location3 = this.lastLocation;
                TeleportState.updateTeleportState(teleportState, location3);
            }
        } else if (PacketReader.checkPacketReader(packet)) {
            int value2 = (Integer) packet.getIntegers().read(0);
            if (value2 < 0) {
                DebugManager debugManager = this.getPlugin().getDebugManager();
                Player player = this.getPlayer();
                Player player2 = player;
                if (debugManager.isDebugging(player2)) {
                    this.getPlugin().getDebugManager().log(this.getPlayer(), "hitbox: ignoring invalid eid " + value2);
                }
                return;
            }
            if (!this.pendingAttacks.offer(new TeleportState(value2, this.lastLocation))) {
                this.hitboxBuffer.checkViolationBuffer((float) 1.0F);
                FeatureType feature = FeatureType.AAC_HITBOX_COUNT;
                this.recordFeature(feature, (float) 1.0F);
            }
            if (this.hitboxBuffer.checkViolationBuffer((float) 0.0F)) {
                event.setCancelled(true);
            }
        }
    }

    private RayTraceCandidate findRayTraceCandidate(Location location, Iterable<AxisAlignedBoundingBox> iterable, Vector vector) {
        Vector direction = location.getDirection();
        Vector vector2 = location.toVector();
        double value;
        BlockRayTraceResult blockRayTraceResult;
        if (this.checkBlocks) {
            NativeAccess instance = NativeAccessProvider.getInstance();
            Player player = this.getPlayer();
            blockRayTraceResult = instance.rayTrace(player, vector2, direction, 4.5);
            value = blockRayTraceResult.isBlockRayTraceResult() ? blockRayTraceResult.getVector().distance(vector2) : Double.POSITIVE_INFINITY;
        } else {
            blockRayTraceResult = null;
            value = Double.POSITIVE_INFINITY;
        }
        double value2 = Double.POSITIVE_INFINITY;
        for (AxisAlignedBoundingBox box : iterable) {
            if (box != null) {
                double value3 = box.expand(vector).rayIntersectionDistance(vector2, direction);
                if (value3 < value2) {
                    value2 = value3;
                    if (value3 < Math.min(3.0, value)) {
                        break;
                    }
                }
            }
        }
        return new RayTraceCandidate(value2, value, blockRayTraceResult);
    }

    public void tick() {
        if (!this.pendingAttacks.isEmpty()) {
            ArrayList<TeleportState> values = new ArrayList();
            while (!this.pendingAttacks.isEmpty() && (TeleportState.findLocation((TeleportState) this.pendingAttacks.element()) != null || TeleportState.checkTeleportState((TeleportState) this.pendingAttacks.element()))) {
                values.add((TeleportState) this.pendingAttacks.poll());
            }
            for (TeleportState teleportState : this.pendingAttacks) {
                TeleportState.checkTeleportState(teleportState, true);
            }
            AAC plugin = this.getPlugin();
            Player player = this.getPlayer();
            Player player2 = player;
            ProtocolVersion version = plugin.getProtocolVersion(player2);
            ArrayList<Location> values2 = new ArrayList();
            for (TeleportState teleportState2 : values) {
                if (TeleportState.findLocationWithOrigin(teleportState2) != null) {
                    Vector vector = this.getPlayer().getVehicle() != null ? lookDirection : (TeleportState.findLocation(teleportState2) == null ? eyePosition : previousEyePosition);
                    Entity entityFromID = ProtocolLibrary.getProtocolManager().getEntityFromID(this.getPlayer().getWorld(), TeleportState.calculateCount(teleportState2));
                    if (entityFromID instanceof LivingEntity && !(entityFromID instanceof Enderman)) {
                        EntityTracker entityTracker = this.getPlugin().getEntityTracker();
                        LivingEntity livingEntity = (LivingEntity) entityFromID;
                        entityTracker.updateLivingEntity(livingEntity);
                        EntityTracker entityTracker2 = this.getPlugin().getEntityTracker();
                        LivingEntity livingEntity2 = (LivingEntity) entityFromID;
                        List values3 = entityTracker2.findList(livingEntity2);
                        if (values3 != null) {
                            values2.clear();
                            values2.add(TeleportState.findLocationWithOrigin(teleportState2));
                            if (TeleportState.findLocation(teleportState2) != null) {
                                values2.add(TeleportState.findLocation(teleportState2));
                                values2.add(TeleportState.findLocation(teleportState2).clone().setDirection(TeleportState.findLocationWithOrigin(teleportState2).getDirection()));
                                values2.add(TeleportState.findLocationWithOrigin(teleportState2).clone().setDirection(TeleportState.findLocation(teleportState2).getDirection()));
                            }
                            RayTraceCandidate rayTraceCandidate = null;
                            for (PlayerPose playerPose : PlayerPose.POSES) {
                                double value = playerPose.calculateValue(version);
                                for (Location location : values2) {
                                    location = location.clone().add(0.0, value, 0.0);
                                    RayTraceCandidate rayTraceCandidate2 = this.findRayTraceCandidate(location, values3, vector);
                                    if (RayTraceCandidate.checkRayTraceCandidate(rayTraceCandidate2, rayTraceCandidate)) {
                                        rayTraceCandidate = rayTraceCandidate2;
                                    }
                                }
                            }
                            assert rayTraceCandidate != null;
                            if (!Double.isFinite(RayTraceCandidate.calculateValueWithDistance(rayTraceCandidate))) {
                                float value2 = RayTraceCandidate.calculateValue(rayTraceCandidate);
                                DebugManager debugManager = this.getPlugin().getDebugManager();
                                player = this.getPlayer();
                                Player player3 = player;
                                if (debugManager.isDebugging(player3)) {
                                    this.getPlugin().getDebugManager().updatePlayer(this.getPlayer(), "hitbox: no intersection: %.3f%+.3f", new Object[] { this.hitboxBuffer.getLevel(), value2 });
                                }
                                this.hitboxBuffer.checkViolationBuffer(value2);
                                if (this.attackTimer.tryAcquire()) {
                                    FeatureType feature = FeatureType.AAC_HITBOX_MISS;
                                    this.recordFeature(feature, value2);
                                }
                            } else if (RayTraceCandidate.calculateValueWithDistance(rayTraceCandidate) > RayTraceCandidate.calculateValueWithTolerance(rayTraceCandidate)) {
                                float value3 = RayTraceCandidate.calculateValue(rayTraceCandidate);
                                DebugManager debugManager2 = this.getPlugin().getDebugManager();
                                player = this.getPlayer();
                                Player player4 = player;
                                if (debugManager2.isDebugging(player4)) {
                                    assert RayTraceCandidate.findBlockRayTraceResult(rayTraceCandidate) != null;
                                    this.getPlugin().getDebugManager().updatePlayer(this.getPlayer(), "hitbox: through block: %s (%.3f > %.3f): %.3f%+.3f", new Object[] { RayTraceCandidate.findBlockRayTraceResult(rayTraceCandidate).getBlockVector().toLocation(this.getPlayer().getWorld()).getBlock().getType(), RayTraceCandidate.calculateValueWithDistance(rayTraceCandidate), RayTraceCandidate.calculateValueWithTolerance(rayTraceCandidate), this.hitboxBuffer.getLevel(), value3 });
                                }
                                this.hitboxBuffer.checkViolationBuffer(value3);
                                if (this.attackTimer.tryAcquire()) {
                                    FeatureType feature2 = FeatureType.AAC_HITBOX_WALLS;
                                    this.recordFeature(feature2, value3);
                                }
                            } else {
                                float value4 = RayTraceCandidate.calculateValue(rayTraceCandidate);
                                DebugManager debugManager3 = this.getPlugin().getDebugManager();
                                player = this.getPlayer();
                                Player player5 = player;
                                if (debugManager3.isDebugging(player5)) {
                                    this.getPlugin().getDebugManager().updatePlayer(this.getPlayer(), "hitbox: normal: %.3f: %.3f%+.3f", new Object[] { RayTraceCandidate.calculateValueWithDistance(rayTraceCandidate), this.hitboxBuffer.getLevel(), value4 });
                                }
                                this.hitboxBuffer.checkViolationBuffer(value4);
                                if (this.attackTimer.tryAcquire()) {
                                    FeatureType feature3 = FeatureType.AAC_HITBOX_REACH;
                                    this.recordFeature(feature3, value4);
                                }
                            }
                        } else {
                            DebugManager debugManager4 = this.getPlugin().getDebugManager();
                            player = this.getPlayer();
                            Player player6 = player;
                            if (debugManager4.isDebugging(player6)) {
                                this.getPlugin().getDebugManager().log(this.getPlayer(), "hitbox: no data for eid " + TeleportState.calculateCount(teleportState2) + " in " + Objects.requireNonNull(TeleportState.findLocationWithOrigin(teleportState2).getWorld()).getName());
                            }
                        }
                    }
                }
            }
        }
    }
}
