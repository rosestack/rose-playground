package com.company.todo.config;

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
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Slf4j
@Configuration
@EnableAspectJAutoProxy
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
        public void springBeanPointcut() {}

        @Pointcut("within(com.company.todo..*.*Repository)" + " || within(com.company.todo..*.*Service)"
                + " || within(com.company.todo..*.*Controller)")
        public void applicationPackagePointcut() {}

        private Logger logger(JoinPoint joinPoint) {
            return LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
        }

        @AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
        public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
            logger(joinPoint)
                    .error(
                            "Exception in {}() with cause = '{}' and exception = '{}'",
                            joinPoint.getSignature().getName(),
                            e.getCause() != null ? e.getCause() : "NULL",
                            e.getMessage(),
                            e);
        }

        @Around("applicationPackagePointcut() && springBeanPointcut()")
        public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
            Logger log = logger(joinPoint);
            if (log.isDebugEnabled()) {
                log.debug(
                        "Enter {}() with arguments = {}",
                        joinPoint.getSignature().getName(),
                        objectMapper.writeValueAsString(joinPoint.getArgs()));
            }
            try {
                Object result = joinPoint.proceed();
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Exit {}() with result = {}",
                            joinPoint.getSignature().getName(),
                            objectMapper.writeValueAsString(result));
                }
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
