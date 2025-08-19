package io.github.rosestack.spring.boot.mybatis.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Rose MyBatis Plus 配置属性
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@Validated
@ConfigurationProperties(prefix = "rose.mybatis")
public class RoseMybatisProperties {

	/**
	 * 是否启用 Rose MyBatis Plus 增强功能
	 */
	private boolean enabled = true;

	/**
	 * 多租户配置
	 */
	private Tenant tenant = new Tenant();

	/**
	 * 分页配置
	 */
	private Pagination pagination = new Pagination();

	/**
	 * 字段填充配置
	 */
	private FieldFill fieldFill = new FieldFill();

	/**
	 * 数据权限配置
	 */
	private DataPermission dataPermission = new DataPermission();

	/**
	 * SQL 审计配置
	 */
	private Audit audit = new Audit();

	private Encryption encryption = new Encryption();

	@Data
	public static class Encryption {
		private boolean enabled = false;
	}

	/**
	 * 多租户配置
	 */
	@Data
	public static class Tenant {
		/**
		 * 是否启用多租户
		 */
		private boolean enabled = false;

		/**
		 * 租户字段名
		 */
		@NotBlank
		private String column = "tenant_id";

		/**
		 * 忽略多租户的表名列表
		 */
		private List<@NotBlank String> ignoreTables = new ArrayList<>();

		/**
		 * 忽略多租户的表名前缀列表
		 */
		private List<@NotBlank String> ignoreTablePrefixes = new ArrayList<>();
	}

	/**
	 * 分页配置
	 */
	@Data
	public static class Pagination {
		/**
		 * 是否启用分页插件
		 */
		private boolean enabled = true;

		/**
		 * 单页最大限制数量
		 */
		@Min(1)
		private Long maxLimit = 1000L;

		/**
		 * 是否启用合理化分页
		 */
		private boolean reasonable = true;

		/**
		 * 数据库类型（自动检测）
		 */
		@NotBlank
		private String dbType = "mysql";
	}

	/**
	 * 字段填充配置
	 */
	@Data
	public static class FieldFill {
		/**
		 * 是否启用字段自动填充
		 */
		private boolean enabled = true;

		/**
		 * 创建时间字段名
		 */
		@NotBlank
		private String createTimeColumn = "created_time";

		@NotBlank
		private String createdByColumn = "created_by";

		/**
		 * 更新时间字段名
		 */
		@NotBlank
		private String updateTimeColumn = "updated_time";

		@NotBlank
		private String updatedByColumn = "updated_by";

		private String defaultUser;
	}

	/**
	 * 数据权限配置
	 */
	@Data
	public static class DataPermission {
		/**
		 * 是否启用数据权限
		 */
		private boolean enabled = true;

		/**
		 * 缓存配置
		 */
		private Cache cache = new Cache();

		/**
		 * 缓存配置
		 */
		@Data
		public static class Cache {
			/**
			 * 缓存过期时间（分钟）
			 */
			@Min(1)
			private long expireMinutes = 30;

			/**
			 * 缓存清理间隔（分钟）
			 */
			@Min(1)
			private long cleanupIntervalMinutes = 60;

			/**
			 * 过期率阈值
			 */
			@DecimalMin("0.0")
			@DecimalMax("1.0")
			private Double expiredRate = 0.5;

			/**
			 * 最大注解缓存数量
			 */
			@Min(1)
			private int maxAnnotationCacheSize = 10000;

			/**
			 * 最大权限缓存数量
			 */
			@Min(1)
			private int maxPermissionCacheSize = 50000;
		}
	}

	/**
	 * SQL 审计配置
	 */
	@Data
	public static class Audit {
		/**
		 * 是否启用 SQL 审计
		 */
		private boolean enabled = true;

		/**
		 * 是否包含 SQL 语句
		 */
		private boolean includeSql = true;

		/**
		 * 日志级别
		 */
		private String logLevel = "INFO";
	}
}
