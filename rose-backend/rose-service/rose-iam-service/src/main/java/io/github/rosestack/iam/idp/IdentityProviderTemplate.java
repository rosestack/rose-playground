package io.github.rosestack.iam.idp;

import lombok.Data;

import java.util.List;

@Data
public class IdentityProviderTemplate {
	private String id;
	private String name;
	private String description;
	private String type;

	private Long identityProviderId;

	private Boolean hidden;

	private List<IdpConnectionTemplateField> fields;

	private String availableUserMatchFields;

	private String allowChallengeAssoc;

	private String scenes; // web,app

	@Data
	private static class IdpConnectionTemplateField {
		private String key;
		private String name;
		private String placeholder;
		private String type; // input
		private Boolean required;
		private String children;
		private String extra;
		private String valueType;
	}
}
