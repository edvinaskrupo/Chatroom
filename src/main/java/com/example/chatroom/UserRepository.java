package com.example.chatroom;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<User> findByUsername(String username) {
        var list = jdbc.query(
                "SELECT * FROM users WHERE username = ?",
                (rs, rowNum) -> new User(rs.getLong("id"), rs.getString("username"), rs.getString("role")),
                username
        );
        return list.stream().findFirst();
    }

    public User save(User user) {
        jdbc.update("INSERT INTO users (username, role) VALUES (?, ?)", user.username(), user.role());
        Long id = jdbc.queryForObject("SELECT id FROM users WHERE username = ?", Long.class, user.username());
        return new User(id, user.username(), user.role());
    }

    public void delete(Long userId) {
        jdbc.update("UPDATE messages SET user_id = NULL, username_cached = 'anonymous' WHERE user_id = ?", userId);
        jdbc.update("DELETE FROM users WHERE id = ?", userId);
    }
}
