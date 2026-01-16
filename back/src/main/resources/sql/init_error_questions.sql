-- 初始化错题测试数据
USE web_experiment;

-- 1. 创建 student_answer 表（如果不存在）
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

-- 2. 查看当前学生ID=17的考试记录
SELECT '=== 学生17的考试记录 ===' as info;
SELECT id, student_id, exam_id, score, status FROM student_exam WHERE student_id = 17;

-- 3. 查看考试对应的题目
SELECT '=== 考试题目 ===' as info;
SELECT q.id, q.exam_id, q.type, q.content, q.answer, q.knowledge_point, q.difficulty 
FROM question q 
WHERE q.exam_id IN (SELECT exam_id FROM student_exam WHERE student_id = 17)
LIMIT 10;

-- 4. 插入测试错题数据（学生ID=17）
-- 注意：需要根据实际的 student_exam_id 和 question_id 调整

-- 假设 student_exam_id = 1（需要根据实际情况调整）
-- 插入一些错题示例
INSERT INTO student_answer (student_exam_id, question_id, answer, is_correct, score, create_time) 
SELECT 
    se.id as student_exam_id,
    q.id as question_id,
    'B' as answer,  -- 错误答案
    0 as is_correct,
    0 as score,
    NOW() as create_time
FROM student_exam se
INNER JOIN question q ON q.exam_id = se.exam_id
WHERE se.student_id = 17
AND q.type = 'choice'
LIMIT 3
ON DUPLICATE KEY UPDATE answer = VALUES(answer);

-- 5. 验证插入的数据
SELECT '=== 插入的错题数据 ===' as info;
SELECT 
    sa.id,
    sa.student_exam_id,
    sa.question_id,
    q.content as question_content,
    q.answer as correct_answer,
    sa.answer as student_answer,
    sa.is_correct,
    sa.create_time
FROM student_answer sa
INNER JOIN student_exam se ON sa.student_exam_id = se.id
INNER JOIN question q ON sa.question_id = q.id
WHERE se.student_id = 17;

SELECT '=== 完成！===' as info;

