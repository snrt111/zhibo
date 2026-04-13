package com.zhibo.backend.service;

import com.zhibo.backend.entity.User;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthQqRequest;
import me.zhyd.oauth.request.AuthWeChatOpenRequest;
import me.zhyd.oauth.request.AuthWeiboRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OAuth2Service {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2Service.class);

    @Autowired
    private UserService userService;

    @Value("${justauth.type.GITHUB.client-id}")
    private String githubClientId;

    @Value("${justauth.type.GITHUB.client-secret}")
    private String githubClientSecret;

    @Value("${justauth.type.GITHUB.redirect-uri}")
    private String githubRedirectUri;

    @Value("${justauth.type.WECHAT_OPEN.client-id}")
    private String wechatClientId;

    @Value("${justauth.type.WECHAT_OPEN.client-secret}")
    private String wechatClientSecret;

    @Value("${justauth.type.WECHAT_OPEN.redirect-uri}")
    private String wechatRedirectUri;

    @Value("${justauth.type.QQ.client-id}")
    private String qqClientId;

    @Value("${justauth.type.QQ.client-secret}")
    private String qqClientSecret;

    @Value("${justauth.type.QQ.redirect-uri}")
    private String qqRedirectUri;

    @Value("${justauth.type.WEIBO.client-id}")
    private String weiboClientId;

    @Value("${justauth.type.WEIBO.client-secret}")
    private String weiboClientSecret;

    @Value("${justauth.type.WEIBO.redirect-uri}")
    private String weiboRedirectUri;

    @Value("${justauth.mock-mode:false}")
    private boolean mockMode;

    public String getAuthorizationUrl(String provider) {
        if (mockMode) {
            return "http://localhost:80/login?code=mock_code_" + provider + "&state=" + provider + "_" + System.currentTimeMillis() + "&provider=" + provider;
        }
        String state = provider + "_" + System.currentTimeMillis();
        switch (provider.toLowerCase()) {
            case "github":
                AuthGithubRequest githubRequest = new AuthGithubRequest(AuthConfig.builder()
                        .clientId(githubClientId)
                        .clientSecret(githubClientSecret)
                        .redirectUri(githubRedirectUri)
                        .build());
                return githubRequest.authorize(state);
            case "wechat":
                AuthWeChatOpenRequest wechatRequest = new AuthWeChatOpenRequest(AuthConfig.builder()
                        .clientId(wechatClientId)
                        .clientSecret(wechatClientSecret)
                        .redirectUri(wechatRedirectUri)
                        .build());
                return wechatRequest.authorize(state);
            case "qq":
                AuthQqRequest qqRequest = new AuthQqRequest(AuthConfig.builder()
                        .clientId(qqClientId)
                        .clientSecret(qqClientSecret)
                        .redirectUri(qqRedirectUri)
                        .build());
                return qqRequest.authorize(state);
            case "weibo":
                AuthWeiboRequest weiboRequest = new AuthWeiboRequest(AuthConfig.builder()
                        .clientId(weiboClientId)
                        .clientSecret(weiboClientSecret)
                        .redirectUri(weiboRedirectUri)
                        .build());
                return weiboRequest.authorize(state);
            default:
                throw new AuthException("不支持的登录方式: " + provider);
        }
    }

    public User loginWithOAuth2(String provider, String code, String state) {
        if (mockMode) {
            User mockUser = new User();
            mockUser.setOpenid(provider + "_mock_openid");
            mockUser.setUnionid(provider + "_mock_unionid");
            mockUser.setProvider(provider);
            mockUser.setNickname(provider + "模拟用户");
            mockUser.setAvatar("https://via.placeholder.com/100");
            mockUser.setUserType(0);
            mockUser.setStatus(1);
            mockUser.setUsername(provider + "_mock_user");
            mockUser.setPassword("");
            
            User existingUser = userService.getByOpenidAndProvider(mockUser.getOpenid(), provider);
            if (existingUser != null) {
                return existingUser;
            }
            userService.save(mockUser);
            return mockUser;
        }
        try {
            AuthUser authUser = getAuthUser(provider, code, state);
            if (authUser == null) {
                throw new AuthException("获取用户信息失败");
            }

            String openid = authUser.getUuid();
            String unionid = authUser.getSource() + "_" + openid;

            User existingUser = userService.getByOpenidAndProvider(openid, provider);
            if (existingUser != null) {
                logger.info("User login with OAuth2, provider: {}, openid: {}", provider, openid);
                return existingUser;
            }

            User newUser = new User();
            newUser.setOpenid(openid);
            newUser.setUnionid(unionid);
            newUser.setProvider(provider);
            newUser.setNickname(authUser.getNickname());
            newUser.setAvatar(authUser.getAvatar());
            newUser.setUserType(0);
            newUser.setStatus(1);
            newUser.setUsername(provider + "_" + openid);
            newUser.setPassword("");
            userService.save(newUser);
            logger.info("New user created via OAuth2, provider: {}, openid: {}", provider, openid);
            return newUser;
        } catch (Exception e) {
            logger.error("OAuth2 login failed, provider: {}", provider, e);
            throw new AuthException("OAuth2登录失败: " + e.getMessage());
        }
    }

    private AuthUser getAuthUser(String provider, String code, String state) {
        AuthCallback callback = AuthCallback.builder()
                .code(code)
                .state(state)
                .build();

        switch (provider.toLowerCase()) {
            case "github":
                AuthGithubRequest githubRequest = new AuthGithubRequest(AuthConfig.builder()
                        .clientId(githubClientId)
                        .clientSecret(githubClientSecret)
                        .redirectUri(githubRedirectUri)
                        .build());
                AuthResponse<AuthUser> githubResponse = githubRequest.login(callback);
                if (!githubResponse.ok()) {
                    throw new AuthException("GitHub登录失败: " + githubResponse.getMsg());
                }
                return githubResponse.getData();
            case "wechat":
                AuthWeChatOpenRequest wechatRequest = new AuthWeChatOpenRequest(AuthConfig.builder()
                        .clientId(wechatClientId)
                        .clientSecret(wechatClientSecret)
                        .redirectUri(wechatRedirectUri)
                        .build());
                AuthResponse<AuthUser> wechatResponse = wechatRequest.login(callback);
                if (!wechatResponse.ok()) {
                    throw new AuthException("微信登录失败: " + wechatResponse.getMsg());
                }
                return wechatResponse.getData();
            case "qq":
                AuthQqRequest qqRequest = new AuthQqRequest(AuthConfig.builder()
                        .clientId(qqClientId)
                        .clientSecret(qqClientSecret)
                        .redirectUri(qqRedirectUri)
                        .build());
                AuthResponse<AuthUser> qqResponse = qqRequest.login(callback);
                if (!qqResponse.ok()) {
                    throw new AuthException("QQ登录失败: " + qqResponse.getMsg());
                }
                return qqResponse.getData();
            case "weibo":
                AuthWeiboRequest weiboRequest = new AuthWeiboRequest(AuthConfig.builder()
                        .clientId(weiboClientId)
                        .clientSecret(weiboClientSecret)
                        .redirectUri(weiboRedirectUri)
                        .build());
                AuthResponse<AuthUser> weiboResponse = weiboRequest.login(callback);
                if (!weiboResponse.ok()) {
                    throw new AuthException("微博登录失败: " + weiboResponse.getMsg());
                }
                return weiboResponse.getData();
            default:
                throw new AuthException("不支持的登录方式: " + provider);
        }
    }
}