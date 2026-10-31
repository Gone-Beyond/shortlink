package com.beyond.shortlink.controller;

import com.beyond.shortlink.common.biz.user.UserTransmitFilter;
import com.beyond.shortlink.dto.resp.UserDTO;
import com.beyond.shortlink.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.beyond.shortlink.common.constant.RedisCacheConstant.USER_LOGIN_KEY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerPathTest {

    private MockMvc mockMvc;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        UserService userService = mock(UserService.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get(USER_LOGIN_KEY + "alice", "valid-token"))
                .thenReturn("{\"id\":123,\"username\":\"alice\"}");
        when(userService.hasUsername("alice")).thenReturn(true);
        UserDTO user = new UserDTO();
        user.setUsername("alice");
        when(userService.getUserByUsername("alice")).thenReturn(user);
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .addFilters(new UserTransmitFilter(redisTemplate)).build();
    }

    @Test
    void usernameAvailabilityIsPublicAtNewPath() throws Exception {
        mockMvc.perform(get("/api/short-link/admin/v1/user/has-username").param("username", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void userDetailsRequireLoginAndAreAvailableAtNewPaths() throws Exception {
        for (String path : new String[]{"/api/short-link/admin/v1/user/alice",
                "/api/short-link/admin/v1/actual/user/alice"}) {
            mockMvc.perform(get(path)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("A000200"));
            mockMvc.perform(get(path).header("username", "alice").header("token", "valid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("0"))
                    .andExpect(jsonPath("$.data.username").value("alice"));
        }
    }
}
