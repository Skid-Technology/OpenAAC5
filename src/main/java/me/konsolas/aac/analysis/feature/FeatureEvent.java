package me.konsolas.aac.analysis.feature;

import java.util.UUID;

public class FeatureEvent {

    private final long timestamp;

    private final UUID playerId;

    private final FeatureType type;

    private final float value;

    public FeatureEvent(long timestamp, UUID playerId, FeatureType type, float value) {
        this.timestamp = timestamp;
        this.playerId = playerId;
        this.type = type;
        this.value = value;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public FeatureType getType() {
        return this.type;
    }

    public float getValue() {
        return this.value;
    }
}
