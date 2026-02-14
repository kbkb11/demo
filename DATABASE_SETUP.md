# 数据库初始化指南

## 快速开始

### 1. 创建数据库和表

在MySQL中执行以下命令来创建数据库和所有表：

```sql
-- 使用MySQL命令行工具执行 schema.sql 文件
mysql -u root -p < schema.sql

-- 或者在MySQL客户端中直接执行 src/main/resources/schema.sql 的内容
```

**或者在 MySQL Workbench / HeidiSQL 等图形化工具中：**

1. 打开你的MySQL客户端
2. 新建一个查询标签
3. 复制 `src/main/resources/schema.sql` 中的所有SQL语句
4. 执行该查询

### 2. 初始化测试数据

执行 `src/main/resources/data.sql` 中的语句来插入测试数据：

```sql
-- 使用MySQL命令行工具
mysql -u root -p student_management < data.sql

-- 或者在MySQL客户端中直接执行 src/main/resources/data.sql 的内容
```

## 测试账号

初始化后，系统会自动创建以下测试账号（密码均为：`admin123` / `teacher123` / `student123`）：

| 用户名 | 密码 | 角色 | 描述 |
|-------|------|------|------|
| admin | admin123 | ADMIN | 管理员 |
| teacher | teacher123 | TEACHER | 教师 |
| student1 | student123 | STUDENT | 学生 |

> **注意**：这些是BCrypt加密后的密码，所有账号使用对应的明文密码登录。

## 测试数据

初始化后会创建以下测试数据：

### 学生信息
- 5名学生分布在不同班级和专业

### 课程信息
- 5门课程，包含学分和任课教师信息

### 成绩信息
- 15条成绩记录，覆盖部分学生的课程成绩

## 数据库配置

确保 `application.properties` 中的数据库连接信息与你的MySQL配置一致：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/student_management?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
```

## 常用SQL命令

```sql
-- 查看所有表
SHOW TABLES;

-- 查看学生表数据
SELECT * FROM student;

-- 查看课程表数据
SELECT * FROM course;

-- 查看成绩表数据
SELECT * FROM score;

-- 查看用户表数据
SELECT * FROM user;

-- 删除整个数据库（谨慎使用）
DROP DATABASE student_management;
```

## 注意事项

1. **数据约束**：
   - 学号（student_number）和用户名（username）是唯一的
   - 一个学生对应一门课程只能有一条成绩记录
   - 删除学生或课程时会级联删除相关成绩

2. **自增ID**：
   - 所有表的主键都采用自增ID，无需手动指定

3. **时间戳**：
   - created_at 在记录创建时自动设置为当前时间
   - updated_at 在记录修改时自动更新为当前时间

4. **导入/导出**：
   - 系统提供Excel导入导出功能
   - 可通过API导出学生和成绩数据到Excel
   - 支持从Excel文件导入学生和成绩信息
