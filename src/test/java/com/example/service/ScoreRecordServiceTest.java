package com.example.service;

import com.example.entity.ScoreRecord;
import com.example.entity.SubjectStat;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * 业务层单元测试：只覆盖不依赖数据库的校验与计算逻辑
 */
public class ScoreRecordServiceTest {

    private final ScoreRecordService service = new ScoreRecordService();

    @Test
    public void 修改人不能为空() {
        try {
            service.updateScore(1, 80, "  ");
            fail("修改人为空时应抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("请填写修改人", e.getMessage());
        }
    }

    @Test
    public void 修改分数必须在一百分以内() {
        try {
            service.updateScore(1, 101, "陈春丽");
            fail("分数大于 100 时应抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("分数必须在 0 到 100 之间", e.getMessage());
        }
    }

    @Test
    public void 录入时分数越界应被拒绝() {
        ScoreRecord record = new ScoreRecord();
        record.setStudentNo("24110741109");
        record.setStudentName("陈春丽");
        record.setSubject("JavaEE");
        record.setScore(-1.0);
        try {
            service.add(record);
            fail("负分应在写库前被拒绝");
        } catch (IllegalArgumentException e) {
            assertEquals("分数必须在 0 到 100 之间", e.getMessage());
        }
    }

    @Test
    public void 录入时学号不能为空() {
        ScoreRecord record = new ScoreRecord();
        record.setStudentNo("  ");
        record.setStudentName("陈春丽");
        record.setSubject("JavaEE");
        record.setScore(90.0);
        try {
            service.add(record);
            fail("学号为空应被拒绝");
        } catch (IllegalArgumentException e) {
            assertEquals("请填写学号", e.getMessage());
        }
    }

    @Test
    public void 分页总数计算() {
        assertEquals(1, service.totalPages(0));
        assertEquals(1, service.totalPages(10));
        assertEquals(2, service.totalPages(11));
        assertEquals(2, service.totalPages(20));
        assertEquals(3, service.totalPages(21));
    }

    @Test
    public void 科目统计及格率计算() {
        SubjectStat stat = new SubjectStat();
        stat.setTotal(4);
        stat.setPassCount(3);
        assertEquals(75.0, stat.getPassRate(), 0.001);

        SubjectStat empty = new SubjectStat();
        assertEquals(0.0, empty.getPassRate(), 0.001);
    }
}
