package io.github.rosestack.iam.userpool;

import lombok.Data;

@Data
public class UserPool {
    private String id; // 68020bd001975eda09b4593d

    private String logo;
    private String name;
    private String domain;
    private String description;
    private String type; // B2E B2B B2C
    private String userId;

    private Boolean deleted;

    /** 状态 */
    private Boolean status;

    //    private Boolean isRoot;

    //    private Long tokenExpiresAfter;
    //
    //    private Boolean appSsoEnabled;
    //    private String allowedOrigins;
    //    private Boolean emailVerifiedDefault;
    //    private Boolean sendWelcomeEmail;
    //    private Boolean registerDisabled;
    //    private Boolean loginRequireEmailVerified;
    //    private Boolean sendUpdatePwdEmailNotifyVerified;
    //    private Boolean robotVerify;
    //    private Boolean robotVerifyLoginTimeCheckEnable;
    //    private Boolean robotVerifyLoginWeekStartEndTime;
    //
    //    private String accountLock;
    //    private String loginFailStrategy;
    //    private Boolean enableSelfUnlock;
    //    private String selfUnlockStrategy; // password-captcha
    //    private String userPoolTypes;
    //    private Integer passwordStrength;
    //    private Integer packageType;
    //    private String packageMode; // standard
    //    private Integer defaultNamespaceId;
    //    private Integer systemNamespaceId;
    //
    //    private Integer verifyCodeLength;
    //    private Integer verifyCodeMaxAttempts;
}
