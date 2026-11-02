package me.konsolas.aac.player;

import java.util.UUID;

public class PlayerLoginInfo {

    private final UUID playerId;

    private final long firstSeen;

    private final String address;

    private final long lastSeen;

    private final boolean exempt;

    public PlayerLoginInfo(UUID playerId, long firstSeen, String address, long lastSeen, boolean exempt) {
        this.playerId = playerId;
        this.firstSeen = firstSeen;
        this.address = address;
        this.lastSeen = lastSeen;
        this.exempt = exempt;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public long getFirstSeen() {
        return this.firstSeen;
    }

    public String getAddress() {
        return this.address;
    }

    public long getLastSeen() {
        return this.lastSeen;
    }

    public boolean isExempt() {
        return this.exempt;
    }

    public PlayerLoginInfo copy() {
        return new PlayerLoginInfo(this.playerId, this.firstSeen, this.address, this.lastSeen, !this.exempt);
    }
}
