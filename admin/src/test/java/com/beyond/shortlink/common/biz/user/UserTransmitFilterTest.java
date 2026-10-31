package com.beyond.shortlink.common.biz.user;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static com.beyond.shortlink.common.constant.RedisCacheConstant.USER_LOGIN_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserTransmitFilterTest {

    private StringRedisTemplate redisTemplate;
    private HashOperations<String, Object, Object> hashOperations;
    private UserTransmitFilter filter;
    private FilterChain filterChain;
    private MockHttpServletResponse response;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        hashOperations = mock(HashOperations.class);
        filter = new UserTransmitFilter(redisTemplate);
        filterChain = mock(FilterChain.class);
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void cleanUp() {
        UserContext.removeUser();
    }

    @ParameterizedTest
    @CsvSource({
            "POST, /api/short-link/admin/v1/user/login",
            "GET, /api/short-link/admin/v1/user/has-username",
            "POST, /api/short-link/admin/v1/user"
    })
    void publicEndpointsDoNotRequireCredentials(String method, String uri) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(redisTemplate);
        assertNull(UserContext.getUsername());
    }

    @ParameterizedTest
    @CsvSource({
            "GET, /api/short-link/admin/v1/group",
            "POST, /api/short-link/admin/v1/group",
            "PUT, /api/short-link/admin/v1/user",
            "GET, /api/short-link/admin/v1/user/login",
            "POST, /api/short-link/admin/v1/user/has-username",
            "GET, /api/short-link/v1/user/has-username",
            "POST, /api/short-link/admin/v1/user/login/extra"
    })
    void protectedEndpointsRejectMissingCredentials(String method, String uri) throws Exception {
        filter.doFilter(new MockHttpServletRequest(method, uri), response, filterChain);
        assertAuthenticationFailure();
        verifyNoInteractions(redisTemplate);
    }

    @ParameterizedTest
    @CsvSource({"alice, ''", "'', token", "'   ', token", "alice, '   '"})
    void incompleteCredentialsAreRejectedBeforeRedisLookup(String username, String token) throws Exception {
        MockHttpServletRequest request = authenticatedRequest(username, token);
        filter.doFilter(request, response, filterChain);
        assertAuthenticationFailure();
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void invalidOrExpiredTokenIsRejected() throws Exception {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        filter.doFilter(authenticatedRequest("alice", "invalid-token"), response, filterChain);
        verify(hashOperations).get(USER_LOGIN_KEY + "alice", "invalid-token");
        assertAuthenticationFailure();
    }

    @Test
    void validTokenPopulatesContextAndClearsItAfterRequest() throws Exception {
        mockLoginSession();
        filter.doFilter(authenticatedRequest("alice", "valid-token"), response, (request, result) -> {
            assertEquals("alice", UserContext.getUsername());
            assertEquals(123L, UserContext.getUserId());
        });
        assertNull(UserContext.getUsername());
        assertNull(UserContext.getUserId());
    }

    @Test
    void downstreamExceptionAlsoClearsContext() {
        mockLoginSession();
        assertThrows(ServletException.class,
                () -> filter.doFilter(authenticatedRequest("alice", "valid-token"), response, (request, result) -> {
                    assertEquals("alice", UserContext.getUsername());
                    throw new ServletException("模拟下游异常");
                }));
        assertNull(UserContext.getUsername());
        assertNull(UserContext.getUserId());
    }

    private void mockLoginSession() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get(USER_LOGIN_KEY + "alice", "valid-token"))
                .thenReturn("{\"id\":123,\"username\":\"alice\",\"realName\":\"测试用户\"}");
    }

    private MockHttpServletRequest authenticatedRequest(String username, String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/short-link/admin/v1/group");
        request.addHeader("username", username);
        request.addHeader("token", token);
        return request;
    }

    private void assertAuthenticationFailure() throws Exception {
        assertEquals(200, response.getStatus());
        assertEquals("UTF-8", response.getCharacterEncoding());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        JSONObject result = JSONUtil.parseObj(response.getContentAsString());
        assertEquals("A000200", result.getStr("code"));
        assertEquals("用户Token验证失败", result.getStr("message"));
        verifyNoInteractions(filterChain);
        assertNull(UserContext.getUsername());
    }
}
