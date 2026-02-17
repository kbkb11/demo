package com.scrapy.demo.controller;

import com.scrapy.demo.domain.Exam;
import com.scrapy.demo.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public List<Exam> list() {
        return examService.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exam> get(@PathVariable Long id) {
        Exam exam = examService.findById(id);
        if (exam == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(exam);
    }

    @PostMapping
    public ResponseEntity<Exam> create(@Valid @RequestBody Exam exam) {
        return ResponseEntity.ok(examService.save(exam));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Exam> update(@PathVariable Long id, @Valid @RequestBody Exam exam) {
        Exam updated = examService.update(id, exam);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!examService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
