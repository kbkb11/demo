package com.scrapy.demo.controller;

import com.scrapy.demo.domain.Role;
import com.scrapy.demo.domain.Student;
import com.scrapy.demo.domain.Teacher;
import com.scrapy.demo.domain.User;
import com.scrapy.demo.repository.RoleRepository;
import com.scrapy.demo.repository.StudentRepository;
import com.scrapy.demo.repository.TeacherRepository;
import com.scrapy.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          RoleRepository roleRepository,
                          StudentRepository studentRepository,
                          TeacherRepository teacherRepository) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.roleRepository = roleRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody Map<String, String> payload) {
        String username = payload.getOrDefault("username", "").trim();
        String password = payload.getOrDefault("password", "").trim();
        String roleName = payload.getOrDefault("role", "").trim().toUpperCase();
        String studentNumber = payload.getOrDefault("studentNumber", "").trim();
        String teacherNumber = payload.getOrDefault("teacherNumber", "").trim();

        if (username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "用户名或密码不能为空"));
        }

        Role role = roleRepository.findByName(roleName).orElse(null);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "角色不合法"));
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setEnabled(true);

        if ("STUDENT".equals(roleName)) {
            if (studentNumber.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "学生注册必须填写学号"));
            }
            Student student = studentRepository.findByStudentNumber(studentNumber).orElse(null);
            if (student == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "学号不存在"));
            }
            user.setStudent(student);
        }

        if ("TEACHER".equals(roleName)) {
            if (teacherNumber.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "教师注册必须填写工号"));
            }
            Teacher teacher = teacherRepository.findByTeacherNumber(teacherNumber).orElse(null);
            if (teacher == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "教师工号不存在"));
            }
            user.setTeacher(teacher);
        }

        User saved = userService.register(user);
        return ResponseEntity.ok(buildUserPayload(saved));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody Map<String, String> payload,
                                                     HttpServletRequest request) {
        String username = payload.getOrDefault("username", "").trim();
        String password = payload.getOrDefault("password", "").trim();
        if (username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "用户名或密码不能为空"));
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );
            User user = userService.findByUsername(username);
            return ResponseEntity.ok(buildUserPayload(user));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "用户名或密码错误"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> current(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.findByUsername(authentication.getName());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(buildUserPayload(user));
    }

    private Map<String, Object> buildUserPayload(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("role", user.getRole() != null ? user.getRole().getName() : null);
        map.put("studentId", user.getStudent() != null ? user.getStudent().getId() : null);
        map.put("teacherId", user.getTeacher() != null ? user.getTeacher().getId() : null);
        String displayName = user.getUsername();
        if (user.getStudent() != null && user.getStudent().getName() != null) {
            displayName = user.getStudent().getName();
        } else if (user.getTeacher() != null && user.getTeacher().getName() != null) {
            displayName = user.getTeacher().getName();
        }
        map.put("displayName", displayName);
        return map;
    }
}
