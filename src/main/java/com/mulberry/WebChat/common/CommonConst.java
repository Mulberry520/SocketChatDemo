package com.mulberry.WebChat.common;

public class CommonConst {
    public static final String SUCCESS_MSG = "Operation success";
    public static final String ERROR_MSG = "Operation failed";

    public static final Integer SUCCESS_CODE = 200;
    public static final Integer ERROR_CODE = 400;
    public static final Integer TOKEN_EXPIRE = 401;

    public static String OAUTH_TOKEN = "Bearer ";
    public static Integer OAUTH_LENGTH = 7;


    public static final String USER_ROLE_PREFIX = "loginUser:";
    public static final String REDIS_REFRESH_PREFIX = "refreshToken:";
    public static final String REFRESH_COOKIE = "refreshToken";

    public static final String USER_ROLE = "ROLE_USER";
    public static final String ADMIN_ROLE = "ROLE_ADMIN";
    public static final String DEFAULT_ROLE = USER_ROLE;

    public static final String AVATAR_FOLDER_PREFIX = "user/avatar/";

    public static final String FRIENDSHIP_APPROVED = "approved";
    public static final String FRIENDSHIP_REJECTED = "rejected";
    public static final String FRIENDSHIP_BLOCKED = "blocked";
    public static final String FRIENDSHIP_UNVERIFIED = "unverified";

    public static final String STATUS_ONLINE = "ONLINE";
    public static final String STATUS_OFFLINE = "ONLINE";
    public static final String STATUS_BANNED = "ONLINE";
}