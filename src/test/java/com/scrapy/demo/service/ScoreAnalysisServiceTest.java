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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 成绩分析服务测试
 * 测试成绩统计、分析、排名等功能
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ScoreAnalysisServiceTest {

    @Autowired
    private ScoreAnalysisService scoreAnalysisService;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    private Student student1;
    private Student student2;
    private Course course1;
    private Course course2;

    @BeforeEach
    void setUp() {
        scoreRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();

        // 创建测试学生
        student1 = new Student();
        student1.setStudentNumber("2024001");
        student1.setName("张三");
        student1.setClazz("计算机1班");
        student1.setMajor("计算机科学与技术");
        studentRepository.save(student1);

        student2 = new Student();
        student2.setStudentNumber("2024002");
        student2.setName("李四");
        student2.setClazz("计算机1班");
        student2.setMajor("计算机科学与技术");
        studentRepository.save(student2);

        // 创建测试课程
        course1 = new Course();
        course1.setName("高等数学");
        course1.setCredit(4.0);
        course1.setTeacherName("王教授");
        courseRepository.save(course1);

        course2 = new Course();
        course2.setName("大学英语");
        course2.setCredit(3.0);
        course2.setTeacherName("李老师");
        courseRepository.save(course2);

        // 创建测试成绩
        Score score1 = new Score(student1, course1, 85.5);
        Score score2 = new Score(student1, course2, 78.0);
        Score score3 = new Score(student2, course1, 72.5);
        Score score4 = new Score(student2, course2, 85.0);

        scoreRepository.save(score1);
        scoreRepository.save(score2);
        scoreRepository.save(score3);
        scoreRepository.save(score4);
    }

    @Test
    void testAnalyzeAllScores() {
        Map<String, Object> analysis = scoreAnalysisService.analyzeAllScores();

        assertNotNull(analysis);
        assertTrue(analysis.containsKey("totalCount"));
        assertTrue(analysis.containsKey("average"));
        assertTrue(analysis.containsKey("highest"));
        assertTrue(analysis.containsKey("lowest"));
        assertTrue(analysis.containsKey("passRate"));
        assertTrue(analysis.containsKey("distribution"));

        assertEquals(4, analysis.get("totalCount"));
        double average = (Double) analysis.get("average");
        assertTrue(average > 70 && average < 90);
    }

    @Test
    void testAnalyzeCourseScores() {
        Map<String, Object> analysis = scoreAnalysisService.analyzeCourseScores(course1.getId());

        assertNotNull(analysis);
        assertEquals(2, analysis.get("totalCount")); // 高等数学有2个成绩
        assertTrue(analysis.containsKey("average"));
        assertTrue(analysis.containsKey("highest"));
        assertTrue(analysis.containsKey("lowest"));
    }

    @Test
    void testAnalyzeStudentScores() {
        Map<String, Object> analysis = scoreAnalysisService.analyzeStudentScores(student1.getId());

        assertNotNull(analysis);
        assertEquals(2, analysis.get("totalCount")); // 张三有2个成绩
        assertTrue(analysis.containsKey("average"));
    }

    @Test
    void testAnalyzeClassScores() {
        Map<String, Object> analysis = scoreAnalysisService.analyzeClassScores("计算机1班");

        assertNotNull(analysis);
        assertTrue((Integer) analysis.get("totalCount") >= 2);
    }

    @Test
    void testGetPassRateByScore() {
        Map<String, Double> passRates = scoreAnalysisService.getPassRateByScore();

        assertNotNull(passRates);
        // 每门课程都有及格率
        assertFalse(passRates.isEmpty());
    }

    @Test
    void testGetStudentRankings() {
        List<Map<String, Object>> rankings = scoreAnalysisService.getStudentRankings();

        assertNotNull(rankings);
        assertTrue(rankings.size() >= 2);

        // 检查排名字段
        for (Map<String, Object> ranking : rankings) {
            assertTrue(ranking.containsKey("studentId"));
            assertTrue(ranking.containsKey("averageScore"));
            assertTrue(ranking.containsKey("rank"));
        }

        // 检查排名顺序（降序）
        double previousScore = Double.MAX_VALUE;
        for (Map<String, Object> ranking : rankings) {
            double currentScore = (Double) ranking.get("averageScore");
            assertTrue(currentScore <= previousScore);
            previousScore = currentScore;
        }
    }

    @Test
    void testScoreDistribution() {
        Map<String, Object> analysis = scoreAnalysisService.analyzeAllScores();
        @SuppressWarnings("unchecked")
        Map<String, Integer> distribution = (Map<String, Integer>) analysis.get("distribution");

        assertNotNull(distribution);
        assertTrue(distribution.containsKey("0-59"));
        assertTrue(distribution.containsKey("60-69"));
        assertTrue(distribution.containsKey("70-79"));
        assertTrue(distribution.containsKey("80-89"));
        assertTrue(distribution.containsKey("90-100"));

        // 验证分布和
        int sum = distribution.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(4, sum);
    }

    @Test
    void testPassRateCalculation() {
        Map<String, Object> analysis = scoreAnalysisService.analyzeAllScores();
        double passRate = (Double) analysis.get("passRate");

        // 4个成绩中有3个及格（85.5, 78.0, 72.5, 85.0都>=60），所以及格率应该是100%
        assertEquals(100.0, passRate);
    }

    @Test
    void testEmptyScoresAnalysis() {
        scoreRepository.deleteAll();

        Map<String, Object> analysis = scoreAnalysisService.analyzeAllScores();

        assertNotNull(analysis);
        assertEquals(0, analysis.get("totalCount"));
        assertEquals(0.0, analysis.get("average"));
    }
}
