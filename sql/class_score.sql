-- 实验四：班级学生成绩管理（初始 MVP）
CREATE DATABASE IF NOT EXISTS class_score DEFAULT CHARACTER SET utf8mb4;
USE class_score;

DROP TABLE IF EXISTS score_record;
CREATE TABLE score_record (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    student_no   VARCHAR(20)  NOT NULL COMMENT '学号',
    student_name VARCHAR(50)  NOT NULL COMMENT '姓名',
    class_name   VARCHAR(50)  NOT NULL COMMENT '班级',
    subject      VARCHAR(50)  NOT NULL COMMENT '科目',
    score        DOUBLE       NOT NULL COMMENT '分数',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO score_record(student_no, student_name, class_name, subject, score) VALUES
('24110741101', '张三', '软件工程2班', 'Java EE', 88.5),
('24110741102', '李四', '软件工程2班', 'Java EE', 76.0),
('24110741103', '王五', '软件工程2班', 'Java EE', 91.5),
('24110741104', '赵六', '软件工程2班', 'Java EE', 52.0),
('24110741109', '陈春丽', '软件工程2班', 'Java EE', 95.0);
