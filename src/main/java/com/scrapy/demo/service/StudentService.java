package com.scrapy.demo.service;

import com.scrapy.demo.domain.Student;
import com.scrapy.demo.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 学生服务
 * 处理学生相关的业务逻辑
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * 获取所有学生
     */
    public List<Student> listAllStudents() {
        return studentRepository.findAll();
    }

    /**
     * 根据ID查找学生
     */
    public Student findById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    /**
     * 根据学号查找学生
     */
    public Student findByStudentNumber(String studentNumber) {
        return studentRepository.findByStudentNumber(studentNumber).orElse(null);
    }

    /**
     * 根据班级查找学生
     */
    public List<Student> findByClass(String clazz) {
        return studentRepository.findByClazz(clazz);
    }

    /**
     * 保存或更新学生
     */
    @Transactional
    public Student save(Student student) {
        return studentRepository.save(student);
    }

    /**
     * 更新学生信息
     */
    @Transactional
    public Student update(Long id, Student student) {
        Student existingStudent = findById(id);
        if (existingStudent == null) {
            return null;
        }
        existingStudent.setName(student.getName());
        existingStudent.setClazz(student.getClazz());
        existingStudent.setMajor(student.getMajor());
        return studentRepository.save(existingStudent);
    }

    /**
     * 删除学生
     */
    @Transactional
    public boolean delete(Long id) {
        Student student = findById(id);
        if (student == null) {
            return false;
        }
        studentRepository.delete(student);
        return true;
    }

    /**
     * 批量导入学生
     */
    @Transactional
    public List<Student> batchImport(List<Student> students) {
        for (Student student : students) {
            // 检查学号是否已存在
            if (findByStudentNumber(student.getStudentNumber()) != null) {
                throw new IllegalArgumentException("学号 " + student.getStudentNumber() + " 已存在");
            }
            save(student);
        }
        return listAllStudents();
    }
}
