package me.konsolas.aac.compat.v1_11;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import me.konsolas.aac.compat.NativeAccess;
import me.konsolas.aac.compat.nms.BlockBreakingHelper;
import me.konsolas.aac.compat.nms.EntityRayTracer;
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
import org.bukkit.util.Vector;

public class Minecraft111Access implements NativeAccess {

    private final MethodHandle declaredField;

    private final Object field;

    private final MethodHandle method;

    private final MethodHandle declaredMethod;

    private final MethodHandle declaredField2;

    private final MethodHandle method2;

    private final MethodHandle method3;

    private final MethodHandle method4;

    private final MethodHandle constructor;

    private final MethodHandle method5;

    private final MethodHandle method6;

    public Minecraft111Access(ProtocolVersion version) {
        if (version != ProtocolVersion.V1_11 && version != ProtocolVersion.V1_12) {
            throw new IllegalArgumentException("Invalid version for SALImplV1_9_10: " + version);
        } else {
            try {
                String name = Bukkit.getServer().getClass().getPackage().getName();
                String text = name.replace("org.bukkit.craftbukkit", "net.minecraft.server");
                Class classValue = Class.forName(name + ".entity.CraftEntity");
                Class classValue2 = Class.forName(name + ".CraftWorld");
                Class classValue3 = Class.forName(name + ".inventory.ItemStack");
                Class classValue4 = Class.forName(text + ".World");
                Class classValue5 = Class.forName(text + ".Entity");
                Class classValue6 = Class.forName(text + ".EntityPlayer");
                Class classValue7 = Class.forName(text + ".ItemStack");
                Class classValue8 = Class.forName(text + ".AxisAlignedBB");
                Class classValue9 = Class.forName(text + ".BlockPosition");
                Class classValue10 = Class.forName(text + ".IBlockData");
                this.declaredField = ReflectionAccess.LOOKUP.unreflectGetter((Field) ReflectionAccess.makeAccessible(classValue6.getSuperclass().getSuperclass().getDeclaredField("activeItem")));
                this.field = classValue7.getField("a").get(null);
                this.method = ReflectionAccess.LOOKUP.unreflect((Method) ReflectionAccess.makeAccessible(classValue3.getMethod("asCraftMirror", classValue7)));
                this.declaredMethod = ReflectionAccess.LOOKUP.unreflect((Method) ReflectionAccess.makeAccessible(MinecraftReflection.getCraftBlockClass().getDeclaredMethod("getNMSBlock")));
                this.declaredField2 = ReflectionAccess.LOOKUP.unreflectGetter((Field) ReflectionAccess.makeAccessible(MinecraftReflection.getBlockClass().getDeclaredField("frictionFactor")));
                this.method2 = ReflectionAccess.LOOKUP.unreflect(classValue.getMethod("getHandle"));
                this.method3 = ReflectionAccess.LOOKUP.unreflect(classValue2.getMethod("getHandle"));
                this.method4 = ReflectionAccess.LOOKUP.unreflect((Method) ReflectionAccess.makeAccessible(MinecraftReflection.getEntityClass().getMethod("getBoundingBox")));
                this.constructor = ReflectionAccess.LOOKUP.unreflectConstructor(classValue9.getConstructor(int.class, int.class, int.class));
                this.method5 = ReflectionAccess.LOOKUP.unreflect((Method) ReflectionAccess.makeAccessible(classValue4.getMethod("getType", classValue9)));
                this.method6 = ReflectionAccess.LOOKUP.unreflect((Method) ReflectionAccess.makeAccessible(classValue10.getMethod("a", classValue4, classValue9, classValue8, List.class, classValue5, boolean.class)));
            } catch (NoSuchMethodException | NoSuchFieldException | ClassNotFoundException | IllegalAccessException exception) {
                throw new RuntimeException("SALImplV1_11_12: <clinit>", exception);
            }
        }
    }

    @Override
    public ItemStack getActiveItem(Player player) {
        try {
            Object result = (Object) this.declaredField.invoke((Object) (Object) this.method2.invoke((Player) player));
            return Objects.equals(result, this.field) ? null : (ItemStack) this.method.invoke((Object) result);
        } catch (Throwable throwable) {
            throw new RuntimeException("SALImplV1_11_12: getItemInUse", throwable);
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
            throw new RuntimeException("SALImplV1_11_12: getFrictionFactor", throwable);
        }
    }

    @Override
    public AxisAlignedBoundingBox getBoundingBox(Entity entity) {
        try {
            Object result = (Object) this.method4.invoke((Object) (Object) this.method2.invoke((Entity) entity));
            return LegacyBoundingBoxHelper.findAxisAlignedBoundingBox(result);
        } catch (Throwable throwable) {
            throw new RuntimeException("SALImplV1_11_12: getBoundingBox", throwable);
        }
    }

    @Override
    public List getBlockCollisionBoxes(Block block, Entity entity) {
        ArrayList values;
        try {
            Object result = (Object) this.method3.invoke((World) block.getWorld());
            Object result2 = (Object) this.constructor.invoke((int) block.getX(), (int) block.getY(), (int) block.getZ());
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
            Object result3 = LegacyBoundingBoxHelper.findObject(value7, value6, value5, value4, value3, value2);
            ArrayList values2 = new ArrayList();
            this.method6.invoke((Object) (Object) this.method5.invoke((Object) result, (Object) result2), (Object) result, (Object) result2, (Object) result3, (List) values2, (Object) (Object) this.method2.invoke((Entity) entity), (boolean) false);
            ArrayList values3 = new ArrayList(values2.size());
            for (Object result4 : values2) {
                z = -block.getX();
                double value8 = -block.getZ();
                double value9 = -block.getY();
                double value10 = z;
                values3.add(LegacyBoundingBoxHelper.findAxisAlignedBoundingBox(result4, value10, value9, value8));
            }
            values = values3;
        } catch (Throwable throwable) {
            throw new RuntimeException("SALImplV1_11_12: getCollisionShape", throwable);
        }
        if (null != null) {
        }
        return values;
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
