package com.beyond.shortlink.toolkit;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 短链接工具类
 */
public class LinkUtil {

    /**
     * 默认缓存时间：30 天，单位毫秒
     */
    private static final long DEFAULT_CACHE_VALID_TIME = Duration.ofDays(30).toMillis();

    /**
     * 计算短链接跳转缓存的有效时间，单位毫秒
     * 永久有效返回 30 天；有期限的取"剩余有效期"和 30 天里较小的那个
     *
     * @param validDate 短链接有效期，永久有效传 null
     */
    public static long getLinkCacheValidTime(LocalDateTime validDate) {
        if (validDate == null) {
            return DEFAULT_CACHE_VALID_TIME;
        }
        long remain = Duration.between(LocalDateTime.now(), validDate).toMillis();
        return Math.min(remain, DEFAULT_CACHE_VALID_TIME);
    }

    /**
     * 获取用户真实 IP
     *
     * @param request 请求
     * @return 用户真实 IP
     */
    public static String getActualIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
