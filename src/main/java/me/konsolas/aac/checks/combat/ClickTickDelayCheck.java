package me.konsolas.aac.checks.combat;

import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.checks.BaseCheck;
import org.bukkit.entity.Player;

public class ClickTickDelayCheck extends BaseCheck {

    private int previousClicks;

    private int clicksThisTick;

    private int previousDelta;

    private int violations;

    public ClickTickDelayCheck(AAC aacPlugin, Player player) {
        super(aacPlugin, player, ("autoclicker"));
    }

    public void tickWithClicksThisTick() {
        int value = this.clicksThisTick - this.previousClicks;
        if (value == this.previousDelta) {
            this.violations++;
            if (this.violations >= 16) {
                float value2 = this.violations;
                FeatureType feature = FeatureType.AAC_CLICK_TICK_DELAY;
                this.recordFeature(feature, value2);
            }
        } else {
            this.violations = 0;
        }
        this.previousDelta = value;
        this.previousClicks = this.clicksThisTick;
        this.clicksThisTick = 0;
    }

    public void tick() {
        this.clicksThisTick++;
    }
}
