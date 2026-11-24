package com.example.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 数据库工具类：连接参数来自 db.properties，支持环境变量覆盖
 * 环境变量：DB_URL / DB_USERNAME / DB_PASSWORD
 */
public class DBUtil {

    private static final Properties CONFIG = new Properties();

    static {
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("classpath 下缺少 db.properties");
            }
            CONFIG.load(in);
            Class.forName(get("db.driver", "com.mysql.cj.jdbc.Driver"));
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBUtil() {
    }

    private static String get(String key, String defaultValue) {
        String envKey = "DB_" + key.substring(key.indexOf('.') + 1).toUpperCase();
        String env = System.getenv(envKey);
        if (env != null && !env.trim().isEmpty()) {
            return env.trim();
        }
        return CONFIG.getProperty(key, defaultValue);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(get("db.url", ""), get("db.username", ""), get("db.password", ""));
    }

    public interface RowMapper<T> {
        T mapRow(ResultSet rs) throws SQLException;
    }

    public static <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        List<T> list = new ArrayList<T>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("数据库查询失败", e);
        }
        return list;
    }

    public static <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) {
        List<T> list = query(sql, mapper, params);
        return list.isEmpty() ? null : list.get(0);
    }

    public static int update(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("数据库更新失败", e);
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
}
