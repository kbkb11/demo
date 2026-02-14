package com.scrapy.demo.service;

import com.scrapy.demo.domain.Score;
import com.scrapy.demo.domain.ScoreHistory;
import com.scrapy.demo.repository.ScoreHistoryRepository;
import com.scrapy.demo.repository.ScoreRepository;
import com.scrapy.demo.repository.StudentRepository;
import com.scrapy.demo.repository.CourseRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 成绩服务
 * 处理成绩的查询、创建、修改等业务逻辑，并维护成绩历史记录
 */
@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final ScoreHistoryRepository historyRepository;
    private final JdbcTemplate jdbcTemplate;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public ScoreService(ScoreRepository scoreRepository, ScoreHistoryRepository historyRepository, JdbcTemplate jdbcTemplate,
                        StudentRepository studentRepository, CourseRepository courseRepository) {
        this.scoreRepository = scoreRepository;
        this.historyRepository = historyRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    /**
     * 获取所有成绩
     */
    public List<Score> listAllScores() {
        return scoreRepository.findAll();
    }

    /**
     * 根据ID查找成绩
     */
    public Score findById(Long id) {
        return scoreRepository.findById(id).orElse(null);
    }

    /**
     * 根据学生ID查找成绩
     */
    public List<Score> findByStudentId(Long studentId) {
        return scoreRepository.findByStudentId(studentId);
    }

    /**
     * 根据课程ID查找成绩
     */
    public List<Score> findByCourseId(Long courseId) {
        return scoreRepository.findByCourseId(courseId);
    }

    /**
     * 保存成绩
     */
    @Transactional
    public Score saveScore(Score score) {
        // Ensure student is persisted
        if (score.getStudent() != null) {
            if (score.getStudent().getId() == null) {
                studentRepository.saveAndFlush(score.getStudent());
            } else {
                // ensure exists in DB
                studentRepository.findById(score.getStudent().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Student does not exist: " + score.getStudent().getId()));
            }
        }

        // Ensure course is persisted
        if (score.getCourse() != null) {
            if (score.getCourse().getId() == null) {
                courseRepository.saveAndFlush(score.getCourse());
            } else {
                courseRepository.findById(score.getCourse().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Course does not exist: " + score.getCourse().getId()));
            }
        }

        return scoreRepository.save(score);
    }

    /**
     * 更新成绩值，并记录成绩历史
     */
    @Transactional
    public Score updateScoreValue(Long scoreId, Double newValue) {
        Score existing = scoreRepository.findById(scoreId)
                .orElseThrow(() -> new IllegalArgumentException("成绩不存在：" + scoreId));
        
        // 记录旧值
        Double oldValue = existing.getValue();

        // 先更新成绩并保存，确保父表记录存在并已持久化
        existing.setValue(newValue);
        Score saved = scoreRepository.save(existing);
        // 强制刷新到数据库，确保 ID 已分配（GenerationType.IDENTITY 情况）
        scoreRepository.flush();

        // MySQL 不能使用双引号包字段名，改为标准标识符写法。
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
                "INSERT INTO score_history (score_id, before_value, after_value, modified_at) VALUES (?, ?, ?, ?)",
                saved.getId(), oldValue, newValue, Timestamp.valueOf(now)
        );

        // 返回已保存的成绩
        return saved;
    }

    /**
     * 删除成绩
     */
    @Transactional
    public boolean delete(Long id) {
        Score score = findById(id);
        if (score == null) {
            return false;
        }
        scoreRepository.delete(score);
        return true;
    }
}
