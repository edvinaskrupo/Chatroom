package com.example.chatroom;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;

    public MessageController(MessageRepository messageRepo, UserRepository userRepo) {
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    public List<Message> getMessages() {
        return messageRepo.findAllDesc();
    }

    @PostMapping
    public ResponseEntity<?> postMessage(@RequestBody MessageRequest request) { // Change signature
        var user = userRepo.findByUsername(request.username()) // Use request object
                .orElseThrow(() -> new RuntimeException("User not found"));
        messageRepo.save(user.id(), request.username(), request.content()); // Use request object
        return ResponseEntity.ok().build();
    }
}
