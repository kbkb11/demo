package com.scrapy.demo.service;

import com.scrapy.demo.domain.Score;
import com.scrapy.demo.domain.ScoreHistory;
import com.scrapy.demo.repository.ScoreHistoryRepository;
import com.scrapy.demo.repository.ScoreRepository;
import com.scrapy.demo.repository.StudentRepository;
import com.scrapy.demo.repository.CourseRepository;
import com.scrapy.demo.repository.ExamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 成绩服务
 * 处理成绩的查询、创建、修改等业务逻辑，并维护成绩历史记录
 */
@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final ScoreHistoryRepository historyRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ExamRepository examRepository;

    public ScoreService(ScoreRepository scoreRepository, ScoreHistoryRepository historyRepository,
                        StudentRepository studentRepository, CourseRepository courseRepository,
                        ExamRepository examRepository) {
        this.scoreRepository = scoreRepository;
        this.historyRepository = historyRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.examRepository = examRepository;
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

        if (score.getExam() != null) {
            if (score.getExam().getId() == null) {
                examRepository.saveAndFlush(score.getExam());
            } else {
                examRepository.findById(score.getExam().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Exam does not exist: " + score.getExam().getId()));
            }
        }

        return scoreRepository.save(score);
    }

    /**
     * 更新成绩值，并记录成绩历史
     */
    @Transactional
    public Score updateScoreValue(Long scoreId, Double newValue) {
        Score payload = new Score();
        payload.setValue(newValue);
        return updateScore(scoreId, payload);
    }

    /**
     * 更新成绩信息（学生/课程/考试/分数）
     */
    @Transactional
    public Score updateScore(Long scoreId, Score payload) {
        Score existing = scoreRepository.findById(scoreId)
                .orElseThrow(() -> new IllegalArgumentException("成绩不存在：" + scoreId));

        if (payload != null) {
            if (payload.getStudent() != null) {
                Long studentId = payload.getStudent().getId();
                if (studentId == null) {
                    studentRepository.saveAndFlush(payload.getStudent());
                    existing.setStudent(payload.getStudent());
                } else {
                    existing.setStudent(studentRepository.findById(studentId)
                            .orElseThrow(() -> new IllegalArgumentException("Student does not exist: " + studentId)));
                }
            }

            if (payload.getCourse() != null) {
                Long courseId = payload.getCourse().getId();
                if (courseId == null) {
                    courseRepository.saveAndFlush(payload.getCourse());
                    existing.setCourse(payload.getCourse());
                } else {
                    existing.setCourse(courseRepository.findById(courseId)
                            .orElseThrow(() -> new IllegalArgumentException("Course does not exist: " + courseId)));
                }
            }

            if (payload.getExam() != null) {
                Long examId = payload.getExam().getId();
                if (examId == null) {
                    examRepository.saveAndFlush(payload.getExam());
                    existing.setExam(payload.getExam());
                } else {
                    existing.setExam(examRepository.findById(examId)
                            .orElseThrow(() -> new IllegalArgumentException("Exam does not exist: " + examId)));
                }
            }
        }

        Double oldValue = existing.getValue();
        Double newValue = payload == null ? null : payload.getValue();
        if (newValue != null) {
            existing.setValue(newValue);
        }

        Score saved = scoreRepository.save(existing);
        scoreRepository.flush();

        if (newValue != null && !Objects.equals(oldValue, newValue)) {
            ScoreHistory history = new ScoreHistory(saved, oldValue, newValue);
            historyRepository.save(history);
        }

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
