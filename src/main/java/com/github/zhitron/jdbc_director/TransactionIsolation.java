package com.github.zhitron.jdbc_director;

import java.sql.Connection;

/**
 * 事务隔离级别枚举类，用于定义数据库事务的不同隔离级别。
 * 每个枚举值对应一个由 {@link Connection} 定义的事务隔离常量。
 *
 * @author zhitron
 */
public enum TransactionIsolation {
    /**
     * 表示不支持事务。
     */
    NONE(Connection.TRANSACTION_NONE),

    /**
     * 读已提交数据的事务隔离级别。
     * 一个事务只能读取其他事务已经提交的数据。
     */
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),

    /**
     * 读未提交数据的事务隔离级别。
     * 一个事务可以读取其他事务未提交的数据，可能导致脏读。
     */
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),

    /**
     * 可重复读的事务隔离级别。
     * 保证在同一事务中多次读取同一数据时结果一致，但可能出现幻读。
     */
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),

    /**
     * 串行化的事务隔离级别。
     * 提供最严格的隔离，防止脏读、不可重复读和幻读，但性能最差。
     */
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

    /**
     * 该枚举值对应的事务隔离级别的整型值。
     */
    public final int level;

    /**
     * 构造方法，用于将枚举值与对应的事务隔离级别整型值关联。
     *
     * @param level 事务隔离级别的整型值，来自 {@link Connection} 的常量
     */
    TransactionIsolation(int level) {
        this.level = level;
    }
}
