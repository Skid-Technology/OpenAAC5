package me.konsolas.aac.world;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import me.konsolas.aac.compat.NativeAccessProvider;
import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.world.collision.BlockCollisionShapes;
import me.konsolas.aac.world.collision.CollisionShape;
import me.konsolas.aac.world.collision.EmptyCollisionShape;
import me.konsolas.aac.world.fluid.FluidType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Ladder;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Player;

public class BlockSnapshot {

    private static final Set AQUATIC_MATERIALS = EnumSet.noneOf(Material.class);

    private final Material material;

    private final float friction;

    private final FluidType fluidType;

    private final int fluidLevel;

    private final BlockFace facing;

    private final boolean waterlogged;

    private final CollisionShape collisionShape;

    BlockSnapshot(Material material, float friction, FluidType fluidType, int fluidLevel, BlockFace facing, boolean waterlogged) {
        this.material = material;
        this.friction = friction;
        this.fluidType = fluidType;
        this.fluidLevel = fluidLevel;
        this.facing = facing;
        this.waterlogged = waterlogged;
        this.collisionShape = EmptyCollisionShape.getCollisionShape();
    }

    BlockSnapshot(Player player, Block block, ProtocolVersion version) {
        this.material = block.getType();
        this.friction = NativeAccessProvider.getInstance().getBlockFriction(block);
        ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
        ProtocolVersion version2 = ProtocolVersion.V1_13;
        if (serverVersion.isAtLeast(version2)) {
            ProtocolVersion version3 = ProtocolVersion.V1_13;
            if (!version.isAtLeast(version3) || !AQUATIC_MATERIALS.contains(this.getMaterial()) && (!(block.getBlockData() instanceof Waterlogged) || !((Waterlogged) block.getBlockData()).isWaterlogged())) {
                if (block.isLiquid() && block.getBlockData() instanceof Levelled) {
                    Levelled levelled = (Levelled) block.getBlockData();
                    this.fluidType = MaterialAlias.LAVA.contains(this.getMaterial()) ? FluidType.LAVA : FluidType.WATER;
                    this.fluidLevel = levelled.getLevel();
                } else {
                    this.fluidType = null;
                    this.fluidLevel = -1;
                }
            } else {
                this.fluidType = FluidType.WATER;
                this.fluidLevel = 0;
            }
        } else if (block.isLiquid()) {
            this.fluidType = MaterialAlias.LAVA.contains(this.getMaterial()) ? FluidType.LAVA : FluidType.WATER;
            this.fluidLevel = block.getData();
        } else {
            this.fluidType = null;
            this.fluidLevel = -1;
        }
        if (MaterialAlias.TRAPDOORS.contains(this.getMaterial())) {
            serverVersion = ProtocolVersion.getServerVersion();
            ProtocolVersion version4 = ProtocolVersion.V1_13;
            if (serverVersion.isAtLeast(version4)) {
                TrapDoor trapDoor = (TrapDoor) block.getBlockData();
                this.facing = trapDoor.getFacing();
                this.waterlogged = trapDoor.isOpen();
            } else {
                org.bukkit.material.TrapDoor trapDoor2 = new org.bukkit.material.TrapDoor(this.getMaterial(), block.getData());
                this.facing = trapDoor2.getFacing();
                this.waterlogged = trapDoor2.isOpen();
            }
        } else if (this.getMaterial() == Material.LADDER) {
            serverVersion = ProtocolVersion.getServerVersion();
            ProtocolVersion version5 = ProtocolVersion.V1_13;
            if (serverVersion.isAtLeast(version5)) {
                Ladder ladder = (Ladder) block.getBlockData();
                this.facing = ladder.getFacing();
            } else {
                org.bukkit.material.Ladder ladder2 = new org.bukkit.material.Ladder(this.getMaterial(), block.getData());
                this.facing = ladder2.getFacing();
            }
            this.waterlogged = false;
        } else {
            this.facing = null;
            this.waterlogged = false;
        }
        Material material2 = this.getMaterial();
        this.collisionShape = BlockCollisionShapes.findCollisionShape(player, block, material2, version);
    }

    public Material getMaterial() {
        return this.material;
    }

    public float getFriction() {
        return this.friction;
    }

    public FluidType getFluidType() {
        return this.fluidType;
    }

    public int getFluidLevel() {
        return this.fluidLevel;
    }

    public BlockFace getFacing() {
        return this.facing;
    }

    public boolean isWaterlogged() {
        return this.waterlogged;
    }

    public CollisionShape getCollisionShape() {
        return this.collisionShape;
    }

    static {
        for (String text : Arrays.asList("BUBBLE_COLUMN", "KELP", "KELP_PLANT", "SEAGRASS", "TALL_SEAGRASS", "TUBE_CORAL", "BRAIN_CORAL", "BUBBLE_CORAL", "FIRE_CORAL")) {
            try {
                AQUATIC_MATERIALS.add(Material.valueOf(text));
            } catch (IllegalArgumentException illegalArgumentException) {
            }
        }
    }
}
