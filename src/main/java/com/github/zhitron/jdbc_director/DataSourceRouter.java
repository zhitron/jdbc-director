package com.github.zhitron.jdbc_director;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 数据源路由类，用于根据SQL操作的上下文动态选择合适的数据源。
 *
 * @author zhitron
 */
public final class DataSourceRouter {
    /**
     * 存储数据源键与对应数据源的映射关系。
     */
    private final Map<DataSourceKey, DataSourceEntry> dataSourceStore = new ConcurrentHashMap<>();

    /**
     * 存储自定义数据源键路由策略的列表。
     */
    private final List<DataSourceKeyRouter> dataSourceKeyRouterList = new CopyOnWriteArrayList<>();

    /**
     * 默认的数据源键路由策略。
     */
    private final DataSourceKeyRouter defaultDataSourceKeyRouter;

    /**
     * 默认数据源，当未找到匹配的数据源时使用。
     */
    private final DataSource defaultDataSource;

    /**
     * 构造一个新的数据源路由器实例。
     *
     * @param dataSourceKeyRouterList 数据源键路由策略列表
     * @param defaultDataSource       默认数据源
     */
    public DataSourceRouter(List<DataSourceKeyRouter> dataSourceKeyRouterList, DataSource defaultDataSource) {
        // 过滤掉空的数据源键路由策略，并重新赋值给局部变量
        List<DataSourceKeyRouter> filteredRouterList = dataSourceKeyRouterList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 如果过滤后的列表为空，则默认数据源键路由策略为 null
        if (filteredRouterList.isEmpty()) {
            this.defaultDataSourceKeyRouter = null;
        } else {
            // 将第一个路由策略作为默认数据源键路由策略
            this.defaultDataSourceKeyRouter = filteredRouterList.get(0);
            // 如果还有多个 DataSourceKeyRouter，则逐个注册剩下的
            if (filteredRouterList.size() > 1) {
                for (int i = 1; i < filteredRouterList.size(); i++) {
                    this.dataSourceKeyRouterList.add(filteredRouterList.get(i));
                }
            }
        }
        this.defaultDataSource = defaultDataSource;
    }

    /**
     * 构造一个新的数据源路由器实例。
     *
     * @param defaultDataSourceKeyRouter 默认数据源键路由策略
     * @param defaultDataSource          默认数据源
     */
    public DataSourceRouter(DataSourceKeyRouter defaultDataSourceKeyRouter, DataSource defaultDataSource) {
        this.defaultDataSourceKeyRouter = defaultDataSourceKeyRouter;
        this.defaultDataSource = defaultDataSource;
    }

    /**
     * 注册一个数据源到路由映射中。
     *
     * @param dataSourceEntry 数据源条目，包含数据源键和数据源实例
     */
    public void registerDataSourceEntry(DataSourceEntry dataSourceEntry) {
        // 如果数据源条目不为空，则将其添加到数据源存储中
        if (dataSourceEntry != null) {
            this.dataSourceStore.put(dataSourceEntry.dataSourceKey(), dataSourceEntry);
        }
    }

    /**
     * 注册一个数据源键路由策略。
     *
     * @param dataSourceKeyRouter 数据源键路由策略
     */
    public void registerDataSourceKeyRouter(DataSourceKeyRouter dataSourceKeyRouter) {
        if (dataSourceKeyRouter != null) {
            this.dataSourceKeyRouterList.add(dataSourceKeyRouter);
        }
    }

    /**
     * 根据SQL操作的上下文信息确定应使用哪个数据源。
     *
     * @param data   SQL操作相关的数据对象
     * @param sql    正在执行的SQL语句
     * @param params SQL语句中的命名参数映射
     * @param args   SQL语句中的位置参数列表
     * @return 返回选定的数据源条目（包含数据源键和数据源实例）
     * @throws PersistenceException 当无法找到合适的数据源且没有配置默认数据源时抛出
     */
    public DataSourceEntry determineDataSource(Object data, String sql, Map<String, Object> params, List<Object> args) {
        DataSourceKey dataSourceKey = null;
        List<DataSourceKeyRouter> dataSourceKeyRouters = dataSourceKeyRouterList;
        if (!dataSourceKeyRouters.isEmpty()) {
            for (DataSourceKeyRouter dataSourceKeyRouter : dataSourceKeyRouters) {
                dataSourceKey = dataSourceKeyRouter.determineDataSourceKey(data, sql, params, args);
                if (dataSourceKey != null) {
                    break;
                }
            }
        }

        // 如果没有通过自定义路由规则确定数据源，则尝试使用默认路由规则
        if (dataSourceKey == null && defaultDataSourceKeyRouter != null) {
            dataSourceKey = defaultDataSourceKeyRouter.determineDataSourceKey(data, sql, params, args);
        }

        // 如果仍然无法确定数据源键，则使用默认数据源键
        if (dataSourceKey == null) {
            dataSourceKey = DataSourceKey.DEFAULT;
        }

        DataSource dataSource = null;

        // 如果是默认数据源键且默认数据源存在，则直接使用默认数据源
        if (dataSourceKey == DataSourceKey.DEFAULT && defaultDataSource != null) {
            dataSource = defaultDataSource;
        } else {
            // 否则从注册的数据源中查找
            DataSourceEntry dataSourceEntry = dataSourceStore.get(dataSourceKey);
            if (dataSourceEntry != null) {
                dataSource = dataSourceEntry.dataSource();
            }
            if (dataSource == null) {
                // 如果找不到匹配的数据源且存在默认数据源，则使用默认数据源
                if (defaultDataSource == null) {
                    throw new PersistenceException("No suitable data source found and no default data source configured.");
                } else {
                    dataSource = defaultDataSource;
                }
            }
        }
        return new DataSourceEntry(dataSourceKey, dataSource);
    }
}
