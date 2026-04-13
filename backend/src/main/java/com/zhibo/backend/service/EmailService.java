package com.zhibo.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    private static final String EMAIL_CODE_PREFIX = "email:code:";
    private static final String EMAIL_SEND_LIMIT_PREFIX = "email:limit:";
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int CODE_LENGTH = 6;
    private static final int SEND_LIMIT_SECONDS = 60;
    private static final int SEND_LIMIT_COUNT = 5;
    private static final int SEND_LIMIT_HOURS = 1;

    @PostConstruct
    public void init() {
        if (mailEnabled && mailSender != null) {
            logger.info("邮件服务已启用，发件人：{}", fromEmail);
        } else {
            logger.info("邮件服务未启用，使用模拟模式");
        }
    }

    public String generateCode(String email) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        String codeStr = code.toString();
        redisTemplate.opsForValue().set(EMAIL_CODE_PREFIX + email, codeStr, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return codeStr;
    }

    public boolean checkSendLimit(String email) {
        String limitKey = EMAIL_SEND_LIMIT_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(limitKey, 1);
        if (count != null && count == 1) {
            redisTemplate.expire(limitKey, SEND_LIMIT_HOURS, TimeUnit.HOURS);
        }
        if (count != null && count > SEND_LIMIT_COUNT) {
            return false;
        }
        String lastSendKey = EMAIL_SEND_LIMIT_PREFIX + email + ":last";
        String lastSendTime = redisTemplate.opsForValue().get(lastSendKey);
        if (lastSendTime != null) {
            long lastTime = Long.parseLong(lastSendTime);
            long currentTime = System.currentTimeMillis() / 1000;
            if (currentTime - lastTime < SEND_LIMIT_SECONDS) {
                return false;
            }
        }
        redisTemplate.opsForValue().set(lastSendKey, String.valueOf(System.currentTimeMillis() / 1000), SEND_LIMIT_SECONDS, TimeUnit.SECONDS);
        return true;
    }

    public boolean sendEmailCode(String email) {
        if (!checkSendLimit(email)) {
            logger.warn("邮箱 {} 发送邮件过于频繁", email);
            throw new RuntimeException("发送过于频繁，请稍后再试");
        }

        String code = generateCode(email);

        if (mailEnabled && mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(email);
                message.setSubject("直播平台 - 验证码");
                message.setText("您的验证码是：" + code + "，有效期5分钟，请勿泄露给他人。");
                mailSender.send(message);
                logger.info("邮件发送成功，邮箱：{}", email);
                return true;
            } catch (Exception e) {
                logger.error("邮件发送失败，邮箱：{}", email, e);
                throw new RuntimeException("邮件发送失败：" + e.getMessage());
            }
        } else {
            logger.info("模拟邮件发送 - 邮箱：{}，验证码：{}（开发环境，实际发送需要配置邮件服务）", email, code);
            return true;
        }
    }

    public boolean verifyCode(String email, String code) {
        String key = EMAIL_CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(key);
        logger.info("邮箱验证码校验 - 邮箱：{}，输入验证码：{}，存储验证码：{}", email, code, storedCode);
        if (storedCode != null && storedCode.equals(code)) {
            redisTemplate.delete(key);
            logger.info("邮箱验证码校验成功 - 邮箱：{}", email);
            return true;
        }
        logger.warn("邮箱验证码校验失败 - 邮箱：{}，输入验证码：{}，存储验证码：{}", email, code, storedCode);
        return false;
    }

    public boolean isCodeExpired(String email) {
        Long expire = redisTemplate.getExpire(EMAIL_CODE_PREFIX + email, TimeUnit.SECONDS);
        return expire == null || expire <= 0;
    }

    public long getRemainingSeconds(String email) {
        Long expire = redisTemplate.getExpire(EMAIL_CODE_PREFIX + email, TimeUnit.SECONDS);
        return expire != null ? expire : 0;
    }
}
