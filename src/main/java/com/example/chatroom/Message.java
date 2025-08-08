package com.example.chatroom;

import java.time.LocalDateTime;

public record Message(Long id, Long userId, String username, String content, LocalDateTime createdAt) {}
