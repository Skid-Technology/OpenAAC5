package me.konsolas.aac.compat;

import me.konsolas.aac.compat.v1_11.Minecraft111Access;
import me.konsolas.aac.compat.v1_13.Minecraft113Access;
import me.konsolas.aac.compat.v1_14.Minecraft114Access;
import me.konsolas.aac.compat.v1_16.Minecraft116Access;
import me.konsolas.aac.compat.v1_17.Minecraft117Access;
import me.konsolas.aac.compat.v1_8.Minecraft18Access;
import me.konsolas.aac.compat.v1_9.Minecraft19Access;
import me.konsolas.aac.protocol.ProtocolVersion;

public final class NativeAccessProvider {

    private static final NativeAccess INSTANCE;

    public static NativeAccess getInstance() {
        return INSTANCE;
    }

    static {
        ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
        switch(serverVersion) {
            case V1_8:
                INSTANCE = new Minecraft18Access();
                break;
            case V1_9:
            case V1_10:
                INSTANCE = new Minecraft19Access(serverVersion);
                break;
            case V1_11:
            case V1_12:
                INSTANCE = new Minecraft111Access(serverVersion);
                break;
            case V1_13:
                INSTANCE = new Minecraft113Access(serverVersion);
                break;
            case V1_14:
            case V1_15:
                INSTANCE = new Minecraft114Access(serverVersion);
                break;
            case V1_16:
                INSTANCE = new Minecraft116Access(serverVersion);
                break;
            case V1_17:
                INSTANCE = new Minecraft117Access();
                break;
            default:
                throw new AssertionError();
        }
    }
}
