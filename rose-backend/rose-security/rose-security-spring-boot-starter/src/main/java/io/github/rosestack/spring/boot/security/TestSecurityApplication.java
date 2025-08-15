package io.github.rosestack.spring.boot.security;

import io.github.rosestack.spring.boot.security.core.service.impl.MemoryUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 安全模块测试启动类
 *
 * <p>用于测试登录登出功能的简单 Spring Boot 应用</p>
 *
 * <h3>预置测试用户：</h3>
 * <ul>
 *   <li><strong>admin/admin123</strong> - 管理员账户</li>
 *   <li><strong>user/user123</strong> - 普通用户账户</li>
 *   <li><strong>test/test123</strong> - 测试账户</li>
 * </ul>
 *
 * <h3>测试接口：</h3>
 * <ul>
 *   <li><strong>POST /api/auth/login</strong> - 登录接口</li>
 *   <li><strong>POST /api/auth/logout</strong> - 登出接口</li>
 *   <li><strong>POST /api/auth/refresh</strong> - 刷新Token</li>
 *   <li><strong>GET /api/auth/me</strong> - 获取当前用户信息</li>
 *   <li><strong>GET /api/test/hello</strong> - 测试接口（需要认证）</li>
 *   <li><strong>GET /api/test/public</strong> - 公开测试接口</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * # 1. 登录
 * curl -X POST http://localhost:8080/api/auth/login \
 *   -H "Content-Type: application/json" \
 *   -d '{"username":"admin","password":"admin123"}'
 *
 * # 2. 使用返回的 Token 访问受保护的接口
 * curl -X GET http://localhost:8080/api/test/hello \
 *   -H "X-API-KEY: <your-access-token>"
 *
 * # 3. 登出
 * curl -X POST http://localhost:8080/api/auth/logout \
 *   -H "X-API-KEY: <your-access-token>"
 * }</pre>
 *
 * @author chensoul
 * @since 1.0.0
 */
@Slf4j
@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
public class TestSecurityApplication {
    @Bean
    public MemoryUserDetailsService inMemoryUserDetailsService(PasswordEncoder passwordEncoder) {
        return new MemoryUserDetailsService(passwordEncoder);
    }

    public static void main(String[] args) {
        log.info("=========================================");
        System.out.println("   Rose Security 测试应用启动中...");
        System.out.println("=========================================");
        System.out.println("预置测试用户：");
        System.out.println("  admin/admin123 (管理员)");
        System.out.println("  user/user123   (普通用户)");
        System.out.println("  test/test123   (测试用户)");
        System.out.println("=========================================");
        System.out.println("测试接口：");
        System.out.println("  POST /api/auth/login   - 登录");
        System.out.println("  POST /api/auth/logout  - 登出");
        System.out.println("  GET  /api/auth/me      - 用户信息");
        System.out.println("  GET  /api/test/hello   - 需要认证");
        System.out.println("  GET  /api/test/public  - 公开接口");
        System.out.println("=========================================");

        SpringApplication.run(TestSecurityApplication.class, args);
    }

    /**
     * 测试控制器
     */
    @RestController
    public static class TestController {

        /**
         * 受保护的测试接口 - 需要认证
         */
        @GetMapping("/api/test/hello")
        public String hello() {
            return "Hello, 认证用户! 当前时间: " + java.time.LocalDateTime.now();
        }

        /**
         * 公开测试接口 - 无需认证
         */
        @GetMapping("/api/test/public")
        public String publicEndpoint() {
            return "这是一个公开接口，无需认证即可访问! 当前时间: " + java.time.LocalDateTime.now();
        }

        /**
         * 应用状态检查
         */
        @GetMapping("/api/status")
        public String status() {
            return "Rose Security Test Application is running! 状态: 正常";
        }
    }
}
