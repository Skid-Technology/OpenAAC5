package me.konsolas.aac.compat.nms;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import org.bukkit.Bukkit;

public class LegacyBoundingBoxHelper {

    private static final MethodHandle constructor;

    private static final MethodHandle minXGetter;

    private static final MethodHandle minYGetter;

    private static final MethodHandle minZGetter;

    private static final MethodHandle maxXGetter;

    private static final MethodHandle maxYGetter;

    private static final MethodHandle maxZGetter;

    public static AxisAlignedBoundingBox findAxisAlignedBoundingBox(Object result) {
        try {
            return new AxisAlignedBoundingBox((double) minXGetter.invoke((Object) result), (double) minYGetter.invoke((Object) result), (double) minZGetter.invoke((Object) result), (double) maxXGetter.invoke((Object) result), (double) maxYGetter.invoke((Object) result), (double) maxZGetter.invoke((Object) result));
        } catch (Throwable throwable) {
            throw new RuntimeException("LegacyBoundingBoxHelper: fromNMS", throwable);
        }
    }

    public static AxisAlignedBoundingBox findAxisAlignedBoundingBox(Object result, double value, double value2, double value3) {
        try {
            return new AxisAlignedBoundingBox((double) minXGetter.invoke((Object) result) + value, (double) minYGetter.invoke((Object) result) + value2, (double) minZGetter.invoke((Object) result) + value3, (double) maxXGetter.invoke((Object) result) + value, (double) maxYGetter.invoke((Object) result) + value2, (double) maxZGetter.invoke((Object) result) + value3);
        } catch (Throwable throwable) {
            throw new RuntimeException("LegacyBoundingBoxHelper: fromNMS_offset", throwable);
        }
    }

    public static Object findObject(double value, double value2, double value3, double value4, double value5, double value6) {
        try {
            return (Object) constructor.invoke((double) value, (double) value2, (double) value3, (double) value4, (double) value5, (double) value6);
        } catch (Throwable throwable) {
            throw new RuntimeException("LegacyBoundingBoxHelper: cons", throwable);
        }
    }

    static {
        ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
        ProtocolVersion version = ProtocolVersion.V1_13;
        if (serverVersion.isAtLeast(version)) {
            constructor = null;
            minXGetter = null;
            minYGetter = null;
            minZGetter = null;
            maxXGetter = null;
            maxYGetter = null;
            maxZGetter = null;
        } else {
            try {
                String name = Bukkit.getServer().getClass().getPackage().getName();
                String text = name.replace("org.bukkit.craftbukkit", "net.minecraft.server");
                Class classValue = Class.forName(text + ".AxisAlignedBB");
                constructor = ReflectionAccess.LOOKUP.unreflectConstructor((Constructor<?>) ReflectionAccess.makeAccessible(classValue.getConstructor(double.class, double.class, double.class, double.class, double.class, double.class)));
                serverVersion = ProtocolVersion.getServerVersion();
                ProtocolVersion version2 = ProtocolVersion.V1_13;
                if (serverVersion.isAtLeast(version2)) {
                    minXGetter = ReflectionAccess.LOOKUP.unreflectGetter((Field) ReflectionAccess.makeAccessible(classValue.getDeclaredField("minX")));
                    minYGetter = ReflectionAccess.LOOKUP.unreflectGetter((Field) ReflectionAccess.makeAccessible(classValue.getDeclaredField("minY")));
                    minZGetter = ReflectionAccess.LOOKUP.unreflectGetter((Field) ReflectionAccess.makeAccessible(classValue.getDeclaredField("minZ")));
                    maxXGetter = ReflectionAccess.LOOKUP.unreflectGetter((Field) ReflectionAccess.makeAccessible(classValue.getDeclaredField("maxX")));
                    maxYGetter = ReflectionAccess.LOOKUP.unreflectGetter((Field) ReflectionAccess.makeAccessible(classValue.getDeclaredField("maxY")));
                    maxZGetter = ReflectionAccess.LOOKUP.unreflectGetter((Field) ReflectionAccess.makeAccessible(classValue.getDeclaredField("maxZ")));
                } else {
                    minXGetter = ReflectionAccess.LOOKUP.unreflectGetter((Field) ReflectionAccess.makeAccessible(classValue.getDeclaredField("a")));
                    minYGetter = ReflectionAccess.LOOKUP.unreflectGetter((Field) ReflectionAccess.makeAccessible(classValue.getDeclaredField("b")));
                    minZGetter = ReflectionAccess.LOOKUP.unreflectGetter((Field) ReflectionAccess.makeAccessible(classValue.getDeclaredField("c")));
                    maxXGetter = ReflectionAccess.LOOKUP.unreflectGetter((Field) ReflectionAccess.makeAccessible(classValue.getDeclaredField("d")));
                    maxYGetter = ReflectionAccess.LOOKUP.unreflectGetter((Field) ReflectionAccess.makeAccessible(classValue.getDeclaredField("e")));
                    maxZGetter = ReflectionAccess.LOOKUP.unreflectGetter((Field) ReflectionAccess.makeAccessible(classValue.getDeclaredField("f")));
                }
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | NoSuchFieldException exception) {
                throw new RuntimeException("LegacyBoundingBoxHelper: <clinit>", exception);
            }
        }
    }
}
