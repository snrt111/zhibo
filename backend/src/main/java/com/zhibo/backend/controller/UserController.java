package com.zhibo.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhibo.backend.common.Result;
import com.zhibo.backend.entity.User;
import com.zhibo.backend.exception.BusinessException;
import com.zhibo.backend.service.EmailService;
import com.zhibo.backend.service.OAuth2Service;
import com.zhibo.backend.service.SmsService;
import com.zhibo.backend.service.UserService;
import com.zhibo.backend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户管理控制器
 * 提供用户注册、登录、信息管理等功能
 * 支持手机号验证码登录和OAuth2第三方登录
 *
 * @author zhibo
 * @since 2026-02-01
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SmsService smsService;

    @Autowired
    private OAuth2Service oAuth2Service;

    @Autowired
    private EmailService emailService;

    /**
     * 测试接口
     *
     * @return 测试字符串
     */
    @GetMapping("/hello")
    public String hello() {
        return "hello...";
    }

    /**
     * 用户注册
     * 创建新用户账户
     *
     * @param user 用户信息（用户名、密码、昵称等）
     * @return 注册成功的用户信息
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        logger.info("User registration request, username: {}", user.getUsername());
        User registeredUser = userService.register(user);
        logger.info("User registered successfully, username: {}", user.getUsername());
        return Result.success("注册成功", registeredUser);
    }

    /**
     * 用户登录
     * 验证用户名和密码，返回JWT Token
     *
     * @param loginRequest 包含username和password的请求体
     * @return JWT Token和用户信息
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        logger.info("User login request, username: {}", username);

        Optional<User> userOptional = userService.login(username, password);
        if (userOptional.isEmpty()) {
            logger.warn("User login failed, username: {}, reason: invalid credentials", username);
            throw new BusinessException(401, "用户名或密码错误");
        }

        User user = userOptional.get();
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());
        logger.info("User logged in successfully, username: {}", username);

        Map<String, Object> data = Map.of(
                "token", token,
                "user", user
        );
        return Result.success("登录成功", data);
    }

    /**
     * 获取当前用户信息
     * 根据Token获取登录用户的详细信息
     *
     * @param authorization 授权Token
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        logger.debug("Getting user info, userId: {}", userId);

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return Result.success("获取成功", user);
    }

    /**
     * 更新当前用户信息
     * 修改登录用户的基本信息
     *
     * @param authorization 授权Token
     * @param user 需要更新的用户信息
     * @return 更新后的用户信息
     */
    @PutMapping("/update")
    public Result<User> updateUserInfo(@RequestHeader("Authorization") String authorization, @RequestBody User user) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        logger.info("Updating user info, userId: {}", userId);

        user.setId(userId);
        boolean success = userService.updateUserInfo(user);
        if (!success) {
            throw new BusinessException("更新失败");
        }
        logger.info("User info updated successfully, userId: {}", userId);
        return Result.success("更新成功", userService.getById(userId));
    }

    /**
     * 重置密码
     * 当前登录用户修改自己的密码
     *
     * @param authorization 授权Token
     * @param resetRequest 包含newPassword的请求体
     * @return 操作结果
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestHeader("Authorization") String authorization, @RequestBody Map<String, String> resetRequest) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        logger.info("Resetting password, userId: {}", userId);

        String newPassword = resetRequest.get("newPassword");
        boolean success = userService.resetPassword(userId, newPassword);
        if (!success) {
            throw new BusinessException("密码重置失败");
        }
        logger.info("Password reset successfully, userId: {}", userId);
        return Result.success("密码重置成功", null);
    }

    /**
     * 获取用户列表
     * 仅管理员可访问
     *
     * @param authorization 授权Token
     * @return 用户列表
     */
    @GetMapping("/list")
    public Result<List<User>> getUserList(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            logger.warn("Access denied for getUserList, userType: {}", userType);
            throw new BusinessException(403, "无权访问");
        }
        logger.debug("Getting user list");
        List<User> userList = userService.list();
        return Result.success("获取用户列表成功", userList);
    }

    /**
     * 获取用户列表（分页）
     * 仅管理员可访问
     *
     * @param authorization 授权Token
     * @param status 用户状态
     * @param userType 用户类型
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页数量
     * @return 用户列表
     */
    @GetMapping("/admin/list")
    public Result<Page<User>> getAdminUserList(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        String token = authorization.replace("Bearer ", "");
        Integer currentUserType = jwtUtil.getUserTypeFromToken(token);
        if (currentUserType != 1) {
            logger.warn("Access denied for getAdminUserList, userType: {}", currentUserType);
            throw new BusinessException(403, "无权访问");
        }
        logger.debug("Getting admin user list, page: {}, size: {}", page, size);
        Page<User> result = userService.getAdminUserList(status, userType, keyword, page, size);
        return Result.success("获取用户列表成功", result);
    }

    /**
     * 删除用户
     * 仅管理员可操作
     *
     * @param authorization 授权Token
     * @param userId 要删除的用户ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{userId}")
    public Result<Void> deleteUser(@RequestHeader("Authorization") String authorization, @PathVariable Long userId) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            logger.warn("Access denied for deleteUser, operator userType: {}, target userId: {}", userType, userId);
            throw new BusinessException(403, "无权访问");
        }
        logger.info("Deleting user, userId: {}", userId);
        boolean success = userService.removeById(userId);
        if (!success) {
            throw new BusinessException("删除用户失败");
        }
        logger.info("User deleted successfully, userId: {}", userId);
        return Result.success("删除用户成功", null);
    }

    /**
     * 更新用户状态
     * 仅管理员可操作，用于禁用或启用用户账户
     *
     * @param authorization 授权Token
     * @param userId 目标用户ID
     * @param request 包含status的请求体
     * @return 操作结果
     */
    @PutMapping("/update-status/{userId}")
    public Result<Void> updateUserStatus(@RequestHeader("Authorization") String authorization, @PathVariable Long userId, @RequestBody Map<String, Integer> request) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            logger.warn("Access denied for updateUserStatus, operator userType: {}, target userId: {}", userType, userId);
            throw new BusinessException(403, "无权访问");
        }
        logger.info("Updating user status, userId: {}, new status: {}", userId, request.get("status"));
        Integer status = request.get("status");
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(status);
        boolean success = userService.updateById(user);
        if (!success) {
            throw new BusinessException("更新用户状态失败");
        }
        logger.info("User status updated successfully, userId: {}, new status: {}", userId, status);
        return Result.success("更新用户状态成功", null);
    }

    /**
     * 更新用户头像
     *
     * @param authorization 授权Token
     * @param request 包含avatar头像URL的请求体
     * @return 更新后的用户信息
     */
    @PostMapping("/update-avatar")
    public Result<User> updateAvatar(@RequestHeader("Authorization") String authorization, @RequestBody Map<String, String> request) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        String avatarUrl = request.get("avatar");
        logger.info("Updating avatar, userId: {}, avatarUrl: {}", userId, avatarUrl);

        boolean success = userService.updateAvatar(userId, avatarUrl);
        if (!success) {
            throw new BusinessException("头像更新失败");
        }
        logger.info("Avatar updated successfully, userId: {}", userId);
        return Result.success("头像更新成功", userService.getById(userId));
    }

    /**
     * 更新用户个人资料
     * 包括昵称、邮箱、手机号、性别、头像等
     *
     * @param authorization 授权Token
     * @param request 包含个人资料字段的请求体
     * @return 更新后的用户信息
     */
    @PostMapping("/update-profile")
    public Result<User> updateProfile(@RequestHeader("Authorization") String authorization, @RequestBody Map<String, Object> request) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        logger.info("Updating profile, userId: {}", userId);

        String nickname = (String) request.get("nickname");
        String email = (String) request.get("email");
        String phone = (String) request.get("phone");
        String avatar = (String) request.get("avatar");
        Integer gender = request.get("gender") != null ? (Integer) request.get("gender") : null;
        boolean success = userService.updateProfile(userId, nickname, email, phone, gender, avatar);
        if (!success) {
            throw new BusinessException("个人信息更新失败");
        }
        logger.info("Profile updated successfully, userId: {}", userId);
        return Result.success("个人信息更新成功", userService.getById(userId));
    }

    /**
     * 修改密码
     * 需要验证原密码
     *
     * @param authorization 授权Token
     * @param request 包含oldPassword和newPassword的请求体
     * @return 操作结果
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestHeader("Authorization") String authorization, @RequestBody Map<String, String> request) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        logger.info("Changing password, userId: {}", userId);

        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        userService.changePassword(userId, oldPassword, newPassword);
        logger.info("Password changed successfully, userId: {}", userId);
        return Result.success("密码修改成功", null);
    }

    /**
     * 根据ID获取用户信息
     * 公开接口，返回用户公开信息（隐藏密码）
     *
     * @param userId 用户ID
     * @return 用户公开信息
     */
    @GetMapping("/{userId}")
    public Result<User> getUserById(@PathVariable Long userId) {
        logger.debug("Getting user by id: {}", userId);
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setPassword(null);
        return Result.success("获取成功", user);
    }

    /**
     * 发送短信验证码
     * 用于手机号登录或注册
     *
     * @param request 包含phone的请求体
     * @return 操作结果
     */
    @PostMapping("/send-sms-code")
    public Result<Void> sendSmsCode(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        if (phone == null || phone.isEmpty()) {
            throw new BusinessException("手机号不能为空");
        }
        logger.info("Sending SMS code, phone: {}", phone);
        boolean success = smsService.sendSmsCode(phone);
        if (!success) {
            throw new BusinessException(500, "验证码发送失败");
        }
        logger.info("SMS code sent successfully, phone: {}", phone);
        return Result.success("验证码发送成功", null);
    }

    /**
     * 手机号验证码登录
     * 使用手机号和验证码进行登录
     *
     * @param request 包含phone和code的请求体
     * @return JWT Token和用户信息
     */
    @PostMapping("/login-with-phone")
    public Result<Map<String, Object>> loginWithPhone(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String code = request.get("code");
        if (phone == null || phone.isEmpty() || code == null || code.isEmpty()) {
            throw new BusinessException("手机号和验证码不能为空");
        }
        logger.info("Login with phone, phone: {}", phone);

        if (!smsService.verifyCode(phone, code)) {
            logger.warn("Phone login failed, phone: {}, reason: invalid code", phone);
            throw new BusinessException(401, "验证码错误或已过期");
        }
        User user = userService.loginWithPhone(phone, code);
        if (user == null) {
            throw new BusinessException(500, "用户登录失败，请重试");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());
        logger.info("Phone login successful, phone: {}", phone);

        Map<String, Object> data = Map.of(
                "token", token,
                "user", user
        );
        return Result.success("登录成功", data);
    }

    /**
     * 邮箱验证码登录
     * 使用邮箱和验证码进行登录
     *
     * @param request 包含email和code的请求体
     * @return JWT Token和用户信息
     */
    @PostMapping("/login-with-email")
    public Result<Map<String, Object>> loginWithEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        if (email == null || email.isEmpty() || code == null || code.isEmpty()) {
            throw new BusinessException("邮箱和验证码不能为空");
        }
        logger.info("Login with email, email: {}", email);

        if (!emailService.verifyCode(email, code)) {
            logger.warn("Email login failed, email: {}, reason: invalid code", email);
            throw new BusinessException(401, "验证码错误或已过期");
        }
        User user = userService.loginWithEmail(email, code);
        if (user == null) {
            throw new BusinessException(500, "用户登录失败，请重试");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());
        logger.info("Email login successful, email: {}", email);

        Map<String, Object> data = Map.of(
                "token", token,
                "user", user
        );
        return Result.success("登录成功", data);
    }

    /**
     * 手机号验证码注册
     * 使用手机号和验证码进行注册
     *
     * @param request 包含phone、code、password、nickname的请求体
     * @return JWT Token和用户信息
     */
    @PostMapping("/register-with-phone")
    public Result<Map<String, Object>> registerWithPhone(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String code = request.get("code");
        String password = request.get("password");
        String nickname = request.get("nickname");

        if (phone == null || phone.isEmpty() || code == null || code.isEmpty() || password == null || password.isEmpty()) {
            throw new BusinessException("手机号、验证码和密码不能为空");
        }

        if (password.length() < 6) {
            throw new BusinessException("密码至少6个字符");
        }

        logger.info("Register with phone, phone: {}", phone);

        if (!smsService.verifyCode(phone, code)) {
            logger.warn("Phone register failed, phone: {}, reason: invalid code", phone);
            throw new BusinessException(401, "验证码错误或已过期");
        }

        User user = userService.registerWithPhone(phone, password, nickname);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());
        logger.info("Phone register successful, phone: {}", phone);

        Map<String, Object> data = Map.of(
                "token", token,
                "user", user
        );
        return Result.success("注册成功", data);
    }

    /**
     * 发送邮箱验证码
     * 用于邮箱注册或登录
     *
     * @param request 包含email的请求体
     * @return 操作结果
     */
    @PostMapping("/send-email-code")
    public Result<Void> sendEmailCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            throw new BusinessException("邮箱不能为空");
        }
        logger.info("Sending email code, email: {}", email);
        boolean success = emailService.sendEmailCode(email);
        if (!success) {
            throw new BusinessException(500, "验证码发送失败");
        }
        logger.info("Email code sent successfully, email: {}", email);
        return Result.success("验证码发送成功", null);
    }

    /**
     * 邮箱验证码注册
     * 使用邮箱和验证码进行注册
     *
     * @param request 包含email、code、password、nickname的请求体
     * @return JWT Token和用户信息
     */
    @PostMapping("/register-with-email")
    public Result<Map<String, Object>> registerWithEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String password = request.get("password");
        String nickname = request.get("nickname");

        if (email == null || email.isEmpty() || code == null || code.isEmpty() || password == null || password.isEmpty()) {
            throw new BusinessException("邮箱、验证码和密码不能为空");
        }

        if (password.length() < 6) {
            throw new BusinessException("密码至少6个字符");
        }

        logger.info("Register with email, email: {}", email);

        if (!emailService.verifyCode(email, code)) {
            logger.warn("Email register failed, email: {}, reason: invalid code", email);
            throw new BusinessException(401, "验证码错误或已过期");
        }

        User user = userService.registerWithEmail(email, password, nickname);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());
        logger.info("Email register successful, email: {}", email);

        Map<String, Object> data = Map.of(
                "token", token,
                "user", user
        );
        return Result.success("注册成功", data);
    }

    /**
     * 获取OAuth2授权URL
     * 获取第三方登录的授权地址
     *
     * @param provider 第三方登录提供者（github、wechat、qq、weibo）
     * @return 授权URL
     */
    @GetMapping("/oauth/authorize/{provider}")
    public Result<String> getOAuth2AuthorizeUrl(@PathVariable String provider) {
        logger.info("Get OAuth2 authorize url, provider: {}", provider);
        String authorizeUrl = oAuth2Service.getAuthorizationUrl(provider);
        return Result.success("获取授权URL成功", authorizeUrl);
    }

    /**
     * OAuth2登录回调处理
     * 处理第三方登录的回调，完成用户登录
     *
     * @param provider 第三方登录提供者
     * @param code 授权码
     * @param state 状态参数
     * @return JWT Token和用户信息
     */
    @GetMapping("/oauth/callback/{provider}")
    public Result<Map<String, Object>> oauth2Callback(
            @PathVariable String provider,
            @RequestParam String code,
            @RequestParam(required = false) String state) {
        logger.info("OAuth2 callback, provider: {}, code: {}", provider, code);
        User user = oAuth2Service.loginWithOAuth2(provider, code, state);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());
        logger.info("OAuth2 login successful, provider: {}, userId: {}", provider, user.getId());

        Map<String, Object> data = Map.of(
                "token", token,
                "user", user
        );
        return Result.success("登录成功", data);
    }

    /**
     * 绑定手机号
     * 当前登录用户绑定手机号，需要验证码验证
     *
     * @param authorization 授权Token
     * @param request 包含phone和code的请求体
     * @return 操作结果
     */
    @PostMapping("/bind-phone")
    public Result<Void> bindPhone(@RequestHeader("Authorization") String authorization, @RequestBody Map<String, String> request) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        String phone = request.get("phone");
        String code = request.get("code");

        if (phone == null || phone.isEmpty() || code == null || code.isEmpty()) {
            throw new BusinessException("手机号和验证码不能为空");
        }

        logger.info("Binding phone, userId: {}, phone: {}", userId, phone);

        if (!smsService.verifyCode(phone, code)) {
            logger.warn("Bind phone failed, userId: {}, phone: {}, reason: invalid code", userId, phone);
            throw new BusinessException(401, "验证码错误或已过期");
        }

        boolean success = userService.bindPhone(userId, phone);
        if (!success) {
            throw new BusinessException("手机号绑定失败");
        }

        logger.info("Phone bound successfully, userId: {}, phone: {}", userId, phone);
        return Result.success("手机号绑定成功", null);
    }

    /**
     * 绑定邮箱
     * 当前登录用户绑定邮箱，需要验证码验证
     *
     * @param authorization 授权Token
     * @param request 包含email和code的请求体
     * @return 操作结果
     */
    @PostMapping("/bind-email")
    public Result<Void> bindEmail(@RequestHeader("Authorization") String authorization, @RequestBody Map<String, String> request) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        String email = request.get("email");
        String code = request.get("code");

        if (email == null || email.isEmpty() || code == null || code.isEmpty()) {
            throw new BusinessException("邮箱和验证码不能为空");
        }

        logger.info("Binding email, userId: {}, email: {}", userId, email);

        if (!emailService.verifyCode(email, code)) {
            logger.warn("Bind email failed, userId: {}, email: {}, reason: invalid code", userId, email);
            throw new BusinessException(401, "验证码错误或已过期");
        }

        boolean success = userService.bindEmail(userId, email);
        if (!success) {
            throw new BusinessException("邮箱绑定失败");
        }

        logger.info("Email bound successfully, userId: {}, email: {}", userId, email);
        return Result.success("邮箱绑定成功", null);
    }

    /**
     * 解绑手机号
     * 当前登录用户解绑手机号
     *
     * @param authorization 授权Token
     * @return 操作结果
     */
    @PostMapping("/unbind-phone")
    public Result<Void> unbindPhone(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        logger.info("Unbinding phone, userId: {}", userId);

        boolean success = userService.unbindPhone(userId);
        if (!success) {
            throw new BusinessException("手机号解绑失败");
        }

        logger.info("Phone unbound successfully, userId: {}", userId);
        return Result.success("手机号解绑成功", null);
    }

    /**
     * 解绑邮箱
     * 当前登录用户解绑邮箱
     *
     * @param authorization 授权Token
     * @return 操作结果
     */
    @PostMapping("/unbind-email")
    public Result<Void> unbindEmail(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        logger.info("Unbinding email, userId: {}", userId);

        boolean success = userService.unbindEmail(userId);
        if (!success) {
            throw new BusinessException("邮箱解绑失败");
        }

        logger.info("Email unbound successfully, userId: {}", userId);
        return Result.success("邮箱解绑成功", null);
    }

    /**
     * 获取账号安全信息
     * 返回当前用户的账号安全状态
     *
     * @param authorization 授权Token
     * @return 账号安全信息
     */
    @GetMapping("/security-info")
    public Result<Map<String, Object>> getSecurityInfo(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        logger.debug("Getting security info, userId: {}", userId);

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        Map<String, Object> securityInfo = Map.of(
                "hasPassword", user.getPassword() != null && !user.getPassword().isEmpty(),
                "hasPhone", user.getPhone() != null && !user.getPhone().isEmpty(),
                "hasEmail", user.getEmail() != null && !user.getEmail().isEmpty(),
                "phone", maskPhone(user.getPhone()),
                "email", maskEmail(user.getEmail())
        );

        return Result.success("获取成功", securityInfo);
    }

    /**
     * 手机号脱敏处理
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return null;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 邮箱脱敏处理
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return null;
        }
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];
        if (local.length() <= 2) {
            return "**@" + domain;
        }
        return local.substring(0, 2) + "***@" + domain;
    }
}
