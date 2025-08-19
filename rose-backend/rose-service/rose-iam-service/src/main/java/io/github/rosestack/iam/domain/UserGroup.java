package io.github.rosestack.iam.domain;

import io.github.rosestack.mybatis.audit.BaseEntity;
import lombok.Data;

@Data
public class UserGroup extends BaseEntity {

	private String groupId;

	private String userId;
}
