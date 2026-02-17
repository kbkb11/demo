-- 初始化测试数据
USE student_management;

-- 插入测试管理员用户 (密码: admin123)
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

-- 绑定学生账号到学生记录
UPDATE `user` SET student_id = (SELECT id FROM student WHERE student_number='2024001' LIMIT 1)
WHERE username='student1';

-- 绑定教师账号到教师记录
UPDATE `user` SET teacher_id = (SELECT id FROM teacher WHERE teacher_number='T0001' LIMIT 1)
WHERE username='teacher';

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
ON DUPLICATE KEY UPDATE student_id=student_id, course_id=course_id;

-- 插入学习资料数据
INSERT INTO `learning_material` (course_keyword, title, url, type, difficulty_tag) VALUES
('高等数学', '高数基础错题拆解', 'https://www.icourse163.org', 'video', 'basic'),
('高等数学', '高数核心题型训练', 'https://www.icourse163.org', 'exercise', 'advanced'),
('大学英语', '英语阅读提分策略', 'https://www.icourse163.org', 'article', 'basic'),
('大学英语', '英语听力专项训练', 'https://www.icourse163.org', 'audio', 'advanced'),
('数据结构', '数据结构图解课', 'https://www.icourse163.org', 'video', 'basic'),
('数据结构', '数据结构刷题集', 'https://www.icourse163.org', 'exercise', 'advanced'),
('操作系统', '操作系统进程与线程', 'https://www.icourse163.org', 'article', 'basic'),
('计算机网络', '网络协议抓包实战', 'https://www.icourse163.org', 'lab', 'advanced')
ON DUPLICATE KEY UPDATE title=title;
