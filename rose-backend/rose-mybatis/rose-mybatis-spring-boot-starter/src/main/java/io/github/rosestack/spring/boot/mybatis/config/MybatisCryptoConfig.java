package io.github.rosestack.spring.boot.mybatis.config;

import io.github.rosestack.crypto.FieldEncryptor;
import io.github.rosestack.crypto.hash.HashService;
import io.github.rosestack.spring.boot.mybatis.crypto.FieldEncryptorInterceptor;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@Slf4j
@ConditionalOnProperty(prefix = "rose.mybatis.crypto", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MybatisCryptoConfig {
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean({FieldEncryptor.class, HashService.class})
	public FieldEncryptorInterceptor fieldEncryptorInterceptor(FieldEncryptor fieldEncryptor, HashService hashService,
																@Autowired(required = false) MeterRegistry registry) {
		log.info("启用字段加密解密拦截器");
		return new FieldEncryptorInterceptor(fieldEncryptor, hashService, registry);
	}
}
