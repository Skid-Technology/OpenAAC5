package me.konsolas.aac.checks.combat;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;
import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.checks.BaseCheck;
import org.bukkit.entity.Player;
import org.hipparchus.stat.StatUtils;

public class AutoClickerCheck extends BaseCheck {

    private boolean checkTiming;

    private long lastClick;

    private long lastSwing;

    private long lastTick;

    private final double[] clickSamples;

    private int sampleIndex;

    private final ClickTickDelayCheck tickDelayCheck;

    private final ClickTimingCheck timingCheck;

    private final NoSwingCheck noSwingCheck;

    public AutoClickerCheck(AAC aacPlugin, Player player) {
        super(aacPlugin, player, ("autoclicker"));
        this.checkTiming = false;
        this.sampleIndex = 0;
        this.tickDelayCheck = this.getConfiguration().getBoolean("check_tick_delay") ? new ClickTickDelayCheck(aacPlugin, player) : null;
        this.timingCheck = this.getConfiguration().getBoolean("check_no_swing") ? new ClickTimingCheck(aacPlugin, player) : null;
        this.noSwingCheck = this.getConfiguration().getBoolean("check_timing") ? new NoSwingCheck(aacPlugin, player) : null;
        this.clickSamples = new double[this.getConfiguration().getInt("sample_size")];
    }

    public void updateCount() {
        if (this.tickDelayCheck != null) {
            this.tickDelayCheck.tickWithClicksThisTick();
        }
        if (this.timingCheck != null) {
            this.timingCheck.tick();
        }
        if (this.noSwingCheck != null) {
            this.noSwingCheck.tick();
        }
        this.lastTick = System.currentTimeMillis();
    }

    public void tickWithTickDelayCheck() {
        if (this.tickDelayCheck != null) {
            this.tickDelayCheck.tick();
        }
        if (this.noSwingCheck != null) {
            this.noSwingCheck.tickWithLastSwing();
        }
    }

    public void tick() {
        long now = System.currentTimeMillis();
        if (this.timingCheck != null) {
            this.timingCheck.tickWithPreviousAttack();
        }
        if (!this.checkTiming && now - this.lastClick >= 100L && System.currentTimeMillis() - this.lastTick <= 1000L) {
            if (this.lastSwing != 0L) {
                double value = (now - this.lastSwing) / 1000.0;
                this.clickSamples[this.sampleIndex++] = value;
                if (this.sampleIndex >= this.clickSamples.length) {
                    this.sampleIndex = 0;
                    double[] values = this.clickSamples;
                    double value2 = StatUtils.mean(values);
                    double[] values2 = this.clickSamples;
                    double value3 = StatUtils.variance(values2);
                    float value4 = (float) (1.0 / value2);
                    FeatureType feature = FeatureType.AAC_CLICK_CPS;
                    this.recordFeature(feature, value4);
                    float value5 = (float) value3;
                    FeatureType feature2 = FeatureType.AAC_CLICK_VAR;
                    this.recordFeature(feature2, value5);
                }
            }
            this.lastSwing = now;
        } else {
            this.lastSwing = 0L;
        }
    }

    public void updateBlockPosition(BlockPosition blockPosition, PlayerDigType playerDigType) {
        this.checkTiming = playerDigType == PlayerDigType.START_DESTROY_BLOCK;
    }

    public void tickWithCheckTiming() {
        this.checkTiming = false;
        this.lastClick = System.currentTimeMillis();
    }
}
