package me.konsolas.aac.database;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

class ConnectionDelegate implements Connection {

    private final Connection delegate;

    ConnectionDelegate(Connection delegate) {
        this.delegate = delegate;
    }

    @Override
    public Statement createStatement() throws SQLException {
        return this.delegate.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String text) throws SQLException {
        return this.delegate.prepareStatement(text);
    }

    @Override
    public CallableStatement prepareCall(String text) throws SQLException {
        return this.delegate.prepareCall(text);
    }

    @Override
    public String nativeSQL(String text) throws SQLException {
        return this.delegate.nativeSQL(text);
    }

    @Override
    public void setAutoCommit(boolean matches) throws SQLException {
        this.delegate.setAutoCommit(matches);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return this.delegate.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        this.delegate.commit();
    }

    @Override
    public void rollback() throws SQLException {
        this.delegate.rollback();
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.delegate.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return this.delegate.getMetaData();
    }

    @Override
    public void setReadOnly(boolean matches) throws SQLException {
        this.delegate.setReadOnly(matches);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return this.delegate.isReadOnly();
    }

    @Override
    public void setCatalog(String text) throws SQLException {
        this.delegate.setCatalog(text);
    }

    @Override
    public String getCatalog() throws SQLException {
        return this.delegate.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int value) throws SQLException {
        this.delegate.setTransactionIsolation(value);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return this.delegate.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return this.delegate.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        this.delegate.clearWarnings();
    }

    @Override
    public Statement createStatement(int value, int value2) throws SQLException {
        return this.delegate.createStatement(value, value2);
    }

    @Override
    public PreparedStatement prepareStatement(String text, int value, int value2) throws SQLException {
        return this.delegate.prepareStatement(text, value, value2);
    }

    @Override
    public CallableStatement prepareCall(String text, int value, int value2) throws SQLException {
        return this.delegate.prepareCall(text, value, value2);
    }

    @Override
    public Map getTypeMap() throws SQLException {
        return this.delegate.getTypeMap();
    }

    @Override
    public void setTypeMap(Map entries) throws SQLException {
        this.delegate.setTypeMap(entries);
    }

    @Override
    public void setHoldability(int value) throws SQLException {
        this.delegate.setHoldability(value);
    }

    @Override
    public int getHoldability() throws SQLException {
        return this.delegate.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return this.delegate.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String text) throws SQLException {
        return this.delegate.setSavepoint(text);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        this.delegate.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        this.delegate.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int value, int value2, int value3) throws SQLException {
        return this.delegate.createStatement(value, value2, value3);
    }

    @Override
    public PreparedStatement prepareStatement(String text, int value, int value2, int value3) throws SQLException {
        return this.delegate.prepareStatement(text, value, value2, value3);
    }

    @Override
    public CallableStatement prepareCall(String text, int value, int value2, int value3) throws SQLException {
        return this.delegate.prepareCall(text, value, value2, value3);
    }

    @Override
    public PreparedStatement prepareStatement(String text, int value) throws SQLException {
        return this.delegate.prepareStatement(text, value);
    }

    @Override
    public PreparedStatement prepareStatement(String text, int[] values) throws SQLException {
        return this.delegate.prepareStatement(text, values);
    }

    @Override
    public PreparedStatement prepareStatement(String text, String[] texts) throws SQLException {
        return this.delegate.prepareStatement(text, texts);
    }

    @Override
    public Clob createClob() throws SQLException {
        return this.delegate.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return this.delegate.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return this.delegate.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return this.delegate.createSQLXML();
    }

    @Override
    public boolean isValid(int value) throws SQLException {
        return this.delegate.isValid(value);
    }

    @Override
    public void setClientInfo(String text, String text2) throws SQLClientInfoException {
        this.delegate.setClientInfo(text, text2);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        this.delegate.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String text) throws SQLException {
        return this.delegate.getClientInfo(text);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return this.delegate.getClientInfo();
    }

    @Override
    public Array createArrayOf(String text, Object[] results) throws SQLException {
        return this.delegate.createArrayOf(text, results);
    }

    @Override
    public Struct createStruct(String text, Object[] results) throws SQLException {
        return this.delegate.createStruct(text, results);
    }

    @Override
    public void setSchema(String text) throws SQLException {
        this.delegate.setSchema(text);
    }

    @Override
    public String getSchema() throws SQLException {
        return this.delegate.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        this.delegate.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int value) throws SQLException {
        this.delegate.setNetworkTimeout(executor, value);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return this.delegate.getNetworkTimeout();
    }

    @Override
    public Object unwrap(Class classValue) throws SQLException {
        return this.delegate.unwrap(classValue);
    }

    @Override
    public boolean isWrapperFor(Class classValue) throws SQLException {
        return this.delegate.isWrapperFor(classValue);
    }
}
