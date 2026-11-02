package me.konsolas.aac.checks.player;

import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.checks.BaseCheck;
import org.bukkit.entity.Player;

public class ActionStateTracker extends BaseCheck {

    private boolean movedThisSecond;

    private boolean attackedThisSecond;

    private boolean interactedThisSecond;

    private int movementSamples;

    private int attackSamples;

    private int interactionSamples;

    public ActionStateTracker(AAC aacPlugin, Player player) {
        super(aacPlugin, player, (String) (null));
        this.movedThisSecond = false;
        this.attackedThisSecond = false;
        this.interactedThisSecond = false;
    }

    public void updateTime(long value) {
        if (value % 20L == 0L) {
            if (this.movedThisSecond) {
                this.movementSamples++;
            }
            if (this.attackedThisSecond) {
                this.attackSamples++;
            }
            if (this.interactedThisSecond) {
                this.interactionSamples++;
            }
            this.movedThisSecond = false;
            this.attackedThisSecond = false;
            this.interactedThisSecond = false;
        }
        if (value % 200L == 0L) {
            if (this.movementSamples > 0) {
                float value2 = this.movementSamples * 20.0F / 1200.0F;
                FeatureType feature = FeatureType.AAC_NORM_MOVE;
                this.recordFeature(feature, value2);
                this.movementSamples = 0;
            }
            if (this.attackSamples > 0) {
                float value3 = this.attackSamples * 20.0F / 1200.0F;
                FeatureType feature2 = FeatureType.AAC_NORM_ATTACK;
                this.recordFeature(feature2, value3);
                this.attackSamples = 0;
            }
            if (this.interactionSamples > 0) {
                float value4 = this.interactionSamples * 20.0F / 1200.0F;
                FeatureType feature3 = FeatureType.AAC_NORM_INTERACT;
                this.recordFeature(feature3, value4);
                this.interactionSamples = 0;
            }
        }
    }

    public void recordMovement() {
        this.movedThisSecond = true;
    }

    public void recordAttack() {
        this.attackedThisSecond = true;
    }

    public void recordInteraction() {
        this.interactedThisSecond = true;
    }
}
