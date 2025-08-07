CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       role VARCHAR(10) NOT NULL
);

CREATE TABLE messages (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id BIGINT,
                          username_cached VARCHAR(50) NOT NULL,
                          content VARCHAR(500) NOT NULL,
                          created_at TIMESTAMP NOT NULL
);
