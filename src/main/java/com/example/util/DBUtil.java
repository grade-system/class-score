package com.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库工具类：统一获取连接、绑定参数、遍历结果集并关闭资源
 */
public class DBUtil {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/class_score?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PWD = "123456";

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PWD);
    }

    public interface RowMapper<T> {
        T mapRow(ResultSet rs) throws SQLException;
    }

    public static <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        List<T> list = new ArrayList<T>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            setParams(ps, params);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapper.mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("数据库查询失败", e);
        } finally {
            close(conn, ps, rs);
        }
        return list;
    }

    public static int update(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            setParams(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("数据库更新失败", e);
        } finally {
            close(conn, ps, null);
        }
    }

    private static void setParams(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    private static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException ignored) {
        }
        try {
            if (ps != null) ps.close();
        } catch (SQLException ignored) {
        }
        try {
            if (conn != null) conn.close();
        } catch (SQLException ignored) {
        }
    }
}
