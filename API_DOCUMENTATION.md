# 学生管理系统 API 文档

## 基本信息

- **基础URL**: `http://localhost:8080`
- **认证方式**: Basic Auth（用户名:密码）
- **响应格式**: JSON

## 认证接口

### 1. 注册新用户
```
POST /api/auth/register
Content-Type: application/json

请求体:
{
  "username": "newuser",
  "password": "password123",
  "email": "user@example.com",
  "phone": "13800138000",
  "role": "STUDENT"
}

响应:
{
  "id": 1,
  "username": "newuser",
  "password": "$2a$10$...",
  "email": "user@example.com",
  "phone": "13800138000",
  "role": "STUDENT"
}
```

### 2. 获取当前用户信息
```
GET /api/auth/me?username=admin
Authorization: Basic admin:admin123

响应:
{
  "id": 1,
  "username": "admin",
  "email": "admin@school.com",
  "phone": "13800138000",
  "role": "ADMIN"
}
```

## 学生管理接口

### 1. 获取所有学生
```
GET /api/students
Authorization: Basic admin:admin123

响应:
[
  {
    "id": 1,
    "studentNumber": "2024001",
    "name": "张三",
    "clazz": "计算机1班",
    "major": "计算机科学与技术"
  },
  ...
]
```

### 2. 根据ID获取学生
```
GET /api/students/{id}
Authorization: Basic admin:admin123

示例: GET /api/students/1

响应:
{
  "id": 1,
  "studentNumber": "2024001",
  "name": "张三",
  "clazz": "计算机1班",
  "major": "计算机科学与技术"
}
```

### 3. 根据学号查询学生
```
GET /api/students/number/{studentNumber}
Authorization: Basic admin:admin123

示例: GET /api/students/number/2024001
```

### 4. 按班级查询学生
```
GET /api/students/class/{className}
Authorization: Basic admin:admin123

示例: GET /api/students/class/计算机1班
```

### 5. 创建新学生
```
POST /api/students
Authorization: Basic admin:admin123
Content-Type: application/json

请求体:
{
  "studentNumber": "2024006",
  "name": "新学生",
  "clazz": "计算机3班",
  "major": "计算机科学与技术"
}
```

### 6. 修改学生信息
```
PUT /api/students/{id}
Authorization: Basic admin:admin123
Content-Type: application/json

示例: PUT /api/students/1

请求体:
{
  "studentNumber": "2024001",
  "name": "张三",
  "clazz": "计算机1班",
  "major": "计算机科学与技术"
}
```

### 7. 删除学生
```
DELETE /api/students/{id}
Authorization: Basic admin:admin123

示例: DELETE /api/students/1
```

## 课程管理接口

### 1. 获取所有课程
```
GET /api/courses
Authorization: Basic admin:admin123
```

### 2. 根据ID获取课程
```
GET /api/courses/{id}
Authorization: Basic admin:admin123

示例: GET /api/courses/1
```

### 3. 根据课程名称查询
```
GET /api/courses/name/{courseName}
Authorization: Basic admin:admin123

示例: GET /api/courses/name/高等数学
```

### 4. 按教师名称查询课程
```
GET /api/courses/teacher/{teacherName}
Authorization: Basic admin:admin123

示例: GET /api/courses/teacher/王教授
```

### 5. 创建新课程
```
POST /api/courses
Authorization: Basic admin:admin123
Content-Type: application/json

请求体:
{
  "name": "线性代数",
  "credit": 3.0,
  "teacherName": "李教授"
}
```

### 6. 修改课程信息
```
PUT /api/courses/{id}
Authorization: Basic admin:admin123
Content-Type: application/json

示例: PUT /api/courses/1
```

### 7. 删除课程
```
DELETE /api/courses/{id}
Authorization: Basic admin:admin123

示例: DELETE /api/courses/1
```

## 成绩管理接口

### 1. 获取所有成绩
```
GET /api/scores
Authorization: Basic admin:admin123

响应:
[
  {
    "id": 1,
    "student": {
      "id": 1,
      "studentNumber": "2024001",
      "name": "张三"
    },
    "course": {
      "id": 1,
      "name": "高等数学"
    },
    "value": 85.5,
    "recordedAt": "2026-02-10T12:00:00"
  },
  ...
]
```

### 2. 根据ID获取成绩
```
GET /api/scores/{id}
Authorization: Basic admin:admin123

示例: GET /api/scores/1
```

### 3. 创建新成绩
```
POST /api/scores
Authorization: Basic admin:admin123
Content-Type: application/json

请求体:
{
  "student": {
    "id": 1
  },
  "course": {
    "id": 1
  },
  "value": 85.5
}
```

### 4. 修改成绩
```
PUT /api/scores/{id}?value=90.0
Authorization: Basic admin:admin123

示例: PUT /api/scores/1?value=90.0

注意: 修改成绩时会自动记录历史版本
```

## 成绩统计与分析接口

### 1. 获取整体成绩统计
```
GET /api/analysis/overall
Authorization: Basic admin:admin123

响应:
{
  "label": "整体成绩",
  "totalCount": 15,
  "average": 81.5,
  "highest": 92.0,
  "lowest": 65.0,
  "passRate": 93.33,
  "distribution": {
    "0-59": 0,
    "60-69": 1,
    "70-79": 3,
    "80-89": 6,
    "90-100": 5
  }
}
```

### 2. 获取课程成绩分析
```
GET /api/analysis/course/{courseId}
Authorization: Basic admin:admin123

示例: GET /api/analysis/course/1
```

### 3. 获取学生成绩分析
```
GET /api/analysis/student/{studentId}
Authorization: Basic admin:admin123

示例: GET /api/analysis/student/1
```

### 4. 获取班级成绩分析
```
GET /api/analysis/class/{className}
Authorization: Basic admin:admin123

示例: GET /api/analysis/class/计算机1班
```

### 5. 获取各课程及格率
```
GET /api/analysis/pass-rates
Authorization: Basic admin:admin123

响应:
{
  "课程ID:1": 100.0,
  "课程ID:2": 80.0,
  "课程ID:3": 100.0
}
```

### 6. 获取学生排名（按平均分）
```
GET /api/analysis/rankings
Authorization: Basic admin:admin123

响应:
[
  {
    "studentId": 1,
    "averageScore": 83.9,
    "rank": 1
  },
  {
    "studentId": 2,
    "averageScore": 81.5,
    "rank": 2
  }
]
```

## Excel导入导出接口

### 1. 导出学生信息
```
GET /api/excel/export/students
Authorization: Basic admin:admin123

响应: 返回Excel文件（students.xlsx）
```

### 2. 导出成绩信息
```
GET /api/excel/export/scores
Authorization: Basic admin:admin123

响应: 返回Excel文件（scores.xlsx）
```

### 3. 导入学生信息
```
POST /api/excel/import/students
Authorization: Basic admin:admin123
Content-Type: multipart/form-data

参数:
- file: 学生信息Excel文件

Excel格式要求（列顺序）:
| 学号 | 姓名 | 班级 | 专业 |

响应:
{
  "total": 3,
  "successCount": 2,
  "errorCount": 1,
  "successMessages": [
    "第 2 行：学生 李四 导入成功",
    "第 3 行：学生 王五 导入成功"
  ],
  "errorMessages": [
    "第 4 行：学号已存在：2024001"
  ]
}
```

### 4. 导入成绩信息
```
POST /api/excel/import/scores
Authorization: Basic admin:admin123
Content-Type: multipart/form-data

参数:
- file: 成绩信息Excel文件

Excel格式要求（列顺序）:
| 学号 | 课程名称 | 成绩 |

响应:
{
  "total": 5,
  "successCount": 5,
  "errorCount": 0,
  "successMessages": [
    "第 2 行：成绩 张三-高等数学 导入成功",
    ...
  ],
  "errorMessages": []
}
```

### 5. 获取学生导入模板
```
GET /api/excel/template/students
Authorization: Basic admin:admin123

响应: 返回模板Excel文件（student_template.xlsx）
```

### 6. 获取成绩导入模板
```
GET /api/excel/template/scores
Authorization: Basic admin:admin123

响应: 返回模板Excel文件（score_template.xlsx）
```

## 权限说明

| 接口 | ADMIN | TEACHER | STUDENT |
|-----|-------|---------|---------|
| 认证接口 | ✅ | ✅ | ✅ |
| 学生管理 | ✅ | ⚠️ | ❌ |
| 课程管理 | ✅ | ⚠️ | ❌ |
| 成绩管理 | ✅ | ✅ | ⚠️ |
| 成绩分析 | ✅ | ✅ | ✅ |
| Excel操作 | ✅ | ⚠️ | ❌ |

- ✅ 完全访问
- ⚠️ 部分访问或受限
- ❌ 无法访问

## 错误响应示例

```json
{
  "error": "用户不存在: admin",
  "status": 404,
  "timestamp": "2026-02-10T12:00:00"
}
```

## 测试工具推荐

- **Postman**: API测试和文档
- **curl**: 命令行测试
- **VS Code REST Client**: VS Code插件

### 使用curl测试示例

```bash
# 获取所有学生
curl -u admin:admin123 http://localhost:8080/api/students

# 创建新学生
curl -u admin:admin123 -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{"studentNumber":"2024006","name":"新学生","clazz":"计算机3班","major":"计算机科学与技术"}'

# 导出学生信息
curl -u admin:admin123 http://localhost:8080/api/excel/export/students \
  -o students.xlsx
```
