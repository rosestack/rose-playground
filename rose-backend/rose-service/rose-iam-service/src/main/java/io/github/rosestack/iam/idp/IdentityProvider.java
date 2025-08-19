package io.github.rosestack.iam.idp;

import lombok.Data;

@Data
public class IdentityProvider {
	private String id;
	private String name;
	private String description;

	private String type;
	private String authType;

	private String bindUrl;

	private Boolean allowMultiple;

	private Boolean allowMultipleConn;

	public enum Type {
		OIDC("oidc"),

		OAUTH2("oauth2"),

		SAML("saml"),

		LDAP("ldap"),

		AD("ad"),

		CAS("cas"),

		AZURE_AD("azure-ad"),

		WECHAT("wechat"),

		GOOGLE("google"),

		QQ("qq"),

		WECHATWORK("wechatwork"),

		DINGTALK("dingtalk"),

		WEIBO("weibo"),

		GITHUB("github"),

		ALIPAY("alipay"),

		APPLE("apple"),

		BAIDU("baidu"),

		LARK("lark"),

		GITLAB("gitlab"),

		TWITTER("twitter"),

		FACEBOOK("facebook"),

		SLACK("slack"),

		LINKEDIN("linkedin"),

		YIDUN("yidun"),

		QINGCLOUD("qingcloud"),

		GITEE("gitee"),

		INSTAGRAM("instagram"),

		WELINK("welink"),

		HUAWEI("huawei"),

		HONOR("honor"),

		XIAOMI("xiaomi"),

		OPPO("oppo"),

		AWS("aws"),

		AMAZON("amazon"),

		DOUYIN("douyin"),

		KUAISHOU("kuaishou"),

		LINE("line"),

		SDBZ("sdbz"),
		;

		private String value;

		Type(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	/**
	 * 认证类型
	 */
	public static enum AuthType {
		SOCIAL("social"),
		ENTERPRISE("enterprise"),
		;

		private String value;

		AuthType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
