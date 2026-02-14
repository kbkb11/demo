package com.scrapy.demo.controller;

import com.scrapy.demo.domain.Course;
import com.scrapy.demo.domain.Score;
import com.scrapy.demo.domain.Student;
import com.scrapy.demo.service.CourseService;
import com.scrapy.demo.service.ScoreAnalysisService;
import com.scrapy.demo.service.ScoreService;
import com.scrapy.demo.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * 成绩分析控制器测试
 * 测试成绩统计和分析相关的REST API
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "testuser", roles = {"ADMIN"})
class ScoreAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScoreAnalysisService scoreAnalysisService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private ScoreService scoreService;

    private Student student1;
    private Course course1;

    @BeforeEach
    void setUp() {
        // 创建测试学生
        student1 = new Student();
        student1.setStudentNumber("2024001");
        student1.setName("张三");
        student1.setClazz("计算机1班");
        student1.setMajor("计算机科学与技术");
        student1 = studentService.save(student1);

        // 创建测试课程
        course1 = new Course();
        course1.setName("高等数学");
        course1.setCredit(4.0);
        course1.setTeacherName("王教授");
        course1 = courseService.save(course1);

        // 创建测试成绩
        Score score = new Score(student1, course1, 85.5);
        scoreService.saveScore(score);
    }

    @Test
    void testGetOverallAnalysis() throws Exception {
        mockMvc.perform(get("/api/analysis/overall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label", is("整体成绩")))
                .andExpect(jsonPath("$.totalCount", notNullValue()))
                .andExpect(jsonPath("$.average", notNullValue()))
                .andExpect(jsonPath("$.highest", notNullValue()))
                .andExpect(jsonPath("$.lowest", notNullValue()))
                .andExpect(jsonPath("$.passRate", notNullValue()))
                .andExpect(jsonPath("$.distribution", notNullValue()));
    }

    @Test
    void testGetCourseAnalysis() throws Exception {
        mockMvc.perform(get("/api/analysis/course/{courseId}", course1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label", containsString("课程成绩")))
                .andExpect(jsonPath("$.totalCount", greaterThan(0)))
                .andExpect(jsonPath("$.average", notNullValue()));
    }

    @Test
    void testGetStudentAnalysis() throws Exception {
        mockMvc.perform(get("/api/analysis/student/{studentId}", student1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label", containsString("学生成绩")))
                .andExpect(jsonPath("$.totalCount", greaterThan(0)));
    }

    @Test
    void testGetClassAnalysis() throws Exception {
        mockMvc.perform(get("/api/analysis/class/{className}", "计算机1班"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label", containsString("班级")))
                .andExpect(jsonPath("$.totalCount", greaterThanOrEqualTo(0)));
    }

    @Test
    void testGetPassRates() throws Exception {
        mockMvc.perform(get("/api/analysis/pass-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void testGetStudentRankings() throws Exception {
        mockMvc.perform(get("/api/analysis/rankings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.ArrayList.class)))
                .andExpect(jsonPath("$[0].studentId", notNullValue()))
                .andExpect(jsonPath("$[0].averageScore", notNullValue()))
                .andExpect(jsonPath("$[0].rank", notNullValue()));
    }
}
