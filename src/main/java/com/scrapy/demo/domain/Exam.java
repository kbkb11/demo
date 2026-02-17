package com.scrapy.demo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "exam")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "考试名称不能为空")
    @Column(nullable = false, unique = true)
    private String name;

    @NotNull(message = "考试日期不能为空")
    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    public Exam() {
    }

    public Exam(String name, LocalDate examDate) {
        this.name = name;
        this.examDate = examDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }
}
