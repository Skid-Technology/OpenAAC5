package me.konsolas.aac.checks.combat;

import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.checks.BaseCheck;
import me.konsolas.aac.util.math.MouseSensitivityMath;
import me.konsolas.aac.util.math.MouseStepEstimator;
import me.konsolas.aac.util.time.ExpiringCounter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AimbotCheck extends BaseCheck {

    private long lastAttack;

    private final ExpiringCounter sampleTimer;

    private float lastPitch;

    private float lastYaw;

    private float previousPitch;

    private float previousYaw;

    private final double[] yawSamples;

    private final double[] pitchSamples;

    private int sampleIndex;

    private boolean checkMouseDelta;

    private boolean checkSensitivity;

    private double yawSensitivity;

    private double pitchSensitivity;

    private float lastSensitivity;

    private final SensitivityCheck sensitivityCheck;

    private final MouseDeltaCheck mouseDeltaCheck;

    private final HeadSnapCheck headSnapCheck;

    private final PitchSpreadCheck pitchSpreadCheck;

    private final boolean checkZeroPoint;

    public AimbotCheck(AAC aacPlugin, Player player) {
        super(aacPlugin, player, ("aimbot"));
        this.sampleTimer = new ExpiringCounter();
        this.yawSamples = new double[50];
        this.pitchSamples = new double[50];
        this.sampleIndex = 0;
        this.checkMouseDelta = false;
        this.sensitivityCheck = this.getConfiguration().getBoolean("check_sensitivity") ? new SensitivityCheck(aacPlugin, player) : null;
        this.mouseDeltaCheck = this.getConfiguration().getBoolean("check_mouse_delta") ? new MouseDeltaCheck(aacPlugin, player) : null;
        this.headSnapCheck = this.getConfiguration().getBoolean("check_head_snap") ? new HeadSnapCheck(aacPlugin, player) : null;
        this.pitchSpreadCheck = this.getConfiguration().getBoolean("check_pitch_spread") ? new PitchSpreadCheck(aacPlugin, player) : null;
        this.checkZeroPoint = this.getConfiguration().getBoolean("check_zero_point");
        Location location = player.getLocation();
        Location location2 = location;
        this.updateLocation(location2);
    }

    public void updateLocation(Location location) {
        this.previousYaw = this.lastYaw = location.getYaw();
        this.previousPitch = this.lastPitch = location.getPitch();
    }

    public void analyzeRotation(float value, float value2) {
        if (this.getPlayer().getVehicle() != null) {
            this.previousYaw = this.lastYaw = value;
            this.previousPitch = this.lastPitch = value2;
        } else {
            float value3 = Math.abs(value2 - this.previousYaw);
            float value4 = Math.abs(value - this.previousPitch);
            if (value3 > 0.1 && value4 > 0.1 && value3 < 180.0F && Math.abs(value) < 89.0F && Math.abs(this.previousPitch) < 89.0F) {
                this.yawSamples[this.sampleIndex] = value3;
                this.pitchSamples[this.sampleIndex] = value4;
                if (++this.sampleIndex == 50) {
                    this.sampleIndex = 0;
                    this.checkMouseDelta = true;
                    this.checkSensitivity = true;
                }
            }
            this.previousPitch = value;
            this.previousYaw = value2;
            if (System.currentTimeMillis() - this.lastAttack <= 500L) {
                if (this.pitchSpreadCheck != null) {
                    this.pitchSpreadCheck.updateValue(value4, value3);
                }
                if (this.headSnapCheck != null) {
                    this.headSnapCheck.updateValue(value4, value3);
                }
                if (this.checkMouseDelta && this.sensitivityCheck != null) {
                    this.updateRotation(value3, value4);
                }
                if (this.checkMouseDelta && this.checkZeroPoint) {
                    float value5 = (float) Math.abs(this.lastYaw - Math.round(this.lastYaw / this.yawSensitivity) * this.yawSensitivity);
                    float value6 = (float) Math.abs(value - Math.round(value2 / this.yawSensitivity) * this.yawSensitivity);
                    if (value6 < 1.0E-5 && value5 > 0.001 && this.sampleTimer.tryAcquire()) {
                        float value7 = (float) this.yawSensitivity;
                        FeatureType feature = FeatureType.AAC_AIMBOT_ZERO_POINT;
                        this.recordFeature(feature, value7);
                    }
                }
            }
        }
    }

    private void updateRotation(float value, float value2) {
        if (this.checkSensitivity) {
            this.checkSensitivity = false;
            double[] values = this.yawSamples;
            this.yawSensitivity = MouseStepEstimator.estimateStep(values);
            double[] values2 = this.pitchSamples;
            this.pitchSensitivity = MouseStepEstimator.estimateStep(values2);
            double value6 = this.yawSensitivity;
            double value7 = MouseSensitivityMath.estimateSensitivity(value6);
            double value8 = this.pitchSensitivity;
            double value9 = MouseSensitivityMath.estimateAbsoluteSensitivity(value8);
            int value10 = MouseSensitivityMath.sensitivityPercent(value7);
            int value11 = MouseSensitivityMath.sensitivityPercent(value9);
            if (!this.sensitivityCheck.checkHeadSnap(value10, value11)) {
                this.sampleIndex = 0;
                this.checkMouseDelta = false;
                return;
            }
            this.lastSensitivity = (float) value7;
        }
        double value12 = value;
        double value13 = this.yawSensitivity;
        double value14 = value12;
        int value15 = MouseSensitivityMath.mouseCounts(value14, value13);
        double value16 = value2;
        double value17 = this.pitchSensitivity;
        double value18 = value16;
        int value19 = MouseSensitivityMath.absoluteMouseCounts(value18, value17);
        float value20 = this.lastSensitivity;
        this.mouseDeltaCheck.updateCount(value15, value19, value, value2, value20);
    }

    public void tick() {
        this.lastAttack = System.currentTimeMillis();
    }
}
