package me.konsolas.aac.compat;

import java.util.Collection;
import java.util.List;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import me.konsolas.aac.world.BlockRayTraceResult;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public interface NativeAccess {

    ItemStack getActiveItem(Player player);

    Collection<ItemStack> getHandItems(Player player);

    boolean isGliding(Player player);

    float getBlockFriction(Block block);

    AxisAlignedBoundingBox getBoundingBox(Entity entity);

    List getBlockCollisionBoxes(Block block, Entity entity);

    BlockRayTraceResult rayTrace(Entity entity, Vector vector, Vector vector2, double value);

    float getBlockDamage(Player player, Block block);
}
