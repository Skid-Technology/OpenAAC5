package me.konsolas.aac.movement;

import org.bukkit.util.Vector;

public class TimedVelocity {

    private final Vector velocity;

    private final long timestamp;

    public TimedVelocity(Vector velocity, long timestamp) {
        this.velocity = velocity;
        this.timestamp = timestamp;
    }

    public Vector getVector() {
        return this.velocity.clone();
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}
