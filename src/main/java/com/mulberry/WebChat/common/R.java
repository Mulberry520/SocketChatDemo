package com.mulberry.WebChat.common;

import lombok.Data;

@Data
public class R<T> {
    private Integer code;
    private String msg;
    private T data;

    private static <DATA> R<DATA> of(int code, String msg, DATA data) {
        R<DATA> r = new R<>();
        r.code = code;
        r.msg = msg;
        r.data = data;
        return r;
    }

    public static R<Void> success() {
        return R.of(CommonConst.SUCCESS_CODE, CommonConst.SUCCESS_MSG, null);
    }

    public static <DATA> R<DATA> success(DATA data) {
        return R.of(CommonConst.SUCCESS_CODE, CommonConst.SUCCESS_MSG, data);
    }

    public static <DATA> R<DATA> success(String msg, DATA data) {
        return R.of(CommonConst.SUCCESS_CODE, msg, data);
    }

    public static R<Void> error() {
        return R.of(CommonConst.ERROR_CODE, CommonConst.ERROR_MSG, null);
    }

    public static R<Void> error(String msg) {
        return R.of(CommonConst.ERROR_CODE, msg, null);
    }

    public static R<Void> error(Integer code, String msg) {
        return R.of(code, msg, null);
    }
}
