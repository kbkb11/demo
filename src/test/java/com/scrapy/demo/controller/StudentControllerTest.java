package com.scrapy.demo.controller;

import com.scrapy.demo.domain.Course;
import com.scrapy.demo.domain.Student;
import com.scrapy.demo.service.StudentService;
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

/**
 * 学生控制器测试
 * 测试学生管理相关的REST API
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "testuser", roles = {"ADMIN"})
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    private Student testStudent;

    @BeforeEach
    void setUp() {
        testStudent = new Student();
        testStudent.setStudentNumber("2024001");
        testStudent.setName("张三");
        testStudent.setClazz("计算机1班");
        testStudent.setMajor("计算机科学与技术");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testListStudents() throws Exception {
        studentService.save(testStudent);

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testGetStudentById() throws Exception {
        Student saved = studentService.save(testStudent);

        mockMvc.perform(get("/api/students/{id}", saved.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testGetStudentByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/students/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testGetStudentByStudentNumber() throws Exception {
        studentService.save(testStudent);

        mockMvc.perform(get("/api/students/number/{studentNumber}", "2024001"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testGetStudentsByClass() throws Exception {
        studentService.save(testStudent);

        Student student2 = new Student();
        student2.setStudentNumber("2024002");
        student2.setName("李四");
        student2.setClazz("计算机1班");
        student2.setMajor("计算机科学与技术");
        studentService.save(student2);

        mockMvc.perform(get("/api/students/class/{className}", "计算机1班"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testCreateStudent() throws Exception {
        String json = objectMapper.writeValueAsString(testStudent);

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testUpdateStudent() throws Exception {
        Student saved = studentService.save(testStudent);

        Student updateData = new Student();
        updateData.setStudentNumber("2024001");
        updateData.setName("张三修改");
        updateData.setClazz("计算机2班");
        updateData.setMajor("软件工程");

        String json = objectMapper.writeValueAsString(updateData);

        mockMvc.perform(put("/api/students/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testDeleteStudent() throws Exception {
        Student saved = studentService.save(testStudent);

        mockMvc.perform(delete("/api/students/{id}", saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testDeleteStudentNotFound() throws Exception {
        mockMvc.perform(delete("/api/students/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void testCreateStudentValidationError() throws Exception {
        Student invalidStudent = new Student();
        invalidStudent.setName("没有学号的学生");

        String json = objectMapper.writeValueAsString(invalidStudent);

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }
}
