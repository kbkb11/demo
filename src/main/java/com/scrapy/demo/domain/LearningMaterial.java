package com.scrapy.demo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "learning_material")
public class LearningMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "科目关键词不能为空")
    @Column(name = "course_keyword", nullable = false)
    private String courseKeyword;

    @NotBlank(message = "资料标题不能为空")
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String type;

    @Column(name = "difficulty_tag", nullable = false)
    private String difficultyTag;

    public LearningMaterial() {
    }

    public LearningMaterial(String courseKeyword, String title, String url, String type, String difficultyTag) {
        this.courseKeyword = courseKeyword;
        this.title = title;
        this.url = url;
        this.type = type;
        this.difficultyTag = difficultyTag;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseKeyword() {
        return courseKeyword;
    }

    public void setCourseKeyword(String courseKeyword) {
        this.courseKeyword = courseKeyword;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDifficultyTag() {
        return difficultyTag;
    }

    public void setDifficultyTag(String difficultyTag) {
        this.difficultyTag = difficultyTag;
    }
}
