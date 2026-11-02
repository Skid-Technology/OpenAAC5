package me.konsolas.aac.checks;

import java.util.Map;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;

public class ViolationBuffer {

    private float level;

    private long lastUpdate;

    private final boolean mitigate;

    private final float threshold;

    private final float maximum;

    private final float decay;

    public ViolationBuffer(boolean mitigate, float threshold, float value2, float decay) {
        this.mitigate = mitigate;
        this.threshold = threshold;
        this.maximum = Math.max(value2, 1.0F);
        this.decay = decay;
    }

    public boolean checkViolationBuffer(float value) {
        long now = System.currentTimeMillis();
        this.level = Math.min(Math.max(Math.max(this.level - this.decay * (float) (now - this.lastUpdate) / 1000.0F, 0.0F) + value, 0.0F), this.maximum);
        this.lastUpdate = now;
        return this.mitigate && this.level >= this.threshold;
    }

    public float getLevel() {
        return this.level;
    }

    public static ViolationBuffer create(Map entries) {
        return new ViolationBuffer((Boolean) entries.get("mitigate"), ((Number) entries.get("threshold")).floatValue(), ((Number) entries.get("max")).floatValue(), ((Number) entries.get("decay")).floatValue());
    }

    public static ViolationBuffer create(ConfigurationSection section, String text) {
        Map values = ((ConfigurationSection) Objects.requireNonNull(section.get(text))).getValues(false);
        Map entries = values;
        return create(entries);
    }
}
