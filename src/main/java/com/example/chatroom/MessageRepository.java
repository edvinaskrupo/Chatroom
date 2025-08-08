package com.example.chatroom;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class MessageRepository {
    private final JdbcTemplate jdbc;

    public MessageRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Message> findAllDesc() {
        return jdbc.query(
                "SELECT * FROM messages ORDER BY created_at DESC",
                (rs, rowNum) -> new Message(
                        rs.getLong("id"),
                        rs.getObject("user_id", Long.class),
                        rs.getString("username_cached"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                )
        );
    }

    public void save(Long userId, String username, String content) {
        jdbc.update(
                "INSERT INTO messages (user_id, username_cached, content, created_at) VALUES (?, ?, ?, ?)",
                userId, username, content, LocalDateTime.now()
        );
    }
}
