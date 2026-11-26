package com.example.servlet;

import com.example.util.DBUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 健康检查：部署后一条 curl 即可判断应用、容器、数据库是否都正常。
 * 正常返回 200 + JSON，数据库不可用返回 503 + 错误原因。
 */
@WebServlet("/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        boolean dbOk = false;
        int tables = 0;
        int records = 0;
        String error = null;

        try (Connection conn = DBUtil.getConnection();
             Statement st = conn.createStatement()) {
            try (ResultSet rs = st.executeQuery(
                    "select count(*) from information_schema.tables where table_schema = database()")) {
                tables = rs.next() ? rs.getInt(1) : 0;
            }
            try (ResultSet rs = st.executeQuery("select count(*) from score_record")) {
                records = rs.next() ? rs.getInt(1) : 0;
            }
            dbOk = true;
        } catch (Exception e) {
            error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        }

        String status = dbOk ? "UP" : "DOWN";
        response.setStatus(dbOk ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE);

        PrintWriter out = response.getWriter();
        out.print("{"
                + "\"status\":\"" + status + "\","
                + "\"app\":\"class-score\","
                + "\"version\":\"" + esc(getServletContext().getInitParameter("app.version") == null
                ? "1.0" : getServletContext().getInitParameter("app.version")) + "\","
                + "\"contextPath\":\"" + esc(request.getContextPath()) + "\","
                + "\"port\":" + request.getServerPort() + ","
                + "\"java\":\"" + esc(System.getProperty("java.version")) + "\","
                + "\"container\":\"" + esc(getServletContext().getServerInfo()) + "\","
                + "\"db\":{\"connected\":" + dbOk
                + ",\"tables\":" + tables
                + ",\"records\":" + records
                + ",\"url\":\"" + esc(DBUtil.config("db.url", "")) + "\""
                + (error == null ? "" : ",\"error\":\"" + esc(error) + "\"")
                + "},"
                + "\"time\":\"" + new java.util.Date() + "\""
                + "}");
        out.flush();
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", " ").replace("\n", " ");
    }
}
