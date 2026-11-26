package com.example.service;

import com.example.dao.ScoreRecordDao;
import com.example.entity.ScoreChangeLog;
import com.example.entity.ScoreRecord;
import com.example.entity.SubjectStat;

import java.util.List;

/**
 * 成绩业务层：参数校验、修改留痕、平均分与及格率统计
 */
public class ScoreRecordService {

    private final ScoreRecordDao scoreRecordDao = new ScoreRecordDao();

    /** 列表查询：按科目筛选、按学号或姓名模糊搜索 */
    public List<ScoreRecord> search(String subject, String keyword) {
        return scoreRecordDao.find(subject, keyword);
    }

    /** 科目下拉选项 */
    public List<String> subjects() {
        return scoreRecordDao.findSubjects();
    }

    /** 各科目统计：平均分、最高分、最低分、及格率 */
    public List<SubjectStat> subjectStats() {
        return scoreRecordDao.findSubjectStats();
    }

    public ScoreRecord get(int id) {
        return scoreRecordDao.findById(id);
    }

    public List<ScoreChangeLog> recentLogs(int limit) {
        return scoreRecordDao.findLogs(limit);
    }

    public void add(ScoreRecord record) {
        if (record.getStudentNo() == null || record.getStudentNo().trim().isEmpty()) {
            throw new IllegalArgumentException("请填写学号");
        }
        if (record.getStudentName() == null || record.getStudentName().trim().isEmpty()) {
            throw new IllegalArgumentException("请填写姓名");
        }
        if (record.getSubject() == null || record.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("请填写科目");
        }
        if (record.getScore() == null || record.getScore() < 0 || record.getScore() > 100) {
            throw new IllegalArgumentException("分数必须在 0 到 100 之间");
        }
        record.setStudentNo(record.getStudentNo().trim());
        record.setStudentName(record.getStudentName().trim());
        if (record.getClassName() == null || record.getClassName().trim().isEmpty()) {
            record.setClassName("软件工程2班");
        }
        if (scoreRecordDao.exists(record.getStudentNo(), record.getSubject())) {
            throw new IllegalArgumentException("该学生该科目已有成绩，请使用“修改”功能");
        }
        scoreRecordDao.insert(record);
    }

    /**
     * 业务规则：修改成绩必须填写修改人，且分数在 0~100 之间，修改后写入留痕
     */
    public void updateScore(int id, double newScore, String operator) {
        if (operator == null || operator.trim().isEmpty()) {
            throw new IllegalArgumentException("请填写修改人");
        }
        if (newScore < 0 || newScore > 100) {
            throw new IllegalArgumentException("分数必须在 0 到 100 之间");
        }
        boolean ok = scoreRecordDao.updateWithLog(id, newScore, operator.trim());
        if (!ok) {
            throw new IllegalArgumentException("成绩记录不存在，无法修改");
        }
    }

    /** 平均分：无数据时返回 0 */
    public double average(List<ScoreRecord> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (ScoreRecord r : list) {
            sum += r.getScore();
        }
        return sum / list.size();
    }

    /** 及格率（>=60 分）：无数据时返回 0 */
    public double passRate(List<ScoreRecord> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        int pass = 0;
        for (ScoreRecord r : list) {
            if (r.getScore() >= 60) {
                pass++;
            }
        }
        return pass * 100.0 / list.size();
    }
}
