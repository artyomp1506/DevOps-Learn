package com.example.demo.exception;

public class IncorrectDataException extends RuntimeException{
    public IncorrectDataException(String message)
    {
        super(message);
    }
}
