package me.konsolas.aac.util.collection;

import java.util.AbstractList;
import java.util.Arrays;

public class FixedSizeList extends AbstractList {

    private final Object[] elements;

    private int cursor;

    public FixedSizeList(int value) {
        this.elements = new Object[value];
    }

    @Override
    public boolean add(Object result) {
        this.elements[this.cursor++] = result;
        if (this.cursor == this.elements.length) {
            this.cursor = 0;
        }
        return true;
    }

    public Object latest() {
        return this.get(this.elements.length - 1);
    }

    public void advance() {
        Arrays.fill(this.elements, null);
    }

    @Override
    public Object get(int value) {
        return this.elements[(this.cursor + value) % this.elements.length];
    }

    @Override
    public int size() {
        return this.elements.length;
    }
}
