package com.beyond.shortlink.common.biz.user;

import lombok.Data;

/**
 * 用户上下文中的用户信息
 */
@Data
public class UserInfoDTO {

    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;
}