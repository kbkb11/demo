package com.scrapy.demo.controller;

import com.scrapy.demo.domain.Course;
import com.scrapy.demo.domain.Score;
import com.scrapy.demo.domain.Student;
import com.scrapy.demo.service.CourseService;
import com.scrapy.demo.service.ScoreService;
import com.scrapy.demo.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * 成绩控制器测试
 * 测试成绩管理相关的REST API
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "testuser", roles = {"ADMIN"})
class ScoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private ObjectMapper objectMapper;

    private Score testScore;
    private Student testStudent;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        // 创建测试学生
        testStudent = new Student();
        testStudent.setStudentNumber("2024001");
        testStudent.setName("张三");
        testStudent.setClazz("计算机1班");
        testStudent.setMajor("计算机科学与技术");
        testStudent = studentService.save(testStudent);

        // 创建测试课程
        testCourse = new Course();
        testCourse.setName("高等数学");
        testCourse.setCredit(4.0);
        testCourse.setTeacherName("王教授");
        testCourse = courseService.save(testCourse);

        // 创建测试成绩
        testScore = new Score();
        testScore.setStudent(testStudent);
        testScore.setCourse(testCourse);
        testScore.setValue(85.5);
    }

    @Test
    void testListScores() throws Exception {
        scoreService.saveScore(testScore);

        mockMvc.perform(get("/api/scores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].value", notNullValue()));
    }

    @Test
    void testGetScoreById() throws Exception {
        Score saved = scoreService.saveScore(testScore);

        mockMvc.perform(get("/api/scores/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", is(85.5)));
    }

    @Test
    void testGetScoreByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/scores/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetScoresByStudent() throws Exception {
        scoreService.saveScore(testScore);

        mockMvc.perform(get("/api/scores/student/{studentId}", testStudent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void testGetScoresByCourse() throws Exception {
        scoreService.saveScore(testScore);

        mockMvc.perform(get("/api/scores/course/{courseId}", testCourse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void testCreateScore() throws Exception {
        String json = objectMapper.writeValueAsString(testScore);

        mockMvc.perform(post("/api/scores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", is(85.5)))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void testUpdateScore() throws Exception {
        Score saved = scoreService.saveScore(testScore);

        mockMvc.perform(put("/api/scores/{id}", saved.getId())
                .param("value", "92.5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", is(92.5)));
    }

    @Test
    void testDeleteScore() throws Exception {
        Score saved = scoreService.saveScore(testScore);

        mockMvc.perform(delete("/api/scores/{id}", saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteScoreNotFound() throws Exception {
        mockMvc.perform(delete("/api/scores/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
