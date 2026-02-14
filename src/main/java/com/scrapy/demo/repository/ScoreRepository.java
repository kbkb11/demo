package com.scrapy.demo.repository;

import com.scrapy.demo.domain.Course;
import com.scrapy.demo.domain.Score;
import com.scrapy.demo.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    /**
     * 根据课程ID查找成绩
     */
    List<Score> findByCourseId(Long courseId);

    /**
     * 根据学生ID查找成绩
     */
    List<Score> findByStudentId(Long studentId);

    /**
     * 根据学生和课程查找成绩
     */
    Optional<Score> findByStudentAndCourse(Student student, Course course);
}

