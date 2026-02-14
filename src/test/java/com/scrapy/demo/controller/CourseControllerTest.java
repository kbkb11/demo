package com.scrapy.demo.controller;

import com.scrapy.demo.domain.Course;
import com.scrapy.demo.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * 课程控制器测试
 * 测试课程管理相关的REST API
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "testuser", roles = {"ADMIN"})
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseService courseService;

    @Autowired
    private ObjectMapper objectMapper;

    private Course testCourse;

    @BeforeEach
    void setUp() {
        testCourse = new Course();
        testCourse.setName("高等数学");
        testCourse.setCredit(4.0);
        testCourse.setTeacherName("王教授");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testListCourses() throws Exception {
        courseService.save(testCourse);

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testGetCourseById() throws Exception {
        Course saved = courseService.save(testCourse);

        mockMvc.perform(get("/api/courses/{id}", saved.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testGetCourseByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/courses/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testGetCourseByName() throws Exception {
        courseService.save(testCourse);

        mockMvc.perform(get("/api/courses/name/{courseName}", "高等数学"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testGetCoursesByTeacher() throws Exception {
        courseService.save(testCourse);

        Course course2 = new Course();
        course2.setName("大学英语");
        course2.setCredit(3.0);
        course2.setTeacherName("王教授");
        courseService.save(course2);

        mockMvc.perform(get("/api/courses/teacher/{teacherName}", "王教授"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testCreateCourse() throws Exception {
        String json = objectMapper.writeValueAsString(testCourse);

        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testUpdateCourse() throws Exception {
        Course saved = courseService.save(testCourse);

        Course updateData = new Course();
        updateData.setName("高等数学（高级）");
        updateData.setCredit(5.0);
        updateData.setTeacherName("李教授");

        String json = objectMapper.writeValueAsString(updateData);

        mockMvc.perform(put("/api/courses/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testDeleteCourse() throws Exception {
        Course saved = courseService.save(testCourse);

        mockMvc.perform(delete("/api/courses/{id}", saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testDeleteCourseNotFound() throws Exception {
        mockMvc.perform(delete("/api/courses/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testCreateCourseValidationError() throws Exception {
        Course invalidCourse = new Course();
        invalidCourse.setCredit(4.0);
        // 缺少name和teacherName

        String json = objectMapper.writeValueAsString(invalidCourse);

        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }
}
