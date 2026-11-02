package me.konsolas.aac.checks.combat;

import me.konsolas.aac.world.BlockRayTraceResult;

class RayTraceCandidate {

    private final double distance;

    private final double tolerance;

    private final BlockRayTraceResult result;

    RayTraceCandidate(double distance, double tolerance, BlockRayTraceResult result) {
        this.distance = distance;
        this.tolerance = tolerance;
        this.result = result;
    }

    private float calculateValue() {
        if (!Double.isFinite(this.distance)) {
            return 1.0F;
        } else {
            return this.distance > this.tolerance ? (float) (this.distance - this.tolerance) + 1.0F : (float) this.distance - 3.0F;
        }
    }

    private boolean checkRayTraceCandidate(RayTraceCandidate rayTraceCandidate) {
        if (rayTraceCandidate == null) {
            return true;
        } else if (Double.isFinite(this.distance) && !Double.isFinite(rayTraceCandidate.distance)) {
            return true;
        } else {
            return this.distance <= this.tolerance && rayTraceCandidate.distance > rayTraceCandidate.tolerance ? true : this.calculateValue() < rayTraceCandidate.calculateValue();
        }
    }

    static boolean checkRayTraceCandidate(RayTraceCandidate rayTraceCandidate, RayTraceCandidate rayTraceCandidate2) {
        return rayTraceCandidate.checkRayTraceCandidate(rayTraceCandidate2);
    }

    static double calculateValueWithDistance(RayTraceCandidate rayTraceCandidate) {
        return rayTraceCandidate.distance;
    }

    static float calculateValue(RayTraceCandidate rayTraceCandidate) {
        return rayTraceCandidate.calculateValue();
    }

    static double calculateValueWithTolerance(RayTraceCandidate rayTraceCandidate) {
        return rayTraceCandidate.tolerance;
    }

    static BlockRayTraceResult findBlockRayTraceResult(RayTraceCandidate rayTraceCandidate) {
        return rayTraceCandidate.result;
    }
}
