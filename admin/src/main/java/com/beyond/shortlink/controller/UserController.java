package com.beyond.shortlink.controller;


import com.beyond.shortlink.common.convention.result.Result;
import com.beyond.shortlink.common.convention.result.Results;
import com.beyond.shortlink.dto.resp.UserDTO;
import com.beyond.shortlink.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserDTO> getUserByUsername(@PathVariable("username") String username) {
        UserDTO userDTO = userService.getUserByUsername(username);
        return Results.success(userDTO);

    }
}
