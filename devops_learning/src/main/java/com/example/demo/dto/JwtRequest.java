package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class JwtRequest {
    @NotBlank(message = "Поле для имени не может быть пустым")
    String username;
    @Size(min = 8, message = "Пароль не может быть короче 8 символов")
    String password;
}
