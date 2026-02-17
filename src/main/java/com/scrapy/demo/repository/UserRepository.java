package com.scrapy.demo.repository;

import com.scrapy.demo.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"role", "student", "teacher"})
    Optional<User> findByUsername(String username);

}
