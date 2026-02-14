package com.scrapy.demo.service;

import com.scrapy.demo.domain.Course;
import com.scrapy.demo.domain.Score;
import com.scrapy.demo.domain.Student;
import com.scrapy.demo.repository.CourseRepository;
import com.scrapy.demo.repository.ScoreRepository;
import com.scrapy.demo.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Excel服务测试
 * 测试学生和成绩信息的导入导出功能
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExcelServiceTest {

    @Autowired
    private ExcelService excelService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        courseRepository.deleteAll();
        scoreRepository.deleteAll();
    }

    @Test
    void testExportStudents() throws Exception {
        // 创建测试学生
        Student student = new Student();
        student.setStudentNumber("2024001");
        student.setName("张三");
        student.setClazz("计算机1班");
        student.setMajor("计算机科学与技术");
        studentRepository.save(student);

        // 导出
        byte[] data = excelService.exportStudents();

        assertNotNull(data);
        assertTrue(data.length > 0);
        // Excel文件应该以特定的字节开头
        assertTrue(data[0] == (byte) 0x50); // 'P' in 'PK'
    }

    @Test
    void testExportScores() throws Exception {
        // 创建测试数据
        Student student = new Student();
        student.setStudentNumber("2024001");
        student.setName("张三");
        student.setClazz("计算机1班");
        student.setMajor("计算机科学与技术");
        studentRepository.save(student);

        Course course = new Course();
        course.setName("高等数学");
        course.setCredit(4.0);
        course.setTeacherName("王教授");
        courseRepository.save(course);

        Score score = new Score(student, course, 85.5);
        scoreRepository.save(score);

        // 导出
        byte[] data = excelService.exportScores();

        assertNotNull(data);
        assertTrue(data.length > 0);
    }

    @Test
    void testExportStudentsEmpty() throws Exception {
        // 不创建学生，直接导出
        byte[] data = excelService.exportStudents();

        assertNotNull(data);
        assertTrue(data.length > 0);
    }

    @Test
    void testImportStudentsSuccess() throws Exception {
        // 创建包含学生数据的Excel文件内容
        org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("学生信息");

        // 创建表头
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("学号");
        headerRow.createCell(1).setCellValue("姓名");
        headerRow.createCell(2).setCellValue("班级");
        headerRow.createCell(3).setCellValue("专业");

        // 创建数据行
        org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("2024001");
        dataRow.createCell(1).setCellValue("张三");
        dataRow.createCell(2).setCellValue("计算机1班");
        dataRow.createCell(3).setCellValue("计算机科学与技术");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        // 创建MockMultipartFile
        MultipartFile file = new MockMultipartFile(
            "file",
            "students.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            out.toByteArray()
        );

        // 导入
        Map<String, Object> result = excelService.importStudents(file);

        assertNotNull(result);
        assertEquals(1, result.get("successCount"));
        assertEquals(0, result.get("errorCount"));

        // 验证数据是否保存
        Student saved = studentRepository.findByStudentNumber("2024001").orElse(null);
        assertNotNull(saved);
        assertEquals("张三", saved.getName());
    }

    @Test
    void testImportStudentsWithValidationError() throws Exception {
        // 创建缺少学号的Excel
        org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("学生信息");

        // 创建表头
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("学号");
        headerRow.createCell(1).setCellValue("姓名");
        headerRow.createCell(2).setCellValue("班级");
        headerRow.createCell(3).setCellValue("专业");

        // 创建缺少学号的数据行
        org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(1);
        dataRow.createCell(1).setCellValue("李四");
        dataRow.createCell(2).setCellValue("计算机1班");
        dataRow.createCell(3).setCellValue("计算机科学与技术");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        MultipartFile file = new MockMultipartFile(
            "file",
            "students.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            out.toByteArray()
        );

        Map<String, Object> result = excelService.importStudents(file);

        assertNotNull(result);
        assertEquals(0, result.get("successCount"));
        assertTrue((Integer) result.get("errorCount") > 0);
    }

    @Test
    void testImportStudentsDuplicate() throws Exception {
        // 先创建一个学生
        Student existingStudent = new Student();
        existingStudent.setStudentNumber("2024001");
        existingStudent.setName("张三");
        existingStudent.setClazz("计算机1班");
        existingStudent.setMajor("计算机科学与技术");
        studentRepository.save(existingStudent);

        // 创建包含重复学号的Excel
        org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("学生信息");

        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("学号");
        headerRow.createCell(1).setCellValue("姓名");
        headerRow.createCell(2).setCellValue("班级");
        headerRow.createCell(3).setCellValue("专业");

        org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("2024001"); // 重复的学号
        dataRow.createCell(1).setCellValue("重复的学生");
        dataRow.createCell(2).setCellValue("计算机1班");
        dataRow.createCell(3).setCellValue("计算机科学与技术");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        MultipartFile file = new MockMultipartFile(
            "file",
            "students.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            out.toByteArray()
        );

        Map<String, Object> result = excelService.importStudents(file);

        assertNotNull(result);
        assertTrue((Integer) result.get("errorCount") > 0);
    }

    @Test
    void testImportScoresSuccess() throws Exception {
        // 创建测试学生和课程
        Student student = new Student();
        student.setStudentNumber("2024001");
        student.setName("张三");
        student.setClazz("计算机1班");
        student.setMajor("计算机科学与技术");
        studentRepository.save(student);

        Course course = new Course();
        course.setName("高等数学");
        course.setCredit(4.0);
        course.setTeacherName("王教授");
        courseRepository.save(course);

        // 创建包含成绩的Excel
        org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("成绩信息");

        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("学号");
        headerRow.createCell(1).setCellValue("课程名称");
        headerRow.createCell(2).setCellValue("成绩");

        org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("2024001");
        dataRow.createCell(1).setCellValue("高等数学");
        dataRow.createCell(2).setCellValue(85.5);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        MultipartFile file = new MockMultipartFile(
            "file",
            "scores.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            out.toByteArray()
        );

        Map<String, Object> result = excelService.importScores(file);

        assertNotNull(result);
        assertEquals(1, result.get("successCount"));
        assertEquals(0, result.get("errorCount"));

        // 验证成绩是否保存
        List<Score> scores = scoreRepository.findAll();
        assertTrue(scores.size() > 0);
        assertEquals(85.5, scores.get(0).getValue());
    }

    @Test
    void testImportScoresInvalidScore() throws Exception {
        // 创建测试学生和课程
        Student student = new Student();
        student.setStudentNumber("2024001");
        student.setName("张三");
        student.setClazz("计算机1班");
        student.setMajor("计算机科学与技术");
        studentRepository.save(student);

        Course course = new Course();
        course.setName("高等数学");
        course.setCredit(4.0);
        course.setTeacherName("王教授");
        courseRepository.save(course);

        // 创建包含无效成绩的Excel
        org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("成绩信息");

        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("学号");
        headerRow.createCell(1).setCellValue("课程名称");
        headerRow.createCell(2).setCellValue("成绩");

        org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("2024001");
        dataRow.createCell(1).setCellValue("高等数学");
        dataRow.createCell(2).setCellValue(150); // 无效的成绩（>100）

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        MultipartFile file = new MockMultipartFile(
            "file",
            "scores.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            out.toByteArray()
        );

        Map<String, Object> result = excelService.importScores(file);

        assertNotNull(result);
        assertTrue((Integer) result.get("errorCount") > 0);
    }
}
