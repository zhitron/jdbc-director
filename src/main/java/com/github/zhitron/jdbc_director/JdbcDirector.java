package com.github.zhitron.jdbc_director;

import com.github.zhitron.universal.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * JDBC 操作协调器，负责管理数据源连接、事务控制及连接生命周期。
 *
 * <p>此类通过组合模式提供对数据库操作的统一入口。它利用数据源路由器动态选择合适的数据源，
 * 并根据配置设置事务隔离级别和自动提交行为。此外，它支持线程内连接复用以提高性能。</p>
 *
 * <p>JdbcDirector 实例通常通过 JdbcDirectorBuilder 构建，确保灵活且类型安全的配置方式。</p>
 *
 * @author zhitron
 */
public class JdbcDirector {
    /**
     * 默认的数据上下文对象，用于无特定数据关联的场景。
     */
    private static final Object DEFAULT_DATA = null;

    /**
     * 默认的SQL语句标识，用于无具体SQL操作的场景。
     */
    private static final String DEFAULT_SQL = null;

    /**
     * 数据源路由器，用于根据上下文动态选择合适的数据源。
     */
    private final DataSourceRouter dataSourceRouter;

    /**
     * 事务隔离级别，定义数据库事务的隔离程度。
     */
    private final TransactionIsolation transactionIsolation;

    /**
     * 期望的自动提交状态，用于配置连接的行为。
     */
    private final boolean desiredAutoCommit;

    /**
     * 构造一个新的 JdbcDirector 实例。
     *
     * @param dataSourceRouter     数据源路由器，用于动态选择数据源
     * @param transactionIsolation 事务隔离级别
     * @param desiredAutoCommit    期望的自动提交行为
     */
    protected JdbcDirector(DataSourceRouter dataSourceRouter, TransactionIsolation transactionIsolation, boolean desiredAutoCommit) {
        this.dataSourceRouter = Objects.requireNonNull(dataSourceRouter);
        this.transactionIsolation = transactionIsolation == null ? TransactionIsolation.NONE : transactionIsolation;
        this.desiredAutoCommit = desiredAutoCommit;
    }

    /**
     * 获取一个 JdbcDirectorBuilder 实例，用于构建配置好的 JdbcDirector 对象。
     *
     * @return 返回一个新的 JdbcDirectorBuilder 实例
     */
    public static JdbcDirectorBuilder builder() {
        return new JdbcDirectorBuilder();
    }

    /**
     * 获取当前使用的数据源路由器。
     *
     * @return 返回数据源路由器实例
     */
    public DataSourceRouter dataSourceRouter() {
        return dataSourceRouter;
    }

    /**
     * 获取当前事务隔离级别。
     *
     * @return 返回事务隔离级别枚举值
     */
    public TransactionIsolation transactionIsolation() {
        return transactionIsolation;
    }

    /**
     * 获取期望的自动提交行为。
     *
     * @return 如果期望自动提交返回true，否则返回false
     */
    public boolean desiredAutoCommit() {
        return desiredAutoCommit;
    }

    /**
     * 获取数据源连接
     *
     * @param data   数据对象，用于数据源路由判断
     * @param sql    SQL语句，用于数据源路由判断
     * @param params 参数映射，用于数据源路由判断
     * @param args   参数列表，用于数据源路由判断
     * @return 数据源条目对象
     * @throws PersistenceException 当数据源路由器或数据源条目为空时抛出异常
     */
    public DataSourceEntry acquireDataSource(Object data, String sql, Map<String, Object> params, List<Object> args) {
        // 调试模式下输出连接开启日志
        if (Logger.isEnabledDebug()) {
            Logger.debug("Opening JDBC DataSource");
        }

        // 获取数据源路由器并进行空值检查
        DataSourceRouter dataSourceRouter = this.dataSourceRouter();
        if (dataSourceRouter == null) {
            throw new PersistenceException("Failed to get DataSourceRouter with a null value");
        }

        // 通过路由器确定具体的数据源
        DataSourceEntry dataSourceEntry = dataSourceRouter.determineDataSource(data, sql, params, args);
        if (dataSourceEntry == null) {
            throw new PersistenceException("Failed to get DataSourceEntry with a null value");
        }
        return dataSourceEntry;
    }

    /**
     * 获取一个数据库连接，使用默认的数据和SQL参数。
     *
     * @return 返回一个数据库连接条目
     */
    public final ConnectionEntry acquireConnection() {
        return this.acquireConnection(DEFAULT_DATA, DEFAULT_SQL, Collections.emptyMap(), Collections.emptyList());
    }

    /**
     * 获取一个数据库连接，使用指定的数据上下文。
     *
     * @param data 数据上下文对象
     * @return 返回一个数据库连接条目
     */
    public final ConnectionEntry acquireConnection(Object data) {
        return this.acquireConnection(data, DEFAULT_SQL, Collections.emptyMap(), Collections.emptyList());
    }

    /**
     * 获取一个数据库连接，使用指定的SQL语句和可变参数。
     *
     * @param sql    SQL语句
     * @param params SQL语句中的命名参数映射
     * @param args   SQL语句中的位置参数列表
     * @return 返回一个数据库连接条目
     */
    public final ConnectionEntry acquireConnection(String sql, Map<String, Object> params, Object... args) {
        return this.acquireConnection(DEFAULT_DATA, sql, params, args);
    }

    /**
     * 获取一个数据库连接，使用指定的SQL语句和参数列表。
     *
     * @param sql    SQL语句
     * @param params SQL语句中的命名参数映射
     * @param args   参数列表
     * @return 返回一个数据库连接条目
     */
    public final ConnectionEntry acquireConnection(String sql, Map<String, Object> params, List<Object> args) {
        return this.acquireConnection(DEFAULT_DATA, sql, params, args);
    }

    /**
     * 获取一个数据库连接，使用指定的数据上下文、SQL语句和可变参数。
     *
     * @param data   数据上下文对象
     * @param sql    SQL语句
     * @param params SQL语句中的命名参数映射
     * @param args   可变参数列表
     * @return 返回一个数据库连接条目
     */
    public final ConnectionEntry acquireConnection(Object data, String sql, Map<String, Object> params, Object... args) {
        return this.acquireConnection(data, sql, params, Arrays.asList(args));
    }

    /**
     * 获取一个数据库连接，使用指定的数据上下文、SQL语句和参数列表。
     *
     * <p>此方法会尝试复用当前线程已有的连接，如果没有则创建新的连接，并根据配置设置
     * 事务隔离级别和自动提交行为。</p>
     *
     * @param data   数据上下文对象
     * @param sql    SQL语句
     * @param params SQL语句中的命名参数映射
     * @param args   参数列表
     * @return 返回一个数据库连接条目
     */
    public ConnectionEntry acquireConnection(Object data, String sql, Map<String, Object> params, List<Object> args) {
        try {
            DataSourceEntry dataSourceEntry = acquireDataSource(data, sql, params, args);
            DataSource dataSource = dataSourceEntry.dataSource();
            Connection connection = dataSource.getConnection();
            if (connection == null) {
                throw new PersistenceException("Failed to get Connection with a null value");
            }
            ConnectionEntry connectionEntry = new ConnectionEntry(dataSourceEntry, connection);
            TransactionIsolation transactionIsolation = this.transactionIsolation();
            if (transactionIsolation != null) {
                connection.setTransactionIsolation(transactionIsolation.level);
            }
            boolean desiredAutoCommit = this.desiredAutoCommit();
            try {
                if (connection.getAutoCommit() != desiredAutoCommit) {
                    if (Logger.isEnabledDebug()) {
                        Logger.debug("Setting autocommit to " + desiredAutoCommit + " on JDBC Connection [" + connection + "]");
                    }
                    connection.setAutoCommit(desiredAutoCommit);
                }
            } catch (SQLException e) {
                // Only a very poorly implemented driver would fail here,
                // and there's not much we can do about that.
                throw new TransactionException("Error configuring AutoCommit. Your driver may not support getAutoCommit() or setAutoCommit(). Requested setting: " + desiredAutoCommit + ".  Cause: " + e.getMessage(), e);
            }
            return connectionEntry;
        } catch (SQLException e) {
            throw new PersistenceException("Error getting connection", e);
        }
    }

    /**
     * 释放指定的数据库连接。
     *
     * <p>在关闭连接之前，如果连接处于非自动提交状态，则尝试将其设置为自动提交，
     * 以避免某些数据库要求在关闭前必须提交或回滚事务。</p>
     *
     * <p>此方法会安全地尝试将连接的自动提交模式设置为 true（如果尚未处于该模式），
     * 然后关闭连接。如果在设置自动提交或关闭连接过程中发生异常，将记录错误并抛出
     * PersistenceException。</p>
     *
     * @param connectionEntry 要释放的连接条目
     */
    public void releaseConnection(ConnectionEntry connectionEntry) {
        try {
            if (connectionEntry != null) {
                Connection connection = connectionEntry.connectionInstance();
                if (!connection.isClosed()) {
                    try {
                        if (!connection.getAutoCommit()) {
                            // MyBatis does not call commit/rollback on a connectionEntry if just selects were performed.
                            // Some databases start transactions with select statements
                            // and they mandate a commit/rollback before closing the connectionEntry.
                            // A workaround is setting the autocommit to true before closing the connectionEntry.
                            // Sybase throws an exception here.
                            if (Logger.isEnabledDebug()) {
                                Logger.debug("Resetting autocommit to true on JDBC Connection [" + connectionEntry + "]");
                            }
                            connection.setAutoCommit(true);
                        }
                    } catch (SQLException e) {
                        if (Logger.isEnabledDebug()) {
                            Logger.debug("Error resetting autocommit to true before closing the connectionEntry.  Cause: " + e.getMessage());
                        }
                    }
                    if (Logger.isEnabledDebug()) {
                        Logger.debug("Releasing JDBC Connection [" + connectionEntry + "]");
                    }
                    connection.close();
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error releasing connection. Cause: " + e.getMessage(), e);
        }
    }

    /**
     * 安全地释放指定的数据库连接。
     *
     * <p>调用 {@link #releaseConnection(ConnectionEntry)} 方法，并捕获所有异常。
     * 如果发生异常，将记录错误日志并返回 false。</p>
     *
     * @param connectionEntry 要释放的连接条目
     * @return 如果连接成功释放返回 true，否则返回 false
     */
    public boolean releaseConnectionSafely(ConnectionEntry connectionEntry) {
        try {
            this.releaseConnection(connectionEntry);
            return true;
        } catch (Exception e) {
            if (Logger.isEnabledError()) {
                Logger.error("Error to release connection!", e);
            }
            return false;
        }
    }

    /**
     * 开始事务处理。
     *
     * <p>将连接的自动提交模式设置为 false，从而显式地开始一个事务。</p>
     *
     * <p>如果连接为空或已关闭，或在设置自动提交模式时发生异常，将抛出 TransactionException。</p>
     *
     * @param connectionEntry 要开启事务的连接条目
     */
    public void beginTransaction(ConnectionEntry connectionEntry) {
        try {
            if (connectionEntry != null) {
                Connection connection = connectionEntry.connectionInstance();
                if (!connection.getAutoCommit()) {
                    if (Logger.isEnabledDebug()) {
                        Logger.debug("Begin transaction on JDBC Connection [" + connection + "]");
                    }
                    connection.setAutoCommit(false);
                }
            }
        } catch (SQLException e) {
            throw new TransactionException("Error begin transaction. Cause: " + e.getMessage(), e);
        }
    }

    /**
     * 安全地开始事务处理。
     *
     * <p>调用 {@link #beginTransaction(ConnectionEntry)} 方法，并捕获所有异常。
     * 如果发生异常，将记录错误日志并返回 false。</p>
     *
     * @param connectionEntry 要开启事务的连接条目
     * @return 如果事务成功开始返回 true，否则返回 false
     */
    public boolean beginTransactionSafely(ConnectionEntry connectionEntry) {
        try {
            this.beginTransaction(connectionEntry);
            return true;
        } catch (Exception e) {
            if (Logger.isEnabledError()) {
                Logger.error("Error to begin transaction!", e);
            }
            return false;
        }
    }

    /**
     * 提交当前事务。
     *
     * <p>仅当连接处于非自动提交模式时才会执行提交操作。</p>
     *
     * <p>如果连接为空或已关闭，或在提交过程中发生异常，将抛出 TransactionException。</p>
     *
     * @param connectionEntry 要提交事务的连接条目
     */
    public void commitTransaction(ConnectionEntry connectionEntry) {
        try {
            if (connectionEntry != null) {
                Connection connection = connectionEntry.connectionInstance();
                if (!connection.getAutoCommit()) {
                    if (Logger.isEnabledDebug()) {
                        Logger.debug("Commit transaction on JDBC Connection [" + connection + "]");
                    }
                    connection.commit();
                }
            }
        } catch (Exception e) {
            throw new TransactionException("Error commit transaction. Cause: " + e.getMessage(), e);
        }
    }

    /**
     * 安全地提交当前事务。
     *
     * <p>调用 {@link #commitTransaction(ConnectionEntry)} 方法，并捕获所有异常。
     * 如果发生异常，将记录错误日志并返回 false。</p>
     *
     * @param connectionEntry 要提交事务的连接条目
     * @return 如果事务成功提交返回 true，否则返回 false
     */
    public boolean commitTransactionSafely(ConnectionEntry connectionEntry) {
        try {
            this.commitTransaction(connectionEntry);
            return true;
        } catch (Exception e) {
            if (Logger.isEnabledError()) {
                Logger.error("Error to commit transaction!", e);
            }
            return false;
        }
    }

    /**
     * 回滚当前事务。
     *
     * <p>仅当连接处于非自动提交模式时才会执行回滚操作。</p>
     *
     * <p>如果连接为空或已关闭，或在回滚过程中发生异常，将抛出 TransactionException。</p>
     *
     * @param connectionEntry 要回滚事务的连接条目
     */
    public void rollbackTransaction(ConnectionEntry connectionEntry) {
        try {
            if (connectionEntry != null) {
                Connection connection = connectionEntry.connectionInstance();
                if (!connection.getAutoCommit()) {
                    if (Logger.isEnabledDebug()) {
                        Logger.debug("Rollback transaction on JDBC Connection [" + connection + "]");
                    }
                    connection.rollback();
                }
            }
        } catch (Exception e) {
            throw new TransactionException("Error rollback transaction. Cause: " + e.getMessage(), e);
        }
    }

    /**
     * 安全地回滚当前事务。
     *
     * <p>调用 {@link #rollbackTransaction(ConnectionEntry)} 方法，并捕获所有异常。
     * 如果发生异常，将记录错误日志并返回 false。</p>
     *
     * @param connectionEntry 要回滚事务的连接条目
     * @return 如果事务成功回滚返回 true，否则返回 false
     */
    public boolean rollbackTransactionSafely(ConnectionEntry connectionEntry) {
        try {
            this.rollbackTransaction(connectionEntry);
            return true;
        } catch (Exception e) {
            if (Logger.isEnabledError()) {
                Logger.error("Error to rollback transaction!", e);
            }
            return false;
        }
    }

}
