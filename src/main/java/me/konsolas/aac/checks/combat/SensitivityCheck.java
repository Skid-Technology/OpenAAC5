package me.konsolas.aac.checks.combat;

import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.checks.BaseCheck;
import org.bukkit.entity.Player;

public class SensitivityCheck extends BaseCheck {

    private boolean previousSnap;

    public SensitivityCheck(AAC aacPlugin, Player player) {
        super(aacPlugin, player, ("aimbot"));
    }

    public boolean checkHeadSnap(int value, int value2) {
        if (value >= 0 && value <= 200) {
            if (value2 >= 0 && value2 <= 200) {
                if (value != value2) {
                    if (this.previousSnap) {
                        FeatureType feature = FeatureType.AAC_AIMBOT_SENS_MISMATCH;
                        float value3 = Math.abs(value - value2);
                        FeatureType feature2 = feature;
                        this.recordFeature(feature2, value3);
                    }
                    this.previousSnap = true;
                    return false;
                } else {
                    this.previousSnap = false;
                    return true;
                }
            } else {
                if (this.previousSnap) {
                    float value4 = value2;
                    FeatureType feature3 = FeatureType.AAC_AIMBOT_SENS_Y_OOR;
                    this.recordFeature(feature3, value4);
                }
                this.previousSnap = true;
                return false;
            }
        } else {
            if (this.previousSnap) {
                float value5 = value;
                FeatureType feature4 = FeatureType.AAC_AIMBOT_SENS_X_OOR;
                this.recordFeature(feature4, value5);
            }
            this.previousSnap = true;
            return false;
        }
    }
}
