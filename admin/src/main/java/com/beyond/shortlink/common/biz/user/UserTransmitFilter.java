package com.beyond.shortlink.common.biz.user;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;

import static com.beyond.shortlink.common.constant.RedisCacheConstant.USER_LOGIN_KEY;

/**
 * 用户信息传输过滤器
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String username = request.getHeader("username");
        String token = request.getHeader("token");

        if (StrUtil.isAllNotBlank(username, token)) {
            Object userInfoJson = stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token);
            if (userInfoJson != null) {
                UserContext.setUser(JSONUtil.toBean(userInfoJson.toString(), UserInfoDTO.class));
            }
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }
}