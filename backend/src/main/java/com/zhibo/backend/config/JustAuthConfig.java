package com.zhibo.backend.config;

import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthQqRequest;
import me.zhyd.oauth.request.AuthWeChatOpenRequest;
import me.zhyd.oauth.request.AuthWeiboRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JustAuthConfig {

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

    @Bean
    public AuthGithubRequest authGithubRequest() {
        return new AuthGithubRequest(AuthConfig.builder()
                .clientId(githubClientId)
                .clientSecret(githubClientSecret)
                .redirectUri(githubRedirectUri)
                .build());
    }

    @Bean
    public AuthWeChatOpenRequest authWeChatOpenRequest() {
        return new AuthWeChatOpenRequest(AuthConfig.builder()
                .clientId(wechatClientId)
                .clientSecret(wechatClientSecret)
                .redirectUri(wechatRedirectUri)
                .build());
    }

    @Bean
    public AuthQqRequest authQqRequest() {
        return new AuthQqRequest(AuthConfig.builder()
                .clientId(qqClientId)
                .clientSecret(qqClientSecret)
                .redirectUri(qqRedirectUri)
                .build());
    }

    @Bean
    public AuthWeiboRequest authWeiboRequest() {
        return new AuthWeiboRequest(AuthConfig.builder()
                .clientId(weiboClientId)
                .clientSecret(weiboClientSecret)
                .redirectUri(weiboRedirectUri)
                .build());
    }
}
