<template>
  <div class="practice-evaluation">
    <!-- 课程信息展示 -->
    <el-card v-if="courseInfo.courseId" class="course-info-card">
      <template #header>
        <div class="course-header">
          <h2>{{ courseInfo.courseName }} - 练习测试</h2>
          <el-tag type="primary">{{ courseInfo.teacherName }}</el-tag>
        </div>
      </template>
      <div class="course-description">
        <p>欢迎来到 {{ courseInfo.courseName }} 的练习测试模块！</p>
        <p>这里有老师为您精心准备的练习题目，请选择您要进行的测试。</p>
      </div>
    </el-card>

    <!-- 教师发布的考试列表 -->
    <el-card v-if="teacherExams.length > 0" class="exams-card">
      <template #header>
        <div class="card-header">
          <h3>教师发布的练习测试</h3>
          <el-tag type="success">{{ teacherExams.length }} 个可用测试</el-tag>
        </div>
      </template>
      
      <div class="exams-grid">
        <div 
          v-for="exam in teacherExams" 
          :key="exam.id"
          class="exam-card"
          @click="selectExam(exam)"
        >
          <div class="exam-header">
            <h4>{{ exam.name }}</h4>
            <div class="exam-tags">
              <el-tag :type="getExamTypeTag(exam.type)">{{ getExamTypeName(exam.type) }}</el-tag>
              <el-tag type="info" size="small">教师出题</el-tag>
            </div>
          </div>
          <div class="exam-info">
            <div class="info-item">
              <i class="el-icon-time"></i>
              <span>{{ exam.duration }}分钟</span>
            </div>
            <div class="info-item">
              <i class="el-icon-trophy"></i>
              <span>{{ exam.totalScore }}分</span>
            </div>
            <div class="info-item">
              <i class="el-icon-document"></i>
              <span>{{ exam.questions ? exam.questions.length : '未知' }}题</span>
            </div>
          </div>
          <div class="exam-description">
            <p>{{ exam.description || '暂无描述' }}</p>
          </div>
          <div class="exam-actions">
            <el-button type="primary" @click.stop="startExam(exam)">
              开始测试
            </el-button>
            <el-button @click.stop="previewExam(exam)">
              预览题目
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- AI智能练习（备选方案） -->
    <el-card class="practice-card">
      <template #header>
        <div class="card-header">
          <h3>AI练习评测助手</h3>
          <el-tag type="warning">智能练习</el-tag>
        </div>
      </template>
      
      <!-- 练习设置 -->
      <div class="practice-settings">
        <h3>练习设置</h3>
        <el-form :model="practiceForm" label-width="120px" class="practice-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="学科类型">
                <el-select v-model="practiceForm.subject" placeholder="请选择学科">
                  <el-option label="JavaScript编程" value="javascript" />
                  <el-option label="算法与数据结构" value="algorithms" />
                  <el-option label="Vue.js开发" value="vue" />
                  <el-option label="数据库设计" value="database" />
                  <el-option label="数学计算" value="mathematics" />
                  <el-option label="TensorFlow.js开发" value="tensorflow-js" />
                  <el-option label="机器学习基础" value="machine-learning" />
                  <el-option label="深度学习应用" value="deep-learning" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="难度等级">
                <el-select v-model="practiceForm.difficulty" placeholder="请选择难度">
                  <el-option label="初级" value="beginner" />
                  <el-option label="中级" value="intermediate" />
                  <el-option label="高级" value="advanced" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <!-- 新增：题目类型选择 -->
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="题目类型">
                <el-checkbox-group v-model="practiceForm.questionTypes">
                  <el-checkbox label="choice">选择题</el-checkbox>
                  <el-checkbox label="fill">填空题</el-checkbox>
                  <el-checkbox label="short">简答题</el-checkbox>
                  <el-checkbox label="coding">编程题</el-checkbox>
                  <el-checkbox label="essay">论述题</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-col>
          </el-row>
          
          <!-- 新增：各类型题目数量设置 -->
          <el-row :gutter="20" v-if="practiceForm.questionTypes.length > 0">
            <el-col :span="24">
              <el-form-item label="题目数量分配">
                <div class="question-type-config">
                  <div v-for="type in practiceForm.questionTypes" :key="type" class="type-config-item">
                    <span class="type-name">{{ getQuestionTypeName(type) }}</span>
                    <el-input-number 
                      v-model="practiceForm.typeConfig[type]" 
                      :min="1" 
                      :max="10"
                      size="small"
                    />
                  </div>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="总题目数量">
                <el-input-number v-model="practiceForm.questionCount" :min="1" :max="50" disabled />
                <div class="question-count-tips">
                  <el-tag size="small" type="success">自动计算：{{ totalQuestionCount }}道题</el-tag>
                  <el-tag size="small" type="info">根据上方题型数量自动更新</el-tag>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="练习时长">
                <el-input-number v-model="practiceForm.timeLimit" :min="5" :max="120" />
                <span class="unit">分钟</span>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="知识点">
            <el-input
              v-model="practiceForm.topics"
              placeholder="请输入想要练习的知识点，用逗号分隔..."
            />
          </el-form-item>
          
          <el-form-item label="特殊要求">
            <el-input
              v-model="practiceForm.requirements"
              type="textarea"
              :rows="3"
              placeholder="请输入特殊练习要求..."
            />
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 生成练习按钮 -->
      <div class="generate-actions">
        <el-button 
          type="primary" 
          size="large"
          @click="generatePractice"
          :loading="generating"
          :disabled="!canGenerate"
        >
          <el-icon v-if="!generating"><Edit /></el-icon>
          <el-icon v-else><Loading /></el-icon>
          {{ generating ? 'AI正在生成题目，请耐心等待...' : '生成练习题目' }}
        </el-button>
        <el-button @click="resetForm">重置设置</el-button>
        <el-button type="info" @click="testBackend">测试后端连接</el-button>
        <el-button type="warning" @click="healthCheck">健康检查</el-button>
        <el-button 
          v-if="practiceQuestions.length > 0" 
          type="danger" 
          @click="confirmClearPractice"
        >
          清除当前练习
        </el-button>
      </div>
    </el-card>
    
    <!-- 练习题目展示 -->
    <div v-if="practiceQuestions.length > 0" class="questions-section" id="questions-section">
      <el-card class="questions-card">
        <template #header>
          <div class="questions-header">
            <div class="header-title">
              <h3>📝 练习题目</h3>
              <el-tag v-if="selectedExam" type="primary" size="small">
                来自：{{ selectedExam.name }}
              </el-tag>
              <el-tag type="success" size="small" effect="plain">
                <i class="el-icon-circle-check"></i> 已自动保存
              </el-tag>
            </div>
            <div class="practice-info">
              <span>共 {{ practiceQuestions.length }} 题</span>
              <span>预计用时: {{ practiceForm.timeLimit }} 分钟</span>
              <el-button type="success" @click="startPractice" v-if="!practiceStarted">
                开始练习
              </el-button>
            </div>
          </div>
        </template>
        
        <div v-if="!practiceStarted" class="questions-preview">
          <div v-for="(question, index) in practiceQuestions" :key="index" class="question-preview">
            <div class="question-header">
              <span class="question-number">第 {{ index + 1 }} 题</span>
              <span class="question-type">{{ question.type }}</span>
              <span class="question-difficulty">{{ question.difficulty }}</span>
            </div>
            <div class="question-content">
              <h4>{{ question.title }}</h4>
              <div v-if="question.options" class="question-options">
                <div v-for="(option, optIndex) in question.options" :key="optIndex" class="option">
                  {{ String.fromCharCode(65 + optIndex) }}. {{ option }}
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 练习进行中 -->
        <div v-else class="practice-session">
          <div class="practice-timer">
            <el-progress 
              :percentage="timerProgress" 
              :format="timerFormat"
              status="warning"
            />
            <div class="timer-text">
              剩余时间: {{ formatTime(remainingTime) }}
            </div>
          </div>
          
          <div class="current-question">
            <div class="question-navigation">
              <el-button @click="previousQuestion" :disabled="currentQuestionIndex === 0">
                上一题
              </el-button>
              <span class="question-counter">
                {{ currentQuestionIndex + 1 }} / {{ practiceQuestions.length }}
              </span>
              <el-button @click="nextQuestion" :disabled="currentQuestionIndex === practiceQuestions.length - 1">
                下一题
              </el-button>
            </div>
            
            <div class="question-display">
              <div class="question-header">
                <span class="question-type">{{ getQuestionTypeName(currentQuestion.type) }}</span>
                <span class="question-difficulty">{{ currentQuestion.difficulty }}</span>
              </div>
              <div class="question-content">
                <h4>{{ currentQuestion.title }}</h4>
                
                <!-- 选择题 -->
                <div v-if="currentQuestion.type === 'choice' && currentQuestion.options" class="question-options">
                  <el-radio-group v-model="userAnswers[currentQuestionIndex]">
                    <el-radio 
                      v-for="(option, optIndex) in currentQuestion.options" 
                      :key="optIndex"
                      :label="String.fromCharCode(65 + optIndex)"
                      class="option-radio"
                    >
                      {{ String.fromCharCode(65 + optIndex) }}. {{ option }}
                    </el-radio>
                  </el-radio-group>
                </div>
                
                <!-- 填空题 -->
                <div v-else-if="currentQuestion.type === 'fill'" class="question-fill">
                  <div class="fill-instruction">请在空白处填入合适的答案：</div>
                  <div v-if="currentQuestion.blanks" class="fill-blanks">
                    <div v-for="(blank, blankIndex) in currentQuestion.blanks" :key="blankIndex" class="blank-item">
                      <span class="blank-label">空白{{ blankIndex + 1 }}：</span>
                      <el-input 
                        v-model="userAnswers[currentQuestionIndex][blankIndex]" 
                        placeholder="请输入答案"
                        size="large"
                        class="blank-input"
                      />
                    </div>
                  </div>
                  <div v-else class="single-fill">
                    <el-input
                      v-model="userAnswers[currentQuestionIndex]"
                      placeholder="请输入答案"
                      size="large"
                    />
                  </div>
                </div>
                
                <!-- 简答题 -->
                <div v-else-if="currentQuestion.type === 'short'" class="question-short">
                  <div class="short-instruction">请详细回答以下问题：</div>
                  <el-input
                    v-model="userAnswers[currentQuestionIndex]"
                    type="textarea"
                    :rows="8"
                    placeholder="请详细阐述你的观点和理由..."
                    show-word-limit
                    :maxlength="2000"
                  />
                  <div class="short-tips">
                    <el-alert title="答题提示" type="info" show-icon :closable="false">
                      <ul>
                        <li>请条理清晰地阐述你的观点</li>
                        <li>可以结合具体例子说明</li>
                        <li>注意逻辑性和完整性</li>
                      </ul>
                    </el-alert>
                  </div>
                </div>
                
                <!-- 编程题 -->
                <div v-else-if="currentQuestion.type === 'coding'" class="question-coding">
                  <div class="coding-instruction">
                    <div class="instruction-content">
                      <h5>编程要求：</h5>
                      <div v-if="currentQuestion.requirements" v-html="currentQuestion.requirements"></div>
                      <div v-if="currentQuestion.examples" class="examples">
                        <h6>示例：</h6>
                        <div v-for="(example, exIndex) in currentQuestion.examples" :key="exIndex" class="example-item">
                          <div class="example-input">输入：{{ example.input }}</div>
                          <div class="example-output">输出：{{ example.output }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="coding-editor">
                    <el-input
                      v-model="userAnswers[currentQuestionIndex]"
                      type="textarea"
                      :rows="15"
                      placeholder="请在此处编写代码..."
                      class="code-textarea"
                    />
                  </div>
                  <div class="coding-actions">
                    <el-button type="primary" @click="runCode(currentQuestionIndex)">
                      <el-icon><CaretRight /></el-icon>
                      运行代码
                    </el-button>
                    <el-button @click="resetCode(currentQuestionIndex)">重置代码</el-button>
                  </div>
                  <div v-if="codeResults[currentQuestionIndex]" class="code-result">
                    <h6>运行结果：</h6>
                    <pre>{{ codeResults[currentQuestionIndex] }}</pre>
                  </div>
                </div>
                
                <!-- 论述题 -->
                <div v-else-if="currentQuestion.type === 'essay'" class="question-essay">
                  <div class="essay-instruction">请深入分析并论述以下问题：</div>
                  <el-input
                    v-model="userAnswers[currentQuestionIndex]"
                    type="textarea"
                    :rows="12"
                    placeholder="请结合理论知识和实际情况，深入分析问题..."
                    show-word-limit
                    :maxlength="5000"
                  />
                  <div class="essay-tips">
                    <el-alert title="论述要求" type="warning" show-icon :closable="false">
                      <ul>
                        <li>观点明确，论证充分</li>
                        <li>结合理论知识和实践案例</li>
                        <li>逻辑清晰，层次分明</li>
                        <li>语言表达准确，文字流畅</li>
                      </ul>
                    </el-alert>
                  </div>
                </div>
                
                <!-- 默认文本输入 -->
                <div v-else class="question-textarea">
                  <el-input
                    v-model="userAnswers[currentQuestionIndex]"
                    type="textarea"
                    :rows="6"
                    placeholder="请输入你的答案..."
                  />
                </div>
              </div>
            </div>
            
            <div class="question-actions">
              <el-button @click="submitPractice" type="primary">
                提交练习
              </el-button>
              <el-button @click="pausePractice">暂停</el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 练习结果 -->
    <div v-if="practiceResult" class="result-section">
      <el-card class="result-card">
        <template #header>
          <h3>练习结果</h3>
        </template>
        
        <div class="result-summary">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="result-stat">
                <div class="stat-number">{{ practiceResult.score }}</div>
                <div class="stat-label">总分</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="result-stat">
                <div class="stat-number">{{ practiceResult.correctCount }}</div>
                <div class="stat-label">正确题数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="result-stat">
                <div class="stat-number">{{ practiceResult.accuracy }}%</div>
                <div class="stat-label">正确率</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="result-stat">
                <div class="stat-number">{{ practiceResult.timeUsed }}</div>
                <div class="stat-label">用时(分钟)</div>
              </div>
            </el-col>
          </el-row>
        </div>
        
        <!-- 添加明显的提示信息 -->
        <el-alert 
          title="💡 获取详细分析报告" 
          type="info" 
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <template #default>
            <div style="line-height: 1.8;">
              <p style="margin: 0 0 10px 0; font-size: 14px;">
                <strong>当前显示的是基础解析。</strong>如需获得更详细的AI分析报告，请点击每道题目右侧的 
                <el-tag type="primary" size="small" style="margin: 0 5px;">生成分析报告</el-tag> 按钮。
              </p>
              <p style="margin: 0; font-size: 13px; color: #606266;">
                📊 详细分析报告包含：知识点梳理、解题思路、错误原因分析、个性化学习建议等内容
              </p>
            </div>
          </template>
        </el-alert>
        
        <div class="result-details">
          <h4>详细分析</h4>
          <div v-for="(analysis, index) in practiceResult.analysis" :key="index" class="analysis-item">
            <div class="analysis-header">
              <h5>第{{ index + 1 }}题：{{ analysis.title }}</h5>
              <div class="analysis-status">
                <el-tag :type="analysis.isCorrect ? 'success' : 'danger'" size="small">
                  {{ analysis.isCorrect ? '正确' : '错误' }}
                </el-tag>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="generateReport(index)"
                  :loading="analysis.isTriggering"
                  style="margin-left: 10px;"
                  :icon="analysis.isTriggering ? 'Loading' : 'MagicStick'"
                >
                  {{ analysis.isTriggering ? 'AI分析中...' : '生成分析报告' }}
                </el-button>
              </div>
            </div>
            <div class="analysis-content">
              <p><strong>你的答案：</strong>{{ analysis.userAnswer || '未作答' }}</p>
              <p><strong>正确答案：</strong>{{ analysis.correctAnswer }}</p>
              <p v-if="analysis.explanation"><strong>基础解析：</strong>{{ analysis.explanation }}</p>
              
              <!-- 如果还没有生成详细分析，显示提示 -->
              <div v-if="!analysis.detailedAnalysis || analysis.detailedAnalysis.length < 100" class="need-ai-tip">
                <el-tag type="warning" size="small" effect="plain">
                  <i class="el-icon-info"></i> 点击上方"生成分析报告"按钮，获取AI提供的详细解析、知识点梳理和学习建议
                </el-tag>
              </div>
              
              <!-- 如果已经生成了详细分析，显示成功提示 -->
              <div v-else class="ai-generated-tip">
                <el-tag type="success" size="small" effect="plain">
                  <i class="el-icon-circle-check"></i> 已生成AI详细分析，点击上方按钮可再次查看
                </el-tag>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 弹窗显示AI分析报告 -->
        <el-dialog 
          v-model="reportDialogVisible" 
          title="AI分析报告" 
          width="70%"
          :before-close="closeReportDialog"
        >
          <div v-if="currentReport" class="detail-content">
            <div class="detail-header">
              <h4>{{ currentReport.title }}</h4>
            </div>
            <div class="detail-analysis">
              <h5>详细分析：</h5>
              <div class="analysis-text" v-html="formatAnalysis(currentReport.detailedAnalysis)"></div>
            </div>
            <div class="detail-suggestion">
              <h5>学习建议：</h5>
              <div class="suggestion-text">{{ currentReport.suggestion }}</div>
            </div>
          </div>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="closeReportDialog">关闭</el-button>
            </span>
          </template>
        </el-dialog>
        
        <div class="result-actions">
          <el-button type="primary" @click="retryPractice">重新练习</el-button>
          <el-button @click="viewAnswers">查看答案</el-button>
          <el-button @click="exportResult">导出结果</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onUnmounted, nextTick, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Loading, CaretRight } from '@element-plus/icons-vue'
import { aiAPI } from '@/api/ai'
import { getPublishedExamsByCourseId, getExamById } from '@/api/exam'
import { submitPractice as submitPracticeAPI, startPractice as startPracticeAPI } from '@/api/practice'
import axios from 'axios'

export default {
  name: 'PracticeEvaluation',
  components: {
    Edit,
    Loading,
    CaretRight
  },
  setup() {
    // 获取路由信息
    const route = useRoute()
    
    // 课程信息
    const courseInfo = reactive({
      courseId: route.query.courseId || null,
      courseName: route.query.courseName || '',
      teacherName: route.query.teacherName || ''
    })
    
    // 教师发布的考试列表
    const teacherExams = ref([])
    const loadingExams = ref(false)
    const selectedExam = ref(null)
    
    const practiceForm = reactive({
      subject: '',
      difficulty: 'intermediate',
      questionTypes: ['choice'], // 默认选择题
      typeConfig: { choice: 2, fill: 1, short: 1, coding: 1, essay: 1 }, // 默认配置
      questionCount: 5,
      timeLimit: 30,
      topics: '',
      requirements: ''
    })
    
    const generating = ref(false)
    const practiceQuestions = ref([])
    const practiceStarted = ref(false)
    const currentQuestionIndex = ref(0)
    const userAnswers = ref([])
    const practiceResult = ref(null)
    const remainingTime = ref(0)
    const timerInterval = ref(null)
    const codeResults = ref({})
    const reportDialogVisible = ref(false)
    const currentReport = ref(null)
    const currentPracticeId = ref(null) // 保存当前练习的ID
    
    // LocalStorage 键名
    const STORAGE_KEYS = {
      PRACTICE_QUESTIONS: 'practice_questions',
      PRACTICE_FORM: 'practice_form',
      PRACTICE_STARTED: 'practice_started',
      CURRENT_QUESTION_INDEX: 'current_question_index',
      USER_ANSWERS: 'user_answers',
      REMAINING_TIME: 'remaining_time',
      PRACTICE_RESULT: 'practice_result',
      SELECTED_EXAM: 'selected_exam',
      CURRENT_PRACTICE_ID: 'current_practice_id',
      START_TIME: 'practice_start_time'
    }
    
    // 计算是否可以生成
    const canGenerate = computed(() => {
      return practiceForm.subject && practiceForm.questionCount > 0
    })
    
    // 计算总题目数量（自动等于各题型数量之和）
    const totalQuestionCount = computed(() => {
      let total = 0
      for (const type of practiceForm.questionTypes) {
        total += practiceForm.typeConfig[type] || 0
      }
      return total
    })
    
    // 监听题型配置变化，自动更新总题目数量
    watch(() => practiceForm.typeConfig, () => {
      practiceForm.questionCount = totalQuestionCount.value
    }, { deep: true })
    
    // 监听题型选择变化，自动更新总题目数量
    watch(() => practiceForm.questionTypes, () => {
      practiceForm.questionCount = totalQuestionCount.value
    }, { deep: true })
    
    // 保存练习数据到 localStorage
    const savePracticeToStorage = () => {
      try {
        localStorage.setItem(STORAGE_KEYS.PRACTICE_QUESTIONS, JSON.stringify(practiceQuestions.value))
        localStorage.setItem(STORAGE_KEYS.PRACTICE_FORM, JSON.stringify(practiceForm))
        localStorage.setItem(STORAGE_KEYS.PRACTICE_STARTED, JSON.stringify(practiceStarted.value))
        localStorage.setItem(STORAGE_KEYS.CURRENT_QUESTION_INDEX, JSON.stringify(currentQuestionIndex.value))
        localStorage.setItem(STORAGE_KEYS.USER_ANSWERS, JSON.stringify(userAnswers.value))
        localStorage.setItem(STORAGE_KEYS.REMAINING_TIME, JSON.stringify(remainingTime.value))
        localStorage.setItem(STORAGE_KEYS.PRACTICE_RESULT, JSON.stringify(practiceResult.value))
        localStorage.setItem(STORAGE_KEYS.SELECTED_EXAM, JSON.stringify(selectedExam.value))
        localStorage.setItem(STORAGE_KEYS.CURRENT_PRACTICE_ID, JSON.stringify(currentPracticeId.value))
        console.log('✅ 练习数据已保存到本地存储')
      } catch (error) {
        console.error('❌ 保存练习数据失败:', error)
      }
    }
    
    // 从 localStorage 恢复练习数据
    const loadPracticeFromStorage = () => {
      try {
        const savedQuestions = localStorage.getItem(STORAGE_KEYS.PRACTICE_QUESTIONS)
        const savedForm = localStorage.getItem(STORAGE_KEYS.PRACTICE_FORM)
        const savedStarted = localStorage.getItem(STORAGE_KEYS.PRACTICE_STARTED)
        const savedIndex = localStorage.getItem(STORAGE_KEYS.CURRENT_QUESTION_INDEX)
        const savedAnswers = localStorage.getItem(STORAGE_KEYS.USER_ANSWERS)
        const savedTime = localStorage.getItem(STORAGE_KEYS.REMAINING_TIME)
        const savedResult = localStorage.getItem(STORAGE_KEYS.PRACTICE_RESULT)
        const savedExam = localStorage.getItem(STORAGE_KEYS.SELECTED_EXAM)
        const savedPracticeId = localStorage.getItem(STORAGE_KEYS.CURRENT_PRACTICE_ID)
        
        if (savedQuestions) {
          practiceQuestions.value = JSON.parse(savedQuestions)
          console.log('✅ 恢复练习题目:', practiceQuestions.value.length, '道题')
        }
        
        if (savedForm) {
          const formData = JSON.parse(savedForm)
          Object.assign(practiceForm, formData)
          console.log('✅ 恢复练习配置')
        }
        
        if (savedStarted) {
          practiceStarted.value = JSON.parse(savedStarted)
          console.log('✅ 恢复练习状态:', practiceStarted.value ? '进行中' : '未开始')
        }
        
        if (savedIndex) {
          currentQuestionIndex.value = JSON.parse(savedIndex)
          console.log('✅ 恢复当前题目索引:', currentQuestionIndex.value)
        }
        
        if (savedAnswers) {
          userAnswers.value = JSON.parse(savedAnswers)
          console.log('✅ 恢复用户答案')
        }
        
        if (savedTime) {
          remainingTime.value = JSON.parse(savedTime)
          console.log('✅ 恢复剩余时间:', remainingTime.value, '秒')
        }
        
        if (savedResult) {
          practiceResult.value = JSON.parse(savedResult)
          console.log('✅ 恢复练习结果')
        }
        
        if (savedExam) {
          selectedExam.value = JSON.parse(savedExam)
          console.log('✅ 恢复选中的考试')
        }
        
        if (savedPracticeId) {
          currentPracticeId.value = JSON.parse(savedPracticeId)
          console.log('✅ 恢复练习ID:', currentPracticeId.value)
        }
        
        // 如果练习正在进行中，重新启动计时器
        if (practiceStarted.value && !practiceResult.value && remainingTime.value > 0) {
          startTimer()
          ElMessage.success('已恢复您的练习进度，请继续答题！')
        }
        
        return true
      } catch (error) {
        console.error('❌ 恢复练习数据失败:', error)
        return false
      }
    }
    
    // 清除 localStorage 中的练习数据
    const clearPracticeStorage = () => {
      try {
        Object.values(STORAGE_KEYS).forEach(key => {
          localStorage.removeItem(key)
        })
        console.log('✅ 已清除本地存储的练习数据')
      } catch (error) {
        console.error('❌ 清除练习数据失败:', error)
      }
    }
    
    // 监听练习数据变化，自动保存
    watch([practiceQuestions, practiceStarted, currentQuestionIndex, userAnswers, remainingTime, practiceResult], () => {
      if (practiceQuestions.value.length > 0) {
        savePracticeToStorage()
      }
    }, { deep: true })
    
    // 当前题目
    const currentQuestion = computed(() => {
      return practiceQuestions.value[currentQuestionIndex.value] || {}
    })
    
    // 计时器进度
    const timerProgress = computed(() => {
      const totalTime = practiceForm.timeLimit * 60
      return ((totalTime - remainingTime.value) / totalTime) * 100
    })
    
    // 生成练习题目（使用新的智能出题接口，支持混合题型）
    const generatePractice = async () => {
      if (!canGenerate.value) {
        ElMessage.warning('请完善练习设置')
        return
      }
      
      generating.value = true
      
      // 显示详细的加载提示
      ElMessage({
        message: 'AI正在基于知识库生成练习题目，预计需要30-60秒，请耐心等待...',
        type: 'info',
        duration: 0,
        showClose: true
      })
      
      try {
        const difficulty = practiceForm.difficulty === 'beginner' ? 'easy' : 
                          practiceForm.difficulty === 'intermediate' ? 'medium' : 'hard'
        const knowledgePoint = practiceForm.topics || practiceForm.subject
        
        // 收集所有生成的题目
        const allQuestions = []
        
        // 根据选中的题型分别生成
        for (const questionType of practiceForm.questionTypes) {
          const count = practiceForm.typeConfig[questionType] || 1
          
          // 映射前端题型到后端题型
          const typeMap = {
            'choice': 'single_choice',
            'fill': 'fill',
            'short': 'short_answer',
            'coding': 'coding',
            'essay': 'essay'
          }
          
          const backendType = typeMap[questionType] || 'single_choice'
          
          const requestData = {
            subject: practiceForm.subject,
            knowledgePoint: knowledgePoint,
            type: backendType,
            difficulty: difficulty,
            count: count
          }
          
          console.log(`📚 生成${count}道${questionType}题目:`, requestData)
          
          try {
            const response = await axios.post('/api/question-bank/generate', requestData)
            console.log(`✅ ${questionType}题目生成成功:`, response)
            
            if (response.data && response.data.success && response.data.data) {
              const questions = response.data.data
              const convertedQuestions = questions.map(q => ({
                id: q.id, // 保留题目ID
                title: q.content,
                type: questionType, // 使用前端的题型名称
                content: q.content,
                options: q.options ? q.options.map(opt => opt.content) : null,
                answer: q.answer,
                explanation: q.analysis || '',
                difficulty: q.difficulty || 'medium',
                score: q.score || 10
              }))
              
              allQuestions.push(...convertedQuestions)
            }
          } catch (error) {
            console.error(`生成${questionType}题目失败:`, error)
            ElMessage.warning(`${getQuestionTypeName(questionType)}生成失败，已跳过`)
          }
        }
        
        if (allQuestions.length > 0) {
          practiceQuestions.value = allQuestions
          
          // 根据题目类型初始化答案数组
          userAnswers.value = practiceQuestions.value.map(q => {
            if (q.type === 'fill' && q.blanks && Array.isArray(q.blanks)) {
              return Array(q.blanks.length).fill('')
            }
            return ''
          })
          
          // 关闭加载提示并显示成功消息
          ElMessage.closeAll()
          ElMessage.success(`✅ 基于知识库生成题目成功！共${practiceQuestions.value.length}道题目`)
          
          // 保存到本地存储
          savePracticeToStorage()
        } else {
          throw new Error('所有题型生成都失败了')
        }
      } catch (error) {
        console.error('生成练习题目失败:', error)
        
        // 关闭加载提示
        ElMessage.closeAll()
        
        let errorMsg = '生成失败，请稍后重试'
        
        // 针对不同错误类型提供更友好的提示
        if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
          errorMsg = 'AI生成超时，这通常是因为题目生成需要较长时间。请稍后重试，或尝试减少题目数量。'
        } else if (error.response) {
          const status = error.response.status
          const data = error.response.data
          
          if (status === 429) {
            errorMsg = 'AI服务繁忙，请稍后重试'
          } else if (status >= 500) {
            errorMsg = 'AI服务暂时不可用，请稍后重试'
          } else {
            errorMsg = `生成失败：${data?.message || data?.msg || error.response.statusText}`
          }
        } else if (error.request) {
          errorMsg = 'AI服务连接失败，请检查网络连接或稍后重试'
        } else if (error.message) {
          errorMsg = `生成失败：${error.message}`
        }
        
        ElMessage.error({
          message: errorMsg,
          duration: 8000, // 延长显示时间
          showClose: true
        })
      } finally {
        generating.value = false
      }
    }
    
    // 重置表单
    const resetForm = () => {
      Object.assign(practiceForm, {
        subject: '',
        difficulty: 'intermediate',
        questionTypes: ['choice'],
        typeConfig: { choice: 2, fill: 1, short: 1, coding: 1, essay: 1 },
        questionCount: 5,
        timeLimit: 30,
        topics: '',
        requirements: ''
      })
      practiceQuestions.value = []
      practiceStarted.value = false
      practiceResult.value = null
      codeResults.value = {}
      
      // 清除本地存储
      clearPracticeStorage()
      
      ElMessage.success('设置已重置')
    }
    
    // 开始练习
    const startPractice = async () => {
      try {
        // 获取当前登录学生ID
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        const studentId = userInfo.id || userInfo.userId || 17 // 使用默认ID 17 作为后备
        
        console.log('用户信息:', userInfo)
        console.log('学生ID:', studentId)
        
        if (!studentId) {
          ElMessage.warning('未检测到登录信息，使用默认学生ID')
        }
        
        // 生成practiceId
        const practiceId = selectedExam.value ? selectedExam.value.id : Date.now()
        console.log('📝 开始练习 - practiceId:', practiceId, 'studentId:', studentId)
        
        // 直接调用开始练习API（后端会自动处理）
        const startResponse = await startPracticeAPI({ practiceId, studentId })
        console.log('✅ 开始练习API响应:', startResponse)
        
        if (!startResponse || !startResponse.success) {
          throw new Error(startResponse?.msg || '开始练习失败')
        }
        
        // 保存后端返回的practiceId（可能是数据库自动生成的）
        currentPracticeId.value = startResponse.data?.practiceId || practiceId
        console.log('💾 保存practiceId:', currentPracticeId.value)
        
        ElMessage.success('练习已开始，开始答题吧！')
        
        practiceStarted.value = true
        currentQuestionIndex.value = 0
        remainingTime.value = practiceForm.timeLimit * 60
        startTimer()
      } catch (error) {
        console.error('❌ 开始练习失败:', error)
        ElMessage.error('开始练习失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
        // 不继续执行，让用户重新尝试
      }
    }
    
    // 开始计时器
    const startTimer = () => {
      timerInterval.value = setInterval(() => {
        if (remainingTime.value > 0) {
          remainingTime.value--
        } else {
          clearInterval(timerInterval.value)
          submitPractice()
        }
      }, 1000)
    }
    
    // 停止计时器
    const stopTimer = () => {
      if (timerInterval.value) {
        clearInterval(timerInterval.value)
        timerInterval.value = null
      }
    }
    
    // 格式化时间
    const formatTime = (seconds) => {
      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = seconds % 60
      return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
    }
    
    // 计时器格式化
    const timerFormat = (percentage) => {
      return formatTime(remainingTime.value)
    }
    
    // 上一题
    const previousQuestion = () => {
      if (currentQuestionIndex.value > 0) {
        currentQuestionIndex.value--
      }
    }
    
    // 下一题
    const nextQuestion = () => {
      if (currentQuestionIndex.value < practiceQuestions.value.length - 1) {
        currentQuestionIndex.value++
      }
    }
    
    // 获取题目类型名称
    const getQuestionTypeName = (type) => {
      const typeMap = {
        choice: '选择题',
        fill: '填空题',
        short: '简答题',
        coding: '编程题',
        essay: '论述题'
      }
      return typeMap[type] || '未知类型'
    }
    
    // 运行代码（模拟）
    const runCode = (questionIndex) => {
      const code = userAnswers.value[questionIndex]
      if (!code || !code.trim()) {
        ElMessage.warning('请先输入代码')
        return
      }
      
      // 简单的代码检查和模拟运行
      try {
        // 这里可以集成在线代码执行服务，现在先做模拟
        let result = '代码运行成功！\n'
        
        // 模拟不同编程语言的运行结果
        if (code.includes('console.log')) {
          result += '输出: Hello World'
        } else if (code.includes('function') || code.includes('=>')) {
          result += '函数定义成功'
        } else if (code.includes('class')) {
          result += '类定义成功'
        } else {
          result += '代码执行完毕'
        }
        
        codeResults.value[questionIndex] = result
        ElMessage.success('代码运行完成')
      } catch (error) {
        codeResults.value[questionIndex] = '运行错误: ' + error.message
        ElMessage.error('代码运行失败')
      }
    }
    
    // 重置代码
    const resetCode = (questionIndex) => {
      userAnswers.value[questionIndex] = ''
      delete codeResults.value[questionIndex]
      ElMessage.success('代码已重置')
    }
    
    // 提交练习
    const submitPractice = async () => {
      stopTimer()
      
      try {
        // 获取当前登录学生ID
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        const studentId = userInfo.id || userInfo.userId || 17 // 使用默认ID 17 作为后备
        
        console.log('提交练习 - 用户信息:', userInfo)
        console.log('提交练习 - 学生ID:', studentId)
        
        if (!studentId) {
          ElMessage.warning('未检测到登录信息，使用默认学生ID')
        }
        
        // 准备提交数据 - 调用练习提交API保存到数据库
        const practiceId = currentPracticeId.value // 使用保存的practiceId
        console.log('📝 使用保存的practiceId:', practiceId)
        
        if (!practiceId) {
          throw new Error('练习ID丢失，请重新开始练习')
        }
        
        // 构建答案数组 - 确保格式匹配后端DTO
        const answersForSubmit = practiceQuestions.value.map((q, idx) => {
          let answer = userAnswers.value[idx]
          if (q.type === 'fill' && Array.isArray(answer)) {
            answer = answer.join('；')
          }
          const questionId = q.id || (idx + 1)
          console.log(`题目${idx + 1}: ID=${questionId}, 答案=${answer}, 类型=${q.type}`)
          
          // 构建符合DTO格式的答案对象
          const answerItem = {
            questionId: questionId,
            answer: answer ? answer.toString() : '',
            questionType: q.type || 'choice',
            selectedOptions: null // 如果是多选题，这里应该是选项数组
          }
          
          return answerItem
        })
        
        console.log('所有题目信息:', practiceQuestions.value.map(q => ({ id: q.id, title: q.title })))
        
        // 构建完整的请求数据，匹配PracticeAnswerDTO格式
        const submitData = {
          practiceId: practiceId,
          studentId: studentId,
          answers: answersForSubmit
        }
        
        // 先调用练习提交API保存答题记录
        console.log('提交练习到数据库:', submitData)
        try {
          // 尝试使用标准接口
          const submitResponse = await submitPracticeAPI(submitData)
          console.log('✅ 练习提交响应:', submitResponse)
          
          if (submitResponse && submitResponse.success) {
            ElMessage.success('答题记录已保存到数据库！')
          } else {
            console.warn('⚠️ 提交返回success=false:', submitResponse)
          }
        } catch (submitError) {
          console.error('❌ 练习提交API调用失败:', submitError)
          console.error('错误详情:', submitError.response || submitError.message)
          ElMessage.error('保存答题记录失败: ' + (submitError.message || '未知错误'))
          // 继续执行AI评测，不中断流程
        }
        
        // 然后调用AI评测获取详细分析
        const answers = practiceQuestions.value.map((q, idx) => {
          if (q.type === 'fill' && Array.isArray(userAnswers.value[idx])) {
            return userAnswers.value[idx].join('；')
          }
          return userAnswers.value[idx]
        })
        const requestData = {
          questions: practiceQuestions.value,
          answers,
          topic: practiceForm.subject,
          timeUsed: Math.round((practiceForm.timeLimit * 60 - remainingTime.value) / 60)
        }
        console.log('发送练习评测请求:', requestData)
        const response = await aiAPI.practiceEvaluation(requestData)
        console.log('收到练习评测响应:', response)
        if (response.success === true && response.data) {
          practiceResult.value = response.data
          ElMessage.success('练习评测完成！')
          
          // 启动AI分析状态检查
          startStatusCheck()
        } else {
          throw new Error(response.msg || '评测失败：响应格式错误')
        }
      } catch (error) {
        console.error('练习评测失败:', error)
        
        // 如果后端评测失败，使用前端简单评测作为备选
        ElMessage.warning('智能评测失败，使用基础评测')
        
        let correctCount = 0
        let totalScore = 0
        let analysis = []
        const questions = practiceQuestions.value
        const answers = userAnswers.value
        
        questions.forEach((q, idx) => {
          const userAns = (answers[idx] || '').toString().trim()
          let stdAns = ''
          let isCorrect = false
          
          // 根据题目类型进行不同的答案匹配
          switch (q.type) {
            case 'choice':
              // 选择题：直接比较选项字母
              stdAns = (q.answer || '').toString().trim().toUpperCase()
              const userChoice = userAns.toUpperCase()
              isCorrect = userChoice === stdAns
              break
              
            case 'fill':
              // 填空题：比较答案内容
              stdAns = (q.answer || '').toString().trim()
              isCorrect = userAns.toLowerCase() === stdAns.toLowerCase()
              break
              
            case 'short':
              // 简答题：检查关键词匹配
              stdAns = (q.answer || q.referenceAnswer || '').toString().trim()
              isCorrect = checkShortAnswerSimilarity(userAns, stdAns)
              break
              
            case 'coding':
              // 编程题：检查代码结构和关键词
              stdAns = (q.answer || '').toString().trim()
              isCorrect = checkCodingAnswerSimilarity(userAns, stdAns)
              break
              
            case 'essay':
              // 论述题：检查内容完整性和关键词
              stdAns = (q.answer || '').toString().trim()
              isCorrect = checkEssayAnswerSimilarity(userAns, stdAns)
              break
              
            default:
              // 默认情况：直接比较
              stdAns = (q.answer || '').toString().trim()
              isCorrect = userAns.toLowerCase() === stdAns.toLowerCase()
          }
          
          if (isCorrect) correctCount++
          
          analysis.push({
            title: q.title,
            userAnswer: userAns,
            correctAnswer: stdAns,
            isCorrect,
            explanation: q.explanation || '',
            detailedAnalysis: generateBasicAnalysis(q, userAns, stdAns, isCorrect, q.type),
            suggestion: generateBasicSuggestion(isCorrect, q.type, practiceForm.subject)
          })
        })
        
        totalScore = correctCount * 10 // 每题10分
        const accuracy = questions.length > 0 ? Math.round((correctCount / questions.length) * 100) : 0
        
        practiceResult.value = {
          score: totalScore,
          correctCount,
          accuracy,
          timeUsed: Math.round((practiceForm.timeLimit * 60 - remainingTime.value) / 60),
          analysis
        }
        
        ElMessage.success('练习提交成功！')
      }
    }
    
    // 生成基础分析（前端备选）
    const generateBasicAnalysis = (question, userAnswer, correctAnswer, isCorrect, questionType) => {
      let analysis = isCorrect ? 
        '✅ 回答正确！你的答案完全正确，说明你已经很好地掌握了相关知识点。' :
        `❌ 回答错误。你的答案：${userAnswer}，正确答案：${correctAnswer}。建议复习相关知识点。`
      
      analysis += '\n\n### 知识点梳理\n本题涉及的核心知识点：\n- 基础概念理解\n- 应用场景分析\n- 技术原理掌握'
      
      analysis += '\n\n### 解题思路\n1. 仔细阅读题目，理解问题要求\n2. 分析题目涉及的知识点\n3. 运用相关知识进行推理\n4. 验证答案的合理性'
      
      return analysis
    }
    
    // 生成基础学习建议（前端备选）
    const generateBasicSuggestion = (isCorrect, questionType, topic) => {
      if (isCorrect) {
        return `✅ 回答正确！你对${topic}的${getQuestionTypeName(questionType)}掌握得很好。建议尝试更难的题目，或者学习相关的进阶知识。`
      } else {
        return `❌ 回答有误，建议重新学习${topic}的基础概念，多做类似练习。`
      }
    }
    
    // 格式化分析文本
    const formatAnalysis = (text) => {
      if (!text) return ''
      
      // 将markdown格式转换为HTML
      return text
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>')
        .replace(/### (.*?)\n/g, '<h3>$1</h3>')
        .replace(/## (.*?)\n/g, '<h2>$1</h2>')
        .replace(/# (.*?)\n/g, '<h1>$1</h1>')
        .replace(/\n/g, '<br>')
        .replace(/✅/g, '<span style="color: #67c23a;">✅</span>')
        .replace(/❌/g, '<span style="color: #f56c6c;">❌</span>')
    }
    
    // 检查简答题答案相似度
    const checkShortAnswerSimilarity = (userAnswer, correctAnswer) => {
      if (!userAnswer || !correctAnswer) return false
      
      // 提取关键词进行比较
      const userKeywords = extractKeywords(userAnswer)
      const correctKeywords = extractKeywords(correctAnswer)
      
      // 计算关键词匹配度
      const matchCount = userKeywords.filter(keyword => 
        correctKeywords.includes(keyword)
      ).length
      
      const similarity = matchCount / Math.max(userKeywords.length, correctKeywords.length)
      return similarity >= 0.3 // 30%的关键词匹配即认为正确
    }
    
    // 检查编程题答案相似度
    const checkCodingAnswerSimilarity = (userAnswer, correctAnswer) => {
      if (!userAnswer || !correctAnswer) return false
      
      // 检查代码结构关键词
      const codeKeywords = ['function', 'class', 'const', 'let', 'var', 'if', 'for', 'while', 'return', 'async', 'await']
      const userHasKeywords = codeKeywords.some(keyword => 
        userAnswer.toLowerCase().includes(keyword)
      )
      
      // 检查基本语法结构
      const hasBasicStructure = userAnswer.includes('{') && userAnswer.includes('}')
      
      // 检查长度合理性（至少有一定内容）
      const hasReasonableLength = userAnswer.length >= 20
      
      return userHasKeywords && hasBasicStructure && hasReasonableLength
    }
    
    // 检查论述题答案相似度
    const checkEssayAnswerSimilarity = (userAnswer, correctAnswer) => {
      if (!userAnswer || !correctAnswer) return false
      
      // 检查内容长度（论述题应该有足够的内容）
      const hasReasonableLength = userAnswer.length >= 50
      
      // 提取关键词进行比较
      const userKeywords = extractKeywords(userAnswer)
      const correctKeywords = extractKeywords(correctAnswer)
      
      // 计算关键词匹配度
      const matchCount = userKeywords.filter(keyword => 
        correctKeywords.includes(keyword)
      ).length
      
      const similarity = matchCount / Math.max(userKeywords.length, correctKeywords.length)
      
      return hasReasonableLength && similarity >= 0.2 // 20%的关键词匹配即认为正确
    }
    
    // 提取关键词的辅助函数
    const extractKeywords = (text) => {
      // 简单的关键词提取：去除常见停用词，保留有意义的词汇
      const stopWords = ['的', '是', '在', '有', '和', '与', '或', '但', '而', '如果', '因为', '所以', 'the', 'is', 'are', 'in', 'on', 'at', 'and', 'or', 'but', 'if', 'because', 'so']
      
      return text.toLowerCase()
        .replace(/[^\w\s\u4e00-\u9fa5]/g, ' ') // 保留中英文和数字
        .split(/\s+/)
        .filter(word => word.length > 1 && !stopWords.includes(word))
        .slice(0, 10) // 取前10个关键词
    }
    
    // 暂停练习
    const pausePractice = () => {
      stopTimer()
      ElMessage.info('练习已暂停')
    }
    
    // 重新练习
    const retryPractice = () => {
      practiceResult.value = null
      practiceStarted.value = false
      currentQuestionIndex.value = 0
      userAnswers.value = new Array(practiceQuestions.value.length).fill('')
      
      // 保存状态到本地存储
      savePracticeToStorage()
      
      ElMessage.success('可以重新开始练习')
    }
    
    // 查看答案
    const viewAnswers = () => {
      ElMessage.info('查看答案功能开发中...')
    }
    
    // 导出结果
    const exportResult = () => {
      ElMessage.info('导出功能开发中...')
    }
    
    // 测试后端连接
    const testBackend = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/ai/test')
        const data = await response.json()
        console.log('后端测试响应:', data)
        ElMessage.success('后端连接正常')
      } catch (error) {
        console.error('后端连接测试失败:', error)
        ElMessage.error('后端连接失败')
      }
    }
    
    // 健康检查
    const healthCheck = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/ai/health')
        const data = await response.json()
        console.log('健康检查响应:', data)
        
        if (data.success && data.data) {
          const health = data.data
          let message = '系统状态正常'
          if (health.aiTest === 'failed') {
            message = `AI服务异常：${health.aiError}`
          }
          ElMessage.success(message)
        } else {
          ElMessage.error('健康检查失败')
        }
      } catch (error) {
        console.error('健康检查失败:', error)
        ElMessage.error('健康检查失败')
      }
    }
    
    // 确认清除练习
    const confirmClearPractice = async () => {
      try {
        await ElMessageBox.confirm(
          '确定要清除当前练习吗？这将删除所有题目、答案和进度，且无法恢复。',
          '确认清除',
          {
            confirmButtonText: '确定清除',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        // 清除所有练习数据
        practiceQuestions.value = []
        practiceStarted.value = false
        practiceResult.value = null
        currentQuestionIndex.value = 0
        userAnswers.value = []
        remainingTime.value = 0
        selectedExam.value = null
        currentPracticeId.value = null
        codeResults.value = {}
        
        // 停止计时器
        stopTimer()
        
        // 清除本地存储
        clearPracticeStorage()
        
        ElMessage.success('练习已清除')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('清除练习失败:', error)
        }
      }
    }
    
    // 生成分析报告
    const generateReport = async (index) => {
      if (!practiceResult.value || !practiceResult.value.analysis[index]) {
        ElMessage.error('未找到题目数据')
        return
      }
      
      const analysisItem = practiceResult.value.analysis[index]
      const question = practiceQuestions.value[index]
      
      // 设置加载状态
      analysisItem.isTriggering = true
      
      try {
        const requestData = {
          question,
          userAnswer: analysisItem.userAnswer,
          correctAnswer: analysisItem.correctAnswer,
          isCorrect: analysisItem.isCorrect,
          topic: practiceForm.subject
        }
        
        const response = await aiAPI.triggerAIAnalysis(requestData)
        if (response.success && response.data) {
          // 更新分析结果
          analysisItem.detailedAnalysis = response.data.detailedAnalysis
          analysisItem.suggestion = response.data.suggestion
          analysisItem.needsAIAnalysis = false
          ElMessage.success('AI分析生成成功！')
          
          // 设置弹窗数据
          currentReport.value = {
            title: question.title,
            detailedAnalysis: response.data.detailedAnalysis,
            suggestion: response.data.suggestion
          }
          
          // 显示弹窗
          reportDialogVisible.value = true
        } else {
          throw new Error(response.msg || 'AI分析生成失败')
        }
      } catch (error) {
        console.error('生成分析报告失败:', error)
        ElMessage.error('生成分析报告失败: ' + error.message)
      } finally {
        analysisItem.isTriggering = false
      }
    }
    
    // 关闭弹窗
    const closeReportDialog = () => {
      currentReport.value = null
      reportDialogVisible.value = false
    }
    
    // 检查AI分析状态
    const checkAnalysisStatus = async () => {
      if (!practiceResult.value || !practiceResult.value.analysis) {
        return
      }
      
      try {
        const response = await aiAPI.checkAnalysisStatus({
          analysis: practiceResult.value.analysis
        })
        
        if (response.success && response.data) {
          const { statusList, allCompleted } = response.data
          
          // 更新分析状态
          statusList.forEach(status => {
            const index = status.index
            if (practiceResult.value.analysis[index]) {
              practiceResult.value.analysis[index].needsAIAnalysis = status.needsAIAnalysis
            }
          })
          
          // 如果所有分析都完成了，显示提示
          if (allCompleted) {
            ElMessage.success('所有AI分析已完成！')
          }
        }
      } catch (error) {
        console.error('检查AI分析状态失败:', error)
      }
    }
    
    // 定期检查AI分析状态
    let statusCheckInterval = null
    
    const startStatusCheck = () => {
      // 暂时禁用自动状态检查，避免频繁请求
      console.log('状态检查已禁用')
      /*
      if (statusCheckInterval) {
        clearInterval(statusCheckInterval)
      }
      
      statusCheckInterval = setInterval(() => {
        checkAnalysisStatus()
      }, 5000) // 每5秒检查一次
      */
    }
    
    const stopStatusCheck = () => {
      if (statusCheckInterval) {
        clearInterval(statusCheckInterval)
        statusCheckInterval = null
      }
    }
    
    // 获取教师发布的考试
    const fetchTeacherExams = async () => {
      if (!courseInfo.courseId) {
        console.log('没有课程ID，跳过获取教师考试')
        return
      }
      
      try {
        loadingExams.value = true
        console.log('获取课程已发布考试列表，课程ID:', courseInfo.courseId)
        
        // 使用新的API获取已发布的考试
        const response = await getPublishedExamsByCourseId(courseInfo.courseId)
        console.log('获取已发布考试响应:', response)
        
        if (response && response.success) {
          const exams = response.data || []
          console.log('成功获取已发布考试:', exams)
          
          // 为每个考试加载题目详情
          const examsWithQuestions = []
          for (const exam of exams) {
            try {
              const examDetail = await getExamById(exam.id)
              if (examDetail && examDetail.success && examDetail.data) {
                examsWithQuestions.push(examDetail.data)
              } else {
                examsWithQuestions.push(exam)
              }
            } catch (error) {
              console.error(`获取考试${exam.id}详情失败:`, error)
              examsWithQuestions.push(exam)
            }
          }
          
          teacherExams.value = examsWithQuestions
          console.log('加载题目详情后的考试列表:', teacherExams.value)
        } else {
          console.log('API调用成功但数据为空')
          teacherExams.value = []
        }
      } catch (error) {
        console.error('获取已发布考试失败:', error)
        // 提供示例数据 - 模拟教师端生成的考试
        teacherExams.value = [
          {
            id: 1,
            name: '数据结构与算法基础测试',
            type: 'quiz',
            duration: 40,
            totalScore: 100,
            description: '张教授精心设计的数据结构基础概念测试，包含栈、队列、链表等重要知识点',
            status: 'published',
            createTime: '2024-12-19 10:00:00',
            questions: [
              {
                id: 1,
                content: '下列关于栈（Stack）的描述，正确的是：',
                type: 'choice',
                score: 20,
                difficulty: 'easy',
                answer: 'A',
                analysis: '栈是一种后进先出(LIFO)的数据结构，只能在栈顶进行插入和删除操作。',
                options: [
                  { key: 'A', content: '栈是一种后进先出(LIFO)的数据结构' },
                  { key: 'B', content: '栈是一种先进先出(FIFO)的数据结构' },
                  { key: 'C', content: '栈可以在任意位置插入和删除元素' },
                  { key: 'D', content: '栈的大小是固定不变的' }
                ]
              },
              {
                id: 2,
                content: '队列（Queue）的基本操作包括：',
                type: 'choice',
                score: 20,
                difficulty: 'easy',
                answer: 'C',
                analysis: '队列的基本操作是入队(enqueue)和出队(dequeue)，分别在队尾插入元素和在队头删除元素。',
                options: [
                  { key: 'A', content: 'push和pop' },
                  { key: 'B', content: 'insert和delete' },
                  { key: 'C', content: 'enqueue和dequeue' },
                  { key: 'D', content: 'add和remove' }
                ]
              },
              {
                id: 3,
                content: '简述链表相比数组的优缺点：',
                type: 'short',
                score: 30,
                difficulty: 'medium',
                answer: '优点：动态分配内存，插入删除操作效率高；缺点：不支持随机访问，需要额外存储指针',
                analysis: '这道题考查学生对链表和数组两种数据结构特点的理解。'
              },
              {
                id: 4,
                content: '实现一个栈的基本操作（包括push、pop、isEmpty），请用你熟悉的编程语言编写代码：',
                type: 'coding',
                score: 30,
                difficulty: 'hard',
                answer: '参考实现：使用数组或链表实现栈的基本操作',
                analysis: '这道编程题考查学生对栈数据结构的实际编程能力。'
              }
            ]
          },
          {
            id: 2,
            name: 'Java程序设计综合练习',
            type: 'homework',
            duration: 60,
            totalScore: 120,
            description: '张教授设计的Java面向对象编程综合练习，涵盖类、对象、继承、多态等核心概念',
            status: 'published',
            createTime: '2024-12-18 14:00:00',
            questions: [
              {
                id: 5,
                content: 'Java语言的主要特点包括：',
                type: 'choice',
                score: 15,
                difficulty: 'easy',
                answer: 'D',
                analysis: 'Java语言具有面向对象、平台无关性、自动内存管理等多个重要特点。',
                options: [
                  { key: 'A', content: '仅仅是面向对象' },
                  { key: 'B', content: '仅仅是平台无关' },
                  { key: 'C', content: '仅仅是安全性高' },
                  { key: 'D', content: '面向对象、平台无关、安全性高、自动内存管理' }
                ]
              },
              {
                id: 6,
                content: '面向对象编程的三大特性是：',
                type: 'choice',
                score: 15,
                difficulty: 'easy',
                answer: 'B',
                analysis: '面向对象编程的三大特性是封装、继承和多态，这是面向对象编程的核心概念。',
                options: [
                  { key: 'A', content: '封装、抽象、继承' },
                  { key: 'B', content: '封装、继承、多态' },
                  { key: 'C', content: '继承、多态、抽象' },
                  { key: 'D', content: '抽象、封装、多态' }
                ]
              },
              {
                id: 7,
                content: '请解释Java中方法重载（Overloading）和方法重写（Overriding）的区别：',
                type: 'short',
                score: 40,
                difficulty: 'medium',
                answer: '重载是同一类中方法名相同但参数不同；重写是子类重新定义父类的方法',
                analysis: '这道题考查学生对Java中重载和重写概念的理解和区分能力。'
              },
              {
                id: 8,
                content: '设计一个Java类层次结构：定义一个Animal基类和Dog、Cat子类，体现继承和多态的特性。请编写完整代码：',
                type: 'coding',
                score: 50,
                difficulty: 'hard',
                answer: '需要定义Animal基类，包含共同属性和方法；Dog和Cat继承Animal并重写方法',
                analysis: '这道编程题综合考查学生对Java面向对象编程的掌握程度。'
              }
            ]
          }
        ]
      } finally {
        loadingExams.value = false
      }
    }
    
    // 获取考试类型名称
    const getExamTypeName = (type) => {
      const typeMap = {
        'quiz': '小测验',
        'homework': '作业',
        'midterm': '期中考试',
        'final': '期末考试',
        'practice': '练习'
      }
      return typeMap[type] || type
    }
    
    // 获取考试类型标签
    const getExamTypeTag = (type) => {
      const tagMap = {
        'quiz': 'success',
        'homework': 'warning',
        'midterm': 'danger',
        'final': 'danger',
        'practice': 'info'
      }
      return tagMap[type] || 'info'
    }
    
    // 选择考试
    const selectExam = (exam) => {
      selectedExam.value = exam
      console.log('选择考试:', exam)
    }
    
    // 获取考试详细信息（包括具体题目）
    const fetchExamDetails = async (examId) => {
      try {
        console.log('获取考试详细信息，ID:', examId)
        const response = await axios.get(`/api/exam/${examId}`)
        console.log('考试详细信息响应:', response.data)
        
        if (response.data && response.data.success) {
          return response.data.data
        } else {
          throw new Error('获取考试详情失败')
        }
      } catch (error) {
        console.error('获取考试详情失败，使用示例数据:', error)
        
        // 当API调用失败时，从示例数据中查找对应的考试
        const examFromList = teacherExams.value.find(exam => exam.id === examId)
        if (examFromList) {
          console.log('找到示例考试数据:', examFromList)
          return examFromList
        } else {
          ElMessage.error('获取考试详情失败')
          return null
        }
      }
    }
    
    // 开始考试 - 获取教师生成的具体题目
    const startExam = async (exam) => {
      selectedExam.value = exam
      console.log('开始考试:', exam)
      
      // 获取考试的详细题目信息
      const examDetails = await fetchExamDetails(exam.id)
      if (examDetails && examDetails.questions) {
        // 将教师生成的题目转换为练习格式
        practiceQuestions.value = examDetails.questions.map(q => ({
          id: q.id,
          title: q.content,
          type: q.type,
          content: q.content,
          options: q.options ? q.options.map(opt => opt.content) : null,
          optionKeys: q.options ? q.options.map(opt => opt.key) : null,
          score: q.score,
          difficulty: q.difficulty || 'medium',
          answer: q.answer,
          explanation: q.analysis || q.explanation || '',
          examSource: true // 标记这是来自教师考试的题目
        }))
        
        // 初始化用户答案数组
        userAnswers.value = practiceQuestions.value.map(q => {
          if (q.type === 'fill' && q.blanks && Array.isArray(q.blanks)) {
            return Array(q.blanks.length).fill('')
          }
          return ''
        })
        
        // 设置考试时间
        practiceForm.timeLimit = examDetails.duration || exam.duration || 30
        
        // 保存到本地存储
        savePracticeToStorage()
        
        ElMessage.success(`开始考试：${exam.name}，共${practiceQuestions.value.length}道题`)
        
        // 自动跳转到练习模块
        startPractice()
        
        // 滚动到练习题目区域
        setTimeout(() => {
          const questionsSection = document.getElementById('questions-section')
          if (questionsSection) {
            questionsSection.scrollIntoView({ 
              behavior: 'smooth', 
              block: 'start' 
            })
            
            // 添加高亮效果
            questionsSection.classList.add('highlight')
            setTimeout(() => {
              questionsSection.classList.remove('highlight')
            }, 2000)
          }
        }, 500)
      } else {
        ElMessage.error('无法获取考试题目，请联系教师')
      }
    }
    
    // 预览考试 - 显示教师生成的具体题目
    const previewExam = async (exam) => {
      console.log('预览考试:', exam)
      
      // 获取考试的详细题目信息
      const examDetails = await fetchExamDetails(exam.id)
      if (examDetails && examDetails.questions) {
        // 将教师生成的题目转换为练习格式
        practiceQuestions.value = examDetails.questions.map(q => ({
          id: q.id,
          title: q.content,
          type: q.type,
          content: q.content,
          options: q.options ? q.options.map(opt => opt.content) : null,
          optionKeys: q.options ? q.options.map(opt => opt.key) : null,
          score: q.score,
          difficulty: q.difficulty || 'medium',
          answer: q.answer,
          explanation: q.analysis || q.explanation || '',
          examSource: true // 标记这是来自教师考试的题目
        }))
        
        ElMessage.success(`预览考试：${exam.name}，共${practiceQuestions.value.length}道题`)
        
        // 保存到本地存储
        savePracticeToStorage()
        
        // 自动滚动到练习题目区域
        setTimeout(() => {
          const questionsSection = document.getElementById('questions-section')
          if (questionsSection) {
            questionsSection.scrollIntoView({ 
              behavior: 'smooth', 
              block: 'start' 
            })
            
            // 添加高亮效果
            questionsSection.classList.add('highlight')
            setTimeout(() => {
              questionsSection.classList.remove('highlight')
            }, 2000)
          }
        }, 300)
      } else {
        ElMessage.error('无法获取考试题目')
      }
    }
    
    // 页面挂载时获取数据
    onMounted(() => {
      console.log('页面挂载，课程信息:', courseInfo)
      
      // 先尝试从本地存储恢复练习数据
      const restored = loadPracticeFromStorage()
      
      if (restored && practiceQuestions.value.length > 0) {
        console.log('✅ 已从本地存储恢复练习数据')
        ElMessage.info('已恢复您上次的练习进度')
      }
      
      // 获取教师发布的考试
      if (courseInfo.courseId) {
        fetchTeacherExams()
      }
    })
    
    // 组件卸载时清理计时器
    onUnmounted(() => {
      stopTimer()
      stopStatusCheck()
      // 注意：不在这里清除localStorage，以便用户返回时可以恢复
    })
    
    return {
      // 课程和考试相关
      courseInfo,
      teacherExams,
      loadingExams,
      selectedExam,
      fetchTeacherExams,
      fetchExamDetails,
      getExamTypeName,
      getExamTypeTag,
      selectExam,
      startExam,
      previewExam,
      // 原有的练习相关
      practiceForm,
      generating,
      practiceQuestions,
      practiceStarted,
      currentQuestionIndex,
      userAnswers,
      practiceResult,
      remainingTime,
      canGenerate,
      totalQuestionCount,
      currentQuestion,
      timerProgress,
      generatePractice,
      resetForm,
      startPractice,
      formatTime,
      timerFormat,
      previousQuestion,
      nextQuestion,
      submitPractice,
      pausePractice,
      retryPractice,
      viewAnswers,
      exportResult,
      testBackend,
      healthCheck,
      getQuestionTypeName,
      runCode,
      resetCode,
      codeResults,
      reportDialogVisible,
      currentReport,
      generateReport,
      closeReportDialog,
      formatAnalysis,
      // 新增：本地存储相关
      savePracticeToStorage,
      loadPracticeFromStorage,
      clearPracticeStorage,
      confirmClearPractice
    }
  }
}
</script>

<style scoped>
.practice-evaluation {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 课程信息卡片样式 */
.course-info-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.course-info-card :deep(.el-card__header) {
  background: transparent;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-header h2 {
  margin: 0;
  color: white;
  font-size: 24px;
}

.course-description {
  padding: 10px 0;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.9);
}

/* 考试列表样式 */
.exams-card {
  margin-bottom: 20px;
}

.exams-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  padding: 20px 0;
}

.exam-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.exam-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  border-color: #409eff;
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.exam-header h4 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
  flex: 1;
  margin-right: 10px;
}

.exam-tags {
  display: flex;
  flex-direction: column;
  gap: 5px;
  align-items: flex-end;
}

.exam-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  color: #606266;
  font-size: 14px;
}

.info-item i {
  margin-right: 5px;
  color: #909399;
}

.exam-description {
  margin-bottom: 15px;
  color: #606266;
  line-height: 1.5;
}

.exam-actions {
  display: flex;
  gap: 10px;
}

.exam-actions .el-button {
  flex: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .exams-grid {
    grid-template-columns: 1fr;
  }
  
  .course-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}

.practice-card {
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

.practice-settings {
  margin-bottom: 30px;
}

.practice-settings h3 {
  margin-bottom: 20px;
  color: #303133;
  border-bottom: 2px solid #e6a23c;
  padding-bottom: 10px;
}

.practice-form {
  max-width: 100%;
}

.unit {
  margin-left: 10px;
  color: #606266;
}

.question-count-tips {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.generate-actions {
  text-align: center;
  padding: 20px 0;
  border-top: 1px solid #ebeef5;
}

.questions-section {
  margin-top: 20px;
  scroll-margin-top: 20px;
  transition: all 0.3s ease;
}

.questions-section.highlight {
  animation: highlight-section 2s ease-in-out;
}

@keyframes highlight-section {
  0% {
    transform: translateY(-10px);
    opacity: 0.8;
  }
  50% {
    transform: translateY(0);
    opacity: 1;
  }
  100% {
    transform: translateY(0);
    opacity: 1;
  }
}

.questions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.header-title .el-tag {
  animation: fade-in 0.5s ease-in-out;
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.questions-header h3 {
  margin: 0;
  color: #303133;
  font-size: 1.3rem;
}

.practice-info {
  display: flex;
  gap: 20px;
  align-items: center;
}

.questions-preview {
  padding: 20px 0;
}

.question-preview {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #e6a23c;
}

.question-header {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.question-number {
  font-weight: bold;
  color: #303133;
}

.question-type,
.question-difficulty {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.question-type {
  background: #e1f3d8;
  color: #67c23a;
}

.question-difficulty {
  background: #fdf6ec;
  color: #e6a23c;
}

.question-content h4 {
  margin: 0 0 15px 0;
  color: #303133;
}

.question-options {
  margin: 15px 0;
}

.option {
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}

.practice-session {
  padding: 20px 0;
}

.practice-timer {
  margin-bottom: 30px;
}

.timer-text {
  text-align: center;
  margin-top: 10px;
  font-size: 16px;
  color: #e6a23c;
  font-weight: bold;
}

.current-question {
  background: #f8f9fa;
  padding: 30px;
  border-radius: 12px;
}

.question-navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.question-counter {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.question-display {
  margin-bottom: 30px;
}

.question-actions {
  text-align: center;
}

.option-radio {
  display: block;
  margin: 15px 0;
  padding: 10px;
  border-radius: 6px;
  transition: background 0.3s;
}

.option-radio:hover {
  background: #f0f9ff;
}

.question-textarea {
  margin-top: 20px;
}

.result-section {
  margin-top: 20px;
}

.result-summary {
  margin-bottom: 30px;
}

.result-stat {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  color: #606266;
  font-size: 14px;
}

.result-details {
  margin-bottom: 30px;
}

.result-details h4 {
  margin-bottom: 20px;
  color: #303133;
}

.analysis-item {
  margin-bottom: 25px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.analysis-item h5 {
  margin: 0 0 15px 0;
  color: #303133;
}

.suggestions {
  margin-top: 15px;
}

.suggestions ul {
  margin: 10px 0;
  padding-left: 20px;
}

.suggestions li {
  margin: 5px 0;
  color: #606266;
}

.result-actions {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

/* 题目类型配置样式 */
.question-type-config {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.type-config-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #f5f7fa;
}

.type-name {
  font-weight: 500;
  color: #606266;
  min-width: 60px;
}

/* 填空题样式 */
.question-fill {
  margin-top: 20px;
}

.fill-instruction {
  margin-bottom: 15px;
  color: #606266;
  font-weight: 500;
}

.fill-blanks {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.blank-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.blank-label {
  min-width: 80px;
  font-weight: 500;
  color: #409eff;
}

.blank-input {
  flex: 1;
  max-width: 300px;
}

.single-fill {
  max-width: 400px;
}

/* 简答题样式 */
.question-short {
  margin-top: 20px;
}

.short-instruction {
  margin-bottom: 15px;
  color: #606266;
  font-weight: 500;
}

.short-tips {
  margin-top: 15px;
}

.short-tips ul {
  margin: 10px 0 0 0;
  padding-left: 20px;
}

.short-tips li {
  margin-bottom: 5px;
  color: #606266;
}

/* 编程题样式 */
.question-coding {
  margin-top: 20px;
}

.coding-instruction {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 6px;
  border-left: 4px solid #409eff;
}

.instruction-content h5 {
  margin: 0 0 10px 0;
  color: #409eff;
  font-size: 16px;
}

.examples {
  margin-top: 15px;
}

.examples h6 {
  margin: 0 0 10px 0;
  color: #606266;
}

.example-item {
  margin-bottom: 10px;
  padding: 10px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.example-input {
  color: #909399;
  margin-bottom: 5px;
}

.example-output {
  color: #67c23a;
  font-weight: 500;
}

.coding-editor {
  margin-bottom: 15px;
}

.code-textarea {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  line-height: 1.4;
}

.code-textarea textarea {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.4;
}

.coding-actions {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
}

.code-result {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.code-result h6 {
  margin: 0 0 10px 0;
  color: #409eff;
}

.code-result pre {
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  color: #606266;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* 论述题样式 */
.question-essay {
  margin-top: 20px;
}

.essay-instruction {
  margin-bottom: 15px;
  color: #606266;
  font-weight: 500;
}

.essay-tips {
  margin-top: 15px;
}

.essay-tips ul {
  margin: 10px 0 0 0;
  padding-left: 20px;
}

.essay-tips li {
  margin-bottom: 5px;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .practice-evaluation {
    padding: 10px;
  }
  
  .questions-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .practice-info {
    flex-direction: column;
    gap: 10px;
  }
  
  .question-navigation {
    flex-direction: column;
    gap: 15px;
  }
  
  .current-question {
    padding: 20px;
  }
  
  .question-type-config {
    flex-direction: column;
  }
  
  .type-config-item {
    width: 100%;
  }
  
  .blank-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .blank-input {
    max-width: 100%;
  }
  
  .coding-actions {
    flex-direction: column;
  }
}

/* 详细解答弹窗样式 */
.analysis-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.analysis-header h5 {
  margin: 0;
  flex: 1;
  margin-right: 15px;
}

.analysis-status {
  display: flex;
  align-items: center;
}

.analysis-content {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 6px;
  margin-top: 10px;
}

.analysis-content p {
  margin: 8px 0;
  line-height: 1.6;
}

.detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #ebeef5;
}

.detail-header h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.detail-answers {
  margin-bottom: 25px;
}

.answer-item {
  margin-bottom: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #409eff;
}

.answer-item h5 {
  margin: 0 0 10px 0;
  color: #409eff;
  font-size: 14px;
}

.answer-content {
  padding: 10px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  min-height: 40px;
  line-height: 1.6;
}

.answer-content.correct {
  border-color: #67c23a;
  background: #f0f9ff;
  color: #67c23a;
}

.detail-analysis {
  margin-bottom: 25px;
}

.detail-analysis h5 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
}

.analysis-text {
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #e6a23c;
  line-height: 1.8;
  color: #606266;
}

.analysis-text h1,
.analysis-text h2,
.analysis-text h3 {
  margin: 15px 0 10px 0;
  color: #303133;
}

.analysis-text h1 {
  font-size: 18px;
}

.analysis-text h2 {
  font-size: 16px;
}

.analysis-text h3 {
  font-size: 14px;
}

.detail-suggestion {
  margin-bottom: 20px;
}

.detail-suggestion h5 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
}

.suggestion-text {
  padding: 15px;
  background: #f0f9ff;
  border-radius: 6px;
  border-left: 4px solid #67c23a;
  line-height: 1.6;
  color: #606266;
  font-weight: 500;
}

/* AI分析状态显示样式 */
.ai-analysis-status {
  margin-top: 10px;
  display: flex;
  align-items: center;
}

.ai-analysis-status .el-tag {
  margin-right: 10px;
}

.ai-analysis-status .el-button {
  margin-left: 10px;
}

/* 需要AI分析的提示样式 */
.need-ai-tip {
  margin-top: 15px;
  padding: 12px 15px;
  background: linear-gradient(135deg, #fff5e6 0%, #ffe8cc 100%);
  border: 1px dashed #e6a23c;
  border-radius: 8px;
  text-align: center;
  animation: gentle-pulse 2s ease-in-out infinite;
}

.need-ai-tip .el-tag {
  font-size: 13px;
  padding: 8px 15px;
  background: white;
  border: 1px solid #e6a23c;
}

.need-ai-tip i {
  margin-right: 5px;
  font-weight: bold;
}

/* AI已生成的提示样式 */
.ai-generated-tip {
  margin-top: 15px;
  padding: 12px 15px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e1f3f8 100%);
  border: 1px solid #67c23a;
  border-radius: 8px;
  text-align: center;
}

.ai-generated-tip .el-tag {
  font-size: 13px;
  padding: 8px 15px;
  background: white;
  border: 1px solid #67c23a;
}

.ai-generated-tip i {
  margin-right: 5px;
  font-weight: bold;
}

@keyframes gentle-pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 2px 8px rgba(230, 162, 60, 0.2);
  }
  50% {
    transform: scale(1.02);
    box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
  }
}

/* 优化分析按钮样式 */
.analysis-status .el-button {
  font-weight: 500;
  transition: all 0.3s ease;
}

.analysis-status .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.analysis-status .el-button.is-loading {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

/* 优化Alert提示框样式 */
.result-details .el-alert {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.result-details .el-alert p {
  line-height: 1.8;
}

.result-details .el-alert strong {
  color: #409eff;
  font-size: 15px;
}
</style> 