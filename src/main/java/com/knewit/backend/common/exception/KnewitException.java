package com.knewit.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class KnewitException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    public KnewitException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
