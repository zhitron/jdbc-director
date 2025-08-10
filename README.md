# JDBC Director

## 📄 项目简介

JDBC Director 是一个用于简化 JDBC 操作的 Java 工具库。它提供了便捷的 API 来管理数据库连接、执行 SQL 语句以及处理结果集，从而减少样板代码并提高开发效率。

---

## 🚀 快速开始

### 构建要求

- JDK 8 或以上（推荐使用 JDK 8）
- Maven 3.x

### 添加依赖

你可以通过 Maven 引入该项目：

```xml

<dependency>
    <groupId>io.github.zhitron</groupId>
    <artifactId>jdbc-director</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 🧩 功能特性

### 1. 数据源管理

- **多数据源支持**：可以注册和管理多个数据源，通过 `DataSourceKey` 进行标识
- **默认数据源配置**：支持设置默认数据源，当无法确定使用哪个数据源时自动使用
- **数据源路由**：提供灵活的数据源路由机制，可根据SQL上下文动态选择数据源

### 2. 连接管理

- **连接获取与释放**：提供统一的连接获取和释放接口
- **连接复用**：支持线程内连接复用以提高性能
- **自动配置**：自动设置连接的事务隔离级别和自动提交行为

### 3. 事务管理

- **事务控制**：支持完整的事务操作（开始、提交、回滚）
- **多种隔离级别**：支持多种事务隔离级别（READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE）
- **自动提交控制**：可配置连接的自动提交行为

### 4. SQL执行支持

- **参数化查询**：支持命名参数和位置参数
- **链式调用API**：提供流畅的API用于执行SQL查询和更新操作
- **结果集映射**：支持结果集到对象的自动映射

### 5. 框架集成

- **Spring集成**：提供专门的 `SpringJdbcDirector` 与Spring框架无缝集成
- **事务管理协同**：与Spring的事务管理机制协同工作

### 6. 异常处理

- **统一异常体系**：提供 `PersistenceException` 和 `TransactionException` 等专用异常类
- **安全操作方法**：提供带异常处理的安全操作方法（如 `releaseConnectionSafely`）

### 7. 配置构建

- **构建器模式**：采用 `JdbcDirectorBuilder` 模式进行灵活配置
- **链式配置**：支持链式调用方式进行组件配置

### 8. 全局访问

- **单例持有**：通过 `JdbcDirectorHolder` 提供全局访问点
- **延迟初始化**：支持通过配置函数进行延迟初始化

这些功能特性使 JDBC Director 成为一个功能完整且易于使用的 JDBC 操作库，能够显著减少样板代码并提高开发效率。

---

## ✍️ 开发者

- **Zhitron**
- 邮箱: zhitron@foxmail.com
- 组织: [Zhitron](https://github.com/zhitron)

---

## 📦 发布状态

当前版本：`1.0.0`

该项目已发布至 [Maven Central](https://search.maven.org/)，支持快照版本与正式版本部署。

---

## 🛠 源码管理

GitHub 地址：https://github.com/zhitron/jdbc-director

使用 Git 进行版本控制：

```bash
git clone https://github.com/zhitron/jdbc-director.git
```

---

## 📚 文档与社区

- Javadoc 文档可通过 `mvn javadoc:javadoc` 生成。
- 如有问题或贡献，请提交 Issues 或 PR 至 GitHub 仓库。

---

## 📎 License

Apache License, Version 2.0  
详见 [LICENSE](https://www.apache.org/licenses/LICENSE-2.0.txt)

---
