package com.github.zhitron.jdbc_director;

import com.github.zhitron.universal.Logger;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * Spring JDBC 操作协调器，负责管理数据源连接、事务控制及连接生命周期。
 *
 * <p>此类继承自 JdbcDirector，专门用于与 Spring 框架集成。它利用 Spring 的 DataSourceUtils
 * 来获取和释放数据库连接，从而更好地与 Spring 的事务管理机制协同工作。</p>
 *
 * <p>SpringJdbcDirector 实例通常通过 SpringJdbcDirectorBuilder 构建，确保灵活且类型安全的配置方式。</p>
 *
 * @author zhitron
 */
public class SpringJdbcDirector extends JdbcDirector {

    /**
     * Spring DataSourceUtils 类中的 getConnection 方法引用。
     */
    private static Method getConnection;

    /**
     * Spring DataSourceUtils 类中的 releaseConnection 方法引用。
     */
    private static Method releaseConnection;

    /**
     * Spring DataSourceUtils 类中的 isConnectionTransactional 方法引用。
     */
    private static Method isConnectionTransactional;

    /*
     * 静态初始化块，用于加载 Spring DataSourceUtils 类及其相关方法。
     * 如果加载失败，将记录错误日志。
     */
    static {
        try {
            Class<?> DataSourceUtils = Class.forName("org.springframework.jdbc.datasource.DataSourceUtils");
            getConnection = DataSourceUtils.getMethod("getConnection", DataSource.class);
            releaseConnection = DataSourceUtils.getMethod("releaseConnection", Connection.class, DataSource.class);
            isConnectionTransactional = DataSourceUtils.getMethod("isConnectionTransactional", Connection.class, DataSource.class);
        } catch (Exception e) {
            if (Logger.isEnabledError()) {
                Logger.error("Error initializing SpringJdbcDirector. Cause: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 构造一个新的 SpringJdbcDirector 实例。
     *
     * @param dataSourceRouter     数据源路由器，用于动态选择数据源
     * @param transactionIsolation 事务隔离级别
     * @param autoCommit           期望的自动提交行为
     */
    protected SpringJdbcDirector(DataSourceRouter dataSourceRouter, TransactionIsolation transactionIsolation, boolean autoCommit) {
        super(dataSourceRouter, transactionIsolation, autoCommit);
    }

    /**
     * 获取一个 SpringJdbcDirectorBuilder 实例，用于构建配置好的 SpringJdbcDirector 对象。
     *
     * @return 返回一个新的 SpringJdbcDirectorBuilder 实例
     */
    public static SpringJdbcDirectorBuilder builder() {
        return new SpringJdbcDirectorBuilder();
    }

    /**
     * 获取一个数据库连接，使用指定的数据上下文、SQL语句和参数列表。
     *
     * <p>此方法会尝试复用当前线程已有的连接，如果没有则创建新的连接，并根据配置设置
     * 事务隔离级别和自动提交行为。该方法使用 Spring 的 DataSourceUtils 来获取连接。</p>
     *
     * @param data   数据上下文对象
     * @param sql    SQL语句
     * @param params SQL语句中的命名参数映射
     * @param args   参数列表
     * @return 返回一个数据库连接条目
     */
    @Override
    public ConnectionEntry acquireConnection(Object data, String sql, Map<String, Object> params, List<Object> args) {
        DataSourceEntry dataSourceEntry = super.acquireDataSource(data, sql, params, args);
        try {
            DataSource dataSource = dataSourceEntry.dataSource();
            Connection connection = (Connection) getConnection.invoke(null, dataSource);
            boolean autoCommit = connection.getAutoCommit();
            boolean connectionTransactional = (boolean) isConnectionTransactional.invoke(null, connection, dataSource);
            if (Logger.isEnabledDebug()) {
                Logger.debug("JDBC Connection [%s] will" + (connectionTransactional ? " " : " not ") + "be managed by Spring", connection);
            }
            return new SpringConnectionEntry(dataSourceEntry, connection, autoCommit, connectionTransactional);
        } catch (Exception e) {
            throw new PersistenceException("Error acquireConnection connectionEntry. Cause: " + e, e);
        }
    }

    /**
     * 释放指定的数据库连接。
     *
     * <p>如果连接是 SpringConnectionEntry 类型，则使用 Spring 的 DataSourceUtils 来释放连接。
     * 否则，调用父类的 releaseConnection 方法。</p>
     *
     * @param connectionEntry 要释放的连接条目
     */
    @Override
    public void releaseConnection(ConnectionEntry connectionEntry) {
        if (connectionEntry instanceof SpringConnectionEntry) {
            SpringConnectionEntry springConnectionEntry = (SpringConnectionEntry) connectionEntry;
            try {
                if (connectionEntry != null) {
                    releaseConnection.invoke(null, connectionEntry.connectionInstance(), connectionEntry.dataSourceEntry().dataSource());
                }
            } catch (Exception e) {
                throw new PersistenceException("Error releasing connectionEntry. Cause: " + e, e);
            }
        } else {
            super.releaseConnection(connectionEntry);
        }
    }

    /**
     * 开始事务处理。
     *
     * <p>如果连接是 SpringConnectionEntry 类型，则记录日志表明事务由 Spring 控制。
     * 否则，调用父类的 beginTransaction 方法。</p>
     *
     * @param connectionEntry 要开启事务的连接条目
     */
    @Override
    public void beginTransaction(ConnectionEntry connectionEntry) {
        if (connectionEntry instanceof SpringConnectionEntry) {
            if (Logger.isEnabledDebug()) {
                Logger.debug("The opening transaction is controlled by Spring");
            }
        } else {
            super.beginTransaction(connectionEntry);
        }
    }

    /**
     * 提交当前事务。
     *
     * <p>如果连接是 SpringConnectionEntry 类型，并且连接不是由 Spring 管理的事务，
     * 也不是自动提交的，则执行提交操作。否则，调用父类的 commitTransaction 方法。</p>
     *
     * @param connectionEntry 要提交事务的连接条目
     */
    @Override
    public void commitTransaction(ConnectionEntry connectionEntry) {
        if (connectionEntry instanceof SpringConnectionEntry) {
            SpringConnectionEntry springConnectionEntry = (SpringConnectionEntry) connectionEntry;
            try {
                if (connectionEntry != null) {
                    Connection connection = connectionEntry.connectionInstance();
                    if (!springConnectionEntry.springTransactional && !springConnectionEntry.autoCommit) {
                        if (Logger.isEnabledDebug()) {
                            Logger.debug("Committing JDBC Connection [%s]", connection);
                        }
                        connection.commit();
                    }
                }
            } catch (Exception e) {
                throw new TransactionException("Error commit transaction. Cause: " + e.getMessage(), e);
            }
        } else {
            super.commitTransaction(connectionEntry);
        }
    }

    /**
     * 回滚当前事务。
     *
     * <p>如果连接是 SpringConnectionEntry 类型，并且连接不是由 Spring 管理的事务，
     * 也不是自动提交的，则执行回滚操作。否则，调用父类的 rollbackTransaction 方法。</p>
     *
     * @param connectionEntry 要回滚事务的连接条目
     */
    @Override
    public void rollbackTransaction(ConnectionEntry connectionEntry) {
        if (connectionEntry instanceof SpringConnectionEntry) {
            SpringConnectionEntry springConnectionEntry = (SpringConnectionEntry) connectionEntry;
            try {
                if (connectionEntry != null) {
                    Connection connection = connectionEntry.connectionInstance();
                    if (!springConnectionEntry.springTransactional && !springConnectionEntry.autoCommit) {
                        if (Logger.isEnabledDebug()) {
                            Logger.debug("Rolling back JDBC Connection [%s]", connection);
                        }
                        connection.rollback();
                    }
                }
            } catch (Exception e) {
                throw new TransactionException("Error rollback transaction. Cause: " + e.getMessage(), e);
            }
        } else {
            super.rollbackTransaction(connectionEntry);
        }
    }

    /**
     * Spring 连接条目，用于封装数据源条目、数据库连接实例以及连接的自动提交和事务管理状态。
     */
    private static final class SpringConnectionEntry extends ConnectionEntry {
        /**
         * 连接的自动提交状态。
         */
        private final boolean autoCommit;

        /**
         * 连接是否由 Spring 管理事务。
         */
        private final boolean springTransactional;

        /**
         * 创建一个新的 SpringConnectionEntry 实例。
         *
         * @param dataSourceEntry     数据源条目，必须非空
         * @param connectionInstance  数据库连接实例，必须非空
         * @param autoCommit          连接的自动提交状态
         * @param springTransactional 连接是否由 Spring 管理事务
         * @throws NullPointerException 如果任一参数为 null
         */
        public SpringConnectionEntry(DataSourceEntry dataSourceEntry, Connection connectionInstance, boolean autoCommit, boolean springTransactional) {
            super(dataSourceEntry, connectionInstance);
            this.autoCommit = autoCommit;
            this.springTransactional = springTransactional;
        }
    }
}
