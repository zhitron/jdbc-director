package com.github.zhitron.jdbc_director;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * 表示一个数据源条目，包含数据源键和对应的数据源对象。
 *
 * @author zhitron
 */
public class DataSourceEntry {
    /**
     * 数据源的唯一标识键，用于区分不同的数据源配置。
     * 该字段是不可变的，一旦创建后不能更改。
     */
    protected final DataSourceKey dataSourceKey;

    /**
     * 实际的数据源对象，提供数据库连接等操作。
     * 该字段是不可变的，一旦创建后不能更改。
     */
    protected final DataSource dataSource;

    /**
     * 创建一个新的数据源条目实例。
     *
     * @param dataSourceKey 非空数据源键
     * @param dataSource    非空数据源实例
     * @throws NullPointerException 如果任一参数为 null，则抛出此异常
     */
    public DataSourceEntry(DataSourceKey dataSourceKey, DataSource dataSource) {
        this.dataSourceKey = Objects.requireNonNull(dataSourceKey);
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    /**
     * 获取数据源键。
     *
     * @return 数据源键，不会为 null
     */
    public DataSourceKey dataSourceKey() {
        return dataSourceKey;
    }

    /**
     * 获取数据源实例。
     *
     * @return 数据源实例，不会为 null
     */
    public DataSource dataSource() {
        return dataSource;
    }

    /**
     * 返回表示当前对象的字符串。
     * 字符串格式为 "{dataSourceKey = dataSource}"。
     *
     * @return 表示当前对象的字符串
     */
    @Override
    public String toString() {
        return dataSourceKey + " : " + dataSource;
    }

    /**
     * 判断当前对象与给定对象是否相等。
     * 如果给定对象是 DataSourceEntry 类型，并且其 dataSourceKey 和 dataSource
     * 分别与当前对象的对应字段相等，则认为两个对象相等。
     *
     * @param o 要比较的对象
     * @return 如果对象相等则返回 true，否则返回 false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataSourceEntry that = (DataSourceEntry) o;
        return Objects.equals(dataSourceKey, that.dataSourceKey) &&
                Objects.equals(dataSource, that.dataSource);
    }

    /**
     * 返回当前对象的哈希码值。
     * 哈希码基于 dataSourceKey 和 dataSource 生成。
     *
     * @return 当前对象的哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(dataSourceKey, dataSource);
    }
}
