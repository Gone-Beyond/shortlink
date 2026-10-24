package com.beyond.shortlink.common.biz.user;

/**
 * 用户上下文
 */
public final class UserContext {

    private static final ThreadLocal<UserInfoDTO> USER_THREAD_LOCAL = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUser(UserInfoDTO user) {
        USER_THREAD_LOCAL.set(user);
    }

    public static String getUsername() {
        UserInfoDTO user = USER_THREAD_LOCAL.get();
        return user == null ? null : user.getUsername();
    }

    public static Long getUserId() {
        UserInfoDTO user = USER_THREAD_LOCAL.get();
        return user == null ? null : user.getId();
    }

    public static void removeUser() {
        USER_THREAD_LOCAL.remove();
    }
}