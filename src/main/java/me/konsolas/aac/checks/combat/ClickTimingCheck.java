package me.konsolas.aac.checks.combat;

import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.checks.BaseCheck;
import org.bukkit.entity.Player;

public class ClickTimingCheck extends BaseCheck {

    boolean previousAttack;

    public ClickTimingCheck(AAC aacPlugin, Player player) {
        super(aacPlugin, player, ("autoclicker"));
        this.previousAttack = false;
    }

    public void tick() {
        if (this.previousAttack) {
            FeatureType feature = FeatureType.AAC_CLICK_NOSWING;
            this.recordFeature(feature, (float) 0.0F);
        } else {
            this.previousAttack = true;
        }
    }

    public void tickWithPreviousAttack() {
        this.previousAttack = false;
    }
}
