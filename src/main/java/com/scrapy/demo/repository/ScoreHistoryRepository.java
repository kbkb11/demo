package com.scrapy.demo.repository;

import com.scrapy.demo.domain.ScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoreHistoryRepository extends JpaRepository<ScoreHistory, Long> {

    List<ScoreHistory> findByScoreIdInOrderByUpdatedAtDesc(List<Long> scoreIds);

    List<ScoreHistory> findByScoreIdOrderByUpdatedAtDesc(Long scoreId);

    List<ScoreHistory> findByScoreStudentIdOrderByUpdatedAtDesc(Long studentId);
}
