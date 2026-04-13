package com.zhibo.backend.service;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${sms.aliyun.enabled:false}")
    private boolean aliyunEnabled;

    @Value("${sms.aliyun.access-key-id}")
    private String accessKeyId;

    @Value("${sms.aliyun.access-key-secret}")
    private String accessKeySecret;

    @Value("${sms.aliyun.region-id}")
    private String regionId;

    @Value("${sms.aliyun.sign-name}")
    private String signName;

    @Value("${sms.aliyun.template-code}")
    private String templateCode;

    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String SMS_SEND_LIMIT_PREFIX = "sms:limit:";
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int CODE_LENGTH = 6;
    private static final int SEND_LIMIT_SECONDS = 60;
    private static final int SEND_LIMIT_COUNT = 5;
    private static final int SEND_LIMIT_HOURS = 1;

    private Client aliyunClient;

    @PostConstruct
    public void init() {
        if (aliyunEnabled) {
            try {
                Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret)
                    .setRegionId(regionId);
                config.endpoint = "dysmsapi.aliyuncs.com";
                aliyunClient = new Client(config);
                logger.info("阿里云短信客户端初始化成功");
            } catch (Exception e) {
                logger.error("阿里云短信客户端初始化失败", e);
            }
        } else {
            logger.info("阿里云短信未启用，使用模拟模式");
        }
    }

    public String generateCode(String phone) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        String codeStr = code.toString();
        redisTemplate.opsForValue().set(SMS_CODE_PREFIX + phone, codeStr, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return codeStr;
    }

    public boolean checkSendLimit(String phone) {
        String limitKey = SMS_SEND_LIMIT_PREFIX + phone;
        Long count = redisTemplate.opsForValue().increment(limitKey, 1);
        if (count != null && count == 1) {
            redisTemplate.expire(limitKey, SEND_LIMIT_HOURS, TimeUnit.HOURS);
        }
        if (count != null && count > SEND_LIMIT_COUNT) {
            return false;
        }
        String lastSendKey = SMS_SEND_LIMIT_PREFIX + phone + ":last";
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

    public boolean sendSmsCode(String phone) {
        if (!checkSendLimit(phone)) {
            logger.warn("手机号 {} 发送短信过于频繁", phone);
            throw new RuntimeException("发送过于频繁，请稍后再试");
        }

        String code = generateCode(phone);

        if (aliyunEnabled && aliyunClient != null) {
            try {
                SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam("{\"code\":\"" + code + "\"}");
                SendSmsResponse response = aliyunClient.sendSms(request);
                if ("OK".equals(response.getBody().getCode())) {
                    logger.info("阿里云短信发送成功，手机号：{}，Message：{}", phone, response.getBody().getMessage());
                    return true;
                } else {
                    logger.error("阿里云短信发送失败，手机号：{}，Code：{}，Message：{}", phone, response.getBody().getCode(), response.getBody().getMessage());
                    throw new RuntimeException("短信发送失败：" + response.getBody().getMessage());
                }
            } catch (Exception e) {
                logger.error("阿里云短信发送异常，手机号：{}", phone, e);
                throw new RuntimeException("短信发送失败：" + e.getMessage());
            }
        } else {
            logger.info("模拟短信发送 - 手机号：{}，验证码：{}（开发环境，实际发送需要启用阿里云短信）", phone, code);
            return true;
        }
    }

    public boolean verifyCode(String phone, String code) {
        String key = SMS_CODE_PREFIX + phone;
        String storedCode = redisTemplate.opsForValue().get(key);
        logger.info("验证码校验 - 手机号：{}，输入验证码：{}，存储验证码：{}", phone, code, storedCode);
        if (storedCode != null && storedCode.equals(code)) {
            redisTemplate.delete(key);
            logger.info("验证码校验成功 - 手机号：{}", phone);
            return true;
        }
        logger.warn("验证码校验失败 - 手机号：{}，输入验证码：{}，存储验证码：{}", phone, code, storedCode);
        return false;
    }

    public boolean isCodeExpired(String phone) {
        Long expire = redisTemplate.getExpire(SMS_CODE_PREFIX + phone, TimeUnit.SECONDS);
        return expire == null || expire <= 0;
    }

    public long getRemainingSeconds(String phone) {
        Long expire = redisTemplate.getExpire(SMS_CODE_PREFIX + phone, TimeUnit.SECONDS);
        return expire != null ? expire : 0;
    }
}