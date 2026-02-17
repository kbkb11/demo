package com.scrapy.demo.repository;

import com.scrapy.demo.domain.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByTeacherNumber(String teacherNumber);

    Optional<Teacher> findFirstByName(String name);
}
