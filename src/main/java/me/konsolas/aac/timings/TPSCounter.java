package me.konsolas.aac.timings;

import java.util.Arrays;
import me.konsolas.aac.AAC;

public class TPSCounter implements Runnable {

    private final long lastTick = 1000000000L;

    private final int sampleIndex = 50;

    private long tickStart;

    private final long[] samples = new long[50];

    private int tickCount = 0;

    public TPSCounter(AAC aacPlugin) {
        this.tickStart = System.nanoTime();
        Arrays.fill(this.samples, 50000000L);
        aacPlugin.getServer().getScheduler().scheduleSyncRepeatingTask(aacPlugin, this, 1L, 1L);
    }

    public double calculateValue() {
        long value = 0L;
        for (long value2 : this.samples) {
            value += value2;
        }
        double value3 = value / 50.0;
        return 1.0E9 / value3;
    }

    @Override
    public void run() {
        long value = System.nanoTime();
        long value2 = value - this.tickStart;
        this.tickStart = value;
        this.samples[this.tickCount++] = value2;
        if (this.tickCount >= 50) {
            this.tickCount = 0;
        }
    }
}
