package me.konsolas.aac.compat.integration;

import java.lang.reflect.Method;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class OptionalIntegrations {

    private OptionalIntegrations() {
    }

    private static Class<?> apiClass(String pluginName, String className) throws ClassNotFoundException {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return Class.forName(className, true, plugin.getClass().getClassLoader());
    }

    public static int viaVersion(Player player) {
        try {
            Class<?> via = apiClass("ViaVersion", "com.viaversion.viaversion.api.Via");
            Object api = via.getMethod("getAPI").invoke(null);
            for (Method method : api.getClass().getMethods()) if (method.getName().equals("getPlayerVersion") && method.getParameterTypes().length == 1) {
                Class<?> parameter = method.getParameterTypes()[0];
                if (parameter == UUID.class)
                    return ((Number) method.invoke(api, player.getUniqueId())).intValue();
                if (parameter.isInstance(player))
                    return ((Number) method.invoke(api, player)).intValue();
            }
            throw new NoSuchMethodException("ViaAPI.getPlayerVersion");
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to query ViaVersion", exception);
        }
    }

    public static int protocolSupportVersion(Player player) {
        try {
            Class<?> api = apiClass("ProtocolSupport", "protocolsupport.api.ProtocolSupportAPI");
            Object version = api.getMethod("getProtocolVersion", Player.class).invoke(null, player);
            return ((Number) version.getClass().getMethod("getId").invoke(version)).intValue();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to query ProtocolSupport", exception);
        }
    }

    public static boolean isFloodgatePlayer(UUID playerId) {
        try {
            Class<?> api = apiClass("floodgate", "org.geysermc.floodgate.api.FloodgateApi");
            Object instance = api.getMethod("getInstance").invoke(null);
            return (Boolean) api.getMethod("isFloodgatePlayer", UUID.class).invoke(instance, playerId);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to query Floodgate", exception);
        }
    }
}
