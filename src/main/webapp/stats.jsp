<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
  <title>科目统计</title>
  <style>
    body { font-family: "Microsoft YaHei", Arial, sans-serif; margin: 30px; }
    table { border-collapse: collapse; }
    th, td { border: 1px solid #999; padding: 6px 16px; text-align: center; }
    th { background: #f0f0f0; }
    .empty { color: #888; margin: 16px 0; }
    a.btn { display: inline-block; margin-bottom: 12px; padding: 6px 14px;
            background: #1976d2; color: #fff; text-decoration: none; border-radius: 3px; }
    .pass { color: #2e7d32; font-weight: bold; }
    .fail { color: #c62828; font-weight: bold; }
  </style>
</head>
<body>
<h2>科目成绩统计</h2>
<a class="btn" href="scores">返回成绩列表</a>

<c:choose>
  <c:when test="${empty stats}">
    <div class="empty">暂无成绩数据，无法统计。</div>
  </c:when>
  <c:otherwise>
    <table>
      <tr>
        <th>科目</th>
        <th>人数</th>
        <th>平均分</th>
        <th>最高分</th>
        <th>最低分</th>
        <th>及格人数</th>
        <th>及格率</th>
      </tr>
      <c:forEach items="${stats}" var="s">
        <tr>
          <td><c:out value="${s.subject}"/></td>
          <td>${s.total}</td>
          <td><fmt:formatNumber value="${s.average}" pattern="0.0"/></td>
          <td><fmt:formatNumber value="${s.maxScore}" pattern="0.0"/></td>
          <td><fmt:formatNumber value="${s.minScore}" pattern="0.0"/></td>
          <td>${s.passCount}</td>
          <td>
            <c:choose>
              <c:when test="${s.passRate >= 60}"><span class="pass"><fmt:formatNumber value="${s.passRate}" pattern="0.0"/>%</span></c:when>
              <c:otherwise><span class="fail"><fmt:formatNumber value="${s.passRate}" pattern="0.0"/>%</span></c:otherwise>
            </c:choose>
          </td>
        </tr>
      </c:forEach>
    </table>
  </c:otherwise>
</c:choose>
</body>
</html>
