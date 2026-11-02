package me.konsolas.aac.movement;

public class RotationDelta {

    private final float yaw;

    private final float pitch;

    public RotationDelta(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }
}
