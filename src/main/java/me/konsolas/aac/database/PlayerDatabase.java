package me.konsolas.aac.database;

import java.io.Closeable;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;
import java.util.logging.Level;
import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureEvent;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.player.PlayerLoginInfo;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerDatabase implements Closeable {

    private final AAC plugin;

    private final ConnectionProvider connectionProvider;

    private final BlockingQueue eventQueue = new LinkedBlockingQueue();

    private final Thread writerThread;

    private volatile boolean closed = true;

    private PlayerDatabase(AAC plugin, ConnectionProvider connectionProvider) throws SQLException {
        this.plugin = plugin;
        this.connectionProvider = connectionProvider;
        this.tick();
        this.writerThread = new Thread(new DatabaseWriter(this, plugin), "AAC Database Thread");
        this.writerThread.start();
    }

    public static PlayerDatabase create(AAC aacPlugin, String text) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException classNotFoundException) {
            throw new RuntimeException(classNotFoundException);
        }
        Supplier supplier = () -> {
            try {
                return DriverManager.getConnection("jdbc:sqlite:" + text);
            } catch (SQLException sQLException) {
                aacPlugin.getLogger().log(Level.SEVERE, "Could not connect to SQLite database", (Throwable) sQLException);
                throw new RuntimeException(sQLException);
            }
        };
        DatabaseDialect databaseDialect = DatabaseDialect.SQLITE;
        Supplier supplier2 = supplier;
        ConnectionProvider connectionProvider = new ConnectionProvider(supplier2, databaseDialect);
        ConnectionProvider connectionProvider2 = connectionProvider;
        return new PlayerDatabase(aacPlugin, connectionProvider2);
    }

    public static PlayerDatabase create(AAC aacPlugin, String text, int value, String text2, String text3, String text4) throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException classNotFoundException) {
            throw new RuntimeException(classNotFoundException);
        }
        String text5 = "jdbc:mysql://" + text + ":" + value + "/" + text2;
        Supplier supplier = () -> {
            try {
                return DriverManager.getConnection(text5, text3, text4);
            } catch (SQLException sQLException) {
                aacPlugin.getLogger().log(Level.SEVERE, "Could not connect to MySQL database", (Throwable) sQLException);
                throw new RuntimeException(sQLException);
            }
        };
        DatabaseDialect databaseDialect = DatabaseDialect.MYSQL;
        Supplier supplier2 = supplier;
        ConnectionProvider connectionProvider = new ConnectionProvider(supplier2, databaseDialect);
        ConnectionProvider connectionProvider2 = connectionProvider;
        return new PlayerDatabase(aacPlugin, connectionProvider2);
    }

    private void tick() throws SQLException {
        Connection connection = this.connectionProvider.getConnection();
        try {
            Statement statement = connection.createStatement();
            try {
                statement.execute("CREATE TABLE IF NOT EXISTS `aac_events` (  `time`       DATETIME NOT NULL,   `uuid`       BINARY(16) NOT NULL,   `type`       INT NOT NULL,   `data`       FLOAT NOT NULL);");
                statement.execute("CREATE TABLE IF NOT EXISTS `aac_players` (  `uuid`       BINARY(16) PRIMARY KEY,   `join`       DATETIME NOT NULL,  `addr`       VARCHAR(45) NOT NULL,  `addr_time`  DATETIME NOT NULL,  `exempt`     BOOLEAN NOT NULL);");
            } catch (Throwable throwable) {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            if (statement != null) {
                statement.close();
            }
        } catch (Throwable throwable3) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Throwable throwable4) {
                    throwable3.addSuppressed(throwable4);
                }
            }
            throw throwable3;
        }
        if (connection != null) {
            connection.close();
        }
        try {
            connection = this.connectionProvider.getConnection();
            try {
                Statement statement2 = connection.createStatement();
                try {
                    statement2.execute("CREATE INDEX `time_uuid_idx` ON `aac_events` (`time`, `uuid`)");
                } catch (Throwable throwable5) {
                    if (statement2 != null) {
                        try {
                            statement2.close();
                        } catch (Throwable throwable6) {
                            throwable5.addSuppressed(throwable6);
                        }
                    }
                    throw throwable5;
                }
                if (statement2 != null) {
                    statement2.close();
                }
            } catch (Throwable throwable7) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable throwable8) {
                        throwable7.addSuppressed(throwable8);
                    }
                }
                throw throwable7;
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException sQLException) {
        }
    }

    public int deleteEventsBefore(long value) throws SQLException {
        Connection connection = this.connectionProvider.getConnection();
        int value2;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM `aac_events` WHERE `time` < ?");
            try {
                preparedStatement.setTimestamp(1, new Timestamp(value));
                value2 = preparedStatement.executeUpdate();
            } catch (Throwable throwable) {
                if (preparedStatement != null) {
                    try {
                        preparedStatement.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (Throwable throwable3) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Throwable throwable4) {
                    throwable3.addSuppressed(throwable4);
                }
            }
            throw throwable3;
        }
        if (connection != null) {
            connection.close();
        }
        return value2;
    }

    public void recordFeature(Player player, FeatureType feature, float value) {
        FeatureEvent event = new FeatureEvent(System.currentTimeMillis(), player.getUniqueId(), feature, value);
        this.eventQueue.add(event);
        this.plugin.getAnalysisManager().updatePlayerWithAlertRecipients(player);
    }

    public CompletableFuture<Integer> resetPlayerAsync(OfflinePlayer offlinePlayer) {
        UUID uniqueId = offlinePlayer.getUniqueId();
        return CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = this.connectionProvider.getConnection();
                Integer value;
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM `aac_events` WHERE `uuid` = ?");
                    try {
                        preparedStatement.setBytes(1, encodeUuid(uniqueId));
                        value = preparedStatement.executeUpdate();
                    } catch (Throwable throwable) {
                        if (preparedStatement != null) {
                            try {
                                preparedStatement.close();
                            } catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                } catch (Throwable throwable3) {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (Throwable throwable4) {
                            throwable3.addSuppressed(throwable4);
                        }
                    }
                    throw throwable3;
                }
                if (connection != null) {
                    connection.close();
                }
                return value;
            } catch (SQLException sQLException) {
                throw new CompletionException(sQLException);
            }
        });
    }

    private void updateList(List<FeatureEvent> values) throws SQLException {
        Connection connection = this.connectionProvider.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `aac_events`(`time`, `uuid`, `type`, `data`) VALUES (?, ?, ?, ?);");
            try {
                for (FeatureEvent event : values) {
                    preparedStatement.setObject(1, new Timestamp(event.getTimestamp()));
                    UUID playerId = event.getPlayerId();
                    UUID uUID = playerId;
                    preparedStatement.setBytes(2, encodeUuid(uUID));
                    preparedStatement.setInt(3, event.getType().getId());
                    preparedStatement.setFloat(4, event.getValue());
                    preparedStatement.addBatch();
                }
                connection.setAutoCommit(false);
                preparedStatement.executeBatch();
                connection.setAutoCommit(true);
            } catch (Throwable throwable) {
                if (preparedStatement != null) {
                    try {
                        preparedStatement.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (Throwable throwable3) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Throwable throwable4) {
                    throwable3.addSuppressed(throwable4);
                }
            }
            throw throwable3;
        }
        if (connection != null) {
            connection.close();
        }
    }

    public PlayerLoginInfo findPlayerLoginInfo(UUID uUID) throws SQLException {
        Connection connection = this.connectionProvider.getConnection();
        PlayerLoginInfo playerLoginInfo;
        decision: {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT `join`, `addr`, `addr_time`, `exempt` FROM `aac_players` WHERE `uuid` = ?");
                decision2: {
                    try {
                        preparedStatement.setBytes(1, encodeUuid(uUID));
                        ResultSet resultSet = preparedStatement.executeQuery();
                        if (!resultSet.next()) {
                            playerLoginInfo = null;
                            break decision2;
                        }
                        playerLoginInfo = new PlayerLoginInfo(uUID, resultSet.getTimestamp(1).getTime(), resultSet.getString(2), resultSet.getTimestamp(3).getTime(), resultSet.getBoolean(4));
                    } catch (Throwable throwable) {
                        if (preparedStatement != null) {
                            try {
                                preparedStatement.close();
                            } catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                    break decision;
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (Throwable throwable3) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable throwable4) {
                        throwable3.addSuppressed(throwable4);
                    }
                }
                throw throwable3;
            }
            if (connection != null) {
                connection.close();
            }
            return playerLoginInfo;
        }
        if (connection != null) {
            connection.close();
        }
        return playerLoginInfo;
    }

    public PlayerLoginInfo findPlayerLoginInfo(UUID uUID, InetAddress inetAddress) throws SQLException {
        long now = System.currentTimeMillis();
        String hostAddress = inetAddress.getHostAddress();
        PlayerLoginInfo playerLoginInfo = this.findPlayerLoginInfo(uUID);
        if (playerLoginInfo == null) {
            playerLoginInfo = new PlayerLoginInfo(uUID, now, hostAddress, now, false);
            Connection connection = this.connectionProvider.getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `aac_players`(`uuid`, `join`, `addr`, `addr_time`, `exempt`) VALUES (?, ?, ?, ?, ?)");
                try {
                    preparedStatement.setBytes(1, encodeUuid(uUID));
                    preparedStatement.setTimestamp(2, new Timestamp(playerLoginInfo.getFirstSeen()));
                    preparedStatement.setString(3, playerLoginInfo.getAddress());
                    preparedStatement.setTimestamp(4, new Timestamp(playerLoginInfo.getLastSeen()));
                    preparedStatement.setBoolean(5, playerLoginInfo.isExempt());
                    preparedStatement.executeUpdate();
                } catch (Throwable throwable) {
                    if (preparedStatement != null) {
                        try {
                            preparedStatement.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (Throwable throwable3) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable throwable4) {
                        throwable3.addSuppressed(throwable4);
                    }
                }
                throw throwable3;
            }
            if (connection != null) {
                connection.close();
            }
        } else if (!playerLoginInfo.getAddress().equals(hostAddress)) {
            playerLoginInfo = new PlayerLoginInfo(uUID, playerLoginInfo.getFirstSeen(), hostAddress, now, playerLoginInfo.isExempt());
            Connection connection2 = this.connectionProvider.getConnection();
            try {
                PreparedStatement preparedStatement2 = connection2.prepareStatement("UPDATE `aac_players` SET `addr` = ?, `addr_time` = ? WHERE `uuid` = ?");
                try {
                    preparedStatement2.setString(1, playerLoginInfo.getAddress());
                    preparedStatement2.setTimestamp(2, new Timestamp(playerLoginInfo.getLastSeen()));
                    preparedStatement2.setBytes(3, encodeUuid(uUID));
                    preparedStatement2.executeUpdate();
                } catch (Throwable throwable5) {
                    if (preparedStatement2 != null) {
                        try {
                            preparedStatement2.close();
                        } catch (Throwable throwable6) {
                            throwable5.addSuppressed(throwable6);
                        }
                    }
                    throw throwable5;
                }
                if (preparedStatement2 != null) {
                    preparedStatement2.close();
                }
            } catch (Throwable throwable7) {
                if (connection2 != null) {
                    try {
                        connection2.close();
                    } catch (Throwable throwable8) {
                        throwable7.addSuppressed(throwable8);
                    }
                }
                throw throwable7;
            }
            if (connection2 != null) {
                connection2.close();
            }
        }
        return playerLoginInfo;
    }

    public void setExempt(UUID uUID, boolean matches) throws SQLException {
        Connection connection = this.connectionProvider.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE `aac_players` SET `exempt` = ? WHERE `uuid` = ?");
            try {
                preparedStatement.setBoolean(1, matches);
                preparedStatement.setBytes(2, encodeUuid(uUID));
                preparedStatement.executeUpdate();
            } catch (Throwable throwable) {
                if (preparedStatement != null) {
                    try {
                        preparedStatement.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (Throwable throwable3) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Throwable throwable4) {
                    throwable3.addSuppressed(throwable4);
                }
            }
            throw throwable3;
        }
        if (connection != null) {
            connection.close();
        }
    }

    public List getEventsSince(long value, UUID uUID) throws SQLException {
        Connection connection = this.connectionProvider.getConnection();
        ArrayList values;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT `time`, `type`, `data` FROM `aac_events` WHERE `time` > ? AND `uuid` = ?");
            try {
                preparedStatement.setObject(1, new Timestamp(value));
                preparedStatement.setBytes(2, encodeUuid(uUID));
                ResultSet resultSet = preparedStatement.executeQuery();
                ArrayList values2 = new ArrayList();
                while (resultSet.next()) {
                    long time = resultSet.getTimestamp(1).getTime();
                    int intValue = resultSet.getInt(2);
                    values2.add(new FeatureEvent(time, uUID, FeatureType.fromId(intValue), resultSet.getFloat(3)));
                }
                values = values2;
            } catch (Throwable throwable) {
                if (preparedStatement != null) {
                    try {
                        preparedStatement.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (Throwable throwable3) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Throwable throwable4) {
                    throwable3.addSuppressed(throwable4);
                }
            }
            throw throwable3;
        }
        if (connection != null) {
            connection.close();
        }
        return values;
    }

    private static byte[] encodeUuid(UUID uUID) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uUID.getMostSignificantBits());
        byteBuffer.putLong(uUID.getLeastSignificantBits());
        return byteBuffer.array();
    }

    @Override
    public void close() {
        try {
            this.closed = false;
            this.writerThread.join();
        } catch (InterruptedException interruptedException) {
            this.plugin.getLogger().warning("Interrupted while closing database connection");
        }
        try {
            this.connectionProvider.close();
        } catch (Exception exception) {
            this.plugin.getLogger().log(Level.WARNING, "Could not close connection", (Throwable) exception);
        }
    }

    static boolean checkPlayerDatabase(PlayerDatabase playerDatabase) {
        return playerDatabase.closed;
    }

    static BlockingQueue findBlockingQueue(PlayerDatabase playerDatabase) {
        return playerDatabase.eventQueue;
    }

    static void updatePlayerDatabase(PlayerDatabase playerDatabase, List values) throws SQLException {
        playerDatabase.updateList(values);
    }

    static Thread findThread(PlayerDatabase playerDatabase) {
        return playerDatabase.writerThread;
    }
}
