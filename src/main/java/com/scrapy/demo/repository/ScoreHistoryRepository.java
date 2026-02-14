package com.scrapy.demo.repository;

import com.scrapy.demo.domain.ScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreHistoryRepository extends JpaRepository<ScoreHistory, Long> {

}
