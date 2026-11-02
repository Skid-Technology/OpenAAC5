package me.konsolas.aac.util.collection;

import java.util.Arrays;
import java.util.Iterator;

public class BlockGrid implements Iterable {

    private final int width;

    private final int height;

    private final int depth;

    private final Object[] elements;

    public BlockGrid(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.elements = new Object[width * height * depth];
    }

    public Object get(int value, int value2, int value3) {
        return this.elements[this.height * this.depth * value + this.depth * value2 + value3];
    }

    public void set(int value, int value2, int value3, Object result) {
        this.elements[this.height * this.depth * value + this.depth * value2 + value3] = result;
    }

    @Override
    public Iterator iterator() {
        return Arrays.asList(this.elements).iterator();
    }
}
