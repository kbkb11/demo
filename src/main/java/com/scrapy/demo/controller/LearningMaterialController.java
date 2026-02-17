package com.scrapy.demo.controller;

import com.scrapy.demo.domain.LearningMaterial;
import com.scrapy.demo.service.LearningMaterialService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materials")
public class LearningMaterialController {

    private final LearningMaterialService learningMaterialService;

    public LearningMaterialController(LearningMaterialService learningMaterialService) {
        this.learningMaterialService = learningMaterialService;
    }

    @GetMapping
    public List<LearningMaterial> list() {
        return learningMaterialService.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LearningMaterial> get(@PathVariable Long id) {
        LearningMaterial material = learningMaterialService.findById(id);
        if (material == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(material);
    }

    @PostMapping
    public ResponseEntity<LearningMaterial> create(@Valid @RequestBody LearningMaterial material) {
        return ResponseEntity.ok(learningMaterialService.save(material));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LearningMaterial> update(@PathVariable Long id,
                                                   @Valid @RequestBody LearningMaterial material) {
        LearningMaterial updated = learningMaterialService.update(id, material);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!learningMaterialService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
