package com.experiment.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.experiment.pojo.ClassSummary;
import com.experiment.pojo.ClassSummaryDTO;
import com.experiment.result.Result;
import com.experiment.service.ClassSummaryService;

import lombok.extern.slf4j.Slf4j;

/**
 * 课堂重点整理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/class-summary")
public class ClassSummaryController {

    @Autowired
    private ClassSummaryService classSummaryService;

    /**
     * 创建课堂总结
     */
    @PostMapping("/create")
    public Result<ClassSummary> createClassSummary(@RequestBody ClassSummaryDTO dto) {
        log.info("创建课堂总结，课程ID: {}, 教师ID: {}", dto.getCourseId(), dto.getTeacherId());
        
        try {
            ClassSummary classSummary = classSummaryService.createClassSummary(dto);
            return Result.success("创建课堂总结成功", classSummary);
        } catch (Exception e) {
            log.error("创建课堂总结失败", e);
            return Result.error("创建课堂总结失败: " + e.getMessage());
        }
    }

    /**
     * 上传录音文件
     */
    @PostMapping("/upload-audio")
    public Result<String> uploadAudioFile(@RequestParam("audioFile") MultipartFile audioFile,
                                         @RequestParam("courseId") Long courseId) {
        log.info("上传录音文件，课程ID: {}, 文件大小: {}", courseId, audioFile.getSize());
        
        try {
            if (audioFile.isEmpty()) {
                return Result.error("录音文件不能为空");
            }
            
            String filePath = classSummaryService.uploadAudioFile(audioFile, courseId);
            return Result.success("录音文件上传成功", filePath);
        } catch (Exception e) {
            log.error("上传录音文件失败", e);
            return Result.error("上传录音文件失败: " + e.getMessage());
        }
    }

    /**
     * 处理录音（语音转文字）
     */
    @PostMapping("/process-audio")
    public Result<String> processAudioToText(@RequestBody Map<String, Object> request) {
        Long summaryId = ((Number) request.get("summaryId")).longValue();
        String audioFilePath = (String) request.get("audioFilePath");
        
        log.info("处理录音转文字，总结ID: {}", summaryId);
        
        try {
            String transcriptText = classSummaryService.processAudioToText(summaryId, audioFilePath);
            return Result.success("录音转文字成功", transcriptText);
        } catch (Exception e) {
            log.error("录音转文字失败", e);
            return Result.error("录音转文字失败: " + e.getMessage());
        }
    }

    /**
     * 生成AI重点整理
     */
    @PostMapping("/generate-summary")
    public Result<String> generateSummaryWithAI(@RequestBody Map<String, Object> request) {
        Long summaryId = ((Number) request.get("summaryId")).longValue();
        String transcriptText = (String) request.get("transcriptText");
        String coursewareContent = (String) request.get("coursewareContent");
        
        log.info("生成AI重点整理，总结ID: {}", summaryId);
        
        try {
            String summaryContent = classSummaryService.generateSummaryWithAI(summaryId, transcriptText, coursewareContent);
            return Result.success("AI重点整理生成成功", summaryContent);
        } catch (Exception e) {
            log.error("AI重点整理生成失败", e);
            return Result.error("AI重点整理生成失败: " + e.getMessage());
        }
    }

    /**
     * 更新最终文档内容
     */
    @PutMapping("/update-content/{summaryId}")
    public Result<Void> updateFinalContent(@PathVariable Long summaryId,
                                          @RequestBody Map<String, String> request) {
        String finalContent = request.get("finalContent");
        
        log.info("更新最终文档内容，总结ID: {}", summaryId);
        
        try {
            classSummaryService.updateFinalContent(summaryId, finalContent);
            return Result.success("更新文档内容成功");
        } catch (Exception e) {
            log.error("更新文档内容失败", e);
            return Result.error("更新文档内容失败: " + e.getMessage());
        }
    }

    /**
     * 发布课堂总结
     */
    @PutMapping("/publish/{summaryId}")
    public Result<Void> publishClassSummary(@PathVariable Long summaryId) {
        log.info("发布课堂总结，ID: {}", summaryId);
        
        try {
            classSummaryService.publishClassSummary(summaryId);
            return Result.success("发布课堂总结成功");
        } catch (Exception e) {
            log.error("发布课堂总结失败", e);
            return Result.error("发布课堂总结失败: " + e.getMessage());
        }
    }

    /**
     * 获取教师的课堂总结列表
     */
    @GetMapping("/teacher/{teacherId}")
    public Result<List<ClassSummary>> getTeacherSummaries(@PathVariable Long teacherId) {
        log.info("获取教师课堂总结列表，教师ID: {}", teacherId);
        
        try {
            List<ClassSummary> summaries = classSummaryService.getTeacherSummaries(teacherId);
            return Result.success("获取教师课堂总结列表成功", summaries);
        } catch (Exception e) {
            log.error("获取教师课堂总结列表失败", e);
            return Result.error("获取教师课堂总结列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取课程的已发布总结列表
     */
    @GetMapping("/course/{courseId}")
    public Result<List<ClassSummary>> getPublishedSummaries(@PathVariable Long courseId) {
        log.info("获取课程已发布总结列表，课程ID: {}", courseId);
        
        try {
            List<ClassSummary> summaries = classSummaryService.getPublishedSummaries(courseId);
            return Result.success("获取课程已发布总结列表成功", summaries);
        } catch (Exception e) {
            log.error("获取课程已发布总结列表失败", e);
            return Result.error("获取课程已发布总结列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有已发布的总结（学生端）
     */
    @GetMapping("/published")
    public Result<List<ClassSummary>> getAllPublishedSummaries() {
        log.info("获取所有已发布的课堂总结");
        
        try {
            List<ClassSummary> summaries = classSummaryService.getAllPublishedSummaries();
            return Result.success("获取所有已发布总结成功", summaries);
        } catch (Exception e) {
            log.error("获取所有已发布总结失败", e);
            return Result.error("获取所有已发布总结失败: " + e.getMessage());
        }
    }

    /**
     * 获取课堂总结详情
     */
    @GetMapping("/detail/{summaryId}")
    public Result<ClassSummary> getSummaryDetail(@PathVariable Long summaryId) {
        log.info("获取课堂总结详情，ID: {}", summaryId);
        
        try {
            ClassSummary summary = classSummaryService.getSummaryById(summaryId);
            if (summary != null) {
                // 增加浏览次数
                classSummaryService.incrementViewCount(summaryId);
                return Result.success("获取课堂总结详情成功", summary);
            } else {
                return Result.error("课堂总结不存在");
            }
        } catch (Exception e) {
            log.error("获取课堂总结详情失败", e);
            return Result.error("获取课堂总结详情失败: " + e.getMessage());
        }
    }

    /**
     * 搜索已发布的课堂总结
     */
    @GetMapping("/search")
    public Result<List<ClassSummary>> searchPublishedSummaries(@RequestParam String keyword) {
        log.info("搜索已发布的课堂总结，关键词: {}", keyword);
        
        try {
            List<ClassSummary> summaries = classSummaryService.searchPublishedSummaries(keyword);
            return Result.success("搜索课堂总结成功", summaries);
        } catch (Exception e) {
            log.error("搜索课堂总结失败", e);
            return Result.error("搜索课堂总结失败: " + e.getMessage());
        }
    }

    /**
     * 删除课堂总结
     */
    @DeleteMapping("/delete/{summaryId}")
    public Result<Void> deleteClassSummary(@PathVariable Long summaryId,
                                          @RequestParam Long teacherId) {
        log.info("删除课堂总结，ID: {}, 教师ID: {}", summaryId, teacherId);
        
        try {
            classSummaryService.deleteClassSummary(summaryId, teacherId);
            return Result.success("删除课堂总结成功");
        } catch (Exception e) {
            log.error("删除课堂总结失败", e);
            return Result.error("删除课堂总结失败: " + e.getMessage());
        }
    }

    /**
     * 获取课堂总结统计信息
     */
    @GetMapping("/statistics/{teacherId}")
    public Result<Map<String, Object>> getSummaryStatistics(@PathVariable Long teacherId) {
        log.info("获取课堂总结统计信息，教师ID: {}", teacherId);
        
        try {
            Map<String, Object> statistics = classSummaryService.getSummaryStatistics(teacherId);
            return Result.success("获取统计信息成功", statistics);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }
} 