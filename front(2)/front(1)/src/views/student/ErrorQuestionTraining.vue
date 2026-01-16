<template>
  <div class="error-question-training">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2><i class="el-icon-warning"></i> 智能错题训练</h2>
      <p class="subtitle">基于您的历史错题，生成个性化训练内容</p>
    </div>

    <!-- 错题统计看板 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="stats-number">{{ statistics.totalErrorQuestions || 0 }}</div>
            <div class="stats-label">历史错题</div>
          </div>
          <i class="el-icon-document stats-icon error-icon"></i>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="stats-number">{{ (statistics.averageErrorRate || 0).toFixed(1) }}%</div>
            <div class="stats-label">平均错误率</div>
          </div>
          <i class="el-icon-warning stats-icon warning-icon"></i>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="stats-number">{{ Object.keys(statistics.knowledgePointDistribution || {}).length }}</div>
            <div class="stats-label">涉及知识点</div>
          </div>
          <i class="el-icon-collection stats-icon knowledge-icon"></i>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="stats-number">{{ completedTrainings }}</div>
            <div class="stats-label">完成训练</div>
          </div>
          <i class="el-icon-trophy stats-icon success-icon"></i>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主要内容区域 -->
    <el-row :gutter="20" class="main-content">
      <!-- 左侧：错题分析列表 -->
      <el-col :span="12">
        <el-card shadow="always" class="content-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-data-analysis"></i> 错题分析</span>
            <el-button type="primary" size="small" @click="refreshErrorAnalysis">
              <i class="el-icon-refresh"></i> 刷新
            </el-button>
          </div>
          
          <div v-loading="loadingAnalysis" class="error-list">
            <div v-if="errorAnalysisList.length === 0" class="no-data">
              <i class="el-icon-info"></i>
              <p>暂无错题记录</p>
            </div>
            
            <div 
              v-for="error in errorAnalysisList" 
              :key="error.questionId"
              class="error-item"
              @click="selectError(error)"
              :class="{ active: selectedError && selectedError.questionId === error.questionId }"
            >
              <div class="error-header">
                <span class="question-type" :class="error.questionType">
                  {{ getQuestionTypeText(error.questionType) }}
                </span>
                <span class="error-rate" :class="getErrorRateClass(error.errorRate)">
                  {{ error.errorRate }}%
                </span>
              </div>
              <div class="error-content">
                <h4>{{ error.questionContent }}</h4>
                <div class="error-details">
                  <span class="knowledge-point">
                    <i class="el-icon-collection-tag"></i>
                    {{ error.knowledgePoint }}
                  </span>
                  <span class="error-type">
                    <i class="el-icon-warning-outline"></i>
                    {{ error.errorType }}
                  </span>
                </div>
                <p class="error-reason">{{ error.errorReason }}</p>
              </div>
              <div class="error-actions">
                <el-button size="mini" type="primary" @click.stop="generateSimilarTraining(error)" :loading="generatingTraining">
                  <i class="el-icon-magic-stick"></i> 生成相似题目
                </el-button>
                <el-button size="mini" type="success" @click.stop="generateKnowledgeTraining(error)" :loading="generatingTraining">
                  <i class="el-icon-collection"></i> 知识点训练
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：训练生成和执行 -->
      <el-col :span="12">
        <el-card shadow="always" class="content-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-magic-stick"></i> 智能训练</span>
            <el-button type="success" size="small" @click="generateComprehensiveTraining" :loading="generatingTraining">
              <i class="el-icon-cpu"></i> 综合训练
            </el-button>
          </div>
          
          <!-- 训练配置 -->
          <div v-if="!currentTraining" class="training-config">
            <el-alert
              title="智能错题训练"
              type="info"
              description="选择左侧错题可生成针对性训练，或点击综合训练进行全面练习"
              show-icon
              :closable="false"
            ></el-alert>
            
            <div class="quick-actions">
              <h4>快速开始</h4>
              <el-row :gutter="15">
                <el-col :span="12">
                  <el-card shadow="hover" class="action-card" @click.native="generateComprehensiveTraining">
                    <div class="action-content">
                      <i class="el-icon-cpu action-icon"></i>
                      <h5>综合训练</h5>
                      <p>基于所有错题生成综合练习</p>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card shadow="hover" class="action-card" @click.native="showKnowledgePointDialog">
                    <div class="action-content">
                      <i class="el-icon-collection action-icon"></i>
                      <h5>知识点训练</h5>
                      <p>针对特定知识点强化练习</p>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </div>

          <!-- 训练进行中 -->
          <div v-if="currentTraining && !trainingCompleted" class="training-progress">
            <div class="training-header">
              <h3>{{ getTrainingTypeText(currentTraining.trainingType) }}</h3>
              <div class="training-info">
                <span>进度: {{ currentQuestionIndex + 1 }} / {{ currentTraining.questions.length }}</span>
                <span>用时: {{ formatTime(elapsedTime) }}</span>
              </div>
            </div>
            
            <el-progress 
              :percentage="Math.round(((currentQuestionIndex + 1) / currentTraining.questions.length) * 100)"
              :stroke-width="8"
              status="active"
            ></el-progress>
            
            <div class="current-question">
              <div class="question-header">
                <span class="question-number">第 {{ currentQuestionIndex + 1 }} 题</span>
                <span class="question-type">{{ getQuestionTypeText(currentQuestion.type) }}</span>
              </div>
              
              <div class="question-content">
                <h4>{{ currentQuestion.title }}</h4>
                
                <!-- 选择题 -->
                <div v-if="currentQuestion.type === 'choice'" class="options">
                  <el-radio-group v-model="userAnswers[currentQuestionIndex]">
                    <el-radio 
                      v-for="(option, index) in currentQuestion.options" 
                      :key="index"
                      :label="option.charAt(0)"
                      class="option-item"
                    >
                      {{ option }}
                    </el-radio>
                  </el-radio-group>
                </div>
                
                <!-- 填空题 -->
                <div v-else-if="currentQuestion.type === 'fill'" class="fill-input">
                  <el-input 
                    v-model="userAnswers[currentQuestionIndex]"
                    placeholder="请输入答案"
                    size="large"
                  ></el-input>
                </div>
                
                <!-- 简答题 -->
                <div v-else-if="currentQuestion.type === 'short'" class="short-input">
                  <el-input 
                    v-model="userAnswers[currentQuestionIndex]"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入答案"
                  ></el-input>
                </div>
              </div>
              
              <div class="question-actions">
                <el-button 
                  v-if="currentQuestionIndex > 0"
                  @click="previousQuestion"
                  icon="el-icon-arrow-left"
                >
                  上一题
                </el-button>
                <el-button 
                  v-if="currentQuestionIndex < currentTraining.questions.length - 1"
                  type="primary"
                  @click="nextQuestion"
                  icon="el-icon-arrow-right"
                >
                  下一题
                </el-button>
                <el-button 
                  v-if="currentQuestionIndex === currentTraining.questions.length - 1"
                  type="success"
                  @click="submitTraining"
                  icon="el-icon-check"
                >
                  提交训练
                </el-button>
              </div>
            </div>
          </div>

          <!-- 训练结果 -->
          <div v-if="trainingCompleted && trainingResult" class="training-result">
            <div class="result-header">
              <h3><i class="el-icon-circle-check"></i> 训练完成</h3>
            </div>
            
            <div class="result-stats">
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="result-stat">
                    <div class="stat-number">{{ trainingResult.accuracy }}%</div>
                    <div class="stat-label">正确率</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="result-stat">
                    <div class="stat-number">{{ trainingResult.correctCount }}</div>
                    <div class="stat-label">正确题数</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="result-stat">
                    <div class="stat-number">{{ formatTime(totalTime) }}</div>
                    <div class="stat-label">总用时</div>
                  </div>
                </el-col>
              </el-row>
            </div>
            
            <div class="improvement-suggestions">
              <h4><i class="el-icon-lightbulb"></i> 改进建议</h4>
              <ul>
                <li v-for="suggestion in trainingResult.improvementSuggestions" :key="suggestion">
                  {{ suggestion }}
                </li>
              </ul>
            </div>
            
            <div class="result-actions">
              <el-button type="primary" @click="restartTraining">
                <i class="el-icon-refresh"></i> 重新训练
              </el-button>
              <el-button type="success" @click="generateNewTraining">
                <i class="el-icon-magic-stick"></i> 生成新训练
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { 
  getErrorQuestionAnalysis, 
  getErrorQuestionStatistics,
  generateSimilarQuestions,
  generateKnowledgePointQuestions,
  generateComprehensiveTraining,
  evaluateTrainingEffect
} from '@/api/errorQuestions'

export default {
  name: 'ErrorQuestionTraining',
  data() {
    return {
      studentId: 17,
      errorAnalysisList: [],
      selectedError: null,
      loadingAnalysis: false,
      statistics: {},
      completedTrainings: 0,
      currentTraining: null,
      currentQuestionIndex: 0,
      userAnswers: [],
      trainingCompleted: false,
      trainingResult: null,
      startTime: null,
      elapsedTime: 0,
      totalTime: 0,
      timer: null,
      generatingTraining: false
    }
  },
  
  computed: {
    currentQuestion() {
      if (this.currentTraining && this.currentTraining.questions) {
        return this.currentTraining.questions[this.currentQuestionIndex] || {}
      }
      return {}
    }
  },
  
  mounted() {
    this.initializeData()
  },
  
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  
  methods: {
    async initializeData() {
      await Promise.all([
        this.loadErrorAnalysis(),
        this.loadStatistics()
      ])
    },
    
    async loadErrorAnalysis() {
      this.loadingAnalysis = true
      try {
        const response = await getErrorQuestionAnalysis(this.studentId)
        if (response.success) {
          this.errorAnalysisList = response.data || []
        }
      } catch (error) {
        this.$message.error('加载错题分析失败')
        console.error(error)
      } finally {
        this.loadingAnalysis = false
      }
    },
    
    async loadStatistics() {
      try {
        const response = await getErrorQuestionStatistics(this.studentId)
        if (response.success) {
          this.statistics = response.data || {}
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    },
    
    refreshErrorAnalysis() {
      this.loadErrorAnalysis()
    },
    
    selectError(error) {
      this.selectedError = error
    },
    
    async generateSimilarTraining(error) {
      this.generatingTraining = true
      try {
        const response = await generateSimilarQuestions({
          studentId: this.studentId,
          originalQuestionId: error.questionId,
          questionCount: 5
        })
        
        if (response.success) {
          this.startTraining(response.data)
          this.$message.success('相似题目训练生成成功')
        } else {
          this.$message.error(response.msg || '生成训练失败')
        }
      } catch (error) {
        this.$message.error('生成训练失败')
        console.error(error)
      } finally {
        this.generatingTraining = false
      }
    },
    
    async generateKnowledgeTraining(error) {
      this.generatingTraining = true
      try {
        const response = await generateKnowledgePointQuestions({
          studentId: this.studentId,
          knowledgePoint: error.knowledgePoint,
          questionCount: 5
        })
        
        if (response.success) {
          this.startTraining(response.data)
          this.$message.success('知识点训练生成成功')
        } else {
          this.$message.error(response.msg || '生成训练失败')
        }
      } catch (error) {
        this.$message.error('生成训练失败')
        console.error(error)
      } finally {
        this.generatingTraining = false
      }
    },
    
    showKnowledgePointDialog() {
      // 简化版本，直接使用默认知识点
      this.generateKnowledgeTraining({ knowledgePoint: 'Linux基础命令' })
    },
    
    async generateComprehensiveTraining() {
      this.generatingTraining = true
      try {
        const response = await generateComprehensiveTraining({
          studentId: this.studentId,
          questionCount: 10
        })
        
        if (response.success) {
          this.startTraining(response.data)
          this.$message.success('综合训练生成成功')
        } else {
          this.$message.error(response.msg || '生成训练失败')
        }
      } catch (error) {
        this.$message.error('生成训练失败')
        console.error(error)
      } finally {
        this.generatingTraining = false
      }
    },
    
    startTraining(trainingData) {
      this.currentTraining = trainingData
      this.currentQuestionIndex = 0
      this.userAnswers = new Array(trainingData.questions.length).fill('')
      this.trainingCompleted = false
      this.trainingResult = null
      this.startTime = Date.now()
      this.elapsedTime = 0
      
      this.timer = setInterval(() => {
        this.elapsedTime = Math.floor((Date.now() - this.startTime) / 1000)
      }, 1000)
    },
    
    nextQuestion() {
      if (this.currentQuestionIndex < this.currentTraining.questions.length - 1) {
        this.currentQuestionIndex++
      }
    },
    
    previousQuestion() {
      if (this.currentQuestionIndex > 0) {
        this.currentQuestionIndex--
      }
    },
    
    async submitTraining() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
      
      this.totalTime = Math.floor((Date.now() - this.startTime) / 1000)
      
      try {
        const response = await evaluateTrainingEffect({
          studentId: this.studentId,
          trainingId: this.currentTraining.id || Date.now(),
          answers: this.userAnswers
        })
        
        if (response.success) {
          this.trainingResult = response.data
          this.trainingCompleted = true
          this.completedTrainings++
          this.$message.success('训练完成！')
        } else {
          this.$message.error('提交训练失败')
        }
      } catch (error) {
        this.$message.error('提交训练失败')
        console.error(error)
      }
    },
    
    restartTraining() {
      this.currentQuestionIndex = 0
      this.userAnswers = new Array(this.currentTraining.questions.length).fill('')
      this.trainingCompleted = false
      this.trainingResult = null
      this.startTime = Date.now()
      this.elapsedTime = 0
      
      this.timer = setInterval(() => {
        this.elapsedTime = Math.floor((Date.now() - this.startTime) / 1000)
      }, 1000)
    },
    
    generateNewTraining() {
      this.currentTraining = null
      this.trainingCompleted = false
      this.trainingResult = null
    },
    
    getQuestionTypeText(type) {
      const typeMap = {
        'choice': '选择题',
        'fill': '填空题',
        'short': '简答题',
        'coding': '编程题'
      }
      return typeMap[type] || type
    },
    
    getErrorRateClass(rate) {
      if (rate >= 80) return 'high-error'
      if (rate >= 60) return 'medium-error'
      return 'low-error'
    },
    
    formatTime(seconds) {
      const mins = Math.floor(seconds / 60)
      const secs = seconds % 60
      return `${mins}:${secs.toString().padStart(2, '0')}`
    },
    
    getTrainingTypeText(type) {
      const typeMap = {
        'similar': '相似题目训练',
        'knowledge_point': '知识点训练',
        'comprehensive': '综合训练'
      }
      return typeMap[type] || type
    }
  }
}
</script>

<style scoped>
.error-question-training {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  color: #303133;
  margin-bottom: 10px;
}

.page-header .subtitle {
  color: #909399;
  font-size: 16px;
}

.stats-cards {
  margin-bottom: 30px;
}

.stats-card {
  position: relative;
  overflow: hidden;
}

.stats-content {
  padding: 20px;
}

.stats-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stats-label {
  color: #909399;
  font-size: 14px;
}

.stats-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 40px;
  opacity: 0.3;
}

.error-icon { color: #f56c6c; }
.warning-icon { color: #e6a23c; }
.knowledge-icon { color: #409eff; }
.success-icon { color: #67c23a; }

.main-content {
  margin-bottom: 30px;
}

.content-card {
  height: 700px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.error-list {
  height: 600px;
  overflow-y: auto;
}

.no-data {
  text-align: center;
  padding: 50px;
  color: #909399;
}

.error-item {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.error-item:hover,
.error-item.active {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
}

.error-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.question-type {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: white;
}

.question-type.choice { background: #409eff; }
.question-type.fill { background: #67c23a; }
.question-type.short { background: #e6a23c; }
.question-type.coding { background: #f56c6c; }

.error-rate {
  font-weight: bold;
  font-size: 14px;
}

.high-error { color: #f56c6c; }
.medium-error { color: #e6a23c; }
.low-error { color: #67c23a; }

.error-content h4 {
  margin: 10px 0;
  color: #303133;
  font-size: 14px;
  line-height: 1.5;
}

.error-details {
  display: flex;
  gap: 15px;
  margin-bottom: 8px;
}

.knowledge-point,
.error-type {
  font-size: 12px;
  color: #909399;
}

.error-reason {
  font-size: 12px;
  color: #606266;
  margin: 5px 0;
}

.error-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.training-config {
  text-align: center;
  padding: 30px;
}

.quick-actions {
  margin-top: 30px;
}

.quick-actions h4 {
  color: #303133;
  margin-bottom: 20px;
}

.action-card {
  cursor: pointer;
  transition: all 0.3s;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.action-content {
  text-align: center;
  padding: 20px;
}

.action-icon {
  font-size: 36px;
  color: #409eff;
  margin-bottom: 10px;
}

.action-content h5 {
  color: #303133;
  margin-bottom: 8px;
}

.action-content p {
  color: #909399;
  font-size: 12px;
}

.training-progress {
  padding: 20px;
}

.training-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.training-info {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #909399;
}

.current-question {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-top: 20px;
  border: 1px solid #ebeef5;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.question-number {
  font-weight: bold;
  color: #409eff;
}

.question-content h4 {
  color: #303133;
  margin-bottom: 20px;
  line-height: 1.6;
}

.options {
  margin: 20px 0;
}

.option-item {
  display: block;
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  transition: all 0.3s;
}

.option-item:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.fill-input,
.short-input {
  margin: 20px 0;
}

.question-actions {
  text-align: center;
  margin-top: 30px;
}

.question-actions .el-button {
  margin: 0 10px;
}

.training-result {
  padding: 20px;
  text-align: center;
}

.result-header h3 {
  color: #67c23a;
  margin-bottom: 20px;
}

.result-stats {
  margin: 30px 0;
}

.result-stat {
  text-align: center;
}

.result-stat .stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.result-stat .stat-label {
  color: #909399;
  font-size: 14px;
}

.improvement-suggestions {
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 8px;
  padding: 20px;
  margin: 20px 0;
  text-align: left;
}

.improvement-suggestions h4 {
  color: #409eff;
  margin-bottom: 15px;
}

.improvement-suggestions ul {
  list-style: none;
  padding: 0;
}

.improvement-suggestions li {
  padding: 5px 0;
  color: #606266;
}

.improvement-suggestions li:before {
  content: "•";
  color: #409eff;
  margin-right: 8px;
}

.result-actions {
  margin-top: 30px;
}

.result-actions .el-button {
  margin: 0 10px;
}
</style> 