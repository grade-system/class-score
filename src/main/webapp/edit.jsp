<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <title>修改成绩</title>
  <style>
    body { font-family: "Microsoft YaHei", Arial, sans-serif; margin: 30px; }
    label { display: inline-block; width: 90px; }
    .row { margin: 10px 0; }
    .err { color: #c62828; margin-bottom: 10px; }
    .tip { color: #666; font-size: 13px; margin-bottom: 12px; }
    input[type=text] { width: 220px; padding: 4px; }
    button { padding: 5px 18px; background: #1976d2; color: #fff; border: none;
             border-radius: 3px; cursor: pointer; }
    a { color: #1976d2; text-decoration: none; margin-left: 8px; }
    .ro { color: #333; }
  </style>
</head>
<body>
<h2>修改成绩</h2>
<div class="tip">业务规则：修改成绩必须填写修改人，系统会记录修改前后分数与修改时间。</div>

<c:if test="${not empty error}">
  <div class="err">${error}</div>
</c:if>

<form action="scores" method="post">
  <input type="hidden" name="action" value="update">
  <input type="hidden" name="id" value="${record.id}">
  <div class="row"><label>学号：</label><span class="ro">${record.studentNo}</span></div>
  <div class="row"><label>姓名：</label><span class="ro">${record.studentName}</span></div>
  <div class="row"><label>班级：</label><span class="ro">${record.className}</span></div>
  <div class="row"><label>科目：</label><span class="ro">${record.subject}</span></div>
  <div class="row"><label>当前分数：</label><span class="ro">${record.score}</span></div>
  <div class="row">
    <label>修改后分数：</label>
    <input type="text" name="score" value="${record.score}">
  </div>
  <div class="row">
    <label>修改人：</label>
    <input type="text" name="operator" value="${param.operator}" placeholder="必填，例如：陈春丽">
  </div>
  <div class="row">
    <button type="submit">保存修改</button>
    <a href="scores">返回列表</a>
  </div>
</form>
</body>
</html>
