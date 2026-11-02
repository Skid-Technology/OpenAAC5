package me.konsolas.aac.compat.v1_17;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import me.konsolas.aac.compat.NativeAccess;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import me.konsolas.aac.world.BlockRayTraceResult;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class Minecraft117Access implements NativeAccess {

    @Override
    public ItemStack getActiveItem(Player player) {
        return player.getItemInUse();
    }

    @Override
    public Collection getHandItems(Player player) {
        return Arrays.asList(player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand());
    }

    @Override
    public boolean isGliding(Player player) {
        return player.isGliding();
    }

    @Override
    public float getBlockFriction(Block block) {
        return block.getType().getSlipperiness();
    }

    @Override
    public AxisAlignedBoundingBox getBoundingBox(Entity entity) {
        return AxisAlignedBoundingBox.fromBukkit(entity.getBoundingBox());
    }

    @Override
    public List getBlockCollisionBoxes(Block block, Entity entity) {
        Collection<BoundingBox> boundingBoxes = block.getCollisionShape().getBoundingBoxes();
        ArrayList values = new ArrayList(boundingBoxes.size());
        for (BoundingBox box : boundingBoxes) {
            values.add(AxisAlignedBoundingBox.fromBukkit(box));
        }
        return values;
    }

    @Override
    public BlockRayTraceResult rayTrace(Entity entity, Vector vector, Vector vector2, double value) {
        RayTraceResult rayTraceResult = entity.getWorld().rayTraceBlocks(vector.toLocation(entity.getWorld()), vector2.clone().normalize(), value);
        return BlockRayTraceResult.create(rayTraceResult);
    }

    @Override
    public float getBlockDamage(Player player, Block block) {
        return block.getBreakSpeed(player);
    }
}
