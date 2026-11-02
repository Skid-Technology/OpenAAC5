package me.konsolas.aac.checks.combat;

import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.checks.BaseCheck;
import org.bukkit.entity.Player;

public class HeadSnapCheck extends BaseCheck {

    private float lastYaw;

    private float lastPitch;

    private final float[] yawSamples;

    private final float[] pitchSamples;

    private int sampleIndex;

    private float previousDelta;

    public HeadSnapCheck(AAC aacPlugin, Player player) {
        super(aacPlugin, player, ("aimbot"));
        this.yawSamples = new float[20];
        this.pitchSamples = new float[20];
        this.sampleIndex = 0;
        this.previousDelta = 0.0F;
    }

    public void updateValue(float value, float value2) {
        if (value2 > 0.0 && value > 0.0) {
            float value3 = Math.abs(value2 - this.lastPitch);
            float value4 = Math.abs(value - this.lastYaw);
            this.lastPitch = value2;
            this.lastYaw = value;
            float value5 = Math.abs(value3 - value2);
            float value6 = Math.abs(value4 - value);
            this.pitchSamples[this.sampleIndex] = value5;
            this.yawSamples[this.sampleIndex] = value6;
            if (++this.sampleIndex == 20) {
                this.sampleIndex = 0;
                for (int index = 0; index < 20; index++) {
                    if (this.yawSamples[index] == 0.0F || this.pitchSamples[index] == 0.0F) {
                        return;
                    }
                }
                float value7 = 0.0F;
                float value8 = 0.0F;
                float value9 = 0.0F;
                float value10 = 0.0F;
                for (int index2 = 0; index2 < 20; index2++) {
                    value7 += this.pitchSamples[index2];
                    value8 += this.pitchSamples[index2] * this.pitchSamples[index2];
                    value9 += this.yawSamples[index2];
                    value10 += this.yawSamples[index2] * this.yawSamples[index2];
                }
                float value11 = (float) (value8 / 20.0F - Math.pow(value7 / 20.0F, 2.0));
                float value12 = (float) (value10 / 20.0F - Math.pow(value9 / 20.0F, 2.0));
                if (value11 > 50.0F && value12 > 3.0F) {
                    if (++this.previousDelta > 3.0F) {
                        float value13 = value11 + value12;
                        FeatureType feature = FeatureType.AAC_AIMBOT_HEAD_SNAP;
                        this.recordFeature(feature, value13);
                    } else {
                        this.previousDelta = Math.max(this.previousDelta - 0.1F, 0.0F);
                    }
                }
            }
        }
    }
}
