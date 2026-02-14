package com.scrapy.demo.service;

import com.scrapy.demo.domain.Student;
import com.scrapy.demo.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 学生服务测试
 * 测试学生管理相关业务逻辑
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StudentServiceTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    private Student testStudent;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();

        testStudent = new Student();
        testStudent.setStudentNumber("2024001");
        testStudent.setName("张三");
        testStudent.setClazz("计算机1班");
        testStudent.setMajor("计算机科学与技术");
    }

    @Test
    void testSaveStudent() {
        Student saved = studentService.save(testStudent);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("2024001", saved.getStudentNumber());
        assertEquals("张三", saved.getName());
    }

    @Test
    void testFindByStudentNumber() {
        studentService.save(testStudent);

        Student found = studentService.findByStudentNumber("2024001");

        assertNotNull(found);
        assertEquals("张三", found.getName());
        assertEquals("计算机科学与技术", found.getMajor());
    }

    @Test
    void testFindById() {
        Student saved = studentService.save(testStudent);

        Student found = studentService.findById(saved.getId());

        assertNotNull(found);
        assertEquals("2024001", found.getStudentNumber());
    }

    @Test
    void testFindByClass() {
        studentService.save(testStudent);

        Student student2 = new Student();
        student2.setStudentNumber("2024002");
        student2.setName("李四");
        student2.setClazz("计算机1班");
        student2.setMajor("计算机科学与技术");
        studentService.save(student2);

        List<Student> students = studentService.findByClass("计算机1班");

        assertNotNull(students);
        assertEquals(2, students.size());
    }

    @Test
    void testUpdateStudent() {
        Student saved = studentService.save(testStudent);

        Student updateData = new Student();
        updateData.setStudentNumber("2024001");
        updateData.setName("张三修改");
        updateData.setClazz("计算机2班");
        updateData.setMajor("软件工程");

        Student updated = studentService.update(saved.getId(), updateData);

        assertNotNull(updated);
        assertEquals("张三修改", updated.getName());
        assertEquals("计算机2班", updated.getClazz());
        assertEquals("软件工程", updated.getMajor());
    }

    @Test
    void testUpdateNonexistentStudent() {
        Student updateData = new Student();
        updateData.setName("不存在");

        Student updated = studentService.update(999L, updateData);

        assertNull(updated);
    }

    @Test
    void testDeleteStudent() {
        Student saved = studentService.save(testStudent);

        boolean deleted = studentService.delete(saved.getId());

        assertTrue(deleted);
        assertNull(studentService.findById(saved.getId()));
    }

    @Test
    void testDeleteNonexistentStudent() {
        boolean deleted = studentService.delete(999L);

        assertFalse(deleted);
    }

    @Test
    void testListAllStudents() {
        studentService.save(testStudent);

        Student student2 = new Student();
        student2.setStudentNumber("2024002");
        student2.setName("李四");
        student2.setClazz("计算机2班");
        student2.setMajor("软件工程");
        studentService.save(student2);

        List<Student> students = studentService.listAllStudents();

        assertNotNull(students);
        assertTrue(students.size() >= 2);
    }

    @Test
    void testBatchImportStudents() {
        List<Student> students = List.of(
            new Student("2024001", "张三", "计算机1班", "计算机科学与技术"),
            new Student("2024002", "李四", "计算机1班", "计算机科学与技术"),
            new Student("2024003", "王五", "计算机2班", "软件工程")
        );

        List<Student> imported = studentService.batchImport(students);

        assertNotNull(imported);
        assertTrue(imported.size() >= 3);
    }

    @Test
    void testBatchImportDuplicateStudentNumber() {
        studentService.save(testStudent);

        List<Student> students = List.of(
            new Student("2024001", "重复学号", "计算机1班", "计算机科学与技术")
        );

        assertThrows(IllegalArgumentException.class, () -> {
            studentService.batchImport(students);
        });
    }
}
