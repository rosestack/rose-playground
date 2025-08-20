package io.github.rosestack.spring.boot.mybatis.config;

import io.github.rosestack.encryption.FieldEncryptor;
import io.github.rosestack.encryption.hash.HashService;
import io.github.rosestack.spring.boot.mybatis.encryption.FieldEncryptionInterceptor;
import io.github.rosestack.spring.boot.mybatis.permission.controller.DataPermissionController;
import io.github.rosestack.spring.boot.mybatis.permission.service.DataPermissionService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Slf4j
@ConditionalOnProperty(prefix = "rose.mybatis.encryption", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EncryptionConfig {
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean({FieldEncryptor.class, HashService.class})
	public FieldEncryptionInterceptor fieldEncryptionInterceptor(FieldEncryptor fieldEncryptor, HashService hashService,
																 @Autowired(required = false) MeterRegistry registry) {
		log.info("启用字段加密解密拦截器");
		return new FieldEncryptionInterceptor(fieldEncryptor, hashService, registry);
	}
}
