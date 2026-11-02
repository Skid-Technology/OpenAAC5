package me.konsolas.aac.compat.nms;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.world.BlockRayTraceResult;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class EntityRayTracer {

    private static final MethodHandle entityHandle;

    private static final MethodHandle vectorConstructor;

    private static final MethodHandle hitBlockPosition;

    private static final MethodHandle hitDirection;

    private static final MethodHandle directionToBlockFace;

    private static final MethodHandle hitPosition;

    private static final MethodHandle vectorX;

    private static final MethodHandle vectorY;

    private static final MethodHandle vectorZ;

    private static final MethodHandle blockX;

    private static final MethodHandle blockY;

    private static final MethodHandle blockZ;

    private static final MethodHandle rayTrace;

    public static BlockRayTraceResult rayTrace(Entity entity, Vector vector, Vector vector2, double value) {
        Vector vector3 = vector.clone().add(vector2.clone().normalize().multiply(value));
        try {
            Object result = (Object) entityHandle.invoke((World) entity.getWorld());
            Object result2 = (Object) vectorConstructor.invoke((double) vector.getX(), (double) vector.getY(), (double) vector.getZ());
            Object result3 = (Object) vectorConstructor.invoke((double) vector3.getX(), (double) vector3.getY(), (double) vector3.getZ());
            Object result4 = (Object) rayTrace.invoke((Object) result, (Object) result2, (Object) result3);
            if (result4 == null) {
                return new BlockRayTraceResult();
            } else {
                Object result5 = (Object) hitPosition.invoke((Object) result4);
                Object result6 = (Object) hitBlockPosition.invoke((Object) result4);
                Vector vector4 = new Vector((double) vectorX.invoke((Object) result5), (double) vectorY.invoke((Object) result5), (double) vectorZ.invoke((Object) result5));
                BlockVector blockVector = new BlockVector((int) blockX.invoke((Object) result6), (int) blockY.invoke((Object) result6), (int) blockZ.invoke((Object) result6));
                BlockFace blockFace = (BlockFace) directionToBlockFace.invoke((Object) (Object) hitDirection.invoke((Object) result4));
                return new BlockRayTraceResult(vector4, blockVector, blockFace);
            }
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    static {
        ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
        ProtocolVersion version = ProtocolVersion.V1_13;
        if (serverVersion.isAtLeast(version)) {
            entityHandle = null;
            vectorConstructor = null;
            hitBlockPosition = null;
            hitDirection = null;
            directionToBlockFace = null;
            hitPosition = null;
            vectorX = null;
            vectorY = null;
            vectorZ = null;
            blockX = null;
            blockY = null;
            blockZ = null;
            rayTrace = null;
        } else {
            try {
                Class vec3DClass = MinecraftReflection.getVec3DClass();
                Class movingObjectPositionClass = MinecraftReflection.getMovingObjectPositionClass();
                Class blockPositionClass = MinecraftReflection.getBlockPositionClass();
                Class minecraftClassWithMinecraftPackage = MinecraftReflection.getNmsClass((String) "EnumDirection");
                Class minecraftClass = MinecraftReflection.getCraftBlockClass();
                entityHandle = ReflectionAccess.LOOKUP.unreflect((Method) ReflectionAccess.makeAccessible(MinecraftReflection.getCraftWorldClass().getMethod("getHandle")));
                vectorConstructor = ReflectionAccess.LOOKUP.findConstructor(vec3DClass, MethodType.methodType(void.class, double.class, double.class, double.class));
                hitPosition = ReflectionAccess.LOOKUP.findGetter(movingObjectPositionClass, "pos", vec3DClass);
                hitBlockPosition = ReflectionAccess.LOOKUP.unreflect(Arrays.stream(movingObjectPositionClass.getMethods()).filter(result -> result.getReturnType().equals(blockPositionClass)).findFirst().orElseThrow(NoSuchMethodException::new));
                serverVersion = ProtocolVersion.getServerVersion();
                ProtocolVersion version2 = ProtocolVersion.V1_14;
                hitDirection = serverVersion.isAtLeast(version2) ? ReflectionAccess.LOOKUP.findVirtual(movingObjectPositionClass, "getDirection", MethodType.methodType(minecraftClassWithMinecraftPackage)) : ReflectionAccess.LOOKUP.findGetter(movingObjectPositionClass, "direction", minecraftClassWithMinecraftPackage);
                directionToBlockFace = ReflectionAccess.LOOKUP.findStatic(minecraftClass, "notchToBlockFace", MethodType.methodType(BlockFace.class, minecraftClassWithMinecraftPackage));
                serverVersion = ProtocolVersion.getServerVersion();
                ProtocolVersion version3 = ProtocolVersion.V1_9;
                if (serverVersion.isAtLeast(version3)) {
                    vectorX = ReflectionAccess.LOOKUP.findGetter(MinecraftReflection.getVec3DClass(), "x", double.class);
                    vectorY = ReflectionAccess.LOOKUP.findGetter(MinecraftReflection.getVec3DClass(), "y", double.class);
                    vectorZ = ReflectionAccess.LOOKUP.findGetter(MinecraftReflection.getVec3DClass(), "z", double.class);
                } else {
                    vectorX = ReflectionAccess.LOOKUP.findGetter(MinecraftReflection.getVec3DClass(), "a", double.class);
                    vectorY = ReflectionAccess.LOOKUP.findGetter(MinecraftReflection.getVec3DClass(), "b", double.class);
                    vectorZ = ReflectionAccess.LOOKUP.findGetter(MinecraftReflection.getVec3DClass(), "c", double.class);
                }
                blockX = ReflectionAccess.LOOKUP.unreflect(blockPositionClass.getMethod("getX"));
                blockY = ReflectionAccess.LOOKUP.unreflect(blockPositionClass.getMethod("getY"));
                blockZ = ReflectionAccess.LOOKUP.unreflect(blockPositionClass.getMethod("getZ"));
                rayTrace = ReflectionAccess.LOOKUP.findVirtual(MinecraftReflection.getWorldClass(), "rayTrace", MethodType.methodType(movingObjectPositionClass, vec3DClass, vec3DClass));
            } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException exception) {
                throw new RuntimeException("LegacyRaytracer: <clinit>", exception);
            }
        }
    }
}
