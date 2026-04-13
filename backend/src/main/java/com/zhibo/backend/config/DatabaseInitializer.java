package com.zhibo.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
@Component
public class DatabaseInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    private boolean initialized = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!initialized) {
            try {
                // 检查数据库连接
                testConnection();
                // 执行初始化脚本
                executeInitScript();
                initialized = true;
                log.info("数据库初始化完成");
            } catch (Exception e) {
                log.error("数据库初始化失败: {}", e.getMessage());
            }
        }
    }

    private void testConnection() {
        try {
            jdbcTemplate.execute("SELECT 1");
            log.info("数据库连接正常");
        } catch (Exception e) {
            log.error("数据库连接失败: {}", e.getMessage());
            throw e;
        }
    }

    private void executeInitScript() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:sql/init.sql");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), "UTF-8"))) {
            StringBuilder script = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                script.append(line).append("\n");
            }
            
            // 分割SQL语句并执行
            String[] statements = script.toString().split(";\s*");
            for (String statement : statements) {
                statement = statement.trim();
                if (!statement.isEmpty() && !statement.startsWith("--")) {
                    try {
                        jdbcTemplate.execute(statement);
                        log.debug("执行SQL语句: {}", statement.substring(0, Math.min(50, statement.length())) + "...");
                    } catch (Exception e) {
                        log.warn("执行SQL语句失败: {}", e.getMessage());
                        // 继续执行其他语句
                    }
                }
            }
        }
    }
}