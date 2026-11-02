package me.konsolas.aac.api;

import java.util.Collection;
import org.bukkit.OfflinePlayer;

public interface AACCustomFeatureProvider {

    Collection getFeaturesAsync(OfflinePlayer offlinePlayer);
}
