package me.konsolas.aac.util.time;

import java.util.concurrent.atomic.AtomicLong;

public class ExpiringCounter {

    private final long delayMillis;

    private final AtomicLong lastUpdate = new AtomicLong();

    public ExpiringCounter() {
        this(1000L);
    }

    public ExpiringCounter(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public boolean tryAcquire() {
        long now = System.currentTimeMillis();
        long next = now + delayMillis;
        return lastUpdate.getAndUpdate(previous -> previous < now ? next : previous) < now;
    }
}
