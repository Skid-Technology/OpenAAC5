package me.konsolas.aac.checks.interaction;

import com.comphenix.protocol.events.PacketContainer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.checks.BaseCheck;
import me.konsolas.aac.checks.ViolationBuffer;
import me.konsolas.aac.compat.NativeAccess;
import me.konsolas.aac.compat.NativeAccessProvider;
import me.konsolas.aac.debug.DebugManager;
import me.konsolas.aac.movement.PlayerPose;
import me.konsolas.aac.protocol.PacketReader;
import me.konsolas.aac.util.collection.FixedSizeList;
import me.konsolas.aac.world.BlockRayTraceResult;
import me.konsolas.aac.world.MaterialAlias;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class InteractCheck extends BaseCheck {

    private final ViolationBuffer interactionBuffer;

    private final double maximumAngleDifference;

    private final Set exemptMaterials;

    private final FixedSizeList interactionHistory;

    private static final double[] angleSamples = new double[] { 0.0, 0.5, 1.0 };

    private static final Vector[] lookDirections;

    private static final Set interactedBlocks;

    public InteractCheck(AAC aacPlugin, Player player) {
        super(aacPlugin, player, ("interact"));
        this.exemptMaterials = EnumSet.noneOf(Material.class);
        this.interactionHistory = new FixedSizeList(5);
        ConfigurationSection configuration = this.getConfiguration();
        ConfigurationSection section = configuration;
        this.interactionBuffer = ViolationBuffer.create(section, (String) "vl");
        this.maximumAngleDifference = this.getConfiguration().getDouble("max_angle_diff");
        for (String text : this.getConfiguration().getStringList("exempt")) {
            Material material = Material.matchMaterial(text);
            this.exemptMaterials.add(material);
        }
    }

    public void handlePacket(PacketContainer packet) {
        Location location = (Location) this.interactionHistory.latest();
        if (location == null || location.getWorld() != this.getPlayer().getWorld()) {
            if (location != null) {
                this.interactionHistory.advance();
            }
            location = this.getPlayer().getLocation();
        }
        Location location2 = PacketReader.findLocation(packet, location);
        if (this.getPlayer().getVehicle() != null) {
            Location location3 = this.getPlayer().getLocation();
            location2.setX(location3.getX());
            location2.setY(location3.getY());
            location2.setZ(location3.getZ());
        }
        this.interactionHistory.add(location2);
    }

    public boolean checkInteract(Block block) {
        if (!this.exemptMaterials.contains(block.getType())) {
            if (this.checkInteract(block, (BlockFace) (Object) null)) {
                if (this.interactionBuffer.checkViolationBuffer((float) 1.0F)) {
                    FeatureType feature = FeatureType.AAC_INTERACT_BREAK;
                    this.recordFeature(feature, (float) 1.0F);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean onPlayerInteractEvent(PlayerInteractEvent playerInteractEvent) {
        if (playerInteractEvent.getAction() == Action.RIGHT_CLICK_BLOCK && playerInteractEvent.getClickedBlock() != null && !this.exemptMaterials.contains(playerInteractEvent.getClickedBlock().getType())) {
            if (playerInteractEvent.getClickedBlock().isLiquid() && playerInteractEvent.getItem() != null && interactedBlocks.contains(playerInteractEvent.getItem().getType())) {
                DebugManager debugManager = this.getPlugin().getDebugManager();
                Player player = this.getPlayer();
                Player player2 = player;
                if (debugManager.isDebugging(player2)) {
                    this.getPlugin().getDebugManager().log(this.getPlayer(), "interact: " + playerInteractEvent.getItem().getType() + " on " + playerInteractEvent.getClickedBlock().getType() + ": allowed");
                }
                return false;
            }
            BlockFace blockFace = playerInteractEvent.getBlockFace();
            Block clickedBlock = playerInteractEvent.getClickedBlock();
            if (this.checkInteract(clickedBlock, blockFace)) {
                if (this.interactionBuffer.checkViolationBuffer((float) 1.0F)) {
                    FeatureType feature = playerInteractEvent.isBlockInHand() ? FeatureType.AAC_INTERACT_PLACE : FeatureType.AAC_INTERACT_GEN;
                    this.recordFeature(feature, (float) 1.0F);
                    return true;
                }
            }
        }
        return false;
    }

    private static float calculateValue(Vector vector, Vector vector2) {
        Vector vector3 = vector.clone().setY(0);
        Vector vector4 = vector2.clone().setY(0);
        double value = vector3.dot(vector4) / (vector3.length() * vector4.length());
        value = Math.min(Math.max(value, -1.0), 1.0);
        double value2 = Math.acos(value);
        value = Math.sqrt(vector.getX() * vector.getX() + vector.getZ() * vector.getZ());
        double value3 = Math.sqrt(vector2.getX() * vector2.getX() + vector2.getZ() * vector2.getZ());
        double value4 = Math.abs(Math.atan(-vector.getY() / value) - Math.atan(-vector2.getY() / value3));
        return (float) Math.sqrt(value2 * value2 + value4 * value4);
    }

    private boolean checkInteract(Block block, BlockFace blockFace) {
        Location location = block.getLocation();
        BlockVector blockVector = location.toVector().toBlockVector();
        for (PlayerPose playerPose : PlayerPose.POSES) {
            AAC plugin = this.getPlugin();
            Player player = this.getPlayer();
            Player player2 = player;
            Vector vector = new Vector(0.0F, playerPose.calculateValue(plugin.getProtocolVersion(player2)), 0.0F);
            if (!this.checkInteract(block, blockFace, blockVector, vector)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkInteract(Block block, BlockFace blockFace, BlockVector blockVector, Vector vector) {
        if (this.interactionHistory.isEmpty()) {
            DebugManager debugManager = this.getPlugin().getDebugManager();
            Player player = this.getPlayer();
            Player player2 = player;
            if (debugManager.isDebugging(player2)) {
                this.getPlugin().getDebugManager().log(this.getPlayer(), "interact: warn: no pos history: " + block.getType() + ":" + blockFace + " at " + blockVector);
            }
            return false;
        } else {
            Collection<Vector> values = this.findCollection(blockFace, blockVector, vector);
            if (values.isEmpty()) {
                DebugManager debugManager2 = this.getPlugin().getDebugManager();
                Player player3 = this.getPlayer();
                Player player4 = player3;
                if (debugManager2.isDebugging(player4)) {
                    this.getPlugin().getDebugManager().log(this.getPlayer(), "interact: occluded:" + block.getType() + ":" + blockFace + " at " + blockVector);
                }
                return true;
            } else {
                float value = Float.POSITIVE_INFINITY;
                for (Vector vector2 : values) {
                    for (Location location : (Iterable<Location>) (Iterable<?>) (this.interactionHistory)) {
                        if (location != null) {
                            float value2 = calculateValue(location.getDirection(), vector2);
                            if (value2 < value) {
                                value = value2;
                            }
                        }
                    }
                }
                DebugManager debugManager3 = this.getPlugin().getDebugManager();
                Player player5 = this.getPlayer();
                Player player6 = player5;
                if (debugManager3.isDebugging(player6)) {
                    this.getPlugin().getDebugManager().updatePlayer(this.getPlayer(), "interact: angle: %.3f rad " + block.getType() + ":" + blockFace + " at " + blockVector, new Object[] { value });
                }
                return value > this.maximumAngleDifference;
            }
        }
    }

    private Collection findCollection(BlockFace blockFace, BlockVector blockVector, Vector vector) {
        ArrayList values = new ArrayList();
        for (Location location : (Iterable<Location>) (Iterable<?>) (this.interactionHistory)) {
            if (location != null) {
                Vector vector2 = location.toVector().add(vector);
                for (Location location2 : (Iterable<Location>) (Iterable<?>) (this.interactionHistory)) {
                    if (location2 != null) {
                        Vector direction = location2.getDirection();
                        NativeAccess instance = NativeAccessProvider.getInstance();
                        Player player = this.getPlayer();
                        BlockRayTraceResult blockRayTraceResult = instance.rayTrace(player, vector2, direction, 5.0);
                        if (blockRayTraceResult.isBlockRayTraceResult() && blockRayTraceResult.getBlockVector().equals(blockVector) && (blockFace == null || blockRayTraceResult.getFace() == blockFace)) {
                            values.add(direction);
                            break;
                        }
                    }
                }
            }
        }
        if (!values.isEmpty()) {
            DebugManager debugManager = this.getPlugin().getDebugManager();
            Player player2 = this.getPlayer();
            Player player3 = player2;
            if (debugManager.isDebugging(player3)) {
                this.getPlugin().getDebugManager().log(this.getPlayer(), "interact: exact: " + values.iterator().next());
            }
            return values;
        } else {
            for (Location location3 : (Iterable<Location>) (Iterable<?>) (this.interactionHistory)) {
                if (location3 != null) {
                    Vector vector3 = location3.toVector().add(vector);
                    for (Vector vector4 : lookDirections) {
                        Vector vector5 = vector4.clone().add(blockVector).subtract(vector3);
                        if (vector5.lengthSquared() > 0.0) {
                            NativeAccess instance2 = NativeAccessProvider.getInstance();
                            Player player4 = this.getPlayer();
                            BlockRayTraceResult blockRayTraceResult2 = instance2.rayTrace(player4, vector3, vector5, 5.0);
                            if (blockRayTraceResult2.isBlockRayTraceResult() && blockRayTraceResult2.getBlockVector().equals(blockVector) && (blockFace == null || blockRayTraceResult2.getFace() == blockFace)) {
                                values.add(vector5);
                            }
                        }
                    }
                }
            }
            DebugManager debugManager2 = this.getPlugin().getDebugManager();
            Player player5 = this.getPlayer();
            Player player6 = player5;
            if (debugManager2.isDebugging(player6)) {
                this.getPlugin().getDebugManager().log(this.getPlayer(), "interact: approx: " + values.size());
            }
            return values;
        }
    }

    static {
        ArrayList<Vector> values2 = new ArrayList();
        for (double value : angleSamples) {
            for (double value2 : angleSamples) {
                for (double value3 : angleSamples) {
                    values2.add(new Vector(value, value2, value3));
                }
            }
        }
        lookDirections = values2.toArray(new Vector[0]);
        interactedBlocks = EnumSet.of(MaterialAlias.LILY_PAD.getMaterial());
        for (Material material : Material.values()) {
            if (material.name().contains("BOAT")) {
                interactedBlocks.add(material);
            }
        }
    }
}
