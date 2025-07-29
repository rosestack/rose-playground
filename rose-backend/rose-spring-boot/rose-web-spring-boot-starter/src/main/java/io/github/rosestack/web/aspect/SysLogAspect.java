package io.github.rosestack.web.aspect;

import io.github.rosestack.core.jackson.JsonUtils;
import io.github.rosestack.core.spring.SpringContextUtils;
import io.github.rosestack.core.util.ServletUtils;
import io.github.rosestack.web.annotation.SysLog;
import io.github.rosestack.web.annotation.SysLogIgnore;
import io.github.rosestack.web.event.SysLogEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 操作日志切面
 * <p>
 * 处理 @SysLog 注解，记录操作日志
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SysLogAspect {
    @Around("@annotation(io.github.rosestack.web.annotation.SysLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        SysLog sysLog = method.getAnnotation(SysLog.class);

        SysLogEvent sysLogEvent = createSysLogEvent(sysLog, joinPoint.getArgs());

        // 执行目标方法
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object result = null;
        try {
            result = joinPoint.proceed();
            stopWatch.stop();

            return result;

        } catch (Exception e) {
            if (sysLog.logResult()) {
                log.debug("返回结果: {}", result);
                sysLogEvent.setResult(JsonUtils.toString(result));
            }
            sysLogEvent.setSuccess(false);
            sysLogEvent.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            stopWatch.stop();
            sysLogEvent.setExecutionTime(stopWatch.getTotalTimeMillis());
            SpringContextUtils.getApplicationContext().publishEvent(sysLogEvent);
        }
    }

    private SysLogEvent createSysLogEvent(SysLog sysLog, Object[] args) {
        SysLogEvent sysLogEvent = new SysLogEvent();
        sysLogEvent.setName(sysLog.name());
        sysLogEvent.setModule(sysLog.module());
        sysLogEvent.setRequestId(ServletUtils.getCurrentRequestId());
        sysLogEvent.setUserId(ServletUtils.getCurrentUserId());
        sysLogEvent.setUsername(ServletUtils.getCurrentUsername());
        sysLogEvent.setClientIp(ServletUtils.getClientIp());
        sysLogEvent.setTenantId(ServletUtils.getCurrentTenantId());
        sysLogEvent.setUserAgent(ServletUtils.getUserAgent());
        sysLogEvent.setLocation(null);
        sysLogEvent.setDeviceInfo(null);
        sysLogEvent.setRequestUrl(ServletUtils.getCurrentRequest().getRequestURI());
        sysLogEvent.setRequestMethod(ServletUtils.getCurrentRequest().getMethod());
        if (HttpMethod.PUT.name().equals(sysLogEvent.getRequestMethod())
                || HttpMethod.POST.name().equals(sysLogEvent.getRequestMethod())) {
            sysLogEvent.setRequestParams(JsonUtils.toString(dealArgs(args)));
        } else {
            sysLogEvent.setRequestParams(JsonUtils.toString(ServletUtils.getParamMap()));
        }
        sysLogEvent.setCreateTime(LocalDateTime.now());
        sysLogEvent.setSuccess(true);
        sysLogEvent.setExecutionTime(0L);
        sysLogEvent.setErrorMessage(null);
        sysLogEvent.setAttributes(new HashMap<>());
        return sysLogEvent;
    }

    private static List<Object> dealArgs(Object[] args) {
        if (args == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(args).filter(t -> !isFiltered(t)).collect(Collectors.toList());
    }

    @SuppressWarnings("rawtypes")
    private static boolean isFiltered(Object o) {
        if (isFilteredByAnnotation(o) || isFilteredByObject(o)) {
            return true;
        }

        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return isFilteredByAnnotation(value) || isFilteredByObject(value);
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.entrySet()) {
                Map.Entry entry = (Map.Entry) value;
                return isFilteredByAnnotation(entry.getValue()) || isFilteredByObject(entry.getValue());
            }
        }
        return false;
    }

    private static boolean isFilteredByAnnotation(Object o) {
        return Objects.isNull(o)
                || o.getClass().isAnnotationPresent(SysLogIgnore.class)
                || o.getClass().isAnnotationPresent(PathVariable.class);
    }

    private static boolean isFilteredByObject(Object o) {
        return o instanceof MultipartFile
                || o instanceof HttpServletRequest
                || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}