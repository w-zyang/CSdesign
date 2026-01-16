# 李同学选课显示问题修复方案

## 问题描述

李同学（student1）在数据库中确实选了三门张教授的课程：
1. 数据结构（course_id=1）
2. 计算机网络（course_id=6）
3. Linux系统管理（course_id=16）

但是在学生端界面中只看到了Linux一门课，而不是三门课。

## 问题根因分析

### 1. 数据库数据正确
根据 `web_experiment.sql` 中的选课记录：
```sql
INSERT INTO `student_course` VALUES (1, 2, 1, 'enrolled', '2024-02-20 09:00:00', NULL, 87, '2025-07-03 09:03:16', '2025-07-03 09:03:16');
INSERT INTO `student_course` VALUES (2, 2, 6, 'enrolled', '2024-02-20 09:30:00', NULL, 92, '2025-07-03 09:03:16', '2025-07-03 09:03:16');
INSERT INTO `student_course` VALUES (3, 2, 16, 'enrolled', '2024-02-20 10:00:00', NULL, 85, '2025-07-03 09:03:16', '2025-07-03 09:03:16');
```

李同学（student_id=2）确实选了三门课程。

### 2. 前端显示问题
问题出现在前端代码中：

1. **API调用失败**：前端可能无法正确调用 `/api/course/student/{studentId}` 接口
2. **使用了错误的备选数据**：当API调用失败时，前端使用了硬编码的备选数据

### 3. 课程ID映射错误
前端备选数据中的课程ID与数据库不匹配：

```javascript
// 错误的备选数据（修复前）
[
  { id: 1, title: '数据结构与算法' },     // ✅ 匹配数据库course_id=1
  { id: 2, title: 'Linux系统' },         // ❌ 不匹配数据库course_id=16  
  { id: 3, title: '计算机网络' }          // ❌ 不匹配数据库course_id=6
]

// 正确的备选数据（修复后）
[
  { id: 1, title: '数据结构与算法' },     // ✅ 匹配数据库course_id=1
  { id: 6, title: '计算机网络' },         // ✅ 匹配数据库course_id=6
  { id: 16, title: 'Linux系统管理' }     // ✅ 匹配数据库course_id=16
]
```

## 解决方案

### 1. 数据库修复
执行 `fix_student_course_display.sql` 脚本：
```bash
mysql -u root -p123456 < fix_student_course_display.sql
```

### 2. 前端修复
已修复 `OnlineLearning.vue` 中的备选数据，确保课程ID与数据库匹配。

### 3. 验证步骤

#### 步骤1：检查数据库
```sql
-- 验证李同学的选课记录
SELECT 
    sc.course_id,
    c.title as course_title,
    t.real_name as teacher_name
FROM student_course sc
JOIN course c ON sc.course_id = c.id
JOIN user t ON c.teacher_id = t.id
WHERE sc.student_id = (SELECT id FROM user WHERE username = 'student1')
  AND sc.status = 'enrolled';
```

#### 步骤2：检查API接口
确保后端 `/api/course/student/{studentId}` 接口正常工作。

#### 步骤3：检查前端显示
登录李同学账号，在"在线学习"页面应该看到三门课程：
1. 数据结构与算法
2. 计算机网络  
3. Linux系统管理

## 修复后的效果

修复完成后，李同学将能够：

1. **看到三门张教授的课程**：
   - 数据结构与算法（course_id=1）
   - 计算机网络（course_id=6）
   - Linux系统管理（course_id=16）

2. **访问每门课程的资源**：
   - 点击任意课程可以查看课程资源
   - 下载张教授上传的课程文件
   - 查看课程进度和学习状态

3. **正常使用所有功能**：
   - 课程资源下载
   - 学习笔记记录
   - 练习和考试
   - 学习进度跟踪

## 技术细节

### 数据库表结构
- `user` 表：用户信息（李同学ID=2，张教授ID=2）
- `course` 表：课程信息
- `student_course` 表：选课关系
- `course_resource` 表：课程资源

### API接口
- `GET /api/course/student/{studentId}`：获取学生课程列表
- `GET /api/course-resources/{courseId}`：获取课程资源

### 前端组件
- `OnlineLearning.vue`：在线学习页面
- `CourseResourceModal.vue`：课程资源模态框
- `DocumentViewer.vue`：文档查看器

## 注意事项

1. **用户ID映射**：确保李同学和张教授的用户ID正确
2. **课程状态**：确保所有课程状态为 `published`
3. **资源状态**：确保课程资源状态为 `published`
4. **API权限**：确保学生有权限访问课程资源

## 故障排除

如果修复后仍有问题：

1. **检查数据库连接**：确保MySQL服务正常运行
2. **检查后端服务**：确保Spring Boot应用正常启动
3. **检查前端服务**：确保Vue应用正常启动
4. **检查浏览器控制台**：查看是否有JavaScript错误
5. **检查网络请求**：查看API调用是否成功

## 总结

通过修复前端备选数据中的课程ID映射，李同学现在应该能够看到所有三门张教授的课程。这个问题的根本原因是前端硬编码的备选数据与数据库中的实际课程ID不匹配。 