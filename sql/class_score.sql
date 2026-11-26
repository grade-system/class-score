-- 实验五：班级学生成绩管理（核心业务闭环）
-- 业务规则：成绩修改必须填写修改人并留痕；同一学生同一科目只允许一条成绩

CREATE DATABASE IF NOT EXISTS class_score DEFAULT CHARACTER SET utf8mb4;
USE class_score;

DROP TABLE IF EXISTS score_change_log;
DROP TABLE IF EXISTS score_record;

CREATE TABLE score_record (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    student_no   VARCHAR(20)  NOT NULL COMMENT '学号',
    student_name VARCHAR(50)  NOT NULL COMMENT '姓名',
    class_name   VARCHAR(50)  NOT NULL COMMENT '班级',
    subject      VARCHAR(50)  NOT NULL COMMENT '科目',
    score        DOUBLE       NOT NULL COMMENT '分数',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
    modifier     VARCHAR(32)  DEFAULT NULL COMMENT '最后修改人',
    update_time  DATETIME     DEFAULT NULL COMMENT '最后修改时间',
    UNIQUE KEY uk_student_subject (student_no, subject)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE score_change_log (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    score_id    INT          NOT NULL COMMENT '成绩记录 id',
    student_no  VARCHAR(20)  NOT NULL COMMENT '学号',
    subject     VARCHAR(50)  NOT NULL COMMENT '科目',
    old_score   DOUBLE       NOT NULL COMMENT '修改前分数',
    new_score   DOUBLE       NOT NULL COMMENT '修改后分数',
    operator    VARCHAR(32)  NOT NULL COMMENT '修改人',
    change_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    KEY idx_score (score_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO score_record(student_no, student_name, class_name, subject, score) VALUES
('24110741101', '张三', '软件工程2班', 'Java EE', 88.5),
('24110741102', '李四', '软件工程2班', 'Java EE', 76.0),
('24110741103', '王五', '软件工程2班', 'Java EE', 91.5),
('24110741104', '赵六', '软件工程2班', 'Java EE', 52.0),
('24110741109', '陈春丽', '软件工程2班', 'Java EE', 95.0),
('24110741110', '周七', '软件工程2班', 'Java EE', 58.0);

-- 最小权限数据库账号：应用只拥有 class_score 库的权限
CREATE USER IF NOT EXISTS 'score_user'@'localhost' IDENTIFIED BY 'Score@2026';
GRANT ALL PRIVILEGES ON class_score.* TO 'score_user'@'localhost';
FLUSH PRIVILEGES;
