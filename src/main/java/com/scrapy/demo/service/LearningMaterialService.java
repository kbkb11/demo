package com.scrapy.demo.service;

import com.scrapy.demo.domain.LearningMaterial;
import com.scrapy.demo.repository.LearningMaterialRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LearningMaterialService {

    private final LearningMaterialRepository learningMaterialRepository;

    public LearningMaterialService(LearningMaterialRepository learningMaterialRepository) {
        this.learningMaterialRepository = learningMaterialRepository;
    }

    public List<LearningMaterial> listAll() {
        return learningMaterialRepository.findAllByOrderByIdAsc();
    }

    public LearningMaterial findById(Long id) {
        return learningMaterialRepository.findById(id).orElse(null);
    }

    public LearningMaterial save(LearningMaterial material) {
        return learningMaterialRepository.save(material);
    }

    public LearningMaterial update(Long id, LearningMaterial payload) {
        LearningMaterial existing = learningMaterialRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        if (payload.getCourseKeyword() != null) {
            existing.setCourseKeyword(payload.getCourseKeyword());
        }
        if (payload.getTitle() != null) {
            existing.setTitle(payload.getTitle());
        }
        if (payload.getUrl() != null) {
            existing.setUrl(payload.getUrl());
        }
        if (payload.getType() != null) {
            existing.setType(payload.getType());
        }
        if (payload.getDifficultyTag() != null) {
            existing.setDifficultyTag(payload.getDifficultyTag());
        }
        return learningMaterialRepository.save(existing);
    }

    public boolean delete(Long id) {
        LearningMaterial existing = learningMaterialRepository.findById(id).orElse(null);
        if (existing == null) {
            return false;
        }
        learningMaterialRepository.delete(existing);
        return true;
    }
}
