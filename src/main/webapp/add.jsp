<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <title>录入成绩</title>
  <style>
    body { font-family: "Microsoft YaHei", Arial, sans-serif; margin: 30px; }
    label { display: inline-block; width: 70px; }
    .row { margin: 10px 0; }
    .err { color: #c62828; margin-bottom: 10px; }
    input[type=text] { width: 220px; padding: 4px; }
    button { padding: 5px 18px; background: #1976d2; color: #fff; border: none;
             border-radius: 3px; cursor: pointer; }
    a { color: #1976d2; text-decoration: none; margin-left: 8px; }
  </style>
</head>
<body>
<h2>教师录入成绩</h2>

<c:if test="${not empty error}">
  <div class="err">${error}</div>
</c:if>

<form action="scores" method="post">
  <div class="row">
    <label>学号：</label>
    <input type="text" name="studentNo" value="${param.studentNo}">
  </div>
  <div class="row">
    <label>姓名：</label>
    <input type="text" name="studentName" value="${param.studentName}">
  </div>
  <div class="row">
    <label>班级：</label>
    <input type="text" name="className" value="${param.className}">
  </div>
  <div class="row">
    <label>科目：</label>
    <input type="text" name="subject" value="${param.subject}">
  </div>
  <div class="row">
    <label>分数：</label>
    <input type="text" name="score" value="${param.score}">
  </div>
  <div class="row">
    <button type="submit">提交</button>
    <a href="scores">返回列表</a>
  </div>
</form>
</body>
</html>
