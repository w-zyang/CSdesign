<template>
  <div class="document-viewer">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <h3>{{ resourceTitle }}</h3>
        <span class="file-info">{{ fileName }}</span>
      </div>
      <div class="toolbar-center">
        <!-- 标注工具 -->
        <div class="annotation-tools">
          <button 
            v-for="tool in annotationTools" 
            :key="tool.type"
            @click="selectTool(tool.type)"
            :class="['tool-btn', { active: selectedTool === tool.type }]"
            :title="tool.name"
          >
            <i :class="tool.icon"></i>
            <span>{{ tool.name }}</span>
          </button>
        </div>
      </div>
      <div class="toolbar-right">
        <div class="zoom-controls">
          <button @click="zoomOut" class="zoom-btn">
            <i class="fas fa-search-minus"></i>
          </button>
          <span class="zoom-level">{{ Math.round(zoomLevel * 100) }}%</span>
          <button @click="zoomIn" class="zoom-btn">
            <i class="fas fa-search-plus"></i>
          </button>
        </div>
        <button @click="toggleNotesPanel" class="notes-toggle" :class="{ active: showNotesPanel }">
          <i class="fas fa-sticky-note"></i>
          笔记面板
        </button>
        <button @click="toggleAIPanel" class="ai-toggle" :class="{ active: showAIPanel }">
          <i class="fas fa-robot"></i>
          AI助手
        </button>
        <button @click="enterVRClassroom" class="vr-toggle">
          <i class="fas fa-vr-cardboard"></i>
          进入VR教室
        </button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="viewer-container">
      <!-- 文档预览区域 -->
      <div class="document-area" :class="{ 'with-notes-panel': showNotesPanel, 'with-ai-panel': showAIPanel }">
        <div class="document-wrapper" :style="{ transform: `scale(${zoomLevel})` }">
          <!-- 文档内容 -->
          <div 
            ref="documentContent"
            class="document-content"
            @mouseup="handleTextSelection"
            @click="handleDocumentClick"
          >
            <!-- TensorFlow Lite教程内容 -->
            <div class="document-page">
              <h1>项目8 TensorFlow Lite</h1>
              
              <h2>项目描述</h2>
              <p>TensorFlow生态系统有着丰富的工具链，TensorFlow Serving是使用广泛的高性能的服务器端部署平台，TensorFlow.js支持使用JavaScript在浏览器端部署，<span class="highlight-target">TensorFlow Lite是一个轻量、快速、兼容度高的专门针对移动式应用场景的深度学习工具</span>，它支持Android、IOS、嵌入式设备、以及极小的MCU设备。<span class="underline-target">全球有超过40亿的设备上部署着TFLite</span>，例如Google Assistant，Google Photos等、Uber、Airbnb、以及国内的许多大公司如网易、爱奇艺和WPS等，都在使用TFLite。</p>
              
              <h2>项目目标</h2>
              <h3>知识目标</h3>
              <ul>
                <li>了解TensorFlow Lite的发展历史</li>
                <li>了解TensorFlow Lite的应用</li>
                <li>掌握TensorFlow Lite的整体架构</li>
                <li>掌握TensorFlow Lite转换器作用</li>
                <li>掌握FlatBuffers格式</li>
                <li>掌握TensorFlow Lite解释执行器特点及工作过程</li>
              </ul>
              
              <h3>技能目标</h3>
              <ul>
                <li>能通过相应工具将模型转化</li>
                <li>能在Android应用中部署转换后的模型</li>
                <li>能熟练Android Studio</li>
                <li>能配置build.gradle构建项目</li>
                <li>能熟练掌握迁移学习改造模型，开发相应AI应用</li>
              </ul>

              <h2>8.1 认识TensorFlow Lite</h2>
              <p>2015年底Google开源了端到端的机器学习开源框架TensorFlow，它既支持大规模的模型训练，也支持各种环境的部署，包括服务器和移动端的部署，支持各种语言，包括Python，C++，Java，Swift甚至Javascript。而近年来移动化浪潮和交互方式的改变，使得机器学习技术开发也在朝着轻量化的端侧发展，TensorFlow团队又在2017年底上线了TensorFlow Lite，把移动端及IoT设备端的深度学习技术的门槛再次大大降低。</p>

              <h3>8.1.1 TensorFlow Lite发展历史</h3>
              <p>TFLite是在边缘设备上运行TensorFlow模型推理的官方框架，它跨平台运行，包括Android、iOS以及基于Linux的IoT设备和微控制器。伴随移动和IoT设备的普及，世界以超乎想象的方式存在被连接的可能，如今已有超过32亿的手机用户和70亿的联网IoT设备。</p>

              <p>基于TF Mobile的经验，也继承了TFMini和内部其他类似项目的很多优秀工作，Google设计了TFLite：</p>
              <ol>
                <li><span class="comment-target">TensorFlow Lite 二进制文件的大小约为 1 MB</span>（针对 32 位 ARM build）；如果仅使用支持常见图像分类模型（InceptionV3 和 MobileNet）所需的运算符，TensorFlow Lite 二进制文件的大小不到 300 KB。</li>
                <li>特别为各种端侧设备优化的算子库。</li>
                <li>能够利用各种硬件加速。</li>
              </ol>

              <h2>8.2 TensorFlow Lite体系结构</h2>
              <p>TensorFlow Lite 是一组工具，可帮助开发者在移动设备、嵌入式设备和 IoT 设备上运行 TensorFlow 模型。它支持设备端机器学习推断，延迟较低，并且二进制文件很小。</p>

              <h3>8.2.1 TensorFlow Lite整体架构</h3>
              <p>TensorFlow Lite 包括两个主要组件：</p>
              <ul>
                <li><span class="highlight-target">TensorFlow Lite 解释器(Interpreter)</span></li>
                <li><span class="highlight-target">TensorFlow Lite 转换器(Converter)</span></li>
                <li>算子库(Op kernels)</li>
                <li>硬件加速代理(Hardware accelerator delegate)</li>
              </ul>

              <h3>8.2.2 模型优化</h3>
              <p><span class="underline-target">量化使用了一些技术，可以降低权重的精确表示</span>，并且可选的降低存储和计算的激活值。量化的好处有：</p>
              <ul>
                <li>对现有 CPU 平台的支持</li>
                <li>激活值得的量化降低了用于读取和存储中间激活值的存储器访问成本</li>
                <li>许多 CPU 和硬件加速器实现提供 SIMD 指令功能，这对量化特别有益</li>
              </ul>

              <h2>8.3 任务1：TensorFlow Lite开发工作流程</h2>
              <p>使用 TensorFlow Lite 的<span class="highlight-target">工作流程包括如下步骤：选择模型、转换模型、部署到设备、优化模型</span>。</p>

              <h3>8.3.1 选择模型</h3>
              <p><span class="comment-target">MobileNet V2是基于一个流线型的架构</span>，它使用深度可分离的卷积来构建轻量级的深层神经网。可用于图像分类任务，比如猫狗分类、花卉分类等等。</p>

              <h3>8.3.2 模型转换</h3>
              <p>TensorFlow Lite转换器将输入的TensorFlow 模型生成 TensorFlow Lite 模型，一种优化的 FlatBuffer 格式，以 .tflite 为文件扩展名。</p>
              <p>在 TFLiteConverter 中有以下的类方法：</p>
              <ul>
                <li><span class="underline-target">tf.lite.TFLiteConverter.from_saved_model()</span>：用来转换 SavedModel 格式模型。</li>
                <li>TFLiteConverter.from_keras_model()：用来转换 tf.keras 模型。</li>
                <li>TFLiteConverter.from_concrete_functions()：用来转换 concrete functions。</li>
              </ul>

              <h3>8.3.3 模型推理</h3>
              <p>TensorFlow Lite 解释器接收一个模型文件，执行模型文件在输入数据上定义的运算符，输出推理结果。如果手机有GPU，GPU 比 CPU 执行更快的浮点矩阵运算，速度提升能有显著效果。例如在有GPU加速的手机上运行MobileNet图像分类，<span class="comment-target">模型运行速度可以提高 5.5 倍</span>。</p>

              <h2>8.4 任务2：实现花卉识别</h2>
              <p>下面将使用TensorFlow Lite实现花卉识别app，在Android设备上运行图像识别模型MobileNets_v2来识别花卉。本项目实施步骤如下：</p>
              <ol>
                <li>通过迁移学习实现花卉识别模型</li>
                <li>使用TFLite转换器转换模型</li>
                <li>在Android应用中使用TFLite解释器运行它</li>
                <li>使用 TensorFlow Lite支持库预处理模型输入和后处理模型输出</li>
              </ol>

              <h3>8.4.1 选择模型</h3>
              <p>选择MobileNet V2进行迁移学习，实现识别花卉模型。通过微调进一步提高性能，调整预训练模型的顶层权重，以便模型学习特定于数据集的高级特征。</p>

              <h3>8.4.2 Android部署</h3>
              <p>我们已经使用MobileNet V2 创建、训练和导出了自定义TensorFlow Lite模型，接下来将在手机端部署，运行一个使用该模型识别花卉图片的Android 应用。</p>
              
              <h4>准备工作</h4>
              <p>安装Android Studio，确认Android Studio版本 3.0+以上。要使用tensorflow lite需要导入对应的库，这里通过修改build.gradle来实现。</p>
              
              <h4>配置build.gradle</h4>
              <p>在dependencies下增加'org.tensorflow:tensorflow-lite:+'，在android下增加 aaptOptions，以防止Android在生成应用程序二进制文件时压缩TensorFlow Lite模型文件。</p>
            </div>
          </div>

          <!-- 标注层 -->
          <div class="annotations-layer">
            <div 
              v-for="annotation in annotations" 
              :key="annotation.id"
              :class="['annotation-mark', annotation.type]"
              :style="getAnnotationStyle(annotation)"
              @click="editAnnotation(annotation)"
            >
              <div v-if="annotation.type === 'sticky-note'" class="sticky-note">
                <i class="fas fa-sticky-note"></i>
              </div>
            </div>
          </div>

          <!-- 选择工具提示 -->
          <div 
            v-if="showSelectionTooltip" 
            class="selection-tooltip"
            :style="{ top: tooltipPosition.y + 'px', left: tooltipPosition.x + 'px' }"
          >
            <button @click="createHighlight" class="tooltip-btn highlight">
              <i class="fas fa-highlighter"></i>
            </button>
            <button @click="createUnderline" class="tooltip-btn underline">
              <i class="fas fa-underline"></i>
            </button>
            <button @click="createComment" class="tooltip-btn comment">
              <i class="fas fa-comment"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- 笔记面板 -->
      <div v-if="showNotesPanel" class="notes-panel">
        <div class="notes-header">
          <h4><i class="fas fa-list"></i> 我的标注</h4>
          <div class="notes-stats">
            <span>共 {{ annotations.length }} 个标注</span>
          </div>
        </div>
        
        <div class="notes-list">
          <!-- 没有标注时的提示 -->
          <div v-if="annotations.length === 0" class="empty-annotations">
            <div class="empty-icon">
              <i class="fas fa-bookmark"></i>
            </div>
            <div class="empty-text">
              <h4>暂无标注</h4>
              <p>选择文档中的文字，使用工具栏创建标注</p>
              <div class="empty-tips">
                <div class="tip-item">
                  <i class="fas fa-highlighter"></i>
                  <span>高亮重要内容</span>
                </div>
                <div class="tip-item">
                  <i class="fas fa-underline"></i>
                  <span>下划线标记</span>
                </div>
                <div class="tip-item">
                  <i class="fas fa-comment"></i>
                  <span>添加文字批注</span>
                </div>
                <div class="tip-item">
                  <i class="fas fa-sticky-note"></i>
                  <span>创建便签</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 标注列表 -->
          <div 
            v-for="annotation in annotations" 
            :key="annotation.id"
            :class="['note-item', annotation.type]"
            @click="jumpToAnnotation(annotation)"
          >
            <div class="note-header">
              <div class="note-type">
                <i :class="getAnnotationIcon(annotation.type)"></i>
                <span>{{ getAnnotationTypeName(annotation.type) }}</span>
              </div>
              <div class="note-actions">
                <button @click.stop="editAnnotation(annotation)" class="action-btn">
                  <i class="fas fa-edit"></i>
                </button>
                <button @click.stop="deleteAnnotation(annotation.id)" class="action-btn">
                  <i class="fas fa-trash"></i>
                </button>
              </div>
            </div>
            
            <div v-if="annotation.selectedText" class="note-text">
              "{{ annotation.selectedText }}"
            </div>
            
            <div v-if="annotation.comment" class="note-comment">
              {{ annotation.comment }}
            </div>
            
            <div class="note-meta">
              <span>第{{ annotation.pageNumber }}页</span>
              <span>{{ formatDate(annotation.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- AI助手面板 -->
      <div v-if="showAIPanel" class="ai-panel">
        <div class="ai-header">
          <h4><i class="fas fa-robot"></i> AI助手</h4>
          <div class="ai-tabs">
            <button 
              :class="['tab-btn', { active: activeTab === 'chat' }]" 
              @click="activeTab = 'chat'"
            >
              <i class="fas fa-comments"></i>
              文件提问
            </button>
            <button 
              :class="['tab-btn', { active: activeTab === 'mindmap' }]" 
              @click="activeTab = 'mindmap'"
            >
              <i class="fas fa-project-diagram"></i>
              思维导图
            </button>
          </div>
        </div>
        
        <!-- 文件提问面板 -->
        <div v-show="activeTab === 'chat'" class="chat-panel">
          <div class="chat-messages" ref="chatMessages">
            <div 
              v-for="message in chatMessages" 
              :key="message.id"
              :class="['message', message.type]"
            >
              <div class="message-content">
                <div class="message-text">{{ message.text }}</div>
                <div class="message-time">{{ formatTime(message.timestamp) }}</div>
              </div>
            </div>
          </div>
          
          <div class="chat-input">
            <div class="input-group">
              <input 
                v-model="chatInput" 
                @keyup.enter="sendMessage"
                placeholder="请输入关于此文档的问题..."
                class="chat-input-field"
              />
              <button 
                @click="sendMessage" 
                :disabled="!chatInput.trim() || isLoading"
                class="send-btn"
              >
                <i v-if="isLoading" class="fas fa-spinner fa-spin"></i>
                <i v-else class="fas fa-paper-plane"></i>
              </button>
            </div>
          </div>
        </div>
        
        <!-- 思维导图面板 -->
        <div v-show="activeTab === 'mindmap'" class="mindmap-panel">
          <div class="mindmap-controls">
                         <button 
               @click="generateMindmapFunc" 
               :disabled="isGeneratingMindmap"
               class="generate-btn"
             >
              <i v-if="isGeneratingMindmap" class="fas fa-spinner fa-spin"></i>
              <i v-else class="fas fa-magic"></i>
              生成思维导图
            </button>
            <button 
              v-if="mindmapData"
              @click="exportMindmap"
              class="export-btn"
            >
              <i class="fas fa-download"></i>
              导出
            </button>
          </div>
          
          <div class="mindmap-container">
            <div v-if="!mindmapData && !isGeneratingMindmap" class="mindmap-placeholder">
              <i class="fas fa-project-diagram"></i>
              <p>点击"生成思维导图"按钮，基于当前文档内容生成思维导图</p>
            </div>
            
            <div v-if="isGeneratingMindmap" class="mindmap-loading">
              <i class="fas fa-spinner fa-spin"></i>
              <p>正在生成思维导图...</p>
            </div>
            
            <div v-if="mindmapData" class="mindmap-viewer" v-html="mindmapData"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 标注编辑模态框 -->
    <div v-if="showAnnotationModal" class="modal-overlay" @click.self="closeAnnotationModal">
      <div class="annotation-modal">
        <div class="modal-header">
          <h3>编辑标注</h3>
          <button @click="closeAnnotationModal" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>标注类型</label>
            <select v-model="annotationForm.type">
              <option value="highlight">高亮标记</option>
              <option value="underline">下划线</option>
              <option value="comment">文字批注</option>
              <option value="sticky-note">便签</option>
            </select>
          </div>
          
          <div class="form-group">
            <label>标注颜色</label>
            <div class="color-picker">
              <button 
                v-for="color in annotationColors"
                :key="color.value"
                @click="annotationForm.color = color.value"
                :class="['color-option', { active: annotationForm.color === color.value }]"
                :style="{ backgroundColor: color.value }"
                :title="color.name"
              ></button>
            </div>
          </div>
          
          <div class="form-group">
            <label>备注说明</label>
            <textarea 
              v-model="annotationForm.comment"
              placeholder="添加您的注释..."
              rows="4"
            ></textarea>
          </div>
          
          <div class="modal-actions">
            <button @click="closeAnnotationModal" class="btn btn-secondary">取消</button>
            <button @click="saveAnnotation" class="btn btn-primary">保存</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { 
  createAnnotation, 
  updateAnnotation, 
  deleteAnnotation as deleteAnnotationApi,
  getResourceAnnotations 
} from '@/api/notes'
import { chatWithDocument, generateMindmap } from '@/api/ai'

const router = useRouter()

// Props
const props = defineProps({
  resourceId: {
    type: Number,
    required: true
  },
  resourceTitle: {
    type: String,
    default: '文档'
  },
  fileName: {
    type: String,
    default: '未知文件'
  }
})

// 响应式数据
const documentContent = ref(null)
const annotations = ref([])
const selectedText = ref('')
const selectedRange = ref(null)
const zoomLevel = ref(1)
const showNotesPanel = ref(true)
const showAIPanel = ref(false)
const showSelectionTooltip = ref(false)
const showAnnotationModal = ref(false)
const selectedTool = ref('highlight')
const currentAnnotation = ref(null)

// AI助手相关数据
const activeTab = ref('chat')
const chatMessages = ref([])
const chatInput = ref('')
const isLoading = ref(false)
const mindmapData = ref('')
const isGeneratingMindmap = ref(false)
const messageIdCounter = ref(0)

// 获取学生ID
const studentId = ref(localStorage.getItem('userId') || 17)

// 位置数据
const tooltipPosition = reactive({ x: 0, y: 0 })

// 表单数据
const annotationForm = reactive({
  type: 'highlight',
  color: '#FFFF00',
  comment: '',
  selectedText: '',
  pageNumber: 1,
  position: null
})

// 配置数据
const annotationTools = [
  { type: 'highlight', name: '高亮', icon: 'fas fa-highlighter' },
  { type: 'underline', name: '下划线', icon: 'fas fa-underline' },
  { type: 'comment', name: '批注', icon: 'fas fa-comment' },
  { type: 'sticky-note', name: '便签', icon: 'fas fa-sticky-note' }
]

const annotationColors = [
  { name: '黄色', value: '#FFFF00' },
  { name: '绿色', value: '#90EE90' },
  { name: '蓝色', value: '#87CEEB' },
  { name: '粉色', value: '#FFB6C1' },
  { name: '橙色', value: '#FFA500' }
]

// 方法
const loadAnnotations = async () => {
  try {
    const response = await getResourceAnnotations(studentId.value, props.resourceId)
    if (response.success) {
      annotations.value = response.data
    }
  } catch (error) {
    console.error('加载标注失败:', error)
  }
}

const handleTextSelection = (event) => {
  const selection = window.getSelection()
  if (selection.rangeCount > 0 && selection.toString().trim()) {
    selectedText.value = selection.toString().trim()
    selectedRange.value = selection.getRangeAt(0)
    
    // 显示选择工具提示
    showSelectionTooltip.value = true
    tooltipPosition.x = event.pageX
    tooltipPosition.y = event.pageY - 50
  } else {
    hideSelectionTooltip()
  }
}

const handleDocumentClick = (event) => {
  if (selectedTool.value === 'sticky-note') {
    createStickyNoteAt(event.pageX, event.pageY)
  }
}

const hideSelectionTooltip = () => {
  showSelectionTooltip.value = false
  selectedText.value = ''
  selectedRange.value = null
}

const createHighlight = () => {
  createAnnotationWithSelection('highlight')
}

const createUnderline = () => {
  createAnnotationWithSelection('underline')
}

const createComment = () => {
  createAnnotationWithSelection('comment')
}

const createStickyNoteAt = (x, y) => {
  const rect = documentContent.value.getBoundingClientRect()
  const relativeX = x - rect.left
  const relativeY = y - rect.top
  
  const annotation = {
    studentId: parseInt(studentId.value),
    resourceId: props.resourceId,
    type: 'sticky-note',
    selectedText: '',
    pageNumber: 1,
    position: JSON.stringify({ x: relativeX, y: relativeY }),
    color: '#FFFF00',
    comment: '点击编辑便签内容...',
    importance: 3
  }
  
  saveNewAnnotation(annotation)
}

const createAnnotationWithSelection = (type) => {
  if (!selectedText.value || !selectedRange.value) return
  
  const selection = window.getSelection()
  const range = selectedRange.value
  
  // 获取选中文本在文档中的位置
  const rect = range.getBoundingClientRect()
  const containerRect = documentContent.value.getBoundingClientRect()
  
  // 计算相对于文档容器的位置（考虑缩放）
  const relativePosition = {
    left: (rect.left - containerRect.left) / zoomLevel.value,
    top: (rect.top - containerRect.top) / zoomLevel.value,
    width: rect.width / zoomLevel.value,
    height: rect.height / zoomLevel.value,
    // 保存文本位置信息用于精确定位
    startOffset: range.startOffset,
    endOffset: range.endOffset,
    startContainer: range.startContainer.nodeType === Node.TEXT_NODE ? 
      range.startContainer.parentNode.tagName : range.startContainer.tagName,
    endContainer: range.endContainer.nodeType === Node.TEXT_NODE ? 
      range.endContainer.parentNode.tagName : range.endContainer.tagName,
    // 新增：基于文本内容的定位
    textBefore: getTextBefore(range.startContainer, range.startOffset),
    textAfter: getTextAfter(range.endContainer, range.endOffset),
    paragraphIndex: getParagraphIndex(range.startContainer)
  }
  
  const annotation = {
    studentId: parseInt(studentId.value),
    resourceId: props.resourceId,
    type: type,
    selectedText: selectedText.value,
    pageNumber: 1,
    position: JSON.stringify(relativePosition),
    color: getDefaultColor(type),
    comment: '',
    importance: 3
  }
  
  saveNewAnnotation(annotation)
  hideSelectionTooltip()
}

// 新增：获取文本前后的上下文，用于精确定位
const getTextBefore = (container, offset) => {
  const textNode = container.nodeType === Node.TEXT_NODE ? container : container.childNodes[0]
  if (!textNode || textNode.nodeType !== Node.TEXT_NODE) return ''
  
  const text = textNode.textContent
  const start = Math.max(0, offset - 20)
  return text.substring(start, offset)
}

const getTextAfter = (container, offset) => {
  const textNode = container.nodeType === Node.TEXT_NODE ? container : container.childNodes[0]
  if (!textNode || textNode.nodeType !== Node.TEXT_NODE) return ''
  
  const text = textNode.textContent
  const end = Math.min(text.length, offset + 20)
  return text.substring(offset, end)
}

const getParagraphIndex = (container) => {
  let element = container.nodeType === Node.TEXT_NODE ? container.parentNode : container
  while (element && element !== documentContent.value) {
    if (element.tagName === 'P' || element.tagName === 'LI' || element.tagName === 'H1' || element.tagName === 'H2') {
      const allParagraphs = documentContent.value.querySelectorAll('p, li, h1, h2, h3, h4, h5, h6')
      return Array.from(allParagraphs).indexOf(element)
    }
    element = element.parentNode
  }
  return 0
}

const saveNewAnnotation = async (annotation) => {
  try {
    const response = await createAnnotation(annotation)
    if (response.success) {
      annotations.value.push(response.data)
      ElMessage.success('标注创建成功')
    }
  } catch (error) {
    console.error('创建标注失败:', error)
    ElMessage.error('创建标注失败')
  }
}

const getDefaultColor = (type) => {
  const colorMap = {
    'highlight': '#FFFF00',
    'underline': '#FF0000',
    'comment': '#00FF00',
    'sticky-note': '#FFB6C1'
  }
  return colorMap[type] || '#FFFF00'
}

const getAnnotationStyle = (annotation) => {
  try {
    const position = JSON.parse(annotation.position)
    
    if (annotation.type === 'sticky-note') {
      return {
        position: 'absolute',
        left: (position.x || position.left) + 'px',
        top: (position.y || position.top) + 'px',
        zIndex: 10,
        transform: `scale(${zoomLevel.value})`
      }
    } else {
      // 尝试基于文本内容重新定位
      const repositionedStyle = repositionAnnotation(annotation)
      if (repositionedStyle) {
        return repositionedStyle
      }
      
      // 降级到原始位置（考虑缩放）
      return {
        position: 'absolute',
        left: (position.left * zoomLevel.value) + 'px',
        top: (position.top * zoomLevel.value) + 'px',
        width: (position.width * zoomLevel.value) + 'px',
        height: (position.height * zoomLevel.value) + 'px',
        backgroundColor: annotation.color,
        opacity: 0.3,
        pointerEvents: 'auto',
        zIndex: 5
      }
    }
  } catch (error) {
    console.error('解析标注位置失败:', error)
    return { display: 'none' }
  }
}

// 新增：基于文本内容重新定位标注
const repositionAnnotation = (annotation) => {
  try {
    const position = JSON.parse(annotation.position)
    
    // 如果有段落索引，尝试基于段落重新定位
    if (position.paragraphIndex !== undefined) {
      const allParagraphs = documentContent.value.querySelectorAll('p, li, h1, h2, h3, h4, h5, h6')
      const targetParagraph = allParagraphs[position.paragraphIndex]
      
      if (targetParagraph) {
        // 在段落中查找匹配的文本
        const textContent = targetParagraph.textContent
        const selectedText = annotation.selectedText
        
        if (textContent.includes(selectedText)) {
          const startIndex = textContent.indexOf(selectedText)
          const endIndex = startIndex + selectedText.length
          
          // 创建临时范围来获取准确位置
          const range = document.createRange()
          const textNode = targetParagraph.childNodes[0]
          
          if (textNode && textNode.nodeType === Node.TEXT_NODE) {
            try {
              range.setStart(textNode, startIndex)
              range.setEnd(textNode, endIndex)
              
              const rect = range.getBoundingClientRect()
              const containerRect = documentContent.value.getBoundingClientRect()
              
              return {
                position: 'absolute',
                left: ((rect.left - containerRect.left) * zoomLevel.value) + 'px',
                top: ((rect.top - containerRect.top) * zoomLevel.value) + 'px',
                width: (rect.width * zoomLevel.value) + 'px',
                height: (rect.height * zoomLevel.value) + 'px',
                backgroundColor: annotation.color,
                opacity: 0.3,
                pointerEvents: 'auto',
                zIndex: 5
              }
            } catch (rangeError) {
              console.warn('无法创建文本范围:', rangeError)
            }
          }
        }
      }
    }
    
    return null
  } catch (error) {
    console.error('重新定位标注失败:', error)
    return null
  }
}

const selectTool = (tool) => {
  selectedTool.value = tool
}

const zoomIn = () => {
  zoomLevel.value = Math.min(zoomLevel.value + 0.25, 3)
}

const zoomOut = () => {
  zoomLevel.value = Math.max(zoomLevel.value - 0.25, 0.5)
}

const toggleNotesPanel = () => {
  showNotesPanel.value = !showNotesPanel.value
}

const toggleAIPanel = () => {
  showAIPanel.value = !showAIPanel.value
}

// AI助手相关方法
const sendMessage = async () => {
  if (!chatInput.value.trim()) return
  
  const userMessage = {
    id: messageIdCounter.value++,
    type: 'user',
    text: chatInput.value,
    timestamp: new Date()
  }
  
  chatMessages.value.push(userMessage)
  
  const question = chatInput.value
  chatInput.value = ''
  isLoading.value = true
  
  try {
    // 获取文档内容
    const documentText = getDocumentText()
    
    const response = await chatWithDocument({
      question: question,
      documentContent: documentText,
      resourceId: props.resourceId
    })
    
    const aiMessage = {
      id: messageIdCounter.value++,
      type: 'ai',
      text: response.data.answer,
      timestamp: new Date()
    }
    
    chatMessages.value.push(aiMessage)
    
    // 滚动到底部
    nextTick(() => {
      const messagesEl = document.querySelector('.chat-messages')
      if (messagesEl) {
        messagesEl.scrollTop = messagesEl.scrollHeight
      }
    })
    
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败，请重试')
  } finally {
    isLoading.value = false
  }
}

const generateMindmapFunc = async () => {
  isGeneratingMindmap.value = true
  
  try {
    const documentText = getDocumentText()
    
    const response = await generateMindmap({
      documentContent: documentText,
      resourceId: props.resourceId
    })
    
    mindmapData.value = response.data.mindmapHtml
    ElMessage.success('思维导图生成成功')
    
  } catch (error) {
    console.error('生成思维导图失败:', error)
    ElMessage.error('生成思维导图失败，请重试')
  } finally {
    isGeneratingMindmap.value = false
  }
}

const exportMindmap = () => {
  if (!mindmapData.value) return
  
  const blob = new Blob([mindmapData.value], { type: 'text/html' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${props.resourceTitle || '文档'}_思维导图.html`
  a.click()
  URL.revokeObjectURL(url)
}

const getDocumentText = () => {
  // 简化实现，获取文档内容
  return documentContent.value?.innerText || ''
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const enterVRClassroom = () => {
  // 跳转到VR教室
  router.push('/student/vr-classroom')
  ElMessage.success('正在进入VR虚拟教室...')
}

const editAnnotation = (annotation) => {
  currentAnnotation.value = annotation
  annotationForm.type = annotation.type
  annotationForm.color = annotation.color
  annotationForm.comment = annotation.comment
  annotationForm.selectedText = annotation.selectedText
  showAnnotationModal.value = true
}

const closeAnnotationModal = () => {
  showAnnotationModal.value = false
  currentAnnotation.value = null
}

const saveAnnotation = async () => {
  try {
    if (currentAnnotation.value) {
      const updatedData = {
        ...currentAnnotation.value,
        type: annotationForm.type,
        color: annotationForm.color,
        comment: annotationForm.comment
      }
      
      const response = await updateAnnotation(currentAnnotation.value.id, updatedData)
      if (response.success) {
        const index = annotations.value.findIndex(a => a.id === currentAnnotation.value.id)
        if (index !== -1) {
          annotations.value[index] = response.data
        }
        ElMessage.success('标注更新成功')
      }
    }
    closeAnnotationModal()
  } catch (error) {
    console.error('保存标注失败:', error)
    ElMessage.error('保存标注失败')
  }
}

const deleteAnnotation = async (annotationId) => {
  try {
    await deleteAnnotationApi(annotationId)
    annotations.value = annotations.value.filter(a => a.id !== annotationId)
    ElMessage.success('标注删除成功')
  } catch (error) {
    console.error('删除标注失败:', error)
    ElMessage.error('删除标注失败')
  }
}

const jumpToAnnotation = (annotation) => {
  // 简化实现，滚动到顶部
  documentContent.value.scrollIntoView({ behavior: 'smooth' })
}

const getAnnotationIcon = (type) => {
  const icons = {
    'highlight': 'fas fa-highlighter',
    'underline': 'fas fa-underline',
    'comment': 'fas fa-comment',
    'sticky-note': 'fas fa-sticky-note'
  }
  return icons[type] || 'fas fa-marker'
}

const getAnnotationTypeName = (type) => {
  const names = {
    'highlight': '高亮',
    'underline': '下划线',
    'comment': '批注',
    'sticky-note': '便签'
  }
  return names[type] || type
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 生命周期
onMounted(() => {
  loadAnnotations()
})
</script>

<style scoped>
.document-viewer {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.toolbar {
  background: white;
  border-bottom: 1px solid #e0e0e0;
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.toolbar-left h3 {
  margin: 0 0 4px 0;
  color: #2c3e50;
  font-size: 1.2rem;
}

.file-info {
  color: #6c757d;
  font-size: 0.9rem;
}

.annotation-tools {
  display: flex;
  gap: 8px;
}

.tool-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border: 2px solid #dee2e6;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.tool-btn:hover {
  border-color: #3498db;
  background: #f8f9fa;
}

.tool-btn.active {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.zoom-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.zoom-btn {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  padding: 6px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.zoom-btn:hover {
  background: #e9ecef;
}

.zoom-level {
  font-weight: 600;
  color: #495057;
  min-width: 50px;
  text-align: center;
}

.notes-toggle {
  background: #17a2b8;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.notes-toggle:hover {
  background: #138496;
}

.notes-toggle.active {
  background: #0f6674;
}

.ai-toggle {
  background: #9b59b6;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 8px;
}

.ai-toggle:hover {
  background: #8e44ad;
}

.ai-toggle.active {
  background: #7d3c98;
}

.vr-toggle {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 8px;
}

.vr-toggle:hover {
  background: #c0392b;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.3);
}

/* 主要内容区域 */
.viewer-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.document-area {
  flex: 1;
  overflow: auto;
  position: relative;
  transition: all 0.3s ease;
}

.document-area.with-notes-panel {
  flex: 0 0 70%;
}

.document-area.with-ai-panel {
  flex: 0 0 60%;
}

.document-area.with-notes-panel.with-ai-panel {
  flex: 0 0 40%;
}

.document-wrapper {
  padding: 20px;
  transform-origin: top left;
  transition: transform 0.3s ease;
}

.document-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  position: relative;
  user-select: text;
}

.document-page {
  padding: 40px;
  line-height: 1.8;
  color: #2c3e50;
}

.document-page h1, .document-page h2 {
  color: #2c3e50;
  margin-top: 2rem;
  margin-bottom: 1rem;
}

.document-page h1 {
  font-size: 2rem;
  border-bottom: 2px solid #3498db;
  padding-bottom: 0.5rem;
}

.document-page h2 {
  font-size: 1.5rem;
  color: #3498db;
}

.document-page ul {
  margin: 1rem 0;
  padding-left: 2rem;
}

.document-page li {
  margin: 0.5rem 0;
}

/* 标注层 */
.annotations-layer {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.annotation-mark {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 2px;
}

.annotation-mark:hover {
  opacity: 0.6 !important;
}

.sticky-note {
  background: #ffeb3b;
  border-radius: 4px;
  padding: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
  cursor: pointer;
  font-size: 16px;
  color: #f57f17;
}

/* 选择工具提示 */
.selection-tooltip {
  position: absolute;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  padding: 8px;
  display: flex;
  gap: 4px;
  z-index: 1000;
}

.tooltip-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tooltip-btn.highlight {
  background: #fff59d;
  color: #f57f17;
}

.tooltip-btn.underline {
  background: #ffcdd2;
  color: #d32f2f;
}

.tooltip-btn.comment {
  background: #c8e6c9;
  color: #388e3c;
}

.tooltip-btn:hover {
  transform: scale(1.1);
}

/* 笔记面板 */
.notes-panel {
  width: 30%;
  background: white;
  border-left: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
}

.notes-header {
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
}

.notes-header h4 {
  margin: 0 0 10px 0;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 8px;
}

.notes-stats {
  color: #6c757d;
  font-size: 0.9rem;
}

.notes-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

/* 空状态提示样式 */
.empty-annotations {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
  color: #6c757d;
  height: 400px;
}

.empty-icon {
  font-size: 48px;
  color: #dee2e6;
  margin-bottom: 20px;
}

.empty-text h4 {
  margin: 0 0 8px 0;
  color: #495057;
  font-size: 16px;
}

.empty-text p {
  margin: 0 0 24px 0;
  color: #6c757d;
  font-size: 14px;
}

.empty-tips {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  font-size: 13px;
  color: #495057;
}

.tip-item i {
  width: 16px;
  color: #6c757d;
}

.note-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-left: 4px solid #3498db;
}

.note-item:hover {
  background: #e9ecef;
  transform: translateY(-1px);
}

.note-item.highlight {
  border-left-color: #ffeb3b;
}

.note-item.underline {
  border-left-color: #f44336;
}

.note-item.comment {
  border-left-color: #4caf50;
}

.note-item.sticky-note {
  border-left-color: #ff9800;
}

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.note-type {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.9rem;
  font-weight: 600;
  color: #495057;
}

.note-actions {
  display: flex;
  gap: 4px;
}

.action-btn {
  background: none;
  border: none;
  color: #6c757d;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: #dee2e6;
  color: #495057;
}

.note-text {
  background: white;
  padding: 8px;
  border-radius: 6px;
  font-style: italic;
  margin-bottom: 8px;
  font-size: 0.9rem;
  color: #495057;
}

.note-comment {
  color: #6c757d;
  font-size: 0.9rem;
  margin-bottom: 8px;
}

.note-meta {
  display: flex;
  gap: 12px;
  font-size: 0.8rem;
  color: #adb5bd;
}

/* AI助手面板样式 */
.ai-panel {
  width: 30%;
  background: white;
  border-left: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
}

.ai-header {
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
}

.ai-header h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 8px;
}

.ai-tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border: 2px solid #dee2e6;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.tab-btn:hover {
  border-color: #9b59b6;
  background: #f8f9fa;
}

.tab-btn.active {
  background: #9b59b6;
  color: white;
  border-color: #9b59b6;
}

/* 聊天面板样式 */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.message.user {
  justify-content: flex-end;
}

.message.user .message-content {
  background: #9b59b6;
  color: white;
  border-radius: 18px 18px 4px 18px;
  max-width: 80%;
}

.message.ai {
  justify-content: flex-start;
}

.message.ai .message-content {
  background: #f8f9fa;
  color: #2c3e50;
  border-radius: 18px 18px 18px 4px;
  max-width: 80%;
  border: 1px solid #e9ecef;
}

.message-content {
  padding: 12px 16px;
  word-wrap: break-word;
}

.message-text {
  font-size: 0.9rem;
  line-height: 1.4;
  margin-bottom: 4px;
}

.message-time {
  font-size: 0.75rem;
  opacity: 0.7;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid #e9ecef;
}

.input-group {
  display: flex;
  gap: 8px;
}

.chat-input-field {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 0.9rem;
  outline: none;
  transition: border-color 0.3s ease;
}

.chat-input-field:focus {
  border-color: #9b59b6;
}

.send-btn {
  padding: 10px 12px;
  background: #9b59b6;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-btn:hover {
  background: #8e44ad;
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 思维导图面板样式 */
.mindmap-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.mindmap-controls {
  padding: 16px;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  gap: 8px;
}

.generate-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.generate-btn:hover {
  background: #2980b9;
}

.generate-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.export-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.export-btn:hover {
  background: #229954;
}

.mindmap-container {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

.mindmap-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6c757d;
  text-align: center;
}

.mindmap-placeholder i {
  font-size: 48px;
  margin-bottom: 16px;
  color: #dee2e6;
}

.mindmap-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6c757d;
}

.mindmap-loading i {
  font-size: 32px;
  margin-bottom: 16px;
}

.mindmap-viewer {
  width: 100%;
  height: 100%;
  min-height: 400px;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  overflow: auto;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.annotation-modal {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #6c757d;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #2c3e50;
  font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

.color-picker {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.color-option {
  width: 32px;
  height: 32px;
  border: 3px solid transparent;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
}

.color-option.active {
  border-color: #2c3e50;
  transform: scale(1.2);
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 20px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover {
  background: #2980b9;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    gap: 12px;
    padding: 16px;
  }
  
  .annotation-tools {
    justify-content: center;
  }
  
  .viewer-container {
    flex-direction: column;
  }
  
  .document-area.with-notes-panel {
    flex: 1;
  }
  
  .notes-panel {
    width: 100%;
    max-height: 40vh;
  }
}

/* 标注目标样式 - 用于视觉指示预期标注位置 */
.highlight-target {
  background-color: rgba(255, 255, 0, 0.1);
  border: 1px dashed rgba(255, 255, 0, 0.3);
  padding: 1px 2px;
  border-radius: 2px;
  position: relative;
}

.highlight-target::after {
  content: "📝";
  position: absolute;
  right: -20px;
  top: -5px;
  font-size: 12px;
  opacity: 0.6;
}

.underline-target {
  text-decoration: underline;
  text-decoration-color: rgba(255, 0, 0, 0.3);
  text-decoration-style: dashed;
  position: relative;
}

.underline-target::after {
  content: "📎";
  position: absolute;
  right: -20px;
  top: -5px;
  font-size: 12px;
  opacity: 0.6;
}

.comment-target {
  background-color: rgba(0, 255, 0, 0.1);
  border-left: 3px solid rgba(0, 255, 0, 0.3);
  padding-left: 5px;
  position: relative;
}

.comment-target::after {
  content: "💬";
  position: absolute;
  right: -20px;
  top: -5px;
  font-size: 12px;
  opacity: 0.6;
}
</style> 