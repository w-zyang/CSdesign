# 📝 文档标注系统测试指南

## 🎯 测试目标

验证修复后的文档标注系统能够：
1. ✅ 准确保存标注位置信息
2. ✅ 正确显示标注在对应文本位置
3. ✅ 支持缩放和重新定位
4. ✅ 提供完整的标注管理功能

## 🚀 快速开始测试

### 步骤1：初始化测试数据

```bash
# 1. 清理旧的错误数据
cd Webstorm-experiment
mysql -u root -p123456 -e "USE web_experiment; DELETE FROM student_annotation WHERE resource_id IN (33, 34);"

# 2. 创建TensorFlow Lite教程测试数据
mysql -u root -p123456 < create_tensorflow_lite_test_data.sql

# 3. 验证数据创建成功
mysql -u root -p123456 < validate_annotation_system.sql
```

### 步骤2：启动前后端服务

```bash
# 启动后端服务
cd Webstorm-experiment/back
mvn spring-boot:run

# 启动前端服务（新终端）
cd Webstorm-experiment/front(2)/front(1)
npm run serve
```

### 步骤3：登录并访问文档

1. 访问 http://localhost:8080
2. 使用学生账号登录：
   - 用户名：`student17`
   - 密码：`123456`
3. 进入"在线学习" → "项目8 TensorFlow Lite教程"

## 🔍 测试场景

### 场景1：验证预期标注位置显示

**预期结果**：
- 页面中带有虚线边框的文本（📝图标）= 应该高亮标注的位置
- 带有虚线下划线的文本（📎图标）= 应该下划线标注的位置  
- 带有绿色左边框的文本（💬图标）= 应该评论标注的位置

**测试步骤**：
1. 打开文档查看器
2. 观察页面中的视觉提示
3. 确认标注目标位置清晰可见

### 场景2：创建新标注

**测试步骤**：
1. 选择工具栏中的标注工具（高亮/下划线/评论）
2. 在文档中选择文本
3. 查看选择工具提示弹出
4. 点击相应的标注类型
5. 验证标注创建成功并显示在正确位置

**预期结果**：
- ✅ 标注准确显示在选择的文本位置
- ✅ 标注颜色和样式正确
- ✅ 右侧笔记面板显示新建标注

### 场景3：缩放功能测试

**测试步骤**：
1. 使用缩放按钮放大/缩小文档
2. 观察现有标注位置是否跟随调整
3. 在不同缩放级别创建新标注

**预期结果**：
- ✅ 标注位置随缩放正确调整
- ✅ 新建标注在不同缩放级别下位置准确

### 场景4：标注管理功能

**测试步骤**：
1. 点击右侧"笔记面板"
2. 查看标注列表
3. 点击标注项跳转到文档位置
4. 编辑和删除标注

**预期结果**：
- ✅ 显示所有标注项目
- ✅ 点击跳转功能正常
- ✅ 编辑和删除功能正常

## 📊 数据验证

### 检查数据库中的标注数据

```sql
-- 查看测试标注数据
USE web_experiment;

SELECT 
    id, type, selected_text, 
    LEFT(comment, 20) AS comment_preview,
    importance,
    JSON_EXTRACT(position, '$.paragraphIndex') AS paragraph_idx
FROM student_annotation 
WHERE student_id = 17 
ORDER BY id DESC LIMIT 10;

-- 验证位置数据完整性
SELECT 
    type,
    COUNT(*) AS count,
    COUNT(CASE WHEN JSON_VALID(position) THEN 1 END) AS valid_positions
FROM student_annotation 
WHERE student_id = 17
GROUP BY type;
```

## 🐛 常见问题解决

### 问题1：标注位置不准确

**解决方案**：
1. 检查浏览器控制台是否有JavaScript错误
2. 验证`paragraphIndex`是否正确计算
3. 确认文档内容与数据库标注文本匹配

### 问题2：缩放后标注丢失

**解决方案**：
1. 检查`getAnnotationStyle`方法中的缩放计算
2. 验证`repositionAnnotation`方法是否正常工作

### 问题3：无法创建新标注

**解决方案**：
1. 检查文本选择事件是否触发
2. 验证标注数据是否正确发送到后端
3. 检查数据库连接和权限

## 📈 测试报告模板

```
测试执行时间：____
测试人员：____

功能测试结果：
□ 标注位置准确显示：通过/失败
□ 新建标注功能：通过/失败  
□ 缩放适配功能：通过/失败
□ 标注管理功能：通过/失败

性能测试：
- 页面加载时间：____秒
- 标注创建响应时间：____秒
- 大量标注显示性能：良好/一般/差

发现的问题：
1. ____
2. ____

改进建议：
1. ____
2. ____
```

## 🎉 测试成功标准

当以下所有条件满足时，标注系统测试通过：

1. ✅ **位置准确性**：新建标注显示在正确的文本位置
2. ✅ **缩放兼容性**：在不同缩放级别下标注位置正确
3. ✅ **功能完整性**：支持高亮、下划线、评论、便签四种标注类型
4. ✅ **数据持久性**：标注数据正确保存到数据库并可重新加载
5. ✅ **用户体验**：操作流畅，响应及时，界面友好

---

**💡 提示**：如果测试中发现问题，请查看浏览器控制台错误信息，并检查相应的JavaScript方法实现。 