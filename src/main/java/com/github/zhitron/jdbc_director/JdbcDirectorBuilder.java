package com.github.zhitron.jdbc_director;

import javax.sql.DataSource;
import java.util.LinkedList;
import java.util.List;

/**
 * JdbcDirector 构建器类，用于配置和组装 JdbcDirector 实例。
 *
 * <p>该构建器支持设置默认数据源、注册多个数据源及其对应的键（DataSourceKey）、
 * 添加自定义的数据源路由策略（DataSourceKeyRouter），以及配置事务隔离级别和自动提交行为。</p>
 *
 * <p>通过链式调用方式提供流畅的 API 体验，并最终通过 {@link #build()} 方法生成配置完成的
 * {@link JdbcDirector} 实例。</p>
 *
 * @author zhitron
 */
public class JdbcDirectorBuilder {
    /**
     * 存储一系列数据源键路由策略。
     * 这些策略决定了执行 SQL 时应选择哪个数据源。
     */
    private final List<DataSourceKeyRouter> dataSourceKeyRouters = new LinkedList<>();
    /**
     * 存储注册的数据源列表。
     * 每个数据源与对应的 DataSourceKey 关联。
     */
    private final List<DataSourceEntry> dataSources = new LinkedList<>();
    /**
     * 默认的数据源，当没有指定具体数据源时使用。
     */
    private DataSource defaultDataSource;
    /**
     * 当前配置的事务隔离级别。
     * 默认为 TransactionIsolation.NONE（不支持事务）。
     */
    private TransactionIsolation transactionIsolation = TransactionIsolation.NONE;

    /**
     * 当前配置的期望自动提交行为。
     * 默认为 true（自动提交）。
     */
    private boolean desiredAutoCommit = true;

    /**
     * 私有构造函数，防止外部直接实例化。
     * 使用静态方法 builder() 创建新实例。
     */
    protected JdbcDirectorBuilder() {
    }

    /**
     * 设置默认的数据源。
     *
     * @param defaultDataSource 要设置为默认的数据源
     * @return 返回当前构建器实例，以支持链式调用
     */
    public JdbcDirectorBuilder defaultDataSource(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
        return this;
    }

    /**
     * 注册一个数据源及其对应的路由规则。
     *
     * @param dataSourceKey 数据源键，用于唯一标识该数据源
     * @param dataSource    数据源实例
     * @return 返回当前构建器实例，以支持链式调用
     */
    public JdbcDirectorBuilder addDataSource(DataSourceKey dataSourceKey, DataSource dataSource) {
        this.dataSources.add(new DataSourceEntry(dataSourceKey, dataSource));
        return this;
    }

    /**
     * 添加一个自定义的 DataSourceKeyRouter。
     *
     * <p>DataSourceKeyRouter 接口实现了一种动态选择数据源的策略。</p>
     *
     * @param dataSourceKeyRouter 自定义的数据源键路由策略
     * @return 返回当前构建器实例，以支持链式调用
     */
    public JdbcDirectorBuilder addDataSourceKeyRouter(DataSourceKeyRouter dataSourceKeyRouter) {
        this.dataSourceKeyRouters.add(dataSourceKeyRouter);
        return this;
    }

    /**
     * 设置事务隔离级别。
     *
     * @param transactionIsolation 要设置的事务隔离级别
     * @return 返回当前构建器实例，以支持链式调用
     */
    public JdbcDirectorBuilder transactionIsolation(TransactionIsolation transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
        return this;
    }

    /**
     * 设置期望的自动提交行为。
     *
     * @param desiredAutoCommit 如果希望启用自动提交则为 true，否则为 false
     * @return 返回当前构建器实例，以支持链式调用
     */
    public JdbcDirectorBuilder desiredAutoCommit(boolean desiredAutoCommit) {
        this.desiredAutoCommit = desiredAutoCommit;
        return this;
    }

    /**
     * 构建并返回配置好的 JdbcDirector 实例。
     *
     * <p>此方法会创建一个 DataSourceRouter，并根据配置注册所有数据源和路由策略，
     * 然后使用这些组件构造一个新的 JdbcDirector 实例。</p>
     *
     * @return 返回配置好的 JdbcDirector 实例
     */
    public JdbcDirector build() {
        if (defaultDataSource == null) {
            throw new IllegalArgumentException("No default data source is set.");
        }
        // 创建 DataSourceRouter，传入第一个 DataSourceKeyRouter 和默认数据源
        DataSourceRouter dataSourceRouter = new DataSourceRouter(dataSourceKeyRouters, defaultDataSource);

        // 批量注册数据源和对应的键
        for (DataSourceEntry dataSourceEntry : dataSources) {
            if (dataSourceEntry != null) {
                dataSourceRouter.registerDataSourceEntry(dataSourceEntry);
            }
        }

        // 构造并返回 JdbcDirector
        return create(dataSourceRouter, transactionIsolation, desiredAutoCommit);
    }

    /**
     * 创建JdbcDirector实例
     *
     * @param dataSourceRouter     数据源路由器，用于确定使用哪个数据源
     * @param transactionIsolation 事务隔离级别，指定事务的隔离等级
     * @param desiredAutoCommit    期望的自动提交状态，true表示自动提交，false表示手动提交
     * @return 返回新创建的JdbcDirector实例
     */
    protected JdbcDirector create(DataSourceRouter dataSourceRouter, TransactionIsolation transactionIsolation, boolean desiredAutoCommit) {
        return new JdbcDirector(dataSourceRouter, transactionIsolation, desiredAutoCommit);
    }

}
