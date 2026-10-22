package com.beyond.shortlink.dto.req;

import lombok.Data;

@Data
public class UserUpdateReqDTO {
    /**
     * ID
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

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String mail;

    private String password;
}
