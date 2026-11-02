package me.konsolas.aac.checks.combat;

import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.checks.BaseCheck;
import me.konsolas.aac.util.time.ExpiringCounter;
import org.bukkit.entity.Player;

public class MouseDeltaCheck extends BaseCheck {

    private final ExpiringCounter sampleTimer;

    public MouseDeltaCheck(AAC aacPlugin, Player player) {
        super(aacPlugin, player, ("aimbot"));
        this.sampleTimer = new ExpiringCounter();
    }

    public void updateCount(int value, int value2, float value3, float value4, float value5) {
        float value6 = value5 * 0.6F + 0.2F;
        float value7 = value6 * value6 * value6 * 8.0F;
        float value8 = value * value7 * 0.15F;
        float value9 = value2 * value7 * 0.15F;
        float value10 = Math.abs(value3 - value8);
        float value11 = Math.abs(value4 - value9);
        if (value10 > 0.01 && this.sampleTimer.tryAcquire()) {
            FeatureType feature = FeatureType.AAC_AIMBOT_BAD_DELTA_X;
            this.recordFeature(feature, value10);
        }
        if (value11 > 0.01 && this.sampleTimer.tryAcquire()) {
            FeatureType feature2 = FeatureType.AAC_AIMBOT_BAD_DELTA_Y;
            this.recordFeature(feature2, value11);
        }
    }
}
