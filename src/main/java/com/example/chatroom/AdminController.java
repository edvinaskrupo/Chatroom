package com.example.chatroom;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository userRepo;
    private final MessageRepository messageRepo;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

    public AdminController(UserRepository userRepo, MessageRepository messageRepo, org.springframework.jdbc.core.JdbcTemplate jdbc) {
        this.userRepo = userRepo;
        this.messageRepo = messageRepo;
        this.jdbc = jdbc;
    }

    @PostMapping("/users")
    public ResponseEntity<?> register(@RequestParam String username) {
        if (userRepo.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("User already exists");
        }
        userRepo.save(new User(null, username, "user"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<?> delete(@PathVariable String username) {
        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepo.delete(user.id());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/{username}")
    public Map<String, Object> stats(@PathVariable String username) {
        userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found")); // Or return a 404 response

        Map<String, Object> statistics = jdbc.queryForMap(
                "SELECT " +
                        "    u.username, " +
                        "    COUNT(m.id) AS message_count, " +
                        "    MIN(m.created_at) AS first_message, " +
                        "    MAX(m.created_at) AS last_message, " +
                        "    COALESCE(AVG(LENGTH(m.content)), 0) AS avg_length " + // Use COALESCE to return 0 instead of NULL for average
                        "FROM users u " +
                        "LEFT JOIN messages m ON u.id = m.user_id " +
                        "WHERE u.username = ? " +
                        "GROUP BY u.username",
                username
        );

        String lastMessageText = jdbc.query(
                "SELECT content FROM messages WHERE username_cached = ? ORDER BY created_at DESC LIMIT 1",
                rs -> rs.next() ? rs.getString(1) : null, // Safely extract result or return null
                username
        );

        statistics.put("last_message_text", lastMessageText);
        return statistics;
    }
}
