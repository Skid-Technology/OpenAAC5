package me.konsolas.aac.movement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;
import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.util.collection.BlockGrid;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import me.konsolas.aac.world.BlockSnapshot;
import me.konsolas.aac.world.BlockSnapshotCache;
import me.konsolas.aac.world.MaterialAlias;
import me.konsolas.aac.world.fluid.FluidContact;
import me.konsolas.aac.world.fluid.FluidType;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

public class MovementEnvironment {

    private static final Set CLIMBABLE_MATERIALS = EnumSet.noneOf(Material.class);

    private final ProtocolVersion protocolVersion;

    private final AxisAlignedBoundingBox boundingBox;

    private final int minX;

    private final int minY;

    private final int minZ;

    private final int maxX;

    private final int maxY;

    private final int maxZ;

    private final BlockGrid blocks;

    public MovementEnvironment(ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
        this.boundingBox = new AxisAlignedBoundingBox(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        this.minX = this.minY = this.minZ = 0;
        this.maxX = this.maxY = this.maxZ = 0;
        this.blocks = new BlockGrid(0, 0, 0);
    }

    public MovementEnvironment(Player player, AxisAlignedBoundingBox box, ProtocolVersion protocolVersion, MovementEnvironment movementEnvironment) {
        this.protocolVersion = protocolVersion;
        this.boundingBox = box.alignToBlocks();
        World world = player.getWorld();
        this.minX = NumberConversions.floor(box.getMinX());
        this.minY = NumberConversions.floor(box.getMinY());
        this.minZ = NumberConversions.floor(box.getMinZ());
        this.maxX = NumberConversions.ceil(box.getMaxX());
        this.maxY = NumberConversions.ceil(box.getMaxY());
        this.maxZ = NumberConversions.ceil(box.getMaxZ());
        this.blocks = new BlockGrid(this.maxX - this.minX, this.maxY - this.minY, this.maxZ - this.minZ);
        for (int value = this.minX; value < this.maxX; value++) {
            for (int value2 = this.minY; value2 < this.maxY; value2++) {
                for (int value3 = this.minZ; value3 < this.maxZ; value3++) {
                    BlockSnapshot blockSnapshot;
                    if (movementEnvironment != null && movementEnvironment.containsBlock(value, value2, value3)) {
                        blockSnapshot = movementEnvironment.getBlockSnapshot(value, value2, value3);
                    } else {
                        Block blockAt = world.getBlockAt(value, value2, value3);
                        blockSnapshot = BlockSnapshotCache.findBlockSnapshot(player, blockAt, protocolVersion);
                    }
                    this.blocks.set(value - this.minX, value2 - this.minY, value3 - this.minZ, blockSnapshot);
                }
            }
        }
    }

    public void invalidateBlock(int value, int value2, int value3) {
        if (this.containsBlock(value, value2, value3)) {
            this.blocks.set(value - this.minX, value2 - this.minY, value3 - this.minZ, BlockSnapshotCache.AIR);
        }
    }

    public ProtocolVersion getProtocolVersion() {
        return this.protocolVersion;
    }

    public AxisAlignedBoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public boolean containsBlock(int value, int value2, int value3) {
        return value >= this.minX && value < this.maxX && value2 >= this.minY && value2 < this.maxY && value3 >= this.minZ && value3 < this.maxZ;
    }

    private BlockSnapshot getBlockSnapshot(int value, int value2, int value3) {
        return value >= this.minX && value < this.maxX && value2 >= this.minY && value2 < this.maxY && value3 >= this.minZ && value3 < this.maxZ ? (BlockSnapshot) this.blocks.get(value - this.minX, value2 - this.minY, value3 - this.minZ) : BlockSnapshotCache.AIR;
    }

    private BlockSnapshot findBlockSnapshot(double value, double value2, double value3) {
        int value4 = NumberConversions.floor(value);
        int value5 = NumberConversions.floor(value2);
        int value6 = NumberConversions.floor(value3);
        int value7 = value6;
        int value8 = value5;
        int value9 = value4;
        return this.getBlockSnapshot(value9, value8, value7);
    }

    public Material getMaterial(double value, double value2, double value3) {
        return this.findBlockSnapshot(value, value2, value3).getMaterial();
    }

    public FluidType getFluidType(double value, double value2, double value3) {
        return this.findBlockSnapshot(value, value2, value3).getFluidType();
    }

    public Collection<AxisAlignedBoundingBox> getCollisionBoxes(AxisAlignedBoundingBox box) {
        ArrayList values = new ArrayList();
        int value = Math.max(this.minX, NumberConversions.floor(box.getMinX()));
        int value2 = Math.max(this.minY, NumberConversions.floor(box.getMinY() - 1.0));
        int value3 = Math.max(this.minZ, NumberConversions.floor(box.getMinZ()));
        int value4 = Math.min(this.maxX, NumberConversions.ceil(box.getMaxX()));
        int value5 = Math.min(this.maxY, NumberConversions.ceil(box.getMaxY()));
        int value6 = Math.min(this.maxZ, NumberConversions.ceil(box.getMaxZ()));
        for (int value7 = value; value7 < value4; value7++) {
            for (int value8 = value2; value8 < value5; value8++) {
                for (int value9 = value3; value9 < value6; value9++) {
                    BlockSnapshot blockSnapshot = (BlockSnapshot) this.blocks.get(value7 - this.minX, value8 - this.minY, value9 - this.minZ);
                    ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
                    ProtocolVersion version = ProtocolVersion.V1_14;
                    if (!serverVersion.isAtLeast(version) || blockSnapshot.getMaterial() != Material.SCAFFOLDING) {
                        for (AxisAlignedBoundingBox box2 : blockSnapshot.getCollisionShape().getBoxesAt(value7, value8, value9)) {
                            if (box2.intersects(box)) {
                                values.add(box2);
                            }
                        }
                    }
                }
            }
        }
        return values;
    }

    public boolean checkMovementEnvironment(AxisAlignedBoundingBox box, double value, double value2, double value3) {
        int value4 = Math.max(this.minX, NumberConversions.floor(box.getMinX() + value));
        int value5 = Math.max(this.minY, NumberConversions.floor(box.getMinY() + value2));
        int value6 = Math.max(this.minZ, NumberConversions.floor(box.getMinZ() + value3));
        int value7 = Math.min(this.maxX, NumberConversions.ceil(box.getMaxX() + value));
        int value8 = Math.min(this.maxY, NumberConversions.ceil(box.getMaxY() + value2));
        int value9 = Math.min(this.maxZ, NumberConversions.ceil(box.getMaxZ() + value3));
        for (int value10 = value4; value10 < value7; value10++) {
            for (int value11 = value5; value11 < value8; value11++) {
                for (int value12 = value6; value12 < value9; value12++) {
                    for (AxisAlignedBoundingBox box2 : ((BlockSnapshot) this.blocks.get(value10 - this.minX, value11 - this.minY, value12 - this.minZ)).getCollisionShape().getBoxes()) {
                        double value13 = value - value10;
                        double value14 = value2 - value11;
                        double value15 = value3 - value12;
                        double value16 = value14;
                        double value17 = value13;
                        if (box2.intersectsOffset(box, value17, value16, value15)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean collides(AxisAlignedBoundingBox box) {
        return !this.checkMovementEnvironment(box, (double) 0.0, (double) 0.0, (double) 0.0);
    }

    public float getBlockFriction(double value, double value2, double value3) {
        double value4 = value2 - 1.0;
        double value5 = value4;
        return this.findBlockSnapshot(value, value5, value3).getFriction();
    }

    public boolean checkMovementEnvironment(double value, double value2, double value3) {
        BlockSnapshot blockSnapshot = this.findBlockSnapshot(value, value2, value3);
        Material material = blockSnapshot.getMaterial();
        if (!CLIMBABLE_MATERIALS.contains(material)) {
            ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
            ProtocolVersion version = ProtocolVersion.V1_14;
            if (!serverVersion.isAtLeast(version) || material != Material.SCAFFOLDING) {
                ProtocolVersion version2 = ProtocolVersion.V1_9;
                if (this.protocolVersion.isAtLeast(version2) && MaterialAlias.TRAPDOORS.contains(material) && blockSnapshot.isWaterlogged()) {
                    double value4 = value2 - 1.0;
                    double value5 = value4;
                    BlockSnapshot blockSnapshot2 = this.findBlockSnapshot(value, value5, value3);
                    return blockSnapshot2.getMaterial() == Material.LADDER && blockSnapshot2.getFacing() == blockSnapshot.getFacing();
                }
                return false;
            }
        }
        return true;
    }

    public boolean evaluateCheckMovementEnvironment(AxisAlignedBoundingBox box) {
        double minY = box.getMinY();
        AxisAlignedBoundingBox box2 = box.offset(0.0, -0.001, 0.0);
        for (AxisAlignedBoundingBox box3 : this.getCollisionBoxes(box2)) {
            if (box3.getMaxY() == minY && box3.intersects(box2)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkMovementEnvironment(AxisAlignedBoundingBox box) {
        AxisAlignedBoundingBox box2 = box.expand(0.001, 0.0, 0.001);
        for (AxisAlignedBoundingBox box3 : this.getCollisionBoxes(box2)) {
            if (box3.getMaxX() == box.getMinX() || box3.getMinX() == box.getMaxX() || box3.getMaxZ() == box.getMinZ() || box3.getMinZ() == box.getMaxZ() && box3.intersects(box2)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkMovementEnvironment(AxisAlignedBoundingBox box, Predicate predicate) {
        int value = NumberConversions.floor(box.getMinX());
        int value2 = NumberConversions.floor(box.getMinY());
        int value3 = NumberConversions.floor(box.getMinZ());
        int value4 = NumberConversions.ceil(box.getMaxX());
        int value5 = NumberConversions.ceil(box.getMaxY());
        int value6 = NumberConversions.ceil(box.getMaxZ());
        for (int value7 = value; value7 < value4; value7++) {
            for (int value8 = value2; value8 < value5; value8++) {
                for (int value9 = value3; value9 < value6; value9++) {
                    if (predicate.test(this.getBlockSnapshot(value7, value8, value9).getMaterial())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int calculateCount(AxisAlignedBoundingBox box, Predicate predicate) {
        int value = NumberConversions.floor(box.getMinX());
        int value2 = NumberConversions.floor(box.getMinY());
        int value3 = NumberConversions.floor(box.getMinZ());
        int value4 = NumberConversions.ceil(box.getMaxX());
        int value5 = NumberConversions.ceil(box.getMaxY());
        int value6 = NumberConversions.ceil(box.getMaxZ());
        int value7 = 0;
        for (int value8 = value; value8 < value4; value8++) {
            for (int value9 = value2; value9 < value5; value9++) {
                for (int value10 = value3; value10 < value6; value10++) {
                    if (predicate.test(this.getBlockSnapshot(value8, value9, value10).getMaterial())) {
                        value7++;
                    }
                }
            }
        }
        return value7;
    }

    private static float calculateValue(int value) {
        if (value >= 8) {
            value = 0;
        }
        return (value + 1) / 9.0F;
    }

    public FluidContact sampleFluidContact(AxisAlignedBoundingBox box, FluidType fluidType) {
        int value = NumberConversions.floor(box.getMinX());
        int value2 = NumberConversions.floor(box.getMinY());
        int value3 = NumberConversions.floor(box.getMinZ());
        int value4 = NumberConversions.floor(box.getMaxX() + 1.0);
        int value5 = NumberConversions.floor(box.getMaxY() + 1.0);
        int value6 = NumberConversions.floor(box.getMaxZ() + 1.0);
        boolean matches = false;
        double value7 = 0.0;
        boolean matches2 = false;
        ProtocolVersion version = ProtocolVersion.V1_13;
        if (this.protocolVersion.isAtLeast(version)) {
            for (int value8 = value; value8 < value4; value8++) {
                for (int value9 = value2; value9 < value5; value9++) {
                    for (int value10 = value3; value10 < value6; value10++) {
                        BlockSnapshot blockSnapshot = this.getBlockSnapshot(value8, value9, value10);
                        if (blockSnapshot.getFluidType() == fluidType && blockSnapshot.getFluidLevel() != -1) {
                            float value11 = value9 + 1;
                            int fluidLevel = blockSnapshot.getFluidLevel();
                            double value12 = value11 - calculateValue(fluidLevel);
                            if (value12 >= box.getMinY()) {
                                matches = true;
                                value7 = Math.max(value12 - box.getMinY(), value7);
                                if (blockSnapshot.getFluidLevel() != 0) {
                                    matches2 = true;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            for (int value13 = value; value13 < value4; value13++) {
                for (int value14 = value2; value14 < value5; value14++) {
                    for (int value15 = value3; value15 < value6; value15++) {
                        BlockSnapshot blockSnapshot2 = this.getBlockSnapshot(value13, value14, value15);
                        if (blockSnapshot2.getFluidType() == fluidType && blockSnapshot2.getFluidLevel() != -1) {
                            float value16 = value14 + 1;
                            int fluidLevel2 = blockSnapshot2.getFluidLevel();
                            double value17 = value16 - calculateValue(fluidLevel2);
                            if (value5 >= value17) {
                                matches = true;
                                if (blockSnapshot2.getFluidLevel() != 0) {
                                    matches2 = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return new FluidContact(matches, matches2, value7);
    }

    static {
        for (String text : Arrays.asList("LADDER", "VINE", "TWISTING_VINES", "TWISTING_VINES_PLANT", "WEEPING_VINES", "WEEPING_VINES_PLANT")) {
            try {
                CLIMBABLE_MATERIALS.add(Material.valueOf(text));
            } catch (IllegalArgumentException illegalArgumentException) {
            }
        }
    }
}
