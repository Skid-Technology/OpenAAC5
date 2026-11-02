package me.konsolas.aac.checks.combat;

import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.checks.BaseCheck;
import org.bukkit.entity.Player;

public class NoSwingCheck extends BaseCheck {

    private long lastSwing;

    private long lastAttack;

    public NoSwingCheck(AAC aacPlugin, Player player) {
        super(aacPlugin, player, ("autoclicker"));
    }

    public void tickWithLastSwing() {
        long now = System.currentTimeMillis();
        long value = now - this.lastSwing;
        this.lastSwing = System.currentTimeMillis();
        if (value < 25L) {
            this.lastAttack = now;
        }
    }

    public void tick() {
        long now = System.currentTimeMillis();
        long value = now - this.lastSwing;
        long value2 = now - this.lastAttack;
        if (value < 5L && value2 > 50L) {
            float value3 = (float) value / 1000.0F;
            FeatureType feature = FeatureType.AAC_CLICK_TIMING;
            this.recordFeature(feature, value3);
        }
    }
}
