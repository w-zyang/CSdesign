-- =============================================
-- 学生笔记和标注功能数据库表
-- =============================================

USE web_experiment;

-- 学生笔记表
CREATE TABLE `student_note` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `resource_id` BIGINT NOT NULL COMMENT '课程资源ID',
    `title` VARCHAR(100) NOT NULL COMMENT '笔记标题',
    `content` TEXT NOT NULL COMMENT '笔记内容',
    `page_number` INT DEFAULT 1 COMMENT '页码（PDF等分页文档）',
    `position` JSON COMMENT '位置信息（坐标、选择范围等）',
    `tags` VARCHAR(200) COMMENT '标签（用逗号分隔）',
    `color` VARCHAR(20) DEFAULT '#FFD700' COMMENT '笔记颜色',
    `is_public` BOOLEAN DEFAULT FALSE COMMENT '是否公开（供其他学生查看）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`resource_id`) REFERENCES `course_resource`(`id`),
    INDEX `idx_student_resource` (`student_id`, `resource_id`),
    INDEX `idx_resource_public` (`resource_id`, `is_public`)
) COMMENT '学生笔记表';

-- 学生标注表
CREATE TABLE `student_annotation` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `resource_id` BIGINT NOT NULL COMMENT '课程资源ID',
    `type` VARCHAR(20) NOT NULL DEFAULT 'highlight' COMMENT '标注类型：highlight, underline, comment, sticky-note',
    `selected_text` TEXT COMMENT '被标注的文本内容',
    `page_number` INT DEFAULT 1 COMMENT '页码',
    `position` JSON COMMENT '位置信息（起始位置、结束位置等）',
    `color` VARCHAR(20) DEFAULT '#FFFF00' COMMENT '标注颜色',
    `comment` TEXT COMMENT '备注说明',
    `importance` INT DEFAULT 3 COMMENT '重要程度（1-5）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`resource_id`) REFERENCES `course_resource`(`id`),
    INDEX `idx_student_resource_type` (`student_id`, `resource_id`, `type`),
    INDEX `idx_resource_page` (`resource_id`, `page_number`)
) COMMENT '学生标注表';

-- 清除所有测试数据，让用户从干净的状态开始
-- 如果您希望保留一些示例数据，可以取消注释以下代码：

/*
-- 基于当前文档内容的真实标注示例
INSERT INTO `student_annotation` (`student_id`, `resource_id`, `type`, `selected_text`, `page_number`, `color`, `comment`, `importance`) VALUES
(17, 1, 'highlight', 'Linux是一个开源的类Unix操作系统，广泛应用于服务器、嵌入式设备和个人计算机。', 1, '#FFFF00', '这是Linux的核心定义，重要概念', 5),
(17, 1, 'underline', 'Shell是用户与内核交互的接口，提供了命令行操作环境。', 1, '#FF6347', 'Shell概念很重要', 4),
(17, 1, 'comment', 'Linux文件系统采用树形结构，所有文件和目录都从根目录(/)开始。', 1, '#90EE90', '文件系统结构是系统管理的基础', 4),
(17, 1, 'sticky-note', '', 1, '#FFB6C1', 'Bash、Zsh、Fish都是常用的Shell，各有特点', 3);
*/

-- 创建索引以提高查询性能
CREATE INDEX idx_student_annotation_create_time ON student_annotation(create_time);
CREATE INDEX idx_student_note_create_time ON student_note(create_time); 