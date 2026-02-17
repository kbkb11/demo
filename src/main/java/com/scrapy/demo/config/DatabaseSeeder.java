package com.scrapy.demo.config;

import com.scrapy.demo.domain.Exam;
import com.scrapy.demo.domain.LearningMaterial;
import com.scrapy.demo.domain.Role;
import com.scrapy.demo.domain.User;
import com.scrapy.demo.domain.Student;
import com.scrapy.demo.domain.Teacher;
import com.scrapy.demo.repository.ExamRepository;
import com.scrapy.demo.repository.LearningMaterialRepository;
import com.scrapy.demo.repository.RoleRepository;
import com.scrapy.demo.repository.StudentRepository;
import com.scrapy.demo.repository.TeacherRepository;
import com.scrapy.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 启动时自动补齐角色与管理员账号，避免认证因缺少数据而失败。
 */
@Configuration
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final LearningMaterialRepository learningMaterialRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseSeeder(RoleRepository roleRepository,
                          UserRepository userRepository,
                          LearningMaterialRepository learningMaterialRepository,
                          ExamRepository examRepository,
                          StudentRepository studentRepository,
                          TeacherRepository teacherRepository,
                          PasswordEncoder passwordEncoder,
                          JdbcTemplate jdbcTemplate) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.learningMaterialRepository = learningMaterialRepository;
        this.examRepository = examRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        repairScoreHistoryForeignKey();

        List<String> roleNames = Arrays.asList("ADMIN", "TEACHER", "STUDENT");
        roleNames.forEach(this::ensureRole);

        ensureUser("admin", "admin123", "admin@school.com", "13800138000", "ADMIN");
        ensureUser("teacher", "teacher123", "teacher@school.com", "13800138001", "TEACHER");
        ensureUser("student1", "student123", "student1@school.com", "13800138002", "STUDENT");
        ensureExams();
        ensureTeachers();
        ensureLearningMaterials();
    }

    private void ensureRole(String roleName) {
        roleRepository.findByName(roleName).orElseGet(() -> {
            Role created = new Role(roleName);
            Role saved = roleRepository.save(created);
            logger.info("Created role {} with id {}", roleName, saved.getId());
            return saved;
        });
    }

    /**
     * 历史脏数据中 score_history 可能残留一条指向 scores 表的外键，导致更新成绩写历史失败。
     */
    private void repairScoreHistoryForeignKey() {
        String fkName = jdbcTemplate.query(
                "SELECT CONSTRAINT_NAME " +
                        "FROM information_schema.REFERENTIAL_CONSTRAINTS " +
                        "WHERE CONSTRAINT_SCHEMA = DATABASE() " +
                        "AND TABLE_NAME = 'score_history' " +
                        "AND REFERENCED_TABLE_NAME = 'scores' " +
                        "LIMIT 1",
                rs -> rs.next() ? rs.getString(1) : null
        );
        if (fkName == null || fkName.isBlank()) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE score_history DROP FOREIGN KEY " + fkName);
        logger.warn("Dropped legacy foreign key {} on score_history -> scores", fkName);
    }

    private void ensureUser(String username,
                            String rawPassword,
                            String email,
                            String phone,
                            String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("角色未初始化：" + roleName));
        User user = userRepository.findByUsername(username).orElseGet(User::new);

        boolean isNewUser = user.getId() == null;
        if (isNewUser) {
            user.setUsername(username);
        }

        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setEnabled(true);
        if ("STUDENT".equals(roleName)) {
            Student linked = studentRepository.findByStudentNumber(username)
                    .or(() -> studentRepository.findFirstByName(username))
                    .orElseGet(() -> studentRepository.findAll().stream().findFirst().orElse(null));
            user.setStudent(linked);
        }
        if ("TEACHER".equals(roleName)) {
            Teacher linked = teacherRepository.findByTeacherNumber(username)
                    .or(() -> teacherRepository.findFirstByName(username))
                    .orElseGet(() -> teacherRepository.findAll().stream().findFirst().orElse(null));
            user.setTeacher(linked);
        }

        String existingPassword = user.getPassword();
        boolean passwordNeedsReset = existingPassword == null || existingPassword.isBlank()
                || !rawPassword.equals(existingPassword);
        if (isNewUser || passwordNeedsReset) {
            user.setPassword(rawPassword);
        }

        try {
            User saved = userRepository.save(user);
            logger.info("{} user {}, role={}, enabled={}, id={}",
                    isNewUser ? "Seeded" : "Updated",
                    username,
                    roleName,
                    saved.isEnabled(),
                    saved.getId());
        } catch (DataIntegrityViolationException ex) {
            // If a legacy seed already inserted the username, update it instead of failing startup.
            userRepository.findByUsername(username).ifPresent(existing -> {
                existing.setEmail(email);
                existing.setPhone(phone);
                existing.setRole(role);
                existing.setEnabled(true);
                if ("STUDENT".equals(roleName)) {
                    Student linked = studentRepository.findByStudentNumber(username)
                            .or(() -> studentRepository.findFirstByName(username))
                            .orElseGet(() -> studentRepository.findAll().stream().findFirst().orElse(null));
                    existing.setStudent(linked);
                }
                if ("TEACHER".equals(roleName)) {
                    Teacher linked = teacherRepository.findByTeacherNumber(username)
                            .or(() -> teacherRepository.findFirstByName(username))
                            .orElseGet(() -> teacherRepository.findAll().stream().findFirst().orElse(null));
                    existing.setTeacher(linked);
                }
                String currentPassword = existing.getPassword();
                boolean needsReset = currentPassword == null || currentPassword.isBlank()
                        || !rawPassword.equals(currentPassword);
                if (needsReset) {
                    existing.setPassword(rawPassword);
                }
                User saved = userRepository.save(existing);
                logger.info("Updated existing user {}, role={}, enabled={}, id={}",
                        username,
                        roleName,
                        saved.isEnabled(),
                        saved.getId());
            });
        }
    }

    private void ensureLearningMaterials() {
        if (learningMaterialRepository.count() > 0) {
            return;
        }
        learningMaterialRepository.saveAll(Arrays.asList(
                new LearningMaterial("高等数学", "高数基础错题拆解", "https://www.icourse163.org", "video", "basic"),
                new LearningMaterial("高等数学", "高数核心题型训练", "https://www.icourse163.org", "exercise", "advanced"),
                new LearningMaterial("大学英语", "英语阅读提分策略", "https://www.icourse163.org", "article", "basic"),
                new LearningMaterial("大学英语", "英语听力专项训练", "https://www.icourse163.org", "audio", "advanced"),
                new LearningMaterial("数据结构", "数据结构图解课", "https://www.icourse163.org", "video", "basic"),
                new LearningMaterial("数据结构", "数据结构刷题集", "https://www.icourse163.org", "exercise", "advanced"),
                new LearningMaterial("操作系统", "操作系统进程与线程", "https://www.icourse163.org", "article", "basic"),
                new LearningMaterial("计算机网络", "网络协议抓包实战", "https://www.icourse163.org", "lab", "advanced")
        ));
        logger.info("Seeded default learning materials");
    }

    private void ensureTeachers() {
        if (teacherRepository.count() > 0) {
            return;
        }
        teacherRepository.saveAll(Arrays.asList(
                new Teacher("T0001", "李老师", "外语学院"),
                new Teacher("T0002", "王教授", "数学学院"),
                new Teacher("T0003", "张教授", "计算机学院"),
                new Teacher("T0004", "刘老师", "计算机学院"),
                new Teacher("T0005", "陈教授", "计算机学院")
        ));
        logger.info("Seeded default teachers");
    }

    private void ensureExams() {
        if (examRepository.count() > 0) {
            return;
        }
        examRepository.saveAll(Arrays.asList(
                new Exam("2026学期第一次月考", LocalDate.of(2026, 3, 15)),
                new Exam("2026学期期中考试", LocalDate.of(2026, 5, 10)),
                new Exam("2026学期期末考试", LocalDate.of(2026, 6, 28))
        ));
        logger.info("Seeded default exams");
    }
}
