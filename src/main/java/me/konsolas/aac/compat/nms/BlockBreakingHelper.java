package me.konsolas.aac.compat.nms;

import it.unimi.dsi.fastutil.objects.AbstractObjectCollection;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import me.konsolas.aac.protocol.ProtocolVersion;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BlockBreakingHelper {

    private static final Method GET_HANDLE_METHOD;

    private static final Method GET_HANDLE_METHOD2;

    private static final Constructor CONSTRUCTOR;

    private static final Method GET_TYPE_METHOD;

    private static Method GET_BLOCK_METHOD;

    private static Method GET_DAMAGE_METHOD;

    private static Method A_METHOD;

    private static Method GET_DAMAGE_METHOD2;

    public static long calculateTime(Block block, Player player) {
        return (long) (1.0F / getWorld(block, player));
    }

    public static float getWorld(Block block, Player player) {
        if (ProtocolVersion.getServerVersion() == ProtocolVersion.V1_8) {
            try {
                Object result = CONSTRUCTOR.newInstance(block.getX(), block.getY(), block.getZ());
                Object result2 = GET_HANDLE_METHOD2.invoke(block.getWorld());
                Object result3 = GET_TYPE_METHOD.invoke(result2, result);
                Object result4 = GET_BLOCK_METHOD.invoke(result3);
                return (Float) GET_DAMAGE_METHOD.invoke(result4, GET_HANDLE_METHOD.invoke(player), result2, result);
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
        } else {
            ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
            ProtocolVersion version = ProtocolVersion.V1_13;
            if (serverVersion.isAtLeast(version)) {
                try {
                    Object result5 = CONSTRUCTOR.newInstance(block.getX(), block.getY(), block.getZ());
                    Object result6 = GET_HANDLE_METHOD2.invoke(block.getWorld());
                    Object result7 = GET_TYPE_METHOD.invoke(result6, result5);
                    return (Float) GET_DAMAGE_METHOD2.invoke(result7, GET_HANDLE_METHOD.invoke(player), result6, result5);
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException exception2) {
                    throw new RuntimeException(exception2);
                }
            } else {
                try {
                    Object result8 = CONSTRUCTOR.newInstance(block.getX(), block.getY(), block.getZ());
                    Object result9 = GET_HANDLE_METHOD2.invoke(block.getWorld());
                    Object result10 = GET_TYPE_METHOD.invoke(result9, result8);
                    return (Float) A_METHOD.invoke(result10, GET_HANDLE_METHOD.invoke(player), result9, result8);
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException exception3) {
                    throw new RuntimeException(exception3);
                }
            }
        }
    }

    public static int getAmplifier(Player player) {
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            if (potionEffect.getType().equals(PotionEffectType.FAST_DIGGING)) {
                return potionEffect.getAmplifier() + 1;
            }
        }
        return 0;
    }

    static {
        ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
        ProtocolVersion version = ProtocolVersion.V1_17;
        if (serverVersion.isAtLeast(version)) {
            GET_HANDLE_METHOD = null;
            GET_HANDLE_METHOD2 = null;
            CONSTRUCTOR = null;
            GET_TYPE_METHOD = null;
        } else {
            try {
                GET_HANDLE_METHOD2 = MinecraftReflection.getCraftWorldClass().getMethod("getHandle");
                GET_HANDLE_METHOD = MinecraftReflection.getCraftPlayerClass().getMethod("getHandle");
                CONSTRUCTOR = MinecraftReflection.getBlockPositionClass().getConstructor(int.class, int.class, int.class);
                GET_TYPE_METHOD = MinecraftReflection.getWorldClass().getMethod("getType", MinecraftReflection.getBlockPositionClass());
                if (ProtocolVersion.getServerVersion() == ProtocolVersion.V1_8) {
                    GET_BLOCK_METHOD = MinecraftReflection.getIBlockDataClass().getMethod("getBlock");
                    GET_DAMAGE_METHOD = MinecraftReflection.getBlockClass().getMethod("getDamage", MinecraftReflection.getEntityHumanClass(), MinecraftReflection.getWorldClass(), MinecraftReflection.getBlockPositionClass());
                } else {
                    serverVersion = ProtocolVersion.getServerVersion();
                    ProtocolVersion version2 = ProtocolVersion.V1_13;
                    if (serverVersion.isAtLeast(version2)) {
                        GET_DAMAGE_METHOD2 = MinecraftReflection.getIBlockDataClass().getMethod("getDamage", MinecraftReflection.getEntityHumanClass(), MinecraftReflection.getIBlockAccessClass(), MinecraftReflection.getBlockPositionClass());
                    } else {
                        A_METHOD = MinecraftReflection.getIBlockDataClass().getMethod("a", MinecraftReflection.getEntityHumanClass(), MinecraftReflection.getWorldClass(), MinecraftReflection.getBlockPositionClass());
                    }
                }
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
