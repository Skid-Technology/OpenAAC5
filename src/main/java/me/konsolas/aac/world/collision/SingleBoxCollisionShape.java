package me.konsolas.aac.world.collision;

import java.util.Collection;
import java.util.Collections;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;

public class SingleBoxCollisionShape extends CollisionShape {

    private final AxisAlignedBoundingBox boundingBox;

    public SingleBoxCollisionShape(AxisAlignedBoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Collection getBoxesAt(int value, int value2, int value3) {
        return Collections.singletonList(this.boundingBox.offset(value, value2, value3));
    }

    @Override
    public Collection getBoxes() {
        return Collections.singletonList(this.boundingBox);
    }
}
