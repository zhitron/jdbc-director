package com.github.zhitron.jdbc_director;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * JDBC 操作协调器持有者，用于存储和管理全局唯一的 JdbcDirector 实例。
 *
 * <p>JdbcDirectorHolder 采用静态引用的方式确保在整个应用程序生命周期中
 * 能够方便地访问已配置的 JdbcDirector 实例。它提供了多种设置方式，包括
 * 直接设置实例以及通过构建器进行配置。</p>
 *
 * @author zhitron
 */
public class JdbcDirectorHolder {
    /**
     * 存储 JdbcDirector 实例的原子引用，确保线程安全的操作。
     */
    private static final AtomicReference<JdbcDirector> GLOBAL_INSTANCE = new AtomicReference<>();

    /**
     * 获取当前持有的 JdbcDirector 实例。
     *
     * @return 返回当前的 JdbcDirector 实例，可能为 null（未初始化）
     */
    public static JdbcDirector getJdbcDirector() {
        JdbcDirector jdbcDirector = GLOBAL_INSTANCE.get();
        if (jdbcDirector == null) {
            throw new IllegalStateException("JdbcDirector is not initialized");
        }
        return jdbcDirector;
    }

    /**
     * 设置一个新的 JdbcDirector 实例。
     *
     * @param jdbcDirector 要设置的 JdbcDirector 实例，不能为 null
     */
    public static void setJdbcDirector(JdbcDirector jdbcDirector) {
        if (jdbcDirector != null) {
            JdbcDirectorHolder.GLOBAL_INSTANCE.set(jdbcDirector);
        }
    }

    /**
     * 使用配置函数设置 JdbcDirector 实例。
     *
     * <p>该方法创建一个新的 JdbcDirectorBuilder，并将它传递给提供的配置函数，
     * 然后使用配置好的构建器生成 JdbcDirector 实例并设置到持有者中。</p>
     *
     * @param jdbcDirectorConfigurator 配置函数，用于定制 JdbcDirector 的构建过程
     */
    public static void setJdbcDirector(Consumer<JdbcDirectorBuilder> jdbcDirectorConfigurator) {
        JdbcDirectorBuilder jdbcDirectorBuilder = JdbcDirector.builder();
        if (jdbcDirectorConfigurator != null) {
            jdbcDirectorConfigurator.accept(jdbcDirectorBuilder);
        }
        JdbcDirectorHolder.GLOBAL_INSTANCE.set(jdbcDirectorBuilder.build());
    }
}
