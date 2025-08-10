package com.github.zhitron.jdbc_director;

import java.util.List;
import java.util.Map;

/**
 * 数据源路由接口，用于决定执行 SQL 时使用哪个数据源。
 *
 * @author zhitron
 */
@FunctionalInterface
public interface DataSourceKeyRouter {

    /**
     * 根据执行上下文信息确定应使用哪个数据源。
     *
     * @param data      执行上下文数据（如方法参数、实体对象等）
     * @param sql       即将执行的 SQL 语句
     * @param params    SQL 命名参数映射
     * @param arguments SQL 位置参数列表
     * @return 选定的数据源键实例
     */
    DataSourceKey determineDataSourceKey(Object data, String sql, Map<String, Object> params, List<Object> arguments);
}
