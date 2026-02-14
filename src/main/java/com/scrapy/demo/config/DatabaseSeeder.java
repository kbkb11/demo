package com.scrapy.demo.config;

import com.scrapy.demo.domain.Role;
import com.scrapy.demo.domain.User;
import com.scrapy.demo.repository.RoleRepository;
import com.scrapy.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseSeeder(RoleRepository roleRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JdbcTemplate jdbcTemplate) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
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

        String existingPassword = user.getPassword();
        boolean passwordNeedsReset = existingPassword == null || existingPassword.isBlank()
                || !passwordEncoder.matches(rawPassword, existingPassword);
        if (isNewUser || passwordNeedsReset) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }

        User saved = userRepository.save(user);
        logger.info("{} user {}, role={}, enabled={}, id={}",
                isNewUser ? "Seeded" : "Updated",
                username,
                roleName,
                saved.isEnabled(),
                saved.getId());
    }
}
