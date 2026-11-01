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

    /**
     * 获取用户访问操作系统
     *
     * @param request 请求
     * @return 访问操作系统
     */
    public static String getOs(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "Unknown";
        }
        String lowerCaseUserAgent = userAgent.toLowerCase();
        if (lowerCaseUserAgent.contains("windows")) {
            return "Windows";
        } else if (lowerCaseUserAgent.contains("android")) {
            return "Android";
        } else if (lowerCaseUserAgent.contains("iphone") || lowerCaseUserAgent.contains("ipad")) {
            return "iOS";
        } else if (lowerCaseUserAgent.contains("mac")) {
            return "Mac OS";
        } else if (lowerCaseUserAgent.contains("linux")) {
            return "Linux";
        } else {
            return "Unknown";
        }
    }

    /**
     * 获取用户访问浏览器
     *
     * @param request 请求
     * @return 访问浏览器
     */
    public static String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "Unknown";
        }
        String lowerCaseUserAgent = userAgent.toLowerCase();
        if (lowerCaseUserAgent.contains("edg")) {
            return "Microsoft Edge";
        } else if (lowerCaseUserAgent.contains("opera") || lowerCaseUserAgent.contains("opr")) {
            return "Opera";
        } else if (lowerCaseUserAgent.contains("chrome")) {
            return "Google Chrome";
        } else if (lowerCaseUserAgent.contains("firefox")) {
            return "Mozilla Firefox";
        } else if (lowerCaseUserAgent.contains("safari")) {
            return "Apple Safari";
        } else if (lowerCaseUserAgent.contains("msie") || lowerCaseUserAgent.contains("trident")) {
            return "Internet Explorer";
        } else {
            return "Unknown";
        }
    }

    /**
     * 获取用户访问设备
     *
     * @param request 请求
     * @return 访问设备
     */
    public static String getDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.toLowerCase().contains("mobile")) {
            return "Mobile";
        }
        return "PC";
    }

    /**
     * 获取用户访问网络
     *
     * @param request 请求
     * @return 访问网络
     */
    public static String getNetwork(HttpServletRequest request) {
        String actualIp = getActualIp(request);
        return actualIp.startsWith("192.168.") || actualIp.startsWith("10.") ? "WIFI" : "Mobile";
    }
}
