package com.github.zhitron.jdbc_director;

/**
 * Spring JDBC 操作协调器构建器，用于配置和组装 SpringJdbcDirector 实例。
 *
 * <p>该构建器继承自 JdbcDirectorBuilder，专门用于构建与 Spring 框架集成的 SpringJdbcDirector 实例。
 * 它支持设置默认数据源、注册多个数据源及其对应的键（DataSourceKey）、添加自定义的数据源路由策略
 * （DataSourceKeyRouter），以及配置事务隔离级别和自动提交行为。</p>
 *
 * <p>通过链式调用方式提供流畅的 API 体验，并最终通过 {@link #build()} 方法生成配置完成的
 * {@link SpringJdbcDirector} 实例。</p>
 *
 * @author zhitron
 */
public class SpringJdbcDirectorBuilder extends JdbcDirectorBuilder {
    /**
     * 创建SpringJdbcDirector实例
     *
     * @param dataSourceRouter     数据源路由器，用于确定使用哪个数据源
     * @param transactionIsolation 事务隔离级别，指定事务的隔离等级
     * @param desiredAutoCommit    期望的自动提交状态，true表示自动提交，false表示手动提交
     * @return 返回新创建的SpringJdbcDirector实例
     */
    @Override
    protected JdbcDirector create(DataSourceRouter dataSourceRouter, TransactionIsolation transactionIsolation, boolean desiredAutoCommit) {
        return new SpringJdbcDirector(dataSourceRouter, transactionIsolation, desiredAutoCommit);
    }
}
