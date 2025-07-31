package io.github.rosestack.audit.aspect;

import io.github.rosestack.audit.annotation.Audit;
import io.github.rosestack.audit.config.AuditProperties;
import io.github.rosestack.audit.entity.AuditLog;
import io.github.rosestack.audit.entity.AuditLogDetail;
import io.github.rosestack.audit.enums.AuditDetailKey;
import io.github.rosestack.audit.enums.AuditEventType;
import io.github.rosestack.audit.enums.AuditRiskLevel;
import io.github.rosestack.audit.enums.AuditStatus;
import io.github.rosestack.audit.event.AuditEvent;
import io.github.rosestack.core.jackson.desensitization.MaskUtils;
import io.github.rosestack.core.util.ServletUtils;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Aspect
@Order(100) // 确保在事务切面之后执行
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rose.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditAspect {
    private final ApplicationEventPublisher eventPublisher;
    private final AuditProperties properties;

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * 环绕通知：拦截@Audit注解的方法
     */
    @Around("@annotation(audit)")
    public Object around(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        // 检查条件表达式
        if (!evaluateCondition(audit.condition(), joinPoint, null)) {
            return joinPoint.proceed();
        }

        LocalDateTime startTime = LocalDateTime.now();
        long executionStartTime = System.currentTimeMillis();

        Object result = null;
        Throwable exception = null;
        AuditStatus status = AuditStatus.SUCCESS;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            status = AuditStatus.FAILURE;
            throw e;
        } finally {
            try {
                // 计算执行时间
                long executionTime = System.currentTimeMillis() - executionStartTime;

                // 再次检查条件（包含返回值）
                if (evaluateCondition(audit.condition(), joinPoint, result)) {
                    // 记录审计日志
                    recordAuditLog(joinPoint, audit, startTime, executionTime, result, exception, status);
                }
            } catch (Exception e) {
                log.error("记录审计日志失败: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 记录审计日志
     */
    private void recordAuditLog(ProceedingJoinPoint joinPoint, Audit audit, LocalDateTime startTime,
                                long executionTime, Object result, Throwable exception, AuditStatus status) {
        try {
            // 构建审计日志
            AuditLog auditLog = buildAuditLog(joinPoint, audit, startTime, executionTime, result, exception, status);

            // 构建审计详情
            List<AuditLogDetail> auditLogDetails = buildAuditDetails(joinPoint, audit, auditLog.getId(), result, exception);

            // 发布审计事件，由监听器处理具体的存储逻辑
            AuditEvent auditEvent = new AuditEvent(auditLog, auditLogDetails);
            eventPublisher.publishEvent(auditEvent);

            log.debug("发布审计事件成功，审计日志ID: {}", auditLog.getId());
        } catch (Exception e) {
            log.error("构建审计日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 构建审计日志对象
     */
    private AuditLog buildAuditLog(ProceedingJoinPoint joinPoint, Audit audit, LocalDateTime startTime,
                                   long executionTime, Object result, Throwable exception, AuditStatus status) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取操作名称
        String operationName = getOperationName(audit, method);

        // 获取事件类型和子类型
        AuditEventType eventType = getEventType(audit, method);

        // 构建审计日志
        AuditLog auditLog = AuditLog.builder()
                .eventTime(startTime)
                .operationName(operationName)
                .status(status.getCode())
                .executionTime(executionTime)
                .build();

        // 设置事件类型
        auditLog.setEventType(eventType);
        auditLog.setEventSubtype(eventType.getEventSubType());

        // 设置风险等级
        auditLog.setRiskLevel(getRiskLevel(audit, eventType));

        setHttpInfo(auditLog);

        return auditLog;
    }

    /**
     * 构建审计详情列表
     */
    private List<AuditLogDetail> buildAuditDetails(ProceedingJoinPoint joinPoint, Audit audit, Long auditLogId,
                                                   Object result, Throwable exception) {
        List<AuditLogDetail> details = new ArrayList<>();

        List<String> maskFields = getMaskFields(audit);

        try {
            // 记录方法参数
            if (audit.recordParams()) {
                details.add(buildParameterDetail(joinPoint, audit, maskFields, auditLogId));
            }

            // 记录方法返回值
            if (audit.recordResult() && result != null) {
                details.add(AuditLogDetail.createDetail(audit, auditLogId, AuditDetailKey.RESPONSE_RESULT, result));
            }

            // 记录HTTP请求信息
            details.addAll(buildHttpDetails(audit, maskFields, auditLogId));

            // 记录异常信息
            if (exception != null && audit.recordException()) {
                details.addAll(buildExceptionDetails(audit, exception, auditLogId));
            }
        } catch (Exception e) {
            log.error("构建审计详情失败: {}", e.getMessage(), e);
        }

        return details;
    }

    private List<String> getMaskFields(Audit audit) {
        List<String> maskFields = Arrays.asList(audit.maskFields());
        maskFields.addAll(properties.getMaskFields());

        return maskFields;
    }

    /**
     * 构建参数详情
     */
    private AuditLogDetail buildParameterDetail(ProceedingJoinPoint joinPoint, Audit audit, List<String> maskFields, Long auditLogId) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        List<Object> newArgs = new ArrayList<>();
        for (int i = 0; i < parameters.length && i < args.length; i++) {
            Parameter parameter = parameters[i];
            Object arg = args[i];

            String paramName = parameter.getName();
            // 跳过特殊类型的参数
            if (isSpecialType(parameter.getType())) {
                continue;
            }

            if (maskFields.contains(paramName) && parameter.getType() == String.class) {
                newArgs.add(MaskUtils.maskToken((String) arg));
            } else {
                newArgs.add(arg);
            }
        }
        return AuditLogDetail.createDetail(audit, auditLogId, AuditDetailKey.REQUEST_PARAMS, newArgs);
    }

    /**
     * 构建HTTP详情
     */
    private List<AuditLogDetail> buildHttpDetails(Audit audit, List<String> maskFields, Long auditLogId) {
        List<AuditLogDetail> details = new ArrayList<>();

        Function<String, String> maskFunction = s -> {
            if (maskFields.contains(s)) {
                return MaskUtils.maskToken(s);
            }
            return s;
        };

        try {
            // 获取 request 请求头
            Map<String, String> headers = ServletUtils.getRequestHeaders(maskFunction);
            if (!headers.isEmpty()) {
                details.add(AuditLogDetail.createDetail(audit, auditLogId, AuditDetailKey.REQUEST_HEADERS, headers));
            }

            //获取 response 请求头
            headers = ServletUtils.getResponseHeaders(maskFunction);
            if (!headers.isEmpty()) {
                details.add(AuditLogDetail.createDetail(audit, auditLogId, AuditDetailKey.RESPONSE_HEADERS, headers));
            }
        } catch (Exception e) {
            log.warn("构建HTTP详情失败: {}", e.getMessage());
        }

        return details;
    }

    /**
     * 构建异常详情
     */
    private List<AuditLogDetail> buildExceptionDetails(Audit audit, Throwable exception, Long auditLogId) {
        List<AuditLogDetail> details = new ArrayList<>();

        try {
            Map<String, Object> exceptionInfo = new HashMap<>();
            exceptionInfo.put("type", exception.getClass().getName());
            exceptionInfo.put("message", exception.getMessage());
            exceptionInfo.put("stackTrace", ExceptionUtils.getStackTrace(exception));

            details.add(AuditLogDetail.createDetail(audit, auditLogId, AuditDetailKey.EXCEPTION_STACK, exceptionInfo));
        } catch (Exception e) {
            log.warn("构建异常详情失败: {}", e.getMessage());
        }

        return details;
    }

    /**
     * 获取操作名称
     */
    private String getOperationName(Audit audit, Method method) {
        if (StringUtils.isNoneBlank(audit.value())) {
            return audit.value();
        }
        if (StringUtils.isNoneBlank(audit.value())) {
            return audit.value();
        }
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }

    /**
     * 获取事件类型
     */
    private AuditEventType getEventType(Audit audit, Method method) {
        if (audit.eventType() != AuditEventType.DATA_OTHER) {
            return audit.eventType();
        }

        // 根据方法名推断事件类型
        String methodName = method.getName().toLowerCase();
        if (methodName.contains("login") || methodName.contains("logout") || methodName.contains("auth")) {
            return AuditEventType.AUTH_LOGIN;
        } else if (methodName.contains("create") || methodName.contains("add") || methodName.contains("insert")) {
            return AuditEventType.DATA_CREATE;
        } else if (methodName.contains("update") || methodName.contains("modify") || methodName.contains("edit")) {
            return AuditEventType.DATA_UPDATE;
        } else if (methodName.contains("delete") || methodName.contains("remove")) {
            return AuditEventType.DATA_DELETE;
        } else if (methodName.contains("query") || methodName.contains("find") || methodName.contains("get") || methodName.contains("list")) {
            return AuditEventType.DATA_READ;
        }

        return AuditEventType.DATA_OTHER;
    }

    /**
     * 获取风险等级
     */
    private AuditRiskLevel getRiskLevel(Audit audit, AuditEventType eventType) {
        if (audit.riskLevel() != AuditRiskLevel.LOW) {
            return audit.riskLevel();
        }
        return AuditRiskLevel.fromEventType(eventType);
    }

    /**
     * 设置HTTP信息
     */
    private void setHttpInfo(AuditLog auditLog) {
        HttpServletRequest request = ServletUtils.getCurrentRequest();
        if (request != null) {
            auditLog.setRequestUri(request.getRequestURI());
            auditLog.setHttpMethod(request.getMethod());
            auditLog.setClientIp(ServletUtils.getClientIpAddress());
            auditLog.setUserAgent(ServletUtils.getUserAgent());
            auditLog.setSessionId(ServletUtils.getCurrentRequest().getSession().getId());
        }
    }

    /**
     * 评估条件表达式
     */
    private boolean evaluateCondition(String condition, ProceedingJoinPoint joinPoint, Object result) {
        if (StringUtils.isBlank(condition)) {
            return true;
        }

        try {
            Expression expression = expressionParser.parseExpression(condition);
            EvaluationContext context = new StandardEvaluationContext();

            // 设置方法参数
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            for (int i = 0; i < paramNames.length && i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }

            // 设置返回值
            if (result != null) {
                context.setVariable("result", result);
            }

            return Boolean.TRUE.equals(expression.getValue(context, Boolean.class));
        } catch (Exception e) {
            log.warn("评估条件表达式失败: {}, 条件: {}", e.getMessage(), condition);
            return true; // 默认记录
        }
    }

    /**
     * 判断是否为特殊类型（不需要序列化的类型）
     */
    private boolean isSpecialType(Class<?> type) {
        return HttpServletRequest.class.isAssignableFrom(type) ||
                type.getName().startsWith("org.springframework.") ||
                type.getName().startsWith("javax.servlet.") ||
                type.getName().startsWith("jakarta.servlet.");
    }
}