package com.example.dao;

import com.example.entity.ScoreRecord;
import com.example.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
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
            return r;
        }
    };

    public List<ScoreRecord> findAll() {
        String sql = "select id, student_no, student_name, class_name, subject, score, create_time "
                + "from score_record order by id desc";
        return DBUtil.query(sql, ROW_MAPPER);
    }

    public int insert(ScoreRecord record) {
        String sql = "insert into score_record(student_no, student_name, class_name, subject, score) "
                + "values(?, ?, ?, ?, ?)";
        return DBUtil.update(sql, record.getStudentNo(), record.getStudentName(),
                record.getClassName(), record.getSubject(), record.getScore());
    }
}
