package com.fanda.auth.jwt.exception;

import com.fanda.auth.global.BaseErrorCode;
import com.fanda.auth.global.CustomException;

public class UserExceptionHandler extends CustomException {

    public UserExceptionHandler(BaseErrorCode code){
        super(code);
    }
}
