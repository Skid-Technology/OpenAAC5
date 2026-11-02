package me.konsolas.aac.world.collision;

import java.util.Collection;
import java.util.Collections;

public class EmptyCollisionShape extends CollisionShape {

    private static final EmptyCollisionShape INSTANCE = new EmptyCollisionShape();

    private EmptyCollisionShape() {
    }

    public static CollisionShape getCollisionShape() {
        return INSTANCE;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Collection getBoxesAt(int value, int value2, int value3) {
        return Collections.emptyList();
    }

    @Override
    public Collection getBoxes() {
        return Collections.emptyList();
    }
}
