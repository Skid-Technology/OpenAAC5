package me.konsolas.aac.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class AACCustomFeature {

    private final String info;

    private final double score;

    private final Map tooltip = new LinkedHashMap();

    public AACCustomFeature(String text, String info, double score, Map entries) {
        this.info = info;
        this.score = score;
        this.tooltip.put("id", text);
        this.tooltip.putAll(entries);
    }

    public String getInfo() {
        return this.info;
    }

    public Map getTooltip() {
        return Collections.unmodifiableMap(this.tooltip);
    }

    public double getScore() {
        return this.score;
    }
}
