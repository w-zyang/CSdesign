-- 课堂重点整理表
CREATE TABLE `class_summary` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `teacher_id` bigint NOT NULL COMMENT '教师ID',
  `title` varchar(200) NOT NULL COMMENT '课堂标题',
  `description` text COMMENT '课堂描述',
  `audio_file_path` varchar(500) COMMENT '录音文件路径',
  `audio_duration` int COMMENT '录音时长（秒）',
  `courseware_file_path` varchar(500) COMMENT '课件文件路径',
  `transcript_text` longtext COMMENT '转录的文本内容',
  `summary_content` longtext COMMENT 'AI生成的重点整理文档',
  `final_content` longtext COMMENT '教师编辑后的最终文档',
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，REVIEWING-审核中，PUBLISHED-已发布',
  `keywords` varchar(500) COMMENT '关键词标签',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `publish_time` datetime COMMENT '发布时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_teacher_id` (`teacher_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_publish_time` (`publish_time`),
  CONSTRAINT `fk_class_summary_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_class_summary_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课堂重点整理表';

-- 插入示例数据
INSERT INTO `class_summary` (
  `course_id`, `teacher_id`, `title`, `description`, `transcript_text`, 
  `summary_content`, `final_content`, `status`, `keywords`, `view_count`, `publish_time`
) VALUES 
(16, 2, 'Linux基础命令操作', '本次课堂讲解了Linux系统的基础命令，包括文件操作、目录管理、权限设置等核心内容。',
 '今天我们学习的主题是Linux系统的基础命令操作。首先，我们来了解一下文件系统的基本概念。在Linux中，一切皆文件，这是一个非常重要的概念。接下来我们学习几个常用的命令：ls命令用于列出目录内容，cd命令用于切换目录，mkdir命令用于创建目录，cp命令用于复制文件，mv命令用于移动或重命名文件。这些命令是Linux操作的基础，同学们需要熟练掌握。在实际使用中，还要注意权限的问题，chmod命令可以修改文件权限，chown命令可以修改文件所有者。大家在课后要多练习这些基本操作。',
 '# Linux基础命令操作课堂总结\n\n## 核心概念\n\n### 一切皆文件\n- Linux系统的基本理念\n- 文件系统的统一抽象\n\n## 基础命令详解\n\n### 1. 目录和文件操作\n- `ls` - 列出目录内容\n  - 常用选项：-l, -a, -h\n- `cd` - 切换目录\n  - 特殊目录：., .., ~, -\n- `mkdir` - 创建目录\n  - 选项：-p 创建父目录\n\n### 2. 文件操作\n- `cp` - 复制文件或目录\n  - 选项：-r 递归复制目录\n- `mv` - 移动/重命名文件\n  - 同时具备移动和重命名功能\n\n### 3. 权限管理\n- `chmod` - 修改文件权限\n  - 数字模式：755, 644等\n  - 符号模式：u+x, g-w等\n- `chown` - 修改文件所有者\n  - 格式：user:group\n\n## 学习要点\n1. 熟练掌握基础命令的语法\n2. 理解文件权限的概念\n3. 多进行实际操作练习\n4. 注意命令的安全性\n\n## 课后作业\n- 练习所有讲解的基础命令\n- 尝试组合使用不同命令\n- 理解权限系统的工作原理',
 '# Linux基础命令操作课堂总结\n\n## 核心概念\n\n### 一切皆文件\n- Linux系统的基本理念：在Linux中，一切都被抽象为文件\n- 文件系统的统一抽象：设备、目录、普通文件都遵循统一接口\n- 这个概念对理解Linux系统架构非常重要\n\n## 基础命令详解\n\n### 1. 目录和文件浏览\n**`ls` - 列出目录内容**\n- 基本用法：`ls [选项] [目录]`\n- 常用选项：\n  - `-l`：长格式显示，包含权限、所有者、大小、时间等\n  - `-a`：显示隐藏文件（以.开头的文件）\n  - `-h`：以人类可读的格式显示文件大小\n  - `-t`：按修改时间排序\n\n**`cd` - 切换目录**\n- 基本用法：`cd [目录路径]`\n- 特殊目录符号：\n  - `.`：当前目录\n  - `..`：父目录\n  - `~`：用户主目录\n  - `-`：上一次访问的目录\n\n### 2. 文件和目录操作\n**`mkdir` - 创建目录**\n- 基本用法：`mkdir [选项] 目录名`\n- 重要选项：\n  - `-p`：创建父目录（如果不存在）\n  - `-m`：设置权限模式\n\n**`cp` - 复制文件或目录**\n- 基本用法：`cp [选项] 源文件 目标文件`\n- 关键选项：\n  - `-r` 或 `-R`：递归复制目录及其内容\n  - `-i`：交互式复制，覆盖前询问\n  - `-p`：保持原文件属性\n\n**`mv` - 移动/重命名文件**\n- 基本用法：`mv [选项] 源文件 目标文件`\n- 功能：既可以移动文件，也可以重命名\n- 注意：移动到同一目录即为重命名\n\n### 3. 权限管理系统\n**`chmod` - 修改文件权限**\n- 数字模式：\n  - 4=读(r), 2=写(w), 1=执行(x)\n  - 例如：755 = rwxr-xr-x\n- 符号模式：\n  - u(用户), g(组), o(其他), a(所有)\n  - 例如：`chmod u+x file`\n\n**`chown` - 修改文件所有者**\n- 基本用法：`chown [用户]:[组] 文件`\n- 例如：`chown root:root file.txt`\n- 需要相应权限才能执行\n\n## 重要学习要点\n1. **命令语法规范**：掌握选项、参数的正确使用\n2. **权限概念**：理解用户、组、其他的权限分级\n3. **安全意识**：使用rm、chmod等命令时要谨慎\n4. **实践重要性**：理论结合实际操作\n\n## 课后练习建议\n1. 创建测试目录结构，练习所有基础命令\n2. 尝试不同的权限设置组合\n3. 理解文件权限对系统安全的影响\n4. 学习使用man命令查看详细帮助\n\n## 下节课预告\n- 文本处理命令：cat, grep, sed, awk\n- 进程管理：ps, top, kill\n- 系统信息查看：df, du, free',
 'PUBLISHED', 'Linux,命令行,文件系统,权限管理,基础操作', 15, '2024-01-15 10:30:00');

-- 创建音频文件上传目录（在应用中创建）
-- uploads/audio/course{courseId}/ 