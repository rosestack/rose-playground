package io.github.rosestack.iam.app;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
public class App {
    private String id;
    private String name;
    private String code;
    private String logo;
    private String description;

    private Integer type;
    private Boolean trial;
    private Boolean deleted;

    private String userPoolId;

    /**
     * 状态
     */
    private Boolean status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 修改时间
     */
    private LocalDateTime updatedAt;

    //    // 是否为集成应用
    //    private Boolean isIntegrateApp;
    //
    //    // 默认应用协议类型
    //    // Enum: oidc,oauth,saml,cas,asa
    //    private String defaultProtocol;
    //
    //    // ["https://example.com/callback"]
    //    private String redirectUris;
    //
    //    private String logoutRedirectUris;
    //
    //    private String initLoginUri;
    //
    //    private Boolean ssoEnabled;
    //    private LocalDateTime ssoEnabledAt;
    //
    //    // mergeLoginAndRegisterPage 是否开启登录注册合并
    //    // enabledBasicLoginMethods 开启的基础登录方式:
    //    //
    // PHONE_CODE,EMAIL_CODE,PHONE_PASSWORD,EMAIL_PASSWORD,USERNAME_PASSWORD,SELF_BUILT_APP_QRCODE
    //    // defaultLoginMethod 应用默认登录方式（不包含社会化登录和企业身份源登录）
    //    // enabledExtIdpConns 开启的外部身份源连接
    //    // showAuthorizationPage
    //    private String loginConfig;
    //
    //    // enabledBasicRegisterMethods 开启的注册方式 PHONE_CODE,EMAIL_CODE,EMAIL_PASSWORD
    //    // defaultRegisterMethod	PASSWORD,PASSCODE
    //    private String registerConfig;
    //
    //    // grant_types
    //    // response_types
    //    // id_token_signed_response_alg
    //    // token_endpoint_auth_method
    //    // introspection_endpoint_auth_method
    //    // revocation_endpoint_auth_method
    //    // authorization_code_expire
    //    // id_token_expire
    //    // access_token_expire
    //    // refresh_token_expire
    //    // cas_expire
    //    // skip_consent
    //    // redirect_uris
    //    // post_logout_redirect_uris
    //    // client_id
    //    // scope
    //    private String oidcConfig;
    //
    //    // 是否开启 SAML 身份提供商
    //    private Boolean samlProviderEnabled;
    //    private String samlConfig;
    //
    //    private Boolean oauthProviderEnabled;
    //    // grants
    //    // introspection_endpoint_auth_method
    //    // revocation_endpoint_auth_method
    //    // redirect_uris
    //    // id
    //    private String oauthConfig;
    //
    //    private Boolean casProviderEnabled;
    //    private String casConfig;

    @Getter
    @AllArgsConstructor
    public static enum AppType {
        WEB("web"),
        SPA("spa"),
        NATIVE("native"),
        API("api"),
        MFA("mfa"),
        MINI_PROGRAM("mini-program"),
        ;

        private String value;
    }

    @Getter
    @AllArgsConstructor
    public static enum DefaultProtocol {
        OIDC("oidc"),
        OAUTH("oauth"),
        SAML("saml"),
        CAS("cas"),
        ASA("asa"),
        ;

        private String value;
    }
}
