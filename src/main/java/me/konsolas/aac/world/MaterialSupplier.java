package me.konsolas.aac.world;

import org.bukkit.Material;

@FunctionalInterface
public interface MaterialSupplier {

    Material getMaterial();
}
