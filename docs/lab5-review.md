# AI 代码三层审查记录（实验五）

| 序号 | 审查位置 | AI 原始输出 | 审查发现 | 处置 | 依据 |
|---|---|---|---|---|---|
| 1 | 成绩查询 SQL（`ScoreRecordDao`） | `String sql = "select * from score_record where student_no = '" + no + "'"; Statement st = conn.createStatement();` | 字符串拼接 SQL，存在 SQL 注入风险；`Statement` 无法参数绑定 | 修改：改为 `PreparedStatement` + `?` 占位符，`DBUtil.query` 统一绑定参数 | 安全性：`' or '1'='1` 可绕过条件；参数化查询是防注入的基本要求 |
| 2 | 修改成绩 + 写留痕（`ScoreRecordDao.updateWithLog`） | AI 生成两个独立方法：`updateScore()` 和 `insertLog()`，各自 `DriverManager.getConnection()` | 两条 SQL 不在同一事务，若写日志失败会出现“分数改了但无留痕”，违反业务规则 R1 | 修改：合并到同一连接，`setAutoCommit(false)`，失败 `rollback()` | 正确性：业务规则要求修改必须留痕，数据必须一致 |
| 3 | 数据库连接（`DBUtil`） | `private static final String PWD = "123456";` | 数据库密码硬编码在源码，随源码/仓库泄露；且使用 root 账号，权限过大 | 修改：改为读取 `db.properties`，支持 `DB_PASSWORD` 环境变量覆盖；数据库改用最小权限账号 `score_user` | 安全性：凭据与代码分离；最小权限原则 |
| 4 | 修改接口（`ScoreRecordServlet`） | AI 只按 `id` 更新，未校验记录是否存在、修改人是否为空 | 不存在的 id 静默失败，无修改人仍可提交，导致留痕缺失 | 拒绝并修改：Service 校验修改人非空、分数范围、记录存在，DAO 返回 false 时报错 | 正确性/安全性：业务规则 R1 与 R2 必须在服务端强制校验，不能只靠前端 |

## 审查方法

- 正确性：逐条对照 R1~R4，检查失败分支与事务边界；
- 安全性：检查 SQL 拼接、凭据存放、输入校验、是否可越权修改任意 id；
- 可维护性：检查分层（Servlet 不写 SQL）、异常处理、连接与结果集是否关闭（统一 `try-with-resources`）。
