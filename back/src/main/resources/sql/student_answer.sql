-- 学生答案表
CREATE TABLE IF NOT EXISTS student_answer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    student_exam_id BIGINT NOT NULL COMMENT '学生考试记录ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    answer TEXT COMMENT '学生答案',
    is_correct TINYINT(1) DEFAULT 0 COMMENT '是否正确：0-错误，1-正确',
    score INT DEFAULT 0 COMMENT '得分',
    teacher_comment TEXT COMMENT '教师评语',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_student_exam_id (student_exam_id),
    INDEX idx_question_id (question_id),
    INDEX idx_is_correct (is_correct),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生答案表';

-- 为已有的student_exam表添加索引（如果不存在）
-- ALTER TABLE student_exam ADD INDEX idx_student_id (student_id);
-- ALTER TABLE student_exam ADD INDEX idx_exam_id (exam_id);

-- 为question表添加索引（如果不存在）
-- ALTER TABLE question ADD INDEX idx_exam_id (exam_id);
-- ALTER TABLE question ADD INDEX idx_knowledge_point (knowledge_point);

