package com.experiment.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;

import com.experiment.config.DashScopeConfig;
import com.experiment.pojo.ChatRequest;
import com.experiment.pojo.ChatResponse;
import com.experiment.result.Result;
import com.experiment.service.AIService;
import com.experiment.utils.AliOssUtil;
import com.experiment.utils.AliOssProperties;
import com.alibaba.fastjson.JSONObject;
import com.experiment.utils.ApiClient;
import com.experiment.utils.ApiAuthAlgorithm;
import com.experiment.utils.CreateResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * AI控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AIController {
    
    @Autowired
    private AIService aiService;
    
    @Autowired
    private DashScopeConfig dashScopeConfig;
    
    @Autowired
    private AliOssProperties aliOssProperties;

    @Value("${xunfei.ppt.appid}")
    private String xunfeiPptAppId;

    @Value("${xunfei.ppt.apisecret}")
    private String xunfeiPptApiSecret;
    
    /**
     * 发送聊天请求
     */
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("收到聊天请求: {}", request);
        try {
            ChatResponse response = aiService.chat(request);
            return Result.success("聊天请求成功", response);
        } catch (Exception e) {
            log.error("聊天请求处理失败", e);
            return Result.error("聊天请求处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 简单文本对话
     */
    @PostMapping("/simple-chat")
    public Result<String> simpleChat(@RequestParam String message) {
        log.info("收到简单聊天请求: {}", message);
        try {
            String response = aiService.simpleChat(message);
            return Result.success("聊天成功", response);
        } catch (Exception e) {
            log.error("简单聊天请求处理失败", e);
            return Result.error("简单聊天请求处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 带系统提示的对话
     */
    @PostMapping("/chat-with-system")
    public Result<String> chatWithSystem(
            @RequestParam String systemPrompt,
            @RequestParam String userMessage) {
        log.info("收到系统聊天请求 - 系统提示: {}, 用户消息: {}", systemPrompt, userMessage);
        try {
            String response = aiService.chatWithSystem(systemPrompt, userMessage);
            return Result.success("系统聊天成功", response);
        } catch (Exception e) {
            log.error("系统聊天请求处理失败", e);
            return Result.error("系统聊天请求处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 学习助手
     */
    @PostMapping("/learning-assistant")
    public Result<String> learningAssistant(@RequestBody Map<String, Object> data) {
        log.info("收到学习助手请求: {}", data);
        try {
            String message = (String) data.get("message");
            
            // 构建学习助手的系统提示词，包含知识库内容
            String systemPrompt = "你是一个专业的学习助手，拥有丰富的编程、算法、框架等知识。请根据用户的问题，结合知识库中的相关信息，提供准确、详细、易懂的回答。如果是编程相关问题，请提供代码示例；如果是概念性问题，请提供清晰的解释。";
            
            String response = aiService.chatWithSystem(systemPrompt, message);
            return Result.success("学习助手回答成功", response);
        } catch (Exception e) {
            log.error("学习助手请求处理失败", e);
            return Result.error("学习助手回答失败: " + e.getMessage());
        }
    }
    
    /**
     * 代码助手 - 针对编程问题的AI助手
     */
    @PostMapping("/code-assistant")
    public Result<String> codeAssistant(@RequestParam String question) {
        log.info("收到代码助手请求: {}", question);
        try {
            String systemPrompt = "你是一个专业的编程助手，擅长各种编程语言和技术栈。请提供清晰、可运行的代码示例，并解释代码逻辑。";
            String response = aiService.chatWithSystem(systemPrompt, question);
            return Result.success("代码助手回复成功", response);
        } catch (Exception e) {
            log.error("代码助手请求处理失败", e);
            return Result.error("代码助手请求处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 写作助手 - 针对写作的AI助手
     */
    @PostMapping("/writing-assistant")
    public Result<String> writingAssistant(@RequestParam String request) {
        log.info("收到写作助手请求: {}", request);
        try {
            String systemPrompt = "你是一个专业的写作助手，擅长各种文体写作。请根据用户需求提供高质量的写作建议和内容。";
            String response = aiService.chatWithSystem(systemPrompt, request);
            return Result.success("写作助手回复成功", response);
        } catch (Exception e) {
            log.error("写作助手请求处理失败", e);
            return Result.error("写作助手请求处理失败: " + e.getMessage());
        }
    }

    /**
     * AI备课助手 - 完全由AI生成课程设计
     */
    @PostMapping("/course-design")
    public Result<Object> courseDesign(@RequestBody Map<String, Object> data) {
        log.info("收到AI备课助手请求: {}", data);
        try {
            Map<String, Object> courseInfo = (Map<String, Object>) data.get("courseInfo");
            List<String> options = (List<String>) data.get("options");
            List<Map<String, Object>> uploadedFiles = (List<Map<String, Object>>) data.get("uploadedFiles");
            Map<String, Object> existingContent = (Map<String, Object>) data.get("existingContent");

            String courseName = (String) courseInfo.get("courseName");
            String courseType = (String) courseInfo.get("courseType");
            String outline = (String) courseInfo.get("outline");
            String requirements = (String) courseInfo.get("requirements");

            // 处理duration可能是Integer或String的情况
            Object durationObj = courseInfo.get("duration");
            String duration = durationObj != null ? durationObj.toString() : "16";

            // 处理difficulty可能是Integer或String的情况
            Object difficultyObj = courseInfo.get("difficulty");
            String difficulty = difficultyObj != null ? difficultyObj.toString() : "intermediate";

            // 构建文件信息
            String fileInfo = "";
            if (uploadedFiles != null && !uploadedFiles.isEmpty()) {
                fileInfo = "上传的文件包括：" + uploadedFiles.stream()
                        .map(file -> (String) file.get("name"))
                        .collect(java.util.stream.Collectors.joining("、")) + "。";
            }

            Map<String, Object> result;

            // 如果有已生成的内容且只请求PPT，直接使用已生成的内容
            if (existingContent != null && options.size() == 1 && options.contains("ppt")) {
                log.info("使用已生成的课程设计内容来生成PPT");
                result = new HashMap<>(existingContent);
            } else {
                // 构建系统提示词，要求AI完全生成内容，但内容要简洁
                String systemPrompt = "你是一个专业的AI备课助手，请根据提供的课程信息生成教学方案。请严格按照以下JSON格式返回，内容要完整：\n" +
                        "{\n" +
                        "  \"ppt\": {\n" +
                        "    \"slides\": [\n" +
                        "      {\"title\": \"标题\", \"content\": \"内容\"}\n" +
                        "    ]\n" +
                        "  },\n" +
                        "  \"lessonPlan\": {\n" +
                        "    \"content\": \"教案内容\"\n" +
                        "  },\n" +
                        "  \"content\": [\n" +
                        "    {\"title\": \"章节标题\", \"duration\": \"时长\", \"keyPoints\": \"重点\", \"description\": \"描述\"}\n" +
                        "  ]\n" +
                        "}\n" ;

                String userMessage = String.format("为课程《%s》生成教学方案。类型：%s，时长：%s课时，难度：%s。大纲：%s。要求：%s。生成：%s。",
                        courseName, courseType, duration, difficulty, outline, requirements != null ? requirements : "无", String.join("、", options));

                String response = aiService.chatWithSystem(systemPrompt, userMessage);

                // 直接解析AI返回的JSON，不使用任何模拟内容
                result = parseAICourseDesignResponse(response, courseInfo, options);
            }

            // 提取ppt.slides结构，准备调用讯飞PPT API
            Map<String, Object> ppt = null;
            List<Map<String, Object>> slides = null;
            if (result.get("ppt") instanceof Map) {
                ppt = (Map<String, Object>) result.get("ppt");
                if (ppt.get("slides") instanceof List) {
                    slides = (List<Map<String, Object>>) ppt.get("slides");
                }
            }

            // 调用讯飞PPT API生成PPT文件，获取下载链接
            String pptDownloadUrl = null;
            try {
                if (slides != null && !slides.isEmpty()) {
                    // 按照讯飞API官方文档的流程：先生成大纲，再生成PPT
                    ApiClient client = new ApiClient("https://zwapi.xfyun.cn/api/ppt/v2");
                    String ts = String.valueOf(System.currentTimeMillis()/1000);
                    ApiAuthAlgorithm auth = new ApiAuthAlgorithm();
                    String signature = auth.getSignature(xunfeiPptAppId, xunfeiPptApiSecret, Long.parseLong(ts));

                    log.info("讯飞API认证信息 - appId: {}, timestamp: {}, signature: {}", xunfeiPptAppId, ts, signature);

                    // 步骤1：调用createOutline生成大纲
                    String outlineQuery = String.format("为课程《%s》生成PPT大纲，包含以下内容：%s",
                            courseName, slides.stream()
                                    .map(slide -> slide.get("title") + "：" + slide.get("content"))
                                    .collect(java.util.stream.Collectors.joining("；")));

                    log.info("调用讯飞createOutline API，查询内容: {}", outlineQuery);
                    String outlineResp = client.createOutline(xunfeiPptAppId, ts, signature, outlineQuery);
                    log.info("讯飞createOutline API响应: {}", outlineResp);

                    // 解析大纲响应
                    CreateResponse outlineResponse = JSONObject.parseObject(outlineResp, CreateResponse.class);
                    if (outlineResponse != null && outlineResponse.isFlag() && outlineResponse.getData() != null) {
                        String generatedOutline = outlineResponse.getData().getOutline();
                        log.info("生成的大纲: {}", generatedOutline);

                        if (generatedOutline != null && !generatedOutline.isEmpty()) {
                            // 步骤2：使用生成的大纲调用createPptByOutline生成PPT
                            log.info("调用讯飞createPptByOutline API");
                            String pptResp = client.createPptByOutline(xunfeiPptAppId, ts, signature, generatedOutline);
                            log.info("讯飞createPptByOutline API响应: {}", pptResp);

                            // 解析PPT生成响应
                            CreateResponse pptResponse = JSONObject.parseObject(pptResp, CreateResponse.class);
                            if (pptResponse != null && pptResponse.isFlag() && pptResponse.getData() != null) {
                                String sid = pptResponse.getData().getSid();
                                log.info("PPT生成任务ID: {}", sid);

                                if (sid != null && !sid.isEmpty()) {
                                    // 轮询检查PPT生成进度
                                    int maxRetries = 30; // 最多等待30次
                                    int retryCount = 0;
                                    boolean pptReady = false;

                                    while (retryCount < maxRetries && !pptReady) {
                                        try {
                                            Thread.sleep(2000); // 等待2秒
                                            String progressResp = client.checkProgress(xunfeiPptAppId, ts, signature, sid);
                                            log.info("检查PPT生成进度 (第{}次): {}", retryCount + 1, progressResp);

                                            JSONObject progressJson = JSONObject.parseObject(progressResp);
                                            if (progressJson != null && progressJson.containsKey("data")) {
                                                JSONObject progressData = progressJson.getJSONObject("data");
                                                if (progressData != null) {
                                                    // 检查pptStatus字段
                                                    String pptStatus = progressData.getString("pptStatus");
                                                    log.info("PPT生成状态: {}", pptStatus);

                                                    if ("done".equals(pptStatus)) {
                                                        // PPT生成完成，获取下载链接
                                                        String pptUrl = progressData.getString("pptUrl");
                                                        if (pptUrl != null && !pptUrl.isEmpty()) {
                                                            pptDownloadUrl = pptUrl;
                                                            pptReady = true;
                                                            log.info("PPT生成完成，下载链接: {}", pptDownloadUrl);
                                                            break; // 立即退出循环
                                                        } else {
                                                            log.warn("PPT生成完成但未返回下载链接");
                                                            break;
                                                        }
                                                    } else if ("failed".equals(pptStatus) || "error".equals(pptStatus)) {
                                                        log.error("PPT生成失败");
                                                        break;
                                                    }
                                                    // 如果状态是building，继续等待
                                                }
                                            }
                                        } catch (Exception e) {
                                            log.warn("检查PPT生成进度时出错: {}", e.getMessage());
                                        }
                                        retryCount++;
                                    }

                                    if (!pptReady) {
                                        log.warn("PPT生成超时，可能仍在处理中");
                                    }
                                }
                            } else {
                                log.error("讯飞createPptByOutline API返回错误: {}", pptResp);
                            }
                        } else {
                            log.error("讯飞createOutline API未返回有效大纲");
                        }
                    } else {
                        log.error("讯飞createOutline API返回错误: {}", outlineResp);
                    }
                }
            } catch (Exception e) {
                log.error("调用讯飞PPT API生成PPT失败", e);
                pptDownloadUrl = null;
            }
            result.put("pptDownloadUrl", pptDownloadUrl);

            // ...后续调用讯飞PPT API的代码将在此处插入...

            return Result.success("AI备课助手生成成功", result);
        } catch (Exception e) {
            log.error("AI备课助手失败", e);
            return Result.error("AI备课助手失败: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    /**
     * 从AI响应中解析课程设计 - 完全依赖AI生成内容
     */
    private Map<String, Object> parseAICourseDesignResponse(String response, Map<String, Object> courseInfo, List<String> options) {
        Map<String, Object> result = new HashMap<>();
        
        // 清理响应内容，去除markdown代码块
        String cleanResponse = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
        
        // 尝试解析AI返回的JSON格式
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> aiResult = mapper.readValue(cleanResponse, Map.class);
            
            // 如果AI返回了有效的JSON，直接使用
            if (aiResult != null && !aiResult.isEmpty()) {
                result.putAll(aiResult);
                log.info("成功解析AI生成的课程设计内容");
                return result;
            }
        } catch (Exception e) {
            log.warn("AI返回的内容不是有效的JSON格式: {}", e.getMessage());
            
            // 尝试修复常见的JSON格式问题
            try {
                String fixedResponse = fixJsonFormat(cleanResponse);
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> aiResult = mapper.readValue(fixedResponse, Map.class);
                
                if (aiResult != null && !aiResult.isEmpty()) {
                    result.putAll(aiResult);
                    log.info("成功修复并解析AI生成的课程设计内容");
                    return result;
                }
            } catch (Exception e2) {
                log.warn("修复JSON格式后仍然解析失败: {}", e2.getMessage());
            }
        }
        
        // 如果AI返回的不是JSON格式，返回错误信息
        log.error("AI返回的内容格式错误，无法解析为有效的课程设计");
        result.put("error", "AI生成的内容格式不正确，请重试");
        result.put("rawResponse", cleanResponse);
        
        return result;
    }
    
    /**
     * 修复常见的JSON格式问题
     */
    private String fixJsonFormat(String json) {
        // 移除末尾的截断内容
        if (json.contains("```")) {
            json = json.substring(0, json.lastIndexOf("```"));
        }
        
        // 尝试找到最后一个完整的JSON对象
        int lastBrace = json.lastIndexOf("}");
        if (lastBrace > 0) {
            json = json.substring(0, lastBrace + 1);
        }
        
        // 修复常见的引号问题
        json = json.replaceAll("([^\"\\s])\"([^\"\\s])", "$1\\\"$2");
        
        return json;
    }
    
    /**
     * 生成考核题目
     */
    @PostMapping("/generate-exam")
    public Result<Object> generateExam(@RequestBody Map<String, Object> data) {
        log.info("收到生成考核题目请求: {}", data);
        try {
            String subject = (String) data.get("subject");
            String difficulty = (String) data.get("difficulty");
            Integer questionCount = (Integer) data.get("questionCount");
            String examType = (String) data.get("examType");
            String examName = null;
            if (data.containsKey("examName")) {
                examName = (String) data.get("examName");
            } else if (data.containsKey("examConfig")) {
                Map<String, Object> examConfig = (Map<String, Object>) data.get("examConfig");
                examName = (String) examConfig.get("name");
            }
            if (examName == null || examName.trim().isEmpty()) {
                examName = "未命名考核";
            }
            List<Map<String, Object>> knowledgePoints = (List<Map<String, Object>>) data.get("knowledgePoints");
            List<Map<String, Object>> questionTypes = (List<Map<String, Object>>) data.get("questionTypes");
            
            // 构建知识点描述
            String knowledgePointsDesc = "";
            if (knowledgePoints != null && !knowledgePoints.isEmpty()) {
                knowledgePointsDesc = "知识点包括：" + knowledgePoints.stream()
                    .map(point -> point.get("name") + "（权重：" + point.get("weight") + "）")
                    .collect(java.util.stream.Collectors.joining("、")) + "。";
            }
            
            // 构建题目类型描述
            String questionTypesDesc = "";
            if (questionTypes != null && !questionTypes.isEmpty()) {
                questionTypesDesc = "题目类型要求：" + questionTypes.stream()
                    .map(type -> type.get("count") + "道" + getQuestionTypeName((String) type.get("type")) + 
                         "（每题" + type.get("scorePer") + "分，" + type.get("difficulty") + "难度）")
                    .collect(java.util.stream.Collectors.joining("、")) + "。";
            }
            
            // 构建系统提示词
            String systemPrompt = "你是专业出题专家。请严格按JSON格式返回，内容简洁：\n" +
                "{\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"type\": \"choice\",\n" +
                "      \"content\": \"简短题目\",\n" +
                "      \"options\": [\n" +
                "        {\"key\": \"A\", \"content\": \"选项A\"},\n" +
                "        {\"key\": \"B\", \"content\": \"选项B\"},\n" +
                "        {\"key\": \"C\", \"content\": \"选项C\"},\n" +
                "        {\"key\": \"D\", \"content\": \"选项D\"}\n" +
                "      ],\n" +
                "      \"answer\": \"A\",\n" +
                "      \"explanation\": \"简短解析\",\n" +
                "      \"score\": 2,\n" +
                "      \"difficulty\": \"中等\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "要求：1.内容简洁 2.JSON格式完整 3.不要多余文字 4.题目类型：choice单选，multiple多选，fill填空，short简答，coding编程";
            
            // 构建userMessage
            String examTypeDesc = "考试";
            if ("homework".equals(examType)) {
                examTypeDesc = "平时作业";
            }
            String userMessage = String.format("为《%s》%s生成%d道%s难度题目。%s%s", 
                examName, examTypeDesc, questionCount, difficulty, knowledgePointsDesc, questionTypesDesc);
            
            String response = aiService.chatWithSystem(systemPrompt, userMessage);
            
            // 尝试解析AI返回的JSON格式
            Map<String, Object> result = parseExamFromResponse(response, data);
            
            return Result.success("考核题目生成成功", result);
        } catch (Exception e) {
            log.error("生成考核题目请求处理失败", e);
            return Result.error("生成考核题目请求处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 从AI响应中解析考核题目
     */
    private Map<String, Object> parseExamFromResponse(String response, Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();
        String cleanResponse = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
        
        // 预处理：修复常见的JSON问题
        cleanResponse = preprocessJsonResponse(cleanResponse);
        
        // 优先尝试完整解析
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> aiResult = mapper.readValue(cleanResponse, Map.class);
            if (aiResult != null && !aiResult.isEmpty()) {
                result.putAll(aiResult);
                log.info("AI考核题目JSON解析成功");
                return result;
            }
        } catch (Exception e) {
            log.warn("AI返回的内容不是有效的JSON格式，尝试修复: {}", e.getMessage());
        }
        
        // 尝试多种修复策略
        for (int i = 0; i < 3; i++) {
            String fixedJson = null;
            switch (i) {
                case 0:
                    // 策略1：截断到最后一个完整的大括号
                    fixedJson = fixJsonByBraces(cleanResponse);
                    break;
                case 1:
                    // 策略2：提取questions数组
                    fixedJson = extractQuestionsArray(cleanResponse);
                    break;
                case 2:
                    // 策略3：逐步构建完整JSON
                    fixedJson = reconstructJson(cleanResponse);
                    break;
            }
            
            if (fixedJson != null) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<String, Object> aiResult = mapper.readValue(fixedJson, Map.class);
                    if (aiResult != null && !aiResult.isEmpty()) {
                        result.putAll(aiResult);
                        result.put("_warning", "AI返回内容被截断，已自动修复（策略" + (i + 1) + "）");
                        log.info("AI考核题目JSON修复成功（策略{}）", i + 1);
                        return result;
                    }
                } catch (Exception e2) {
                    log.warn("修复策略{}失败: {}", i + 1, e2.getMessage());
                }
            }
        }
        
        // 如果所有策略都失败，返回默认题目
        log.error("AI考核题目JSON彻底解析失败，返回默认题目");
        result.put("error", "AI返回内容格式错误，已使用默认题目");
        result.put("rawResponse", cleanResponse.substring(0, Math.min(cleanResponse.length(), 500)) + "...");
        
        List<Map<String, Object>> questions = generateDefaultQuestions(requestData);
        result.put("questions", questions);
        return result;
    }
    
    /**
     * 预处理JSON响应，简化处理逻辑
     */
    private String preprocessJsonResponse(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "{}";
        }
        
        try {
            // 基本清理
            json = json.trim();
            
            // 移除markdown代码块标记
            if (json.startsWith("```json")) {
                json = json.substring(7);
            }
            if (json.startsWith("```")) {
                json = json.substring(3);
            }
            if (json.endsWith("```")) {
                json = json.substring(0, json.length() - 3);
            }
            json = json.trim();
            
            // 简单的清理操作
            json = basicCleanup(json);
            
            log.debug("JSON预处理完成，长度: {}", json.length());
            return json;
            
        } catch (Exception e) {
            log.warn("JSON预处理出错，返回原始内容: {}", e.getMessage());
            return json;
        }
    }
    
    /**
     * 基本的JSON清理
     */
    private String basicCleanup(String json) {
        if (json == null) {
            return "{}";
        }
        
        // 移除BOM字符
        if (json.startsWith("\uFEFF")) {
            json = json.substring(1);
        }
        
        // 找到JSON的实际开始位置
        int jsonStart = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{' || c == '[') {
                jsonStart = i;
                break;
            }
        }
        
        if (jsonStart > 0) {
            json = json.substring(jsonStart);
        }
        
        // 移除多余的逗号
        json = removeTrailingCommas(json);
        
        return json;
    }
    
    /**
     * 通过大括号截断修复JSON
     */
    private String fixJsonByBraces(String json) {
        // 进一步清理反斜杠问题
        json = advancedCleanBackslashes(json);
        
        int braceCount = 0;
        int lastValidIndex = -1;
        boolean inString = false;
        boolean escaped = false;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (escaped) {
                escaped = false;
                continue;
            }
            
            if (c == '\\') {
                escaped = true;
                continue;
            }
            
            if (c == '"') {
                inString = !inString;
            } else if (!inString) {
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        lastValidIndex = i;
                    }
                }
            }
        }
        
        if (lastValidIndex > 0) {
            String result = json.substring(0, lastValidIndex + 1);
            log.debug("通过大括号截断修复JSON，原长度: {}, 修复后长度: {}", json.length(), result.length());
            return result;
        }
        return null;
    }
    
    /**
     * 高级反斜杠清理
     */
    private String advancedCleanBackslashes(String json) {
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;
        
        for (int i = 0; i < json.length(); i++) {
            char current = json.charAt(i);
            char next = (i + 1 < json.length()) ? json.charAt(i + 1) : '\0';
            
            if (escaped) {
                // 如果当前字符被转义，直接添加
                result.append(current);
                escaped = false;
                continue;
            }
            
            if (current == '\\') {
                // 检查是否是有效的转义序列
                if (next == '"' || next == '\\' || next == '/' || next == 'b' || 
                    next == 'f' || next == 'n' || next == 'r' || next == 't' || 
                    next == 'u') {
                    result.append(current);
                    escaped = true;
                } else if (inString) {
                    // 在字符串中，但不是有效转义，移除反斜杠
                    log.debug("移除无效转义字符: \\{}", next);
                    continue;
                } else {
                    // 不在字符串中，移除反斜杠
                    continue;
                }
            } else {
                if (current == '"') {
                    inString = !inString;
                }
                result.append(current);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 清理孤立的反斜杠
     */
    private String cleanupOrphanBackslashes(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\') {
                // 检查是否是已经保护的特殊字符
                if (i + 1 < text.length()) {
                    String next = text.substring(i + 1, Math.min(i + 11, text.length()));
                    if (next.startsWith("§") && (next.contains("LINE§") || next.contains("TAB§") || 
                        next.contains("RETURN§") || next.contains("QUOTE§") || next.contains("BACKSLASH§") ||
                        next.contains("BACKSPACE§") || next.contains("FORMFEED§") || next.contains("SLASH§"))) {
                        // 这是保护的字符，保留反斜杠
                        result.append(c);
                    } else {
                        // 孤立的反斜杠，忽略
                        log.debug("移除孤立反斜杠，位置: {}", i);
                    }
                } else {
                    // 字符串末尾的反斜杠，忽略
                    log.debug("移除末尾反斜杠");
                }
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 移除多余的逗号
     */
    private String removeTrailingCommas(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        
        // 移除对象结束前的逗号
        json = json.replace(",}", "}");
        json = json.replace(", }", "}");
        json = json.replace(",  }", "}");
        json = json.replace(",   }", "}");
        
        // 移除数组结束前的逗号
        json = json.replace(",]", "]");
        json = json.replace(", ]", "]");
        json = json.replace(",  ]", "]");
        json = json.replace(",   ]", "]");
        
        return json;
    }
    
    /**
     * 提取questions数组
     */
    private String extractQuestionsArray(String json) {
        try {
            // 先进行高级反斜杠清理
            json = advancedCleanBackslashes(json);
            
            // 寻找questions数组的开始和结束
            int questionsStart = json.indexOf("\"questions\"");
            if (questionsStart == -1) {
                return null;
            }
            
            int arrayStart = json.indexOf("[", questionsStart);
            if (arrayStart == -1) {
                return null;
            }
            
            // 找到匹配的结束括号
            int arrayEnd = findMatchingBracket(json, arrayStart, '[', ']');
            if (arrayEnd == -1) {
                return null;
            }
            
            String questionsArray = json.substring(arrayStart + 1, arrayEnd);
            String result = "{\"questions\":[" + questionsArray + "]}";
            
            log.debug("提取questions数组成功，数组长度: {}", questionsArray.length());
            return result;
        } catch (Exception e) {
            log.warn("提取questions数组失败: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 找到匹配的括号
     */
    private int findMatchingBracket(String text, int start, char openBracket, char closeBracket) {
        int count = 1;
        boolean inString = false;
        boolean escaped = false;
        
        for (int i = start + 1; i < text.length(); i++) {
            char c = text.charAt(i);
            
            if (escaped) {
                escaped = false;
                continue;
            }
            
            if (c == '\\') {
                escaped = true;
                continue;
            }
            
            if (c == '"') {
                inString = !inString;
            } else if (!inString) {
                if (c == openBracket) {
                    count++;
                } else if (c == closeBracket) {
                    count--;
                    if (count == 0) {
                        return i;
                    }
                }
            }
        }
        
        return -1;
    }
    
    /**
     * 重构JSON
     */
    private String reconstructJson(String json) {
        try {
            // 寻找第一个完整的question对象
            Pattern pattern = Pattern.compile("\\{\\s*\"id\".*?\"explanation\"\\s*:\\s*\"[^\"]*\"\\s*\\}", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(json);
            List<String> questions = new ArrayList<>();
            
            while (matcher.find() && questions.size() < 10) { // 限制最多10题
                questions.add(matcher.group());
            }
            
            if (!questions.isEmpty()) {
                return "{\"questions\":[" + String.join(",", questions) + "]}";
            }
        } catch (Exception e) {
            log.warn("重构JSON失败: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 生成默认题目（当AI返回异常时使用）
     */
    private List<Map<String, Object>> generateDefaultQuestions(Map<String, Object> requestData) {
        List<Map<String, Object>> questions = new ArrayList<>();
        List<Map<String, Object>> questionTypes = (List<Map<String, Object>>) requestData.get("questionTypes");
        String subject = (String) requestData.get("subject");
        
        int questionId = 1;
        if (questionTypes != null) {
            for (Map<String, Object> type : questionTypes) {
                String typeKey = (String) type.get("type");
                Integer count = (Integer) type.get("count");
                Integer scorePer = (Integer) type.get("scorePer");
                String difficulty = (String) type.get("difficulty");
                
                for (int i = 0; i < count; i++) {
                    Map<String, Object> question = new HashMap<>();
                    question.put("id", questionId);
                    question.put("type", typeKey);
                    question.put("content", String.format("这是第%d题，关于%s的%s类型题目", questionId, subject, getQuestionTypeName(typeKey)));
                    question.put("score", scorePer);
                    question.put("difficulty", difficulty);
                    question.put("answer", String.format("第%d题的参考答案", questionId));
                    question.put("explanation", String.format("第%d题的详细解析", questionId));
                    
                    if ("choice".equals(typeKey) || "multiple".equals(typeKey)) {
                        List<Map<String, Object>> options = new ArrayList<>();
                        options.add(Map.of("key", "A", "content", "选项A"));
                        options.add(Map.of("key", "B", "content", "选项B"));
                        options.add(Map.of("key", "C", "content", "选项C"));
                        options.add(Map.of("key", "D", "content", "选项D"));
                        question.put("options", options);
                    }
                    
                    questions.add(question);
                    questionId++;
                }
            }
        }
        
        return questions;
    }
    
    /**
     * 获取题目类型名称
     */
    private String getQuestionTypeName(String type) {
        switch (type) {
            case "choice": return "单选题";
            case "multiple": return "多选题";
            case "fill": return "填空题";
            case "short": return "简答题";
            case "coding": return "编程题";
            default: return "未知类型";
        }
    }
    
    /**
     * 学情数据分析
     */
    @PostMapping("/analyze-student-data")
    public Result<String> analyzeStudentData(@RequestBody Map<String, Object> data) {
        log.info("收到学情数据分析请求: {}", data);
        try {
            String studentAnswers = (String) data.get("studentAnswers");
            String correctAnswers = (String) data.get("correctAnswers");
            
            String systemPrompt = "你是一个专业的教育数据分析专家，擅长分析学生答题情况，提供个性化的教学建议。";
            String userMessage = String.format("请分析以下学生答题情况：学生答案：%s，正确答案：%s。请提供错误分析、知识点掌握情况评估和教学建议。", studentAnswers, correctAnswers);
            
            String response = aiService.chatWithSystem(systemPrompt, userMessage);
            return Result.success("学情数据分析成功", response);
        } catch (Exception e) {
            log.error("学情数据分析请求处理失败", e);
            return Result.error("学情数据分析请求处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 实时练习评测
     */
    @PostMapping("/practice-evaluation")
    public Result<Object> practiceEvaluation(@RequestBody Map<String, Object> data) {
        log.info("收到练习评测请求: {}", data);
        try {
            List<Map<String, Object>> questions = (List<Map<String, Object>>) data.get("questions");
            List<String> answers = (List<String>) data.get("answers");
            Integer timeUsed = (Integer) data.get("timeUsed");
            String topic = data.get("topic") != null ? data.get("topic").toString() : "";
            
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> analysis = new ArrayList<>();
            int totalScore = 0;
            int correctCount = 0;
            int maxScore = 0;
            
            // 快速计算基础结果（不调用AI）
            for (int i = 0; i < questions.size(); i++) {
                Map<String, Object> question = questions.get(i);
                String userAnswer = i < answers.size() ? answers.get(i) : "";
                String correctAnswer = question.get("answer") != null ? question.get("answer").toString() :
                        (question.get("correctAnswer") != null ? question.get("correctAnswer").toString() : "");
                String questionType = question.get("type") != null ? question.get("type").toString() : "choice";
                int qScore = question.get("score") != null ? Integer.parseInt(question.get("score").toString()) : 10;
                maxScore += qScore;

                // 判分逻辑（可根据题型扩展）
                boolean isCorrect = false;
                if ("choice".equals(questionType)) {
                    // 选择题：直接比较选项字母
                    isCorrect = userAnswer.trim().toUpperCase().equals(correctAnswer.trim().toUpperCase());
                } else if ("fill".equals(questionType)) {
                    // 填空题：比较答案内容（忽略大小写）
                    isCorrect = userAnswer.trim().toLowerCase().equals(correctAnswer.trim().toLowerCase());
                } else if ("short".equals(questionType)) {
                    // 简答题：检查关键词匹配
                    isCorrect = checkShortAnswerSimilarity(userAnswer, correctAnswer);
                } else if ("coding".equals(questionType)) {
                    // 编程题：检查代码结构和关键词
                    isCorrect = checkCodingAnswerSimilarity(userAnswer, correctAnswer);
                } else if ("essay".equals(questionType)) {
                    // 论述题：检查内容完整性和关键词
                    isCorrect = checkEssayAnswerSimilarity(userAnswer, correctAnswer);
                } else {
                    // 默认情况：直接比较
                    isCorrect = userAnswer.trim().toLowerCase().equals(correctAnswer.trim().toLowerCase());
                }
                int score = isCorrect ? qScore : 0;

                // 生成基础分析（快速响应，不调用AI）
                String basicAnalysis = generateBasicAnalysis(question, userAnswer, correctAnswer, isCorrect, questionType);
                String basicSuggestion = generateBasicSuggestion(isCorrect, questionType, topic);
                
                Map<String, Object> analysisItem = new HashMap<>();
                analysisItem.put("title", question.get("title"));
                analysisItem.put("userAnswer", userAnswer);
                analysisItem.put("correctAnswer", correctAnswer);
                analysisItem.put("isCorrect", isCorrect);
                analysisItem.put("score", score);
                analysisItem.put("maxScore", qScore);
                analysisItem.put("feedback", isCorrect ? "回答正确！" : "回答错误，正确答案是：" + correctAnswer);
                analysisItem.put("explanation", question.get("explanation"));
                analysisItem.put("detailedAnalysis", basicAnalysis);
                analysisItem.put("suggestion", basicSuggestion);
                analysisItem.put("type", questionType);
                analysisItem.put("questionId", question.get("id"));
                analysisItem.put("needsAIAnalysis", true); // 标记需要AI分析
                analysis.add(analysisItem);
                
                totalScore += score;
                if (isCorrect) correctCount++;
            }

            double accuracy = questions.size() > 0 ? (double) correctCount / questions.size() * 100 : 0;
            String grade = getGrade(totalScore, maxScore);
            String overallSuggestion = generateSuggestions(correctCount, questions.size());
            
            result.put("analysis", analysis); // 改为analysis以匹配前端期望
            result.put("totalScore", totalScore);
            result.put("maxScore", maxScore);
            result.put("correctCount", correctCount);
            result.put("totalQuestions", questions.size());
            result.put("accuracy", accuracy);
            result.put("timeUsed", timeUsed);
            result.put("grade", grade);
            result.put("suggestions", overallSuggestion);
            
            // 异步生成AI分析（不阻塞响应）
            generateAIAnalysisAsync(analysis, questions, topic);
            
            return Result.success("练习评测成功", result);
        } catch (Exception e) {
            log.error("练习评测请求处理失败", e);
            return Result.error("练习评测请求处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 异步生成AI分析
     */
    private void generateAIAnalysisAsync(List<Map<String, Object>> analysis, List<Map<String, Object>> questions, String topic) {
        // 使用线程池异步处理
        new Thread(() -> {
            for (int i = 0; i < analysis.size(); i++) {
                try {
                    Map<String, Object> analysisItem = analysis.get(i);
                    Map<String, Object> question = questions.get(i);
                    
                    // 检查是否需要AI分析
                    if (!Boolean.TRUE.equals(analysisItem.get("needsAIAnalysis"))) {
                        continue;
                    }
                    
                    String userAnswer = (String) analysisItem.get("userAnswer");
                    String correctAnswer = (String) analysisItem.get("correctAnswer");
                    Boolean isCorrect = (Boolean) analysisItem.get("isCorrect");
                    String questionType = (String) analysisItem.get("type");
                    
                    // 调用AI生成详细分析
                    String aiAnalysis = "";
                    try {
                        String systemPrompt = buildDetailedAnalysisPrompt(questionType, topic);
                        String userMessage = buildDetailedAnalysisMessage(question, userAnswer, correctAnswer, isCorrect);
                        aiAnalysis = aiService.chatWithSystem(systemPrompt, userMessage);
                        
                        if (aiAnalysis != null && !aiAnalysis.trim().isEmpty() && !aiAnalysis.contains("API密钥未配置")) {
                            // 更新分析结果
                            analysisItem.put("detailedAnalysis", aiAnalysis);
                            
                            // 提取学习建议
                            String suggestion = extractSuggestionFromAnalysis(aiAnalysis);
                            if (suggestion != null && !suggestion.trim().isEmpty()) {
                                analysisItem.put("suggestion", suggestion);
                            }
                            
                            log.info("第{}题AI分析生成成功", i + 1);
                        }
                    } catch (Exception e) {
                        log.warn("第{}题AI分析生成失败: {}", i + 1, e.getMessage());
                    }
                    
                    // 标记AI分析完成
                    analysisItem.put("needsAIAnalysis", false);
                    
                    // 添加延迟避免API限流
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    log.error("异步AI分析处理失败", e);
                }
            }
            log.info("所有题目的AI分析生成完成");
        }).start();
    }
    
    /**
     * 从AI分析中提取学习建议
     */
    private String extractSuggestionFromAnalysis(String analysis) {
        if (analysis == null || analysis.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 尝试从AI分析中提取学习建议
            if (analysis.contains("学习建议")) {
                int idx = analysis.indexOf("学习建议");
                String suggestionPart = analysis.substring(idx);
                // 提取学习建议部分，去除markdown格式
                String suggestion = suggestionPart.replaceAll("^#*\\s*学习建议\\s*", "")
                                                 .replaceAll("^\\s*[-*]\\s*", "")
                                                 .replaceAll("\\n+", " ")
                                                 .trim();
                if (suggestion.length() > 200) {
                    suggestion = suggestion.substring(0, 200) + "...";
                }
                return suggestion;
            } else if (analysis.contains("建议")) {
                // 如果没有"学习建议"标题，但有"建议"内容
                int idx = analysis.indexOf("建议");
                String suggestionPart = analysis.substring(idx);
                String suggestion = suggestionPart.replaceAll("^#*\\s*建议\\s*", "")
                                                 .replaceAll("^\\s*[-*]\\s*", "")
                                                 .replaceAll("\\n+", " ")
                                                 .trim();
                if (suggestion.length() > 200) {
                    suggestion = suggestion.substring(0, 200) + "...";
                }
                return suggestion;
            }
        } catch (Exception e) {
            log.warn("提取学习建议失败", e);
        }
        
        return null;
    }
    
    /**
     * 根据得分计算等级
     */
    private String getGrade(int score, int maxScore) {
        double percentage = (double) score / maxScore * 100;
        if (percentage >= 90) return "优秀";
        else if (percentage >= 80) return "良好";
        else if (percentage >= 70) return "中等";
        else if (percentage >= 60) return "及格";
        else return "不及格";
    }
    
    /**
     * 生成学习建议
     */
    private String generateSuggestions(int correctCount, int totalQuestions) {
        double accuracy = (double) correctCount / totalQuestions;
        if (accuracy >= 0.9) {
            return "表现优秀！继续保持这种学习状态。";
        } else if (accuracy >= 0.8) {
            return "表现良好，建议复习错题，巩固薄弱知识点。";
        } else if (accuracy >= 0.7) {
            return "表现中等，建议多做练习，加强基础知识的掌握。";
        } else if (accuracy >= 0.6) {
            return "表现一般，建议系统复习，查漏补缺。";
        } else {
            return "需要加强学习，建议重新学习相关知识点，多做基础练习。";
        }
    }
    
    /**
     * 生成详细的题目解答分析
     */
    @PostMapping("/generate-detailed-analysis")
    public Result<Object> generateDetailedAnalysis(@RequestBody Map<String, Object> data) {
        log.info("收到生成详细解答分析请求: {}", data);
        try {
            Map<String, Object> question = (Map<String, Object>) data.get("question");
            String userAnswer = (String) data.get("userAnswer");
            String correctAnswer = (String) data.get("correctAnswer");
            Boolean isCorrect = (Boolean) data.get("isCorrect");
            String questionType = (String) question.get("type");
            String topic = (String) data.get("topic");
            
            // 构建AI提示词
            String systemPrompt = buildDetailedAnalysisPrompt(questionType, topic);
            String userMessage = buildDetailedAnalysisMessage(question, userAnswer, correctAnswer, isCorrect);
            
            // 调用AI生成详细分析
            String analysis = aiService.chatWithSystem(systemPrompt, userMessage);
            
            // 如果AI调用失败，生成基础分析
            if (analysis == null || analysis.trim().isEmpty() || analysis.contains("API密钥未配置")) {
                analysis = generateBasicAnalysis(question, userAnswer, correctAnswer, isCorrect, questionType);
            }
            
                    Map<String, Object> result = new HashMap<>();
            result.put("detailedAnalysis", analysis);
            result.put("questionType", questionType);
                    result.put("topic", topic);
            
            return Result.success("生成详细解答分析成功", result);
        } catch (Exception e) {
            log.error("生成详细解答分析失败", e);
            return Result.error("生成详细解答分析失败: " + e.getMessage());
        }
    }
    
    /**
     * 查看单道题的详细解答
     */
    @PostMapping("/view-question-detail")
    public Result<Object> viewQuestionDetail(@RequestBody Map<String, Object> data) {
        log.info("收到查看题目详情请求: {}", data);
        try {
            Map<String, Object> question = (Map<String, Object>) data.get("question");
            String userAnswer = (String) data.get("userAnswer");
            String correctAnswer = (String) data.get("correctAnswer");
            Boolean isCorrect = (Boolean) data.get("isCorrect");
            String questionType = (String) question.get("type");
            String topic = (String) data.get("topic");
            
            // 构建AI提示词
            String systemPrompt = buildDetailedAnalysisPrompt(questionType, topic);
            String userMessage = buildDetailedAnalysisMessage(question, userAnswer, correctAnswer, isCorrect);
            
            // 调用AI生成详细分析
            String analysis = aiService.chatWithSystem(systemPrompt, userMessage);
            
            // 如果AI调用失败，生成基础分析
            if (analysis == null || analysis.trim().isEmpty() || analysis.contains("API密钥未配置")) {
                analysis = generateBasicAnalysis(question, userAnswer, correctAnswer, isCorrect, questionType);
            }
            
            // 生成学习建议
            String suggestion = generateBasicSuggestion(isCorrect, questionType, topic);
            
            Map<String, Object> result = new HashMap<>();
            result.put("question", question);
            result.put("userAnswer", userAnswer);
            result.put("correctAnswer", correctAnswer);
            result.put("isCorrect", isCorrect);
            result.put("detailedAnalysis", analysis);
            result.put("suggestion", suggestion);
            result.put("questionType", questionType);
            result.put("topic", topic);
            
            return Result.success("获取题目详情成功", result);
        } catch (Exception e) {
            log.error("获取题目详情失败", e);
            return Result.error("获取题目详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建详细分析的AI提示词
     */
    private String buildDetailedAnalysisPrompt(String questionType, String topic) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的教学分析助手，请为学生的答题情况提供详细的分析和指导。");
        prompt.append("请从以下角度进行分析：");
        prompt.append("1. 答案正确性分析：详细解释正确答案的要点和逻辑；");
        prompt.append("2. 错误原因分析：如果答错了，分析可能的错误原因；");
        prompt.append("3. 知识点梳理：总结本题涉及的核心知识点；");
        prompt.append("4. 解题思路：提供清晰的解题思路和方法；");
        prompt.append("5. 学习建议：针对性的学习建议和改进方向；");
        prompt.append("6. 相关练习：推荐相关的练习题目或学习资源。");
        prompt.append("请用中文回答，语言要通俗易懂，适合学生理解。");
        prompt.append("主题是：").append(topic).append("，题目类型是：").append(getQuestionTypeName(questionType));
        
        return prompt.toString();
    }
    
    /**
     * 构建详细分析的用户消息
     */
    private String buildDetailedAnalysisMessage(Map<String, Object> question, String userAnswer, 
                                               String correctAnswer, Boolean isCorrect) {
        StringBuilder message = new StringBuilder();
        message.append("题目：").append(question.get("title")).append("\n");
        
        if (question.containsKey("options")) {
            message.append("选项：\n");
            List<String> options = (List<String>) question.get("options");
            for (String option : options) {
                message.append(option).append("\n");
            }
        }
        
        message.append("正确答案：").append(correctAnswer).append("\n");
        message.append("学生答案：").append(userAnswer).append("\n");
        message.append("是否正确：").append(isCorrect ? "是" : "否").append("\n");
        
        if (question.containsKey("explanation")) {
            message.append("题目解析：").append(question.get("explanation")).append("\n");
        }
        
        message.append("请为这道题目提供详细的分析和指导。");
        
        return message.toString();
    }
    
    /**
     * 生成基础分析（当AI调用失败时使用）
     */
    private String generateBasicAnalysis(Map<String, Object> question, String userAnswer, 
                                        String correctAnswer, Boolean isCorrect, String questionType) {
        StringBuilder analysis = new StringBuilder();
        
        analysis.append("## 答案分析\n\n");
        
        if (isCorrect) {
            analysis.append("✅ **回答正确！**\n\n");
            analysis.append("你的答案完全正确，说明你已经很好地掌握了相关知识点。\n\n");
        } else {
            analysis.append("❌ **回答错误**\n\n");
            analysis.append("你的答案：").append(userAnswer).append("\n");
            analysis.append("正确答案：").append(correctAnswer).append("\n\n");
            analysis.append("### 错误分析\n");
            analysis.append("可能的原因：\n");
            analysis.append("- 对相关概念理解不够深入\n");
            analysis.append("- 审题不够仔细\n");
            analysis.append("- 知识点掌握不够牢固\n\n");
        }
        
        analysis.append("### 知识点梳理\n");
        analysis.append("本题涉及的核心知识点：\n");
        analysis.append("- 基础概念理解\n");
        analysis.append("- 应用场景分析\n");
        analysis.append("- 技术原理掌握\n\n");
        
        analysis.append("### 解题思路\n");
        analysis.append("1. 仔细阅读题目，理解问题要求\n");
        analysis.append("2. 分析题目涉及的知识点\n");
        analysis.append("3. 运用相关知识进行推理\n");
        analysis.append("4. 验证答案的合理性\n\n");
        
        analysis.append("### 学习建议\n");
        if (isCorrect) {
            analysis.append("- 继续保持良好的学习状态\n");
            analysis.append("- 可以尝试更难的题目\n");
            analysis.append("- 深入理解相关原理\n");
        } else {
            analysis.append("- 复习相关的基础知识\n");
            analysis.append("- 多做类似的练习题目\n");
            analysis.append("- 加强概念理解\n");
        }
        
        return analysis.toString();
    }
    
    /**
     * 生成基础学习建议
     */
    private String generateBasicSuggestion(boolean isCorrect, String questionType, String topic) {
        StringBuilder suggestion = new StringBuilder();
        
        if (isCorrect) {
            suggestion.append("✅ 回答正确！");
            switch (questionType) {
                case "choice":
                    suggestion.append("你对").append(topic).append("的选择题掌握得很好。建议尝试更难的题目，或者学习相关的进阶知识。");
                    break;
                case "fill":
                    suggestion.append("你对").append(topic).append("的填空题理解准确。建议多练习应用场景，加深对概念的理解。");
                    break;
                case "short":
                    suggestion.append("你对").append(topic).append("的简答题回答完整。建议尝试编程实践，将理论知识应用到实际项目中。");
                    break;
                case "coding":
                    suggestion.append("你的编程思路清晰。建议尝试更复杂的算法实现，或者学习相关的设计模式。");
                    break;
                default:
                    suggestion.append("继续保持良好的学习状态，可以尝试更高难度的题目。");
            }
        } else {
            suggestion.append("❌ 回答有误，");
            switch (questionType) {
                case "choice":
                    suggestion.append("建议重新学习").append(topic).append("的基础概念，理解每个选项的含义和区别。");
                    break;
                case "fill":
                    suggestion.append("建议复习").append(topic).append("的核心概念，特别注意关键术语的定义。");
                    break;
                case "short":
                    suggestion.append("建议深入理解").append(topic).append("的原理，多阅读相关文档和示例。");
                    break;
                case "coding":
                    suggestion.append("建议从基础语法开始练习，逐步掌握").append(topic).append("的编程技巧。");
                    break;
                default:
                    suggestion.append("建议复习相关知识点，多做类似练习。");
            }
        }
        
        return suggestion.toString();
    }
    
    /**
     * 生成练习题目
     */
    @PostMapping("/generate-practice")
    public Result<Object> generatePractice(@RequestBody Map<String, Object> data) {
        log.info("收到生成练习题目请求: {}", data);
        try {
            String topic = (String) data.get("topic");
            String difficulty = (String) data.get("difficulty");
            Integer count = (Integer) data.get("count");
            List<String> questionTypes = (List<String>) data.get("questionTypes");
            Map<String, Integer> typeConfig = (Map<String, Integer>) data.get("typeConfig");
            
            // 如果没有指定题目类型，默认为选择题
            if (questionTypes == null || questionTypes.isEmpty()) {
                questionTypes = Arrays.asList("choice");
            }

            // AI生成不同类型的题目
            List<Map<String, Object>> questions = new ArrayList<>();
            
            // 根据题目类型分别生成
            for (String questionType : questionTypes) {
                int typeCount = typeConfig != null && typeConfig.containsKey(questionType) ? 
                    typeConfig.get(questionType) : 
                    Math.max(1, count / questionTypes.size());
                
                List<Map<String, Object>> typeQuestions;
                
                // 对于选择题且主题是tensorflow-js时，优先使用本地题库
                if ("choice".equals(questionType) && "tensorflow-js".equals(topic)) {
                    typeQuestions = generateChoiceQuestionsFromLocal(topic, difficulty, typeCount);
                    // 如果本地题库不足，用AI补充
                    if (typeQuestions.size() < typeCount) {
                        List<Map<String, Object>> aiQuestions = generateQuestionsByType(
                            topic, difficulty, questionType, typeCount - typeQuestions.size());
                        typeQuestions.addAll(aiQuestions);
                    }
                } else {
                    typeQuestions = generateQuestionsByType(topic, difficulty, questionType, typeCount);
                }
                
                questions.addAll(typeQuestions);
            }
            
            // 如果指定了总数但没有具体配置，则调整题目数量
            if (typeConfig == null || typeConfig.isEmpty()) {
                while (questions.size() > count) {
                    questions.remove(questions.size() - 1);
                }
            while (questions.size() < count) {
                    // 随机选择一种类型补充
                    String randomType = questionTypes.get((int) (Math.random() * questionTypes.size()));
                    List<Map<String, Object>> additionalQuestions = generateQuestionsByType(
                        topic, difficulty, randomType, 1);
                    if (!additionalQuestions.isEmpty()) {
                        questions.addAll(additionalQuestions);
                    }
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("topic", topic);
            result.put("difficulty", difficulty);
            result.put("questions", questions);
            
            log.info("生成练习题目成功，共{}道题目", questions.size());
            return Result.success("生成练习题目成功", result);
        } catch (Exception e) {
            log.error("生成练习题目失败", e);
            return Result.error("生成练习题目失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析Markdown题库为结构化题目列表
     */
    private List<Map<String, Object>> parseMarkdownQuestions(String filePath) {
        List<Map<String, Object>> questions = new ArrayList<>();
        try {
            // 尝试多种路径解析方式
            java.nio.file.Path path = null;
            
            // 方式1：相对于当前工作目录
            path = java.nio.file.Paths.get(filePath);
            if (!java.nio.file.Files.exists(path)) {
                // 方式2：相对于项目根目录
                path = java.nio.file.Paths.get("Webstorm-experiment", filePath);
            }
            if (!java.nio.file.Files.exists(path)) {
                // 方式3：绝对路径
                String userDir = System.getProperty("user.dir");
                path = java.nio.file.Paths.get(userDir, filePath);
            }
            if (!java.nio.file.Files.exists(path)) {
                // 方式4：从项目根目录开始查找
                path = java.nio.file.Paths.get("..", filePath);
            }
            
            if (path == null || !java.nio.file.Files.exists(path)) {
                log.error("无法找到题库文件: {}", filePath);
                return questions;
            }
            
            log.info("成功找到题库文件: {}", path.toAbsolutePath());
            List<String> lines = java.nio.file.Files.readAllLines(path);
            Map<String, Object> current = null;
            List<String> options = null;
            for (String line : lines) {
                line = line.trim();
                if (line.matches("^### \\d+\\..*")) {
                    if (current != null) {
                        current.put("options", options);
                        questions.add(current);
                    }
                    current = new HashMap<>();
                    options = new ArrayList<>();
                    current.put("title", line.replaceAll("^### \\d+\\. *", ""));
                } else if (line.matches("^- \\[.\\] [A-D]\\..*")) {
                    if (options == null) options = new ArrayList<>();
                    // 提取选项内容，去掉前面的标记
                    String optionContent = line.replaceAll("^- \\[.\\] ", "");
                    options.add(optionContent);
                } else if (line.startsWith("**答案**:")) {
                    if (current != null) current.put("answer", line.replace("**答案**:", "").trim());
                } else if (line.startsWith("**解析**:")) {
                    if (current != null) current.put("explanation", line.replace("**解析**:", "").trim());
                }
            }
            if (current != null) {
                current.put("options", options);
                questions.add(current);
            }
            log.info("成功解析题库，共{}道题目", questions.size());
        } catch (Exception e) {
            log.error("解析Markdown题库失败: {}", filePath, e);
        }
        return questions;
    }
    
    /**
     * 从AI响应中解析题目，优先解析JSON数组
     */
    private List<Map<String, Object>> parseQuestionsFromResponse(String response) {
        List<Map<String, Object>> questions = new ArrayList<>();
        // 优先尝试解析JSON数组
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            questions = mapper.readValue(response, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>(){});
            if (!questions.isEmpty()) return questions;
        } catch (Exception ignore) {}
        // 兜底：原有分行逻辑
        String[] lines = response.split("\n");
        Map<String, Object> currentQuestion = null;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("题目") || line.startsWith("问题") || line.startsWith("Q")) {
                if (currentQuestion != null) {
                    questions.add(currentQuestion);
                }
                currentQuestion = new HashMap<>();
                currentQuestion.put("title", line);
                currentQuestion.put("options", new ArrayList<>());
            } else if (line.startsWith("A.") || line.startsWith("B.") || line.startsWith("C.") || line.startsWith("D.")) {
                if (currentQuestion != null) {
                    ((List<String>) currentQuestion.get("options")).add(line);
                }
            } else if (line.startsWith("答案") || line.startsWith("参考答案")) {
                if (currentQuestion != null) {
                    currentQuestion.put("answer", line);
                }
            } else if (line.startsWith("解析")) {
                if (currentQuestion != null) {
                    currentQuestion.put("explanation", line);
                }
            }
        }
        if (currentQuestion != null) {
            questions.add(currentQuestion);
        }
        // 如果没有解析到题目，返回模拟题目
        if (questions.isEmpty()) {
            questions.add(createMockQuestion());
        }
        return questions;
    }
    
    /**
     * 从本地题库生成选择题
     */
    private List<Map<String, Object>> generateChoiceQuestionsFromLocal(String topic, String difficulty, int count) {
        List<Map<String, Object>> questions = new ArrayList<>();
        try {
            List<Map<String, Object>> allQuestions = parseMarkdownQuestions("front(2)/front(1)/public/knowledge-base/tensorflow-js/exercises/practice-questions.md");
            Collections.shuffle(allQuestions);
            
            for (Map<String, Object> q : allQuestions) {
                if (questions.size() >= count) break;
                q.put("type", "choice"); // 确保题目类型
                q.put("difficulty", difficulty); // 设置难度
                questions.add(q);
            }
            
            log.info("从本地题库生成{}道选择题", questions.size());
        } catch (Exception e) {
            log.error("从本地题库生成选择题失败", e);
        }
        
        return questions;
    }
    
    /**
     * 根据题目类型生成题目
     */
    private List<Map<String, Object>> generateQuestionsByType(String topic, String difficulty, String questionType, int count) {
        List<Map<String, Object>> questions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            try {
                // 编程题优先调用AI接口
                if ("coding".equals(questionType) || "programming".equals(questionType)) {
                    String systemPrompt = getSystemPromptByType(questionType, topic, difficulty);
                    String userMessage = getUserMessageByType(questionType, topic, difficulty);
                    systemPrompt += " 这是第" + (i + 1) + "道题目，请确保与之前的题目完全不同。";
                    String response = aiService.chatWithSystem(systemPrompt, userMessage);
                    Map<String, Object> question = parseQuestionFromResponse(response, questionType);
                    if (question != null && !isDuplicateQuestion(question, questions)) {
                        question.put("type", questionType);
                        question.put("difficulty", difficulty);
                        questions.add(question);
                        continue;
                    }
                }
                // 其他题型或AI失败时走原有逻辑
                String systemPrompt = getSystemPromptByType(questionType, topic, difficulty);
                String userMessage = getUserMessageByType(questionType, topic, difficulty);
                systemPrompt += " 这是第" + (i + 1) + "道题目，请确保与之前的题目完全不同。";
                String response = aiService.chatWithSystem(systemPrompt, userMessage);
                Map<String, Object> question = parseQuestionFromResponse(response, questionType);
                if (question != null && !isDuplicateQuestion(question, questions)) {
                    question.put("type", questionType);
                    question.put("difficulty", difficulty);
                    questions.add(question);
                } else {
                    Map<String, Object> mockQuestion = createMockQuestionByType(questionType, topic, difficulty, i);
                    while (isDuplicateQuestion(mockQuestion, questions)) {
                        mockQuestion = createMockQuestionByType(questionType, topic, difficulty, i);
                    }
                    questions.add(mockQuestion);
                }
            } catch (Exception e) {
                log.error("生成{}类型题目失败 (第{}道)", questionType, i + 1, e);
                Map<String, Object> mockQuestion = createMockQuestionByType(questionType, topic, difficulty, i);
                while (isDuplicateQuestion(mockQuestion, questions)) {
                    mockQuestion = createMockQuestionByType(questionType, topic, difficulty, i);
                }
                questions.add(mockQuestion);
            }
        }
        return questions;
    }
    
    /**
     * 检查题目是否重复
     */
    private boolean isDuplicateQuestion(Map<String, Object> newQuestion, List<Map<String, Object>> existingQuestions) {
        if (newQuestion == null || !newQuestion.containsKey("title")) {
            return true;
        }
        
        String newTitle = (String) newQuestion.get("title");
        for (Map<String, Object> existingQuestion : existingQuestions) {
            if (existingQuestion.containsKey("title")) {
                String existingTitle = (String) existingQuestion.get("title");
                // 简单的重复检查，如果标题相似度超过80%则认为重复
                if (calculateSimilarity(newTitle, existingTitle) > 0.8) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 计算两个字符串的相似度
     */
    private double calculateSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0.0;
        }
        
        // 简单的相似度计算，基于共同字符的比例
        String longer = str1.length() > str2.length() ? str1 : str2;
        String shorter = str1.length() > str2.length() ? str2 : str1;
        
        if (longer.length() == 0) {
            return 1.0;
        }
        
        int commonChars = 0;
        for (int i = 0; i < shorter.length(); i++) {
            if (longer.contains(String.valueOf(shorter.charAt(i)))) {
                commonChars++;
            }
        }
        
        return (double) commonChars / longer.length();
    }
    
    /**
     * 根据题目类型获取系统提示词
     */
    private String getSystemPromptByType(String questionType, String topic, String difficulty) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的教学题目生成助手。请严格按照要求生成高质量的").append(getQuestionTypeName(questionType)).append("。");
        
        // 添加随机性指令，避免重复
        prompt.append("重要要求：每道题目必须独特，不能与之前生成的题目重复。请从不同角度、不同知识点、不同应用场景来设计题目。");
        
        switch (questionType) {
            case "choice":
                prompt.append("请生成选择题，包含1个题目、4个选项(A、B、C、D)、1个正确答案和解析。选项要合理，避免明显错误。每个选项都应该有合理的干扰性。");
                break;
            case "fill":
                prompt.append("请生成填空题，在题目中用____表示空白处，提供标准答案。可以有多个空白，答案要准确。空白处应该考察核心知识点。");
                break;
            case "short":
                prompt.append("请生成简答题，要求学生用简洁的语言回答问题，提供参考答案和评分要点。答案要有条理，涵盖关键概念。");
                break;
            case "coding":
                prompt.append("请生成编程题，包含问题描述、输入输出示例、参考代码和测试用例。代码要规范，问题要有实际应用价值。");
                break;
            case "essay":
                prompt.append("请生成论述题，要求深入分析和论证，提供评分标准和参考要点。论述要有深度，体现综合思考能力。");
                break;
        }
        
        prompt.append("题目难度为").append(difficulty).append("，主题是").append(topic).append("。");
        prompt.append("请确保每道题目都是独特的，避免重复。从以下不同维度设计题目：");
        prompt.append("1. 基础概念理解；2. 实际应用场景；3. 技术实现细节；4. 性能优化考虑；5. 最佳实践应用；6. 常见问题解决；7. 架构设计思路；8. 工具使用技巧。");
        prompt.append("请返回JSON格式的响应，格式如下：");
        
        switch (questionType) {
            case "choice":
                prompt.append("{\"title\":\"题目内容\",\"options\":[\"A. 选项1\",\"B. 选项2\",\"C. 选项3\",\"D. 选项4\"],\"answer\":\"A\",\"explanation\":\"解析内容\"}");
                break;
            case "fill":
                prompt.append("{\"title\":\"题目内容____空白处____\",\"answer\":\"标准答案\"}");
                break;
            case "short":
                prompt.append("{\"title\":\"题目内容\",\"referenceAnswer\":\"参考答案\"}");
                break;
            case "coding":
                prompt.append("{\"title\":\"题目内容\",\"requirements\":\"编程要求\",\"examples\":[{\"input\":\"输入示例\",\"output\":\"输出示例\"}]}");
                break;
            case "essay":
                prompt.append("{\"title\":\"题目内容\",\"keyPoints\":[\"要点1\",\"要点2\",\"要点3\"]}");
                break;
        }
        
        return prompt.toString();
    }
    
    /**
     * 根据题目类型获取用户消息
     */
    private String getUserMessageByType(String questionType, String topic, String difficulty) {
        // 增加更多随机性，让AI生成不同的题目
        String[] variations = {
            String.format("请生成1道关于'%s'的%s难度%s，要求内容准确、难度适中。请从基础概念角度设计题目。", topic, difficulty, getQuestionTypeName(questionType)),
            String.format("创建1道%s难度的%s，主题是'%s'，要求题目新颖、有挑战性。请从实际应用场景角度设计题目。", difficulty, getQuestionTypeName(questionType), topic),
            String.format("设计1道关于'%s'的%s，难度为%s，要求结合实际应用场景。请从技术实现细节角度设计题目。", topic, getQuestionTypeName(questionType), difficulty),
            String.format("请为'%s'主题生成1道%s难度的%s，要求考察核心知识点。请从性能优化角度设计题目。", topic, difficulty, getQuestionTypeName(questionType)),
            String.format("制作1道%s，主题是'%s'，难度%s，要求题目有深度和广度。请从最佳实践角度设计题目。", getQuestionTypeName(questionType), topic, difficulty),
            String.format("生成1道关于'%s'的%s难度%s，要求题目独特、有启发性。请从常见问题解决角度设计题目。", topic, difficulty, getQuestionTypeName(questionType)),
            String.format("创建1道%s难度的%s，主题是'%s'，要求题目实用、有价值。请从架构设计角度设计题目。", difficulty, getQuestionTypeName(questionType), topic),
            String.format("设计1道关于'%s'的%s，难度为%s，要求题目全面、系统。请从工具使用技巧角度设计题目。", topic, getQuestionTypeName(questionType), difficulty),
            String.format("请为'%s'主题生成1道%s难度的%s，要求题目创新、前沿。请从发展趋势角度设计题目。", topic, difficulty, getQuestionTypeName(questionType)),
            String.format("制作1道%s，主题是'%s'，难度%s，要求题目经典、权威。请从理论基础角度设计题目。", getQuestionTypeName(questionType), topic, difficulty)
        };
        return variations[(int)(Math.random() * variations.length)];
    }
    
    /**
     * 从AI响应中解析题目
     */
    private Map<String, Object> parseQuestionFromResponse(String response, String questionType) {
        try {
            // 去除markdown代码块
            response = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            // 尝试JSON解析
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> question = mapper.readValue(response, Map.class);
            // 补充type字段
            if (question != null && !question.containsKey("type")) {
                question.put("type", questionType);
            }
            // 验证题目结构
            if (validateQuestionStructure(question, questionType)) {
                return question;
            }
        } catch (Exception e) {
            log.warn("JSON解析失败，尝试文本解析: {}", e.getMessage());
        }
        // 如果JSON解析失败，尝试文本解析
        Map<String, Object> question = parseQuestionFromText(response, questionType);
        // 补充type字段
        if (question != null && !question.containsKey("type")) {
            question.put("type", questionType);
        }
        return question;
    }
    
    /**
     * 验证题目结构
     */
    private boolean validateQuestionStructure(Map<String, Object> question, String questionType) {
        if (question == null || !question.containsKey("title")) {
            return false;
        }
        
        switch (questionType) {
            case "choice":
                return question.containsKey("options") && question.containsKey("answer");
            case "fill":
                return question.containsKey("answer");
            case "short":
                return question.containsKey("referenceAnswer");
            case "coding":
                return question.containsKey("requirements");
            default:
                return true;
        }
    }
    
    /**
     * 从文本中解析题目
     */
    private Map<String, Object> parseQuestionFromText(String text, String questionType) {
        Map<String, Object> question = new HashMap<>();
        String[] lines = text.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("题目") || line.startsWith("问题")) {
                question.put("title", line.replaceFirst("^(题目|问题)[:：]?\\s*", ""));
            } else if (questionType.equals("choice") && (line.startsWith("A.") || line.startsWith("B.") || line.startsWith("C.") || line.startsWith("D."))) {
                if (!question.containsKey("options")) {
                    question.put("options", new ArrayList<String>());
                }
                ((List<String>) question.get("options")).add(line);
            } else if (line.startsWith("答案") || line.startsWith("参考答案")) {
                question.put("answer", line.replaceFirst("^(答案|参考答案)[:：]?\\s*", ""));
            }
        }
        
        return question.isEmpty() ? null : question;
    }
    
    /**
     * 创建指定类型的模拟题目
     */
    private Map<String, Object> createMockQuestionByType(String questionType, String topic, String difficulty, int index) {
        Map<String, Object> question = new HashMap<>();
        question.put("type", questionType);
        question.put("difficulty", difficulty);
        
        // 为不同主题和题型生成不同的模拟题目
        String[] topics = topic.split("[,，]");
        String mainTopic = topics[0].trim();
        
        // 使用index来增加随机性，避免重复
        long seed = System.currentTimeMillis() + index * 1000;
        java.util.Random random = new java.util.Random(seed);
        
        switch (questionType) {
            case "choice":
                question.put("title", generateChoiceTitle(mainTopic, difficulty, random));
                question.put("options", generateChoiceOptions(mainTopic, random));
                question.put("answer", "A");
                question.put("explanation", "这是关于" + mainTopic + "的基础知识。");
                break;
            case "fill":
                question.put("title", generateFillTitle(mainTopic, difficulty, random));
                question.put("answer", generateFillAnswer(mainTopic, random));
                question.put("explanation", "填空题考察对" + mainTopic + "基础概念的理解。");
                break;
            case "short":
                question.put("title", generateShortTitle(mainTopic, difficulty, random));
                question.put("answer", generateShortAnswer(mainTopic, random));
                question.put("referenceAnswer", generateShortAnswer(mainTopic, random));
                question.put("explanation", "简答题需要全面理解" + mainTopic + "的相关知识。");
                break;
            case "coding":
                question.put("title", generateCodingTitle(mainTopic, difficulty, random));
                question.put("requirements", generateCodingRequirements(mainTopic, random));
                question.put("answer", generateCodingAnswer(mainTopic, random));
                question.put("examples", Arrays.asList(
                    Map.of("input", generateCodingInput(mainTopic, random), "output", generateCodingOutput(mainTopic, random))
                ));
                question.put("explanation", "编程题考察" + mainTopic + "的实际应用能力。");
                break;
            case "essay":
                question.put("title", generateEssayTitle(mainTopic, difficulty, random));
                question.put("answer", generateEssayAnswer(mainTopic, random));
                question.put("keyPoints", generateEssayKeyPoints(mainTopic, random));
                question.put("explanation", "论述题需要深入分析" + mainTopic + "的相关问题。");
                break;
        }
        
        return question;
    }
    
    /**
     * 生成选择题标题
     */
    private String generateChoiceTitle(String topic, String difficulty, java.util.Random random) {
        // 增加更多样化的标题模板
        String[] titles = {
            "关于" + topic + "的基础概念，以下说法正确的是？",
            "在" + topic + "中，下列哪个选项描述最准确？",
            topic + "的核心特性是什么？",
            "下列关于" + topic + "的描述，错误的是？",
            topic + "的主要应用场景是？",
            "在" + topic + "开发中，最重要的考虑因素是？",
            topic + "与传统方法相比，主要优势在于？",
            "使用" + topic + "时，需要注意的关键点是？",
            topic + "的实现原理是什么？",
            "在" + topic + "项目中，常见的挑战包括？",
            topic + "的最佳实践是什么？",
            "如何优化" + topic + "的性能？",
            topic + "的架构设计原则是？",
            "在" + topic + "中，数据流向是怎样的？",
            topic + "的错误处理机制是？"
        };
        return titles[random.nextInt(titles.length)];
    }
    
    /**
     * 生成选择题选项
     */
    private List<String> generateChoiceOptions(String topic, java.util.Random random) {
        List<String> options = new ArrayList<>();
        
        // 为不同主题生成更具体的选项
        String[][] optionSets = {
            {
                "A. " + topic + "的基础概念和原理",
                "B. " + topic + "的高级特性和扩展功能", 
                "C. " + topic + "的实际应用场景和案例",
                "D. " + topic + "的性能优化和最佳实践"
            },
            {
                "A. " + topic + "的核心组件和架构",
                "B. " + topic + "的配置和参数设置",
                "C. " + topic + "的集成和部署方式",
                "D. " + topic + "的监控和维护策略"
            },
            {
                "A. " + topic + "的基本操作和API使用",
                "B. " + topic + "的扩展开发和自定义功能",
                "C. " + topic + "的测试和调试方法",
                "D. " + topic + "的安全性和稳定性保障"
            },
            {
                "A. " + topic + "的设计模式和架构思想",
                "B. " + topic + "的工具链和开发环境",
                "C. " + topic + "的社区支持和生态系统",
                "D. " + topic + "的未来发展趋势和方向"
            }
        };
        
        String[] selectedOptions = optionSets[random.nextInt(optionSets.length)];
        Collections.shuffle(Arrays.asList(selectedOptions));
        options.addAll(Arrays.asList(selectedOptions));
        return options;
    }
    
    /**
     * 生成填空题标题
     */
    private String generateFillTitle(String topic, String difficulty, java.util.Random random) {
        // 增加更多样化的填空题模板
        String[] titles = {
            topic + "中，____是一个重要的概念。",
            "在" + topic + "开发中，____是必不可少的。",
            topic + "的核心功能是____。",
            "____是" + topic + "的基础组成部分。",
            topic + "的主要特点是____。",
            "使用" + topic + "时，需要____来确保正确性。",
            topic + "的____机制保证了系统的稳定性。",
            "在" + topic + "架构中，____负责处理核心逻辑。",
            topic + "的____功能提供了扩展能力。",
            "____是" + topic + "性能优化的关键因素。",
            topic + "的____接口简化了开发流程。",
            "在" + topic + "中，____用于数据持久化。",
            topic + "的____模块处理用户交互。",
            "____是" + topic + "安全机制的核心。",
            topic + "的____策略决定了资源分配。"
        };
        return titles[random.nextInt(titles.length)];
    }
    
    /**
     * 生成填空题答案
     */
    private String generateFillAnswer(String topic, java.util.Random random) {
        // 增加更多样化的答案选项
        String[] answers = {
            "核心概念",
            "基础功能", 
            "主要特性",
            "重要组件",
            "关键要素",
            "验证机制",
            "容错处理",
            "核心引擎",
            "扩展接口",
            "缓存策略",
            "API设计",
            "数据存储",
            "用户界面",
            "加密算法",
            "调度算法"
        };
        return answers[random.nextInt(answers.length)];
    }
    
    /**
     * 生成简答题标题
     */
    private String generateShortTitle(String topic, String difficulty, java.util.Random random) {
        // 增加更多样化的简答题模板
        String[] titles = {
            "请简述" + topic + "的主要特点。",
            "解释" + topic + "的核心概念。",
            "描述" + topic + "的应用场景。",
            "分析" + topic + "的优势和劣势。",
            "说明" + topic + "的工作原理。",
            "阐述" + topic + "的设计原则。",
            "介绍" + topic + "的实现方法。",
            "分析" + topic + "的性能特点。",
            "说明" + topic + "的配置要求。",
            "描述" + topic + "的部署流程。",
            "解释" + topic + "的架构设计。",
            "分析" + topic + "的扩展性。",
            "说明" + topic + "的安全机制。",
            "描述" + topic + "的监控方式。",
            "阐述" + topic + "的最佳实践。"
        };
        return titles[random.nextInt(titles.length)];
    }
    
    /**
     * 生成简答题答案
     */
    private String generateShortAnswer(String topic, java.util.Random random) {
        // 增加更多样化的答案模板
        String[] answerTemplates = {
            topic + "具有以下特点：1) 功能强大且灵活；2) 易于使用和学习；3) 应用广泛且成熟；4) 性能优秀且稳定。在实际开发中，" + topic + "能够有效提高开发效率，降低维护成本，是现代软件开发的重要工具。",
            topic + "的核心概念包括：1) 模块化设计，便于维护和扩展；2) 标准化接口，提高兼容性；3) 自动化处理，减少人工干预；4) 可配置性，适应不同需求。" + topic + "通过这些特性，为开发者提供了强大的支持。",
            topic + "的应用场景主要包括：1) 企业级应用开发；2) 数据处理和分析；3) 系统集成和互操作；4) 性能优化和监控。" + topic + "在这些场景中表现出色，是业界公认的解决方案。",
            topic + "的优势在于：1) 成熟稳定的技术栈；2) 丰富的生态系统；3) 良好的社区支持；4) 完善的文档和教程。劣势包括：1) 学习曲线相对较陡；2) 某些高级功能需要深入理解。" + topic + "总体而言利大于弊。",
            topic + "的工作原理基于：1) 事件驱动架构，响应式处理；2) 异步编程模型，提高并发性能；3) 内存管理机制，优化资源使用；4) 错误处理策略，保证系统稳定性。" + topic + "通过这些机制实现高效运行。"
        };
        return answerTemplates[random.nextInt(answerTemplates.length)];
    }
    
    /**
     * 生成编程题标题
     */
    private String generateCodingTitle(String topic, String difficulty, java.util.Random random) {
        // 增加更多样化的编程题模板
        String[] titles = {
            "编写一个" + topic + "相关的程序",
            "实现" + topic + "的基本功能",
            "创建一个" + topic + "的示例代码",
            "开发" + topic + "的核心模块",
            "构建" + topic + "的应用框架",
            "设计" + topic + "的数据结构",
            "实现" + topic + "的算法逻辑",
            "创建" + topic + "的工具类",
            "开发" + topic + "的配置管理",
            "构建" + topic + "的测试框架",
            "实现" + topic + "的缓存机制",
            "设计" + topic + "的API接口",
            "创建" + topic + "的数据库操作",
            "实现" + topic + "的日志系统",
            "构建" + topic + "的安全模块"
        };
        return titles[random.nextInt(titles.length)];
    }
    
    /**
     * 生成编程题要求
     */
    private String generateCodingRequirements(String topic, java.util.Random random) {
        // 增加更多样化的编程要求
        String[] requirements = {
            "请实现一个关于" + topic + "的函数，要求代码简洁、功能完整、注释清晰。",
            "创建一个" + topic + "相关的类，包含必要的属性和方法，遵循面向对象设计原则。",
            "实现" + topic + "的核心算法，要求时间复杂度合理，空间复杂度优化。",
            "开发一个" + topic + "的工具模块，提供常用的功能接口，支持扩展。",
            "构建" + topic + "的数据处理流程，包含输入验证、业务逻辑、结果输出。",
            "设计" + topic + "的配置管理系统，支持动态加载和热更新。",
            "实现" + topic + "的缓存策略，提高系统性能和响应速度。",
            "创建" + topic + "的异常处理机制，确保程序稳定运行。",
            "开发" + topic + "的单元测试，覆盖主要功能和边界情况。",
            "构建" + topic + "的API接口，遵循RESTful设计规范。"
        };
        return requirements[random.nextInt(requirements.length)];
    }
    
    /**
     * 生成编程题输入示例
     */
    private String generateCodingInput(String topic, java.util.Random random) {
        // 增加更多样化的输入示例
        String[] inputs = {
            "输入参数示例：{\"data\": [1, 2, 3], \"config\": {\"type\": \"default\"}}",
            "测试数据：{\"name\": \"test\", \"value\": 100, \"enabled\": true}",
            "输入格式：{\"items\": [\"item1\", \"item2\"], \"options\": {\"sort\": true}}",
            "参数示例：{\"query\": \"search\", \"filters\": {\"category\": \"tech\"}}",
            "数据输入：{\"numbers\": [10, 20, 30], \"operation\": \"sum\"}",
            "配置参数：{\"timeout\": 5000, \"retries\": 3, \"async\": true}",
            "输入结构：{\"user\": {\"id\": 1, \"name\": \"admin\"}, \"action\": \"create\"}",
            "测试用例：{\"input\": \"hello world\", \"expected\": \"HELLO WORLD\"}",
            "参数配置：{\"mode\": \"production\", \"debug\": false, \"cache\": true}",
            "数据格式：{\"records\": [{\"id\": 1, \"value\": \"test\"}], \"total\": 1}"
        };
        return inputs[random.nextInt(inputs.length)];
    }
    
    /**
     * 生成编程题输出示例
     */
    private String generateCodingOutput(String topic, java.util.Random random) {
        // 增加更多样化的输出示例
        String[] outputs = {
            "期望输出结果：{\"success\": true, \"result\": \"processed data\", \"count\": 3}",
            "输出格式：{\"status\": \"ok\", \"data\": [\"result1\", \"result2\"], \"message\": \"success\"}",
            "返回结果：{\"code\": 200, \"message\": \"operation completed\", \"data\": {\"id\": 123}}",
            "输出示例：{\"processed\": true, \"items\": [\"item1\", \"item2\"], \"summary\": {\"total\": 2}}",
            "期望结果：{\"valid\": true, \"score\": 95.5, \"feedback\": \"excellent work\"}",
            "输出结构：{\"result\": \"success\", \"details\": {\"time\": \"2024-01-01\", \"duration\": 100}}",
            "返回格式：{\"success\": true, \"data\": {\"name\": \"result\", \"value\": 42}}",
            "输出示例：{\"status\": \"completed\", \"output\": \"processed result\", \"metrics\": {\"time\": 50}}",
            "期望输出：{\"valid\": true, \"result\": \"test passed\", \"performance\": {\"memory\": \"10MB\"}}",
            "返回结果：{\"code\": 0, \"message\": \"success\", \"data\": {\"processed\": 100, \"errors\": 0}}"
        };
        return outputs[random.nextInt(outputs.length)];
    }
    
    /**
     * 生成编程题答案
     */
    private String generateCodingAnswer(String topic, java.util.Random random) {
        // 生成编程题的参考答案代码
        String[] codeTemplates = {
            "function process" + topic + "Data(data) {\n  // 处理" + topic + "相关数据\n  return data.map(item => item.toUpperCase());\n}",
            "class " + topic + "Manager {\n  constructor() {\n    this.data = [];\n  }\n  \n  addItem(item) {\n    this.data.push(item);\n  }\n}",
            "const " + topic + "Config = {\n  timeout: 5000,\n  retries: 3,\n  async: true\n};",
            "async function handle" + topic + "Request(params) {\n  try {\n    const result = await api.call(params);\n    return { success: true, data: result };\n  } catch (error) {\n    return { success: false, error: error.message };\n  }\n}",
            "const " + topic + "Utils = {\n  validate: (data) => data && data.length > 0,\n  format: (data) => JSON.stringify(data, null, 2)\n};"
        };
        return codeTemplates[random.nextInt(codeTemplates.length)];
    }
    
    /**
     * 生成论述题答案
     */
    private String generateEssayAnswer(String topic, java.util.Random random) {
        // 生成论述题的参考答案
        String[] essayTemplates = {
            topic + "是一个重要的技术概念，在现代软件开发中发挥着关键作用。它提供了强大的功能和灵活的扩展性，能够有效解决复杂的技术问题。通过合理使用" + topic + "，开发者可以构建出高质量、高性能的应用程序。",
            "从技术角度来看，" + topic + "具有以下核心特性：首先，它提供了标准化的接口和规范，确保了系统的兼容性和可维护性；其次，它支持模块化设计，便于代码复用和团队协作；最后，它具备良好的扩展性，能够适应不断变化的需求。",
            topic + "的应用价值体现在多个方面：在企业级应用中，它能够提供稳定可靠的服务支持；在数据处理领域，它具备高效的处理能力和灵活的配置选项；在系统集成方面，它提供了丰富的接口和工具，简化了开发流程。",
            "相比其他技术方案，" + topic + "具有明显的优势：它拥有成熟的技术生态和丰富的社区资源，学习成本相对较低；同时，它的性能表现优秀，能够满足大多数应用场景的需求；此外，它的维护成本较低，长期使用效益显著。",
            "未来，" + topic + "将继续保持其重要地位，并在以下几个方面得到进一步发展：首先，随着技术的不断进步，它将集成更多先进的功能和特性；其次，它将更好地支持云原生和微服务架构；最后，它将提供更好的开发体验和更丰富的工具支持。"
        };
        return essayTemplates[random.nextInt(essayTemplates.length)];
    }
    
    /**
     * 生成论述题标题
     */
    private String generateEssayTitle(String topic, String difficulty, java.util.Random random) {
        String[] titles = {
            "论述" + topic + "的重要性及其应用场景。",
            "分析" + topic + "的发展趋势和未来前景。",
            "探讨" + topic + "在实际项目中的应用价值。",
            "比较" + topic + "与其他技术的优劣。",
            "阐述" + topic + "的核心原理和技术特点。"
        };
        return titles[random.nextInt(titles.length)];
    }
    
    /**
     * 生成论述题要点
     */
    private List<String> generateEssayKeyPoints(String topic, java.util.Random random) {
        List<String> points = new ArrayList<>();
        points.add(topic + "的基本概念和定义");
        points.add(topic + "的核心特性和优势");
        points.add(topic + "的应用场景和案例");
        points.add(topic + "的发展趋势和前景");
        return points;
    }
    
    /**
     * 根据主题获取对应的AI提示词
     */
    private String getPromptByTopic(String topic) {
        switch (topic) {
            case "tensorflow-js":
                return "你是一个编程教育专家，请根据用户要求，结合TensorFlow.js知识库内容，生成高质量的选择题。知识库包含：1）TensorFlow.js基础概念（张量、变量、操作、模型构建、内存管理）；2）项目实践（汽车油耗预测、手写数字识别、CNN模型、数据预处理）；3）高级应用（模型转换、性能优化、WebGL后端、实际应用场景）；4）开发最佳实践（错误处理、调试、版本管理、代码组织、测试策略）；5）综合考点（环境配置、API使用、性能优化、实际应用）；6）手势识别项目（图像分类、实时处理、模型部署、用户体验）。请严格以如下JSON数组格式返回，不要输出多余内容：\n" +
                        "[{\"title\":\"题干1\",\"options\":[\"A. ...\",\"B. ...\"],\"answer\":\"A\",\"explanation\":\"解析1\"}]\n" +
                        "题目数量必须等于用户要求，每道题考查不同角度和细节，涵盖理论知识和实践应用。";
            case "machine-learning":
                return "你是一个机器学习教育专家，请根据用户要求，结合机器学习知识库内容，生成高质量的选择题。知识库包含：1）机器学习基础概念（监督学习、无监督学习、强化学习）；2）算法原理（线性回归、逻辑回归、决策树、随机森林、SVM、K-means）；3）模型评估（准确率、精确率、召回率、F1分数、ROC曲线）；4）特征工程（特征选择、特征缩放、特征编码）；5）深度学习基础（神经网络、激活函数、反向传播、梯度下降）；6）实际应用（图像识别、自然语言处理、推荐系统）。请严格以如下JSON数组格式返回，不要输出多余内容：\n" +
                        "[{\"title\":\"题干1\",\"options\":[\"A. ...\",\"B. ...\"],\"answer\":\"A\",\"explanation\":\"解析1\"}]\n" +
                        "题目数量必须等于用户要求，每道题考查不同角度和细节。";
            case "deep-learning":
                return "你是一个深度学习教育专家，请根据用户要求，结合深度学习知识库内容，生成高质量的选择题。知识库包含：1）神经网络基础（前馈神经网络、反向传播、梯度下降）；2）卷积神经网络（CNN架构、卷积层、池化层、全连接层）；3）循环神经网络（RNN、LSTM、GRU、序列建模）；4）深度学习框架（TensorFlow、PyTorch、Keras）；5）模型优化（正则化、Dropout、批归一化、学习率调度）；6）高级应用（计算机视觉、自然语言处理、生成对抗网络）。请严格以如下JSON数组格式返回，不要输出多余内容：\n" +
                        "[{\"title\":\"题干1\",\"options\":[\"A. ...\",\"B. ...\"],\"answer\":\"A\",\"explanation\":\"解析1\"}]\n" +
                        "题目数量必须等于用户要求，每道题考查不同角度和细节。";
            case "javascript":
                return "你是一个JavaScript编程教育专家，请根据用户要求，结合JavaScript知识库内容，生成高质量的选择题。知识库包含：1）JavaScript基础语法（变量、数据类型、运算符、控制结构）；2）函数和对象（函数定义、作用域、闭包、对象创建）；3）异步编程（Promise、async/await、事件循环）；4）DOM操作（元素选择、事件处理、动态内容）；5）ES6+特性（箭头函数、解构赋值、模块化、类）；6）前端框架基础（Vue.js、React基础概念）。请严格以如下JSON数组格式返回，不要输出多余内容：\n" +
                        "[{\"title\":\"题干1\",\"options\":[\"A. ...\",\"B. ...\"],\"answer\":\"A\",\"explanation\":\"解析1\"}]\n" +
                        "题目数量必须等于用户要求，每道题考查不同角度和细节。";
            case "algorithms":
                return "你是一个算法与数据结构教育专家，请根据用户要求，结合算法知识库内容，生成高质量的选择题。知识库包含：1）基础数据结构（数组、链表、栈、队列、树、图）；2）排序算法（冒泡排序、选择排序、插入排序、快速排序、归并排序）；3）搜索算法（线性搜索、二分搜索、深度优先搜索、广度优先搜索）；4）动态规划（背包问题、最长公共子序列、编辑距离）；5）算法复杂度分析（时间复杂度、空间复杂度、大O表示法）；6）实际应用（路径规划、网络流、字符串匹配）。请严格以如下JSON数组格式返回，不要输出多余内容：\n" +
                        "[{\"title\":\"题干1\",\"options\":[\"A. ...\",\"B. ...\"],\"answer\":\"A\",\"explanation\":\"解析1\"}]\n" +
                        "题目数量必须等于用户要求，每道题考查不同角度和细节。";
            case "vue":
                return "你是一个Vue.js开发教育专家，请根据用户要求，结合Vue.js知识库内容，生成高质量的选择题。知识库包含：1）Vue.js基础概念（响应式数据、组件化、生命周期）；2）模板语法（指令、插值、事件处理）；3）组件开发（组件注册、Props传递、事件通信）；4）状态管理（Vuex、Pinia、响应式原理）；5）路由管理（Vue Router、路由配置、导航守卫）；6）实际应用（单页应用、组件库使用、性能优化）。请严格以如下JSON数组格式返回，不要输出多余内容：\n" +
                        "[{\"title\":\"题干1\",\"options\":[\"A. ...\",\"B. ...\"],\"answer\":\"A\",\"explanation\":\"解析1\"}]\n" +
                        "题目数量必须等于用户要求，每道题考查不同角度和细节。";
            case "database":
                return "你是一个数据库设计教育专家，请根据用户要求，结合数据库知识库内容，生成高质量的选择题。知识库包含：1）数据库基础概念（关系型数据库、非关系型数据库、ACID特性）；2）SQL语言（DDL、DML、DQL、DCL）；3）数据库设计（ER图、范式理论、索引设计）；4）事务管理（事务隔离级别、锁机制、并发控制）；5）性能优化（查询优化、索引优化、分库分表）；6）实际应用（MySQL、PostgreSQL、MongoDB使用）。请严格以如下JSON数组格式返回，不要输出多余内容：\n" +
                        "[{\"title\":\"题干1\",\"options\":[\"A. ...\",\"B. ...\"],\"answer\":\"A\",\"explanation\":\"解析1\"}]\n" +
                        "题目数量必须等于用户要求，每道题考查不同角度和细节。";
            case "mathematics":
                return "你是一个数学教育专家，请根据用户要求，结合数学知识库内容，生成高质量的选择题。知识库包含：1）线性代数（矩阵运算、向量空间、特征值分解）；2）微积分（导数、积分、偏导数、梯度）；3）概率统计（概率分布、期望方差、假设检验、置信区间）；4）数值计算（数值积分、数值微分、插值方法）；5）优化理论（梯度下降、牛顿法、约束优化）；6）实际应用（机器学习数学基础、数据科学数学工具）。请严格以如下JSON数组格式返回，不要输出多余内容：\n" +
                        "[{\"title\":\"题干1\",\"options\":[\"A. ...\",\"B. ...\"],\"answer\":\"A\",\"explanation\":\"解析1\"}]\n" +
                        "题目数量必须等于用户要求，每道题考查不同角度和细节。";
            default:
                return "你是一个编程教育专家，请根据用户要求，结合编程知识库内容，生成高质量的选择题。请严格以如下JSON数组格式返回，不要输出多余内容：\n" +
                        "[{\"title\":\"题干1\",\"options\":[\"A. ...\",\"B. ...\"],\"answer\":\"A\",\"explanation\":\"解析1\"}]\n" +
                        "题目数量必须等于用户要求，每道题考查不同角度和细节。";
        }
    }

    /**
     * 创建模拟题目
     */
    private Map<String, Object> createMockQuestion() {
        Map<String, Object> question = new HashMap<>();
        question.put("title", "TensorFlow.js中张量的基本操作是什么？");
        question.put("options", Arrays.asList(
            "A. 创建、运算、销毁",
            "B. 读取、写入、删除", 
            "C. 初始化、训练、预测",
            "D. 编译、运行、调试"
        ));
        question.put("answer", "A");
        question.put("explanation", "张量的基本操作包括创建、运算和销毁。");
        return question;
    }
    
    /**
     * 知识库查询
     */
    @PostMapping("/knowledge-query")
    public Result<String> knowledgeQuery(@RequestBody Map<String, Object> data) {
        log.info("收到知识库查询请求: {}", data);
        try {
            String query = (String) data.get("query");
            
            // 构建知识库查询的提示词
            String systemPrompt = "你是一个专业的教育知识库助手，拥有丰富的TensorFlow.js、机器学习、编程等知识。请根据用户的问题，结合知识库中的相关信息，提供准确、详细的回答。如果知识库中有相关信息，请优先使用知识库内容；如果没有，可以基于你的知识回答。";
            
            String response = aiService.chatWithSystem(systemPrompt, query);
            return Result.success("知识库查询成功", response);
        } catch (Exception e) {
            log.error("知识库查询请求处理失败", e);
            return Result.error("知识库查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取知识库分类
     */
    @GetMapping("/knowledge-categories")
    public Result<Object> getKnowledgeCategories() {
        log.info("收到获取知识库分类请求");
        try {
            Map<String, Object> categories = new HashMap<>();
            categories.put("tensorflow-js", "TensorFlow.js");
            categories.put("computer-science", "计算机科学");
            categories.put("programming", "编程技术");
            categories.put("algorithms", "算法与数据结构");
            categories.put("tensorflow-js-projects", "TensorFlow.js项目实践");
            categories.put("advanced-tensorflow-js", "TensorFlow.js高级应用");
            categories.put("comprehensive-tensorflow-js", "TensorFlow.js综合考点");
            categories.put("gesture-recognition", "手势识别项目");
            
            return Result.success("获取知识库分类成功", categories);
        } catch (Exception e) {
            log.error("获取知识库分类失败", e);
            return Result.error("获取知识库分类失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取知识库内容
     */
    @GetMapping("/knowledge-content")
    public Result<Object> getKnowledgeContent(@RequestParam String category, @RequestParam String topic) {
        log.info("收到获取知识库内容请求: category={}, topic={}", category, topic);
        try {
            // 这里可以根据category和topic从文件系统或数据库读取对应的知识库内容
            Map<String, Object> content = new HashMap<>();
            content.put("category", category);
            content.put("topic", topic);
            content.put("content", "这是" + category + "分类下" + topic + "主题的知识内容。");
            
            return Result.success("获取知识库内容成功", content);
        } catch (Exception e) {
            log.error("获取知识库内容失败", e);
            return Result.error("获取知识库内容失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索知识库
     */
    @PostMapping("/knowledge-search")
    public Result<Object> searchKnowledgeBase(@RequestBody Map<String, Object> data) {
        log.info("收到知识库搜索请求: {}", data);
        try {
            String keyword = (String) data.get("keyword");
            
            // 模拟搜索结果
            List<Map<String, Object>> results = new ArrayList<>();
            
            // 基础概念
            Map<String, Object> result1 = new HashMap<>();
            result1.put("title", "TensorFlow.js基础概念");
            result1.put("category", "tensorflow-js");
            result1.put("topic", "basics");
            result1.put("summary", "包含TensorFlow.js的核心概念和基本用法");
            results.add(result1);
            
            // 张量操作
            Map<String, Object> result2 = new HashMap<>();
            result2.put("title", "张量操作详解");
            result2.put("category", "tensorflow-js");
            result2.put("topic", "tensors");
            result2.put("summary", "详细介绍TensorFlow.js中的张量操作");
            results.add(result2);
            
            // 项目实践
            Map<String, Object> result3 = new HashMap<>();
            result3.put("title", "汽车油耗预测项目");
            result3.put("category", "tensorflow-js-projects");
            result3.put("topic", "car-mpg-prediction");
            result3.put("summary", "使用TensorFlow.js实现汽车油耗预测的完整项目");
            results.add(result3);
            
            Map<String, Object> result4 = new HashMap<>();
            result4.put("title", "手写数字识别项目");
            result4.put("category", "tensorflow-js-projects");
            result4.put("topic", "mnist-recognition");
            result4.put("summary", "使用CNN实现MNIST手写数字识别的项目");
            results.add(result4);
            
            // 高级应用
            Map<String, Object> result5 = new HashMap<>();
            result5.put("title", "模型转换与部署");
            result5.put("category", "advanced-tensorflow-js");
            result5.put("topic", "model-conversion");
            result5.put("summary", "将Python模型转换为TensorFlow.js格式并部署");
            results.add(result5);
            
            Map<String, Object> result6 = new HashMap<>();
            result6.put("title", "性能优化技巧");
            result6.put("category", "advanced-tensorflow-js");
            result6.put("topic", "performance-optimization");
            result6.put("summary", "TensorFlow.js应用性能优化的最佳实践");
            results.add(result6);
            
            // 综合考点
            Map<String, Object> result7 = new HashMap<>();
            result7.put("title", "TensorFlow.js综合考点");
            result7.put("category", "comprehensive-tensorflow-js");
            result7.put("topic", "comprehensive-knowledge");
            result7.put("summary", "涵盖TensorFlow.js所有重要知识点的综合考点");
            results.add(result7);
            
            // 手势识别项目
            Map<String, Object> result8 = new HashMap<>();
            result8.put("title", "剪刀石头布手势识别");
            result8.put("category", "gesture-recognition");
            result8.put("topic", "rock-paper-scissors");
            result8.put("summary", "使用TensorFlow.js实现实时手势识别项目");
            results.add(result8);
            
            // 根据关键词过滤结果
            if (keyword != null && !keyword.trim().isEmpty()) {
                results = results.stream()
                    .filter(result -> {
                        String title = (String) result.get("title");
                        String summary = (String) result.get("summary");
                        return title.toLowerCase().contains(keyword.toLowerCase()) ||
                               summary.toLowerCase().contains(keyword.toLowerCase());
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("keyword", keyword);
            response.put("results", results);
            response.put("total", results.size());
            
            return Result.success("知识库搜索成功", response);
        } catch (Exception e) {
            log.error("知识库搜索失败", e);
            return Result.error("知识库搜索失败: " + e.getMessage());
        }
    }

    /**
     * 文件上传接口 - 支持OSS和本地存储
     */
    @PostMapping("/upload/file")
    public Result<Object> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("收到文件上传请求: {}", file.getOriginalFilename());
        try {
            // 检查文件大小
            if (file.getSize() > 10 * 1024 * 1024) { // 10MB
                return Result.error("文件大小不能超过10MB");
            }
            
            // 检查文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.matches(".*\\.(doc|docx|pdf|txt|md|ppt|pptx|jpg|jpeg|png|gif|mp4|avi|mp3|wav)$")) {
                return Result.error("不支持的文件类型，只支持文档、图片、视频、音频格式");
            }
            
            // 生成唯一文件名
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8) + fileExtension;
            
            String fileUrl;
            String storageType;
            
            // 尝试使用OSS上传
            try {
                // 检查OSS配置
                String endpoint = aliOssProperties.getEndpoint();
                String accessKeyId = aliOssProperties.getAccessKeyId();
                String accessKeySecret = aliOssProperties.getAccessKeySecret();
                String bucketName = aliOssProperties.getBucketName();
                
                if (endpoint != null && accessKeyId != null && accessKeySecret != null && bucketName != null) {
                    // 使用OSS上传
                    AliOssUtil ossUtil = new AliOssUtil(endpoint, accessKeyId, accessKeySecret, bucketName);
                    fileUrl = ossUtil.upload(file.getBytes(), "uploads/" + fileName);
                    storageType = "oss";
                    log.info("文件上传到OSS成功: {}", fileUrl);
                } else {
                    throw new Exception("OSS配置不完整");
                }
            } catch (Exception e) {
                log.warn("OSS上传失败，使用本地存储: {}", e.getMessage());
                
                // 本地存储兜底
                String uploadDir = "uploads/";
                java.io.File dir = new java.io.File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                
                String filePath = uploadDir + fileName;
                java.io.File dest = new java.io.File(filePath);
                file.transferTo(dest);
                
                fileUrl = "/api/ai/download/" + fileName;
                storageType = "local";
                log.info("文件上传到本地成功: {}", filePath);
            }
            
            // 返回文件信息
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("originalName", originalFilename);
            fileInfo.put("fileName", fileName);
            fileInfo.put("url", fileUrl);
            fileInfo.put("size", file.getSize());
            fileInfo.put("uploadTime", java.time.LocalDateTime.now());
            fileInfo.put("storageType", storageType);
            fileInfo.put("fileType", getFileType(originalFilename));
            
            return Result.success("文件上传成功", fileInfo);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件类型
     */
    private String getFileType(String filename) {
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        if (ext.matches("(doc|docx|pdf|txt|md)")) return "document";
        if (ext.matches("(ppt|pptx)")) return "presentation";
        if (ext.matches("(jpg|jpeg|png|gif)")) return "image";
        if (ext.matches("(mp4|avi|mov)")) return "video";
        if (ext.matches("(mp3|wav)")) return "audio";
        return "other";
    }
    
    /**
     * 测试接口 - 用于验证后端服务是否正常
     */
    @GetMapping("/test")
    public Result<Object> test() {
        log.info("收到测试请求");
        Map<String, Object> testData = new HashMap<>();
        testData.put("message", "后端服务正常运行");
        testData.put("timestamp", System.currentTimeMillis());
        testData.put("questions", Arrays.asList(
            Map.of("title", "测试题目1", "options", Arrays.asList("A. 选项1", "B. 选项2"), "answer", "A", "explanation", "测试解析"),
            Map.of("title", "测试题目2", "options", Arrays.asList("A. 选项1", "B. 选项2"), "answer", "B", "explanation", "测试解析")
        ));
        return Result.success("测试成功", testData);
    }
    
    /**
     * 健康检查接口 - 检查AI服务状态
     */
    @GetMapping("/health")
    public Result<Object> healthCheck() {
        log.info("收到健康检查请求");
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "healthy");
        healthData.put("timestamp", System.currentTimeMillis());
        healthData.put("aiService", "available");
        
        // 检查AI服务配置
        String apiKey = dashScopeConfig.getApiKey();
        String appId = dashScopeConfig.getAppId();
        healthData.put("apiKeyConfigured", apiKey != null && !apiKey.isEmpty());
        healthData.put("appIdConfigured", appId != null && !appId.isEmpty());
        
        // 尝试简单的AI调用测试
        try {
            String testResponse = aiService.chatWithSystem("你是一个测试助手", "请回复'OK'");
            healthData.put("aiTest", "success");
            healthData.put("aiResponse", testResponse);
        } catch (Exception e) {
            healthData.put("aiTest", "failed");
            healthData.put("aiError", e.getMessage());
        }
        
        return Result.success("健康检查完成", healthData);
    }
    
    /**
     * 文件下载接口
     */
    @GetMapping("/download/{fileName}")
    public void downloadFile(@PathVariable String fileName, HttpServletResponse response) {
        try {
            String filePath = "uploads/" + fileName;
            java.io.File file = new java.io.File(filePath);
            
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            
            // 写入响应流
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
                 java.io.OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();
            }
        } catch (Exception e) {
            log.error("文件下载失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 检查简答题答案相似度
     */
    private boolean checkShortAnswerSimilarity(String userAnswer, String correctAnswer) {
        if (userAnswer == null || correctAnswer == null || userAnswer.trim().isEmpty() || correctAnswer.trim().isEmpty()) {
            return false;
        }
        
        // 提取关键词进行比较
        List<String> userKeywords = extractKeywords(userAnswer);
        List<String> correctKeywords = extractKeywords(correctAnswer);
        
        // 计算关键词匹配度
        int matchCount = 0;
        for (String keyword : userKeywords) {
            if (correctKeywords.contains(keyword)) {
                matchCount++;
            }
        }
        
        double similarity = (double) matchCount / Math.max(userKeywords.size(), correctKeywords.size());
        return similarity >= 0.3; // 30%的关键词匹配即认为正确
    }
    
    /**
     * 检查编程题答案相似度
     */
    private boolean checkCodingAnswerSimilarity(String userAnswer, String correctAnswer) {
        if (userAnswer == null || correctAnswer == null || userAnswer.trim().isEmpty() || correctAnswer.trim().isEmpty()) {
            return false;
        }
        
        // 检查代码结构关键词
        String[] codeKeywords = {"function", "class", "const", "let", "var", "if", "for", "while", "return", "async", "await"};
        boolean userHasKeywords = false;
        for (String keyword : codeKeywords) {
            if (userAnswer.toLowerCase().contains(keyword)) {
                userHasKeywords = true;
                break;
            }
        }
        
        // 检查基本语法结构
        boolean hasBasicStructure = userAnswer.contains("{") && userAnswer.contains("}");
        
        // 检查长度合理性（至少有一定内容）
        boolean hasReasonableLength = userAnswer.length() >= 20;
        
        return userHasKeywords && hasBasicStructure && hasReasonableLength;
    }
    
    /**
     * 检查论述题答案相似度
     */
    private boolean checkEssayAnswerSimilarity(String userAnswer, String correctAnswer) {
        if (userAnswer == null || correctAnswer == null || userAnswer.trim().isEmpty() || correctAnswer.trim().isEmpty()) {
            return false;
        }
        
        // 检查内容长度（论述题应该有足够的内容）
        boolean hasReasonableLength = userAnswer.length() >= 50;
        
        // 提取关键词进行比较
        List<String> userKeywords = extractKeywords(userAnswer);
        List<String> correctKeywords = extractKeywords(correctAnswer);
        
        // 计算关键词匹配度
        int matchCount = 0;
        for (String keyword : userKeywords) {
            if (correctKeywords.contains(keyword)) {
                matchCount++;
            }
        }
        
        double similarity = (double) matchCount / Math.max(userKeywords.size(), correctKeywords.size());
        
        return hasReasonableLength && similarity >= 0.2; // 20%的关键词匹配即认为正确
    }
    
    /**
     * 提取关键词的辅助函数
     */
    private List<String> extractKeywords(String text) {
        // 简单的关键词提取：去除常见停用词，保留有意义的词汇
        String[] stopWords = {"的", "是", "在", "有", "和", "与", "或", "但", "而", "如果", "因为", "所以", 
                             "the", "is", "are", "in", "on", "at", "and", "or", "but", "if", "because", "so"};
        
        String[] words = text.toLowerCase()
                .replaceAll("[^\\w\\s\\u4e00-\\u9fa5]", " ") // 保留中英文和数字
                .split("\\s+");
        
        List<String> keywords = new ArrayList<>();
        for (String word : words) {
            if (word.length() > 1 && !Arrays.asList(stopWords).contains(word)) {
                keywords.add(word);
            }
        }
        
        // 取前10个关键词
        return keywords.size() > 10 ? keywords.subList(0, 10) : keywords;
    }
    
    /**
     * 检查AI分析状态
     */
    @PostMapping("/check-analysis-status")
    public Result<Object> checkAnalysisStatus(@RequestBody Map<String, Object> data) {
        log.info("收到检查AI分析状态请求: {}", data);
        try {
            List<Map<String, Object>> analysis = (List<Map<String, Object>>) data.get("analysis");
            if (analysis == null) {
                return Result.error("分析数据不能为空");
            }
            
            // 检查每道题的AI分析状态
            List<Map<String, Object>> statusList = new ArrayList<>();
            for (int i = 0; i < analysis.size(); i++) {
                Map<String, Object> analysisItem = analysis.get(i);
                Map<String, Object> status = new HashMap<>();
                status.put("index", i);
                status.put("needsAIAnalysis", analysisItem.get("needsAIAnalysis"));
                status.put("hasAIAnalysis", analysisItem.get("detailedAnalysis") != null && 
                    !analysisItem.get("detailedAnalysis").toString().contains("基础分析"));
                statusList.add(status);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("statusList", statusList);
            result.put("allCompleted", statusList.stream().noneMatch(s -> Boolean.TRUE.equals(s.get("needsAIAnalysis"))));
            
            return Result.success("检查AI分析状态成功", result);
        } catch (Exception e) {
            log.error("检查AI分析状态失败", e);
            return Result.error("检查AI分析状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动触发单道题的AI分析
     */
    @PostMapping("/trigger-ai-analysis")
    public Result<Object> triggerAIAnalysis(@RequestBody Map<String, Object> data) {
        log.info("收到手动触发AI分析请求: {}", data);
        try {
            Map<String, Object> question = (Map<String, Object>) data.get("question");
            String userAnswer = (String) data.get("userAnswer");
            String correctAnswer = (String) data.get("correctAnswer");
            Boolean isCorrect = (Boolean) data.get("isCorrect");
            String questionType = (String) question.get("type");
            String topic = (String) data.get("topic");
            
            // 调用AI生成详细分析
            String aiAnalysis = "";
            try {
                String systemPrompt = buildDetailedAnalysisPrompt(questionType, topic);
                String userMessage = buildDetailedAnalysisMessage(question, userAnswer, correctAnswer, isCorrect);
                aiAnalysis = aiService.chatWithSystem(systemPrompt, userMessage);
                
                if (aiAnalysis == null || aiAnalysis.trim().isEmpty() || aiAnalysis.contains("API密钥未配置")) {
                    aiAnalysis = generateBasicAnalysis(question, userAnswer, correctAnswer, isCorrect, questionType);
                }
            } catch (Exception e) {
                log.error("AI分析生成失败", e);
                aiAnalysis = generateBasicAnalysis(question, userAnswer, correctAnswer, isCorrect, questionType);
            }
            
            // 提取学习建议
            String suggestion = extractSuggestionFromAnalysis(aiAnalysis);
            if (suggestion == null || suggestion.trim().isEmpty()) {
                suggestion = generateBasicSuggestion(isCorrect, questionType, topic);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("detailedAnalysis", aiAnalysis);
            result.put("suggestion", suggestion);
            result.put("questionType", questionType);
            result.put("topic", topic);
            
            return Result.success("AI分析生成成功", result);
        } catch (Exception e) {
            log.error("手动触发AI分析失败", e);
            return Result.error("手动触发AI分析失败: " + e.getMessage());
        }
    }

    /**
     * 根据文档内容进行对话
     */
    @PostMapping("/chat-with-document")
    public Result<Object> chatWithDocument(@RequestBody Map<String, Object> data) {
        log.info("收到文档对话请求: {}", data);
        try {
            String question = (String) data.get("question");
            String documentContent = (String) data.get("documentContent");
            Object resourceIdObj = data.get("resourceId");
            
            // 构建系统提示词
            String systemPrompt = "你是一个专业的文档分析助手。用户会提供一个文档内容，然后询问关于该文档的问题。请基于文档内容准确、详细地回答用户的问题。如果问题超出文档范围，请明确说明。\n\n文档内容：\n" + documentContent;
            
            String response = aiService.chatWithSystem(systemPrompt, question);
            
            Map<String, Object> result = new HashMap<>();
            result.put("answer", response);
            result.put("resourceId", resourceIdObj);
            
            return Result.success("文档对话成功", result);
        } catch (Exception e) {
            log.error("文档对话处理失败", e);
            return Result.error("文档对话处理失败: " + e.getMessage());
        }
    }

    /**
     * 生成思维导图
     */
    @PostMapping("/generate-mindmap")
    public Result<Object> generateMindmap(@RequestBody Map<String, Object> data) {
        log.info("收到思维导图生成请求: {}", data);
        try {
            String documentContent = (String) data.get("documentContent");
            Object resourceIdObj = data.get("resourceId");
            
            // 构建系统提示词
            String systemPrompt = "你是一个专业的思维导图生成助手。请基于提供的文档内容，生成一个结构清晰、层次分明的思维导图。请使用HTML格式输出，包含适当的CSS样式，使思维导图美观易读。思维导图应该包含主题、主要分支和子分支，体现文档的核心内容和逻辑结构。";
            
            String userMessage = "请基于以下文档内容生成思维导图：\n\n" + documentContent;
            
            String response = aiService.chatWithSystem(systemPrompt, userMessage);
            
            // 如果AI返回的不是HTML格式，则包装成HTML
            String mindmapHtml = response;
            if (!response.trim().startsWith("<")) {
                mindmapHtml = generateMindmapHtml(response);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("mindmapHtml", mindmapHtml);
            result.put("resourceId", resourceIdObj);
            
            return Result.success("思维导图生成成功", result);
        } catch (Exception e) {
            log.error("思维导图生成失败", e);
            return Result.error("思维导图生成失败: " + e.getMessage());
        }
    }

    /**
     * 将文本内容转换为HTML格式的思维导图
     */
    private String generateMindmapHtml(String textContent) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html lang=\"zh-CN\">\n")
            .append("<head>\n")
            .append("    <meta charset=\"UTF-8\">\n")
            .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
            .append("    <title>思维导图</title>\n")
            .append("    <style>\n")
            .append("        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n")
            .append("        .mindmap { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n")
            .append("        .mindmap-content { line-height: 1.6; color: #333; }\n")
            .append("        .mindmap-content h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }\n")
            .append("        .mindmap-content h2 { color: #3498db; margin-top: 20px; }\n")
            .append("        .mindmap-content h3 { color: #e74c3c; margin-top: 15px; }\n")
            .append("        .mindmap-content ul { margin-left: 20px; }\n")
            .append("        .mindmap-content li { margin: 5px 0; }\n")
            .append("    </style>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("    <div class=\"mindmap\">\n")
            .append("        <div class=\"mindmap-content\">\n")
            .append(textContent.replace("\n", "<br>\n"))
            .append("        </div>\n")
            .append("    </div>\n")
            .append("</body>\n")
            .append("</html>");
        
        return html.toString();
    }
} 