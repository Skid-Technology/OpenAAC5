package me.konsolas.aac.timings;

import me.konsolas.aac.AAC;

public class CheckTask implements Runnable {

    private final AAC plugin;

    public CheckTask(AAC plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 1L, 1L);
    }

    @Override
    public void run() {
        this.plugin.getPacketDispatcher().tick();
    }
}
