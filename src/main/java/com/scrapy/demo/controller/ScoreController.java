package com.scrapy.demo.controller;

import com.scrapy.demo.domain.Score;
import com.scrapy.demo.service.ScoreService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成绩管理控制器
 * 提供成绩的查询、创建、修改、删除等功能
 */
@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    /**
     * 获取所有成绩列表
     * GET /api/scores
     */
    @GetMapping
    public List<Score> list() {
        return scoreService.listAllScores();
    }

    /**
     * 根据ID获取成绩信息
     * GET /api/scores/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Score> get(@PathVariable Long id) {
        Score score = scoreService.findById(id);
        if (score == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(score);
    }

    /**
     * 创建新成绩
     * POST /api/scores
     */
    @PostMapping
    public ResponseEntity<Score> create(@Valid @RequestBody Score score) {
        return ResponseEntity.ok(scoreService.saveScore(score));
    }

    /**
     * 更新成绩
     * PUT /api/scores/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Score> updateValue(@PathVariable Long id, @RequestParam Double value) {
        try {
            Score updated = scoreService.updateScoreValue(id, value);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 按学生ID查询成绩
     * GET /api/scores/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public List<Score> listByStudent(@PathVariable Long studentId) {
        return scoreService.findByStudentId(studentId);
    }

    /**
     * 按课程ID查询成绩
     * GET /api/scores/course/{courseId}
     */
    @GetMapping("/course/{courseId}")
    public List<Score> listByCourse(@PathVariable Long courseId) {
        return scoreService.findByCourseId(courseId);
    }

    /**
     * 删除成绩
     * DELETE /api/scores/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Score score = scoreService.findById(id);
        if (score == null) {
            return ResponseEntity.notFound().build();
        }
        scoreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
