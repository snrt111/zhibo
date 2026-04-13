package com.zhibo.backend.common;

import lombok.Data;

/**
 * 统一响应结果封装类
 * 用于封装所有API接口的返回数据
 * 提供统一的响应格式：code、message、data
 *
 * @param <T> 响应数据类型
 * @author zhibo
 * @since 2026-02-01
 */
@Data
public class Result<T> {

    /** 响应状态码（200表示成功，500表示失败） */
    private Integer code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /**
     * 返回成功结果（无数据）
     *
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    /**
     * 返回成功结果（带数据）
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 返回成功结果（自定义消息和数据）
     *
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 返回错误结果（默认错误码500）
     *
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 返回错误结果（自定义错误码）
     *
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
