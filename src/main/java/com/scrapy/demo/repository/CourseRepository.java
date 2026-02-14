package com.scrapy.demo.repository;

import com.scrapy.demo.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * 根据课程名称查找课程
     */
    Optional<Course> findByName(String name);

    /**
     * 根据教师名称查找课程
     */
    List<Course> findByTeacherName(String teacherName);
}
