package io.github.rosestack.iam.domain;

import io.github.rosestack.mybatis.audit.BaseEntity;
import lombok.Data;

@Data
public class UserPosition extends BaseEntity {
	private String positionId;

	private String userId;
}
