package me.konsolas.aac.movement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InputStateIterator implements Iterator {

    private static final float[][] windowSamples = new float[][] { { 1.0F, 0.0F }, { 1.0F, -1.0F }, { 1.0F, 1.0F }, { 0.0F, 1.0F }, { 0.0F, -1.0F }, { -1.0F, 0.0F }, { -1.0F, 1.0F }, { -1.0F, -1.0F }, { 0.0F, 0.0F } };

    private int index = 0;

    private final List values = new ArrayList();

    public InputStateIterator(PlayerInputState playerInputState, float value, float value2, boolean matches, int value3, boolean matches2, Boolean matches3, Boolean matches4, Boolean matches5, Boolean matches6) {
        boolean matches7 = playerInputState.isSprinting() && playerInputState.getForward() > 0.0F;
        for (int index = 0; index <= value3; index++) {
            float[] values = new float[] { playerInputState.getForward(), playerInputState.getStrafe() };
            boolean sprinting = playerInputState.isSprinting();
            float[] values2 = values;
            this.addMovementCandidates(values2, matches3, matches4, matches5, matches6, index, value, value2, matches2, matches, matches7, sprinting);
            for (float[] values3 : windowSamples) {
                if (values3[0] != playerInputState.getForward() || values3[1] != playerInputState.getStrafe()) {
                    boolean sneaking = playerInputState.isSneaking();
                    this.addMovementCandidates(values3, matches3, matches4, matches5, matches6, index, value, value2, matches2, matches, matches7, sneaking);
                }
            }
        }
    }

    private void addMovementCandidates(float[] values, Boolean matches, Boolean matches2, Boolean matches3, Boolean matches4, int value, float value2, float value3, boolean matches5, boolean matches6, boolean matches7, boolean matches8) {
        if (matches8 && matches5 && (values[0] > 0.0F || matches7 && values[0] >= 0.0F)) {
            this.addInputVariants(values, true, false, matches, matches2, matches3, matches4, value2, value3, value);
            if (matches6) {
                this.addInputVariants(values, true, true, matches, matches2, matches3, matches4, value2, value3, value);
            }
        }
        this.addInputVariants(values, false, false, matches, matches2, matches3, matches4, value2, value3, value);
        if (matches6) {
            this.addInputVariants(values, false, true, matches, matches2, matches3, matches4, value2, value3, value);
        }
        if (!matches8 && matches5 && (values[0] > 0.0F || matches7 && values[0] >= 0.0F)) {
            this.addInputVariants(values, true, false, matches, matches2, matches3, matches4, value2, value3, value);
            if (matches6) {
                this.addInputVariants(values, true, true, matches, matches2, matches3, matches4, value2, value3, value);
            }
        }
    }

    private void addInputVariants(float[] values2, boolean matches, boolean matches2, Boolean matches3, Boolean matches4, Boolean matches5, Boolean matches6, float value, float value2, int value3) {
        boolean matches7 = matches3 == null ? false : matches3;
        boolean matches8 = matches4 == null ? false : matches4;
        boolean matches9 = matches5 == null ? true : matches5;
        boolean matches10 = matches6 == null ? true : matches6;
        this.values.add(new PlayerInputState(values2[0], values2[1], matches, matches2, matches7, matches8, matches9, matches10, value, value2, value3));
        if (matches5 == null) {
            this.values.add(new PlayerInputState(values2[0], values2[1], matches, matches2, matches7, matches8, false, matches10, value, value2, value3));
        }
        if (matches6 == null) {
            this.values.add(new PlayerInputState(values2[0], values2[1], matches, matches2, matches7, matches8, matches9, false, value, value2, value3));
        }
        if (matches3 == null) {
            this.values.add(new PlayerInputState(values2[0], values2[1], matches, matches2, true, matches8, matches9, matches10, value, value2, value3));
        }
        if (matches4 == null) {
            this.values.add(new PlayerInputState(values2[0], values2[1], matches, matches2, matches7, true, matches9, matches10, value, value2, value3));
        }
        if (matches3 == null && matches4 == null) {
            this.values.add(new PlayerInputState(values2[0], values2[1], matches, matches2, true, true, matches9, matches10, value, value2, value3));
        }
    }

    @Override
    public boolean hasNext() {
        return this.index < this.values.size();
    }

    public PlayerInputState getPlayerInputState() {
        return this.index >= this.values.size() ? null : (PlayerInputState) this.values.get(this.index++);
    }

    @Override
    public Object next() {
        return this.getPlayerInputState();
    }
}
