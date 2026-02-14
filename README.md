# 学生管理系统项目

一个基于 Spring Boot 的完整学生管理系统，支持用户管理、学生信息管理、课程与成绩管理、成绩统计与分析、Excel导入导出等功能。

## 📋 项目概述

### 核心功能

1. **用户与权限管理**
   - 支持三种角色：学生、教师、管理员
   - 基于 Spring Security 的权限控制
   - 用户注册和登录功能

2. **学生信息管理**
   - 查询、创建、修改、删除学生信息
   - 按学号、班级、专业查询
   - 学生信息导入导出

3. **课程与成绩管理**
   - 课程信息管理（CRUD操作）
   - 成绩录入和修改
   - 成绩历史记录追踪
   - 按学生、课程、班级查询成绩

4. **成绩统计与分析**
   - 整体、课程、学生、班级成绩统计
   - 平均分、最高分、最低分计算
   - 及格率分析
   - 成绩分布统计（五个区间）
   - 学生排名（按平均分）

5. **Excel导入导出**
   - 导出学生和成绩信息到Excel
   - 从Excel导入学生和成绩数据
   - 导入验证和详细错误报告
   - 提供导入模板

## 🛠️ 技术栈

- **后端框架**: Spring Boot 3.3.6
- **数据库**: MySQL 8.0+
- **ORM**: Spring Data JPA + Hibernate
- **安全**: Spring Security
- **Excel处理**: Apache POI
- **构建工具**: Maven
- **Java版本**: Java 21

## 📁 项目结构

```
src/
├── main/
│   ├── java/com/scrapy/demo/
│   │   ├── ScrapyApplication.java          # 主应用入口
│   │   ├── config/
│   │   │   └── SecurityConfig.java         # 安全配置
│   │   ├── controller/
│   │   │   ├── AuthController.java         # 认证控制器
│   │   │   ├── StudentController.java      # 学生管理控制器
│   │   │   ├── CourseController.java       # 课程管理控制器
│   │   │   ├── ScoreController.java        # 成绩管理控制器
│   │   │   ├── ScoreAnalysisController.java # 成绩分析控制器
│   │   │   └── ExcelController.java        # Excel导入导出控制器
│   │   ├── domain/
│   │   │   ├── User.java                   # 用户实体
│   │   │   ├── Role.java                   # 角色枚举
│   │   │   ├── Student.java                # 学生实体
│   │   │   ├── Course.java                 # 课程实体
│   │   │   ├── Score.java                  # 成绩实体
│   │   │   └── ScoreHistory.java           # 成绩历史实体
│   │   ├── repository/
│   │   │   ├── UserRepository.java         # 用户数据访问
│   │   │   ├── StudentRepository.java      # 学生数据访问
│   │   │   ├── CourseRepository.java       # 课程数据访问
│   │   │   ├── ScoreRepository.java        # 成绩数据访问
│   │   │   └── ScoreHistoryRepository.java # 成绩历史数据访问
│   │   ├── service/
│   │   │   ├── UserService.java            # 用户服务
│   │   │   ├── StudentService.java         # 学生服务
│   │   │   ├── CourseService.java          # 课程服务
│   │   │   ├── ScoreService.java           # 成绩服务
│   │   │   ├── ScoreAnalysisService.java   # 成绩分析服务
│   │   │   ├── ExcelService.java           # Excel服务
│   │   │   └── security/
│   │   │       └── CustomUserDetails.java  # 自定义用户详情
│   ├── resources/
│   │   ├── application.properties          # 应用配置
│   │   ├── schema.sql                      # 数据库表结构
│   │   └── data.sql                        # 初始化数据
│   └── test/
│       └── java/com/scrapy/demo/
│           └── ScrapyApplicationTests.java # 应用测试
├── pom.xml                                 # Maven配置
├── all.md                                  # 项目需求文档
├── backend-progress.md                     # 后端开发进度
├── DATABASE_SETUP.md                       # 数据库初始化指南
└── API_DOCUMENTATION.md                    # API详细文档
```

## 🚀 快速开始

### 前置要求

- Java 21+
- Maven 3.6+
- MySQL 8.0+

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd demo
   ```

2. **配置数据库**
   
   编辑 `src/main/resources/application.properties`，修改数据库连接信息：
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/student_management
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

3. **初始化数据库**
   
   详见 [DATABASE_SETUP.md](DATABASE_SETUP.md)
   
   执行以下SQL脚本：
   - `src/main/resources/schema.sql` - 创建数据库和表
   - `src/main/resources/data.sql` - 初始化测试数据

4. **编译项目**
   ```bash
   mvn clean compile
   ```

5. **运行应用**
   ```bash
   mvn spring-boot:run
   ```
   
   或者使用IDE运行 `ScrapyApplication` 主类

6. **访问应用**
   ```
   http://localhost:8080
   ```

## 📚 API文档

详见 [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

主要API端点：

### 认证
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/me` - 获取当前用户信息

### 学生管理
- `GET /api/students` - 获取所有学生
- `POST /api/students` - 创建学生
- `PUT /api/students/{id}` - 修改学生
- `DELETE /api/students/{id}` - 删除学生
- `GET /api/students/class/{className}` - 按班级查询

### 课程管理
- `GET /api/courses` - 获取所有课程
- `POST /api/courses` - 创建课程
- `PUT /api/courses/{id}` - 修改课程
- `DELETE /api/courses/{id}` - 删除课程

### 成绩管理
- `GET /api/scores` - 获取所有成绩
- `POST /api/scores` - 创建成绩
- `PUT /api/scores/{id}` - 修改成绩

### 成绩分析
- `GET /api/analysis/overall` - 整体成绩统计
- `GET /api/analysis/course/{courseId}` - 课程分析
- `GET /api/analysis/student/{studentId}` - 学生分析
- `GET /api/analysis/rankings` - 学生排名

### Excel操作
- `GET /api/excel/export/students` - 导出学生
- `GET /api/excel/export/scores` - 导出成绩
- `POST /api/excel/import/students` - 导入学生
- `POST /api/excel/import/scores` - 导入成绩

## 🔐 测试账号

| 用户名 | 密码 | 角色 |
|-------|------|------|
| admin | admin123 | 管理员 |
| teacher | teacher123 | 教师 |
| student1 | student123 | 学生 |

## 📊 数据库设计

### 用户表 (user)
- id: 用户ID
- username: 用户名（唯一）
- password: 密码（BCrypt加密）
- email: 邮箱
- phone: 电话
- role: 角色（ADMIN/TEACHER/STUDENT）

### 学生表 (student)
- id: 学生ID
- student_number: 学号（唯一）
- name: 姓名
- clazz: 班级
- major: 专业

### 课程表 (course)
- id: 课程ID
- name: 课程名称
- credit: 学分
- teacher_name: 任课教师

### 成绩表 (score)
- id: 成绩ID
- student_id: 学生ID（外键）
- course_id: 课程ID（外键）
- value: 成绩值
- recorded_at: 记录时间

### 成绩历史表 (score_history)
- id: 历史ID
- score_id: 成绩ID（外键）
- before_value: 修改前成绩
- after_value: 修改后成绩
- modified_at: 修改时间

## 🔒 权限管理

- **ADMIN**: 拥有系统全部管理权限
- **TEACHER**: 可管理自己任课的课程和学生成绩
- **STUDENT**: 只能查看自己的信息和成绩

详见 [SecurityConfig.java](src/main/java/com/scrapy/demo/config/SecurityConfig.java)

## 📝 开发进度

详见 [backend-progress.md](backend-progress.md)

### 已完成
- ✅ 基础框架搭建
- ✅ 实体模型和数据库设计
- ✅ 认证和权限管理
- ✅ 学生、课程、成绩的CRUD操作
- ✅ 成绩统计与分析
- ✅ Excel导入导出

### 待开发
- [ ] 前端界面
- [ ] 数据可视化
- [ ] API文档自动生成
- [ ] 单元测试和集成测试
- [ ] 性能优化

## 🧪 测试

### 运行所有测试
```bash
mvn test
```

### 运行特定测试
```bash
mvn test -Dtest=ScrapyApplicationTests
```

## 📦 部署

### 构建JAR包
```bash
mvn clean package -DskipTests
```

生成的JAR文件位于 `target/demo-0.0.1-SNAPSHOT.jar`

### 运行JAR包
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

## 🐛 常见问题

### 1. 数据库连接失败
- 检查MySQL是否启动
- 验证 `application.properties` 中的数据库配置
- 确保 student_management 数据库已创建

### 2. Excel导入失败
- 检查Excel文件格式是否正确
- 验证学号和课程名称是否存在
- 查看返回的errorMessages获取详细错误信息

### 3. 权限拒绝错误
- 确认使用的账户权限是否足够
- 检查API端点是否需要特定角色权限
- 查看SecurityConfig中的权限配置

## 📄 许可证

MIT License

## 👥 贡献

欢迎提交Issue和Pull Request

## 📞 联系方式

如有问题，请通过以下方式联系：
- 提交Issue
- 发送邮件

---

**最后更新**: 2026-02-10
