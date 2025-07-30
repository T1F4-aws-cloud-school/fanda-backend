package com.fanda.auth.global;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{

    private final BaseErrorCode errorCode;

    public CustomException(BaseErrorCode errorCode){
        this.errorCode = errorCode;
    }
}
