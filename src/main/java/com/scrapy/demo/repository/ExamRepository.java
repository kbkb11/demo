package com.scrapy.demo.repository;

import com.scrapy.demo.domain.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    Optional<Exam> findByName(String name);
}
