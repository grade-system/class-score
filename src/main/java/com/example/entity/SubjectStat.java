package com.example.entity;

/**
 * 科目统计结果（只读）：条数、平均分、最高分、最低分、及格率
 */
public class SubjectStat {

    private String subject;
    private int total;
    private double average;
    private double maxScore;
    private double minScore;
    private int passCount;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(double maxScore) {
        this.maxScore = maxScore;
    }

    public double getMinScore() {
        return minScore;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }

    public int getPassCount() {
        return passCount;
    }

    public void setPassCount(int passCount) {
        this.passCount = passCount;
    }

    /** 及格率（>=60 分）；条数为 0 时返回 0，避免除零 */
    public double getPassRate() {
        return total == 0 ? 0 : passCount * 100.0 / total;
    }
}
