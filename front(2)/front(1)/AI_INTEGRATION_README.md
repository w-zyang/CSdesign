# AI功能集成说明

## 概述

本项目已成功集成AI功能，包括通义千问API的本地知识库支持，为教师和学生提供智能化的教学和学习体验。

## 功能特性

### 教师侧功能

#### 1. AI备课助手 (`/teacher/ai-course-design`)
- **自动教学内容设计**：根据课程大纲和知识库自动生成教学内容
- **实训练习设计**：生成针对性的练习和指导
- **时间分布规划**：智能分配教学时间
- **考核题目生成**：自动生成多样化的考核题目
- **教学建议**：提供个性化的教学改进建议

#### 2. 学情数据分析 (`/teacher/student-analysis`)
- **学生答题分析**：自动化检测学生答案，提供错误定位
- **整体数据分析**：分析学生整体表现和知识掌握情况
- **教学建议生成**：基于分析结果提供教学改进建议

### 学生侧功能

#### 1. 在线学习助手 (`/student/learning-assistant`)
- **智能问答**：结合教学内容解答学生问题
- **学习资源推荐**：推荐相关学习资料
- **快捷问题**：提供常见问题的快速解答

#### 2. 实时练习评测助手 (`/student/practice-evaluation`)
- **智能题目生成**：根据学生历史练习情况生成随练题目
- **实时纠错**：对练习进行实时纠错和指导
- **进度跟踪**：跟踪学习进度和掌握情况

## 技术架构

### 前端架构
- **Vue 3** + **Element Plus**：现代化的前端框架和UI组件库
- **Vue Router**：单页面应用路由管理
- **Axios**：HTTP请求库，用于与后端API通信
- **响应式设计**：支持多设备访问

### API接口设计
```javascript
// AI相关API接口
export const aiAPI = {
  // 基础聊天功能
  chat(data),
  simpleChat(message),
  chatWithSystem(systemPrompt, userMessage),
  
  // 专业助手
  learningAssistant(question),
  codeAssistant(question),
  writingAssistant(request),
  
  // 教师功能
  courseDesign(data),
  generateExam(data),
  analyzeStudentData(data),
  
  // 学生功能
  practiceEvaluation(data),
  generatePractice(data)
}
```

### 本地知识库

#### 知识库结构
```
knowledge-base/
├── computer-science/          # 计算机科学
│   ├── programming/          # 编程语言
│   │   └── javascript-basics.md
│   ├── algorithms/           # 算法与数据结构
│   │   └── sorting-algorithms.md
│   ├── database/             # 数据库
│   ├── web-development/      # Web开发
│   └── software-engineering/ # 软件工程
├── mathematics/              # 数学
├── physics/                  # 物理学
├── chemistry/                # 化学
├── biology/                  # 生物学
└── general/                  # 通用知识
```

#### 知识库特点
- **Markdown格式**：易于维护和更新
- **结构化组织**：按学科和主题分类
- **中文内容**：主要面向中文教学
- **代码示例**：包含丰富的代码示例和解释

## 后端集成

### 通义千问API配置
```java
@Configuration
@ConfigurationProperties(prefix = "dashscope")
public class DashScopeConfig {
    private String apiKey;
    private String model = "qwen-plus";
    private String baseUrl;
}
```

### AI控制器
```java
@RestController
@RequestMapping("/api/ai")
public class AIController {
    // 基础聊天功能
    @PostMapping("/chat")
    @PostMapping("/simple-chat")
    @PostMapping("/chat-with-system")
    
    // 专业助手
    @PostMapping("/learning-assistant")
    @PostMapping("/code-assistant")
    @PostMapping("/writing-assistant")
    
    // 教师功能
    @PostMapping("/course-design")
    @PostMapping("/generate-exam")
    @PostMapping("/analyze-student-data")
    
    // 学生功能
    @PostMapping("/practice-evaluation")
    @PostMapping("/generate-practice")
}
```

## 使用指南

### 教师使用指南

1. **AI备课助手**
   - 访问 `/teacher/ai-course-design`
   - 填写课程基本信息（课程名称、类型、大纲等）
   - 选择需要AI生成的内容（教学内容、练习、考核等）
   - 点击"生成课程设计"按钮
   - 查看生成结果，可保存或导出

2. **学情数据分析**
   - 访问 `/teacher/student-analysis`
   - 选择要分析的课程和考试/练习
   - 设置分析维度和时间范围
   - 点击"开始分析"按钮
   - 查看分析结果和教学建议

### 学生使用指南

1. **在线学习助手**
   - 访问 `/student/learning-assistant`
   - 在聊天框中输入学习问题
   - 或点击快捷问题标签
   - 查看AI助手的回答和相关资源推荐

2. **实时练习评测**
   - 访问 `/student/practice-evaluation`
   - 设置练习参数（学科、难度、题目数量等）
   - 点击"生成练习题目"
   - 开始练习，系统会实时提供纠错和指导

## 配置说明

### 环境变量配置
```bash
# API基础URL
VITE_API_BASE_URL=http://localhost:8080

# 应用名称
VITE_APP_NAME=智能教学平台
```

### 后端配置
```yaml
# application.yml
dashscope:
  api-key: your-api-key-here
  model: qwen-plus
  base-url: https://dashscope.aliyuncs.com/api/v1
```

## 部署说明

### 前端部署
1. 安装依赖：`npm install`
2. 构建项目：`npm run build`
3. 部署到Web服务器

### 后端部署
1. 配置数据库连接
2. 设置通义千问API密钥
3. 启动Spring Boot应用

## 注意事项

1. **API密钥安全**：确保API密钥安全存储，不要提交到版本控制系统
2. **知识库更新**：定期更新本地知识库内容，保持教学内容的准确性
3. **网络连接**：确保服务器能够访问通义千问API
4. **数据隐私**：注意保护学生隐私数据，遵守相关法律法规

## 扩展功能

### 可扩展的AI功能
- **语音识别**：支持语音输入问题
- **图像识别**：支持图片题目识别
- **多语言支持**：支持多语言教学
- **个性化推荐**：基于学习历史推荐内容
- **智能评估**：更精准的学习效果评估

### 知识库扩展
- 添加更多学科内容
- 支持多媒体资源
- 建立知识图谱
- 支持知识库版本管理

## 技术支持

如有技术问题，请联系开发团队或查看项目文档。 