package io.github.rosestack.spring.boot.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;

@Slf4j
@EnableAspectJAutoProxy
@Profile({"dev", "test"})
public class LoggingAspectConfig {

	@Bean
	public LoggingAspect loggingAspect(ObjectMapper objectMapper) {
		log.info("Initializing LoggingAspect for dev or test profile");

		return new LoggingAspect(objectMapper);
	}

	@Aspect
	public class LoggingAspect {
		private final ObjectMapper objectMapper;

		public LoggingAspect(ObjectMapper objectMapper) {
			this.objectMapper = objectMapper;
		}

		@Pointcut("within(@org.springframework.stereotype.Repository *)"
			+ " || within(@org.springframework.stereotype.Service *)"
			+ " || within(@org.springframework.web.bind.annotation.RestController *)")
		public void springBeanPointcut() {
		}

		private Logger logger(JoinPoint joinPoint) {
			return LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
		}

		@AfterThrowing(pointcut = "springBeanPointcut()", throwing = "e")
		public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
			logger(joinPoint)
				.error(
					"Exception in {}() with cause = '{}' and exception = '{}'",
					joinPoint.getSignature().getName(),
					e.getCause() != null ? e.getCause() : "NULL",
					e.getMessage(),
					e);
		}

		@Around("springBeanPointcut()")
		public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
			Logger log = logger(joinPoint);
			log.info(
				"Enter {}() with arguments = {}",
				joinPoint.getSignature().getName(),
				objectMapper.writeValueAsString(joinPoint.getArgs()));
			try {
				Object result = joinPoint.proceed();
				log.info(
					"Exit {}() with result = {}",
					joinPoint.getSignature().getName(),
					objectMapper.writeValueAsString(result));
				return result;
			} catch (IllegalArgumentException e) {
				log.error(
					"Illegal argument: {} in {}()",
					objectMapper.writeValueAsString(joinPoint.getArgs()),
					joinPoint.getSignature().getName());
				throw e;
			}
		}
	}
}
