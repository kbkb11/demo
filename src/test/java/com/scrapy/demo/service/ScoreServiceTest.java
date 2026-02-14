package com.scrapy.demo.service;

import com.scrapy.demo.domain.Course;
import com.scrapy.demo.domain.Score;
import com.scrapy.demo.domain.Student;
import com.scrapy.demo.repository.CourseRepository;
import com.scrapy.demo.repository.ScoreRepository;
import com.scrapy.demo.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import jakarta.validation.ConstraintViolationException;

/**
 * 成绩服务测试
 * 测试成绩管理及历史记录相关业务逻辑
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ScoreServiceTest {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    private Score testScore;
    private Student testStudent;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        scoreRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();

        // 创建测试学生
        testStudent = new Student();
        testStudent.setStudentNumber("2024001");
        testStudent.setName("张三");
        testStudent.setClazz("计算机1班");
        testStudent.setMajor("计算机科学与技术");
        studentRepository.save(testStudent);

        // 创建测试课程
        testCourse = new Course();
        testCourse.setName("高等数学");
        testCourse.setCredit(4.0);
        testCourse.setTeacherName("王教授");
        courseRepository.save(testCourse);

        // 创建测试成绩
        testScore = new Score();
        testScore.setStudent(testStudent);
        testScore.setCourse(testCourse);
        testScore.setValue(85.5);
    }

    @Test
    void testSaveScore() {
        Score saved = scoreService.saveScore(testScore);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(85.5, saved.getValue());
        assertEquals("张三", saved.getStudent().getName());
        assertEquals("高等数学", saved.getCourse().getName());
    }

    @Test
    void testFindById() {
        Score saved = scoreService.saveScore(testScore);

        Score found = scoreService.findById(saved.getId());

        assertNotNull(found);
        assertEquals(85.5, found.getValue());
    }

    @Test
    void testFindByStudentId() {
        scoreService.saveScore(testScore);

        Score score2 = new Score();
        score2.setStudent(testStudent);
        score2.setCourse(testCourse);
        score2.setValue(90.0);
        scoreService.saveScore(score2);

        List<Score> scores = scoreService.findByStudentId(testStudent.getId());

        assertNotNull(scores);
        assertTrue(scores.size() >= 1);
    }

    @Test
    void testFindByCourseId() {
        scoreService.saveScore(testScore);

        List<Score> scores = scoreService.findByCourseId(testCourse.getId());

        assertNotNull(scores);
        assertTrue(scores.size() >= 1);
    }

    @Test
    void testUpdateScoreValue() {
        Score saved = scoreService.saveScore(testScore);

        Score updated = scoreService.updateScoreValue(saved.getId(), 92.0);

        assertNotNull(updated);
        assertEquals(92.0, updated.getValue());
    }

    @Test
    void testDeleteScore() {
        Score saved = scoreService.saveScore(testScore);

        boolean deleted = scoreService.delete(saved.getId());

        assertTrue(deleted);
        assertNull(scoreService.findById(saved.getId()));
    }

    @Test
    void testListAllScores() {
        scoreService.saveScore(testScore);

        Score score2 = new Score();
        score2.setStudent(testStudent);
        score2.setCourse(testCourse);
        score2.setValue(88.0);
        scoreService.saveScore(score2);

        List<Score> scores = scoreService.listAllScores();

        assertNotNull(scores);
        assertTrue(scores.size() >= 2);
    }

    @Test
    void testUpdateScoreNonexistent() {
        assertThrows(IllegalArgumentException.class, () -> {
            scoreService.updateScoreValue(999L, 90.0);
        });
    }

    @Test
    void testScoreValidation() {
        testScore.setValue(-5.0); // 无效的负分数
        assertThrows(ConstraintViolationException.class, () -> {
            scoreService.saveScore(testScore);
        });
    }

    @Test
    void testScoreWithRecordedAtTimestamp() {
        Score saved = scoreService.saveScore(testScore);

        assertNotNull(saved.getRecordedAt());
    }
}
