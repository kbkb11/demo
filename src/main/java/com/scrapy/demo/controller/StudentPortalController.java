package com.scrapy.demo.controller;

import com.scrapy.demo.domain.Score;
import com.scrapy.demo.domain.Student;
import com.scrapy.demo.repository.ScoreRepository;
import com.scrapy.demo.repository.StudentRepository;
import com.scrapy.demo.repository.UserRepository;
import com.scrapy.demo.service.TeachingInsightService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
public class StudentPortalController {

    private final StudentRepository studentRepository;
    private final ScoreRepository scoreRepository;
    private final TeachingInsightService teachingInsightService;
    private final UserRepository userRepository;

    public StudentPortalController(StudentRepository studentRepository,
                                   ScoreRepository scoreRepository,
                                   TeachingInsightService teachingInsightService,
                                   UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.scoreRepository = scoreRepository;
        this.teachingInsightService = teachingInsightService;
        this.userRepository = userRepository;
    }

    @GetMapping("/self")
    public ResponseEntity<Map<String, Object>> getSelf(Authentication authentication) {
        Student student = resolveStudent(authentication);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("studentId", student.getId());
        payload.put("studentNumber", student.getStudentNumber());
        payload.put("name", student.getName());
        payload.put("clazz", student.getClazz());
        payload.put("major", student.getMajor());
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/self/scores")
    public ResponseEntity<List<Score>> getSelfScores(Authentication authentication) {
        Student student = resolveStudent(authentication);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(scoreRepository.findByStudentId(student.getId()));
    }

    @GetMapping("/self/recommendations")
    public ResponseEntity<Map<String, Object>> getSelfRecommendations(Authentication authentication) {
        Student student = resolveStudent(authentication);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(teachingInsightService.getStudentRecommendations(student.getId()));
    }

    @GetMapping("/self/trend")
    public ResponseEntity<List<Map<String, Object>>> getSelfTrend(Authentication authentication) {
        Student student = resolveStudent(authentication);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        List<Score> scores = scoreRepository.findByStudentId(student.getId());
        Map<Long, List<Score>> byExam = scores.stream()
                .filter(score -> score.getExam() != null)
                .collect(Collectors.groupingBy(score -> score.getExam().getId()));
        Map<Long, ExamTrendItem> buckets = new LinkedHashMap<>();
        byExam.forEach((examId, list) -> {
            ExamTrendItem item = new ExamTrendItem();
            item.examId = examId;
            item.examName = list.get(0).getExam().getName();
            item.examDate = list.get(0).getExam().getExamDate();
            item.average = list.stream().mapToDouble(Score::getValue).average().orElse(0.0);
            buckets.put(examId, item);
        });
        List<Map<String, Object>> result = buckets.values().stream()
                .sorted((a, b) -> {
                    if (a.examDate == null && b.examDate == null) {
                        return 0;
                    }
                    if (a.examDate == null) {
                        return 1;
                    }
                    if (b.examDate == null) {
                        return -1;
                    }
                    return a.examDate.compareTo(b.examDate);
                })
                .map(item -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("examId", item.examId);
                    map.put("examName", item.examName);
                    map.put("examDate", item.examDate != null ? item.examDate.toString() : null);
                    map.put("average", Math.round(item.average * 100.0) / 100.0);
                    return map;
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    private static final class ExamTrendItem {
        private Long examId;
        private String examName;
        private LocalDate examDate;
        private double average;
    }

    private Student resolveStudent(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        if (username == null || username.isBlank()) {
            return null;
        }
        Student linked = userRepository.findByUsername(username)
                .map(user -> user.getStudent())
                .orElse(null);
        if (linked != null) {
            return linked;
        }
        return studentRepository.findByStudentNumber(username)
                .or(() -> studentRepository.findFirstByName(username))
                .orElse(null);
    }
}
