package me.konsolas.aac.analysis;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.configuration.ConfigurationSection;

public class AnalysisActionRule {

    private final long delayTicks;

    private final List<Pattern> includePatterns = new ArrayList();

    private final List<Pattern> excludePatterns = new ArrayList();

    private final Int2ObjectMap thresholdCommands = new Int2ObjectArrayMap();

    public AnalysisActionRule(ConfigurationSection section) {
        this.delayTicks = (long) (20.0 * section.getDouble("delay", 0.0));
        for (String text : section.getStringList("features")) {
            if (text.startsWith("-")) {
                this.excludePatterns.add(Pattern.compile(("\\Q" + text.substring(1) + "\\E").replace("*", "\\E.*\\Q")));
            } else {
                this.includePatterns.add(Pattern.compile(("\\Q" + text + "\\E").replace("*", "\\E.*\\Q")));
            }
        }
        ConfigurationSection configurationSection = section.getConfigurationSection("thresholds");
        if (configurationSection != null) {
            for (String text2 : configurationSection.getKeys(false)) {
                if (configurationSection.get(text2) instanceof String) {
                    this.thresholdCommands.put(Integer.parseInt(text2), Collections.singletonList(configurationSection.getString(text2)));
                } else {
                    this.thresholdCommands.put(Integer.parseInt(text2), configurationSection.getStringList(text2));
                }
            }
        }
    }

    public long getDelayTicks() {
        return this.delayTicks;
    }

    private boolean matchesFeature(String text) {
        for (Pattern pattern : this.excludePatterns) {
            if (pattern.matcher(text).matches()) {
                return false;
            }
        }
        for (Pattern pattern2 : this.includePatterns) {
            if (pattern2.matcher(text).matches()) {
                return true;
            }
        }
        return false;
    }

    public List matchingCommands(PlayerAnalysisReport playerAnalysisReport) {
        double value = 0.0;
        for (FeatureResult result : playerAnalysisReport.getFeatures()) {
            Object result2 = result.getDetails().get("id");
            String text = (String) result2;
            if (this.matchesFeature(text)) {
                value += result.getScore();
            }
        }
        Entry entry = null;
        ObjectIterator objectIterator = this.thresholdCommands.int2ObjectEntrySet().iterator();
        while (objectIterator.hasNext()) {
            Entry entry2 = (Entry) objectIterator.next();
            if (entry2.getIntKey() < value && (entry == null || entry2.getIntKey() > entry.getIntKey())) {
                entry = entry2;
            }
        }
        return entry == null ? null : (List) entry.getValue();
    }
}
