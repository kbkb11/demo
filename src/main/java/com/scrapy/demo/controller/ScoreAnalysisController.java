package com.scrapy.demo.controller;

import com.scrapy.demo.service.ScoreAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    public ScoreAnalysisController(ScoreAnalysisService scoreAnalysisService) {
        this.scoreAnalysisService = scoreAnalysisService;
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
}
