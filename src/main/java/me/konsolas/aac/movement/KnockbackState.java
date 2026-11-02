package me.konsolas.aac.movement;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class KnockbackState {

    private final Location origin;

    private final Location destination;

    private final Vector velocity;

    private final float horizontalMagnitude;

    public KnockbackState(Location origin, Location destination, Vector velocity, float horizontalMagnitude) {
        this.origin = origin;
        this.destination = destination;
        this.velocity = velocity;
        this.horizontalMagnitude = horizontalMagnitude;
    }

    public Location getOrigin() {
        return this.origin;
    }

    public Location getDestination() {
        return this.destination;
    }

    public Vector getVelocity() {
        return this.velocity;
    }

    public float getHorizontalMagnitude() {
        return this.horizontalMagnitude;
    }
}
