package com.example.entity;

import java.util.Date;

/**
 * 成绩修改留痕：对应 score_change_log 表
 */
public class ScoreChangeLog {

    private Integer id;
    private Integer scoreId;
    private String studentNo;
    private String subject;
    private Double oldScore;
    private Double newScore;
    private String operator;
    private Date changeTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getScoreId() {
        return scoreId;
    }

    public void setScoreId(Integer scoreId) {
        this.scoreId = scoreId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Double getOldScore() {
        return oldScore;
    }

    public void setOldScore(Double oldScore) {
        this.oldScore = oldScore;
    }

    public Double getNewScore() {
        return newScore;
    }

    public void setNewScore(Double newScore) {
        this.newScore = newScore;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Date getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(Date changeTime) {
        this.changeTime = changeTime;
    }
}
