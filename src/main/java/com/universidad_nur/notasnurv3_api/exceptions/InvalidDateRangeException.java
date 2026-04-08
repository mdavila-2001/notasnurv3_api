package com.universidad_nur.notasnurv3_api.exceptions;

public class InvalidDateRangeException extends RuntimeException {
    public InvalidDateRangeException(String message) {
        super(message);
    }
}