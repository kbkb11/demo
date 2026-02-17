package com.scrapy.demo.service;

import com.scrapy.demo.domain.Course;
import com.scrapy.demo.domain.Exam;
import com.scrapy.demo.domain.Score;
import com.scrapy.demo.domain.Student;
import com.scrapy.demo.repository.CourseRepository;
import com.scrapy.demo.repository.ExamRepository;
import com.scrapy.demo.repository.ScoreRepository;
import com.scrapy.demo.repository.StudentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * Excel导入导出服务
 * 支持学生信息和成绩数据的批量导入导出
 */
@Service
public class ExcelService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ExamRepository examRepository;
    private final ScoreRepository scoreRepository;

    public ExcelService(StudentRepository studentRepository, CourseRepository courseRepository, 
                       ExamRepository examRepository, ScoreRepository scoreRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.examRepository = examRepository;
        this.scoreRepository = scoreRepository;
    }

    /**
     * 导出学生信息到Excel
     */
    public byte[] exportStudents() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("学生信息");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学号", "姓名", "班级", "专业"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        // 填充数据
        List<Student> students = studentRepository.findAll();
        int rowNum = 1;
        for (Student student : students) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getStudentNumber());
            row.createCell(1).setCellValue(student.getName());
            row.createCell(2).setCellValue(student.getClazz());
            row.createCell(3).setCellValue(student.getMajor());
        }

        // 调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 转换为字节数组
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    /**
     * 导出成绩信息到Excel
     */
    public byte[] exportScores() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("成绩信息");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学号", "学生姓名", "课程名称", "考试名称", "成绩", "记录时间"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        // 填充数据
        List<Score> scores = scoreRepository.findAll();
        int rowNum = 1;
        for (Score score : scores) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(score.getStudent().getStudentNumber());
            row.createCell(1).setCellValue(score.getStudent().getName());
            row.createCell(2).setCellValue(score.getCourse().getName());
            row.createCell(3).setCellValue(score.getExam() != null ? score.getExam().getName() : "");
            row.createCell(4).setCellValue(score.getValue());
            row.createCell(5).setCellValue(score.getRecordedAt() != null ? 
                    score.getRecordedAt().toString() : "");
        }

        // 调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 转换为字节数组
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    /**
     * 导入学生信息
     * Excel格式：学号、姓名、班级、专业
     */
    @Transactional
    public Map<String, Object> importStudents(MultipartFile file) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> successMessages = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);

            int rowNum = 0;
            for (Row row : sheet) {
                rowNum++;
                if (rowNum == 1) continue; // 跳过表头

                try {
                    // 读取单元格数据
                    String studentNumber = getCellStringValue(row.getCell(0));
                    String name = getCellStringValue(row.getCell(1));
                    String clazz = getCellStringValue(row.getCell(2));
                    String major = getCellStringValue(row.getCell(3));

                    // 数据验证
                    if (studentNumber == null || studentNumber.isEmpty()) {
                        errorMessages.add(String.format("第 %d 行：学号不能为空", rowNum));
                        continue;
                    }
                    if (name == null || name.isEmpty()) {
                        errorMessages.add(String.format("第 %d 行：姓名不能为空", rowNum));
                        continue;
                    }

                    // 检查是否已存在
                    if (studentRepository.findByStudentNumber(studentNumber).isPresent()) {
                        errorMessages.add(String.format("第 %d 行：学号已存在：%s", rowNum, studentNumber));
                        continue;
                    }

                    // 创建并保存学生
                    Student student = new Student(studentNumber, name, clazz, major);
                    studentRepository.save(student);
                    successMessages.add(String.format("第 %d 行：学生 %s 导入成功", rowNum, name));

                } catch (Exception e) {
                    errorMessages.add(String.format("第 %d 行：处理失败 - %s", rowNum, e.getMessage()));
                }
            }

            workbook.close();
        }

        result.put("total", successMessages.size() + errorMessages.size());
        result.put("successCount", successMessages.size());
        result.put("errorCount", errorMessages.size());
        result.put("successMessages", successMessages);
        result.put("errorMessages", errorMessages);

        return result;
    }

    /**
     * 导入成绩信息
     * Excel格式：学号、课程名称、考试名称、成绩
     */
    @Transactional
    public Map<String, Object> importScores(MultipartFile file) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> successMessages = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);

            int rowNum = 0;
            for (Row row : sheet) {
                rowNum++;
                if (rowNum == 1) continue; // 跳过表头

                try {
                    // 读取单元格数据
                    String studentNumber = getCellStringValue(row.getCell(0));
                    String courseName = getCellStringValue(row.getCell(1));
                    String examName = getCellStringValue(row.getCell(2));
                    Double scoreValue = getCellDoubleValue(row.getCell(3));

                    // 数据验证
                    if (studentNumber == null || studentNumber.isEmpty()) {
                        errorMessages.add(String.format("第 %d 行：学号不能为空", rowNum));
                        continue;
                    }
                    if (courseName == null || courseName.isEmpty()) {
                        errorMessages.add(String.format("第 %d 行：课程名称不能为空", rowNum));
                        continue;
                    }
                    if (examName == null || examName.isEmpty()) {
                        errorMessages.add(String.format("第 %d 行：考试名称不能为空", rowNum));
                        continue;
                    }
                    if (scoreValue == null || scoreValue < 0 || scoreValue > 100) {
                        errorMessages.add(String.format("第 %d 行：成绩必须为0-100之间的数值", rowNum));
                        continue;
                    }

                    // 查找学生和课程
                    Student student = studentRepository.findByStudentNumber(studentNumber)
                            .orElse(null);
                    if (student == null) {
                        errorMessages.add(String.format("第 %d 行：学生不存在：%s", rowNum, studentNumber));
                        continue;
                    }

                    Course course = courseRepository.findByName(courseName)
                            .orElse(null);
                    if (course == null) {
                        errorMessages.add(String.format("第 %d 行：课程不存在：%s", rowNum, courseName));
                        continue;
                    }

                    Exam exam = examRepository.findByName(examName).orElse(null);
                    if (exam == null) {
                        errorMessages.add(String.format("第 %d 行：考试不存在：%s", rowNum, examName));
                        continue;
                    }

                    // 检查是否已存在该学生/课程/考试成绩
                    Optional<Score> existingScore = scoreRepository.findByStudentAndCourseAndExam(student, course, exam);
                    if (existingScore.isPresent()) {
                        // 更新现有成绩
                        Score score = existingScore.get();
                        score.setValue(scoreValue);
                        scoreRepository.save(score);
                        successMessages.add(String.format("第 %d 行：成绩 %s-%s-%s 已更新", rowNum, 
                                student.getName(), courseName, examName));
                    } else {
                        // 创建新成绩
                        Score score = new Score();
                        score.setStudent(student);
                        score.setCourse(course);
                        score.setExam(exam);
                        score.setValue(scoreValue);
                        scoreRepository.save(score);
                        successMessages.add(String.format("第 %d 行：成绩 %s-%s-%s 导入成功", rowNum, 
                                student.getName(), courseName, examName));
                    }

                } catch (Exception e) {
                    errorMessages.add(String.format("第 %d 行：处理失败 - %s", rowNum, e.getMessage()));
                }
            }

            workbook.close();
        }

        result.put("total", successMessages.size() + errorMessages.size());
        result.put("successCount", successMessages.size());
        result.put("errorCount", errorMessages.size());
        result.put("successMessages", successMessages);
        result.put("errorMessages", errorMessages);

        return result;
    }

    /**
     * 获取单元格字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            default:
                return null;
        }
    }

    /**
     * 获取单元格数值
     */
    private Double getCellDoubleValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        return null;
    }
}
