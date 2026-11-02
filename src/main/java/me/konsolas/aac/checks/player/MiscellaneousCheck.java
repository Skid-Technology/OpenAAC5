package me.konsolas.aac.checks.player;

import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.checks.BaseCheck;
import org.bukkit.entity.Player;

public class MiscellaneousCheck extends BaseCheck {

    private long lastRotation;

    private float previousYaw;

    private float previousPitch;

    private final boolean checkAbilities;

    private final boolean checkPitch;

    private final boolean checkRotationRate;

    public MiscellaneousCheck(AAC aacPlugin, Player player) {
        super(aacPlugin, player, ("misc"));
        this.checkAbilities = this.getConfiguration().getBoolean("invalid_pitch");
        this.checkPitch = this.getConfiguration().getBoolean("player_abilities");
        this.checkRotationRate = this.getConfiguration().getBoolean("rotation_rate");
    }

    public void updateTime(long value) {
        if (this.getPlayer().getAllowFlight()) {
            this.lastRotation = value;
        } else if (this.checkPitch && value > 100L && value - this.lastRotation > 100L) {
            FeatureType feature = FeatureType.AAC_MISC_ABILITIES;
            this.recordFeature(feature, (float) 1.0F);
        }
    }

    public void updateTime(long value, float value2, float value3) {
        float value4 = value2 - this.previousYaw;
        float value5 = value3 - this.previousPitch;
        this.previousYaw = value2;
        this.previousPitch = value3;
        if (this.checkRotationRate && (value4 > 1.0E8F || value5 > 1.0E8F)) {
            FeatureType feature = FeatureType.AAC_MISC_ROTATION;
            float value6 = Math.max(value4, value5);
            FeatureType feature2 = feature;
            this.recordFeature(feature2, value6);
        }
        if (this.checkAbilities && value > 100L && (value2 > 90.1F || value2 < -90.1F)) {
            FeatureType feature3 = FeatureType.AAC_MISC_PITCH;
            this.recordFeature(feature3, value2);
        }
    }
}
