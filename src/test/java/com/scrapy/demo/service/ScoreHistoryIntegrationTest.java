package com.scrapy.demo.service;

import com.scrapy.demo.domain.Score;
import com.scrapy.demo.domain.ScoreHistory;
import com.scrapy.demo.domain.Student;
import com.scrapy.demo.domain.Course;
import com.scrapy.demo.repository.ScoreHistoryRepository;
import com.scrapy.demo.repository.ScoreRepository;
import com.scrapy.demo.repository.StudentRepository;
import com.scrapy.demo.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ScoreHistoryIntegrationTest {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private ScoreHistoryRepository historyRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    private Score testScore;
    private Student testStudent;
    private Course testCourse;

    @BeforeEach
    public void setup() {
        historyRepository.deleteAll();
        scoreRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();

        testStudent = new Student();
        testStudent.setStudentNumber("2024009");
        testStudent.setName("集成测试用户");
        testStudent.setClazz("测试班级");
        testStudent.setMajor("测试专业");
        studentRepository.save(testStudent);

        testCourse = new Course();
        testCourse.setName("集成测试课程");
        testCourse.setCredit(2.0);
        testCourse.setTeacherName("测试老师");
        courseRepository.save(testCourse);

        testScore = new Score();
        testScore.setStudent(testStudent);
        testScore.setCourse(testCourse);
        testScore.setValue(75.0);
    }

    @Test
    public void testUpdateCreatesHistoryRecord() {
        Score saved = scoreService.saveScore(testScore);

        Score updated = scoreService.updateScoreValue(saved.getId(), 88.0);

        assertNotNull(updated);
        assertEquals(88.0, updated.getValue());

        List<ScoreHistory> histories = historyRepository.findAll();
        assertFalse(histories.isEmpty(), "score_history should have at least one record");

        ScoreHistory h = histories.get(0);
        assertEquals(saved.getId(), h.getScore().getId());
        assertEquals(75.0, h.getBeforeValue());
        assertEquals(88.0, h.getAfterValue());
    }
}
