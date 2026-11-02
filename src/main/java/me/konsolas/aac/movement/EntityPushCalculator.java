package me.konsolas.aac.movement;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class EntityPushCalculator {

    public static Vector calculatePush(Player player) {
        Vector vector = new Vector(0, 0, 0);
        for (Entity entity : player.getNearbyEntities(1.0, 1.0, 1.0)) {
            if (!entity.equals(player.getVehicle()) && entity instanceof LivingEntity) {
                double value = player.getLocation().getX() - entity.getLocation().getX();
                double value2 = player.getLocation().getZ() - entity.getLocation().getZ();
                double value3 = Math.max(Math.abs(value), Math.abs(value2));
                if (value3 > 0.01F) {
                    value3 = Math.sqrt(value3);
                    value /= value3;
                    value2 /= value3;
                    double value4 = Math.min(1.0, 1.0 / value3);
                    value *= value4;
                    value2 *= value4;
                    value *= 0.05;
                    value2 *= 0.05;
                    value *= 1.0;
                    value2 *= 1.0;
                    vector.add(new Vector(value, 0.0, value2));
                }
            }
        }
        return vector;
    }
}
