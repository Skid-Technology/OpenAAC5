package me.konsolas.aac.metrics;

import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.Bukkit;

class MetricsSubmissionTask extends TimerTask {

    final Timer timer;

    final MetricsLite metrics;

    MetricsSubmissionTask(MetricsLite metrics, Timer timer) {
        this.metrics = metrics;
        this.timer = timer;
    }

    @Override
    public void run() {
        if (!MetricsLite.access$000(this.metrics).isEnabled()) {
            this.timer.cancel();
        } else {
            Bukkit.getScheduler().runTask(MetricsLite.access$000(this.metrics), this::lambda$run$0);
        }
    }

    private void lambda$run$0() {
        MetricsLite.access$100(this.metrics);
    }
}
