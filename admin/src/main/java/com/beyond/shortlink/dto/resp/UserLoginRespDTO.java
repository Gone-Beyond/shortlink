package com.beyond.shortlink.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户登录响应实体
 */
@Data
@AllArgsConstructor
public class UserLoginRespDTO {

    /**
     * 用户 Token
     */
    private String token;
}
