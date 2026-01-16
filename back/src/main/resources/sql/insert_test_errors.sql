-- 插入测试错题数据
USE web_experiment;

-- 首先查看学生17的考试记录
SELECT '=== 学生17的考试记录 ===' as info;
SELECT id, student_id, exam_id, score, status FROM student_exam WHERE student_id = 17 LIMIT 5;

-- 查看可用的题目
SELECT '=== 可用题目 ===' as info;
SELECT q.id, q.exam_id, q.type, LEFT(q.content, 50) as content_preview 
FROM question q 
WHERE q.exam_id IN (SELECT exam_id FROM student_exam WHERE student_id = 17)
LIMIT 10;

-- 插入错题数据（使用学生17的第一个考试记录）
INSERT INTO student_answer (student_exam_id, question_id, answer, is_correct, score, create_time) 
SELECT 
    se.id as student_exam_id,
    q.id as question_id,
    CASE 
        WHEN q.answer = 'A' THEN 'B'
        WHEN q.answer = 'B' THEN 'C'
        WHEN q.answer = 'C' THEN 'D'
        ELSE 'A'
    END as answer,  -- 故意选错误答案
    0 as is_correct,
    0 as score,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) as create_time  -- 随机过去30天内的时间
FROM student_exam se
INNER JOIN question q ON q.exam_id = se.exam_id
WHERE se.student_id = 17
AND q.type IN ('single_choice', 'multiple_choice')
LIMIT 5;

-- 验证插入结果
SELECT '=== 插入的错题数据 ===' as info;
SELECT 
    sa.id,
    sa.student_exam_id,
    sa.question_id,
    LEFT(q.content, 50) as question_preview,
    q.answer as correct_answer,
    sa.answer as student_answer,
    sa.is_correct,
    sa.create_time
FROM student_answer sa
INNER JOIN student_exam se ON sa.student_exam_id = se.id
INNER JOIN question q ON sa.question_id = q.id
WHERE se.student_id = 17
AND sa.is_correct = 0
ORDER BY sa.create_time DESC;

SELECT '=== 完成！===' as info;

