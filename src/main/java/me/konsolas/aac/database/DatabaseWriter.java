package me.konsolas.aac.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureEvent;

class DatabaseWriter implements Runnable {

    final AAC plugin;

    final PlayerDatabase database;

    DatabaseWriter(PlayerDatabase database, AAC plugin) {
        this.database = database;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            ArrayList values = new ArrayList();
            while (PlayerDatabase.checkPlayerDatabase(this.database)) {
                values.add((FeatureEvent) PlayerDatabase.findBlockingQueue(this.database).take());
                PlayerDatabase.findBlockingQueue(this.database).drainTo(values);
                try {
                    PlayerDatabase playerDatabase = this.database;
                    PlayerDatabase.updatePlayerDatabase(playerDatabase, values);
                } catch (SQLException sQLException) {
                    this.plugin.getLogger().log(Level.SEVERE, "Failed to save events to database", (Throwable) sQLException);
                }
                values.clear();
                Thread.sleep(1000L);
            }
        } catch (InterruptedException interruptedException) {
            this.plugin.getLogger().info(PlayerDatabase.findThread(this.database).getName() + " exiting");
        }
    }
}
