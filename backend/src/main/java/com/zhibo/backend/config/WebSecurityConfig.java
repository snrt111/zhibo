package com.zhibo.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护，因为我们使用的是JWT
            .csrf(csrf -> csrf.disable())
            // 配置请求授权
            .authorizeHttpRequests(authorize -> authorize
                // 允许所有请求的访问
                .anyRequest().permitAll()
            )
            // 禁用表单登录
            .formLogin(form -> form.disable())
            // 禁用注销功能
            .logout(logout -> logout.disable());

        return http.build();
    }
}
