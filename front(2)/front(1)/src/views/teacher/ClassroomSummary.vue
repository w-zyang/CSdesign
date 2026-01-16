<template>
  <div class="classroom-summary">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>📚 课堂重点整理</h2>
      <p class="subtitle">通过录音录制教师上课内容，结合课件生成课堂重点整理文档</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon">📚</div>
              <div class="stat-info">
                <div class="stat-number">{{ statistics.totalCount || 0 }}</div>
                <div class="stat-label">总课堂数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon">✅</div>
              <div class="stat-info">
                <div class="stat-number">{{ statistics.publishedCount || 0 }}</div>
                <div class="stat-label">已发布</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon">📝</div>
              <div class="stat-info">
                <div class="stat-number">{{ statistics.draftCount || 0 }}</div>
                <div class="stat-label">草稿</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon">👁️</div>
              <div class="stat-info">
                <div class="stat-number">{{ statistics.totalViews || 0 }}</div>
                <div class="stat-label">总浏览量</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <el-row :gutter="20">
        <!-- 左侧：课堂总结列表 -->
        <el-col :span="10">
          <el-card class="summary-list-card">
            <template #header>
              <div class="card-header">
                <span>我的课堂总结</span>
                <el-button type="primary" @click="showCreateDialog = true">
                  新建课堂总结
                </el-button>
              </div>
            </template>
            
            <div class="summary-list">
              <div 
                v-for="summary in summaryList" 
                :key="summary.id"
                class="summary-item"
                :class="{ active: selectedSummary?.id === summary.id }"
                @click="selectSummary(summary)"
              >
                <div class="summary-header">
                  <h4>{{ summary.title }}</h4>
                  <el-tag 
                    :type="getStatusType(summary.status)"
                    size="small"
                  >
                    {{ getStatusText(summary.status) }}
                  </el-tag>
                </div>
                <p class="summary-desc">{{ summary.description }}</p>
                <div class="summary-meta">
                  <span>{{ summary.courseName }}</span>
                  <span>{{ formatDate(summary.createTime) }}</span>
                </div>
                <div class="summary-actions">
                  <el-button size="small" @click.stop="editSummary(summary)">编辑</el-button>
                  <el-button 
                    v-if="summary.status === 'PUBLISHED'"
                    size="small" 
                    type="success"
                    @click.stop="viewPublished(summary)"
                  >
                    查看
                  </el-button>
                  <el-button 
                    size="small" 
                    type="danger"
                    @click.stop="confirmDelete(summary)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
              
              <div v-if="summaryList.length === 0" class="empty-state">
                <p>暂无课堂总结</p>
                <el-button type="primary" @click="showCreateDialog = true">
                  创建第一个课堂总结
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：处理区域 -->
        <el-col :span="14">
          <el-card class="process-card" v-if="selectedSummary">
            <template #header>
              <div class="card-header">
                <span>{{ selectedSummary.title }} - 处理面板</span>
              </div>
            </template>

            <!-- 步骤指示器 -->
            <div class="process-steps">
              <el-steps :active="currentStep" align-center>
                <el-step title="录音上传" description="上传课堂录音文件"></el-step>
                <el-step title="语音转文字" description="AI识别音频内容"></el-step>
                <el-step title="AI分析" description="生成重点整理"></el-step>
                <el-step title="编辑发布" description="完善并发布"></el-step>
              </el-steps>
            </div>

            <!-- 步骤内容 -->
            <div class="step-content">
              <!-- 步骤1: 录音上传 -->
              <div class="step-panel" v-show="currentStep === 0">
                <h3>🎤 录音上传</h3>
                
                <!-- 实时录音 -->
                <div class="recording-section">
                  <h4>实时录音</h4>
                  <div class="recorder-controls">
                    <el-button 
                      :type="isRecording ? 'danger' : 'primary'"
                      size="large"
                      @click="toggleRecording"
                    >
                      {{ isRecording ? '🛑 停止录音' : '🎤 开始录音' }}
                    </el-button>
                    <div v-if="isRecording" class="recording-indicator">
                      🔴 录音中... {{ recordingDuration }}s
                    </div>
                  </div>
                  
                  <div v-if="recordedBlob" class="recorded-audio">
                    <audio :src="recordedAudioUrl" controls style="width: 100%; margin: 10px 0;"></audio>
                    <el-button type="success" @click="uploadRecordedAudio">
                      上传此录音
                    </el-button>
                  </div>
                </div>

                <!-- 文件上传 -->
                <div class="upload-section">
                  <h4>上传录音文件</h4>
                  <el-upload
                    class="audio-uploader"
                    :before-upload="beforeAudioUpload"
                    :show-file-list="false"
                    accept=".mp3,.wav,.m4a,.webm"
                    :http-request="customAudioUpload"
                  >
                    <el-button type="primary">
                      📁 选择音频文件
                    </el-button>
                    <template #tip>
                      <div class="el-upload__tip">
                        支持 MP3、WAV、M4A、WebM 格式，文件大小不超过100MB
                      </div>
                    </template>
                  </el-upload>
                </div>

                <!-- 课件输入 -->
                <div class="courseware-section">
                  <h4>课件内容（可选）</h4>
                  <el-input
                    v-model="coursewareContent"
                    type="textarea"
                    :rows="4"
                    placeholder="可以粘贴课件文本内容，帮助AI更好地生成重点整理"
                  />
                </div>

                <div class="step-actions">
                  <el-button 
                    type="primary" 
                    @click="nextStep"
                    :disabled="!hasAudioFile"
                  >
                    下一步：语音转文字
                  </el-button>
                </div>
              </div>

              <!-- 步骤2: 语音转文字 -->
              <div class="step-panel" v-show="currentStep === 1">
                <h3>🎵 语音转文字</h3>
                
                <div v-if="transcriptLoading" class="loading-area">
                  <el-icon class="is-loading" style="font-size: 32px;"><Loading /></el-icon>
                  <p>正在转录音频内容，请稍候...</p>
                </div>
                
                <div v-else-if="transcriptText" class="transcript-result">
                  <h4>转录结果</h4>
                  <el-input
                    v-model="transcriptText"
                    type="textarea"
                    :rows="10"
                    placeholder="转录的文本内容"
                  />
                </div>
                
                <div v-else class="transcript-start">
                  <p>点击开始按钮，将录音内容转换为文字</p>
                  <el-button type="primary" size="large" @click="startTranscript">
                    开始语音转文字
                  </el-button>
                </div>

                <div class="step-actions">
                  <el-button @click="prevStep">上一步</el-button>
                  <el-button 
                    type="primary" 
                    @click="nextStep"
                    :disabled="!transcriptText"
                  >
                    下一步：AI分析
                  </el-button>
                </div>
              </div>

              <!-- 步骤3: AI分析 -->
              <div class="step-panel" v-show="currentStep === 2">
                <h3>🤖 AI重点整理生成</h3>
                
                <div v-if="aiLoading" class="loading-area">
                  <el-icon class="is-loading" style="font-size: 32px;"><Loading /></el-icon>
                  <p>AI正在分析课堂内容，生成重点整理...</p>
                </div>
                
                <div v-else-if="aiSummary" class="ai-result">
                  <h4>AI生成的重点整理</h4>
                  <div class="summary-preview" v-html="renderMarkdown(aiSummary)"></div>
                </div>
                
                <div v-else class="ai-start">
                  <p>基于转录文本和课件内容，AI将生成结构化的课堂重点整理</p>
                  <el-button type="primary" size="large" @click="startAIAnalysis">
                    开始AI分析
                  </el-button>
                </div>

                <div class="step-actions">
                  <el-button @click="prevStep">上一步</el-button>
                  <el-button 
                    type="primary" 
                    @click="nextStep"
                    :disabled="!aiSummary"
                  >
                    下一步：编辑发布
                  </el-button>
                </div>
              </div>

              <!-- 步骤4: 编辑发布 -->
              <div class="step-panel" v-show="currentStep === 3">
                <h3>✏️ 编辑和发布</h3>
                
                <el-tabs v-model="activeTab" class="edit-tabs">
                  <el-tab-pane label="📝 编辑文档" name="edit">
                    <el-input
                      v-model="finalContent"
                      type="textarea"
                      :rows="15"
                      placeholder="在此编辑最终的课堂重点整理文档（支持Markdown格式）"
                    />
                  </el-tab-pane>
                  <el-tab-pane label="👀 预览效果" name="preview">
                    <div class="summary-preview" v-html="renderMarkdown(finalContent)"></div>
                  </el-tab-pane>
                </el-tabs>

                <div class="step-actions">
                  <el-button @click="prevStep">上一步</el-button>
                  <el-button @click="saveDraft" :loading="saving">
                    💾 保存草稿
                  </el-button>
                  <el-button type="success" @click="publishSummary" :loading="publishing">
                    🚀 发布到学生端
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>
          
          <!-- 未选择状态 -->
          <el-card v-else class="no-selection">
            <div class="empty-state">
              <h3>请选择一个课堂总结开始处理</h3>
              <p>在左侧列表中选择课堂总结，或创建新的课堂总结</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 创建课堂总结对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建课堂总结"
      width="500px"
    >
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="createForm.courseId" placeholder="选择课程" style="width: 100%">
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课堂标题" prop="title">
          <el-input v-model="createForm.title" placeholder="请输入课堂标题" />
        </el-form-item>
        <el-form-item label="课堂描述" prop="description">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入课堂描述"
          />
        </el-form-item>
        <el-form-item label="关键词" prop="keywords">
          <el-input
            v-model="createForm.keywords"
            placeholder="请输入关键词，用逗号分隔"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="createClassSummary" :loading="creating">
            创建
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import classSummaryApi from '@/api/classSummary'
import { getCoursesByTeacherId } from '@/api/course'

export default {
  name: 'ClassroomSummary',
  components: {
    Loading
  },
  setup() {
    // 基础数据
    const teacherId = ref(2) // 当前教师ID
    const statistics = ref({})
    const summaryList = ref([])
    const selectedSummary = ref(null)
    const currentStep = ref(0)
    const courseList = ref([])

    // 录音相关
    const isRecording = ref(false)
    const mediaRecorder = ref(null)
    const recordedBlob = ref(null)
    const recordedAudioUrl = ref('')
    const recordingDuration = ref(0)
    const recordingTimer = ref(null)

    // 处理状态
    const transcriptLoading = ref(false)
    const aiLoading = ref(false)
    const saving = ref(false)
    const publishing = ref(false)
    const creating = ref(false)

    // 内容数据
    const coursewareContent = ref('')
    const transcriptText = ref('')
    const aiSummary = ref('')
    const finalContent = ref('')

    // UI状态
    const showCreateDialog = ref(false)
    const activeTab = ref('edit')

    // 表单数据
    const createForm = reactive({
      courseId: '',
      title: '',
      description: '',
      keywords: ''
    })

    const createRules = {
      courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
      title: [{ required: true, message: '请输入课堂标题', trigger: 'blur' }],
      description: [{ required: true, message: '请输入课堂描述', trigger: 'blur' }]
    }

    const createFormRef = ref(null)

    // 计算属性
    const hasAudioFile = computed(() => {
      return selectedSummary.value?.audioFilePath || recordedBlob.value
    })

    // 工具方法
    const getStatusType = (status) => {
      const types = {
        'DRAFT': '',
        'REVIEWING': 'warning',
        'PUBLISHED': 'success'
      }
      return types[status] || ''
    }

    const getStatusText = (status) => {
      const texts = {
        'DRAFT': '草稿',
        'REVIEWING': '审核中',
        'PUBLISHED': '已发布'
      }
      return texts[status] || status
    }

    const formatDate = (dateString) => {
      if (!dateString) return ''
      return new Date(dateString).toLocaleString('zh-CN')
    }

    const renderMarkdown = (content) => {
      if (!content) return ''
      // 简单的markdown渲染
      return content
        .replace(/### (.*)/g, '<h3>$1</h3>')
        .replace(/## (.*)/g, '<h2>$1</h2>')
        .replace(/# (.*)/g, '<h1>$1</h1>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>')
        .replace(/`(.*?)`/g, '<code>$1</code>')
        .replace(/\n/g, '<br>')
    }

    // 数据加载方法
    const loadStatistics = async () => {
      try {
        const response = await classSummaryApi.getSummaryStatistics(teacherId.value)
        if (response.success) {
          statistics.value = response.data
        }
      } catch (error) {
        console.error('加载统计信息失败:', error)
      }
    }

    const loadSummaryList = async () => {
      try {
        const response = await classSummaryApi.getTeacherSummaries(teacherId.value)
        if (response.success) {
          summaryList.value = response.data
        }
      } catch (error) {
        console.error('加载课堂总结列表失败:', error)
      }
    }

    const loadCourseList = async () => {
      try {
        const response = await getCoursesByTeacherId(teacherId.value)
        if (response.success) {
          courseList.value = response.data
        } else {
          console.error('API返回错误:', response.msg)
        }
      } catch (error) {
        console.error('加载课程列表失败:', error)
      }
    }

    // 核心业务方法
    const selectSummary = (summary) => {
      selectedSummary.value = summary
      transcriptText.value = summary.transcriptText || ''
      aiSummary.value = summary.summaryContent || ''
      finalContent.value = summary.finalContent || summary.summaryContent || ''
      
      // 根据进度设置当前步骤
      if (!summary.audioFilePath) {
        currentStep.value = 0
      } else if (!summary.transcriptText) {
        currentStep.value = 1
      } else if (!summary.summaryContent) {
        currentStep.value = 2
      } else {
        currentStep.value = 3
      }
    }

    const createClassSummary = async () => {
      try {
        await createFormRef.value.validate()
        creating.value = true
        
        const response = await classSummaryApi.createClassSummary({
          ...createForm,
          teacherId: teacherId.value
        })
        
        if (response.success) {
          ElMessage.success('创建课堂总结成功')
          showCreateDialog.value = false
          Object.assign(createForm, {
            courseId: '',
            title: '',
            description: '',
            keywords: ''
          })
          await loadSummaryList()
          await loadStatistics()
        } else {
          ElMessage.error(response.msg || '创建失败')
        }
      } catch (error) {
        console.error('创建课堂总结失败:', error)
        ElMessage.error('创建失败')
      } finally {
        creating.value = false
      }
    }

    // 录音功能
    const startRecording = async () => {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
        mediaRecorder.value = new MediaRecorder(stream)
        const chunks = []
        
        mediaRecorder.value.ondataavailable = (event) => {
          chunks.push(event.data)
        }
        
        mediaRecorder.value.onstop = () => {
          recordedBlob.value = new Blob(chunks, { type: 'audio/webm' })
          recordedAudioUrl.value = URL.createObjectURL(recordedBlob.value)
          stream.getTracks().forEach(track => track.stop())
        }
        
        mediaRecorder.value.start()
        isRecording.value = true
        recordingDuration.value = 0
        
        recordingTimer.value = setInterval(() => {
          recordingDuration.value++
        }, 1000)
        
        ElMessage.success('开始录音')
      } catch (error) {
        console.error('录音失败:', error)
        ElMessage.error('录音功能不可用，请检查麦克风权限')
      }
    }

    const stopRecording = () => {
      if (mediaRecorder.value && isRecording.value) {
        mediaRecorder.value.stop()
        isRecording.value = false
        clearInterval(recordingTimer.value)
        ElMessage.success('录音已停止')
      }
    }

    const toggleRecording = () => {
      if (isRecording.value) {
        stopRecording()
      } else {
        startRecording()
      }
    }

    const uploadRecordedAudio = async () => {
      if (!recordedBlob.value) return
      
      try {
        const file = new File([recordedBlob.value], 'recording.webm', { 
          type: 'audio/webm' 
        })
        
        const response = await classSummaryApi.uploadAudioFile(file, selectedSummary.value.courseId)
        if (response.success) {
          selectedSummary.value.audioFilePath = response.data
          selectedSummary.value.audioDuration = recordingDuration.value
          ElMessage.success('录音上传成功')
        } else {
          ElMessage.error('录音上传失败')
        }
      } catch (error) {
        console.error('录音上传失败:', error)
        ElMessage.error('录音上传失败')
      }
    }

    // 文件上传
    const beforeAudioUpload = (file) => {
      const isAudio = file.type.startsWith('audio/')
      const isLt100M = file.size / 1024 / 1024 < 100
      
      if (!isAudio) {
        ElMessage.error('只能上传音频文件')
        return false
      }
      if (!isLt100M) {
        ElMessage.error('文件大小不能超过100MB')
        return false
      }
      return true
    }

    const customAudioUpload = async (options) => {
      try {
        const response = await classSummaryApi.uploadAudioFile(options.file, selectedSummary.value.courseId)
        if (response.success) {
          selectedSummary.value.audioFilePath = response.data
          ElMessage.success('音频文件上传成功')
        } else {
          ElMessage.error('音频文件上传失败')
        }
      } catch (error) {
        console.error('音频文件上传失败:', error)
        ElMessage.error('音频文件上传失败')
      }
    }

    // 语音转文字
    const startTranscript = async () => {
      if (!selectedSummary.value.audioFilePath) {
        ElMessage.error('请先上传录音文件')
        return
      }
      
      transcriptLoading.value = true
      try {
        const response = await classSummaryApi.processAudioToText(
          selectedSummary.value.id,
          selectedSummary.value.audioFilePath
        )
        
        if (response.success) {
          transcriptText.value = response.data
          selectedSummary.value.transcriptText = response.data
          ElMessage.success('语音转文字完成')
        } else {
          ElMessage.error('语音转文字失败')
        }
      } catch (error) {
        console.error('语音转文字失败:', error)
        ElMessage.error('语音转文字失败')
      } finally {
        transcriptLoading.value = false
      }
    }

    // AI分析
    const startAIAnalysis = async () => {
      if (!transcriptText.value) {
        ElMessage.error('请先完成语音转文字')
        return
      }
      
      aiLoading.value = true
      try {
        const response = await classSummaryApi.generateSummaryWithAI(
          selectedSummary.value.id,
          transcriptText.value,
          coursewareContent.value
        )
        
        if (response.success) {
          aiSummary.value = response.data
          finalContent.value = response.data
          selectedSummary.value.summaryContent = response.data
          ElMessage.success('AI重点整理生成完成')
        } else {
          ElMessage.error('AI分析失败')
        }
      } catch (error) {
        console.error('AI分析失败:', error)
        ElMessage.error('AI分析失败')
      } finally {
        aiLoading.value = false
      }
    }

    // 保存和发布
    const saveDraft = async () => {
      saving.value = true
      try {
        const response = await classSummaryApi.updateFinalContent(
          selectedSummary.value.id,
          finalContent.value
        )
        
        if (response.success) {
          selectedSummary.value.finalContent = finalContent.value
          ElMessage.success('保存成功')
        } else {
          ElMessage.error('保存失败')
        }
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败')
      } finally {
        saving.value = false
      }
    }

    const publishSummary = async () => {
      publishing.value = true
      try {
        const response = await classSummaryApi.publishClassSummary(
          selectedSummary.value.id,
          finalContent.value
        )
        
        if (response.success) {
          selectedSummary.value.status = 'PUBLISHED'
          selectedSummary.value.publishTime = new Date().toISOString()
          ElMessage.success('发布成功')
          await loadSummaryList()
          await loadStatistics()
        } else {
          ElMessage.error('发布失败')
        }
      } catch (error) {
        console.error('发布失败:', error)
        ElMessage.error('发布失败')
      } finally {
        publishing.value = false
      }
    }

    // 步骤控制
    const nextStep = () => {
      if (currentStep.value < 3) {
        currentStep.value++
      }
    }

    const prevStep = () => {
      if (currentStep.value > 0) {
        currentStep.value--
      }
    }

    // 其他操作
    const editSummary = (summary) => {
      selectSummary(summary)
      currentStep.value = 3
    }

    const viewPublished = (summary) => {
      // 这里可以打开新窗口查看发布的内容
      window.open(`/student/class-summary/${summary.id}`, '_blank')
    }

    const confirmDelete = async (summary) => {
      try {
        await ElMessageBox.confirm(
          '确定要删除这个课堂总结吗？此操作不可恢复。',
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        const response = await classSummaryApi.deleteClassSummary(summary.id)
        if (response.success) {
          ElMessage.success('删除成功')
          await loadSummaryList()
          await loadStatistics()
          if (selectedSummary.value?.id === summary.id) {
            selectedSummary.value = null
          }
        } else {
          ElMessage.error('删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          ElMessage.error('删除失败')
        }
      }
    }

    // 生命周期
    onMounted(() => {
      loadStatistics()
      loadSummaryList()
      loadCourseList()
    })

    return {
      // 数据
      statistics,
      summaryList,
      selectedSummary,
      currentStep,
      courseList,
      
      // 录音
      isRecording,
      recordedBlob,
      recordedAudioUrl,
      recordingDuration,
      
      // 状态
      transcriptLoading,
      aiLoading,
      saving,
      publishing,
      creating,
      
      // 内容
      coursewareContent,
      transcriptText,
      aiSummary,
      finalContent,
      
      // UI
      showCreateDialog,
      activeTab,
      createForm,
      createRules,
      createFormRef,
      
      // 计算属性
      hasAudioFile,
      
      // 方法
      getStatusType,
      getStatusText,
      formatDate,
      renderMarkdown,
      selectSummary,
      createClassSummary,
      toggleRecording,
      uploadRecordedAudio,
      beforeAudioUpload,
      customAudioUpload,
      startTranscript,
      startAIAnalysis,
      saveDraft,
      publishSummary,
      nextStep,
      prevStep,
      editSummary,
      viewPublished,
      confirmDelete
    }
  }
}
</script>

<style scoped>
.classroom-summary {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.subtitle {
  color: #909399;
  margin: 0;
}

/* 统计卡片 */
.stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stat-icon {
  font-size: 32px;
  margin-right: 16px;
  width: 50px;
  text-align: center;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* 主要内容 */
.main-content {
  min-height: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 总结列表 */
.summary-list {
  max-height: 600px;
  overflow-y: auto;
}

.summary-item {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s;
  background: white;
}

.summary-item:hover {
  border-color: #409eff;
  transform: translateY(-1px);
}

.summary-item.active {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.summary-header h4 {
  margin: 0;
  color: #303133;
}

.summary-desc {
  color: #606266;
  font-size: 14px;
  margin: 8px 0;
  line-height: 1.4;
}

.summary-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 12px;
}

.summary-actions {
  display: flex;
  gap: 8px;
}

/* 处理面板 */
.process-card {
  min-height: 600px;
}

.process-steps {
  margin-bottom: 30px;
}

.step-content {
  min-height: 400px;
}

.step-panel h3 {
  color: #303133;
  margin-bottom: 20px;
}

/* 录音区域 */
.recording-section,
.upload-section,
.courseware-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.recording-section h4,
.upload-section h4,
.courseware-section h4 {
  margin-bottom: 12px;
  color: #303133;
}

.recorder-controls {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.recording-indicator {
  color: #f56c6c;
  font-weight: bold;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.recorded-audio {
  margin-top: 16px;
}

/* 加载状态 */
.loading-area {
  text-align: center;
  padding: 60px;
  color: #909399;
}

.loading-area p {
  margin-top: 16px;
  font-size: 16px;
}

/* 预览区域 */
.summary-preview {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 20px;
  min-height: 400px;
  line-height: 1.6;
}

.summary-preview h1,
.summary-preview h2,
.summary-preview h3,
.summary-preview h4 {
  color: #303133;
  margin-top: 20px;
  margin-bottom: 12px;
}

.summary-preview h1 {
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 8px;
}

.summary-preview code {
  background: #f1f1f1;
  padding: 2px 6px;
  border-radius: 3px;
}

/* 步骤操作 */
.step-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px;
  color: #909399;
}

.no-selection {
  height: 600px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.edit-tabs {
  margin-bottom: 20px;
}

/* 对话框 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style> 