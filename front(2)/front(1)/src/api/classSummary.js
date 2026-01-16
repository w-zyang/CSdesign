import request from '../utils/request'

const BASE_URL = '/api/class-summary'

/**
 * 课堂重点整理API接口
 */
export default {
  /**
   * 创建课堂总结
   */
  createClassSummary(data) {
    return request({
      url: `${BASE_URL}/create`,
      method: 'post',
      data
    })
  },

  /**
   * 上传录音文件
   */
  uploadAudioFile(audioFile, courseId) {
    const formData = new FormData()
    formData.append('audioFile', audioFile)
    formData.append('courseId', courseId)
    
    return request({
      url: `${BASE_URL}/upload-audio`,
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      timeout: 300000 // 5分钟超时，适应大文件上传
    })
  },

  /**
   * 处理录音（语音转文字）
   */
  processAudioToText(summaryId, audioFilePath) {
    return request({
      url: `${BASE_URL}/process-audio`,
      method: 'post',
      data: {
        summaryId,
        audioFilePath
      },
      timeout: 180000 // 3分钟超时，语音识别需要时间
    })
  },

  /**
   * 生成AI重点整理
   */
  generateSummaryWithAI(summaryId, transcriptText, coursewareContent = '') {
    return request({
      url: `${BASE_URL}/generate-summary`,
      method: 'post',
      data: {
        summaryId,
        transcriptText,
        coursewareContent
      },
      timeout: 120000 // 2分钟超时，AI生成需要时间
    })
  },

  /**
   * 更新最终文档内容
   */
  updateFinalContent(summaryId, finalContent) {
    return request({
      url: `${BASE_URL}/update-content/${summaryId}`,
      method: 'put',
      data: {
        finalContent
      }
    })
  },

  /**
   * 发布课堂总结
   */
  publishClassSummary(summaryId) {
    return request({
      url: `${BASE_URL}/publish/${summaryId}`,
      method: 'put'
    })
  },

  /**
   * 获取教师的课堂总结列表
   */
  getTeacherSummaries(teacherId) {
    return request({
      url: `${BASE_URL}/teacher/${teacherId}`,
      method: 'get'
    })
  },

  /**
   * 获取课程的已发布总结列表
   */
  getPublishedSummaries(courseId) {
    return request({
      url: `${BASE_URL}/course/${courseId}`,
      method: 'get'
    })
  },

  /**
   * 获取所有已发布的总结（学生端）
   */
  getAllPublishedSummaries() {
    return request({
      url: `${BASE_URL}/published`,
      method: 'get'
    })
  },

  /**
   * 获取课堂总结详情
   */
  getSummaryDetail(summaryId) {
    return request({
      url: `${BASE_URL}/detail/${summaryId}`,
      method: 'get'
    })
  },

  /**
   * 搜索已发布的课堂总结
   */
  searchPublishedSummaries(keyword) {
    return request({
      url: `${BASE_URL}/search`,
      method: 'get',
      params: {
        keyword
      }
    })
  },

  /**
   * 删除课堂总结
   */
  deleteClassSummary(summaryId, teacherId) {
    return request({
      url: `${BASE_URL}/delete/${summaryId}`,
      method: 'delete',
      params: {
        teacherId
      }
    })
  },

  /**
   * 获取课堂总结统计信息
   */
  getSummaryStatistics(teacherId) {
    return request({
      url: `${BASE_URL}/statistics/${teacherId}`,
      method: 'get'
    })
  }
} 