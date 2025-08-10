package com.github.zhitron.jdbc_director;

import java.util.Objects;

/**
 * 数据源标识接口，用于区分不同的数据源配置。
 *
 * @author zhitron
 */
public interface DataSourceKey {

    /**
     * 默认的数据源标识实例，用于当未指定数据源 ID 时使用。
     */
    DataSourceKey DEFAULT = new ID("");

    /**
     * 根据给定的数据源 ID 创建一个数据源键。
     * 如果提供的 ID 为 null 或空白字符串，则返回默认的数据源键。
     *
     * @param id 数据源的唯一标识符
     * @return 对应的数据源键实例
     */
    static DataSourceKey of(String id) {
        if (id == null || id.isEmpty()) {
            return DEFAULT;
        }
        return new ID(id);
    }

    /**
     * 判断两个对象是否相等。
     * 该方法用于比较当前数据源键与另一个对象是否为同一实例或具有相同的 ID。
     *
     * @param object 要比较的对象
     * @return 如果对象相等则返回 true，否则返回 false
     */
    @Override
    boolean equals(Object object);

    /**
     * 返回当前数据源键的哈希码值。
     * 哈希码基于数据源键的 ID 生成，确保相同 ID 的实例具有相同的哈希码。
     *
     * @return 当前对象的哈希码
     */
    @Override
    int hashCode();

    /**
     * 基于字符串 ID 的数据源键实现。
     * 该类实现了 DataSourceKey 接口，并持有一个字符串形式的数据源 ID。
     */
    final class ID implements DataSourceKey {
        /**
         * 数据源的唯一标识符。
         */
        private final String id;

        /**
         * 构造一个带有指定 ID 的数据源键实例。
         *
         * @param id 数据源的唯一标识符
         */
        public ID(String id) {
            this.id = id;
        }

        /**
         * 获取数据源的唯一标识符。
         *
         * @return 数据源的唯一标识符
         */
        public String id() {
            return id;
        }

        /**
         * 判断当前对象与给定对象是否相等。
         * 如果给定对象是 ID 类型，并且其 ID 与当前对象的 ID 相同，则认为两个对象相等。
         *
         * @param o 要比较的对象
         * @return 如果对象相等则返回 true，否则返回 false
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;
            ID that = (ID) o;
            return Objects.equals(id, that.id);
        }

        /**
         * 返回当前对象的哈希码值。
         * 哈希码基于数据源键的 ID 生成，确保相同 ID 的实例具有相同的哈希码。
         *
         * @return 当前对象的哈希码
         */
        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        /**
         * 返回表示当前对象的字符串。
         * 字符串包含类名和数据源的唯一标识符。
         *
         * @return 表示当前对象的字符串
         */
        @Override
        public String toString() {
            return id;
        }
    }
}
