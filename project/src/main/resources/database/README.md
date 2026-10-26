# 短链接分表初始化

在 MySQL 8 的 `link` 库执行 `link-sharding.sql`，创建 `t_link_0`～`t_link_15` 和 `t_link_goto_0`～`t_link_goto_15`。脚本通过循环生成，使用与原 `t_link` 相同的字段结构和短码大小写比较规则；两类分表的主键都由 MyBatis-Plus 分配，不使用数据库自增。

该脚本用于原 `t_link` 为空的开发环境。已有数据时，必须先迁移主表并补齐路由记录，再切换数据源；脚本不会迁移或删除旧表。

两个模块的 `shardingsphere-config.yaml` 都是可提交的配置模板。各自复制为同目录下的 `shardingsphere-config-dev.yaml`，在开发配置中填写真实数据库密码和加密密钥。开发配置已统一排除出 Git；项目模块的数据源通过该文件启用分表。Redis 继续读取环境变量 `DEV_PASSWORD`。

`t_link` 按 `gid` 分片，`t_link_goto` 按 `full_short_url` 分片，两者都使用 `HASH_MOD`，分片数为 16。配置已开启 `sql-show`，启动后可查看实际访问的物理表。

启动本地 MySQL、Redis，准备好开发配置后，在仓库根目录执行：

```powershell
mvn -pl project "-Dtest=ShortLinkShardingIT" test
```

测试覆盖实际创建接口的两表写入、路由写入失败时主表回滚、跨分组短链接重复时的唯一索引兜底。测试使用独立分组与测试域名，完成后清理数据库记录；成功创建产生的布隆过滤器标记会保留。该测试需要显式指定，不会随默认测试命令自动连接本地数据库。
