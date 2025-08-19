package io.github.rosestack.mybatis.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * 核心层租户上下文持有者（不依赖 Spring）。
 */
public class TenantContextHolder {
	private static final Logger log = LoggerFactory.getLogger(TenantContextHolder.class);

	private static final InheritableThreadLocal<String> TENANT_ID = new InheritableThreadLocal<>();

	public static String getCurrentTenantId() {
		return TENANT_ID.get();
	}

	public static void setCurrentTenantId(String tenantId) {
		log.info("切换租户为 {}", tenantId);

		TENANT_ID.set(tenantId);
	}

	public static boolean hasTenantContext() {
		return TENANT_ID.get() != null;
	}

	public static void clear() {
		TENANT_ID.remove();
	}

	public static void runWithTenant(String tenantId, Runnable r) {
		String origin = getCurrentTenantId();
		try {
			setCurrentTenantId(tenantId);
			r.run();
		} finally {
			if (origin != null) {
				setCurrentTenantId(origin);
			} else {
				clear();
			}
		}
	}

	public static <T> T runWithTenant(String tenantId, Supplier<T> s) {
		String origin = getCurrentTenantId();
		try {
			setCurrentTenantId(tenantId);
			return s.get();
		} finally {
			if (origin != null) {
				setCurrentTenantId(origin);
			} else {
				clear();
			}
		}
	}
}
