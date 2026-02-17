-- 创建数据库
CREATE DATABASE IF NOT EXISTS student_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_management;

-- 学生表
CREATE TABLE IF NOT EXISTS `student` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_number` VARCHAR(20) NOT NULL UNIQUE,
    `name` VARCHAR(50) NOT NULL,
    `clazz` VARCHAR(50),
    `major` VARCHAR(100),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student_number (student_number),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 课程表
CREATE TABLE IF NOT EXISTS `course` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `credit` DECIMAL(3,1) NOT NULL,
    `teacher_name` VARCHAR(50) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 教师表
CREATE TABLE IF NOT EXISTS `teacher` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `teacher_number` VARCHAR(20) NOT NULL UNIQUE,
    `name` VARCHAR(50) NOT NULL,
    `department` VARCHAR(100),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_teacher_number (teacher_number),
    INDEX idx_teacher_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 角色表
CREATE TABLE IF NOT EXISTS `roles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(100),
    `phone` VARCHAR(20),
    `role_id` BIGINT NOT NULL,
    `student_id` BIGINT NULL,
    `teacher_id` BIGINT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_user_student (student_id),
    INDEX idx_user_teacher (teacher_id),
    FOREIGN KEY (`role_id`) REFERENCES `roles`(`id`) ON DELETE RESTRICT,
    FOREIGN KEY (`student_id`) REFERENCES `student`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`teacher_id`) REFERENCES `teacher`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 考试表
CREATE TABLE IF NOT EXISTS `exam` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `exam_date` DATE NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_exam_date (exam_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 成绩表
CREATE TABLE IF NOT EXISTS `score` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id` BIGINT NOT NULL,
    `course_id` BIGINT NOT NULL,
    `exam_id` BIGINT NULL,
    `value` DECIMAL(5,2) NOT NULL,
    `recorded_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`student_id`) REFERENCES `student`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`course_id`) REFERENCES `course`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`exam_id`) REFERENCES `exam`(`id`) ON DELETE SET NULL,
    UNIQUE KEY `uk_student_course_exam` (`student_id`, `course_id`, `exam_id`),
    INDEX idx_student_id (student_id),
    INDEX idx_course_id (course_id),
    INDEX idx_exam_id (exam_id),
    INDEX idx_value (value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 成绩历史表
CREATE TABLE IF NOT EXISTS `score_history` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `score_id` BIGINT NOT NULL,
    `before_value` DECIMAL(5,2),
    `after_value` DECIMAL(5,2) NOT NULL,
    `modified_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`score_id`) REFERENCES `score`(`id`) ON DELETE CASCADE,
    INDEX idx_score_id (score_id),
    INDEX idx_modified_at (modified_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初始化角色
INSERT INTO `roles` (name) VALUES
('ADMIN'),
('TEACHER'),
('STUDENT')
ON DUPLICATE KEY UPDATE name=name;

-- 插入测试管理员用户 (密码: admin123)
INSERT INTO `user` (username, password, email, phone, role_id) VALUES
('admin', 'admin123', 'admin@school.com', '13800138000', (SELECT id FROM roles WHERE name='ADMIN' LIMIT 1))
ON DUPLICATE KEY UPDATE username=username;

-- 插入测试教师用户 (密码: teacher123)
INSERT INTO `user` (username, password, email, phone, role_id) VALUES
('teacher', 'teacher123', 'teacher@school.com', '13800138001', (SELECT id FROM roles WHERE name='TEACHER' LIMIT 1))
ON DUPLICATE KEY UPDATE username=username;

-- 插入测试学生用户 (密码: student123)
INSERT INTO `user` (username, password, email, phone, role_id) VALUES
('student1', 'student123', 'student1@school.com', '13800138002', (SELECT id FROM roles WHERE name='STUDENT' LIMIT 1))
ON DUPLICATE KEY UPDATE username=username;

-- 插入测试学生数据
INSERT INTO `student` (student_number, name, clazz, major) VALUES
('2024001', '张三', '计算机1班', '计算机科学与技术'),
('2024002', '李四', '计算机1班', '计算机科学与技术'),
('2024003', '王五', '计算机2班', '软件工程'),
('2024004', '赵六', '计算机2班', '软件工程'),
('2024005', '钱七', '人工智能班', '人工智能')
ON DUPLICATE KEY UPDATE student_number=student_number;

-- 插入测试教师数据
INSERT INTO `teacher` (teacher_number, name, department) VALUES
('T0001', '李老师', '外语学院'),
('T0002', '王教授', '数学学院'),
('T0003', '张教授', '计算机学院'),
('T0004', '刘老师', '计算机学院'),
('T0005', '陈教授', '计算机学院')
ON DUPLICATE KEY UPDATE teacher_number=teacher_number;

-- 插入测试课程数据
INSERT INTO `course` (name, credit, teacher_name) VALUES
('高等数学', 4.0, '王教授'),
('大学英语', 3.0, '李老师'),
('数据结构', 4.0, '张教授'),
('操作系统', 4.0, '刘老师'),
('计算机网络', 3.0, '陈教授')
ON DUPLICATE KEY UPDATE name=name;

-- 插入考试数据
INSERT INTO `exam` (name, exam_date) VALUES
('2026学期第一次月考', '2026-03-15'),
('2026学期期中考试', '2026-05-10'),
('2026学期期末考试', '2026-06-28')
ON DUPLICATE KEY UPDATE name=name;

-- 插入测试成绩数据
INSERT INTO `score` (student_id, course_id, exam_id, value) VALUES
(1, 1, 1, 85.5),
(1, 2, 1, 78.0),
(1, 3, 1, 92.0),
(1, 4, 1, 88.0),
(1, 5, 1, 76.0),
(2, 1, 1, 72.5),
(2, 2, 1, 85.0),
(2, 3, 1, 78.0),
(2, 4, 1, 82.0),
(2, 5, 1, 90.0),
(3, 1, 1, 65.0),
(3, 2, 1, 70.0),
(3, 3, 1, 88.0),
(3, 4, 1, 75.0),
(3, 5, 1, 80.0)
ON DUPLICATE KEY UPDATE student_id=student_id, course_id=course_id, exam_id=exam_id;
