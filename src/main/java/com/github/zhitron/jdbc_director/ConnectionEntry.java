package com.github.zhitron.jdbc_director;

import java.sql.Connection;
import java.util.Objects;

/**
 * ConnectionEntry 是一个不可变的类，用于封装数据源条目和对应的数据库连接实例。
 * 该类提供了对连接的有效管理，并确保了数据源和连接实例的非空性。
 *
 * @author zhitron
 */
public class ConnectionEntry {
    /**
     * 数据源条目，包含数据源的配置信息。
     * 该字段是不可变的，一旦创建后不能更改。
     */
    protected final DataSourceEntry dataSourceEntry;

    /**
     * 数据库连接实例，用于执行数据库操作。
     * 该字段是不可变的，一旦创建后不能更改。
     */
    protected final Connection connectionInstance;

    /**
     * 创建一个新的 ConnectionEntry 实例。
     *
     * @param dataSourceEntry    数据源条目，必须非空
     * @param connectionInstance 数据库连接实例，必须非空
     * @throws NullPointerException 如果任一参数为 null
     */
    public ConnectionEntry(DataSourceEntry dataSourceEntry, Connection connectionInstance) {
        this.dataSourceEntry = Objects.requireNonNull(dataSourceEntry);
        this.connectionInstance = Objects.requireNonNull(connectionInstance);
    }

    /**
     * 获取数据源条目。
     *
     * @return 数据源条目
     */
    public DataSourceEntry dataSourceEntry() {
        return dataSourceEntry;
    }

    /**
     * 获取数据库连接实例。
     *
     * @return 数据库连接实例
     */
    public Connection connectionInstance() {
        return connectionInstance;
    }

    /**
     * 返回 ConnectionEntry 的字符串表示形式。
     * 字符串格式为 "[connectionInstance]dataSourceEntry"。
     *
     * @return 表示当前对象的字符串
     */
    @Override
    public String toString() {
        return dataSourceEntry + " : " + connectionInstance;
    }

    /**
     * 判断两个 ConnectionEntry 是否相等。
     * 如果给定对象是 ConnectionEntry 类型，并且其 dataSourceEntry 和 connectionInstance
     * 分别与当前对象的对应字段相等，则认为两个对象相等。
     *
     * @param o 要比较的对象
     * @return 如果相等则返回 true，否则返回 false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionEntry that = (ConnectionEntry) o;
        return Objects.equals(dataSourceEntry, that.dataSourceEntry) &&
                Objects.equals(connectionInstance, that.connectionInstance);
    }

    /**
     * 返回此 ConnectionEntry 的哈希码值。
     * 哈希码基于 dataSourceEntry 和 connectionInstance 生成。
     *
     * @return 此对象的哈希码值
     */
    @Override
    public int hashCode() {
        return Objects.hash(dataSourceEntry, connectionInstance);
    }
}
