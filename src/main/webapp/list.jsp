<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
  <title>班级学生成绩管理</title>
  <style>
    body { font-family: "Microsoft YaHei", Arial, sans-serif; margin: 30px; }
    .topbar { display: flex; justify-content: space-between; align-items: center;
              border-bottom: 1px solid #e0e0e0; padding-bottom: 10px; margin-bottom: 16px; }
    .stats { display: flex; gap: 28px; margin: 14px 0 18px; }
    .stat { border: 1px solid #d0d7de; border-radius: 6px; padding: 10px 22px; background: #f7f9fc; }
    .stat .label { color: #666; font-size: 13px; }
    .stat .value { font-size: 22px; font-weight: bold; color: #1565c0; }
    table { border-collapse: collapse; }
    th, td { border: 1px solid #999; padding: 6px 14px; text-align: center; }
    th { background: #f0f0f0; }
    .ok { color: #2e7d32; margin: 10px 0; }
    .empty { color: #888; margin: 16px 0; }
    a.btn { display: inline-block; margin-bottom: 12px; padding: 6px 14px;
            background: #1976d2; color: #fff; text-decoration: none; border-radius: 3px; }
    .pass { color: #2e7d32; font-weight: bold; }
    .fail { color: #c62828; font-weight: bold; }
  </style>
</head>
<body>
<div class="topbar">
  <h2 style="margin:0;">班级学生成绩管理</h2>
  <div style="color:#666;font-size:14px;">JavaEE8 + Servlet + JSP + JDBC + Tomcat9</div>
</div>

<c:if test="${param.ok == 1}">
  <div class="ok">成绩录入成功！</div>
</c:if>
<c:if test="${param.updated == 1}">
  <div class="ok">成绩修改成功，已记录修改人与修改时间。</div>
</c:if>

<div class="stats">
  <div class="stat">
    <div class="label">平均分</div>
    <div class="value">${average}</div>
  </div>
  <div class="stat">
    <div class="label">及格率</div>
    <div class="value">${passRate}%</div>
  </div>
</div>

<a class="btn" href="add.jsp">+ 录入成绩</a>
<a class="btn" href="scores?action=logs">修改记录</a>

<c:choose>
  <c:when test="${empty scoreList}">
    <div class="empty">暂无成绩记录，请点击“录入成绩”。</div>
  </c:when>
  <c:otherwise>
    <table>
      <tr>
        <th>编号</th>
        <th>学号</th>
        <th>姓名</th>
        <th>班级</th>
        <th>科目</th>
        <th>分数</th>
        <th>是否及格</th>
        <th>录入时间</th>
        <th>修改人</th>
        <th>修改时间</th>
        <th>操作</th>
      </tr>
      <c:forEach items="${scoreList}" var="s">
        <tr>
          <td>${s.id}</td>
          <td>${s.studentNo}</td>
          <td>${s.studentName}</td>
          <td>${s.className}</td>
          <td>${s.subject}</td>
          <td>${s.score}</td>
          <td>
            <c:choose>
              <c:when test="${s.score >= 60}"><span class="pass">及格</span></c:when>
              <c:otherwise><span class="fail">不及格</span></c:otherwise>
            </c:choose>
          </td>
          <td><fmt:formatDate value="${s.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
          <td><c:out value="${empty s.modifier ? '-' : s.modifier}"/></td>
          <td>
            <c:choose>
              <c:when test="${empty s.updateTime}">-</c:when>
              <c:otherwise><fmt:formatDate value="${s.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/></c:otherwise>
            </c:choose>
          </td>
          <td><a href="scores?action=edit&id=${s.id}">修改</a></td>
        </tr>
      </c:forEach>
    </table>
  </c:otherwise>
</c:choose>
</body>
</html>
