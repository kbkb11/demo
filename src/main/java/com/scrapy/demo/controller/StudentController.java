package com.scrapy.demo.controller;

import com.scrapy.demo.domain.Student;
import com.scrapy.demo.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生管理控制器
 * 提供学生信息的查询、创建、修改、删除等功能
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * 获取所有学生列表
     * GET /api/students
     */
    @GetMapping
    public List<Student> list() {
        return studentService.listAllStudents();
    }

    /**
     * 根据ID获取学生信息
     * GET /api/students/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Student> get(@PathVariable Long id) {
        Student student = studentService.findById(id);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student);
    }

    /**
     * 根据学号获取学生信息
     * GET /api/students/number/{studentNumber}
     */
    @GetMapping("/number/{studentNumber}")
    public ResponseEntity<Student> getByStudentNumber(@PathVariable String studentNumber) {
        Student student = studentService.findByStudentNumber(studentNumber);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student);
    }

    /**
     * 按班级查询学生列表
     * GET /api/students/class/{className}
     */
    @GetMapping("/class/{className}")
    public List<Student> listByClass(@PathVariable String className) {
        return studentService.findByClass(className);
    }

    /**
     * 创建新学生
     * POST /api/students
     */
    @PostMapping
    public ResponseEntity<Student> create(@Valid @RequestBody Student student) {
        return ResponseEntity.ok(studentService.save(student));
    }

    /**
     * 更新学生信息
     * PUT /api/students/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Student> update(@PathVariable Long id, @Valid @RequestBody Student student) {
        Student existing = studentService.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        student.setId(id);
        return ResponseEntity.ok(studentService.save(student));
    }

    /**
     * 删除学生
     * DELETE /api/students/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Student student = studentService.findById(id);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
