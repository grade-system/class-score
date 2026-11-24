<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
  <title>成绩修改记录</title>
  <style>
    body { font-family: "Microsoft YaHei", Arial, sans-serif; margin: 30px; }
    table { border-collapse: collapse; }
    th, td { border: 1px solid #999; padding: 6px 14px; text-align: center; }
    th { background: #f0f0f0; }
    .empty { color: #888; margin: 16px 0; }
    a.btn { display: inline-block; margin-bottom: 12px; padding: 6px 14px;
            background: #1976d2; color: #fff; text-decoration: none; border-radius: 3px; }
  </style>
</head>
<body>
<h2>成绩修改记录</h2>
<a class="btn" href="scores">返回成绩列表</a>

<c:choose>
  <c:when test="${empty logs}">
    <div class="empty">暂无修改记录。</div>
  </c:when>
  <c:otherwise>
    <table>
      <tr>
        <th>记录编号</th>
        <th>学号</th>
        <th>科目</th>
        <th>修改前</th>
        <th>修改后</th>
        <th>修改人</th>
        <th>修改时间</th>
      </tr>
      <c:forEach items="${logs}" var="log">
        <tr>
          <td>${log.scoreId}</td>
          <td>${log.studentNo}</td>
          <td>${log.subject}</td>
          <td>${log.oldScore}</td>
          <td>${log.newScore}</td>
          <td><c:out value="${log.operator}"/></td>
          <td><fmt:formatDate value="${log.changeTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
        </tr>
      </c:forEach>
    </table>
  </c:otherwise>
</c:choose>
</body>
</html>
