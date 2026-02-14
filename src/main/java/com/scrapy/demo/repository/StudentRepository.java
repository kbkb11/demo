package com.scrapy.demo.repository;

import com.scrapy.demo.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * 根据学号查找学生
     */
    Optional<Student> findByStudentNumber(String studentNumber);

    /**
     * 根据班级查找学生
     */
    List<Student> findByClazz(String clazz);

    /**
     * 根据专业查找学生
     */
    List<Student> findByMajor(String major);
}
