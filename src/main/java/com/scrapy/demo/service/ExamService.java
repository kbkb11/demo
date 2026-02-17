package com.scrapy.demo.service;

import com.scrapy.demo.domain.Exam;
import com.scrapy.demo.repository.ExamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamService {

    private final ExamRepository examRepository;

    public ExamService(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    public List<Exam> listAll() {
        return examRepository.findAll();
    }

    public Exam findById(Long id) {
        return examRepository.findById(id).orElse(null);
    }

    public Exam save(Exam exam) {
        return examRepository.save(exam);
    }

    public Exam update(Long id, Exam payload) {
        Exam existing = examRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        if (payload.getName() != null) {
            existing.setName(payload.getName());
        }
        if (payload.getExamDate() != null) {
            existing.setExamDate(payload.getExamDate());
        }
        return examRepository.save(existing);
    }

    public boolean delete(Long id) {
        Exam existing = examRepository.findById(id).orElse(null);
        if (existing == null) {
            return false;
        }
        examRepository.delete(existing);
        return true;
    }
}
