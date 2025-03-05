package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Файл уже существует")
public class FileAlreadyExistsException extends RuntimeException{
    public FileAlreadyExistsException(String message) {super(message);}
}
