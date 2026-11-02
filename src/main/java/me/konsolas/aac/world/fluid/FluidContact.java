package me.konsolas.aac.world.fluid;

public class FluidContact {

    private final boolean touching;

    private final boolean flowing;

    private final double depth;

    public FluidContact(boolean touching, boolean flowing, double depth) {
        this.touching = touching;
        this.flowing = flowing;
        this.depth = depth;
    }

    public boolean isTouching() {
        return this.touching;
    }

    public boolean isFlowing() {
        return this.flowing;
    }

    public double getDepth() {
        return this.depth;
    }
}
