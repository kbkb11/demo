package com.scrapy.demo.service;

import com.scrapy.demo.domain.LearningMaterial;
import com.scrapy.demo.domain.Score;
import com.scrapy.demo.domain.Student;
import com.scrapy.demo.repository.LearningMaterialRepository;
import com.scrapy.demo.repository.ScoreRepository;
import com.scrapy.demo.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
public class TeachingInsightService {

    private static final double PASS_LINE = 60.0;

    private final ScoreRepository scoreRepository;
    private final StudentRepository studentRepository;
    private final LearningMaterialRepository learningMaterialRepository;
    private final LlmReasonService llmReasonService;

    public TeachingInsightService(ScoreRepository scoreRepository,
                                  StudentRepository studentRepository,
                                  LearningMaterialRepository learningMaterialRepository,
                                  LlmReasonService llmReasonService) {
        this.scoreRepository = scoreRepository;
        this.studentRepository = studentRepository;
        this.learningMaterialRepository = learningMaterialRepository;
        this.llmReasonService = llmReasonService;
    }

    public Map<String, Object> getClassOverview(String clazz) {
        List<Student> students = studentRepository.findByClazz(clazz);
        List<Score> scores = scoreRepository.findByStudentClazz(clazz);
        List<LocalDate> classDates = extractSortedExamDates(scores);
        LocalDate latestDate = classDates.isEmpty() ? null : classDates.get(classDates.size() - 1);
        LocalDate previousDate = classDates.size() > 1 ? classDates.get(classDates.size() - 2) : null;
        List<Score> latestScores = filterByExamDate(scores, latestDate);
        List<Score> previousScores = filterByExamDate(scores, previousDate);

        double classAvg = latestScores.stream().mapToDouble(Score::getValue).average().orElse(0.0);
        double passRate = latestScores.isEmpty() ? 0.0 : latestScores.stream().filter(s -> s.getValue() >= PASS_LINE).count() * 100.0 / latestScores.size();
        double previousAvg = previousScores.stream().mapToDouble(Score::getValue).average().orElse(classAvg);
        double trendValue = classAvg - previousAvg;
        String trend = trendValue > 1.0 ? "上升" : (trendValue < -1.0 ? "下降" : "稳定");

        Map<Long, List<Score>> studentScoreMap = scores.stream().collect(Collectors.groupingBy(s -> s.getStudent().getId()));
        long riskStudents = students.stream()
                .filter(st -> {
                    List<Score> studentScores = studentScoreMap.getOrDefault(st.getId(), Collections.emptyList());
                    StudentExamSnapshot snapshot = buildStudentExamSnapshot(studentScores);
                    return isRiskStudent(snapshot.currentScores, snapshot.deltaAverage);
                })
                .count();
        double riskRatio = students.isEmpty() ? 0.0 : riskStudents * 100.0 / students.size();

        Map<String, Double> passRateByCourse = latestScores.stream()
                .collect(Collectors.groupingBy(s -> s.getCourse().getName(), LinkedHashMap::new, Collectors.collectingAndThen(Collectors.toList(), list ->
                        list.stream().filter(score -> score.getValue() >= PASS_LINE).count() * 100.0 / list.size()
                )));

        List<Map<String, Object>> weakSubjects = latestScores.stream()
                .collect(Collectors.groupingBy(s -> s.getCourse().getName(), Collectors.averagingDouble(Score::getValue)))
                .entrySet().stream()
                .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                .limit(3)
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("courseName", entry.getKey());
                    item.put("average", round2(entry.getValue()));
                    return item;
                })
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("className", clazz);
        result.put("studentCount", students.size());
        result.put("classAverage", round2(classAvg));
        result.put("overallPassRate", round2(passRate));
        result.put("trend", trend);
        result.put("trendValue", round2(trendValue));
        result.put("riskStudentRatio", round2(riskRatio));
        result.put("riskStudentCount", riskStudents);
        result.put("subjectPassRates", passRateByCourse);
        result.put("weakSubjects", weakSubjects);
        result.put("summary", List.of(
                "本班整体平均分 " + round2(classAvg),
                "成绩趋势：" + trend,
                "风险学生比例 " + round2(riskRatio) + "%"
        ));
        return result;
    }

    public List<Map<String, Object>> getClassStudentFlags(String clazz) {
        List<Score> scores = scoreRepository.findByStudentClazz(clazz);
        Map<Long, List<Score>> scoreByStudent = scores.stream().collect(Collectors.groupingBy(s -> s.getStudent().getId()));
        Map<Long, StudentExamSnapshot> snapshots = new HashMap<>();
        Map<Long, Double> currentAvg = new HashMap<>();
        Map<Long, Double> previousAvg = new HashMap<>();
        scoreByStudent.forEach((studentId, list) -> {
            StudentExamSnapshot snapshot = buildStudentExamSnapshot(list);
            snapshots.put(studentId, snapshot);
            currentAvg.put(studentId, snapshot.currentAverage);
            previousAvg.put(studentId, snapshot.previousAverage);
        });

        Map<Long, Integer> currentRank = buildRankMap(currentAvg);
        Map<Long, Integer> previousRank = buildRankMap(previousAvg);

        List<Map<String, Object>> result = new ArrayList<>();
        scoreByStudent.forEach((studentId, studentScores) -> {
            Student student = studentScores.get(0).getStudent();
            StudentExamSnapshot snapshot = snapshots.getOrDefault(studentId, StudentExamSnapshot.empty());
            double current = snapshot.currentAverage;
            double previous = snapshot.previousAverage;
            int currentPos = currentRank.getOrDefault(studentId, 0);
            int previousPos = previousRank.getOrDefault(studentId, currentPos);
            int rankDelta = previousPos - currentPos;
            String rankTrend = rankDelta > 0 ? "上升" : (rankDelta < 0 ? "下降" : "稳定");
            boolean risk = isRiskStudent(snapshot.currentScores, snapshot.deltaAverage);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("studentId", studentId);
            row.put("studentName", student.getName());
            row.put("currentAverage", round2(current));
            row.put("previousAverage", round2(previous));
            row.put("scoreDelta", round2(current - previous));
            row.put("currentRank", currentPos);
            row.put("previousRank", previousPos);
            row.put("rankTrend", rankTrend);
            row.put("rankDelta", rankDelta);
            row.put("risk", risk);
            row.put("tag", risk ? "风险" : ("下降".equals(rankTrend) ? "需关注" : "正常"));
            result.add(row);
        });

        result.sort(Comparator
                .comparing((Map<String, Object> row) -> Boolean.TRUE.equals(row.get("risk"))).reversed()
                .thenComparing(row -> ((Number) row.get("scoreDelta")).doubleValue()));
        return result;
    }

    public Map<String, Object> getStudentRecommendations(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在: " + studentId));
        List<Score> studentScores = scoreRepository.findByStudentId(studentId);
        List<Score> classScores = scoreRepository.findByStudentClazz(student.getClazz());

        Map<Long, CourseExamSnapshot> studentCourseSnapshots = buildCourseSnapshots(studentScores);
        Map<Long, CourseExamSnapshot> classCourseSnapshots = buildCourseSnapshots(classScores);

        List<Map<String, Object>> items = new ArrayList<>();
        for (CourseExamSnapshot snapshot : studentCourseSnapshots.values()) {
            Score score = snapshot.latestScore;
            if (score == null) {
                continue;
            }
            String courseName = score.getCourse().getName();
            CourseExamSnapshot classSnapshot = classCourseSnapshots.get(score.getCourse().getId());
            double classAvg = classSnapshot == null ? score.getValue() : classSnapshot.latestAverage;
            double delta = snapshot.delta;
            String trend = delta > 1.0 ? "上升" : (delta < -1.0 ? "下降" : "稳定");

            List<LearningMaterial> materials = learningMaterialRepository
                    .findByCourseKeywordContainingIgnoreCaseOrderByIdAsc(courseName);
            if (materials.isEmpty()) {
                materials = learningMaterialRepository.findAllByOrderByIdAsc().stream().limit(2).toList();
            }

            List<Map<String, Object>> materialViews = materials.stream().limit(3).map(material -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("title", material.getTitle());
                map.put("url", material.getUrl());
                map.put("type", material.getType());
                map.put("difficulty", material.getDifficultyTag());
                return map;
            }).toList();

            Map<String, Object> reasonContext = new LinkedHashMap<>();
            reasonContext.put("studentName", student.getName());
            reasonContext.put("course", courseName);
            reasonContext.put("trend", trend);
            reasonContext.put("score", score.getValue());
            reasonContext.put("classAvg", classAvg);
            reasonContext.put("differenceWithClassAvg", score.getValue() - classAvg);
            reasonContext.put("materials", materialViews.stream().map(m -> m.get("title")).toList());

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("courseId", score.getCourse().getId());
            row.put("courseName", courseName);
            row.put("currentScore", round2(score.getValue()));
            row.put("classAverage", round2(classAvg));
            row.put("differenceWithClassAvg", round2(score.getValue() - classAvg));
            row.put("trend", trend);
            row.put("trendDelta", round2(delta));
            row.put("reason", llmReasonService.buildRecommendationReason(reasonContext));
            row.put("materials", materialViews);
            items.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("studentId", student.getId());
        result.put("studentName", student.getName());
        result.put("clazz", student.getClazz());
        result.put("recommendations", items);
        return result;
    }

    private StudentExamSnapshot buildStudentExamSnapshot(List<Score> scores) {
        List<LocalDate> dates = extractSortedExamDates(scores);
        LocalDate latestDate = dates.isEmpty() ? null : dates.get(dates.size() - 1);
        LocalDate previousDate = dates.size() > 1 ? dates.get(dates.size() - 2) : null;
        List<Score> currentScores = filterByExamDate(scores, latestDate);
        List<Score> previousScores = filterByExamDate(scores, previousDate);
        double currentAvg = currentScores.stream().mapToDouble(Score::getValue).average().orElse(0.0);
        double previousAvg = previousScores.stream().mapToDouble(Score::getValue).average().orElse(currentAvg);
        double deltaAvg = currentAvg - previousAvg;
        return new StudentExamSnapshot(currentScores, currentAvg, previousAvg, deltaAvg);
    }

    private Map<Long, CourseExamSnapshot> buildCourseSnapshots(List<Score> scores) {
        Map<Long, List<Score>> byCourse = scores.stream().collect(Collectors.groupingBy(s -> s.getCourse().getId()));
        Map<Long, CourseExamSnapshot> result = new LinkedHashMap<>();
        byCourse.forEach((courseId, list) -> {
            List<Score> sorted = list.stream()
                    .sorted(Comparator.comparing(this::resolveExamDate, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(Score::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
            if (sorted.isEmpty()) {
                return;
            }
            Score latest = sorted.get(sorted.size() - 1);
            Score previous = sorted.size() > 1 ? sorted.get(sorted.size() - 2) : null;
            double delta = previous == null ? 0.0 : latest.getValue() - previous.getValue();
            double latestAvg = sorted.stream()
                    .filter(score -> Objects.equals(resolveExamDate(score), resolveExamDate(latest)))
                    .mapToDouble(Score::getValue)
                    .average()
                    .orElse(latest.getValue());
            result.put(courseId, new CourseExamSnapshot(latest, delta, latestAvg));
        });
        return result;
    }

    private Map<Long, Integer> buildRankMap(Map<Long, Double> avgMap) {
        List<Map.Entry<Long, Double>> sorted = avgMap.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .toList();
        Map<Long, Integer> rankMap = new HashMap<>();
        for (int i = 0; i < sorted.size(); i++) {
            rankMap.put(sorted.get(i).getKey(), i + 1);
        }
        return rankMap;
    }

    private boolean isRiskStudent(List<Score> scores, double avgDelta) {
        if (scores.isEmpty()) {
            return false;
        }
        long failed = scores.stream().filter(s -> s.getValue() < PASS_LINE).count();
        double avg = scores.stream().mapToDouble(Score::getValue).average().orElse(0.0);
        return failed >= 2 || avg < PASS_LINE || avgDelta < -5;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private LocalDate resolveExamDate(Score score) {
        if (score == null) {
            return null;
        }
        if (score.getExam() != null && score.getExam().getExamDate() != null) {
            return score.getExam().getExamDate();
        }
        if (score.getRecordedAt() != null) {
            return score.getRecordedAt().toLocalDate();
        }
        return null;
    }

    private List<LocalDate> extractSortedExamDates(List<Score> scores) {
        return scores.stream()
                .map(this::resolveExamDate)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    private List<Score> filterByExamDate(List<Score> scores, LocalDate date) {
        if (date == null) {
            return Collections.emptyList();
        }
        return scores.stream()
                .filter(score -> Objects.equals(resolveExamDate(score), date))
                .toList();
    }

    private static final class StudentExamSnapshot {
        private final List<Score> currentScores;
        private final double currentAverage;
        private final double previousAverage;
        private final double deltaAverage;

        private StudentExamSnapshot(List<Score> currentScores, double currentAverage, double previousAverage, double deltaAverage) {
            this.currentScores = currentScores;
            this.currentAverage = currentAverage;
            this.previousAverage = previousAverage;
            this.deltaAverage = deltaAverage;
        }

        private static StudentExamSnapshot empty() {
            return new StudentExamSnapshot(Collections.emptyList(), 0.0, 0.0, 0.0);
        }
    }

    private static final class CourseExamSnapshot {
        private final Score latestScore;
        private final double delta;
        private final double latestAverage;

        private CourseExamSnapshot(Score latestScore, double delta, double latestAverage) {
            this.latestScore = latestScore;
            this.delta = delta;
            this.latestAverage = latestAverage;
        }
    }
}
