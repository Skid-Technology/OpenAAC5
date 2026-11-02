package me.konsolas.aac.protocol;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.konsolas.aac.compat.integration.OptionalIntegrations;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public enum ProtocolVersion {

    V1_8(1, 8),
    V1_9(1, 9),
    V1_10(1, 10),
    V1_11(1, 11),
    V1_12(1, 12),
    V1_13(1, 13),
    V1_14(1, 14),
    V1_15(1, 15),
    V1_16(1, 16),
    V1_17(1, 17);

    private static final Pattern VERSION_PATTERN = Pattern.compile(".*\\(.*MC.\\s*([a-zA-z0-9\\-.]+).*");

    private static final boolean VIA_VERSION_AVAILABLE = detectIntegration("ViaVersion");

    private static final boolean PROTOCOL_SUPPORT_AVAILABLE = detectIntegration("ProtocolSupport");

    private static final ProtocolVersion SERVER_VERSION = detectServerVersion();

    private final int major;

    private final int minor;

    ProtocolVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public static ProtocolVersion getServerVersion() {
        return SERVER_VERSION;
    }

    public boolean isAtLeast(ProtocolVersion version) {
        return version != null && compareTo(version) >= 0;
    }

    public static ProtocolVersion detectPlayerVersion(Player player) {
        if (VIA_VERSION_AVAILABLE) {
            ProtocolVersion version = fromProtocolId(OptionalIntegrations.viaVersion(player));
            if (version != SERVER_VERSION)
                return version;
        }
        if (PROTOCOL_SUPPORT_AVAILABLE) {
            ProtocolVersion version = fromProtocolId(OptionalIntegrations.protocolSupportVersion(player));
            if (version != SERVER_VERSION)
                return version;
        }
        return SERVER_VERSION;
    }

    private static boolean detectIntegration(String name) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        if (plugin == null)
            return false;
        Bukkit.getLogger().info("[AAC5] Detected " + name + (name.equals("ViaVersion") ? " " + plugin.getDescription().getVersion() : ""));
        return true;
    }

    private static ProtocolVersion detectServerVersion() {
        Matcher matcher = VERSION_PATTERN.matcher(Bukkit.getVersion());
        if (!matcher.matches() || matcher.group(1) == null)
            throw new IllegalStateException("Failed to determine server version");
        String value = matcher.group(1);
        String[] parts = value.split("\\.");
        if (parts.length < 2)
            throw new IllegalStateException("Corrupt server version " + value);
        int major = Integer.parseInt(parts[0]), minor = Integer.parseInt(parts[1]);
        for (ProtocolVersion version : values()) if (version.major == major && version.minor == minor)
            return version;
        throw new IllegalStateException("Unsupported minecraft version: " + major + "." + minor);
    }

    @Override
    public String toString() {
        return major + "." + minor + ".x";
    }

    public static ProtocolVersion fromProtocolId(int value) {
        if (755 <= value && value <= 756) {
            return V1_17;
        } else if (701 <= value && value <= 754) {
            return V1_16;
        } else if (550 <= value && value <= 578) {
            return V1_15;
        } else if (441 <= value && value <= 498) {
            return V1_14;
        } else if (341 <= value && value <= 404) {
            return V1_13;
        } else if (317 <= value && value <= 340) {
            return V1_12;
        } else if (301 <= value && value <= 316) {
            return V1_11;
        } else if (201 <= value && value <= 210) {
            return V1_10;
        } else if (48 <= value && value <= 110) {
            return V1_9;
        } else if (6 <= value && value <= 47) {
            return V1_8;
        } else if (value <= 0) {
            return null;
        } else if (value <= 5) {
            Bukkit.getLogger().info("[AAC5] Unsupported protocol " + value + " (assuming " + V1_8 + ")");
            return V1_8;
        } else {
            Bukkit.getLogger().info("[AAC5] Unsupported protocol " + value + " (assuming " + V1_17 + ")");
            return V1_17;
        }
    }
}
