package me.konsolas.aac.checks.combat;

import org.bukkit.Location;

class TeleportState {

    private final int teleportId;

    private final Location origin;

    private Location destination;

    private boolean confirmed;

    TeleportState(int teleportId, Location origin) {
        this.teleportId = teleportId;
        this.origin = origin;
    }

    private void updateLocation(Location location) {
        if (this.destination == null) {
            this.destination = location;
        }
    }

    static void updateTeleportState(TeleportState teleportState, Location location) {
        teleportState.updateLocation(location);
    }

    static Location findLocation(TeleportState teleportState) {
        return teleportState.destination;
    }

    static boolean checkTeleportState(TeleportState teleportState) {
        return teleportState.confirmed;
    }

    static boolean checkTeleportState(TeleportState teleportState, boolean matches) {
        return teleportState.confirmed = matches;
    }

    static Location findLocationWithOrigin(TeleportState teleportState) {
        return teleportState.origin;
    }

    static int calculateCount(TeleportState teleportState) {
        return teleportState.teleportId;
    }
}
