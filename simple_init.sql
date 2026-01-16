-- 删除并重新创建数据库
DROP DATABASE IF EXISTS web_experiment;
CREATE DATABASE web_experiment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE web_experiment;

-- 创建用户表
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100),
    `real_name` VARCHAR(50),
    `role` VARCHAR(20) NOT NULL DEFAULT 'student',
    `user_id` VARCHAR(50),
    `college` VARCHAR(100),
    `major` VARCHAR(100),
    `grade` VARCHAR(20),
    `class_name` VARCHAR(50),
    `subject` VARCHAR(100),
    `register_date` DATETIME,
    `last_login` DATETIME,
    `status` VARCHAR(20) DEFAULT 'active',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入测试用户数据
INSERT INTO `user` (`username`, `password`, `email`, `real_name`, `role`, `user_id`, `register_date`, `last_login`, `status`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', 'admin@university.edu', '管理员', 'admin', 'ADM001', '2024-01-01 08:00:00', NOW(), 'active');

INSERT INTO `user` (`username`, `password`, `email`, `real_name`, `role`, `user_id`, `college`, `subject`, `register_date`, `last_login`, `status`) VALUES
('teacher1', 'e10adc3949ba59abbe56e057f20f883e', 'teacher@university.edu', '张老师', 'teacher', 'T001', '计算机学院', 'Java编程', '2024-01-01 09:00:00', NOW(), 'active');

INSERT INTO `user` (`username`, `password`, `email`, `real_name`, `role`, `user_id`, `college`, `major`, `grade`, `class_name`, `register_date`, `last_login`, `status`) VALUES
('student1', 'e10adc3949ba59abbe56e057f20f883e', 'student@university.edu', '李同学', 'student', 'S001', '计算机学院', '计算机科学', '2024级', '计科1班', '2024-01-01 10:00:00', NOW(), 'active');

-- 验证数据插入
SELECT COUNT(*) as total_users FROM user;
SELECT role, COUNT(*) as count FROM user GROUP BY role; 