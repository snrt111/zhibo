package com.zhibo.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库user表，存储用户基本信息
 *
 * @author zhibo
 * @since 2026-02-01
 */
@Data
@TableName("user")
public class User {

    /** 用户ID（主键，自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（登录账号，唯一） */
    private String username;

    /** 密码（加密存储） */
    private String password;

    /** 昵称（显示名称） */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 头像URL */
    private String avatar;

    /** 性别（0-未知，1-男，2-女） */
    private Integer gender;

    /** 用户类型（0-普通用户，1-管理员） */
    private Integer userType;

    /** 账户状态（0-禁用，1-正常） */
    private Integer status;

    /** 微信OpenID（第三方登录） */
    private String openid;

    /** 微信UnionID（第三方登录） */
    private String unionid;

    /** 第三方登录提供者（如wechat） */
    private String provider;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
