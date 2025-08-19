package io.github.rosestack.mybatis.audit;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 多租户基础实体类
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseTenantEntity extends BaseEntity {

	/**
	 * 租户ID
	 */
	@TableField(value = "tenant_id", fill = FieldFill.INSERT)
	private String tenantId;
}
