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
        return jdbc.queryForMap(
                "SELECT username_cached AS username, COUNT(*) AS message_count, " +
                        "MIN(created_at) AS first_message, MAX(created_at) AS last_message, " +
                        "AVG(LENGTH(content)) AS avg_length, " +
                        "(SELECT content FROM messages WHERE username_cached = ? ORDER BY created_at DESC LIMIT 1) AS last_message_text " +
                        "FROM messages WHERE username_cached = ?",
                username, username
        );
    }
}
