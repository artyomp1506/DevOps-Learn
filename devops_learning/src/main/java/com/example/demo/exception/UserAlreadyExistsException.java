package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message)
    {
        super(message);
    }
}
