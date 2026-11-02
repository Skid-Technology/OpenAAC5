package me.konsolas.aac.checks;

import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.database.PlayerDatabase;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public abstract class BaseCheck {

    private final AAC plugin;

    private final Player player;

    private final ConfigurationSection configuration;

    public BaseCheck(AAC plugin, Player player, String text) {
        this.plugin = plugin;
        this.player = player;
        this.configuration = text == null ? plugin.getConfig().getConfigurationSection("checks") : plugin.getConfig().getConfigurationSection("checks." + text);
        if (this.configuration == null) {
            throw new IllegalArgumentException("Missing configuration section for checks." + text);
        }
    }

    protected ConfigurationSection getConfiguration() {
        return this.configuration;
    }

    public AAC getPlugin() {
        return this.plugin;
    }

    public Player getPlayer() {
        return this.player;
    }

    protected void recordFeature(FeatureType feature, float value) {
        PlayerDatabase database = this.getPlugin().getDatabase();
        Player player = this.getPlayer();
        database.recordFeature(player, feature, value);
    }
}
