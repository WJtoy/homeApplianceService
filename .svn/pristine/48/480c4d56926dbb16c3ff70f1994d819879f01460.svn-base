package com.kkl.kklplus.golden.utils;

public class RestResultGenerators {


    /**
     * 自定义
     *
     * @param code
     * @param msg
     * @return
     */
    public static <T> RestResult<T> custom(Integer code, String msg) {
        RestResult res = RestResult.newInstance();
        res.setCode(code);
        res.setMsg(msg);
        return res;
    }

    /**
     * 自定义
     *
     * @param code
     * @param msg
     * @return
     */
    public static <T> RestResult<T> custom(Integer code, String msg, Object data) {
        RestResult res = RestResult.newInstance();
        res.setCode(code);
        res.setMsg(msg);
        res.setData(data);
        return res;
    }

}
