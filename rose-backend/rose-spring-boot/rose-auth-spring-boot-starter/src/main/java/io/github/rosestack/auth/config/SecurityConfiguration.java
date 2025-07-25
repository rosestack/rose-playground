package io.github.rosestack.auth.config;

import io.github.rosestack.auth.properties.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 配置类
 * <p>
 * 提供 Spring Security 的核心配置，包括安全过滤器链、CORS、密码编码器等。
 * 
 * @author chensoul
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    
    private final AuthProperties authProperties;
    
    /**
     * 配置安全过滤器链
     */
    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // 禁用 CSRF（前后端分离项目）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置会话管理为无状态
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 配置 CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 公开端点
                .requestMatchers(
                    "/api/auth/**",
                    "/actuator/health",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                
                // 管理端点需要管理员权限
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            
            // TODO: 添加 JWT 认证过滤器
            // .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            
            // TODO: 配置异常处理
            // .exceptionHandling(exceptions -> exceptions
            //     .authenticationEntryPoint(authenticationEntryPoint())
            //     .accessDeniedHandler(accessDeniedHandler())
            // )
            
            .build();
    }
    
    /**
     * 配置密码编码器
     */
    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    /**
     * 配置 CORS
     */
    @Bean
    @ConditionalOnMissingBean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        AuthProperties.Cors corsConfig = authProperties.getCors();
        
        // 允许的源
        if (corsConfig.getAllowedOrigins().length == 1 && "*".equals(corsConfig.getAllowedOrigins()[0])) {
            configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        } else {
            configuration.setAllowedOrigins(Arrays.asList(corsConfig.getAllowedOrigins()));
        }
        
        // 允许的方法
        configuration.setAllowedMethods(Arrays.asList(corsConfig.getAllowedMethods()));
        
        // 允许的头
        configuration.setAllowedHeaders(Arrays.asList(corsConfig.getAllowedHeaders()));
        
        // 是否允许凭证
        configuration.setAllowCredentials(corsConfig.isAllowCredentials());
        
        // 预检请求缓存时间
        configuration.setMaxAge(corsConfig.getMaxAge());
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
