package com.github.zhitron.jdbc_director;

import com.github.zhitron.universal.Logger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author zhitron
 */
public class JdbcDirectorTest {

    @Test
    public void test() {
        DataSource defaultDataSource = buildHikariDataSource(); // 默认数据源
        DataSource dataSource1 = buildHikariDataSource();       // 数据源 1
        DataSource dataSource2 = buildHikariDataSource();       // 数据源 2

        JdbcDirector director = new JdbcDirectorBuilder()
                .defaultDataSource(defaultDataSource)
                .addDataSource(DataSourceKey.of("db1"), dataSource1)
                .addDataSource(DataSourceKey.of("db2"), dataSource2)
                .addDataSourceKeyRouter((data, sql, params, arguments) -> {
                    if (data instanceof Integer) {
                        if ((Integer) data == 1) {
                            return DataSourceKey.of("db1");
                        } else if ((Integer) data == 2) {
                            return DataSourceKey.of("db2");
                        }
                    }
                    return null;
                })
                .transactionIsolation(TransactionIsolation.READ_COMMITTED)
                .desiredAutoCommit(false)
                .build();

        // 测试使用默认数据源
        ConnectionEntry defaultConnection = director.acquireConnection();
        DataSourceEntry dataSourceEntry = defaultConnection.dataSourceEntry();

        assertEquals(dataSourceEntry.dataSourceKey(), DataSourceKey.DEFAULT);

        // 测试根据 data 参数选择数据源 1
        ConnectionEntry db1Connection = director.acquireConnection(1);
        dataSourceEntry = db1Connection.dataSourceEntry();
        assertEquals(dataSourceEntry.dataSourceKey(), DataSourceKey.of("db1"));

        // 测试根据 data 参数选择数据源 2
        ConnectionEntry db2Connection = director.acquireConnection(2);
        dataSourceEntry = db2Connection.dataSourceEntry();
        assertEquals(dataSourceEntry.dataSourceKey(), DataSourceKey.of("db2"));

        // 测试无效的 data 参数，应该使用默认数据源
        ConnectionEntry invalidConnection = director.acquireConnection(999);
        dataSourceEntry = invalidConnection.dataSourceEntry();
        assertEquals(dataSourceEntry.dataSourceKey(), DataSourceKey.DEFAULT);

        // 清理连接
        director.releaseConnection(defaultConnection);
        director.releaseConnection(db1Connection);
        director.releaseConnection(db2Connection);
        director.releaseConnection(invalidConnection);
    }

    @Test
    public void test_connection() {
        JdbcDirectorHolder.setJdbcDirector(jdbcDirectorBuilder -> {
            jdbcDirectorBuilder.transactionIsolation(TransactionIsolation.SERIALIZABLE);
            jdbcDirectorBuilder.defaultDataSource(buildHikariDataSource());
        });
        JdbcDirector jdbcDirector = JdbcDirectorHolder.getJdbcDirector();
        ConnectionEntry connectionEntry = jdbcDirector.acquireConnection();
        try (Connection connection = connectionEntry.connectionInstance()) {
            Logger.info("Database connection established successfully!");
        } catch (SQLException e) {
            Logger.error("Failed to connect to the database.", e);
        } finally {
            jdbcDirector.releaseConnection(connectionEntry);
        }
    }

    private static HikariDataSource buildHikariDataSource() {
        try (InputStream input = ClassLoader.getSystemClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                throw new RuntimeException("Unable to find config.properties");
            }
            prop.load(input);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(prop.getProperty("db.url"));
            config.setUsername(prop.getProperty("db.user"));
            config.setPassword(prop.getProperty("db.password"));
            config.setDriverClassName(prop.getProperty("db.driver"));
            // 可选配置
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setIdleTimeout(30000);
            config.setMaxLifetime(1800000);
            config.setConnectionTimeout(30000);
            return new HikariDataSource(config);
        } catch (Exception e) {
            throw new RuntimeException("Error loading database configuration", e);
        }
    }
}
