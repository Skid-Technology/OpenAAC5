package me.konsolas.aac.api.internal;

import java.util.Set;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.AACAPI;
import me.konsolas.aac.api.AACCustomFeatureProvider;
import me.konsolas.aac.api.AACExemption;
import org.bukkit.entity.Player;

public class AACApiImplementation implements AACAPI {

    private final AAC plugin;

    public AACApiImplementation(AAC plugin) {
        this.plugin = plugin;
    }

    @Override
    public void addExemption(Player player, AACExemption aACExemption) {
        this.plugin.getPacketDispatcher().addExemption(player, aACExemption);
    }

    @Override
    public void removeExemption(Player player, AACExemption aACExemption) {
        this.plugin.getPacketDispatcher().removeExemption(player, aACExemption);
    }

    @Override
    public Set getExemptions(Player player) {
        return this.plugin.getPacketDispatcher().getExemptions(player);
    }

    @Override
    public void registerCustomFeatureProvider(AACCustomFeatureProvider aACCustomFeatureProvider) {
        this.plugin.getAnalysisManager().registerFeatureProvider(aACCustomFeatureProvider);
    }
}
