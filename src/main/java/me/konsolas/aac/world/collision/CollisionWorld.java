package me.konsolas.aac.world.collision;

import java.util.Collection;
import java.util.function.Predicate;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import org.bukkit.Material;
import org.bukkit.util.Vector;

public interface CollisionWorld {

    Collection findCollection(AxisAlignedBoundingBox box);

    boolean checkCollisionWorld(AxisAlignedBoundingBox box, double value, double value2, double value3);

    Material findMaterial(Vector vector);

    boolean checkCollisionWorld(AxisAlignedBoundingBox box, Predicate predicate);
}
