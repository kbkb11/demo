package com.scrapy.demo.service;

import com.scrapy.demo.domain.Course;
import com.scrapy.demo.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import jakarta.validation.ConstraintViolationException;

/**
 * 课程服务测试
 * 测试课程管理相关业务逻辑
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    private Course testCourse;

    @BeforeEach
    void setUp() {
        courseRepository.deleteAll();

        testCourse = new Course();
        testCourse.setName("高等数学");
        testCourse.setCredit(4.0);
        testCourse.setTeacherName("王教授");
    }

    @Test
    void testSaveCourse() {
        Course saved = courseService.save(testCourse);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("高等数学", saved.getName());
        assertEquals(4.0, saved.getCredit());
    }

    @Test
    void testFindByName() {
        courseService.save(testCourse);

        Course found = courseService.findByName("高等数学");

        assertNotNull(found);
        assertEquals("王教授", found.getTeacherName());
    }

    @Test
    void testFindById() {
        Course saved = courseService.save(testCourse);

        Course found = courseService.findById(saved.getId());

        assertNotNull(found);
        assertEquals("高等数学", found.getName());
    }

    @Test
    void testFindByTeacher() {
        courseService.save(testCourse);

        Course course2 = new Course();
        course2.setName("大学英语");
        course2.setCredit(3.0);
        course2.setTeacherName("王教授");
        courseService.save(course2);

        List<Course> courses = courseService.findByTeacher("王教授");

        assertNotNull(courses);
        assertEquals(2, courses.size());
    }

    @Test
    void testUpdateCourse() {
        Course saved = courseService.save(testCourse);

        Course updateData = new Course();
        updateData.setName("高等数学（修改）");
        updateData.setCredit(5.0);
        updateData.setTeacherName("李教授");

        Course updated = courseService.update(saved.getId(), updateData);

        assertNotNull(updated);
        assertEquals("高等数学（修改）", updated.getName());
        assertEquals(5.0, updated.getCredit());
        assertEquals("李教授", updated.getTeacherName());
    }

    @Test
    void testDeleteCourse() {
        Course saved = courseService.save(testCourse);

        boolean deleted = courseService.delete(saved.getId());

        assertTrue(deleted);
        assertNull(courseService.findById(saved.getId()));
    }

    @Test
    void testListAllCourses() {
        courseService.save(testCourse);

        Course course2 = new Course();
        course2.setName("大学英语");
        course2.setCredit(3.0);
        course2.setTeacherName("李老师");
        courseService.save(course2);

        List<Course> courses = courseService.listAllCourses();

        assertNotNull(courses);
        assertTrue(courses.size() >= 2);
    }

    @Test
    void testCreditValidation() {
        testCourse.setCredit(0.2); // 低于0.5的最小值
        
        // 持久化时应触发验证异常
        assertThrows(ConstraintViolationException.class, () -> {
            courseService.save(testCourse);
        });
    }

    @Test
    void testFindNonexistentCourse() {
        Course found = courseService.findByName("不存在的课程");

        assertNull(found);
    }
}
