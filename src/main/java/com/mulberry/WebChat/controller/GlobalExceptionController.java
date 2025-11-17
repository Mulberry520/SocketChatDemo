package com.mulberry.WebChat.controller;

import com.mulberry.WebChat.common.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionController {
    @ExceptionHandler
    public R<Void> handleException(Exception e) {
        // TODO

        System.out.println("error");
        return R.error(e.getMessage());
    }
}
