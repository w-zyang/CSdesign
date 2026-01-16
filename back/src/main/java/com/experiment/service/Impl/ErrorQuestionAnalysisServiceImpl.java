package com.experiment.service.Impl;

import com.experiment.mapper.StudentExamMapper;
import com.experiment.mapper.QuestionMapper;
import com.experiment.mapper.StudentAnswerMapper;
import com.experiment.pojo.ErrorQuestionAnalysisDTO;
import com.experiment.pojo.ErrorQuestionTrainingDTO;
import com.experiment.pojo.StudentExam;
import com.experiment.pojo.Question;
import com.experiment.service.ErrorQuestionAnalysisService;
import com.experiment.service.AIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ErrorQuestionAnalysisServiceImpl implements ErrorQuestionAnalysisService {

    @Autowired
    private StudentExamMapper studentExamMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private StudentAnswerMapper studentAnswerMapper;
    
    @Autowired
    private AIService aiService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<ErrorQuestionAnalysisDTO> analyzeStudentErrorQuestions(Long studentId) {
        log.info("开始分析学生 {} 的历史错题", studentId);
        
        List<ErrorQuestionAnalysisDTO> errorAnalysisList = new ArrayList<>();
        
        try {
            // 从数据库查询学生的所有错题
            List<Map<String, Object>> errorQuestions = studentAnswerMapper.selectErrorQuestionsByStudentId(studentId);
            
            log.info("学生 {} 共有 {} 道错题记录", studentId, errorQuestions.size());
            
            // 转换为DTO对象
            for (Map<String, Object> errorData : errorQuestions) {
                ErrorQuestionAnalysisDTO analysis = new ErrorQuestionAnalysisDTO();
                
                // 基本信息
                analysis.setQuestionId(getLongValue(errorData.get("question_id")));
                analysis.setQuestionContent((String) errorData.get("question_content"));
                analysis.setQuestionType((String) errorData.get("question_type"));
                analysis.setKnowledgePoint((String) errorData.get("knowledge_point"));
                analysis.setDifficulty((String) errorData.get("difficulty"));
                analysis.setCorrectAnswer((String) errorData.get("correct_answer"));
                analysis.setStudentAnswer((String) errorData.get("student_answer"));
                
                // 统计信息
                int errorCount = getIntValue(errorData.get("error_count"));
                int totalAttempts = getIntValue(errorData.get("total_attempts"));
                double errorRate = totalAttempts > 0 ? (errorCount * 100.0 / totalAttempts) : 0.0;
                
                analysis.setErrorCount(errorCount);
                analysis.setErrorRate(errorRate);
                analysis.setLastErrorTime((LocalDateTime) errorData.get("last_error_time"));
                
                // 分析错误类型和原因
                String errorType = analyzeErrorType(analysis.getQuestionType(), 
                    analysis.getStudentAnswer(), analysis.getCorrectAnswer());
                analysis.setErrorType(errorType);
                analysis.setErrorReason(generateErrorReason(errorType, analysis.getKnowledgePoint()));
                
                // 生成改进建议
                analysis.setImprovementSuggestion(generateImprovementSuggestion(
                    analysis.getKnowledgePoint(), errorType, errorRate));
                
                // 提取相关概念（从知识点推导）
                analysis.setRelatedConcepts(extractRelatedConcepts(analysis.getKnowledgePoint()));
                
                // 设置主题（从知识点推导）
                analysis.setTopic(extractTopic(analysis.getKnowledgePoint()));
                
                errorAnalysisList.add(analysis);
            }
            
            log.info("学生 {} 的错题分析完成，共分析 {} 道错题", studentId, errorAnalysisList.size());
        } catch (Exception e) {
            log.error("分析学生错题失败", e);
        }
        
        return errorAnalysisList;
    }

    @Override
    public ErrorQuestionAnalysisDTO analyzeSpecificErrorQuestion(Long studentId, Long questionId) {
        log.info("分析学生 {} 的特定错题 {}", studentId, questionId);
        
        try {
            // 查询题目信息
            Question question = questionMapper.selectById(questionId);
            if (question == null) {
                log.warn("题目 {} 不存在", questionId);
                return null;
            }
            
            // 查询学生对该题的错误记录
            List<Map<String, Object>> errorRecords = studentAnswerMapper.selectErrorRecordsByStudentAndQuestion(studentId, questionId);
            if (errorRecords.isEmpty()) {
                log.warn("学生 {} 没有题目 {} 的错误记录", studentId, questionId);
                return null;
            }
            
            // 统计错误次数和总次数
            Integer errorCount = studentAnswerMapper.countErrorsByStudentAndQuestion(studentId, questionId);
            Integer totalAttempts = studentAnswerMapper.countTotalByStudentAndQuestion(studentId, questionId);
            double errorRate = totalAttempts > 0 ? (errorCount * 100.0 / totalAttempts) : 0.0;
            
            // 获取最近一次错误记录
            Map<String, Object> latestError = errorRecords.get(0);
            
            // 构建分析DTO
            ErrorQuestionAnalysisDTO analysis = new ErrorQuestionAnalysisDTO();
            analysis.setQuestionId(questionId);
            analysis.setQuestionContent(question.getContent());
            analysis.setQuestionType(question.getType());
            analysis.setKnowledgePoint(question.getKnowledgePoint());
            analysis.setDifficulty(question.getDifficulty());
            analysis.setCorrectAnswer(question.getAnswer());
            analysis.setStudentAnswer((String) latestError.get("student_answer"));
            analysis.setErrorCount(errorCount);
            analysis.setErrorRate(errorRate);
            analysis.setLastErrorTime((LocalDateTime) latestError.get("create_time"));
            
            // 分析错误类型和原因
            String errorType = analyzeErrorType(question.getType(), 
                analysis.getStudentAnswer(), analysis.getCorrectAnswer());
            analysis.setErrorType(errorType);
            analysis.setErrorReason(generateErrorReason(errorType, question.getKnowledgePoint()));
            
            // 生成改进建议
            analysis.setImprovementSuggestion(generateImprovementSuggestion(
                question.getKnowledgePoint(), errorType, errorRate));
            
            // 提取相关概念
            analysis.setRelatedConcepts(extractRelatedConcepts(question.getKnowledgePoint()));
            
            // 设置主题
            analysis.setTopic(extractTopic(question.getKnowledgePoint()));
            
            return analysis;
        } catch (Exception e) {
            log.error("分析特定错题失败", e);
            return null;
        }
    }

    @Override
    public ErrorQuestionTrainingDTO generateSimilarQuestions(Long studentId, Long originalQuestionId, Integer questionCount) {
        log.info("为学生 {} 生成相似题目训练，原题ID: {}, 数量: {}", studentId, originalQuestionId, questionCount);
        
        try {
            // 获取原题信息
            ErrorQuestionAnalysisDTO originalError = analyzeSpecificErrorQuestion(studentId, originalQuestionId);
            
            if (originalError == null) {
                throw new RuntimeException("无法获取原题信息");
            }
            
            // 使用AI生成相似题目
            String prompt = buildSimilarQuestionPrompt(originalError, questionCount);
            String aiResponse = aiService.chatWithSystem(
                "你是一个专业的教育题目生成专家，擅长根据学生的错题情况生成相似的练习题目。",
                prompt
            );
            
            List<Map<String, Object>> questions = parseQuestionsFromAI(aiResponse);
            
            // 创建训练DTO
            ErrorQuestionTrainingDTO training = new ErrorQuestionTrainingDTO();
            training.setStudentId(studentId);
            training.setTrainingType("similar");
            training.setOriginalQuestionId(originalQuestionId);
            training.setKnowledgePoint(originalError.getKnowledgePoint());
            training.setDifficulty(originalError.getDifficulty());
            training.setQuestionCount(questionCount);
            training.setQuestions(questions);
            training.setAnalysisReport(generateAnalysisReport(originalError, "similar"));
            training.setTargetErrorTypes(Arrays.asList(originalError.getErrorType()));
            training.setExpectedImprovement(75.0);
            training.setCreateTime(LocalDateTime.now());
            
            log.info("成功生成 {} 道相似题目", questions.size());
            return training;
            
        } catch (Exception e) {
            log.error("生成相似题目失败", e);
            // 返回备用题目
            return generateFallbackTraining(studentId, originalQuestionId, questionCount, "similar");
        }
    }

    @Override
    public ErrorQuestionTrainingDTO generateKnowledgePointQuestions(Long studentId, String knowledgePoint, Integer questionCount) {
        log.info("为学生 {} 生成知识点训练题目，知识点: {}, 数量: {}", studentId, knowledgePoint, questionCount);
        
        try {
            // 分析该知识点的历史错题
            List<ErrorQuestionAnalysisDTO> relatedErrors = analyzeStudentErrorQuestions(studentId)
                .stream()
                .filter(error -> knowledgePoint.equals(error.getKnowledgePoint()))
                .collect(Collectors.toList());
            
            // 构建AI提示词
            String prompt = buildKnowledgePointPrompt(knowledgePoint, relatedErrors, questionCount);
            String aiResponse = aiService.chatWithSystem(
                "你是一个专业的教育题目生成专家，擅长根据特定知识点生成有针对性的练习题目。",
                prompt
            );
            
            List<Map<String, Object>> questions = parseQuestionsFromAI(aiResponse);
            
            // 创建训练DTO
            ErrorQuestionTrainingDTO training = new ErrorQuestionTrainingDTO();
            training.setStudentId(studentId);
            training.setTrainingType("knowledge_point");
            training.setKnowledgePoint(knowledgePoint);
            training.setDifficulty("medium");
            training.setQuestionCount(questionCount);
            training.setQuestions(questions);
            training.setAnalysisReport(generateKnowledgePointReport(knowledgePoint, relatedErrors));
            training.setTargetErrorTypes(relatedErrors.stream()
                .map(ErrorQuestionAnalysisDTO::getErrorType)
                .distinct()
                .collect(Collectors.toList()));
            training.setExpectedImprovement(80.0);
            training.setCreateTime(LocalDateTime.now());
            
            log.info("成功生成 {} 道知识点训练题目", questions.size());
            return training;
            
        } catch (Exception e) {
            log.error("生成知识点题目失败", e);
            return generateFallbackTraining(studentId, null, questionCount, "knowledge_point");
        }
    }

    @Override
    public ErrorQuestionTrainingDTO generateComprehensiveTraining(Long studentId, Integer questionCount) {
        log.info("为学生 {} 生成综合错题训练，数量: {}", studentId, questionCount);
        
        try {
            // 获取学生所有错题
            List<ErrorQuestionAnalysisDTO> allErrors = analyzeStudentErrorQuestions(studentId);
            
            if (allErrors.isEmpty()) {
                throw new RuntimeException("未发现错题记录");
            }
            
            // 按错误率排序，选择最需要练习的知识点
            List<String> priorityKnowledgePoints = allErrors.stream()
                .sorted((e1, e2) -> Double.compare(e2.getErrorRate(), e1.getErrorRate()))
                .map(ErrorQuestionAnalysisDTO::getKnowledgePoint)
                .distinct()
                .limit(3)
                .collect(Collectors.toList());
            
            // 构建综合训练提示词
            String prompt = buildComprehensivePrompt(allErrors, priorityKnowledgePoints, questionCount);
            String aiResponse = aiService.chatWithSystem(
                "你是一个专业的教育题目生成专家，擅长根据学生的整体错题情况生成综合性的练习题目。",
                prompt
            );
            
            List<Map<String, Object>> questions = parseQuestionsFromAI(aiResponse);
            
            // 创建训练DTO
            ErrorQuestionTrainingDTO training = new ErrorQuestionTrainingDTO();
            training.setStudentId(studentId);
            training.setTrainingType("comprehensive");
            training.setQuestionCount(questionCount);
            training.setQuestions(questions);
            training.setAnalysisReport(generateComprehensiveReport(allErrors, priorityKnowledgePoints));
            training.setTargetErrorTypes(allErrors.stream()
                .map(ErrorQuestionAnalysisDTO::getErrorType)
                .distinct()
                .collect(Collectors.toList()));
            training.setExpectedImprovement(85.0);
            training.setCreateTime(LocalDateTime.now());
            
            log.info("成功生成 {} 道综合训练题目", questions.size());
            return training;
            
        } catch (Exception e) {
            log.error("生成综合训练题目失败", e);
            return generateFallbackTraining(studentId, null, questionCount, "comprehensive");
        }
    }

    @Override
    public Map<String, Object> getErrorQuestionStatistics(Long studentId) {
        log.info("获取学生 {} 的错题统计", studentId);
        
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            List<ErrorQuestionAnalysisDTO> errorQuestions = analyzeStudentErrorQuestions(studentId);
            
            statistics.put("totalErrorQuestions", errorQuestions.size());
            statistics.put("averageErrorRate", errorQuestions.stream()
                .mapToDouble(ErrorQuestionAnalysisDTO::getErrorRate)
                .average()
                .orElse(0.0));
            
            // 按知识点分组统计
            Map<String, Long> knowledgePointCount = errorQuestions.stream()
                .collect(Collectors.groupingBy(
                    ErrorQuestionAnalysisDTO::getKnowledgePoint,
                    Collectors.counting()
                ));
            statistics.put("knowledgePointDistribution", knowledgePointCount);
            
            // 按题目类型分组统计
            Map<String, Long> questionTypeCount = errorQuestions.stream()
                .collect(Collectors.groupingBy(
                    ErrorQuestionAnalysisDTO::getQuestionType,
                    Collectors.counting()
                ));
            statistics.put("questionTypeDistribution", questionTypeCount);
            
            // 按错误类型分组统计
            Map<String, Long> errorTypeCount = errorQuestions.stream()
                .collect(Collectors.groupingBy(
                    ErrorQuestionAnalysisDTO::getErrorType,
                    Collectors.counting()
                ));
            statistics.put("errorTypeDistribution", errorTypeCount);
            
        } catch (Exception e) {
            log.error("获取错题统计失败", e);
        }
        
        return statistics;
    }

    @Override
    public Map<String, Object> getErrorQuestionDistribution(Long studentId) {
        Map<String, Object> distribution = new HashMap<>();
        
        try {
            List<ErrorQuestionAnalysisDTO> errorQuestions = analyzeStudentErrorQuestions(studentId);
            
            // 难度分布
            Map<String, Long> difficultyDistribution = errorQuestions.stream()
                .collect(Collectors.groupingBy(
                    ErrorQuestionAnalysisDTO::getDifficulty,
                    Collectors.counting()
                ));
            distribution.put("difficultyDistribution", difficultyDistribution);
            
            // 主题分布
            Map<String, Long> topicDistribution = errorQuestions.stream()
                .collect(Collectors.groupingBy(
                    ErrorQuestionAnalysisDTO::getTopic,
                    Collectors.counting()
                ));
            distribution.put("topicDistribution", topicDistribution);
            
            // 时间分布（最近30天）
            Map<String, Long> timeDistribution = errorQuestions.stream()
                .filter(error -> error.getLastErrorTime().isAfter(LocalDateTime.now().minusDays(30)))
                .collect(Collectors.groupingBy(
                    error -> error.getLastErrorTime().toLocalDate().toString(),
                    Collectors.counting()
                ));
            distribution.put("recentTimeDistribution", timeDistribution);
            
        } catch (Exception e) {
            log.error("获取错题分布失败", e);
        }
        
        return distribution;
    }

    @Override
    public Map<String, Object> evaluateTrainingEffect(Long studentId, Long trainingId, List<String> answers) {
        log.info("评估学生 {} 的训练效果，训练ID: {}", studentId, trainingId);
        
        Map<String, Object> evaluation = new HashMap<>();
        
        try {
            // 这里应该根据训练ID获取训练题目，然后评估答案
            // 目前使用模拟评估
            
            int correctCount = 0;
            List<Map<String, Object>> detailedResults = new ArrayList<>();
            
            for (int i = 0; i < answers.size(); i++) {
                String userAnswer = answers.get(i);
                // 简单模拟：假设正确答案是A
                boolean isCorrect = "A".equals(userAnswer);
                if (isCorrect) correctCount++;
                
                Map<String, Object> result = new HashMap<>();
                result.put("questionIndex", i + 1);
                result.put("userAnswer", userAnswer);
                result.put("correctAnswer", "A");
                result.put("isCorrect", isCorrect);
                result.put("errorType", isCorrect ? null : determineErrorType(userAnswer, "A"));
                detailedResults.add(result);
            }
            
            double accuracy = answers.size() > 0 ? (double) correctCount / answers.size() * 100 : 0;
            
            evaluation.put("accuracy", accuracy);
            evaluation.put("correctCount", correctCount);
            evaluation.put("totalQuestions", answers.size());
            evaluation.put("detailedResults", detailedResults);
            evaluation.put("improvementSuggestions", generateImprovementSuggestions(accuracy, detailedResults));
            evaluation.put("nextSteps", generateNextSteps(accuracy));
            
        } catch (Exception e) {
            log.error("评估训练效果失败", e);
        }
        
        return evaluation;
    }

    // 私有辅助方法
    
    /**
     * 获取Long类型值
     */
    private Long getLongValue(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) return Long.parseLong((String) value);
        return null;
    }
    
    /**
     * 获取Integer类型值
     */
    private Integer getIntValue(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof String) return Integer.parseInt((String) value);
        return 0;
    }
    
    /**
     * 分析错误类型
     */
    private String analyzeErrorType(String questionType, String studentAnswer, String correctAnswer) {
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            return "未作答";
        }
        
        // 选择题
        if ("choice".equals(questionType) || "single_choice".equals(questionType)) {
            if (Math.abs(studentAnswer.charAt(0) - correctAnswer.charAt(0)) == 1) {
                return "选项混淆";
            } else {
                return "概念理解错误";
            }
        }
        
        // 填空题
        if ("fill".equals(questionType)) {
            if (studentAnswer.length() < correctAnswer.length() / 2) {
                return "知识点缺失";
            } else {
                return "细节错误";
            }
        }
        
        // 简答题
        if ("short".equals(questionType) || "short_answer".equals(questionType)) {
            if (studentAnswer.length() < 20) {
                return "回答不完整";
            } else {
                return "理解偏差";
            }
        }
        
        // 编程题
        if ("coding".equals(questionType)) {
            return "逻辑错误";
        }
        
        return "答案错误";
    }
    
    /**
     * 生成错误原因
     */
    private String generateErrorReason(String errorType, String knowledgePoint) {
        switch (errorType) {
            case "未作答":
                return "对" + knowledgePoint + "不熟悉，未能作答";
            case "选项混淆":
                return "对" + knowledgePoint + "的相似概念区分不清";
            case "概念理解错误":
                return "对" + knowledgePoint + "的核心概念理解有误";
            case "知识点缺失":
                return knowledgePoint + "相关知识掌握不全面";
            case "细节错误":
                return "对" + knowledgePoint + "的细节把握不准确";
            case "回答不完整":
                return "对" + knowledgePoint + "的理解不够深入";
            case "理解偏差":
                return "对" + knowledgePoint + "的理解存在偏差";
            case "逻辑错误":
                return knowledgePoint + "相关的编程逻辑有误";
            default:
                return "对" + knowledgePoint + "掌握不够扎实";
        }
    }
    
    /**
     * 生成改进建议
     */
    private String generateImprovementSuggestion(String knowledgePoint, String errorType, double errorRate) {
        StringBuilder suggestion = new StringBuilder();
        
        if (errorRate >= 70) {
            suggestion.append("该知识点错误率较高，建议：");
            suggestion.append("1. 系统复习").append(knowledgePoint).append("的基础概念；");
            suggestion.append("2. 多做相关练习题巩固理解；");
            suggestion.append("3. 寻求老师或同学的帮助。");
        } else if (errorRate >= 40) {
            suggestion.append("该知识点掌握一般，建议：");
            suggestion.append("1. 重点复习").append(knowledgePoint).append("的易错点；");
            suggestion.append("2. 通过练习加深理解。");
        } else {
            suggestion.append("该知识点基本掌握，建议：");
            suggestion.append("1. 注意").append(knowledgePoint).append("的细节；");
            suggestion.append("2. 保持练习频率。");
        }
        
        return suggestion.toString();
    }
    
    /**
     * 提取相关概念
     */
    private List<String> extractRelatedConcepts(String knowledgePoint) {
        List<String> concepts = new ArrayList<>();
        
        if (knowledgePoint == null) {
            return concepts;
        }
        
        // 简单的关键词提取（实际应用中可以使用更复杂的NLP技术）
        if (knowledgePoint.contains("数据结构")) {
            concepts.addAll(Arrays.asList("栈", "队列", "链表", "树"));
        } else if (knowledgePoint.contains("算法")) {
            concepts.addAll(Arrays.asList("时间复杂度", "空间复杂度", "排序", "查找"));
        } else if (knowledgePoint.contains("网络")) {
            concepts.addAll(Arrays.asList("协议", "TCP/IP", "HTTP", "Socket"));
        } else if (knowledgePoint.contains("操作系统") || knowledgePoint.contains("Linux")) {
            concepts.addAll(Arrays.asList("进程", "线程", "文件系统", "命令行"));
        } else if (knowledgePoint.contains("数据库")) {
            concepts.addAll(Arrays.asList("SQL", "索引", "事务", "查询优化"));
        } else {
            // 默认添加知识点本身
            concepts.add(knowledgePoint);
        }
        
        return concepts;
    }
    
    /**
     * 提取主题
     */
    private String extractTopic(String knowledgePoint) {
        if (knowledgePoint == null) {
            return "计算机基础";
        }
        
        if (knowledgePoint.contains("数据结构") || knowledgePoint.contains("算法")) {
            return "数据结构与算法";
        } else if (knowledgePoint.contains("网络")) {
            return "计算机网络";
        } else if (knowledgePoint.contains("操作系统") || knowledgePoint.contains("Linux")) {
            return "操作系统";
        } else if (knowledgePoint.contains("数据库")) {
            return "数据库系统";
        } else if (knowledgePoint.contains("编程") || knowledgePoint.contains("Java") || 
                   knowledgePoint.contains("Python") || knowledgePoint.contains("JavaScript")) {
            return "程序设计";
        } else {
            return "计算机基础";
        }
    }

    private String buildSimilarQuestionPrompt(ErrorQuestionAnalysisDTO originalError, Integer questionCount) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下错题信息，生成").append(questionCount).append("道相似的练习题目：\n\n");
        prompt.append("原题内容：").append(originalError.getQuestionContent()).append("\n");
        prompt.append("知识点：").append(originalError.getKnowledgePoint()).append("\n");
        prompt.append("题目类型：").append(originalError.getQuestionType()).append("\n");
        prompt.append("错误类型：").append(originalError.getErrorType()).append("\n");
        prompt.append("错误原因：").append(originalError.getErrorReason()).append("\n\n");
        prompt.append("要求：\n");
        prompt.append("1. 题目应该针对学生的薄弱环节\n");
        prompt.append("2. 保持相同的知识点和难度\n");
        prompt.append("3. 避免完全相同的题目\n");
        prompt.append("4. 返回JSON格式的题目数组\n\n");
        prompt.append("返回格式：\n");
        prompt.append("[{\"title\":\"题目内容\",\"type\":\"").append(originalError.getQuestionType());
        prompt.append("\",\"options\":[\"A. 选项1\",\"B. 选项2\",\"C. 选项3\",\"D. 选项4\"],\"answer\":\"A\",\"explanation\":\"解析\"}]");
        
        return prompt.toString();
    }

    private String buildKnowledgePointPrompt(String knowledgePoint, List<ErrorQuestionAnalysisDTO> relatedErrors, Integer questionCount) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据知识点\"").append(knowledgePoint).append("\"生成").append(questionCount).append("道练习题目：\n\n");
        
        if (!relatedErrors.isEmpty()) {
            prompt.append("学生在该知识点的常见错误：\n");
            for (ErrorQuestionAnalysisDTO error : relatedErrors) {
                prompt.append("- ").append(error.getErrorType()).append("：").append(error.getErrorReason()).append("\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("要求：\n");
        prompt.append("1. 针对上述错误类型设计题目\n");
        prompt.append("2. 覆盖该知识点的核心概念\n");
        prompt.append("3. 题目难度渐进，由易到难\n");
        prompt.append("4. 返回JSON格式的题目数组\n\n");
        prompt.append("返回格式：\n");
        prompt.append("[{\"title\":\"题目内容\",\"type\":\"choice\",\"options\":[\"A. 选项1\",\"B. 选项2\",\"C. 选项3\",\"D. 选项4\"],\"answer\":\"A\",\"explanation\":\"解析\"}]");
        
        return prompt.toString();
    }

    private String buildComprehensivePrompt(List<ErrorQuestionAnalysisDTO> allErrors, List<String> priorityKnowledgePoints, Integer questionCount) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据学生的整体错题情况生成").append(questionCount).append("道综合练习题目：\n\n");
        
        prompt.append("重点知识点：\n");
        for (String kp : priorityKnowledgePoints) {
            prompt.append("- ").append(kp).append("\n");
        }
        prompt.append("\n");
        
        prompt.append("主要错误类型：\n");
        Set<String> errorTypes = allErrors.stream()
            .map(ErrorQuestionAnalysisDTO::getErrorType)
            .collect(Collectors.toSet());
        for (String errorType : errorTypes) {
            prompt.append("- ").append(errorType).append("\n");
        }
        prompt.append("\n");
        
        prompt.append("要求：\n");
        prompt.append("1. 覆盖重点知识点\n");
        prompt.append("2. 针对主要错误类型\n");
        prompt.append("3. 题目类型多样化\n");
        prompt.append("4. 返回JSON格式的题目数组\n\n");
        prompt.append("返回格式：\n");
        prompt.append("[{\"title\":\"题目内容\",\"type\":\"choice\",\"options\":[\"A. 选项1\",\"B. 选项2\",\"C. 选项3\",\"D. 选项4\"],\"answer\":\"A\",\"explanation\":\"解析\"}]");
        
        return prompt.toString();
    }

    private List<Map<String, Object>> parseQuestionsFromAI(String aiResponse) {
        try {
            // 尝试解析AI返回的JSON
            return objectMapper.readValue(aiResponse, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            log.warn("AI返回格式解析失败，使用备用题目: {}", e.getMessage());
            // 返回备用题目
            return generateFallbackQuestions();
        }
    }

    private List<Map<String, Object>> generateFallbackQuestions() {
        List<Map<String, Object>> questions = new ArrayList<>();
        
        Map<String, Object> question1 = new HashMap<>();
        question1.put("title", "Linux系统中，使用哪个命令可以显示当前工作目录？");
        question1.put("type", "choice");
        question1.put("options", Arrays.asList("A. pwd", "B. cd", "C. ls", "D. mkdir"));
        question1.put("answer", "A");
        question1.put("explanation", "pwd命令用于显示当前工作目录的完整路径");
        questions.add(question1);
        
        Map<String, Object> question2 = new HashMap<>();
        question2.put("title", "在数据结构中，哪种结构遵循\"后进先出\"的原则？");
        question2.put("type", "choice");
        question2.put("options", Arrays.asList("A. 栈", "B. 队列", "C. 链表", "D. 数组"));
        question2.put("answer", "A");
        question2.put("explanation", "栈是一种后进先出(LIFO)的数据结构");
        questions.add(question2);
        
        return questions;
    }

    private ErrorQuestionTrainingDTO generateFallbackTraining(Long studentId, Long originalQuestionId, Integer questionCount, String trainingType) {
        ErrorQuestionTrainingDTO training = new ErrorQuestionTrainingDTO();
        training.setStudentId(studentId);
        training.setTrainingType(trainingType);
        training.setOriginalQuestionId(originalQuestionId);
        training.setQuestionCount(questionCount);
        training.setQuestions(generateFallbackQuestions());
        training.setAnalysisReport("由于AI服务暂时不可用，系统生成了备用练习题目。");
        training.setTargetErrorTypes(Arrays.asList("概念理解", "基础知识"));
        training.setExpectedImprovement(60.0);
        training.setCreateTime(LocalDateTime.now());
        
        return training;
    }

    private String generateAnalysisReport(ErrorQuestionAnalysisDTO originalError, String trainingType) {
        StringBuilder report = new StringBuilder();
        report.append("**错题分析报告**\n\n");
        report.append("原题知识点：").append(originalError.getKnowledgePoint()).append("\n");
        report.append("错误类型：").append(originalError.getErrorType()).append("\n");
        report.append("错误原因：").append(originalError.getErrorReason()).append("\n");
        report.append("历史错误次数：").append(originalError.getErrorCount()).append("\n");
        report.append("错误率：").append(originalError.getErrorRate()).append("%\n\n");
        report.append("**训练目标**\n");
        report.append("通过相似题目的练习，帮助您：\n");
        report.append("1. 巩固").append(originalError.getKnowledgePoint()).append("的核心概念\n");
        report.append("2. 纠正").append(originalError.getErrorType()).append("类型的错误\n");
        report.append("3. 提高该知识点的掌握程度\n");
        
        return report.toString();
    }

    private String generateKnowledgePointReport(String knowledgePoint, List<ErrorQuestionAnalysisDTO> relatedErrors) {
        StringBuilder report = new StringBuilder();
        report.append("**知识点专项训练报告**\n\n");
        report.append("目标知识点：").append(knowledgePoint).append("\n");
        report.append("相关错题数量：").append(relatedErrors.size()).append("\n\n");
        
        if (!relatedErrors.isEmpty()) {
            report.append("**主要问题分析**\n");
            Map<String, Long> errorTypeCount = relatedErrors.stream()
                .collect(Collectors.groupingBy(ErrorQuestionAnalysisDTO::getErrorType, Collectors.counting()));
            
            for (Map.Entry<String, Long> entry : errorTypeCount.entrySet()) {
                report.append("- ").append(entry.getKey()).append("：").append(entry.getValue()).append("次\n");
            }
        }
        
        report.append("\n**训练目标**\n");
        report.append("1. 系统掌握").append(knowledgePoint).append("的核心概念\n");
        report.append("2. 解决相关的理解和应用问题\n");
        report.append("3. 提高该知识点的整体掌握水平\n");
        
        return report.toString();
    }

    private String generateComprehensiveReport(List<ErrorQuestionAnalysisDTO> allErrors, List<String> priorityKnowledgePoints) {
        StringBuilder report = new StringBuilder();
        report.append("**综合错题训练报告**\n\n");
        report.append("错题总数：").append(allErrors.size()).append("\n");
        report.append("重点攻克知识点：").append(String.join("、", priorityKnowledgePoints)).append("\n\n");
        
        report.append("**薄弱环节分析**\n");
        for (String kp : priorityKnowledgePoints) {
            long count = allErrors.stream()
                .filter(error -> kp.equals(error.getKnowledgePoint()))
                .count();
            double avgErrorRate = allErrors.stream()
                .filter(error -> kp.equals(error.getKnowledgePoint()))
                .mapToDouble(ErrorQuestionAnalysisDTO::getErrorRate)
                .average()
                .orElse(0.0);
            report.append("- ").append(kp).append("：").append(count).append("道错题，平均错误率").append(String.format("%.1f", avgErrorRate)).append("%\n");
        }
        
        report.append("\n**训练目标**\n");
        report.append("1. 全面提升薄弱知识点的掌握程度\n");
        report.append("2. 减少常见错误类型的发生\n");
        report.append("3. 建立知识点之间的关联和理解\n");
        
        return report.toString();
    }

    private String determineErrorType(String userAnswer, String correctAnswer) {
        // 简单的错误类型判断逻辑
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return "未作答";
        } else if (!userAnswer.equals(correctAnswer)) {
            return "答案错误";
        }
        return "正确";
    }

    private List<String> generateImprovementSuggestions(double accuracy, List<Map<String, Object>> detailedResults) {
        List<String> suggestions = new ArrayList<>();
        
        if (accuracy >= 80) {
            suggestions.add("表现优秀！继续保持这种学习状态");
            suggestions.add("可以尝试更高难度的题目来进一步提升");
        } else if (accuracy >= 60) {
            suggestions.add("基础掌握较好，需要加强细节理解");
            suggestions.add("建议多做相关练习，巩固薄弱知识点");
        } else {
            suggestions.add("需要系统复习相关知识点");
            suggestions.add("建议从基础概念开始重新学习");
            suggestions.add("可以寻求老师或同学的帮助");
        }
        
        return suggestions;
    }

    private List<String> generateNextSteps(double accuracy) {
        List<String> nextSteps = new ArrayList<>();
        
        if (accuracy >= 80) {
            nextSteps.add("进入下一个知识点的学习");
            nextSteps.add("尝试综合性的应用题目");
        } else if (accuracy >= 60) {
            nextSteps.add("重复练习相似题目");
            nextSteps.add("复习相关理论知识");
        } else {
            nextSteps.add("回到基础知识的学习");
            nextSteps.add("寻求额外的学习资源");
            nextSteps.add("与老师讨论学习方法");
        }
        
        return nextSteps;
    }
} 