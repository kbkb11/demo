package com.scrapy.demo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "score_history")
public class ScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "成绩记录不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "score_id", nullable = false)
    private Score score;

    @NotNull(message = "修改前成绩不能为空")
    @Column(nullable = false)
    private Double beforeValue;

    @NotNull(message = "修改后成绩不能为空")
    @Column(nullable = false)
    private Double afterValue;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime updatedAt;

    public ScoreHistory() {
    }

    public ScoreHistory(Score score, Double beforeValue, Double afterValue) {
        this.score = score;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
    }

    @PrePersist
    public void prePersist() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public Double getBeforeValue() {
        return beforeValue;
    }

    public void setBeforeValue(Double beforeValue) {
        this.beforeValue = beforeValue;
    }

    public Double getAfterValue() {
        return afterValue;
    }

    public void setAfterValue(Double afterValue) {
        this.afterValue = afterValue;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
