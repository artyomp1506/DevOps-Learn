package com.example.demo.entity;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@RedisHash("JwtToken")
public class JwtToken {
    private String id;
    @Indexed
    private String body;
    @TimeToLive
    private Long expirationInSeconds;

    public JwtToken(String body, Long expirationInSeconds) {
        this.body = body;
        this.expirationInSeconds = expirationInSeconds;
    }
}

