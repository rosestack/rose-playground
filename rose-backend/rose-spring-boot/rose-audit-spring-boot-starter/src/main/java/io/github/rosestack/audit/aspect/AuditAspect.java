package io.github.rosestack.audit.aspect;

import io.github.rosestack.audit.annotation.Audit;
import io.github.rosestack.audit.entity.AuditLog;
import io.github.rosestack.audit.entity.AuditLogDetail;
import io.github.rosestack.audit.enums.*;
import io.github.rosestack.audit.properties.AuditProperties;
import io.github.rosestack.audit.service.AuditLogDetailService;
import io.github.rosestack.audit.service.AuditLogService;
import io.github.rosestack.audit.util.AuditJsonUtils;
import io.github.rosestack.audit.util.AuditMaskingUtils;
import io.github.rosestack.core.jackson.JsonUtils;
import io.github.rosestack.core.util.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 审计切面
 * <p>
 * 拦截标记了 @Audit 注解的方法，自动记录审计日志。
 * 支持同步和异步记录，提供丰富的上下文信息收集功能。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@Order(100) // 确保在事务切面之后执行
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rose.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final AuditLogDetailService auditLogDetailService;
    private final AuditProperties auditProperties;

    /**
     * SpEL表达式解析器
     */
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * 环绕通知：拦截@Audit注解的方法
     */
    @Around("@annotation(audit)")
    public Object around(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        // 检查是否启用审计
        if (!auditProperties.isEnabled()) {
            return joinPoint.proceed();
        }

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
                // 不影响业务执行
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

            if (audit.async()) {
                // 异步记录
                CompletableFuture<AuditLog> future = auditLogService.recordAuditLogAsync(auditLog);
                future.thenAccept(savedLog -> recordAuditDetails(joinPoint, audit, savedLog, result, exception))
                        .exceptionally(throwable -> {
                            log.error("异步记录审计日志失败: {}", throwable.getMessage(), throwable);
                            return null;
                        });
            } else {
                // 同步记录
                AuditLog savedLog = auditLogService.recordAuditLog(auditLog);
                recordAuditDetails(joinPoint, audit, savedLog, result, exception);
            }
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
        String eventSubtype = eventType.getEventSubType();

        // 获取风险等级
        RiskLevel riskLevel = getRiskLevel(audit, eventType);

        // 构建审计日志
        AuditLog auditLog = AuditLog.builder()
                .eventTime(startTime)
                .operationName(operationName)
                .status(status.getCode())
                .executionTime(executionTime)
                .build();

        // 设置事件类型
        auditLog.setEventType(eventType);
        auditLog.setEventSubtype(eventSubtype);

        // 设置风险等级
        auditLog.setRiskLevel(riskLevel);

        // 设置HTTP信息
        if (audit.recordHttpInfo()) {
            setHttpInfo(auditLog);
        }

        // 设置异常信息
        if (exception != null && audit.recordException()) {
            auditLog.setErrorCode(exception.getClass().getSimpleName());
        }

        // 设置自定义属性
        setCustomAttributes(auditLog, audit);

        return auditLog;
    }

    /**
     * 记录审计详情
     */
    private void recordAuditDetails(ProceedingJoinPoint joinPoint, Audit audit, AuditLog auditLog,
                                    Object result, Throwable exception) {
        try {
            List<AuditLogDetail> details = new ArrayList<>();

            // 记录方法参数
            if (audit.recordParams()) {
                details.add(buildParameterDetail(joinPoint, audit, auditLog.getId()));
            }

            // 记录返回值
            if (audit.recordResult() && result != null) {
                details.add(buildResultDetail(result, audit, auditLog.getId()));
            }

            // 记录HTTP请求信息
            if (audit.recordHttpInfo()) {
                details.addAll(buildHttpDetails(auditLog.getId()));
            }

            // 记录异常信息
            if (exception != null && audit.recordException()) {
                details.addAll(buildExceptionDetails(exception, auditLog.getId()));
            }

            // 批量保存详情
            if (!details.isEmpty()) {
                if (audit.async()) {
                    auditLogDetailService.recordAuditDetailBatchAsync(details);
                } else {
                    auditLogDetailService.recordAuditDetailBatch(details);
                }
            }
        } catch (Exception e) {
            log.error("记录审计详情失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 构建参数详情
     */
    private AuditLogDetail buildParameterDetail(ProceedingJoinPoint joinPoint, Audit audit, Long auditLogId) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();
        Set<String> maskParams = new HashSet<>(Arrays.asList(audit.maskParams()));

        List<Object> newArgs = new ArrayList<>();
        for (int i = 0; i < parameters.length && i < args.length; i++) {
            Parameter parameter = parameters[i];
            Object arg = args[i];

            String paramName = parameter.getName();
            // 跳过特殊类型的参数
            if (isSpecialType(parameter.getType())) {
                continue;
            }

            if (maskParams.contains(paramName.toLowerCase()) && parameter.getType() == String.class) {
                newArgs.add(AuditMaskingUtils.maskByFieldName(paramName.toLowerCase(), (String) arg));
            } else {
                newArgs.add(arg);
            }
        }

        return AuditLogDetail.builder()
                .auditLogId(auditLogId)
                .detailKey(AuditDetailKey.REQUEST_PARAMS.getCode())
                .detailType(AuditDetailType.HTTP_REQUEST.getCode())
                .detailValue(JsonUtils.toString(newArgs))
                .isSensitive(AuditDetailKey.REQUEST_PARAMS.isSensitive())
                .build();
    }

    /**
     * 构建返回值详情
     */
    private AuditLogDetail buildResultDetail(Object result, Audit audit, Long auditLogId) {
        return AuditLogDetail.builder()
                .auditLogId(auditLogId)
                .detailKey(AuditDetailKey.RESPONSE_RESULT.getCode())
                .detailType(AuditDetailKey.RESPONSE_RESULT.getDetailType().getCode())
                .detailValue(AuditJsonUtils.toMaskedJsonString(result))
                .isSensitive(AuditDetailKey.RESPONSE_RESULT.isSensitive())
                .build();
    }

    /**
     * 构建HTTP详情
     */
    private List<AuditLogDetail> buildHttpDetails(Long auditLogId) {
        List<AuditLogDetail> details = new ArrayList<>();

        try {
            // 获取请求头
            Map<String, String> headers = ServletUtils.getRequestHeaders();
            if (!headers.isEmpty()) {
                String headersJson = AuditJsonUtils.toJsonString(headers);

                AuditLogDetail headerDetail = AuditLogDetail.builder()
                        .auditLogId(auditLogId)
                        .detailKey(AuditDetailKey.REQUEST_HEADERS.getCode())
                        .detailType(AuditDetailKey.REQUEST_HEADERS.getDetailType().getCode())
                        .detailValue(headersJson)
                        .isSensitive(AuditDetailKey.REQUEST_HEADERS.isSensitive())
                        .build();

                details.add(headerDetail);
            }

            ServletUtils.getRequestBody();

            // 获取请求参数
            Map<String, String> params = ServletUtils.getRequestParams();
            if (!params.isEmpty()) {
                String paramsJson = AuditJsonUtils.toMaskedJsonString(params);

                AuditLogDetail paramDetail = AuditLogDetail.builder()
                        .auditLogId(auditLogId)
                        .detailKey(AuditDetailKey.REQUEST_PARAMS.getCode())
                        .detailType(AuditDetailKey.REQUEST_PARAMS.getDetailType().getCode())
                        .detailValue(paramsJson)
                        .isSensitive(AuditDetailKey.REQUEST_PARAMS.isSensitive())
                        .build();

                details.add(paramDetail);
            }
        } catch (Exception e) {
            log.warn("构建HTTP详情失败: {}", e.getMessage());
        }

        return details;
    }

    /**
     * 构建异常详情
     */
    private List<AuditLogDetail> buildExceptionDetails(Throwable exception, Long auditLogId) {
        List<AuditLogDetail> details = new ArrayList<>();

        try {
            Map<String, Object> exceptionInfo = new HashMap<>();
            exceptionInfo.put("type", exception.getClass().getName());
            exceptionInfo.put("message", exception.getMessage());
            exceptionInfo.put("stackTrace", getStackTrace(exception));

            String exceptionJson = AuditJsonUtils.toJsonString(exceptionInfo);

            AuditLogDetail detail = AuditLogDetail.builder()
                    .auditLogId(auditLogId)
                    .detailKey(AuditDetailKey.EXCEPTION_STACK.getCode())
                    .detailType(AuditDetailKey.EXCEPTION_STACK.getDetailType().getCode())
                    .detailValue(exceptionJson)
                    .isSensitive(AuditDetailKey.EXCEPTION_STACK.isSensitive())
                    .build();

            details.add(detail);
        } catch (Exception e) {
            log.warn("构建异常详情失败: {}", e.getMessage());
        }

        return details;
    }

    /**
     * 获取操作名称
     */
    private String getOperationName(Audit audit, Method method) {
        if (StringUtils.hasText(audit.value())) {
            return audit.value();
        }
        if (StringUtils.hasText(audit.value())) {
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
    private RiskLevel getRiskLevel(Audit audit, AuditEventType eventType) {
        if (audit.riskLevel() != RiskLevel.LOW) {
            return audit.riskLevel();
        }
        return RiskLevel.fromEventType(eventType);
    }

    /**
     * 设置HTTP信息
     */
    private void setHttpInfo(AuditLog auditLog) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                auditLog.setRequestUri(request.getRequestURI());
                auditLog.setHttpMethod(request.getMethod());
                auditLog.setClientIp(ServletUtils.getClientIpAddress());
                auditLog.setUserAgent(ServletUtils.getUserAgent());
                auditLog.setSessionId(ServletUtils.getCurrentRequest().getSession().getId());
            }
        } catch (Exception e) {
            log.debug("设置HTTP信息失败: {}", e.getMessage());
        }
    }

    /**
     * 设置自定义属性
     */
    private void setCustomAttributes(AuditLog auditLog, Audit audit) {
        if (StringUtils.hasText(audit.customAttributes())) {
            // 解析自定义属性并设置到审计日志中
            // 这里可以扩展为更复杂的属性处理逻辑
        }

        if (audit.tags().length > 0) {
            // 可以将标签信息设置到某个字段中
        }
    }

    /**
     * 评估条件表达式
     */
    private boolean evaluateCondition(String condition, ProceedingJoinPoint joinPoint, Object result) {
        if (!StringUtils.hasText(condition)) {
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
                type.getName().startsWith("jakarta.servlet.");
    }

    /**
     * 获取异常堆栈信息（截取前几行）
     */
    private String getStackTrace(Throwable exception) {
        if (exception == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        StackTraceElement[] elements = exception.getStackTrace();
        int maxLines = 5; // 只保留前5行堆栈信息

        for (int i = 0; i < Math.min(elements.length, maxLines); i++) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append(elements[i].toString());
        }

        if (elements.length > maxLines) {
            sb.append("\n... and ").append(elements.length - maxLines).append(" more");
        }

        return sb.toString();
    }
}