# 实验四 AI 辅助编程关键对话记录

工具：Trae（IDE 内置 AI 编程助手），模型 DeepSeek-V4.1-Flash。
基线：JavaEE8 + javax.servlet.* + Tomcat 9 + JDK 8 + MySQL 8。

## 一、对话记录

| 序号 | Prompt 摘要 | AI 输出要点 | 我的修改与理由 |
| --- | --- | --- | --- |
| 1 | 生成一个处理 /scores 的 Servlet，要求 JavaEE8（javax） | 返回完整 Servlet，但 import 写成 jakarta.servlet.*，并建议升级 Tomcat 10 | 改回 javax.servlet.*；Tomcat 9 不识别 jakarta 包 |
| 2 | 写一个查询 score_record 的 DAO，包含列表与新增 | 每个方法都 DriverManager 新建连接，try-finally 关闭 | 抽出 DBUtil 模板方法，DAO 只保留 SQL 与 RowMapper 映射 |
| 3 | JSP 中遍历成绩并显示是否及格 | 给出 c:forEach + c:choose 片段，未格式化时间 | 保留，补充 fmt:formatDate 显示录入时间 |
| 4 | 增加平均分与及格率 | 建议在 JSP 里用 <% %> 脚本片段循环计算 | 拒绝：统计放 Service，Servlet 设 request 属性，JSP 只展示 |
| 5 | 表单提交的中文姓名出现乱码，怎么改 | 建议在每个 Servlet 里调用 setCharacterEncoding("UTF-8") | 改为统一 EncodingFilter 映射 /*，避免每处重复 |

## 二、AI 建议 vs JavaEE8 基线冲突

| AI 建议内容 | 冲突点 | 我的处置 | 验证结果 |
| --- | --- | --- | --- |
| 使用 jakarta.servlet.http.HttpServlet，建议 Tomcat 10 | 实验基线为 JavaEE8 + javax.* + Tomcat 9，jakarta 包在 Tomcat 9 不存在 | 全部替换为 javax.servlet.*，pom 引 javax.servlet-api 4.0.1 | mvn package 成功，Tomcat 9 启动无 ClassNotFound，页面 200 |
| 升级到 Spring Boot 3 可以少写配置 | Spring Boot 3 基于 Jakarta EE 9+，需 JDK17，与 JavaEE8 基线冲突 | 拒绝升级，保持 Servlet + JSP + JDBC + Tomcat 9 | 项目在 JDK8 编译、Tomcat 9 部署运行正常 |
| 用 Statement 拼接 SQL 查询更方便 | 存在 SQL 注入风险，且不符合参数化查询规范 | 改为 PreparedStatement + 占位符绑定参数 | 新增含特殊字符的记录仍可正常保存 |
