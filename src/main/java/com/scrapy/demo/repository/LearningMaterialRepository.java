package com.scrapy.demo.repository;

import com.scrapy.demo.domain.LearningMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningMaterialRepository extends JpaRepository<LearningMaterial, Long> {

    List<LearningMaterial> findByCourseKeywordContainingIgnoreCaseOrderByIdAsc(String courseKeyword);

    List<LearningMaterial> findAllByOrderByIdAsc();
}
