package com.zhibo.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.entity.User;
import com.zhibo.backend.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     */
    public User register(User user) {
        // 检查用户名是否已存在
        if (existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        // 检查邮箱是否已存在
        if (user.getEmail() != null && existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        // 检查手机号是否已存在
        if (user.getPhone() != null && existsByPhone(user.getPhone())) {
            throw new RuntimeException("手机号已存在");
        }
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 设置默认值
        if (user.getUserType() == null) {
            user.setUserType(0); // 默认为普通用户
        }
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认状态为正常
        }
        if (user.getGender() == null) {
            user.setGender(0); // 默认性别为未知
        }
        // 保存用户
        save(user);
        return user;
    }

    /**
     * 用户登录
     */
    public Optional<User> login(String username, String password) {
        User user = getByUsername(username);
        if (user == null) {
            return Optional.empty();
        }
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    /**
     * 根据用户名获取用户
     */
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }

    /**
     * 根据邮箱获取用户
     */
    public User getByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return getOne(wrapper);
    }

    /**
     * 根据手机号获取用户
     */
    public User getByPhone(String phone) {
        logger.info("查询用户 - 手机号：{}", phone);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = getOne(wrapper);
        logger.info("查询用户结果 - 手机号：{}，用户：{}", phone, user != null ? user.getUsername() : "null");
        return user;
    }

    /**
     * 检查用户名是否已存在
     */
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return count(wrapper) > 0;
    }

    /**
     * 检查邮箱是否已存在
     */
    public boolean existsByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return count(wrapper) > 0;
    }

    /**
     * 检查手机号是否已存在
     */
    public boolean existsByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return count(wrapper) > 0;
    }

    /**
     * 根据openid和provider获取用户
     */
    public User getByOpenidAndProvider(String openid, String provider) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid)
               .eq(User::getProvider, provider);
        return getOne(wrapper);
    }

    /**
     * 手机号验证码登录
     */
    public User loginWithPhone(String phone, String code) {
        logger.info("手机号验证码登录 - 手机号：{}", phone);
        User user = getByPhone(phone);
        logger.info("查询用户结果 - 手机号：{}，用户：{}", phone, user != null ? user.getUsername() : "null");
        if (user == null) {
            logger.info("新用户注册 - 手机号：{}", phone);
            user = new User();
            user.setPhone(phone);
            user.setUsername("user_" + phone);
            user.setNickname("用户" + phone.substring(phone.length() - 4));
            user.setPassword("");
            user.setGender(0);
            user.setUserType(0);
            user.setStatus(1);
            try {
                logger.info("开始插入新用户 - 手机号：{}", phone);
                int inserted = baseMapper.insert(user);
                logger.info("使用baseMapper插入结果 - {}，手机号：{}，用户ID：{}", inserted, phone, user.getId());
                if (inserted > 0) {
                    if (user.getId() == null) {
                        logger.info("尝试通过用户名查询新注册用户");
                        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
                        wrapper.eq(User::getUsername, "user_" + phone);
                        user = getOne(wrapper);
                        logger.info("通过用户名查询新注册用户结果 - {}", user != null ? user.getUsername() : "null");
                    }
                } else {
                    logger.error("插入用户失败 - 手机号：{}", phone);
                    throw new RuntimeException("用户注册失败：插入返回0");
                }
            } catch (Exception e) {
                logger.error("保存用户异常 - 手机号：{}，异常：{}", phone, e.getMessage(), e);
                throw new RuntimeException("用户注册失败：" + e.getMessage());
            }
        }
        if (user == null) {
            logger.error("用户注册后查询失败 - 手机号：{}", phone);
            throw new RuntimeException("用户注册失败，请重试");
        }
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }
        logger.info("手机号验证码登录成功 - 用户：{}，ID：{}", user.getUsername(), user.getId());
        return user;
    }

    /**
     * 邮箱验证码登录
     * 如果邮箱未注册，自动创建新用户
     */
    public User loginWithEmail(String email, String code) {
        logger.info("开始邮箱验证码登录 - 邮箱：{}", email);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = getOne(wrapper);
        if (user == null) {
            logger.info("邮箱未注册，自动创建新用户 - 邮箱：{}", email);
            String emailPrefix = email.split("@")[0];
            user = new User();
            user.setEmail(email);
            user.setUsername("user_" + emailPrefix);
            user.setNickname("用户" + emailPrefix);
            user.setPassword("");
            user.setGender(0);
            user.setUserType(0);
            user.setStatus(1);
            try {
                logger.info("开始插入新用户 - 邮箱：{}", email);
                int inserted = baseMapper.insert(user);
                logger.info("使用baseMapper插入结果 - {}，邮箱：{}，用户ID：{}", inserted, email, user.getId());
                if (inserted > 0) {
                    if (user.getId() == null) {
                        logger.info("尝试通过用户名查询新注册用户");
                        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
                        userWrapper.eq(User::getUsername, "user_" + emailPrefix);
                        user = getOne(userWrapper);
                        logger.info("通过用户名查询新注册用户结果 - {}", user != null ? user.getUsername() : "null");
                    }
                } else {
                    logger.error("插入用户失败 - 邮箱：{}", email);
                    throw new RuntimeException("用户注册失败：插入返回0");
                }
            } catch (Exception e) {
                logger.error("保存用户异常 - 邮箱：{}，异常：{}", email, e.getMessage(), e);
                throw new RuntimeException("用户注册失败：" + e.getMessage());
            }
        }
        if (user == null) {
            logger.error("用户注册后查询失败 - 邮箱：{}", email);
            throw new RuntimeException("用户注册失败，请重试");
        }
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }
        logger.info("邮箱验证码登录成功 - 用户：{}，ID：{}", user.getUsername(), user.getId());
        return user;
    }

    /**
     * 更新用户信息
     */
    public boolean updateUserInfo(User user) {
        return updateById(user);
    }

    /**
     * 获取管理员用户列表（分页）
     */
    public Page<User> getAdminUserList(Integer status, Integer userType, String keyword, int page, int size) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        if (userType != null) {
            wrapper.eq(User::getUserType, userType);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword);
        }
        
        wrapper.orderByDesc(User::getCreatedAt);
        return page(pageParam, wrapper);
    }

    /**
     * 重置密码
     */
    public boolean resetPassword(Long userId, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    /**
     * 更新用户头像
     */
    public boolean updateAvatar(Long userId, String avatarUrl) {
        User user = getById(userId);
        if (user == null) {
            return false;
        }
        user.setAvatar(avatarUrl);
        return updateById(user);
    }

    /**
     * 更新用户基本信息
     */
    public boolean updateProfile(Long userId, String nickname, String email, String phone, Integer gender, String avatar) {
        User user = getById(userId);
        if (user == null) {
            return false;
        }
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (gender != null) {
            user.setGender(gender);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        return updateById(user);
    }

    /**
     * 验证密码
     */
    public boolean verifyPassword(Long userId, String password) {
        User user = getById(userId);
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * 修改密码
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    /**
     * 手机号验证码注册
     */
    @Transactional
    public User registerWithPhone(String phone, String password, String nickname) {
        // 检查手机号是否已存在
        if (existsByPhone(phone)) {
            throw new RuntimeException("手机号已注册");
        }

        User user = new User();
        user.setPhone(phone);
        user.setUsername("user_" + phone);
        user.setNickname(nickname != null && !nickname.isEmpty() ? nickname : "用户" + phone.substring(phone.length() - 4));
        user.setPassword(passwordEncoder.encode(password));
        user.setGender(0);
        user.setUserType(0);
        user.setStatus(1);

        save(user);
        return user;
    }

    /**
     * 邮箱验证码注册
     */
    @Transactional
    public User registerWithEmail(String email, String password, String nickname) {
        // 检查邮箱是否已存在
        if (existsByEmail(email)) {
            throw new RuntimeException("邮箱已注册");
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername("user_" + email.substring(0, email.indexOf('@')));
        user.setNickname(nickname != null && !nickname.isEmpty() ? nickname : "用户" + email.substring(0, 4));
        user.setPassword(passwordEncoder.encode(password));
        user.setGender(0);
        user.setUserType(0);
        user.setStatus(1);

        save(user);
        return user;
    }

    /**
     * 绑定手机号
     */
    public boolean bindPhone(Long userId, String phone) {
        // 检查手机号是否已被其他用户绑定
        User existingUser = lambdaQuery().eq(User::getPhone, phone).one();
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new RuntimeException("该手机号已被其他用户绑定");
        }

        User user = new User();
        user.setId(userId);
        user.setPhone(phone);
        return updateById(user);
    }

    /**
     * 绑定邮箱
     */
    public boolean bindEmail(Long userId, String email) {
        // 检查邮箱是否已被其他用户绑定
        User existingUser = lambdaQuery().eq(User::getEmail, email).one();
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new RuntimeException("该邮箱已被其他用户绑定");
        }

        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        return updateById(user);
    }

    /**
     * 解绑手机号
     */
    public boolean unbindPhone(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setPhone(null);
        return updateById(user);
    }

    /**
     * 解绑邮箱
     */
    public boolean unbindEmail(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail(null);
        return updateById(user);
    }
}