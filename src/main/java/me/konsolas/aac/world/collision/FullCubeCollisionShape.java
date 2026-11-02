package me.konsolas.aac.world.collision;

import java.util.Collection;
import java.util.Collections;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;

public class FullCubeCollisionShape extends CollisionShape {

    private static final FullCubeCollisionShape INSTANCE = new FullCubeCollisionShape();

    private static final AxisAlignedBoundingBox UNIT_BOX = new AxisAlignedBoundingBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);

    private FullCubeCollisionShape() {
    }

    public static CollisionShape getCollisionShape() {
        return INSTANCE;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Collection getBoxesAt(int value, int value2, int value3) {
        return Collections.singletonList(new AxisAlignedBoundingBox(value, value2, value3, value + 1, value2 + 1, value3 + 1));
    }

    @Override
    public Collection getBoxes() {
        return Collections.singletonList(UNIT_BOX);
    }
}
