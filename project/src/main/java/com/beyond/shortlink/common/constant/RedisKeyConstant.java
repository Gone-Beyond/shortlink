package com.beyond.shortlink.common.constant;

/**
 * Redis Key 常量类
 */
public class RedisKeyConstant {

    /**
     * 短链接跳转缓存 key，后面拼 fullShortUrl
     */
    public static final String GOTO_SHORT_LINK_KEY = "short-link:goto:%s";

    /**
     * 短链接跳转分布式锁 key，后面拼 fullShortUrl
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "short-link:lock:goto:%s";

    /**
     * 短链接跳转空值缓存 key，后面拼 fullShortUrl
     */
    public static final String IS_NULL_GOTO_SHORT_LINK_KEY = "short-link:is-null:goto:%s";

    /**
     * 短链接统计判断是否新用户缓存标识，后面拼 fullShortUrl
     */
    public static final String SHORT_LINK_STATS_UV_KEY = "short-link:stats:uv:";

    /**
     * 短链接统计判断是否新 IP 缓存标识，后面拼 fullShortUrl
     */
    public static final String SHORT_LINK_STATS_UIP_KEY = "short-link:stats:uip:";
}
