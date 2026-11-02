package me.konsolas.aac.compat.v1_8;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import me.konsolas.aac.compat.NativeAccess;
import me.konsolas.aac.compat.nms.BlockBreakingHelper;
import me.konsolas.aac.compat.nms.EntityRayTracer;
import me.konsolas.aac.compat.nms.LegacyBoundingBoxHelper;
import me.konsolas.aac.compat.nms.MinecraftReflection;
import me.konsolas.aac.compat.nms.ReflectionAccess;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import me.konsolas.aac.world.BlockRayTraceResult;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Minecraft18Access implements NativeAccess {

    private final MethodHandle declaredMethod;

    private final MethodHandle declaredMethod2;

    private final MethodHandle declaredField;

    private final MethodHandle method;

    private final MethodHandle method2;

    private final MethodHandle method3;

    private final MethodHandle constructor;

    private final MethodHandle method4;

    private final MethodHandle method5;

    private final MethodHandle method6;

    public Minecraft18Access() {
        try {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            String text = name.replace("org.bukkit.craftbukkit", "net.minecraft.server");
            Class classValue = Class.forName(name + ".entity.CraftEntity");
            Class classValue2 = Class.forName(name + ".CraftWorld");
            Class classValue3 = Class.forName(text + ".World");
            Class classValue4 = Class.forName(text + ".Entity");
            Class.forName(text + ".EntityHuman");
            Class classValue5 = Class.forName(text + ".Block");
            Class classValue6 = Class.forName(text + ".AxisAlignedBB");
            Class classValue7 = Class.forName(text + ".BlockPosition");
            Class classValue8 = Class.forName(text + ".IBlockData");
            this.declaredMethod = ReflectionAccess.LOOKUP.unreflect((Method) ReflectionAccess.makeAccessible(MinecraftReflection.getEntityPlayerClass().getSuperclass().getDeclaredMethod("bS")));
            this.declaredMethod2 = ReflectionAccess.LOOKUP.unreflect((Method) ReflectionAccess.makeAccessible(MinecraftReflection.getCraftBlockClass().getDeclaredMethod("getNMSBlock")));
            this.declaredField = ReflectionAccess.LOOKUP.unreflectGetter((Field) ReflectionAccess.makeAccessible(MinecraftReflection.getBlockClass().getDeclaredField("frictionFactor")));
            this.method = ReflectionAccess.LOOKUP.unreflect(classValue.getMethod("getHandle"));
            this.method2 = ReflectionAccess.LOOKUP.unreflect(classValue2.getMethod("getHandle"));
            this.method3 = ReflectionAccess.LOOKUP.unreflect((Method) ReflectionAccess.makeAccessible(MinecraftReflection.getEntityClass().getMethod("getBoundingBox")));
            this.constructor = ReflectionAccess.LOOKUP.unreflectConstructor(classValue7.getConstructor(int.class, int.class, int.class));
            this.method4 = ReflectionAccess.LOOKUP.unreflect((Method) ReflectionAccess.makeAccessible(classValue3.getMethod("getType", classValue7)));
            this.method5 = ReflectionAccess.LOOKUP.unreflect((Method) ReflectionAccess.makeAccessible(classValue8.getMethod("getBlock")));
            this.method6 = ReflectionAccess.LOOKUP.unreflect((Method) ReflectionAccess.makeAccessible(classValue5.getMethod("a", classValue3, classValue7, classValue8, classValue6, List.class, classValue4)));
        } catch (NoSuchMethodException | NoSuchFieldException | ClassNotFoundException | IllegalAccessException exception) {
            throw new RuntimeException("SALImplV1_8: <clinit>", exception);
        }
    }

    @Override
    public ItemStack getActiveItem(Player player) {
        try {
            return (boolean) this.declaredMethod.invoke((Object) (Object) this.method.invoke((Player) player)) ? player.getItemInHand() : null;
        } catch (Throwable throwable) {
            throw new RuntimeException("SALImplV1_8: getItemInUse", throwable);
        }
    }

    @Override
    public Collection getHandItems(Player player) {
        return Collections.singleton(player.getItemInHand());
    }

    @Override
    public boolean isGliding(Player player) {
        return false;
    }

    @Override
    public float getBlockFriction(Block block) {
        try {
            return (float) this.declaredField.invoke((Object) (Object) this.declaredMethod2.invoke((Block) block));
        } catch (Throwable throwable) {
            throw new RuntimeException("SALImplV1_8: getFrictionFactor", throwable);
        }
    }

    @Override
    public AxisAlignedBoundingBox getBoundingBox(Entity entity) {
        try {
            Object result = (Object) this.method3.invoke((Object) (Object) this.method.invoke((Entity) entity));
            return LegacyBoundingBoxHelper.findAxisAlignedBoundingBox(result);
        } catch (Throwable throwable) {
            throw new RuntimeException("SALImplV1_8: getBoundingBox", throwable);
        }
    }

    @Override
    public List getBlockCollisionBoxes(Block block, Entity entity) {
        try {
            Object result = (Object) this.method2.invoke((World) block.getWorld());
            Object result2 = (Object) this.constructor.invoke((int) block.getX(), (int) block.getY(), (int) block.getZ());
            Object result3 = (Object) this.method4.invoke((Object) result, (Object) result2);
            double x = block.getX();
            double y = block.getY();
            double z = block.getZ();
            double value = block.getX() + 1.0;
            double value2 = block.getZ() + 1.0;
            double value3 = block.getY() + 1.0;
            double value4 = value;
            double value5 = z;
            double value6 = y;
            double value7 = x;
            Object result4 = LegacyBoundingBoxHelper.findObject(value7, value6, value5, value4, value3, value2);
            ArrayList values = new ArrayList();
            Object result5 = (Object) this.method5.invoke((Object) result3);
            this.method6.invoke((Object) result5, (Object) result, (Object) result2, (Object) result3, (Object) result4, (List) values, (Object) (Object) this.method.invoke((Entity) entity));
            ArrayList values2 = new ArrayList(values.size());
            for (Object result6 : values) {
                z = -block.getX();
                double value8 = -block.getZ();
                double value9 = -block.getY();
                double value10 = z;
                values2.add(LegacyBoundingBoxHelper.findAxisAlignedBoundingBox(result6, value10, value9, value8));
            }
            return values2;
        } catch (Throwable throwable) {
            throw new RuntimeException("SALImplV1_8: getCollisionShape", throwable);
        }
    }

    @Override
    public BlockRayTraceResult rayTrace(Entity entity, Vector vector, Vector vector2, double value) {
        return EntityRayTracer.rayTrace(entity, vector, vector2, value);
    }

    @Override
    public float getBlockDamage(Player player, Block block) {
        return BlockBreakingHelper.getWorld(block, player);
    }
}
