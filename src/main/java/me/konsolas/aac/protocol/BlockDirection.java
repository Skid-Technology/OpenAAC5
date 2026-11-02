package me.konsolas.aac.protocol;

import com.comphenix.protocol.wrappers.EnumWrappers.Direction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum BlockDirection {

    DOWN,
    BLOCK_DIRECTION,
    NORTH,
    SOUTH,
    WEST,
    EAST;

    private static final List DIRECTIONS = Collections.unmodifiableList(Arrays.asList(values()));

    static BlockDirection create(Direction direction) {
        return (BlockDirection) DIRECTIONS.get(direction.ordinal());
    }

    static BlockDirection create(int value) {
        return (BlockDirection) DIRECTIONS.get(value);
    }
}
