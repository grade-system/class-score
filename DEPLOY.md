# 班级学生成绩管理 部署说明（DEPLOY.md）

本文档面向没有参与开发的人：按顺序执行即可在一台全新的 Linux 服务器上完成部署，并给出部署后自检与故障分支。
技术栈固定为 **JavaEE 8（javax.servlet）+ JDK 8 + Tomcat 9 + MySQL 8**，不要升级到 Tomcat 10（jakarta.*）。

## 〇、部署工件清单（均在仓库内）

| 工件 | 仓库路径 | 用途 |
|---|---|---|
| WAR 构建配置 | `pom.xml`（`finalName=class-score`） | 决定 WAR 名与默认上下文路径 |
| 云服务器部署脚本 | `scripts/deploy_war.sh` | 复制 WAR、清理旧目录、等待解压、健康检查 |
| 部署后自检脚本 | `scripts/check_deploy.sh` | 5 项检查：进程 / 端口 / 解压 / 健康 / 业务页 |
| 环境变量模板 | `scripts/tomcat-setenv.sh.template` | 数据库地址、账号、密码注入 Tomcat 9 |
| 配置模板 | `src/main/resources/db.properties.template` | 数据库连接参数说明与默认值 |
| 数据库初始化脚本 | `sql/class_score.sql` | 建库、建表、建最小权限账号、导入 6 条数据 |
| 健康检查接口 | `src/main/java/com/example/servlet/HealthServlet.java` | `GET /health` 返回 JSON 与 200/503 |
| 部署文档 | `DEPLOY.md` | 本文档 |

> 未提供 `Dockerfile` / `docker-compose.yml`：验证机没有安装 Docker 与 WSL2，无法保证“提交即验证”，
> 因此只提交在本机 Linux 虚拟机上真实跑通过的云服务器部署脚本，避免提交未验证的工件。

## 一、环境前提

| 组件 | 版本要求 | 本次验证环境 |
|---|---|---|
| 操作系统 | Ubuntu 20.04/22.04 | Ubuntu 22.04（虚拟机 javaee-vm） |
| JDK | 8 | OpenJDK 1.8.0_504 |
| Maven | 3.6+ | 3.8.x |
| MySQL | 8.0 | 8.0.46 |
| Tomcat | 9.0.x（禁止 Tomcat 10+） | 9.0.106，安装目录 `/opt/tomcat` |
| 端口 | 8080 可访问 | 8080（宿主机映射 8088 → 虚拟机 8080） |

判断依据：项目依赖 `javax.servlet-api 4.0.1`，Tomcat 9 实现 Servlet 4.0；Tomcat 10 改用 `jakarta.servlet` 包名，会直接 `ClassNotFoundException`。

## 二、获取代码并构建

```bash
git clone https://github.com/grade-system/class-score.git class-score
cd class-score
mvn clean package -DskipTests
ls -lh target/class-score.war
```

`pom.xml` 中 `<finalName>class-score</finalName>` 决定默认访问路径 `/class-score`；如需其他上下文路径，
不必改 pom，直接用 `scripts/deploy_war.sh` 的重命名方式部署（见第四节）。

## 三、初始化数据库

1. 上传并执行初始化脚本（自动建库、建表、创建最小权限账号 `score_user`、导入 6 条成绩）：

```bash
scp sql/class_score.sql dev@<服务器IP>:~/class_score.sql
ssh dev@<服务器IP>
sudo mysql --defaults-file=/etc/mysql/debian.cnf < ~/class_score.sql
# 或服务器 root 有密码时：mysql -uroot -p < ~/class_score.sql
```

2. 校验：

```bash
mysql -uscore_user -p'Score@2026' -e "use class_score; show tables; select count(*) from score_record;"
```

预期：`score_change_log`、`score_record` 两张表，6 条记录。

## 四、配置数据库密码（不进仓库）并部署 WAR

`src/main/resources/db.properties` 里 **`db.password` 为空**，密码只通过环境变量注入：

```bash
# 1) 注入环境变量（Tomcat 9 启动时会自动执行 bin/setenv.sh）
cp scripts/tomcat-setenv.sh.template /opt/tomcat/bin/setenv.sh
chmod +x /opt/tomcat/bin/setenv.sh
# 编辑其中的 DB_URL / DB_USERNAME / DB_PASSWORD

# 2) 部署 WAR（上下文路径可外置，默认 class-score）
scp target/class-score.war dev@<服务器IP>:~/
ssh dev@<服务器IP>
chmod +x scripts/deploy_war.sh scripts/check_deploy.sh
./scripts/deploy_war.sh ~/class-score.war class-score /opt/tomcat
# 换上下文路径示例：./scripts/deploy_war.sh ~/class-score.war grade /opt/tomcat  -> http://IP:8080/grade/scores
```

`deploy_war.sh` 会：清理 `webapps/class-score` 旧目录 → 复制 WAR → 等待 autoDeploy 解压（最多 30 秒）→ 调 `/health`。

## 五、部署后验证

```bash
# 一条命令五项检查（进程/端口/解压/健康/业务页）
./scripts/check_deploy.sh
# 宿主机 8088 映射到虚拟机 8080 时：
PORT=8088 CTX=class-score ./scripts/check_deploy.sh
```

健康检查接口：

```bash
curl -s http://localhost:8080/class-score/health
# {"status":"UP","app":"class-score","contextPath":"/class-score","port":8080,
#  "java":"1.8.0_504","container":"Apache Tomcat/9.0.106",
#  "db":{"connected":true,"tables":2,"records":6,"url":"jdbc:mysql://..."},"time":"..."}
```

浏览器访问 `http://<服务器IP>:8080/class-score/scores`（或宿主映射端口 8088）。

业务验证：列表点“修改”→ 填新分数与修改人 → 保存 → 列表提示“成绩修改成功”且修改人/时间更新 → “修改记录”可见留痕。

## 六、故障分支

| 现象 | 判断依据 | 处理 |
|---|---|---|
| 8080 打不开，本机 curl 正常 | `ss -lntp \| grep 8080` 看到别的进程；宿主机 8080 已被本机 Tomcat 占用 | 改端口转发（宿主 8088→虚拟机 8080），或改 Tomcat `conf/server.xml` 的 Connector 端口 |
| 启动即失败，日志 `Address already in use: bind` | `catalina.out` 明确报端口占用 | 同上；确认只有一个 Tomcat 实例在跑 |
| `/health` 返回 503，error 为“数据库密码未配置” | 部署时忘记 `setenv.sh` | 写 `/opt/tomcat/bin/setenv.sh` 后重启 Tomcat；`DB_PASSWORD` 必须非空 |
| `/health` 返回 503，error 含 `Access denied for user 'score_user'` | 密码与初始化脚本不一致 | 重跑 `sql/class_score.sql`，或修正 setenv.sh 中 `DB_PASSWORD` |
| `/health` 返回 503，error 含 `Unknown database 'class_score'` | 未执行初始化脚本 | 按第三节初始化 |
| `/scores` 返回 500，堆栈 `Communications link failure` | MySQL 未启动或端口不通 | `systemctl status mysql`；`ss -lnt \| grep 3306` |
| 访问返回 404 | WAR 未复制、未解压，或上下文路径不是 `class-score` | `ls /opt/tomcat/webapps/`；用 `ls /opt/tomcat/webapps/*.war` 反查实际上下文；或 `CTX=<实际名> ./scripts/check_deploy.sh` |
| 部署后仍是旧页面 | 旧目录未清理，Tomcat 复用了旧解压目录 | `rm -rf webapps/class-score webapps/class-score.war` 后重新 `deploy_war.sh` |
| `deploy_war.sh` 报 `Permission denied`；日志有“无法被彻底删除”“scratchDir 不可用” | 上次用 root 复制过 WAR，`webapps/class-score` 属主是 root，普通用户与 Tomcat 都删不掉 | 脚本已自动降级 `sudo -n` 清理；也可手动 `sudo rm -rf /opt/tomcat/webapps/class-score /opt/tomcat/work/Catalina/localhost/class-score` 后重新部署 |
| 启动报 `ClassNotFoundException: javax.servlet.*` / `NoClassDefFoundError` | 用了 Tomcat 10+ 或 JDK 11+ 编译 | 换 Tomcat 9；`java -version` 确认 1.8，`mvn -v` 显示的 JDK 也必须是 1.8 |
| 页面中文乱码 | JSP/响应编码不是 UTF-8 | 确认 `EncodingFilter` 生效、JSP 为 UTF-8、`JAVA_OPTS` 含 `-Dfile.encoding=UTF-8` |
| 时间相差 8 小时 | JDBC 未配时区 | URL 保留 `serverTimezone=Asia/Shanghai` |

## 七、账号与配置速查

| 项 | 值 |
|---|---|
| 数据库 | `class_score`，账号 `score_user / Score@2026`（最小权限，仅本库增删改查） |
| 健康检查 | `GET /class-score/health`（200 正常 / 503 数据库不可用） |
| 列表页 | `GET /class-score/scores` |
| 修改记录 | `GET /class-score/scores?action=logs` |
| 配置优先级 | 环境变量 `DB_URL / DB_USERNAME / DB_PASSWORD` > `db.properties` |

生产环境请修改默认密码，并确保 `setenv.sh` 不被提交到仓库（权限 700，属主为运行 Tomcat 的账号）。
