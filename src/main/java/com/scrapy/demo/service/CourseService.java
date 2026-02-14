package com.scrapy.demo.service;

import com.scrapy.demo.domain.Course;
import com.scrapy.demo.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 课程服务
 * 处理课程相关的业务逻辑
 */
@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /**
     * 获取所有课程
     */
    public List<Course> listAllCourses() {
        return courseRepository.findAll();
    }

    /**
     * 根据ID查找课程
     */
    public Course findById(Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    /**
     * 根据课程名称查找课程
     */
    public Course findByName(String name) {
        return courseRepository.findByName(name).orElse(null);
    }

    /**
     * 根据教师名称查找课程
     */
    public List<Course> findByTeacher(String teacherName) {
        return courseRepository.findByTeacherName(teacherName);
    }

    /**
     * 保存或更新课程
     */
    @Transactional
    public Course save(Course course) {
        return courseRepository.save(course);
    }

    /**
     * 更新课程信息
     */
    @Transactional
    public Course update(Long id, Course course) {
        Course existing = findById(id);
        if (existing == null) {
            return null;
        }
        existing.setName(course.getName());
        existing.setCredit(course.getCredit());
        existing.setTeacherName(course.getTeacherName());
        return courseRepository.save(existing);
    }

    /**
     * 删除课程
     */
    @Transactional
    public boolean delete(Long id) {
        Course course = findById(id);
        if (course == null) {
            return false;
        }
        courseRepository.delete(course);
        return true;
    }
}
