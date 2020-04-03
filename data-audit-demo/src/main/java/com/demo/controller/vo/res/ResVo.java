package com.demo.controller.vo.res;

public class ResVo<T> {

    /**响应码*/
    private int errcode;

    /**错误信息*/
    private String errmsg;

    /**响应数据*/
    private T data;

    public ResVo(){}

    private ResVo(int code, String msg, T data){
        this.errcode = code;
        this.errmsg = msg;
        this.data =data;
    }

    public static <T> ResVo<T> ok(T data){
        return new ResVo(0, "成功", data);
    }

    public static <T> ResVo<T> fail(CodeMsg ec){
        return new ResVo(ec.getCode(), ec.getMsg(), null);
    }

    public static <T> ResVo<T> fail(CodeMsg ec, T data){
        return new ResVo(ec.getCode(), ec.getMsg(), data);
    }

    public int getErrcode() {
        return errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public T getData() {
        return data;
    }
}
