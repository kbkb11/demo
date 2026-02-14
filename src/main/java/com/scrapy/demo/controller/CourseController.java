package com.scrapy.demo.controller;

import com.scrapy.demo.domain.Course;
import com.scrapy.demo.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程管理控制器
 * 提供课程信息的查询、创建、修改、删除等功能
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * 获取所有课程列表
     * GET /api/courses
     */
    @GetMapping
    public List<Course> list() {
        return courseService.listAllCourses();
    }

    /**
     * 根据ID获取课程信息
     * GET /api/courses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Course> get(@PathVariable Long id) {
        Course course = courseService.findById(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(course);
    }

    /**
     * 根据课程名称查询课程
     * GET /api/courses/name/{courseName}
     */
    @GetMapping("/name/{courseName}")
    public ResponseEntity<Course> getByName(@PathVariable String courseName) {
        Course course = courseService.findByName(courseName);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(course);
    }

    /**
     * 根据教师名称查询课程
     * GET /api/courses/teacher/{teacherName}
     */
    @GetMapping("/teacher/{teacherName}")
    public List<Course> listByTeacher(@PathVariable String teacherName) {
        return courseService.findByTeacher(teacherName);
    }

    /**
     * 创建新课程
     * POST /api/courses
     */
    @PostMapping
    public ResponseEntity<Course> create(@Valid @RequestBody Course course) {
        return ResponseEntity.ok(courseService.save(course));
    }

    /**
     * 更新课程信息
     * PUT /api/courses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Course> update(@PathVariable Long id, @Valid @RequestBody Course course) {
        Course existing = courseService.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        course.setId(id);
        return ResponseEntity.ok(courseService.save(course));
    }

    /**
     * 删除课程
     * DELETE /api/courses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Course course = courseService.findById(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
