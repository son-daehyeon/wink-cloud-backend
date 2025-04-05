package com.github.son_daehyeon.domain.auth.exception;

import org.springframework.http.HttpStatus;

import com.github.son_daehyeon.common.api.ApiException;

public class AuthenticationFailException extends ApiException {

    public AuthenticationFailException() {

        super(HttpStatus.UNAUTHORIZED, "인증에 실패하였습니다.");
    }
}
