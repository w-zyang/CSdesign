package com.experiment.service.Impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.experiment.mapper.ClassSummaryMapper;
import com.experiment.pojo.ClassSummary;
import com.experiment.pojo.ClassSummaryDTO;
import com.experiment.service.AIService;
import com.experiment.service.ClassSummaryService;

import lombok.extern.slf4j.Slf4j;

/**
 * 课堂重点整理服务实现类
 */
@Slf4j
@Service
public class ClassSummaryServiceImpl implements ClassSummaryService {
    
    @Autowired
    private ClassSummaryMapper classSummaryMapper;
    
    @Autowired
    private AIService aiService;
    
    private static final String AUDIO_UPLOAD_PATH = "uploads/audio/";
    
    @Override
    public ClassSummary createClassSummary(ClassSummaryDTO dto) {
        ClassSummary classSummary = new ClassSummary();
        classSummary.setCourseId(dto.getCourseId());
        classSummary.setTeacherId(dto.getTeacherId());
        classSummary.setTitle(dto.getTitle());
        classSummary.setDescription(dto.getDescription());
        classSummary.setCoursewareFilePath(dto.getCoursewareFilePath());
        classSummary.setKeywords(dto.getKeywords());
        classSummary.setStatus("DRAFT");
        classSummary.setCreateTime(LocalDateTime.now());
        classSummary.setUpdateTime(LocalDateTime.now());
        classSummary.setViewCount(0);
        
        classSummaryMapper.insert(classSummary);
        log.info("创建课堂总结成功，ID: {}", classSummary.getId());
        
        return classSummary;
    }
    
    @Override
    public String uploadAudioFile(MultipartFile audioFile, Long courseId) {
        try {
            // 创建上传目录
            String uploadDir = AUDIO_UPLOAD_PATH + "course" + courseId + "/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成唯一文件名
            String originalFilename = audioFile.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            String filePath = uploadDir + filename;
            
            // 保存文件
            Path path = Paths.get(filePath);
            Files.write(path, audioFile.getBytes());
            
            log.info("录音文件上传成功: {}", filePath);
            return filePath;
            
        } catch (IOException e) {
            log.error("录音文件上传失败", e);
            throw new RuntimeException("录音文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public String processAudioToText(Long summaryId, String audioFilePath) {
        try {
            log.info("开始处理录音文件: {}", audioFilePath);
            
            // 构建语音转文字的提示词
            String prompt = "请将以下录音内容转录为文字，保持原有的语言风格和教学特点。" +
                    "如果有专业术语，请准确转录。如果有停顿或重复，可以适当整理但保持原意。";
            
            // 调用AI服务进行语音转文字（这里模拟实现）
            String transcriptText = simulateAudioToText(audioFilePath);
            
            // 更新数据库
            classSummaryMapper.updateTranscriptText(summaryId, transcriptText);
            
            log.info("录音转文字完成，字数: {}", transcriptText.length());
            return transcriptText;
            
        } catch (Exception e) {
            log.error("录音转文字失败", e);
            throw new RuntimeException("录音转文字失败: " + e.getMessage());
        }
    }
    
    @Override
    public String generateSummaryWithAI(Long summaryId, String transcriptText, String coursewareContent) {
        try {
            log.info("开始生成AI重点整理，转录文字长度: {}", transcriptText.length());
            
            // 构建AI分析提示词
            StringBuilder prompt = new StringBuilder();
            prompt.append("请基于以下课堂录音转录内容");
            if (coursewareContent != null && !coursewareContent.isEmpty()) {
                prompt.append("和课件内容");
            }
            prompt.append("，生成一份结构化的课堂重点整理文档。\n\n");
            
            prompt.append("要求：\n");
            prompt.append("1. 提取课堂的核心知识点和重点概念\n");
            prompt.append("2. 整理成清晰的层次结构（使用Markdown格式）\n");
            prompt.append("3. 包含关键概念、重要公式、案例分析等\n");
            prompt.append("4. 添加学习要点和注意事项\n");
            prompt.append("5. 如果有实例或案例，请详细说明\n\n");
            
            prompt.append("课堂录音转录内容：\n");
            prompt.append(transcriptText);
            
            if (coursewareContent != null && !coursewareContent.isEmpty()) {
                prompt.append("\n\n课件内容：\n");
                prompt.append(coursewareContent);
            }
            
            // 调用AI服务生成总结
            String summaryContent = aiService.simpleChat(prompt.toString());
            
            // 更新数据库
            classSummaryMapper.updateSummaryContent(summaryId, summaryContent);
            
            log.info("AI重点整理生成完成，内容长度: {}", summaryContent.length());
            return summaryContent;
            
        } catch (Exception e) {
            log.error("AI重点整理生成失败", e);
            throw new RuntimeException("AI重点整理生成失败: " + e.getMessage());
        }
    }
    
    @Override
    public void updateFinalContent(Long summaryId, String finalContent) {
        classSummaryMapper.updateFinalContent(summaryId, finalContent);
        log.info("更新最终文档内容成功，总结ID: {}", summaryId);
    }
    
    @Override
    public void publishClassSummary(Long summaryId) {
        classSummaryMapper.updateStatus(summaryId, "PUBLISHED", LocalDateTime.now());
        log.info("发布课堂总结成功，ID: {}", summaryId);
    }
    
    @Override
    public List<ClassSummary> getTeacherSummaries(Long teacherId) {
        return classSummaryMapper.selectByTeacherId(teacherId);
    }
    
    @Override
    public List<ClassSummary> getPublishedSummaries(Long courseId) {
        return classSummaryMapper.selectPublishedByCourseId(courseId);
    }
    
    @Override
    public List<ClassSummary> getAllPublishedSummaries() {
        return classSummaryMapper.selectAllPublished();
    }
    
    @Override
    public ClassSummary getSummaryById(Long summaryId) {
        return classSummaryMapper.selectById(summaryId);
    }
    
    @Override
    public void incrementViewCount(Long summaryId) {
        classSummaryMapper.incrementViewCount(summaryId);
    }
    
    @Override
    public List<ClassSummary> searchPublishedSummaries(String keyword) {
        return classSummaryMapper.searchPublished(keyword);
    }
    
    @Override
    public void deleteClassSummary(Long summaryId, Long teacherId) {
        ClassSummary summary = classSummaryMapper.selectById(summaryId);
        if (summary == null) {
            throw new RuntimeException("课堂总结不存在");
        }
        if (!summary.getTeacherId().equals(teacherId)) {
            throw new RuntimeException("无权限删除此课堂总结");
        }
        
        // 删除相关文件
        deleteAssociatedFiles(summary);
        
        // 删除数据库记录
        classSummaryMapper.deleteById(summaryId);
        log.info("删除课堂总结成功，ID: {}", summaryId);
    }
    
    @Override
    public Map<String, Object> getSummaryStatistics(Long teacherId) {
        List<ClassSummary> summaries = classSummaryMapper.selectByTeacherId(teacherId);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", summaries.size());
        
        long publishedCount = summaries.stream()
                .filter(s -> "PUBLISHED".equals(s.getStatus()))
                .count();
        statistics.put("publishedCount", publishedCount);
        
        long draftCount = summaries.stream()
                .filter(s -> "DRAFT".equals(s.getStatus()))
                .count();
        statistics.put("draftCount", draftCount);
        
        int totalViews = summaries.stream()
                .filter(s -> s.getViewCount() != null)
                .mapToInt(ClassSummary::getViewCount)
                .sum();
        statistics.put("totalViews", totalViews);
        
        return statistics;
    }
    
    /**
     * 模拟录音转文字功能
     * 实际实现中应该调用真实的语音识别服务
     */
    private String simulateAudioToText(String audioFilePath) {
        // 这里模拟语音转文字的结果
        return "今天我们学习的主题是Linux系统的基础命令操作。首先，我们来了解一下文件系统的基本概念。" +
                "在Linux中，一切皆文件，这是一个非常重要的概念。接下来我们学习几个常用的命令：" +
                "ls命令用于列出目录内容，cd命令用于切换目录，mkdir命令用于创建目录，" +
                "cp命令用于复制文件，mv命令用于移动或重命名文件。" +
                "这些命令是Linux操作的基础，同学们需要熟练掌握。" +
                "在实际使用中，还要注意权限的问题，chmod命令可以修改文件权限，" +
                "chown命令可以修改文件所有者。大家在课后要多练习这些基本操作。";
    }
    
    /**
     * 删除关联的文件
     */
    private void deleteAssociatedFiles(ClassSummary summary) {
        try {
            if (summary.getAudioFilePath() != null) {
                Files.deleteIfExists(Paths.get(summary.getAudioFilePath()));
            }
        } catch (IOException e) {
            log.warn("删除关联文件失败: {}", e.getMessage());
        }
    }
} 