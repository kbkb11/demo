package com.scrapy.demo.controller;

import com.scrapy.demo.service.LlmReasonService;
import com.scrapy.demo.service.ScoreAnalysisService;
import com.scrapy.demo.service.TeachingInsightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 成绩统计与分析控制器
 * 提供成绩分析、统计、排名等API端点
 */
@RestController
@RequestMapping("/api/analysis")
public class ScoreAnalysisController {

    private final ScoreAnalysisService scoreAnalysisService;
    private final TeachingInsightService teachingInsightService;
    private final LlmReasonService llmReasonService;

    public ScoreAnalysisController(ScoreAnalysisService scoreAnalysisService,
                                   TeachingInsightService teachingInsightService,
                                   LlmReasonService llmReasonService) {
        this.scoreAnalysisService = scoreAnalysisService;
        this.teachingInsightService = teachingInsightService;
        this.llmReasonService = llmReasonService;
    }

    /**
     * 获取整体成绩统计
     * GET /api/analysis/overall
     */
    @GetMapping("/overall")
    public ResponseEntity<Map<String, Object>> getOverallAnalysis() {
        return ResponseEntity.ok(scoreAnalysisService.analyzeAllScores());
    }

    /**
     * 获取指定课程的成绩统计
     * GET /api/analysis/course/{courseId}
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<Map<String, Object>> getCourseAnalysis(@PathVariable Long courseId) {
        return ResponseEntity.ok(scoreAnalysisService.analyzeCourseScores(courseId));
    }

    /**
     * 获取指定学生的成绩统计
     * GET /api/analysis/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentAnalysis(@PathVariable Long studentId) {
        return ResponseEntity.ok(scoreAnalysisService.analyzeStudentScores(studentId));
    }

    /**
     * 获取指定班级的成绩统计
     * GET /api/analysis/class/{className}
     */
    @GetMapping("/class/{className}")
    public ResponseEntity<Map<String, Object>> getClassAnalysis(@PathVariable String className) {
        return ResponseEntity.ok(scoreAnalysisService.analyzeClassScores(className));
    }

    /**
     * 获取各课程的及格率
     * GET /api/analysis/pass-rates
     */
    @GetMapping("/pass-rates")
    public ResponseEntity<Map<String, Double>> getPassRates() {
        return ResponseEntity.ok(scoreAnalysisService.getPassRateByScore());
    }

    /**
     * 获取学生排名（按平均分）
     * GET /api/analysis/rankings
     */
    @GetMapping("/rankings")
    public ResponseEntity<List<Map<String, Object>>> getStudentRankings() {
        return ResponseEntity.ok(scoreAnalysisService.getStudentRankings());
    }

    /**
     * 各考试平均分趋势
     * GET /api/analysis/exam-averages
     */
    @GetMapping("/exam-averages")
    public ResponseEntity<Map<String, Double>> getExamAverages() {
        return ResponseEntity.ok(scoreAnalysisService.getExamAverages());
    }

    /**
     * 各考试及格率趋势
     * GET /api/analysis/exam-passrates
     */
    @GetMapping("/exam-passrates")
    public ResponseEntity<Map<String, Double>> getExamPassRates() {
        return ResponseEntity.ok(scoreAnalysisService.getExamPassRates());
    }

    /**
     * 班级总览：整体水平、趋势、风险学生比例、偏科信息
     * GET /api/analysis/classroom/{className}/overview
     */
    @GetMapping("/classroom/{className}/overview")
    public ResponseEntity<Map<String, Object>> getClassroomOverview(@PathVariable String className) {
        return ResponseEntity.ok(teachingInsightService.getClassOverview(className));
    }

    /**
     * 教师端学生进步/后退标注
     * GET /api/analysis/classroom/{className}/student-flags
     */
    @GetMapping("/classroom/{className}/student-flags")
    public ResponseEntity<List<Map<String, Object>>> getStudentFlags(@PathVariable String className) {
        return ResponseEntity.ok(teachingInsightService.getClassStudentFlags(className));
    }

    /**
     * 学生个性化学习资料推荐（支持LLM生成推荐原因）
     * GET /api/analysis/student/{studentId}/recommendations
     */
    @GetMapping("/student/{studentId}/recommendations")
    public ResponseEntity<Map<String, Object>> getStudentRecommendations(@PathVariable Long studentId) {
        try {
            return ResponseEntity.ok(teachingInsightService.getStudentRecommendations(studentId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/llm/reason")
    public ResponseEntity<Map<String, String>> reason(@RequestBody Map<String, Object> payload) {
        String reason = llmReasonService.buildRecommendationReason(payload);
        return ResponseEntity.ok(Collections.singletonMap("reason", reason));
    }
}
