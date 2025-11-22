package com.mulberry.WebChat.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(String msg) {
        super(msg);
        this.code = 400;
    }

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }
}
