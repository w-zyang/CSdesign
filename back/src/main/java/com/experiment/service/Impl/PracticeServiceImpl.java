package com.experiment.service.Impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.experiment.constant.PracticeConstants;
import com.experiment.exception.PracticeException;
import com.experiment.mapper.PracticeMapper;
import com.experiment.mapper.StudentPracticeMapper;
import com.experiment.mapper.StudentExamMapper;
import com.experiment.mapper.StudentAnswerMapper;
import com.experiment.mapper.QuestionMapper;
import com.experiment.pojo.Practice;
import com.experiment.pojo.StudentPractice;
import com.experiment.pojo.StudentExam;
import com.experiment.pojo.StudentAnswer;
import com.experiment.pojo.Question;
import com.experiment.result.PageResult;
import com.experiment.service.PracticeService;

@Service
public class PracticeServiceImpl implements PracticeService {
    
    @Autowired
    private PracticeMapper practiceMapper;
    
    @Autowired
    private StudentPracticeMapper studentPracticeMapper;
    
    @Autowired
    private StudentExamMapper studentExamMapper;
    
    @Autowired
    private StudentAnswerMapper studentAnswerMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Override
    public List<Map<String, Object>> getPracticeTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        
        Map<String, Object> algorithm = new HashMap<>();
        algorithm.put("id", 1);
        algorithm.put("name", "算法练习");
        algorithm.put("description", "数据结构与算法基础练习");
        algorithm.put("icon", "💻");
        algorithm.put("duration", 30);
        algorithm.put("questionCount", 10);
        algorithm.put("type", PracticeConstants.TYPE_ALGORITHM);
        types.add(algorithm);
        
        Map<String, Object> programming = new HashMap<>();
        programming.put("id", 2);
        programming.put("name", "编程实践");
        programming.put("description", "实际编程项目练习");
        programming.put("icon", "🔧");
        programming.put("duration", 45);
        programming.put("questionCount", 8);
        programming.put("type", PracticeConstants.TYPE_PROGRAMMING);
        types.add(programming);
        
        Map<String, Object> systemDesign = new HashMap<>();
        systemDesign.put("id", 3);
        systemDesign.put("name", "系统设计");
        systemDesign.put("description", "软件系统设计练习");
        systemDesign.put("icon", "🏗️");
        systemDesign.put("duration", 60);
        systemDesign.put("questionCount", 15);
        systemDesign.put("type", PracticeConstants.TYPE_SYSTEM_DESIGN);
        types.add(systemDesign);
        
        Map<String, Object> database = new HashMap<>();
        database.put("id", 4);
        database.put("name", "数据库操作");
        database.put("description", "SQL和数据库管理练习");
        database.put("icon", "🗄️");
        database.put("duration", 90);
        database.put("questionCount", 5);
        database.put("type", PracticeConstants.TYPE_DATABASE);
        types.add(database);
        
        return types;
    }
    
    @Override
    public List<Map<String, Object>> getRecommendedPractices() {
        List<Map<String, Object>> practices = new ArrayList<>();
        
        Map<String, Object> practice1 = new HashMap<>();
        practice1.put("id", 1);
        practice1.put("title", "数组和链表操作");
        practice1.put("description", "练习数组和链表的基本操作和算法");
        practice1.put("type", "算法练习");
        practice1.put("difficulty", PracticeConstants.DIFFICULTY_EASY);
        practice1.put("difficultyText", "简单");
        practice1.put("duration", 15);
        practices.add(practice1);
        
        Map<String, Object> practice2 = new HashMap<>();
        practice2.put("id", 2);
        practice2.put("title", "栈和队列应用");
        practice2.put("description", "学习栈和队列在实际问题中的应用");
        practice2.put("type", "算法练习");
        practice2.put("difficulty", PracticeConstants.DIFFICULTY_MEDIUM);
        practice2.put("difficultyText", "中等");
        practice2.put("duration", 25);
        practices.add(practice2);
        
        Map<String, Object> practice3 = new HashMap<>();
        practice3.put("id", 3);
        practice3.put("title", "树结构遍历");
        practice3.put("description", "练习二叉树的各种遍历算法");
        practice3.put("type", "算法练习");
        practice3.put("difficulty", PracticeConstants.DIFFICULTY_MEDIUM);
        practice3.put("difficultyText", "中等");
        practice3.put("duration", 30);
        practices.add(practice3);
        
        Map<String, Object> practice4 = new HashMap<>();
        practice4.put("id", 4);
        practice4.put("title", "图论算法");
        practice4.put("description", "学习图的表示和基本算法");
        practice4.put("type", "算法练习");
        practice4.put("difficulty", PracticeConstants.DIFFICULTY_HARD);
        practice4.put("difficultyText", "困难");
        practice4.put("duration", 40);
        practices.add(practice4);
        
        return practices;
    }
    
    @Override
    public List<Map<String, Object>> getPracticeHistory(Long studentId) {
        List<StudentPractice> studentPractices = studentPracticeMapper.selectByStudentId(studentId);
        List<Map<String, Object>> history = new ArrayList<>();
        
        for (StudentPractice sp : studentPractices) {
            if (PracticeConstants.STUDENT_STATUS_COMPLETED.equals(sp.getStatus())) {
                Practice practice = practiceMapper.selectById(sp.getPracticeId());
                if (practice != null) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("id", sp.getId());
                    record.put("title", practice.getTitle());
                    record.put("score", sp.getScore());
                    record.put("totalScore", sp.getTotalScore());
                    record.put("accuracy", sp.getAccuracy());
                    record.put("completedTime", sp.getCompleteTime());
                    history.add(record);
                }
            }
        }
        
        return history;
    }
    
    @Override
    @Transactional
    public Map<String, Object> startPractice(Long practiceId, Long studentId) {
        System.out.println("📝 开始练习: practiceId=" + practiceId + ", studentId=" + studentId);
        
        // 检查Practice是否存在，如果不存在则创建（用于AI生成的练习）
        Practice practice = practiceMapper.selectById(practiceId);
        if (practice == null) {
            System.out.println("⚠️ Practice不存在，创建临时记录");
            practice = new Practice();
            practice.setTitle("AI智能练习");
            practice.setDescription("AI生成的个性化练习");
            practice.setType("ai_practice");
            practice.setDifficulty("medium");
            practice.setDuration(30);
            practice.setQuestionCount(5);
            practice.setStatus("active");
            practice.setCreateTime(LocalDateTime.now());
            practice.setUpdateTime(LocalDateTime.now());
            
            // 插入Practice记录（让数据库自动生成ID）
            practiceMapper.insert(practice);
            practiceId = practice.getId(); // 使用数据库生成的ID
            System.out.println("✅ Practice记录创建成功，ID=" + practiceId);
        }
        
        // 检查是否已经开始练习
        StudentPractice existingPractice = studentPracticeMapper.selectByStudentAndPractice(studentId, practiceId);
        if (existingPractice != null && PracticeConstants.STUDENT_STATUS_IN_PROGRESS.equals(existingPractice.getStatus())) {
            System.out.println("⚠️ 练习已经在进行中");
            throw new PracticeException(PracticeConstants.ERROR_PRACTICE_ALREADY_STARTED);
        }
        
        // 创建新的练习记录
        StudentPractice studentPractice = new StudentPractice();
        studentPractice.setStudentId(studentId);
        studentPractice.setPracticeId(practiceId);
        studentPractice.setTotalScore(PracticeConstants.DEFAULT_TOTAL_SCORE);
        studentPractice.setStatus(PracticeConstants.STUDENT_STATUS_IN_PROGRESS);
        studentPractice.setStartTime(LocalDateTime.now());
        studentPractice.setCreateTime(LocalDateTime.now());
        studentPractice.setUpdateTime(LocalDateTime.now());
        
        studentPracticeMapper.insert(studentPractice);
        System.out.println("✅ StudentPractice记录创建成功");
        
        Map<String, Object> result = new HashMap<>();
        result.put("practiceId", practiceId);
        result.put("studentId", studentId);
        result.put("status", "started");
        result.put("startTime", studentPractice.getStartTime());
        result.put("practice", practice);
        
        return result;
    }
    
    @Override
    @Transactional
    public Map<String, Object> submitPractice(Long practiceId, Long studentId, List<Map<String, Object>> answers) {
        // 检查练习记录是否存在
        StudentPractice studentPractice = studentPracticeMapper.selectByStudentAndPractice(studentId, practiceId);
        if (studentPractice == null) {
            throw new PracticeException(PracticeConstants.ERROR_PRACTICE_RECORD_NOT_FOUND);
        }
        
        if (PracticeConstants.STUDENT_STATUS_COMPLETED.equals(studentPractice.getStatus())) {
            throw new PracticeException(PracticeConstants.ERROR_PRACTICE_ALREADY_COMPLETED);
        }
        
        // 创建或获取StudentExam记录（用于关联答题记录）
        StudentExam studentExam = getOrCreateStudentExam(studentId, practiceId);
        
        // 保存每道题的答题记录到student_answer表
        List<StudentAnswer> studentAnswers = new ArrayList<>();
        int totalScore = 0;
        int correctCount = 0;
        
        System.out.println("========================================");
        System.out.println("📝 开始处理答题记录，共 " + answers.size() + " 道题");
        
        for (Map<String, Object> answerData : answers) {
            Long questionId = Long.valueOf(answerData.get("questionId").toString());
            String studentAnswerText = answerData.get("answer") != null ? answerData.get("answer").toString() : "";
            
            System.out.println("处理题目 ID=" + questionId + ", 学生答案=" + studentAnswerText);
            
            // 获取题目信息
            Question question = questionMapper.selectById(questionId);
            if (question == null) {
                System.out.println("⚠️ 题目不存在，跳过: questionId=" + questionId);
                continue;
            }
            
            System.out.println("✅ 找到题目: " + question.getContent() + ", 正确答案=" + question.getAnswer());
            
            // 判断答案是否正确
            boolean isCorrect = checkAnswer(question, studentAnswerText, answerData);
            int questionScore = isCorrect ? (question.getScore() != null ? question.getScore() : 10) : 0;
            
            System.out.println((isCorrect ? "✅ 答对了" : "❌ 答错了") + ", 得分=" + questionScore);
            
            // 创建答题记录
            StudentAnswer studentAnswer = new StudentAnswer();
            studentAnswer.setStudentExamId(studentExam.getId());
            studentAnswer.setQuestionId(questionId);
            studentAnswer.setAnswer(studentAnswerText);
            studentAnswer.setIsCorrect(isCorrect);
            studentAnswer.setScore(questionScore);
            studentAnswer.setCreateTime(LocalDateTime.now());
            studentAnswer.setUpdateTime(LocalDateTime.now());
            
            studentAnswers.add(studentAnswer);
            totalScore += questionScore;
            if (isCorrect) {
                correctCount++;
            }
        }
        
        System.out.println("========================================");
        System.out.println("📊 答题统计: 总分=" + totalScore + ", 正确数=" + correctCount + ", 总题数=" + answers.size());
        System.out.println("准备批量插入 " + studentAnswers.size() + " 条答题记录到 student_answer 表");
        
        // 批量插入答题记录
        if (!studentAnswers.isEmpty()) {
            try {
                studentAnswerMapper.batchInsert(studentAnswers);
                System.out.println("✅ 成功插入 " + studentAnswers.size() + " 条答题记录");
            } catch (Exception e) {
                System.err.println("❌ 批量插入答题记录失败: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        } else {
            System.out.println("⚠️ 没有答题记录需要插入");
        }
        System.out.println("========================================");
        
        // 计算正确率
        int accuracy = answers.isEmpty() ? 0 : (int) ((double) correctCount / answers.size() * 100);
        
        // 更新练习记录
        studentPractice.setScore(totalScore);
        studentPractice.setAccuracy(accuracy);
        studentPractice.setStatus(PracticeConstants.STUDENT_STATUS_COMPLETED);
        studentPractice.setCompleteTime(LocalDateTime.now());
        studentPractice.setUpdateTime(LocalDateTime.now());
        
        // 计算实际用时
        if (studentPractice.getStartTime() != null) {
            long durationMinutes = java.time.Duration.between(studentPractice.getStartTime(), LocalDateTime.now()).toMinutes();
            studentPractice.setDuration((int) durationMinutes);
        }
        
        studentPracticeMapper.update(studentPractice);
        
        // 更新StudentExam记录
        studentExam.setScore(totalScore);
        studentExam.setStatus("submitted");
        studentExam.setSubmitTime(LocalDateTime.now());
        studentExam.setUpdateTime(LocalDateTime.now());
        studentExamMapper.update(studentExam);
        
        Map<String, Object> result = new HashMap<>();
        result.put("practiceId", practiceId);
        result.put("studentId", studentId);
        result.put("score", totalScore);
        result.put("totalScore", studentPractice.getTotalScore());
        result.put("accuracy", accuracy);
        result.put("correctCount", correctCount);
        result.put("totalCount", answers.size());
        result.put("status", "completed");
        result.put("completeTime", studentPractice.getCompleteTime());
        result.put("duration", studentPractice.getDuration());
        
        return result;
    }
    
    /**
     * 获取或创建StudentExam记录
     */
    private StudentExam getOrCreateStudentExam(Long studentId, Long practiceId) {
        // 查找是否已存在（使用practiceId作为examId）
        StudentExam studentExam = studentExamMapper.selectByStudentAndExam(studentId, practiceId);
        
        if (studentExam == null) {
            // 创建新的StudentExam记录
            studentExam = new StudentExam();
            studentExam.setStudentId(studentId);
            studentExam.setExamId(practiceId); // 使用practiceId作为examId
            studentExam.setTotalScore(100);
            studentExam.setStatus("in_progress");
            studentExam.setStartTime(LocalDateTime.now());
            studentExam.setCreateTime(LocalDateTime.now());
            studentExam.setUpdateTime(LocalDateTime.now());
            studentExamMapper.insert(studentExam);
        }
        
        return studentExam;
    }
    
    /**
     * 检查答案是否正确
     */
    private boolean checkAnswer(Question question, String studentAnswer, Map<String, Object> answerData) {
        if (question.getAnswer() == null || studentAnswer == null) {
            return false;
        }
        
        String correctAnswer = question.getAnswer().trim();
        String userAnswer = studentAnswer.trim();
        
        // 根据题目类型判断
        String questionType = question.getType();
        
        if ("single_choice".equals(questionType) || "choice".equals(questionType)) {
            // 单选题：直接比较
            return correctAnswer.equalsIgnoreCase(userAnswer);
        } else if ("multiple_choice".equals(questionType) || "multiple".equals(questionType)) {
            // 多选题：比较选项（需要排序后比较）
            String[] correctOptions = correctAnswer.split(",");
            String[] userOptions = userAnswer.split(",");
            java.util.Arrays.sort(correctOptions);
            java.util.Arrays.sort(userOptions);
            return java.util.Arrays.equals(correctOptions, userOptions);
        } else if ("true_false".equals(questionType) || "judge".equals(questionType)) {
            // 判断题
            return correctAnswer.equalsIgnoreCase(userAnswer);
        } else if ("fill_blank".equals(questionType) || "short_answer".equals(questionType)) {
            // 填空题和简答题：包含关键词即可（简化处理）
            return correctAnswer.equalsIgnoreCase(userAnswer) || 
                   userAnswer.toLowerCase().contains(correctAnswer.toLowerCase());
        }
        
        return false;
    }
    
    @Override
    public Practice getPracticeById(Long id) {
        return practiceMapper.selectById(id);
    }
    
    @Override
    public List<Practice> getPracticesByCourseId(Long courseId) {
        return practiceMapper.selectByCourseId(courseId);
    }
    
    @Override
    public List<Practice> getPracticesByType(String type) {
        return practiceMapper.selectByType(type);
    }
    
    @Override
    public List<Practice> getPracticesByDifficulty(String difficulty) {
        return practiceMapper.selectByDifficulty(difficulty);
    }
    
    @Override
    public PageResult<Practice> getPracticesByPage(Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<Practice> practices = practiceMapper.selectByPage(offset, size);
        int total = practiceMapper.countTotal();
        
        PageResult<Practice> result = new PageResult<>();
        result.setRecords(practices);
        result.setTotal(total);
        
        return result;
    }
    
    @Override
    public boolean createPractice(Practice practice) {
        practice.setCreateTime(LocalDateTime.now());
        practice.setUpdateTime(LocalDateTime.now());
        return practiceMapper.insert(practice) > 0;
    }
    
    @Override
    public boolean updatePractice(Practice practice) {
        practice.setUpdateTime(LocalDateTime.now());
        return practiceMapper.update(practice) > 0;
    }
    
    @Override
    public boolean deletePractice(Long id) {
        return practiceMapper.deleteById(id) > 0;
    }
    
    @Override
    public StudentPractice getStudentPractice(Long studentId, Long practiceId) {
        return studentPracticeMapper.selectByStudentAndPractice(studentId, practiceId);
    }
    
    @Override
    public List<StudentPractice> getStudentPracticeHistory(Long studentId) {
        return studentPracticeMapper.selectByStudentId(studentId);
    }
    
    @Override
    public Double getPracticeAverageScore(Long practiceId) {
        return studentPracticeMapper.selectAverageScoreByPractice(practiceId);
    }
    
    @Override
    public Double getStudentAverageScore(Long studentId) {
        return studentPracticeMapper.selectAverageScoreByStudent(studentId);
    }
    
    // 计算得分的辅助方法
    private int calculateScore(List<Map<String, Object>> answers) {
        // 这里简化处理，实际应该根据题目答案计算
        // 假设每个答案正确得10分
        int correctCount = 0;
        for (Map<String, Object> answer : answers) {
            // 这里应该根据实际题目类型和答案进行判断
            // 暂时返回一个随机分数
            if (Math.random() > 0.3) {
                correctCount++;
            }
        }
        return correctCount * 10;
    }
    
    // 计算正确率的辅助方法
    private int calculateAccuracy(List<Map<String, Object>> answers) {
        if (answers == null || answers.isEmpty()) {
            return 0;
        }
        
        int correctCount = 0;
        for (Map<String, Object> answer : answers) {
            // 这里应该根据实际题目类型和答案进行判断
            // 暂时返回一个随机正确率
            if (Math.random() > 0.3) {
                correctCount++;
            }
        }
        
        return (int) ((double) correctCount / answers.size() * 100);
    }
} 