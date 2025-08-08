package com.example.chatroom;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ChatroomIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Admin Controller Tests

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"}) // Simulate a logged-in admin
    public void adminCanRegisterUser() throws Exception {
        mockMvc.perform(post("/api/admin/users").param("username", "newUser"))
                .andExpect(status().isOk());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = 'newUser'", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    public void adminCannotRegisterExistingUser() throws Exception {
        // Create a user to ensure it exists
        jdbcTemplate.update("INSERT INTO users (username, role) VALUES ('existingUser', 'user')");

        mockMvc.perform(post("/api/admin/users").param("username", "existingUser"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User already exists"));
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    public void adminCanDeleteUserAndMessagesBecomeAnonymous() throws Exception {
        // Create user and message
        jdbcTemplate.update("INSERT INTO users (id, username, role) VALUES (100, 'userToDelete', 'user')");
        jdbcTemplate.update("INSERT INTO messages (user_id, username_cached, content, created_at) VALUES (100, 'userToDelete', 'my secret message', NOW())");

        // Perform delete operation
        mockMvc.perform(delete("/api/admin/users/userToDelete"))
                .andExpect(status().isOk());

        // Verify user is deleted
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = 'userToDelete'", Integer.class);
        assertThat(userCount).isEqualTo(0);

        // Verify message is now anonymous
        String messageAuthor = jdbcTemplate.queryForObject("SELECT username_cached FROM messages WHERE content = 'my secret message'", String.class);
        assertThat(messageAuthor).isEqualTo("anonymous");
    }

    // Message Controller Tests

    @Test
    public void anyUserCanGetAllMessagesSorted() throws Exception {
        // Insert messages in a specific order
        jdbcTemplate.update("INSERT INTO messages (username_cached, content, created_at) VALUES ('test', 'Older Message', NOW() - INTERVAL '1' HOUR)");
        jdbcTemplate.update("INSERT INTO messages (username_cached, content, created_at) VALUES ('test', 'Newest Message', NOW())");

        // Perform GET and check order
        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content", is("Newest Message")))
                .andExpect(jsonPath("$[1].content", is("Older Message")));
    }

    @Test
    public void userCanPostNewMessage() throws Exception {
        // Ensure user exists
        jdbcTemplate.update("INSERT INTO users (username, role) VALUES ('chatter', 'user')");

        // Post a message
        String messageJson = "{\"username\":\"chatter\", \"content\":\"Hello from test!\"}";
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(messageJson))
                .andExpect(status().isOk());

        // Verify the message was saved
        Integer msgCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM messages WHERE content = 'Hello from test!'", Integer.class);
        assertThat(msgCount).isEqualTo(1);
    }
}