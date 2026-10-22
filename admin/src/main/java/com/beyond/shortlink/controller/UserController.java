package com.beyond.shortlink.controller;


import cn.hutool.core.bean.BeanUtil;
import com.beyond.shortlink.common.convention.result.Result;
import com.beyond.shortlink.common.convention.result.Results;
import com.beyond.shortlink.dto.req.UserLoginReqDTO;
import com.beyond.shortlink.dto.req.UserRegisterReqDTO;
import com.beyond.shortlink.dto.req.UserUpdateReqDTO;
import com.beyond.shortlink.dto.resp.UserActualRespDTO;
import com.beyond.shortlink.dto.resp.UserDTO;
import com.beyond.shortlink.dto.resp.UserLoginRespDTO;
import com.beyond.shortlink.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/short-link/v1/user/{username}")
    public Result<UserDTO> getUserByUsername(@PathVariable("username") String username) {
        return Results.success(userService.getUserByUsername(username));

    }

    @GetMapping("/api/short-link/v1/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable("username") String username) {
        return Results.success(BeanUtil.toBean(userService.getUserByUsername(username), UserActualRespDTO.class));
    }

    @GetMapping("/api/short-link/v1/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username) {
        return Results.success(userService.hasUsername(username));
    }

    /**
     * 注册用户
     */
    @PostMapping("/api/short-link/admin/v1/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParam) {
        userService.register(requestParam);
        return Results.success();
    }

    @PutMapping("/api/short-link/admin/v1/user")
    public Result<Void> update(@RequestBody UserUpdateReqDTO requestParam) {
        userService.update(requestParam);
        return Results.success();
    }


    @PostMapping("/api/short-link/admin/v1/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam) {

        return Results.success(userService.login(requestParam));
    }










}
