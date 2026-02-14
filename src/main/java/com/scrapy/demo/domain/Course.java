package com.scrapy.demo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "课程名称不能为空")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "学分不能为空")
    @DecimalMin(value = "0.5", message = "学分不能低于0.5")
    private Double credit;

    @NotBlank(message = "任课教师不能为空")
    @Column(nullable = false)
    private String teacherName;

    public Course() {
    }

    public Course(String name, Double credit, String teacherName) {
        this.name = name;
        this.credit = credit;
        this.teacherName = teacherName;
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

    public Double getCredit() {
        return credit;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
}
