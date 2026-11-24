# 班级学生成绩管理 部署说明（DEPLOY.md）

本文档面向没有参与开发的人，按顺序执行即可在一台全新的 Linux 服务器上完成部署。

## 一、环境前提

| 组件 | 版本要求 | 本次验证环境 |
|---|---|---|
| 操作系统 | Ubuntu 20.04/22.04 | Ubuntu 22.04 (javaee-vm) |
| JDK | 8 | OpenJDK 1.8.0_504 |
| Maven | 3.6+ | 3.8.x |
| MySQL | 8.0 | 8.0.46 |
| Tomcat | 9.0.x（禁止 Tomcat 10+） | 9.0.106，安装目录 `/opt/tomcat` |
| 端口 | 8080 可访问 | 8080（或映射为 8088） |

说明：项目基于 JavaEE8（`javax.servlet.*`），必须使用 JDK8 + Tomcat9；Tomcat10 使用 `jakarta.*`，无法运行本项目。

## 二、获取代码并构建

```bash
git clone <你的仓库地址> class-score
cd class-score
mvn clean package -DskipTests
ls -lh target/class-score.war
```

`pom.xml` 中 `<finalName>class-score</finalName>` 决定访问路径为 `/class-score`，不要随意修改。

## 三、初始化数据库

1. 上传初始化脚本到服务器：

```bash
scp sql/class_score.sql dev@<服务器IP>:~/class_score.sql
```

2. 使用 MySQL 管理员账号执行（脚本会建库、建表、建最小权限账号 `score_user` 并导入 6 条初始数据）：

```bash
sudo mysql --defaults-file=/etc/mysql/debian.cnf < ~/class_score.sql
```

如果服务器 root 有密码，也可用：

```bash
mysql -uroot -p < ~/class_score.sql
```

3. 校验：

```bash
mysql -uscore_user -p'Score@2026' -e "use class_score; show tables; select count(*) from score_record;"
```

预期输出：`score_change_log`、`score_record` 两张表，6 条成绩记录。

## 四、部署 WAR

```bash
scp target/class-score.war dev@<服务器IP>:~/class-score.war
ssh dev@<服务器IP>
cp ~/class-score.war /opt/tomcat/webapps/
```

Tomcat 默认开启自动部署（autoDeploy），约 10 秒后自动解压 `class-score` 目录。也可以手动重启：

```bash
/opt/tomcat/bin/shutdown.sh
/opt/tomcat/bin/startup.sh
```

## 五、验证访问

在服务器本机验证：

```bash
curl -s -o /dev/null -w "HTTP %{http_code}\n" http://localhost:8080/class-score/scores
```

预期 `HTTP 200`。

在另一台电脑或手机（同一网络）浏览器访问：

```
http://<服务器IP>:8080/class-score/scores
```

若服务器端口被占用，可在 VirtualBox/云安全组上把宿主端口 8088 转发到虚拟机 8080，访问：

```
http://<服务器IP>:8088/class-score/scores
```

业务验证步骤：

1. 列表页点击某条记录的“修改”；
2. 填写“修改后分数”和“修改人”，点击“保存修改”；
3. 列表页出现“成绩修改成功”，该行“修改人/修改时间”更新；
4. 点击“修改记录”，可看到修改前后分数、修改人与修改时间。

## 六、常见问题

| 现象 | 原因 | 处理 |
|---|---|---|
| 访问返回 500，日志 `Unknown database 'class_score'` | 未执行初始化脚本 | 按第三节重新初始化 |
| 访问返回 404 | WAR 未复制到 webapps，或 finalName 被改 | 检查 `/opt/tomcat/webapps/class-score.war` |
| 页面中文乱码 | 请求未使用 UTF-8 | 确认 EncodingFilter 已生效，JSP 均为 UTF-8 |
| 8080 打不开但本机 8080 正常 | 宿主 8080 已被其他 Tomcat 占用，端口转发未生效 | `ss -lntp \| grep 8080` 查占用；改用 8088 转发 |
| 数据库连接失败 `Access denied` | 未创建 `score_user` 或密码不一致 | 重跑初始化脚本，或核对 `db.properties` |
| 时间相差 8 小时 | JDBC 未配置时区 | URL 中保留 `serverTimezone=Asia/Shanghai` |

## 七、账号与配置

数据库连接配置位于 `src/main/resources/db.properties`，支持环境变量覆盖：

```bash
export DB_URL="jdbc:mysql://localhost:3306/class_score?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8"
export DB_USERNAME=score_user
export DB_PASSWORD='Score@2026'
```

生产环境请修改默认密码，不要把真实密码提交到公开仓库。
