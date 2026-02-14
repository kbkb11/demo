-- 初始化测试数据
USE student_management;

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
