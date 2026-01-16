-- 题库表（用于缓存AI生成的题目，提升出题速度）
CREATE TABLE IF NOT EXISTS question_bank (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    subject VARCHAR(100) NOT NULL COMMENT '学科/主题',
    knowledge_point VARCHAR(200) NOT NULL COMMENT '知识点',
    type VARCHAR(50) NOT NULL COMMENT '题目类型：single_choice, multiple_choice, fill, short_answer, coding',
    difficulty VARCHAR(20) NOT NULL COMMENT '难度：easy, medium, hard',
    content TEXT NOT NULL COMMENT '题目内容',
    options TEXT COMMENT '选项（JSON格式）',
    answer TEXT NOT NULL COMMENT '正确答案',
    analysis TEXT COMMENT '题目解析',
    score INT DEFAULT 10 COMMENT '建议分值',
    source VARCHAR(50) DEFAULT 'ai_generated' COMMENT '来源：ai_generated, manual, imported',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    avg_score DOUBLE DEFAULT 0.0 COMMENT '平均得分率',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_subject (subject),
    INDEX idx_knowledge_point (knowledge_point),
    INDEX idx_type (type),
    INDEX idx_difficulty (difficulty),
    INDEX idx_use_count (use_count),
    INDEX idx_composite (subject, knowledge_point, type, difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库表';

-- 插入一些示例题目（TensorFlow.js相关）
INSERT INTO question_bank (subject, knowledge_point, type, difficulty, content, options, answer, analysis, score, source) VALUES
('TensorFlow.js', '基础概念', 'single_choice', 'easy', 
'TensorFlow.js是什么？', 
'[{"label":"A","content":"一个JavaScript机器学习库"},{"label":"B","content":"一个Python深度学习框架"},{"label":"C","content":"一个数据可视化工具"},{"label":"D","content":"一个Web服务器"}]',
'A',
'TensorFlow.js是Google开发的JavaScript机器学习库，可以在浏览器和Node.js中运行。',
10, 'manual'),

('TensorFlow.js', '张量操作', 'single_choice', 'medium',
'在TensorFlow.js中，如何创建一个2x3的张量？',
'[{"label":"A","content":"tf.tensor([[1,2,3],[4,5,6]])"},{"label":"B","content":"tf.array([[1,2,3],[4,5,6]])"},{"label":"C","content":"tf.matrix([[1,2,3],[4,5,6]])"},{"label":"D","content":"tf.create([[1,2,3],[4,5,6]])"}]',
'A',
'使用tf.tensor()方法可以创建张量，传入二维数组即可创建2x3的张量。',
10, 'manual'),

('计算机科学', '算法基础', 'single_choice', 'easy',
'以下哪种排序算法的平均时间复杂度最低？',
'[{"label":"A","content":"冒泡排序"},{"label":"B","content":"快速排序"},{"label":"C","content":"选择排序"},{"label":"D","content":"插入排序"}]',
'B',
'快速排序的平均时间复杂度为O(n log n)，是最优的比较排序算法之一。',
10, 'manual'),

('计算机科学', '数据结构', 'single_choice', 'medium',
'栈的特点是什么？',
'[{"label":"A","content":"先进先出(FIFO)"},{"label":"B","content":"后进先出(LIFO)"},{"label":"C","content":"随机访问"},{"label":"D","content":"双向访问"}]',
'B',
'栈是一种后进先出(LIFO)的数据结构，最后压入的元素最先弹出。',
10, 'manual'),

('JavaScript', '基础语法', 'single_choice', 'easy',
'JavaScript中声明变量使用哪个关键字？',
'[{"label":"A","content":"var, let, const"},{"label":"B","content":"int, float, string"},{"label":"C","content":"dim, declare"},{"label":"D","content":"variable, value"}]',
'A',
'JavaScript中可以使用var、let或const来声明变量，其中let和const是ES6新增的。',
10, 'manual');

-- 查询题库统计信息
SELECT 
    subject,
    COUNT(*) as total_questions,
    SUM(CASE WHEN difficulty = 'easy' THEN 1 ELSE 0 END) as easy_count,
    SUM(CASE WHEN difficulty = 'medium' THEN 1 ELSE 0 END) as medium_count,
    SUM(CASE WHEN difficulty = 'hard' THEN 1 ELSE 0 END) as hard_count
FROM question_bank
GROUP BY subject;

