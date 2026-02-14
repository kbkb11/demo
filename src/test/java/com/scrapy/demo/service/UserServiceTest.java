package com.scrapy.demo.service;

import com.scrapy.demo.domain.Role;
import com.scrapy.demo.domain.User;
import com.scrapy.demo.repository.RoleRepository;
import com.scrapy.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试
 * 测试用户相关业务逻辑
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // 清空现有数据
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // 创建测试角色
        testRole = new Role();
        testRole.setName("STUDENT");
        testRole = roleRepository.save(testRole);

        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setRole(testRole);
    }

    @Test
    void testRegisterUser() {
        // 注册用户
        User registered = userService.register(testUser);

        assertNotNull(registered);
        assertNotNull(registered.getId());
        assertEquals("testuser", registered.getUsername());
        assertTrue(registered.isEnabled());
        // 密码应该被加密
        assertNotEquals("password123", registered.getPassword());
    }

    @Test
    void testFindByUsername() {
        // 先注册用户
        userService.register(testUser);

        // 按用户名查询
        User found = userService.findByUsername("testuser");

        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
        assertEquals("test@example.com", found.getEmail());
    }

    @Test
    void testFindByUsernameNotFound() {
        // 查询不存在的用户
        User found = userService.findByUsername("nonexistent");

        assertNull(found);
    }

    @Test
    void testLoadUserByUsername() {
        // 先注册用户
        userService.register(testUser);

        // 通过UserDetailsService加载
        UserDetails userDetails = userService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        // 加载不存在的用户应该抛出异常
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    void testListAllUsers() {
        // 注册多个用户
        userService.register(testUser);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password456");
        user2.setRole(testRole);
        userService.register(user2);

        // 列出所有用户
        var users = userService.listAllUsers();

        assertNotNull(users);
        assertTrue(users.size() >= 2);
    }

    @Test
    void testPasswordEncryption() {
        // 测试密码加密
        User registered = userService.register(testUser);

        // 密码不应该是原文
        assertNotEquals("password123", registered.getPassword());
        // 密码应该开始于$2a$（BCrypt标记）
        assertTrue(registered.getPassword().startsWith("$2a$"));
    }
}
