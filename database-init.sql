-- 创建数据库
CREATE DATABASE IF NOT EXISTS student_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_management;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(100),
    `phone` VARCHAR(20),
    `role` VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

-- 成绩表
CREATE TABLE IF NOT EXISTS `score` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id` BIGINT NOT NULL,
    `course_id` BIGINT NOT NULL,
    `value` DECIMAL(5,2) NOT NULL,
    `recorded_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`student_id`) REFERENCES `student`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`course_id`) REFERENCES `course`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_student_course` (`student_id`, `course_id`),
    INDEX idx_student_id (student_id),
    INDEX idx_course_id (course_id),
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

-- 插入测试管理员用户 (密码: admin123)
INSERT INTO `user` (username, password, email, phone, role) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@school.com', '13800138000', 'ADMIN')
ON DUPLICATE KEY UPDATE username=username;

-- 插入测试教师用户 (密码: teacher123)
INSERT INTO `user` (username, password, email, phone, role) VALUES
('teacher', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'teacher@school.com', '13800138001', 'TEACHER')
ON DUPLICATE KEY UPDATE username=username;

-- 插入测试学生用户 (密码: student123)
INSERT INTO `user` (username, password, email, phone, role) VALUES
('student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'student1@school.com', '13800138002', 'STUDENT')
ON DUPLICATE KEY UPDATE username=username;

-- 插入测试学生数据
INSERT INTO `student` (student_number, name, clazz, major) VALUES
('2024001', '张三', '计算机1班', '计算机科学与技术'),
('2024002', '李四', '计算机1班', '计算机科学与技术'),
('2024003', '王五', '计算机2班', '软件工程'),
('2024004', '赵六', '计算机2班', '软件工程'),
('2024005', '钱七', '人工智能班', '人工智能')
ON DUPLICATE KEY UPDATE student_number=student_number;

-- 插入测试课程数据
INSERT INTO `course` (name, credit, teacher_name) VALUES
('高等数学', 4.0, '王教授'),
('大学英语', 3.0, '李老师'),
('数据结构', 4.0, '张教授'),
('操作系统', 4.0, '刘老师'),
('计算机网络', 3.0, '陈教授')
ON DUPLICATE KEY UPDATE name=name;

-- 插入测试成绩数据
INSERT INTO `score` (student_id, course_id, value) VALUES
(1, 1, 85.5),
(1, 2, 78.0),
(1, 3, 92.0),
(1, 4, 88.0),
(1, 5, 76.0),
(2, 1, 72.5),
(2, 2, 85.0),
(2, 3, 78.0),
(2, 4, 82.0),
(2, 5, 90.0),
(3, 1, 65.0),
(3, 2, 70.0),
(3, 3, 88.0),
(3, 4, 75.0),
(3, 5, 80.0)
ON DUPLICATE KEY UPDATE student_id=student_id, course_id=course_id;
