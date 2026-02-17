package com.scrapy.demo.service;

import com.scrapy.demo.domain.Exam;
import com.scrapy.demo.domain.Score;
import com.scrapy.demo.repository.ScoreRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.time.LocalDate;

/**
 * 成绩统计与分析服务
 * 支持平均分、最高分、最低分、及格率、成绩分布、成绩趋势等统计功能
 */
@Service
public class ScoreAnalysisService {

    private final ScoreRepository scoreRepository;

    public ScoreAnalysisService(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    /**
     * 获取某课程的所有成绩统计
     */
    public Map<String, Object> analyzeCourseScores(Long courseId) {
        List<Score> scores = scoreRepository.findByCourseId(courseId);
        return analyzeScores(scores, "课程成绩");
    }

    /**
     * 获取某学生的所有成绩统计
     */
    public Map<String, Object> analyzeStudentScores(Long studentId) {
        List<Score> scores = scoreRepository.findByStudentId(studentId);
        return analyzeScores(scores, "学生成绩");
    }

    /**
     * 获取整体成绩统计
     */
    public Map<String, Object> analyzeAllScores() {
        List<Score> scores = scoreRepository.findAll();
        return analyzeScores(scores, "整体成绩");
    }

    /**
     * 核心分析方法 - 执行成绩统计
     */
    private Map<String, Object> analyzeScores(List<Score> scores, String label) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("label", label);
        result.put("totalCount", scores.size());

        if (scores.isEmpty()) {
            result.put("average", 0.0);
            result.put("highest", 0.0);
            result.put("lowest", 0.0);
            result.put("passRate", 0.0);
            result.put("distribution", getEmptyDistribution());
            return result;
        }

        // 提取所有成绩值
        List<Double> values = scores.stream()
                .map(Score::getValue)
                .toList();

        // 计算统计指标
        double average = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double highest = values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double lowest = values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);

        // 计算及格率（及格分数线：60分）
        long passCount = values.stream().filter(v -> v >= 60).count();
        double passRate = (double) passCount / scores.size() * 100;

        result.put("average", Math.round(average * 100) / 100.0);
        result.put("highest", highest);
        result.put("lowest", lowest);
        result.put("passRate", Math.round(passRate * 100) / 100.0);

        // 成绩分布
        result.put("distribution", getScoreDistribution(values));

        return result;
    }

    /**
     * 获取成绩分布（区间统计）
     * 区间：0-59, 60-69, 70-79, 80-89, 90-100
     */
    private Map<String, Integer> getScoreDistribution(List<Double> scores) {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("0-59", 0);
        distribution.put("60-69", 0);
        distribution.put("70-79", 0);
        distribution.put("80-89", 0);
        distribution.put("90-100", 0);

        for (Double score : scores) {
            if (score < 60) {
                distribution.put("0-59", distribution.get("0-59") + 1);
            } else if (score < 70) {
                distribution.put("60-69", distribution.get("60-69") + 1);
            } else if (score < 80) {
                distribution.put("70-79", distribution.get("70-79") + 1);
            } else if (score < 90) {
                distribution.put("80-89", distribution.get("80-89") + 1);
            } else {
                distribution.put("90-100", distribution.get("90-100") + 1);
            }
        }

        return distribution;
    }

    /**
     * 获取空分布
     */
    private Map<String, Integer> getEmptyDistribution() {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("0-59", 0);
        distribution.put("60-69", 0);
        distribution.put("70-79", 0);
        distribution.put("80-89", 0);
        distribution.put("90-100", 0);
        return distribution;
    }

    /**
     * 按班级获取成绩统计
     */
    public Map<String, Object> analyzeClassScores(String clazz) {
        // 获取该班级所有学生的成绩
        List<Score> scores = scoreRepository.findAll().stream()
                .filter(score -> score.getStudent() != null && 
                        clazz.equals(score.getStudent().getClazz()))
                .toList();
        return analyzeScores(scores, "班级 " + clazz + " 成绩");
    }

    /**
     * 获取及格分数率统计
     */
    public Map<String, Double> getPassRateByScore() {
        Map<String, Double> passRates = new LinkedHashMap<>();
        List<Score> allScores = scoreRepository.findAll();

        if (allScores.isEmpty()) {
            return passRates;
        }

        // 按课程分组
        Map<Long, List<Score>> groupedByCourse = new LinkedHashMap<>();
        for (Score score : allScores) {
            groupedByCourse.computeIfAbsent(score.getCourse().getId(), k -> new ArrayList<>())
                    .add(score);
        }

        // 计算每门课程的及格率
        groupedByCourse.forEach((courseId, courseScores) -> {
            long passCount = courseScores.stream()
                    .filter(s -> s.getValue() >= 60)
                    .count();
            double passRate = (double) passCount / courseScores.size() * 100;
            String courseName = courseScores.get(0).getCourse() == null
                    ? ("课程ID:" + courseId)
                    : courseScores.get(0).getCourse().getName();
            passRates.put(courseName, Math.round(passRate * 100) / 100.0);
        });

        return passRates;
    }

    /**
     * 获取学生排名（按平均分）
     */
    public List<Map<String, Object>> getStudentRankings() {
        List<Score> allScores = scoreRepository.findAll();
        Map<Long, List<Double>> studentScores = new LinkedHashMap<>();

        // 按学生分组成绩
        for (Score score : allScores) {
            Long studentId = score.getStudent().getId();
            studentScores.computeIfAbsent(studentId, k -> new ArrayList<>())
                    .add(score.getValue());
        }

        // 计算平均分并排序
        List<Map<String, Object>> rankings = new ArrayList<>();
        studentScores.forEach((studentId, scores) -> {
            double average = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            Map<String, Object> entry = new HashMap<>();
            entry.put("studentId", studentId);
            entry.put("averageScore", Math.round(average * 100) / 100.0);
            rankings.add(entry);
        });

        // 按平均分降序排列
        rankings.sort((a, b) -> Double.compare((Double) b.get("averageScore"), 
                                                (Double) a.get("averageScore")));
        
        // 添加排名
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).put("rank", i + 1);
        }

        return rankings;
    }

    /**
     * 获取各考试平均分（按考试日期排序）
     */
    public Map<String, Double> getExamAverages() {
        List<ExamBucket> buckets = buildExamBuckets(scoreRepository.findAll());
        Map<String, Double> result = new LinkedHashMap<>();
        for (ExamBucket bucket : buckets) {
            result.put(bucket.label(), round2(bucket.average()));
        }
        return result;
    }

    /**
     * 获取各考试及格率（按考试日期排序）
     */
    public Map<String, Double> getExamPassRates() {
        List<ExamBucket> buckets = buildExamBuckets(scoreRepository.findAll());
        Map<String, Double> result = new LinkedHashMap<>();
        for (ExamBucket bucket : buckets) {
            result.put(bucket.label(), round2(bucket.passRate()));
        }
        return result;
    }

    private List<ExamBucket> buildExamBuckets(List<Score> scores) {
        Map<Long, ExamBucket> map = new LinkedHashMap<>();
        for (Score score : scores) {
            Exam exam = score.getExam();
            if (exam == null) {
                continue;
            }
            ExamBucket bucket = map.computeIfAbsent(exam.getId(), id -> new ExamBucket(exam));
            bucket.accept(score.getValue());
        }
        List<ExamBucket> buckets = new ArrayList<>(map.values());
        buckets.sort(Comparator
                .comparing((ExamBucket bucket) -> bucket.examDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(bucket -> bucket.examName == null ? "" : bucket.examName));
        return buckets;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static final class ExamBucket {
        private final Long examId;
        private final String examName;
        private final LocalDate examDate;
        private double sum;
        private int count;
        private int passCount;

        private ExamBucket(Exam exam) {
            this.examId = exam.getId();
            this.examName = exam.getName();
            this.examDate = exam.getExamDate();
        }

        private void accept(Double value) {
            if (value == null) {
                return;
            }
            sum += value;
            count++;
            if (value >= 60) {
                passCount++;
            }
        }

        private double average() {
            return count == 0 ? 0.0 : sum / count;
        }

        private double passRate() {
            return count == 0 ? 0.0 : passCount * 100.0 / count;
        }

        private String label() {
            if (examName != null && !examName.isBlank()) {
                return examName;
            }
            return examDate == null ? "未命名考试" : examDate.toString();
        }
    }
}
