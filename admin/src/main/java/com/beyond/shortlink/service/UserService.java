package com.beyond.shortlink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.beyond.shortlink.dao.entity.UserDO;
import com.beyond.shortlink.dto.req.UserLoginReqDTO;
import com.beyond.shortlink.dto.req.UserRegisterReqDTO;
import com.beyond.shortlink.dto.req.UserUpdateReqDTO;
import com.beyond.shortlink.dto.resp.UserDTO;
import com.beyond.shortlink.dto.resp.UserLoginRespDTO;

public interface UserService extends IService<UserDO> {
    UserDTO getUserByUsername(String username);

    Boolean hasUsername(String username);

    void register(UserRegisterReqDTO requestParam);

    void update(UserUpdateReqDTO requestParam);


    UserLoginRespDTO login(UserLoginReqDTO requestParam);
}
