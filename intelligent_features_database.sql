-- =============================================
-- 智能学习功能数据库表
-- 包含：智能学习路径规划、情绪感知学习助手、智能同伴学习
-- =============================================

USE web_experiment;

-- =============================================
-- 1. 智能学习路径规划相关表
-- =============================================

-- 知识点表
CREATE TABLE `knowledge_point` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '知识点名称',
    `description` TEXT COMMENT '知识点描述',
    `subject` VARCHAR(50) NOT NULL COMMENT '学科领域',
    `difficulty_level` INT DEFAULT 1 COMMENT '难度等级(1-5)',
    `estimated_duration` INT DEFAULT 60 COMMENT '预计学习时长(分钟)',
    `keywords` VARCHAR(200) COMMENT '关键词(逗号分隔)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_subject_difficulty` (`subject`, `difficulty_level`)
) COMMENT '知识点表';

-- 知识点依赖关系表
CREATE TABLE `knowledge_dependency` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `prerequisite_id` BIGINT NOT NULL COMMENT '前置知识点ID',
    `dependent_id` BIGINT NOT NULL COMMENT '依赖知识点ID',
    `dependency_type` VARCHAR(20) DEFAULT 'required' COMMENT '依赖类型:required,recommended,optional',
    `weight` DECIMAL(3,2) DEFAULT 1.00 COMMENT '依赖权重',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`prerequisite_id`) REFERENCES `knowledge_point`(`id`),
    FOREIGN KEY (`dependent_id`) REFERENCES `knowledge_point`(`id`),
    UNIQUE KEY `uk_dependency` (`prerequisite_id`, `dependent_id`)
) COMMENT '知识点依赖关系表';

-- 学习路径模板表
CREATE TABLE `learning_path_template` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '路径名称',
    `description` TEXT COMMENT '路径描述',
    `subject` VARCHAR(50) NOT NULL COMMENT '学科领域',
    `target_level` VARCHAR(20) NOT NULL COMMENT '目标水平:beginner,intermediate,advanced',
    `estimated_duration` INT NOT NULL COMMENT '预计完成时长(小时)',
    `difficulty_progression` JSON COMMENT '难度递进配置',
    `is_active` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '学习路径模板表';

-- 学习路径模板知识点关联表
CREATE TABLE `path_template_knowledge` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '路径模板ID',
    `knowledge_point_id` BIGINT NOT NULL COMMENT '知识点ID',
    `sequence_order` INT NOT NULL COMMENT '学习顺序',
    `is_required` BOOLEAN DEFAULT TRUE COMMENT '是否必需',
    `weight` DECIMAL(3,2) DEFAULT 1.00 COMMENT '权重',
    FOREIGN KEY (`template_id`) REFERENCES `learning_path_template`(`id`),
    FOREIGN KEY (`knowledge_point_id`) REFERENCES `knowledge_point`(`id`),
    UNIQUE KEY `uk_template_sequence` (`template_id`, `sequence_order`)
) COMMENT '学习路径模板知识点关联表';

-- 个性化学习路径表
CREATE TABLE `personalized_learning_path` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `template_id` BIGINT COMMENT '基础模板ID',
    `name` VARCHAR(100) NOT NULL COMMENT '路径名称',
    `current_step` INT DEFAULT 1 COMMENT '当前学习步骤',
    `completion_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '完成率(%)',
    `start_time` DATETIME COMMENT '开始时间',
    `estimated_completion` DATETIME COMMENT '预计完成时间',
    `actual_completion` DATETIME COMMENT '实际完成时间',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态:active,paused,completed,abandoned',
    `adaptation_config` JSON COMMENT '个性化配置',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`template_id`) REFERENCES `learning_path_template`(`id`)
) COMMENT '个性化学习路径表';

-- 学习路径步骤表
CREATE TABLE `learning_path_step` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `path_id` BIGINT NOT NULL COMMENT '学习路径ID',
    `knowledge_point_id` BIGINT NOT NULL COMMENT '知识点ID',
    `step_number` INT NOT NULL COMMENT '步骤序号',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态:pending,in_progress,completed,skipped',
    `start_time` DATETIME COMMENT '开始时间',
    `complete_time` DATETIME COMMENT '完成时间',
    `study_duration` INT DEFAULT 0 COMMENT '实际学习时长(分钟)',
    `mastery_level` DECIMAL(3,2) DEFAULT 0.00 COMMENT '掌握程度(0-1)',
    `difficulty_rating` INT COMMENT '学生主观难度评分(1-5)',
    `notes` TEXT COMMENT '学习笔记',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`path_id`) REFERENCES `personalized_learning_path`(`id`),
    FOREIGN KEY (`knowledge_point_id`) REFERENCES `knowledge_point`(`id`),
    UNIQUE KEY `uk_path_step` (`path_id`, `step_number`)
) COMMENT '学习路径步骤表';

-- 学习能力评估表
CREATE TABLE `learning_ability_assessment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `subject` VARCHAR(50) NOT NULL COMMENT '学科领域',
    `learning_speed` DECIMAL(3,2) DEFAULT 1.00 COMMENT '学习速度系数',
    `retention_rate` DECIMAL(3,2) DEFAULT 0.80 COMMENT '知识保持率',
    `difficulty_preference` INT DEFAULT 3 COMMENT '难度偏好(1-5)',
    `learning_style` VARCHAR(20) DEFAULT 'visual' COMMENT '学习风格:visual,auditory,kinesthetic,mixed',
    `concentration_span` INT DEFAULT 45 COMMENT '专注时长(分钟)',
    `optimal_study_time` VARCHAR(20) DEFAULT 'morning' COMMENT '最佳学习时间:morning,afternoon,evening,night',
    `assessment_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评估时间',
    `confidence_score` DECIMAL(3,2) DEFAULT 0.70 COMMENT '自信度评分',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`),
    UNIQUE KEY `uk_student_subject` (`student_id`, `subject`)
) COMMENT '学习能力评估表';

-- =============================================
-- 2. 情绪感知学习助手相关表
-- =============================================

-- 情绪检测记录表
CREATE TABLE `emotion_detection_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `session_id` VARCHAR(100) NOT NULL COMMENT '学习会话ID',
    `detection_type` VARCHAR(20) NOT NULL COMMENT '检测类型:text,facial,voice,behavioral',
    `emotion_type` VARCHAR(20) NOT NULL COMMENT '情绪类型:happy,sad,frustrated,confused,excited,bored,focused,stressed',
    `confidence` DECIMAL(3,2) NOT NULL COMMENT '置信度(0-1)',
    `intensity` DECIMAL(3,2) NOT NULL COMMENT '情绪强度(0-1)',
    `context` VARCHAR(200) COMMENT '上下文信息',
    `raw_data` JSON COMMENT '原始检测数据',
    `detection_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
    `course_id` BIGINT COMMENT '关联课程ID',
    `lesson_id` BIGINT COMMENT '关联课时ID',
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`course_id`) REFERENCES `course`(`id`),
    FOREIGN KEY (`lesson_id`) REFERENCES `lesson`(`id`),
    INDEX `idx_student_session` (`student_id`, `session_id`),
    INDEX `idx_detection_time` (`detection_time`)
) COMMENT '情绪检测记录表';

-- 学习状态监控表
CREATE TABLE `learning_status_monitor` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `session_id` VARCHAR(100) NOT NULL COMMENT '学习会话ID',
    `focus_level` DECIMAL(3,2) DEFAULT 0.80 COMMENT '专注度(0-1)',
    `engagement_score` DECIMAL(3,2) DEFAULT 0.70 COMMENT '参与度(0-1)',
    `fatigue_level` DECIMAL(3,2) DEFAULT 0.20 COMMENT '疲劳度(0-1)',
    `learning_efficiency` DECIMAL(3,2) DEFAULT 0.75 COMMENT '学习效率(0-1)',
    `interaction_frequency` INT DEFAULT 0 COMMENT '交互频次',
    `pause_duration` INT DEFAULT 0 COMMENT '暂停时长(秒)',
    `scroll_speed` DECIMAL(5,2) COMMENT '滚动速度(像素/秒)',
    `click_pattern` JSON COMMENT '点击模式数据',
    `typing_pattern` JSON COMMENT '输入模式数据',
    `monitor_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '监控时间',
    `course_id` BIGINT COMMENT '关联课程ID',
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`course_id`) REFERENCES `course`(`id`),
    INDEX `idx_student_session_monitor` (`student_id`, `session_id`)
) COMMENT '学习状态监控表';

-- 情绪干预记录表
CREATE TABLE `emotion_intervention_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `emotion_record_id` BIGINT NOT NULL COMMENT '情绪检测记录ID',
    `intervention_type` VARCHAR(30) NOT NULL COMMENT '干预类型:encouragement,break_suggestion,difficulty_adjustment,content_recommendation,music_therapy',
    `intervention_content` TEXT NOT NULL COMMENT '干预内容',
    `trigger_threshold` DECIMAL(3,2) COMMENT '触发阈值',
    `effectiveness_score` DECIMAL(3,2) COMMENT '干预效果评分(0-1)',
    `student_feedback` VARCHAR(20) COMMENT '学生反馈:helpful,neutral,unhelpful',
    `intervention_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '干预时间',
    `duration` INT COMMENT '干预持续时间(分钟)',
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`emotion_record_id`) REFERENCES `emotion_detection_record`(`id`),
    INDEX `idx_intervention_type` (`intervention_type`)
) COMMENT '情绪干预记录表';

-- 学习建议配置表
CREATE TABLE `learning_suggestion_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `emotion_type` VARCHAR(20) NOT NULL COMMENT '情绪类型',
    `intensity_range` VARCHAR(10) NOT NULL COMMENT '强度范围:low,medium,high',
    `suggestion_type` VARCHAR(30) NOT NULL COMMENT '建议类型',
    `suggestion_content` TEXT NOT NULL COMMENT '建议内容模板',
    `priority` INT DEFAULT 5 COMMENT '优先级(1-10)',
    `is_active` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_emotion_intensity_type` (`emotion_type`, `intensity_range`, `suggestion_type`)
) COMMENT '学习建议配置表';

-- =============================================
-- 3. 智能同伴学习相关表
-- =============================================

-- 学习伙伴匹配表
CREATE TABLE `learning_companion_matching` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `student1_id` BIGINT NOT NULL COMMENT '学生1 ID',
    `student2_id` BIGINT NOT NULL COMMENT '学生2 ID',
    `matching_score` DECIMAL(3,2) NOT NULL COMMENT '匹配度(0-1)',
    `matching_criteria` JSON COMMENT '匹配标准',
    `common_subjects` VARCHAR(200) COMMENT '共同学科',
    `complementary_strengths` VARCHAR(200) COMMENT '互补优势',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态:pending,accepted,rejected,active,inactive',
    `match_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '匹配时间',
    `activation_time` DATETIME COMMENT '激活时间',
    `collaboration_count` INT DEFAULT 0 COMMENT '协作次数',
    `satisfaction_rating` DECIMAL(3,2) COMMENT '满意度评分(0-1)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`student1_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`student2_id`) REFERENCES `user`(`id`),
    UNIQUE KEY `uk_student_pair` (`student1_id`, `student2_id`),
    INDEX `idx_matching_score` (`matching_score` DESC)
) COMMENT '学习伙伴匹配表';

-- 协作学习会话表
CREATE TABLE `collaborative_learning_session` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `session_name` VARCHAR(100) NOT NULL COMMENT '会话名称',
    `session_type` VARCHAR(20) NOT NULL COMMENT '会话类型:study_group,peer_tutoring,project_collaboration,discussion,quiz_competition',
    `creator_id` BIGINT NOT NULL COMMENT '创建者ID',
    `subject` VARCHAR(50) NOT NULL COMMENT '学科',
    `topic` VARCHAR(100) NOT NULL COMMENT '主题',
    `max_participants` INT DEFAULT 4 COMMENT '最大参与人数',
    `current_participants` INT DEFAULT 1 COMMENT '当前参与人数',
    `session_description` TEXT COMMENT '会话描述',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `estimated_duration` INT NOT NULL COMMENT '预计时长(分钟)',
    `actual_duration` INT COMMENT '实际时长(分钟)',
    `status` VARCHAR(20) DEFAULT 'scheduled' COMMENT '状态:scheduled,active,completed,cancelled',
    `meeting_url` VARCHAR(200) COMMENT '会议链接',
    `shared_documents` JSON COMMENT '共享文档',
    `learning_goals` TEXT COMMENT '学习目标',
    `success_metrics` JSON COMMENT '成功指标',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`creator_id`) REFERENCES `user`(`id`),
    INDEX `idx_session_time` (`start_time`, `end_time`),
    INDEX `idx_subject_topic` (`subject`, `topic`)
) COMMENT '协作学习会话表';

-- 协作学习参与者表
CREATE TABLE `collaborative_session_participant` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `session_id` BIGINT NOT NULL COMMENT '会话ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `role` VARCHAR(20) DEFAULT 'participant' COMMENT '角色:creator,moderator,participant,observer',
    `join_time` DATETIME COMMENT '加入时间',
    `leave_time` DATETIME COMMENT '离开时间',
    `participation_score` DECIMAL(3,2) DEFAULT 0.00 COMMENT '参与度评分(0-1)',
    `contribution_score` DECIMAL(3,2) DEFAULT 0.00 COMMENT '贡献度评分(0-1)',
    `interaction_count` INT DEFAULT 0 COMMENT '交互次数',
    `message_count` INT DEFAULT 0 COMMENT '消息数量',
    `resource_shared_count` INT DEFAULT 0 COMMENT '分享资源数量',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态:active,inactive,left',
    `peer_rating` DECIMAL(3,2) COMMENT '同伴评分(0-1)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`session_id`) REFERENCES `collaborative_learning_session`(`id`),
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`),
    UNIQUE KEY `uk_session_student` (`session_id`, `student_id`)
) COMMENT '协作学习参与者表';

-- 协作学习互动记录表
CREATE TABLE `collaborative_interaction_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `session_id` BIGINT NOT NULL COMMENT '会话ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `interaction_type` VARCHAR(30) NOT NULL COMMENT '交互类型:message,question,answer,resource_share,screen_share,code_review,vote,reaction',
    `content` TEXT COMMENT '交互内容',
    `target_student_id` BIGINT COMMENT '目标学生ID(私聊或指定对象)',
    `metadata` JSON COMMENT '元数据(文件、链接、代码等)',
    `interaction_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '交互时间',
    `response_count` INT DEFAULT 0 COMMENT '回应数量',
    `helpful_votes` INT DEFAULT 0 COMMENT '有用投票数',
    `quality_score` DECIMAL(3,2) COMMENT '质量评分(0-1)',
    FOREIGN KEY (`session_id`) REFERENCES `collaborative_learning_session`(`id`),
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`target_student_id`) REFERENCES `user`(`id`),
    INDEX `idx_session_time` (`session_id`, `interaction_time`),
    INDEX `idx_interaction_type` (`interaction_type`)
) COMMENT '协作学习互动记录表';

-- 学习小组表
CREATE TABLE `study_group` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `group_name` VARCHAR(100) NOT NULL COMMENT '小组名称',
    `group_description` TEXT COMMENT '小组描述',
    `subject` VARCHAR(50) NOT NULL COMMENT '学科',
    `leader_id` BIGINT NOT NULL COMMENT '组长ID',
    `max_members` INT DEFAULT 6 COMMENT '最大成员数',
    `current_members` INT DEFAULT 1 COMMENT '当前成员数',
    `group_type` VARCHAR(20) DEFAULT 'study' COMMENT '小组类型:study,project,competition,practice',
    `privacy_level` VARCHAR(20) DEFAULT 'public' COMMENT '隐私级别:public,private,invite_only',
    `activity_level` VARCHAR(20) DEFAULT 'moderate' COMMENT '活跃度:low,moderate,high',
    `learning_goals` TEXT COMMENT '学习目标',
    `group_rules` TEXT COMMENT '小组规则',
    `meeting_schedule` JSON COMMENT '会议安排',
    `performance_metrics` JSON COMMENT '绩效指标',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态:active,inactive,disbanded',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`leader_id`) REFERENCES `user`(`id`),
    INDEX `idx_subject_type` (`subject`, `group_type`)
) COMMENT '学习小组表';

-- 学习小组成员表
CREATE TABLE `study_group_member` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `group_id` BIGINT NOT NULL COMMENT '小组ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `role` VARCHAR(20) DEFAULT 'member' COMMENT '角色:leader,co_leader,member',
    `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `contribution_score` DECIMAL(3,2) DEFAULT 0.00 COMMENT '贡献度(0-1)',
    `attendance_rate` DECIMAL(3,2) DEFAULT 1.00 COMMENT '出勤率(0-1)',
    `peer_rating` DECIMAL(3,2) COMMENT '同伴评分(0-1)',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态:active,inactive,left,kicked',
    `leave_time` DATETIME COMMENT '离开时间',
    `leave_reason` VARCHAR(100) COMMENT '离开原因',
    FOREIGN KEY (`group_id`) REFERENCES `study_group`(`id`),
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`),
    UNIQUE KEY `uk_group_student` (`group_id`, `student_id`)
) COMMENT '学习小组成员表';

-- =============================================
-- 插入基础数据
-- =============================================

-- 插入知识点数据
INSERT INTO `knowledge_point` (`name`, `description`, `subject`, `difficulty_level`, `estimated_duration`, `keywords`) VALUES
('变量与数据类型', 'Java基础数据类型、变量声明与初始化', 'Java编程', 1, 60, 'Java,变量,数据类型,基础'),
('控制结构', 'if语句、循环语句、分支语句', 'Java编程', 2, 90, 'Java,控制结构,循环,条件'),
('面向对象基础', '类、对象、封装、继承、多态', 'Java编程', 3, 120, 'Java,面向对象,类,对象'),
('集合框架', 'List、Set、Map等集合类的使用', 'Java编程', 4, 150, 'Java,集合,List,Map'),
('异常处理', 'try-catch、异常类型、自定义异常', 'Java编程', 3, 90, 'Java,异常,try-catch'),
('线性表', '数组、链表的定义与操作', '数据结构', 2, 120, '数据结构,数组,链表,线性表'),
('栈与队列', '栈和队列的概念与应用', '数据结构', 3, 100, '数据结构,栈,队列,LIFO,FIFO'),
('树与二叉树', '树的定义、二叉树遍历算法', '数据结构', 4, 180, '数据结构,树,二叉树,遍历'),
('图论基础', '图的表示方法、搜索算法', '数据结构', 5, 200, '数据结构,图,搜索,算法'),
('排序算法', '冒泡、选择、插入、快速排序等', '算法', 3, 150, '算法,排序,冒泡,快排');

-- 插入知识点依赖关系
INSERT INTO `knowledge_dependency` (`prerequisite_id`, `dependent_id`, `dependency_type`, `weight`) VALUES
(1, 2, 'required', 1.00),  -- 变量类型 -> 控制结构
(2, 3, 'required', 1.00),  -- 控制结构 -> 面向对象
(3, 4, 'required', 0.90),  -- 面向对象 -> 集合框架
(3, 5, 'recommended', 0.70), -- 面向对象 -> 异常处理
(6, 7, 'required', 1.00),  -- 线性表 -> 栈队列
(7, 8, 'required', 0.80),  -- 栈队列 -> 树
(8, 9, 'required', 0.90),  -- 树 -> 图
(6, 10, 'recommended', 0.60); -- 线性表 -> 排序算法

-- 插入学习路径模板
INSERT INTO `learning_path_template` (`name`, `description`, `subject`, `target_level`, `estimated_duration`, `difficulty_progression`) VALUES
('Java编程入门路径', '从零基础到Java初级程序员的完整学习路径', 'Java编程', 'beginner', 40, '{"start_difficulty": 1, "end_difficulty": 3, "progression": "gradual"}'),
('数据结构掌握路径', '系统学习各种数据结构的实现和应用', '数据结构', 'intermediate', 60, '{"start_difficulty": 2, "end_difficulty": 5, "progression": "steep"}'),
('算法进阶路径', '深入学习常用算法的设计与分析', '算法', 'advanced', 80, '{"start_difficulty": 3, "end_difficulty": 5, "progression": "moderate"}');

-- 插入路径模板知识点关联
INSERT INTO `path_template_knowledge` (`template_id`, `knowledge_point_id`, `sequence_order`, `is_required`, `weight`) VALUES
-- Java编程入门路径
(1, 1, 1, TRUE, 1.00),
(1, 2, 2, TRUE, 1.00),
(1, 3, 3, TRUE, 1.00),
(1, 4, 4, TRUE, 0.90),
(1, 5, 5, FALSE, 0.70),
-- 数据结构掌握路径
(2, 6, 1, TRUE, 1.00),
(2, 7, 2, TRUE, 1.00),
(2, 8, 3, TRUE, 1.00),
(2, 9, 4, TRUE, 0.90),
-- 算法进阶路径
(3, 10, 1, TRUE, 1.00),
(3, 8, 2, TRUE, 0.80),
(3, 9, 3, TRUE, 0.90);

-- 插入情绪建议配置
INSERT INTO `learning_suggestion_config` (`emotion_type`, `intensity_range`, `suggestion_type`, `suggestion_content`, `priority`) VALUES
('frustrated', 'high', 'break_suggestion', '检测到您可能感到沮丧，建议休息5-10分钟，做些深呼吸或简单运动。', 9),
('frustrated', 'medium', 'difficulty_adjustment', '学习内容可能有些困难，为您推荐一些基础资料来巩固前置知识。', 7),
('bored', 'high', 'content_recommendation', '内容可能不够有趣，为您推荐一些互动练习或实际项目案例。', 8),
('confused', 'high', 'encouragement', '遇到困难很正常！建议先回顾相关知识点，或寻求同伴帮助。', 8),
('stressed', 'high', 'music_therapy', '检测到学习压力较大，为您播放一些舒缓的背景音乐。', 6),
('focused', 'high', 'encouragement', '您的专注度很高！继续保持这种状态，学习效果会很棒。', 5),
('excited', 'high', 'encouragement', '您的学习热情很高！可以尝试一些挑战性更强的内容。', 6);

-- 为现有学生插入学习能力评估数据
INSERT INTO `learning_ability_assessment` (`student_id`, `subject`, `learning_speed`, `retention_rate`, `difficulty_preference`, `learning_style`, `concentration_span`, `optimal_study_time`, `confidence_score`) VALUES
(2, 'Java编程', 1.20, 0.85, 3, 'visual', 50, 'morning', 0.80),
(2, '数据结构', 1.00, 0.80, 4, 'visual', 45, 'morning', 0.75),
(3, 'Java编程', 0.90, 0.75, 2, 'auditory', 40, 'afternoon', 0.70),
(4, '数据结构', 1.10, 0.88, 4, 'kinesthetic', 55, 'evening', 0.85),
(5, 'Java编程', 0.80, 0.70, 2, 'visual', 35, 'night', 0.65);

-- 创建一些示例学习小组
INSERT INTO `study_group` (`group_name`, `group_description`, `subject`, `leader_id`, `max_members`, `current_members`, `group_type`, `learning_goals`) VALUES
('Java学习互助组', '专注于Java编程学习的互助小组，欢迎初学者加入', 'Java编程', 2, 6, 3, 'study', '掌握Java基础语法，能够编写简单的Java程序'),
('算法竞赛训练队', '备战ACM等算法竞赛的训练小组', '算法', 4, 8, 4, 'competition', '提高算法设计能力，在竞赛中取得好成绩'),
('数据结构项目组', '通过实际项目学习数据结构的应用', '数据结构', 5, 5, 2, 'project', '完成一个综合性的数据结构应用项目');

-- 插入小组成员
INSERT INTO `study_group_member` (`group_id`, `student_id`, `role`, `contribution_score`, `attendance_rate`) VALUES
(1, 2, 'leader', 0.95, 1.00),
(1, 3, 'member', 0.80, 0.90),
(1, 7, 'member', 0.75, 0.85),
(2, 4, 'leader', 0.90, 0.95),
(2, 6, 'member', 0.85, 0.90),
(2, 8, 'member', 0.70, 0.80),
(2, 32, 'member', 0.88, 0.92),
(3, 5, 'leader', 0.85, 0.90),
(3, 92, 'member', 0.80, 0.85);

-- 插入学习伙伴匹配示例
INSERT INTO `learning_companion_matching` (`student1_id`, `student2_id`, `matching_score`, `common_subjects`, `complementary_strengths`, `status`, `collaboration_count`, `satisfaction_rating`) VALUES
(2, 3, 0.85, 'Java编程,数据结构', '编程能力强-理论基础好', 'active', 5, 0.88),
(4, 6, 0.90, '数据结构,算法', '算法设计-代码实现', 'active', 8, 0.92),
(5, 7, 0.75, 'Java编程', '项目经验-学习能力', 'active', 3, 0.80),
(32, 92, 0.82, '软件工程,机器学习', '工程实践-理论研究', 'pending', 0, NULL); 