package com.beyond.shortlink.controller;


import cn.hutool.core.bean.BeanUtil;
import com.beyond.shortlink.common.convention.result.Result;
import com.beyond.shortlink.common.convention.result.Results;
import com.beyond.shortlink.dto.resp.UserActualRespDTO;
import com.beyond.shortlink.dto.resp.UserDTO;
import com.beyond.shortlink.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;


    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserDTO> getUserByUsername(@PathVariable("username") String username) {
        return Results.success(userService.getUserByUsername(username));

    }

    @GetMapping("/api/shortlink/v1/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable("username") String username) {
        return Results.success(BeanUtil.toBean(userService.getUserByUsername(username), UserActualRespDTO.class));
    }



}
