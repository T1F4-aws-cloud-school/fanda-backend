package com.fanda.auth.jwt.exception;

import com.fanda.auth.global.BaseErrorCode;
import com.fanda.auth.global.CustomException;
import lombok.Getter;

@Getter
public class SecurityCustomException extends CustomException {

    private final Throwable cause;

    public SecurityCustomException(BaseErrorCode errorCode){
        super(errorCode);
        this.cause = null;
    }

    public SecurityCustomException(BaseErrorCode errorCode, Throwable cause){
        super(errorCode);
        this.cause = cause;
    }
}
