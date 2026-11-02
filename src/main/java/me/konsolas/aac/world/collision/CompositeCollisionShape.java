package me.konsolas.aac.world.collision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;

public class CompositeCollisionShape extends CollisionShape {

    private final List<AxisAlignedBoundingBox> shapes;

    public CompositeCollisionShape(List shapes) {
        this.shapes = shapes;
        if (shapes.size() < 2) {
            throw new IllegalArgumentException("shapes.size() = " + shapes.size());
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Collection getBoxesAt(int value, int value2, int value3) {
        ArrayList values = new ArrayList(this.shapes.size());
        for (AxisAlignedBoundingBox box : this.shapes) {
            values.add(box.offset(value, value2, value3));
        }
        return values;
    }

    @Override
    public Collection getBoxes() {
        return Collections.unmodifiableList(this.shapes);
    }
}
