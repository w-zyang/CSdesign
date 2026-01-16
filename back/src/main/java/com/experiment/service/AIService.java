package com.experiment.service;

import com.experiment.pojo.ChatRequest;
import com.experiment.pojo.ChatResponse;

/**
 * AI服务接口
 */
public interface AIService {
    
    /**
     * 发送聊天请求
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponse chat(ChatRequest request);
    
    /**
     * 简单文本对话
     * @param message 用户消息
     * @return AI回复
     */
    String simpleChat(String message);
    
    /**
     * 带系统提示的对话
     * @param systemPrompt 系统提示
     * @param userMessage 用户消息
     * @return AI回复
     */
    String chatWithSystem(String systemPrompt, String userMessage);
} 