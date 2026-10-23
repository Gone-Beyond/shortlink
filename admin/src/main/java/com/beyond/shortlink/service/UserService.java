package com.beyond.shortlink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.beyond.shortlink.dao.entity.UserDO;
import com.beyond.shortlink.dto.req.UserLoginReqDTO;
import com.beyond.shortlink.dto.req.UserRegisterReqDTO;
import com.beyond.shortlink.dto.req.UserUpdateReqDTO;
import com.beyond.shortlink.dto.resp.UserDTO;
import com.beyond.shortlink.dto.resp.UserLoginRespDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询脱敏用户信息
     */
    UserDTO getUserByUsername(String username);

    /**
     * 查询用户名是否存在
     */
    Boolean hasUsername(String username);

    /**
     * 注册用户
     */
    void register(UserRegisterReqDTO requestParam);

    /**
     * 修改用户信息
     */
    void update(UserUpdateReqDTO requestParam);

    /**
     * 用户登录
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * 检查用户是否登录
     */
    Boolean checkLogin(String username, String token);

    void logout(String username, String token);
}
