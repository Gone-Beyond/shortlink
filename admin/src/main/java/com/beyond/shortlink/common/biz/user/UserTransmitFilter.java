package com.beyond.shortlink.common.biz.user;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.beyond.shortlink.common.convention.result.Result;
import com.beyond.shortlink.common.convention.result.Results;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;

import static com.beyond.shortlink.common.constant.RedisCacheConstant.USER_LOGIN_KEY;
import static com.beyond.shortlink.common.enums.UserErrorCodeEnum.USER_TOKEN_FAIL;

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
        try {
            if (!isIgnore(request.getRequestURI(), request.getMethod())) {
                String username = request.getHeader("username");
                String token = request.getHeader("token");
                if (!StrUtil.isAllNotBlank(username, token)) {
                    returnJson((HttpServletResponse) servletResponse,
                            Results.failure(USER_TOKEN_FAIL.code(), USER_TOKEN_FAIL.message()));
                    return;
                }
                Object userInfoJson = stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token);
                if (userInfoJson == null) {
                    returnJson((HttpServletResponse) servletResponse,
                            Results.failure(USER_TOKEN_FAIL.code(), USER_TOKEN_FAIL.message()));
                    return;
                }
                UserContext.setUser(JSONUtil.toBean(userInfoJson.toString(), UserInfoDTO.class));
            }

            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }

    /**
     * 只放行指定方法的公开接口，注册和修改用户共用路径。
     */
    private boolean isIgnore(String requestURI, String method) {
        return ("POST".equals(method) && "/api/short-link/admin/v1/user/login".equals(requestURI))
                || ("GET".equals(method) && "/api/short-link/admin/v1/user/has-username".equals(requestURI))
                || ("POST".equals(method) && "/api/short-link/admin/v1/user".equals(requestURI));
    }

    private void returnJson(HttpServletResponse response, Result<Void> result) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(result));
    }
}
