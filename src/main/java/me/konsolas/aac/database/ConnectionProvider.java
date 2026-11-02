package me.konsolas.aac.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import org.bukkit.Bukkit;

class ConnectionProvider implements AutoCloseable {

    private final Supplier connectionFactory;

    private final DatabaseDialect dialect;

    private Connection connection;

    ConnectionProvider(Supplier connectionFactory, DatabaseDialect dialect) {
        this.connectionFactory = connectionFactory;
        this.dialect = dialect;
        this.connection = (Connection) connectionFactory.get();
    }

    public Connection getConnection() throws SQLException {
        if (this.connection == null || !this.connection.isValid(30)) {
            Bukkit.getLogger().info("[AAC5] Reconnecting to database...");
            this.connection = (Connection) this.connectionFactory.get();
        }
        return new ConnectionDelegate(this.connection);
    }

    public DatabaseDialect getDialect() {
        return this.dialect;
    }

    @Override
    public void close() throws SQLException {
        this.connection.close();
    }
}
