package io.github.rose.security;

import io.github.rose.security.auth.AuthExceptionHandler;
import io.github.rose.security.auth.exception.ThingsboardErrorResponseHandler;
import io.github.rose.security.auth.jwt.JwtTokenAuthenticationProcessingFilter;
import io.github.rose.security.auth.jwt.RefreshTokenAuthenticationProvider;
import io.github.rose.security.auth.jwt.RefreshTokenProcessingFilter;
import io.github.rose.security.auth.jwt.SkipPathRequestMatcher;
import io.github.rose.security.auth.jwt.extractor.TokenExtractor;
import io.github.rose.security.auth.rest.RestAuthenticationProvider;
import io.github.rose.security.auth.rest.RestLoginProcessingFilter;
import io.github.rose.security.auth.rest.RestPublicLoginProcessingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    public static final String JWT_TOKEN_HEADER_PARAM = "Authorization";
    public static final String JWT_TOKEN_QUERY_PARAM = "token";

    public static final String DEVICE_API_ENTRY_POINT = "/api/v1/**";
    public static final String FORM_BASED_LOGIN_ENTRY_POINT = "/api/auth/login";
    public static final String PUBLIC_LOGIN_ENTRY_POINT = "/api/auth/login/public";
    public static final String TOKEN_REFRESH_ENTRY_POINT = "/api/auth/token";
    public static final String[] NON_TOKEN_BASED_AUTH_ENTRY_POINTS = new String[]{"/index.html", "/api/noauth/**", "/startup-report", "/actuator/**"};
    public static final String TOKEN_BASED_AUTH_ENTRY_POINT = "/api/**";
    public static final String WS_ENTRY_POINT = "/api/ws/**";
    public static final String MAIL_OAUTH2_PROCESSING_ENTRY_POINT = "/api/system/mail/oauth2/code";

    @Autowired
    private ThingsboardErrorResponseHandler restAccessDeniedHandler;
    @Autowired
    @Qualifier("defaultAuthenticationSuccessHandler")
    private AuthenticationSuccessHandler successHandler;
    @Autowired
    @Qualifier("defaultAuthenticationFailureHandler")
    private AuthenticationFailureHandler failureHandler;
    @Autowired
    private RestAuthenticationProvider restAuthenticationProvider;
    @Autowired
    private JwtAuthenticationProvider jwtAuthenticationProvider;
    @Autowired
    private RefreshTokenAuthenticationProvider refreshTokenAuthenticationProvider;
    @Autowired
    @Qualifier("jwtHeaderTokenExtractor")
    private TokenExtractor jwtHeaderTokenExtractor;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private AuthExceptionHandler authExceptionHandler;

    //    @Autowired(required = false)
//    OAuth2Configuration oauth2Configuration;

//    @Autowired(required = false)
//    @Qualifier("oauth2AuthenticationSuccessHandler")
//    private AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
//
//    @Autowired(required = false)
//    @Qualifier("oauth2AuthenticationFailureHandler")
//    private AuthenticationFailureHandler oauth2AuthenticationFailureHandler;
//
//    @Autowired(required = false)
//    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    protected RestLoginProcessingFilter buildRestLoginProcessingFilter() throws Exception {
        RestLoginProcessingFilter filter = new RestLoginProcessingFilter(FORM_BASED_LOGIN_ENTRY_POINT, successHandler, failureHandler);
        filter.setAuthenticationManager(this.authenticationManager);
        return filter;
    }

    @Bean
    protected RestPublicLoginProcessingFilter buildRestPublicLoginProcessingFilter() throws Exception {
        RestPublicLoginProcessingFilter filter = new RestPublicLoginProcessingFilter(PUBLIC_LOGIN_ENTRY_POINT, successHandler, failureHandler);
        filter.setAuthenticationManager(this.authenticationManager);
        return filter;
    }

    protected JwtTokenAuthenticationProcessingFilter buildJwtTokenAuthenticationProcessingFilter() throws Exception {
        List<String> pathsToSkip = new ArrayList<>(Arrays.asList(NON_TOKEN_BASED_AUTH_ENTRY_POINTS));
        pathsToSkip.addAll(Arrays.asList(WS_ENTRY_POINT, TOKEN_REFRESH_ENTRY_POINT, FORM_BASED_LOGIN_ENTRY_POINT,
                PUBLIC_LOGIN_ENTRY_POINT, DEVICE_API_ENTRY_POINT, MAIL_OAUTH2_PROCESSING_ENTRY_POINT));
        SkipPathRequestMatcher matcher = new SkipPathRequestMatcher(pathsToSkip, TOKEN_BASED_AUTH_ENTRY_POINT);
        JwtTokenAuthenticationProcessingFilter filter
                = new JwtTokenAuthenticationProcessingFilter(failureHandler, jwtHeaderTokenExtractor, matcher);
        filter.setAuthenticationManager(this.authenticationManager);
        return filter;
    }

    @Bean
    protected RefreshTokenProcessingFilter buildRefreshTokenProcessingFilter() throws Exception {
        RefreshTokenProcessingFilter filter = new RefreshTokenProcessingFilter(TOKEN_REFRESH_ENTRY_POINT, successHandler, failureHandler);
        filter.setAuthenticationManager(this.authenticationManager);
        return filter;
    }

    @Bean
    public AuthenticationManager authenticationManager(ObjectPostProcessor<Object> objectPostProcessor) throws Exception {
        DefaultAuthenticationEventPublisher eventPublisher = objectPostProcessor
                .postProcess(new DefaultAuthenticationEventPublisher());
        AuthenticationManagerBuilder auth = new AuthenticationManagerBuilder(objectPostProcessor);
        auth.authenticationEventPublisher(eventPublisher);
        auth.authenticationProvider(restAuthenticationProvider);
        auth.authenticationProvider(jwtAuthenticationProvider);
        auth.authenticationProvider(refreshTokenAuthenticationProvider);
        return auth.build();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.headers(headers -> headers
                        .cacheControl(config -> {
                        })
                        .frameOptions(config -> {
                        }).disable())
                .cors(cors -> {
                })
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(config -> {
                })
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(config -> config
                        .requestMatchers(NON_TOKEN_BASED_AUTH_ENTRY_POINTS).permitAll() // static resources, user activation and password reset end-points (webjars included)
                        .requestMatchers(
                                FORM_BASED_LOGIN_ENTRY_POINT, // Login end-point
                                PUBLIC_LOGIN_ENTRY_POINT, // Public login end-point
                                TOKEN_REFRESH_ENTRY_POINT, // Token refresh end-point
                                MAIL_OAUTH2_PROCESSING_ENTRY_POINT, // Mail oauth2 code processing url
                                WS_ENTRY_POINT).permitAll() // Protected WebSocket API End-points
                        .requestMatchers(TOKEN_BASED_AUTH_ENTRY_POINT).authenticated() // Protected API End-points
                        .anyRequest().permitAll())
                .exceptionHandling(config -> config.accessDeniedHandler(restAccessDeniedHandler))
                .addFilterBefore(buildRestLoginProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(buildRestPublicLoginProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(buildJwtTokenAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(buildRefreshTokenProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(authExceptionHandler, buildRestLoginProcessingFilter().getClass());
//        if (oauth2Configuration != null) {
//            http.oauth2Login(login -> login
//                    .authorizationEndpoint(config -> config
//                            .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
//                            .authorizationRequestResolver(oAuth2AuthorizationRequestResolver))
//                    .loginPage("/oauth2Login")
//                    .loginProcessingUrl(oauth2Configuration.getLoginProcessingUrl())
//                    .successHandler(oauth2AuthenticationSuccessHandler)
//                    .failureHandler(oauth2AuthenticationFailureHandler));
//        }
        return http.build();
    }
}
