package me.konsolas.aac.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.analyzer.AccountFeatureAnalyzer;
import me.konsolas.aac.analysis.analyzer.AverageFeatureAnalyzer;
import me.konsolas.aac.analysis.analyzer.CountFeatureAnalyzer;
import me.konsolas.aac.analysis.analyzer.FeatureAnalyzer;
import me.konsolas.aac.analysis.analyzer.FrequencyFeatureAnalyzer;
import me.konsolas.aac.analysis.analyzer.PercentileFeatureAnalyzer;
import me.konsolas.aac.analysis.analyzer.RateFeatureAnalyzer;
import me.konsolas.aac.analysis.analyzer.TTestFeatureAnalyzer;
import me.konsolas.aac.analysis.analyzer.ThresholdFeatureAnalyzer;
import me.konsolas.aac.analysis.feature.FeatureAccumulator;
import me.konsolas.aac.analysis.feature.FeatureEvent;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.player.PlayerLoginInfo;
import org.bukkit.configuration.ConfigurationSection;

public class FeatureRegistry {

    private static final Collection<FeatureAnalyzer> analyzers;

    private final AAC plugin;

    private final Map<FeatureAnalyzer, FeatureWeight> featureWeights = new HashMap();

    public FeatureRegistry(AAC plugin) {
        this.plugin = plugin;
        ConfigurationSection section = Objects.requireNonNull(plugin.getConfig().getConfigurationSection("features"));
        for (FeatureAnalyzer featureAnalyzer : analyzers) {
            double value;
            if ((value = section.getDouble(featureAnalyzer.getConfigurationPath() + ".weight")) > 0.0) {
                this.featureWeights.put(featureAnalyzer, new FeatureWeight(section.getString(featureAnalyzer.getConfigurationPath() + ".format"), value));
            }
        }
    }

    public List<FeatureResult> analyze(List<FeatureEvent> values, PlayerLoginInfo playerLoginInfo) {
        FeatureAccumulator featureAccumulator = new FeatureAccumulator();
        for (FeatureEvent event : values) {
            FeatureType type = event.getType();
            float value = event.getValue();
            FeatureType feature = type;
            featureAccumulator.record(feature, value);
        }
        ArrayList<FeatureResult> values2 = new ArrayList();
        for (Entry<FeatureAnalyzer, FeatureWeight> entry : this.featureWeights.entrySet()) {
            FeatureAnalyzer featureAnalyzer = (FeatureAnalyzer) entry.getKey();
            String text = FeatureWeight.findString((FeatureWeight) entry.getValue());
            featureAnalyzer.analyze(values, featureAccumulator, playerLoginInfo, text).map(result -> {
                double value2 = FeatureWeight.calculateValue((FeatureWeight) entry.getValue());
                double value3 = value2;
                return result.scaledBy(value3);
            }).ifPresent(values2::add);
        }
        values2.sort(Comparator.comparingDouble(FeatureResult::getScore).reversed());
        return values2;
    }

    static {
        ArrayList values = new ArrayList();
        values.add(new TTestFeatureAnalyzer("aac_delays_break", FeatureType.AAC_DELAYS_BREAK, 0.1, true));
        values.add(new TTestFeatureAnalyzer("aac_delays_place", FeatureType.AAC_DELAYS_PLACE, 0.1, true));
        values.add(new TTestFeatureAnalyzer("aac_delays_consume", FeatureType.AAC_DELAYS_CONSUME, 0.1, true));
        values.add(new TTestFeatureAnalyzer("aac_delays_bow", FeatureType.AAC_DELAYS_BOW, 0.1, true));
        values.add(new TTestFeatureAnalyzer("aac_delays_regen", FeatureType.AAC_DELAYS_REGEN, 0.1, true));
        values.add(new TTestFeatureAnalyzer("aac_delays_sneak", FeatureType.AAC_DELAYS_UNSNEAK, 0.1, true));
        values.add(new TTestFeatureAnalyzer("aac_delays_release", FeatureType.AAC_DELAYS_RELEASE, 0.1, true));
        values.add(new TTestFeatureAnalyzer("aac_delays_breakdelay", FeatureType.AAC_DELAYS_BREAKDELAY, 0.1, true));
        values.add(new FrequencyFeatureAnalyzer("aac_move_nofall", FeatureType.AAC_MOVE_NOFALL, FeatureType.AAC_NORM_MOVE, result -> 5.0 * result));
        values.add(new FrequencyFeatureAnalyzer("aac_move_vel", FeatureType.AAC_MOVE_VEL, FeatureType.AAC_NORM_MOVE, result -> 5.0 * result));
        values.add(new FrequencyFeatureAnalyzer("aac_move_vehicle", FeatureType.AAC_MOVE_VEHICLE, FeatureType.AAC_NORM_MOVE, result -> result));
        values.add(new RateFeatureAnalyzer("aac_move_elytra", FeatureType.AAC_MOVE_ELYTRA, FeatureType.AAC_NORM_MOVE, result -> result));
        values.add(new RateFeatureAnalyzer("aac_move_water", FeatureType.AAC_MOVE_WATER, FeatureType.AAC_NORM_MOVE, result -> result));
        values.add(new RateFeatureAnalyzer("aac_move_lava", FeatureType.AAC_MOVE_LAVA, FeatureType.AAC_NORM_MOVE, result -> result));
        values.add(new FrequencyFeatureAnalyzer("aac_move_noslow", FeatureType.AAC_MOVE_NOSLOW, FeatureType.AAC_NORM_MOVE, result -> result));
        values.add(new RateFeatureAnalyzer("aac_move_generic", FeatureType.AAC_MOVE_GENERIC, FeatureType.AAC_NORM_MOVE, result -> result));
        values.add(new FrequencyFeatureAnalyzer("aac_move_timer", FeatureType.AAC_MOVE_TIMER, FeatureType.AAC_NORM_MOVE, result -> 5.0 * result));
        values.add(new CountFeatureAnalyzer("aac_move_inf", FeatureType.AAC_MOVE_INF, result -> 100.0 * result));
        values.add(new FrequencyFeatureAnalyzer("aac_aimbot_spread", FeatureType.AAC_AIMBOT_PITCH_SPREAD, FeatureType.AAC_NORM_ATTACK, result -> 5.0 * result));
        values.add(new FrequencyFeatureAnalyzer("aac_aimbot_sync", FeatureType.AAC_AIMBOT_SENS_MISMATCH, FeatureType.AAC_NORM_ATTACK, result -> 20.0 * result));
        values.add(new FrequencyFeatureAnalyzer("aac_aimbot_horz", FeatureType.AAC_AIMBOT_SENS_X_OOR, FeatureType.AAC_NORM_ATTACK, result -> 20.0 * result));
        values.add(new FrequencyFeatureAnalyzer("aac_aimbot_vert", FeatureType.AAC_AIMBOT_SENS_Y_OOR, FeatureType.AAC_NORM_ATTACK, result -> 20.0 * result));
        values.add(new FrequencyFeatureAnalyzer("aac_aimbot_snap", FeatureType.AAC_AIMBOT_HEAD_SNAP, FeatureType.AAC_NORM_ATTACK, result -> 5.0 * result));
        values.add(new FrequencyFeatureAnalyzer("aac_aimbot_zero", FeatureType.AAC_AIMBOT_ZERO_POINT, FeatureType.AAC_NORM_ATTACK, result -> 5.0 * result));
        values.add(new FrequencyFeatureAnalyzer("aac_aimassist_horz", FeatureType.AAC_AIMBOT_BAD_DELTA_X, FeatureType.AAC_NORM_ATTACK, result -> 10.0 * result));
        values.add(new FrequencyFeatureAnalyzer("aac_aimassist_vert", FeatureType.AAC_AIMBOT_BAD_DELTA_Y, FeatureType.AAC_NORM_ATTACK, result -> 10.0 * result));
        values.add(new FrequencyFeatureAnalyzer("aac_clk_noswing", FeatureType.AAC_CLICK_NOSWING, FeatureType.AAC_NORM_ATTACK, result -> 20.0 * result));
        values.add(new FrequencyFeatureAnalyzer("aac_clk_tick_align", FeatureType.AAC_CLICK_TICK_DELAY, FeatureType.AAC_NORM_ATTACK, result -> result));
        values.add(new FrequencyFeatureAnalyzer("aac_clk_timing", FeatureType.AAC_CLICK_TIMING, FeatureType.AAC_NORM_ATTACK, result -> result));
        values.add(new AverageFeatureAnalyzer("aac_clk_cps", FeatureType.AAC_CLICK_CPS, result -> result * result / 5.0));
        values.add(new AverageFeatureAnalyzer("aac_clk_var", FeatureType.AAC_CLICK_VAR, result -> 0.01 / result));
        values.add(new FrequencyFeatureAnalyzer("aac_hitbox_multi", FeatureType.AAC_HITBOX_COUNT, FeatureType.AAC_NORM_ATTACK, result -> 5.0 * result));
        values.add(new FrequencyFeatureAnalyzer("aac_hitbox_miss", FeatureType.AAC_HITBOX_MISS, FeatureType.AAC_NORM_ATTACK, result -> 5.0 * result));
        values.add(new FrequencyFeatureAnalyzer("aac_hitbox_walls", FeatureType.AAC_HITBOX_WALLS, FeatureType.AAC_NORM_ATTACK, result -> 5.0 * result));
        values.add(new PercentileFeatureAnalyzer("aac_hitbox_reach", FeatureType.AAC_HITBOX_REACH, FeatureType.AAC_NORM_ATTACK, 90.0, result -> 200.0 * Math.cbrt(result)));
        values.add(new FrequencyFeatureAnalyzer("aac_interact_generic", FeatureType.AAC_INTERACT_GEN, FeatureType.AAC_NORM_INTERACT, result -> 1.0 * result));
        values.add(new FrequencyFeatureAnalyzer("aac_interact_break", FeatureType.AAC_INTERACT_BREAK, FeatureType.AAC_NORM_INTERACT, result -> 5.0 * result));
        values.add(new FrequencyFeatureAnalyzer("aac_interact_place", FeatureType.AAC_INTERACT_PLACE, FeatureType.AAC_NORM_INTERACT, result -> 2.0 * result));
        values.add(new CountFeatureAnalyzer("aac_misc_abilities", FeatureType.AAC_MISC_ABILITIES, result -> 100.0 * result));
        values.add(new CountFeatureAnalyzer("aac_misc_pitch", FeatureType.AAC_MISC_PITCH, result -> 100.0 * result));
        values.add(new CountFeatureAnalyzer("aac_misc_rotation", FeatureType.AAC_MISC_ROTATION, result -> 100.0 * result));
        values.add(new AccountFeatureAnalyzer("aac_new_user", 20.0, PlayerLoginInfo::getFirstSeen));
        values.add(new AccountFeatureAnalyzer("aac_new_addr", 20.0, PlayerLoginInfo::getLastSeen));
        values.add(new ThresholdFeatureAnalyzer("aac_info_move", 10.0, FeatureType.AAC_NORM_MOVE));
        values.add(new ThresholdFeatureAnalyzer("aac_info_combat", 10.0, FeatureType.AAC_NORM_ATTACK));
        values.add(new ThresholdFeatureAnalyzer("aac_info_interact", 10.0, FeatureType.AAC_NORM_INTERACT));
        analyzers = Collections.unmodifiableCollection(values);
    }
}
