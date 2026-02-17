package com.scrapy.demo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "teacher")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "教师编号不能为空")
    @Column(name = "teacher_number", nullable = false, unique = true)
    private String teacherNumber;

    @NotBlank(message = "教师姓名不能为空")
    @Column(nullable = false)
    private String name;

    @Column
    private String department;

    public Teacher() {
    }

    public Teacher(String teacherNumber, String name, String department) {
        this.teacherNumber = teacherNumber;
        this.name = name;
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTeacherNumber() {
        return teacherNumber;
    }

    public void setTeacherNumber(String teacherNumber) {
        this.teacherNumber = teacherNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
