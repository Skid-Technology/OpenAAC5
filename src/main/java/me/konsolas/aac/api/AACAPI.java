package me.konsolas.aac.api;

import java.util.Set;
import org.bukkit.entity.Player;

public interface AACAPI {

    void addExemption(Player player, AACExemption aACExemption);

    void removeExemption(Player player, AACExemption aACExemption);

    Set getExemptions(Player player);

    void registerCustomFeatureProvider(AACCustomFeatureProvider aACCustomFeatureProvider);
}
