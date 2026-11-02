package me.konsolas.aac.checks.combat;

import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.checks.BaseCheck;
import org.bukkit.entity.Player;

public class PitchSpreadCheck extends BaseCheck {

    private final float[] pitchSamples;

    private int sampleIndex;

    private float lastPitch;

    private int sampleCount;

    public PitchSpreadCheck(AAC aacPlugin, Player player) {
        super(aacPlugin, player, ("aimbot"));
        this.pitchSamples = new float[20];
        this.sampleIndex = 0;
        this.lastPitch = 0.0F;
    }

    public void updateValue(float value, float value2) {
        if (value2 > 0.0 && value > 0.0) {
            this.pitchSamples[this.sampleIndex++] = value;
            if (this.sampleIndex == this.pitchSamples.length) {
                float value3 = 0.0F;
                float value4 = 0.0F;
                for (float value5 : this.pitchSamples) {
                    value3 += value5;
                    value4 += value5 * value5;
                }
                float value6 = value3 / this.pitchSamples.length;
                float value7 = value4 / this.pitchSamples.length - value6 * value6;
                double value8 = Math.abs(value6 - this.lastPitch);
                if (!(value8 > 1.0) || !(value7 > 36.0)) {
                    this.sampleCount = Math.max(this.sampleCount - 1, 0);
                } else if ((this.sampleCount += 4) > 20) {
                    FeatureType feature = FeatureType.AAC_AIMBOT_PITCH_SPREAD;
                    this.recordFeature(feature, value7);
                }
                this.sampleIndex = 0;
                this.lastPitch = value6;
            }
        }
    }
}
