package com.example.service;

import com.example.dao.ScoreRecordDao;
import com.example.entity.ScoreRecord;

import java.util.List;

/**
 * 成绩业务层：参数校验、平均分与及格率统计
 */
public class ScoreRecordService {

    private final ScoreRecordDao scoreRecordDao = new ScoreRecordDao();

    public List<ScoreRecord> listAll() {
        return scoreRecordDao.findAll();
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
        scoreRecordDao.insert(record);
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
