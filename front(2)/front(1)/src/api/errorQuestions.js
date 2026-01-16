import request from '@/utils/request'

/**
 * 获取学生的历史错题分析
 * @param {number} studentId 学生ID
 */
export const getErrorQuestionAnalysis = (studentId) => {
  return request.get(`/api/error-questions/analysis/${studentId}`)
}

/**
 * 分析特定错题
 * @param {number} studentId 学生ID
 * @param {number} questionId 题目ID
 */
export const analyzeSpecificErrorQuestion = (studentId, questionId) => {
  return request.get(`/api/error-questions/analysis/${studentId}/${questionId}`)
}

/**
 * 生成相似题目训练
 * @param {object} params 参数
 * @param {number} params.studentId 学生ID
 * @param {number} params.originalQuestionId 原始错题ID
 * @param {number} params.questionCount 生成题目数量
 */
export const generateSimilarQuestions = (params) => {
  return request.post('/api/error-questions/training/similar', params)
}

/**
 * 根据知识点生成训练题目
 * @param {object} params 参数
 * @param {number} params.studentId 学生ID
 * @param {string} params.knowledgePoint 知识点
 * @param {number} params.questionCount 生成题目数量
 */
export const generateKnowledgePointQuestions = (params) => {
  return request.post('/api/error-questions/training/knowledge-point', params)
}

/**
 * 生成综合错题训练
 * @param {object} params 参数
 * @param {number} params.studentId 学生ID
 * @param {number} params.questionCount 生成题目数量
 */
export const generateComprehensiveTraining = (params) => {
  return request.post('/api/error-questions/training/comprehensive', params)
}

/**
 * 获取学生错题统计
 * @param {number} studentId 学生ID
 */
export const getErrorQuestionStatistics = (studentId) => {
  return request.get(`/api/error-questions/statistics/${studentId}`)
}

/**
 * 获取学生错题分布情况
 * @param {number} studentId 学生ID
 */
export const getErrorQuestionDistribution = (studentId) => {
  return request.get(`/api/error-questions/distribution/${studentId}`)
}

/**
 * 评估训练效果
 * @param {object} params 参数
 * @param {number} params.studentId 学生ID
 * @param {number} params.trainingId 训练ID
 * @param {Array<string>} params.answers 学生答案
 */
export const evaluateTrainingEffect = (params) => {
  return request.post('/api/error-questions/training/evaluate', params)
} 