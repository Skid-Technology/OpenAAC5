package me.konsolas.aac.compat.nms;

import java.util.HashMap;
import java.util.Map;
import me.konsolas.aac.protocol.ProtocolVersion;
import org.bukkit.Bukkit;

public final class MinecraftReflection {

    public static String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();

    public static String minecraftPackage = craftBukkitPackage.replace("org.bukkit.craftbukkit", "net.minecraft.server");

    private static final Map classCache = new HashMap();

    private MinecraftReflection() {
        throw new AssertionError();
    }

    private static Class resolveClass(String text) {
        try {
            if (classCache.containsKey(text)) {
                return (Class) classCache.get(text);
            } else {
                Class classValue = Class.forName(text);
                classCache.put(text, classValue);
                return classValue;
            }
        } catch (ClassNotFoundException classNotFoundException) {
            throw new IllegalArgumentException("Class not found: " + text, classNotFoundException);
        }
    }

    public static Class getNmsClass(String text) {
        String text2 = minecraftPackage + '.' + text;
        String text3 = text2;
        return resolveClass(text3);
    }

    public static Class getCraftClass(String text) {
        String text2 = craftBukkitPackage + '.' + text;
        String text3 = text2;
        return resolveClass(text3);
    }

    public static Class getCraftPlayerClass() {
        return getCraftClass((String) "entity.CraftPlayer");
    }

    public static Class getCraftWorldClass() {
        return getCraftClass((String) "CraftWorld");
    }

    public static Class getCraftBlockClass() {
        return getCraftClass((String) "block.CraftBlock");
    }

    public static Class getEntityClass() {
        return getNmsClass((String) "Entity");
    }

    public static Class getEntityPlayerClass() {
        return getNmsClass((String) "EntityPlayer");
    }

    public static Class getEntityHumanClass() {
        return getNmsClass((String) "EntityHuman");
    }

    public static Class getWorldClass() {
        return getNmsClass((String) "World");
    }

    public static Class getBlockClass() {
        return getNmsClass((String) "Block");
    }

    public static Class getMovingObjectPositionClass() {
        ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
        ProtocolVersion version = ProtocolVersion.V1_14;
        Class classValue;
        if (serverVersion.isAtLeast(version)) {
            classValue = getNmsClass((String) "MovingObjectPositionBlock");
        } else {
            classValue = getNmsClass((String) "MovingObjectPosition");
        }
        return classValue;
    }

    public static Class getBlockPositionClass() {
        return getNmsClass((String) "BlockPosition");
    }

    public static Class getIBlockDataClass() {
        return getNmsClass((String) "IBlockData");
    }

    public static Class getIBlockAccessClass() {
        return getNmsClass((String) "IBlockAccess");
    }

    public static Class getVec3DClass() {
        return getNmsClass((String) "Vec3D");
    }

    public static Class getVoxelShapeCollisionClass() {
        return getNmsClass((String) "VoxelShapeCollision");
    }
}
