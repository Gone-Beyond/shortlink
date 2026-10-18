package com.beyond.shortlink.common.enums;

import com.beyond.shortlink.common.convention.errorcode.IErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserErrorCodeEnum implements IErrorCode {

    USER_NULL("B000200", "用户记录不存在"),
    USER_EXIST("B000201", "用户记录已存在");


    private final String code;

    private final String message;


    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
