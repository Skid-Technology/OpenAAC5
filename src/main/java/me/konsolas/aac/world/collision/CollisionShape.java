package me.konsolas.aac.world.collision;

import java.util.Collection;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;

public abstract class CollisionShape {

    public abstract boolean isEmpty();

    public abstract Collection<AxisAlignedBoundingBox> getBoxesAt(int value, int value2, int value3);

    public abstract Collection<AxisAlignedBoundingBox> getBoxes();
}
