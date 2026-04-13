package com.zhibo.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/health",
                    "/actuator/**",
                    "/user/hello",
                    "/user/register",
                    "/user/login",
                    "/user/send-sms-code",
                    "/user/login-with-phone",
                    "/user/send-email-code",
                    "/user/login-with-email",
                    "/user/register-with-phone",
                    "/user/register-with-email",
                    "/user/oauth/**",
                    "/user/{userId}",
                    "/live/**",
                    "/category/**",
                    "/gift/**",
                    "/message/**",
                    "/ws/**",
                    "/upload/**",
                    "/uploads/**",
                    "/metrics/**"
                ).permitAll()
                .requestMatchers("/user/list", "/user/delete/**", "/user/update-status/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable());

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
