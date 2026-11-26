package com.example.dao;

import com.example.entity.ScoreChangeLog;
import com.example.entity.ScoreRecord;
import com.example.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 成绩数据访问层：只负责 SQL 与结果集映射
 */
public class ScoreRecordDao {

    private static final DBUtil.RowMapper<ScoreRecord> ROW_MAPPER = new DBUtil.RowMapper<ScoreRecord>() {
        @Override
        public ScoreRecord mapRow(ResultSet rs) throws SQLException {
            ScoreRecord r = new ScoreRecord();
            r.setId(rs.getInt("id"));
            r.setStudentNo(rs.getString("student_no"));
            r.setStudentName(rs.getString("student_name"));
            r.setClassName(rs.getString("class_name"));
            r.setSubject(rs.getString("subject"));
            r.setScore(rs.getDouble("score"));
            r.setCreateTime(rs.getTimestamp("create_time"));
            r.setModifier(rs.getString("modifier"));
            r.setUpdateTime(rs.getTimestamp("update_time"));
            return r;
        }
    };

    private static final DBUtil.RowMapper<ScoreChangeLog> LOG_MAPPER = new DBUtil.RowMapper<ScoreChangeLog>() {
        @Override
        public ScoreChangeLog mapRow(ResultSet rs) throws SQLException {
            ScoreChangeLog log = new ScoreChangeLog();
            log.setId(rs.getInt("id"));
            log.setScoreId(rs.getInt("score_id"));
            log.setStudentNo(rs.getString("student_no"));
            log.setSubject(rs.getString("subject"));
            log.setOldScore(rs.getDouble("old_score"));
            log.setNewScore(rs.getDouble("new_score"));
            log.setOperator(rs.getString("operator"));
            log.setChangeTime(rs.getTimestamp("change_time"));
            return log;
        }
    };

    /**
     * 列表查询：科目精确筛选 + 学号/姓名模糊搜索，条件为空时等价于全量查询
     */
    public List<ScoreRecord> find(String subject, String keyword) {
        StringBuilder sql = new StringBuilder("select id, student_no, student_name, class_name, subject, score, "
                + "create_time, modifier, update_time from score_record where 1 = 1");
        List<Object> args = new ArrayList<Object>();
        if (subject != null && !subject.trim().isEmpty()) {
            sql.append(" and subject = ?");
            args.add(subject.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" and (student_no like ? or student_name like ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        sql.append(" order by id desc");
        return DBUtil.query(sql.toString(), ROW_MAPPER, args.toArray());
    }

    /** 科目下拉选项：去重并按字典序 */
    public List<String> findSubjects() {
        String sql = "select distinct subject from score_record order by subject";
        return DBUtil.query(sql, new DBUtil.RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs) throws SQLException {
                return rs.getString(1);
            }
        });
    }

    public ScoreRecord findById(int id) {
        String sql = "select id, student_no, student_name, class_name, subject, score, "
                + "create_time, modifier, update_time from score_record where id = ?";
        return DBUtil.queryOne(sql, ROW_MAPPER, id);
    }

    public int insert(ScoreRecord record) {
        String sql = "insert into score_record(student_no, student_name, class_name, subject, score) "
                + "values(?, ?, ?, ?, ?)";
        return DBUtil.update(sql, record.getStudentNo(), record.getStudentName(),
                record.getClassName(), record.getSubject(), record.getScore());
    }

    /**
     * 修改成绩并写留痕：两条 SQL 在同一个事务中，任一步失败整体回滚
     */
    public boolean updateWithLog(int id, double newScore, String operator) {
        String selectSql = "select student_no, subject, score from score_record where id = ? for update";
        String updateSql = "update score_record set score = ?, modifier = ?, update_time = now() where id = ?";
        String logSql = "insert into score_change_log(score_id, student_no, subject, old_score, new_score, operator) "
                + "values(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String studentNo;
                String subject;
                double oldScore;
                try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        studentNo = rs.getString("student_no");
                        subject = rs.getString("subject");
                        oldScore = rs.getDouble("score");
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setDouble(1, newScore);
                    ps.setString(2, operator);
                    ps.setInt(3, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                    ps.setInt(1, id);
                    ps.setString(2, studentNo);
                    ps.setString(3, subject);
                    ps.setDouble(4, oldScore);
                    ps.setDouble(5, newScore);
                    ps.setString(6, operator);
                    ps.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("修改成绩失败", e);
        }
    }

    public List<ScoreChangeLog> findLogs(int limit) {
        String sql = "select id, score_id, student_no, subject, old_score, new_score, operator, change_time "
                + "from score_change_log order by id desc limit ?";
        return DBUtil.query(sql, LOG_MAPPER, limit);
    }

    /** 业务规则：同一学生同一科目只允许一条成绩 */
    public boolean exists(String studentNo, String subject) {
        String sql = "select count(1) from score_record where student_no = ? and subject = ?";
        Integer count = DBUtil.queryOne(sql, new DBUtil.RowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs) throws SQLException {
                return rs.getInt(1);
            }
        }, studentNo, subject);
        return count != null && count > 0;
    }
}
