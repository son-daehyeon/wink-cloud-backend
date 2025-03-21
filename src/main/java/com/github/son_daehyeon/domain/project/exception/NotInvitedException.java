package com.github.son_daehyeon.domain.project.exception;

import org.springframework.http.HttpStatus;

import com.github.son_daehyeon.common.api.exception.ApiException;

public class NotInvitedException extends ApiException {

    public NotInvitedException() {

        super(HttpStatus.BAD_REQUEST, "초대된 유저가 아닙니다.");
    }
}
