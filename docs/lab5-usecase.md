# 核心业务闭环与业务规则（实验五）

## 一、闭环用例

教师修改成绩：

1. 打开成绩列表（`/class-score/scores`）；
2. 点击某条记录的“修改”，进入修改页（`/class-score/scores?action=edit&id=4`）；
3. 填写“修改后分数”和“修改人”，点击“保存修改”；
4. 列表页提示“成绩修改成功”，该行分数、修改人、修改时间更新，平均分/及格率同步变化；
5. 点击“修改记录”，可查看修改前分数、修改后分数、修改人与时间。

## 二、业务规则

| 编号 | 规则 | 实现位置 |
|---|---|---|
| R1 | 修改成绩必须填写修改人，并记录修改前后分数与时间 | `ScoreRecordService.updateScore` + `score_change_log` 表 |
| R2 | 分数必须在 0~100 之间，修改后仍然生效 | `ScoreRecordService.updateScore` |
| R3 | 同一学生同一科目只允许一条成绩，重复录入被拒绝 | `unique key uk_student_subject` + `ScoreRecordService.add` |
| R4 | 修改分数与写留痕必须在同一事务中，任一失败整体回滚 | `ScoreRecordDao.updateWithLog` |

## 三、数据库变化

- `score_record` 新增 `modifier`（最后修改人）、`update_time`（最后修改时间）；
- 新增 `score_change_log`：记录 `old_score`、`new_score`、`operator`、`change_time`；
- 新增唯一索引 `uk_student_subject(student_no, subject)`。
