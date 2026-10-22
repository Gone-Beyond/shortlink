package com.beyond.shortlink.dto.req;

import lombok.Data;

@Data
public class UserLoginReqDTO {


    /**
     * 用户名
     */
    private String username;



    private String password;
}
