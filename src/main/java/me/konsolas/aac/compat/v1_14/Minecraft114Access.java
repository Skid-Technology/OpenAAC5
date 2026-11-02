package me.konsolas.aac.compat.v1_14;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import me.konsolas.aac.compat.NativeAccess;
import me.konsolas.aac.compat.nms.BlockBreakingHelper;
import me.konsolas.aac.compat.nms.LegacyBoundingBoxHelper;
import me.konsolas.aac.compat.nms.MinecraftReflection;
import me.konsolas.aac.compat.nms.ReflectionAccess;
import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import me.konsolas.aac.world.BlockRayTraceResult;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class Minecraft114Access implements NativeAccess {

    private final MethodHandle declaredField;

    private final Object field;

    private final MethodHandle method;

    private final MethodHandle declaredMethod;

    private final MethodHandle declaredField2;

    private final MethodHandle method2;

    private final MethodHandle method3;

    private final MethodHandle constructor;

    private final MethodHandle method4;

    private final MethodHandle method5;

    private final Object method6;

    private final MethodHandle method7;

    public Minecraft114Access(ProtocolVersion version) {
        if (version != ProtocolVersion.V1_14 && version != ProtocolVersion.V1_15) {
            throw new IllegalArgumentException("Invalid version for SALImplV1_14_15: " + version);
        } else {
            try {
                String name = Bukkit.getServer().getClass().getPackage().getName();
                String text = name.replace("org.bukkit.craftbukkit", "net.minecraft.server");
                Class classValue = Class.forName(name + ".entity.CraftEntity");
                Class classValue2 = Class.forName(name + ".CraftWorld");
                Class classValue3 = Class.forName(name + ".inventory.ItemStack");
                Class classValue4 = Class.forName(text + ".World");
                Class.forName(text + ".Entity");
                Class classValue5 = Class.forName(text + ".EntityPlayer");
                Class classValue6 = Class.forName(text + ".ItemStack");
                Class.forName(text + ".AxisAlignedBB");
                Class classValue7 = Class.forName(text + ".BlockPosition");
                Class.forName(text + ".IBlockData");
                this.declaredField = ReflectionAccess.LOOKUP.unreflectGetter((Field) ReflectionAccess.makeAccessible(classValue5.getSuperclass().getSuperclass().getDeclaredField("activeItem")));
                this.field = classValue6.getField("a").get(null);
                this.method = ReflectionAccess.LOOKUP.unreflect((Method) ReflectionAccess.makeAccessible(classValue3.getMethod("asCraftMirror", classValue6)));
                this.declaredMethod = ReflectionAccess.LOOKUP.unreflect((Method) ReflectionAccess.makeAccessible(MinecraftReflection.getCraftBlockClass().getDeclaredMethod("getNMSBlock")));
                this.declaredField2 = ReflectionAccess.LOOKUP.unreflectGetter((Field) ReflectionAccess.makeAccessible(MinecraftReflection.getBlockClass().getDeclaredField("frictionFactor")));
                this.method2 = ReflectionAccess.LOOKUP.unreflect(classValue.getMethod("getHandle"));
                this.method3 = ReflectionAccess.LOOKUP.unreflect(classValue2.getMethod("getHandle"));
                this.constructor = ReflectionAccess.LOOKUP.unreflectConstructor(classValue7.getConstructor(int.class, int.class, int.class));
                this.method4 = ReflectionAccess.LOOKUP.unreflect((Method) ReflectionAccess.makeAccessible(classValue4.getMethod("getType", classValue7)));
                this.method5 = ReflectionAccess.LOOKUP.unreflect((Method) ReflectionAccess.makeAccessible(MinecraftReflection.getIBlockDataClass().getMethod("b", MinecraftReflection.getIBlockAccessClass(), MinecraftReflection.getBlockPositionClass(), MinecraftReflection.getVoxelShapeCollisionClass())));
                this.method6 = MinecraftReflection.getVoxelShapeCollisionClass().getMethod("a").invoke(null);
                this.method7 = ReflectionAccess.LOOKUP.unreflect((Method) ReflectionAccess.makeAccessible(MinecraftReflection.getNmsClass((String) "VoxelShape").getMethod("d")));
            } catch (NoSuchMethodException | NoSuchFieldException | ClassNotFoundException | InvocationTargetException | IllegalAccessException exception) {
                throw new RuntimeException("SALImplV1_14_15: <clinit>", exception);
            }
        }
    }

    @Override
    public ItemStack getActiveItem(Player player) {
        try {
            Object result = (Object) this.declaredField.invoke((Object) (Object) this.method2.invoke((Player) player));
            return Objects.equals(result, this.field) ? null : (ItemStack) this.method.invoke((Object) result);
        } catch (Throwable throwable) {
            throw new RuntimeException("SALImplV1_14_15: getItemInUse", throwable);
        }
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
        try {
            return (float) this.declaredField2.invoke((Object) (Object) this.declaredMethod.invoke((Block) block));
        } catch (Throwable throwable) {
            throw new RuntimeException("SALImplV1_14_15: getFrictionFactor", throwable);
        }
    }

    @Override
    public AxisAlignedBoundingBox getBoundingBox(Entity entity) {
        BoundingBox boundingBox = entity.getBoundingBox();
        return new AxisAlignedBoundingBox(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
    }

    @Override
    public List getBlockCollisionBoxes(Block block, Entity entity) {
        try {
            Object result = (Object) this.method3.invoke((World) block.getWorld());
            Object result2 = (Object) this.constructor.invoke((int) block.getX(), (int) block.getY(), (int) block.getZ());
            Object result3 = (Object) this.method5.invoke((Object) (Object) this.method4.invoke((Object) result, (Object) result2), (Object) result, (Object) result2, (Object) this.method6);
            List values = (List) this.method7.invoke((Object) result3);
            ArrayList values2 = new ArrayList(values.size());
            for (Object result4 : values) {
                values2.add(LegacyBoundingBoxHelper.findAxisAlignedBoundingBox(result4));
            }
            return values2;
        } catch (Throwable throwable) {
            throw new RuntimeException("SALImplV1_14_15: getCollisionShape", throwable);
        }
    }

    @Override
    public BlockRayTraceResult rayTrace(Entity entity, Vector vector, Vector vector2, double value) {
        RayTraceResult rayTraceResult = entity.getWorld().rayTraceBlocks(vector.toLocation(entity.getWorld()), vector2.clone().normalize(), value);
        return BlockRayTraceResult.create(rayTraceResult);
    }

    @Override
    public float getBlockDamage(Player player, Block block) {
        return BlockBreakingHelper.getWorld(block, player);
    }
}
