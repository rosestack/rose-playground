package io.github.rosestack.iam.idp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 身份源
 */
@Data
public class Identity {
    /**
     * 身份源 ID
     */
    private String id;

    /**
     * 身份源连接 ID
     */
    private String identityProviderId;

    /**
     * 外部身份源类型： - `wechat`: 微信 - `qq`: QQ - `wechatwork`: 企业微信 - `dingtalk`: 钉钉 - `weibo`: 微博 -
     * `github`: GitHub - `alipay`: 支付宝 - `baidu`: 百度 - `lark`: 飞书 - `welink`: Welink - `yidun`: 网易易盾
     * - `qingcloud`: 青云 - `google`: Google - `gitlab`: GitLab - `gitee`: Gitee - `twitter`: Twitter -
     * `facebook`: Facebook - `slack`: Slack - `linkedin`: Linkedin - `instagram`: Instagram - `oidc`:
     * OIDC 型企业身份源 - `oauth2`: OAuth2 型企业身份源 - `saml`: SAML 型企业身份源 - `ldap`: LDAP 型企业身份源 - `ad`: AD
     * 型企业身份源 - `cas`: CAS 型企业身份源 - `azure-ad`: Azure AD 型企业身份源
     */
    private Provider provider;

    /**
     * Identity 类型，如 unionid, openid, primary
     */
    private String type;

    /**
     * 在外部身份源中的 ID
     */
    private String userId;

    /**
     * 用户在 idp 中的身份信息
     */
    private Object userInfoInIdp;

    /**
     * 在外部身份源中的 Access Token（此参数只会在用户主动获取时返回，管理侧接口不会返回）。
     */
    private String accessToken;

    /**
     * 在外部身份源中的 Refresh Token（此参数只会在用户主动获取时返回，管理侧接口不会返回）。
     */
    private String refreshToken;

    /**
     * 身份来自的身份源连接 ID 列表
     */
    private List<String> originConnIds;

    /**
     * 外部身份源类型： - `wechat`: 微信 - `qq`: QQ - `wechatwork`: 企业微信 - `dingtalk`: 钉钉 - `weibo`: 微博 -
     * `github`: GitHub - `alipay`: 支付宝 - `baidu`: 百度 - `lark`: 飞书 - `welink`: Welink - `yidun`: 网易易盾
     * - `qingcloud`: 青云 - `google`: Google - `gitlab`: GitLab - `gitee`: Gitee - `twitter`: Twitter -
     * `facebook`: Facebook - `slack`: Slack - `linkedin`: Linkedin - `instagram`: Instagram - `oidc`:
     * OIDC 型企业身份源 - `oauth2`: OAuth2 型企业身份源 - `saml`: SAML 型企业身份源 - `ldap`: LDAP 型企业身份源 - `ad`: AD
     * 型企业身份源 - `cas`: CAS 型企业身份源 - `azure-ad`: Azure AD 型企业身份源
     */
    public static enum Provider {
        @JsonProperty("oidc")
        OIDC("oidc"),

        @JsonProperty("oauth2")
        OAUTH2("oauth2"),

        @JsonProperty("saml")
        SAML("saml"),

        @JsonProperty("ldap")
        LDAP("ldap"),

        @JsonProperty("ad")
        AD("ad"),

        @JsonProperty("cas")
        CAS("cas"),

        @JsonProperty("azure-ad")
        AZURE_AD("azure-ad"),

        @JsonProperty("wechat")
        WECHAT("wechat"),

        @JsonProperty("google")
        GOOGLE("google"),

        @JsonProperty("qq")
        QQ("qq"),

        @JsonProperty("wechatwork")
        WECHATWORK("wechatwork"),

        @JsonProperty("dingtalk")
        DINGTALK("dingtalk"),

        @JsonProperty("weibo")
        WEIBO("weibo"),

        @JsonProperty("github")
        GITHUB("github"),

        @JsonProperty("alipay")
        ALIPAY("alipay"),

        @JsonProperty("apple")
        APPLE("apple"),

        @JsonProperty("baidu")
        BAIDU("baidu"),

        @JsonProperty("lark")
        LARK("lark"),

        @JsonProperty("gitlab")
        GITLAB("gitlab"),

        @JsonProperty("twitter")
        TWITTER("twitter"),

        @JsonProperty("facebook")
        FACEBOOK("facebook"),

        @JsonProperty("slack")
        SLACK("slack"),

        @JsonProperty("linkedin")
        LINKEDIN("linkedin"),

        @JsonProperty("yidun")
        YIDUN("yidun"),

        @JsonProperty("qingcloud")
        QINGCLOUD("qingcloud"),

        @JsonProperty("gitee")
        GITEE("gitee"),

        @JsonProperty("instagram")
        INSTAGRAM("instagram"),

        @JsonProperty("welink")
        WELINK("welink"),

        @JsonProperty("huawei")
        HUAWEI("huawei"),

        @JsonProperty("honor")
        HONOR("honor"),

        @JsonProperty("xiaomi")
        XIAOMI("xiaomi"),

        @JsonProperty("oppo")
        OPPO("oppo"),

        @JsonProperty("aws")
        AWS("aws"),

        @JsonProperty("amazon")
        AMAZON("amazon"),

        @JsonProperty("douyin")
        DOUYIN("douyin"),

        @JsonProperty("kuaishou")
        KUAISHOU("kuaishou"),

        @JsonProperty("line")
        LINE("line"),

        @JsonProperty("sdbz")
        SDBZ("sdbz"),
        ;

        private String value;

        Provider(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
