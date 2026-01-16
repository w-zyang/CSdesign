<template>
  <div class="learning-assistant">
    <el-card class="assistant-card">
      <template #header>
        <div class="card-header">
          <h2>AI学习助手</h2>
          <el-tag type="success">智能问答</el-tag>
        </div>
      </template>
      
      <!-- 聊天界面 -->
      <div class="chat-container">
        <div class="chat-messages" ref="messagesContainer">
          <div 
            v-for="(message, index) in messages" 
            :key="index"
            :class="['message', message.type]"
          >
            <div class="message-avatar">
              <el-avatar :icon="message.type === 'user' ? 'User' : 'ChatDotRound'" />
            </div>
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(message.content)"></div>
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
          </div>
          
          <!-- 加载状态 -->
          <div v-if="loading" class="message assistant">
            <div class="message-avatar">
              <el-avatar icon="ChatDotRound" />
            </div>
            <div class="message-content">
              <div class="loading-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 输入区域 -->
        <div class="chat-input">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入你的学习问题..."
            @keydown.enter.prevent="sendMessage"
            :disabled="loading"
          />
          <div class="input-actions">
            <el-button 
              type="primary" 
              @click="sendMessage"
              :loading="loading"
              :disabled="!inputMessage.trim()"
            >
              发送
            </el-button>
            <el-button @click="clearChat">清空对话</el-button>
          </div>
        </div>
      </div>
      
      <!-- 快捷问题 -->
      <div class="quick-questions">
        <h3>常见问题</h3>
        <div class="question-tags">
          <el-tag 
            v-for="question in quickQuestions" 
            :key="question"
            @click="askQuickQuestion(question)"
            class="question-tag"
            type="info"
            effect="plain"
          >
            {{ question }}
          </el-tag>
        </div>
      </div>
      
      <!-- 练习题目生成器 -->
      <div class="practice-generator">
        <h3>练习题目生成器</h3>
        <div class="generator-form">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-input
                v-model="practiceTopic"
                placeholder="输入主题，如：JavaScript、Vue.js、算法等"
                clearable
              />
            </el-col>
            <el-col :span="4">
              <el-select v-model="practiceDifficulty" placeholder="难度">
                <el-option label="简单" value="easy" />
                <el-option label="中等" value="medium" />
                <el-option label="困难" value="hard" />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-input-number
                v-model="practiceCount"
                :min="1"
                :max="20"
                placeholder="题目数量"
              />
            </el-col>
            <el-col :span="8">
              <el-button 
                type="primary" 
                @click="generatePracticeQuestions(practiceTopic, practiceDifficulty, practiceCount)"
                :disabled="!practiceTopic.trim()"
                :loading="loading"
              >
                生成练习题目
              </el-button>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>
    
    <!-- 学习资源推荐 -->
    <el-card class="resources-card">
      <template #header>
        <h3>相关学习资源</h3>
      </template>
      <div class="resources-list">
        <div v-for="resource in learningResources" :key="resource.id" class="resource-item">
          <el-icon><Document /></el-icon>
          <span>{{ resource.title }}</span>
          <el-button type="text" @click="viewResource(resource)">查看</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { aiAPI } from '@/api/ai'

export default {
  name: 'LearningAssistant',
  setup() {
    const messages = ref([])
    const inputMessage = ref('')
    const loading = ref(false)
    const messagesContainer = ref(null)
    
    // 练习题目生成器
    const practiceTopic = ref('')
    const practiceDifficulty = ref('medium')
    const practiceCount = ref(5)
    
    // 快捷问题
    const quickQuestions = [
      'JavaScript变量声明有哪些方式？',
      '什么是闭包？',
      'Promise和async/await的区别？',
      '数组的常用方法有哪些？',
      '如何理解事件循环？',
      'Vue的生命周期有哪些？',
      '生成TensorFlow.js基础练习题目',
      '生成JavaScript数组操作练习',
      '生成Vue.js组件练习题目'
    ]
    
    // 学习资源
    const learningResources = reactive([
      { id: 1, title: 'JavaScript基础知识', type: 'document' },
      { id: 2, title: '排序算法详解', type: 'document' },
      { id: 3, title: 'Vue.js官方文档', type: 'link' },
      { id: 4, title: 'ES6新特性', type: 'document' }
    ])
    
    // 发送消息
    const sendMessage = async () => {
      if (!inputMessage.value.trim() || loading.value) return
      
      const userMessage = inputMessage.value.trim()
      const timestamp = new Date()
      
      // 添加用户消息
      messages.value.push({
        type: 'user',
        content: userMessage,
        timestamp
      })
      
      inputMessage.value = ''
      loading.value = true
      
      try {
        // 使用知识库增强的AI服务
        const response = await aiAPI.learningAssistant(userMessage)
        
        if (response.success === true) {
          messages.value.push({
            type: 'assistant',
            content: response.data,
            timestamp: new Date()
          })
        } else {
          throw new Error(response.msg || '请求失败')
        }
      } catch (error) {
        console.error('AI请求失败:', error)
        let errorMsg = 'AI助手暂时无法回答，请稍后重试'
        
        if (error.response) {
          errorMsg = `请求失败：${error.response.data?.msg || error.response.statusText}`
        } else if (error.request) {
          errorMsg = '网络连接失败，请检查网络连接'
        } else if (error.message) {
          errorMsg = `请求失败：${error.message}`
        }
        
        ElMessage.error(errorMsg)
        
        messages.value.push({
          type: 'assistant',
          content: `抱歉，我现在无法回答您的问题。\n\n**错误信息：** ${errorMsg}\n\n**建议：**\n1. 检查网络连接\n2. 稍后重试\n3. 尝试重新提问`,
          timestamp: new Date()
        })
      } finally {
        loading.value = false
        await nextTick()
        scrollToBottom()
      }
    }
    
    // 生成练习题目
    const generatePracticeQuestions = async (topic, difficulty = 'medium', count = 5) => {
      const userMessage = `请为我生成${count}道关于${topic}的${difficulty}难度练习题目，包括选择题和编程题。`
      
      messages.value.push({
        type: 'user',
        content: userMessage,
        timestamp: new Date()
      })
      
      loading.value = true
      
      try {
        const response = await aiAPI.generatePractice({
          topic: topic,
          difficulty: difficulty,
          count: count
        })
        
        if (response.success === true) {
          let practiceContent = `## ${topic} 练习题目 (${difficulty}难度)\n\n`
          
          if (response.data.questions && response.data.questions.length > 0) {
            response.data.questions.forEach((question, index) => {
              practiceContent += `### 题目 ${index + 1}\n`
              practiceContent += `${question.title}\n\n`
              
              if (question.options && question.options.length > 0) {
                question.options.forEach(option => {
                  practiceContent += `${option}\n`
                })
                practiceContent += '\n'
              }
              
              if (question.answer) {
                practiceContent += `**参考答案：** ${question.answer}\n\n`
              }
            })
          } else {
            practiceContent += '暂无相关题目，请尝试其他主题。'
          }
          
          messages.value.push({
            type: 'assistant',
            content: practiceContent,
            timestamp: new Date()
          })
        } else {
          throw new Error(response.msg || '生成题目失败')
        }
      } catch (error) {
        console.error('生成练习题目失败:', error)
        let errorMsg = '生成练习题目失败，请稍后重试'
        
        if (error.response) {
          errorMsg = `生成失败：${error.response.data?.msg || error.response.statusText}`
        } else if (error.request) {
          errorMsg = '网络连接失败，请检查网络连接'
        } else if (error.message) {
          errorMsg = `生成失败：${error.message}`
        }
        
        ElMessage.error(errorMsg)
        
        messages.value.push({
          type: 'assistant',
          content: `抱歉，生成练习题目失败。\n\n**错误信息：** ${errorMsg}\n\n**建议：**\n1. 检查网络连接\n2. 稍后重试\n3. 尝试其他主题\n4. 减少题目数量`,
          timestamp: new Date()
        })
      } finally {
        loading.value = false
        await nextTick()
        scrollToBottom()
      }
    }
    
    // 快捷问题
    const askQuickQuestion = (question) => {
      // 检查是否是生成练习题目的请求
      if (question.includes('生成') && question.includes('练习题目')) {
        const topic = question.replace('生成', '').replace('练习题目', '').trim()
        generatePracticeQuestions(topic, 'medium', 5)
      } else {
        inputMessage.value = question
        sendMessage()
      }
    }
    
    // 清空对话
    const clearChat = () => {
      messages.value = []
      ElMessage.success('对话已清空')
    }
    
    // 格式化消息内容
    const formatMessage = (content) => {
      // 简单的代码高亮处理
      return content
        .replace(/```(\w+)?\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
        .replace(/`([^`]+)`/g, '<code>$1</code>')
        .replace(/\n/g, '<br>')
    }
    
    // 格式化时间
    const formatTime = (timestamp) => {
      return new Date(timestamp).toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    // 滚动到底部
    const scrollToBottom = () => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }
    
    // 查看资源
    const viewResource = (resource) => {
      ElMessage.info(`查看资源: ${resource.title}`)
      // 这里可以跳转到具体的资源页面
    }
    
    // 组件挂载时添加欢迎消息
    onMounted(() => {
      messages.value.push({
        type: 'assistant',
        content: '你好！我是你的AI学习助手。我可以帮助你解答各种学习问题，包括编程、算法、数学等。请随时向我提问！',
        timestamp: new Date()
      })
    })
    
    return {
      messages,
      inputMessage,
      loading,
      messagesContainer,
      quickQuestions,
      learningResources,
      sendMessage,
      askQuickQuestion,
      clearChat,
      formatMessage,
      formatTime,
      viewResource,
      generatePracticeQuestions,
      practiceTopic,
      practiceDifficulty,
      practiceCount
    }
  }
}
</script>

<style scoped>
.learning-assistant {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.assistant-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #303133;
}

.chat-container {
  height: 500px;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 15px;
}

.message {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 10px;
}

.message-content {
  max-width: 70%;
  background: white;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.message.user .message-content {
  background: #409eff;
  color: white;
}

.message-text {
  line-height: 1.5;
  margin-bottom: 5px;
}

.message-text pre {
  background: #f4f4f4;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 10px 0;
}

.message-text code {
  background: #f4f4f4;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}

.message.user .message-text pre,
.message.user .message-text code {
  background: rgba(255,255,255,0.2);
  color: white;
}

.message-time {
  font-size: 12px;
  color: #999;
  text-align: right;
}

.message.user .message-time {
  color: rgba(255,255,255,0.8);
}

.loading-dots {
  display: flex;
  gap: 4px;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  background: #409eff;
  border-radius: 50%;
  animation: loading 1.4s infinite ease-in-out;
}

.loading-dots span:nth-child(1) { animation-delay: -0.32s; }
.loading-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes loading {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.chat-input {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.input-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.quick-questions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.quick-questions h3 {
  margin-bottom: 15px;
  color: #303133;
}

.question-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.question-tag {
  cursor: pointer;
  transition: all 0.3s ease;
}

.question-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.practice-generator {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.practice-generator h3 {
  margin-bottom: 15px;
  color: #303133;
}

.generator-form {
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.resources-card {
  margin-top: 20px;
}

.resources-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.resource-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 6px;
  transition: background 0.3s;
}

.resource-item:hover {
  background: #e9ecef;
}

.resource-item .el-icon {
  color: #409eff;
}

.resource-item span {
  flex: 1;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .learning-assistant {
    padding: 10px;
  }
  
  .chat-container {
    height: 400px;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .question-tags {
    justify-content: center;
  }
}
</style>
