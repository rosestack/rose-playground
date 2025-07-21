## 7. 异常处理

### 7.1 异常体系设计

#### 7.1.1 异常分类

在DDD分层架构中，异常按照业务层次和处理方式进行分类：

```java
/**
 * 基础异常类
 */
public abstract class BaseException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;
    private final Object[] args;
    private final Throwable cause;
    
    protected BaseException(String errorCode, String errorMessage, Object... args) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.args = args;
        this.cause = null;
    }
    
    protected BaseException(String errorCode, String errorMessage, Throwable cause, Object... args) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.args = args;
        this.cause = cause;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public Object[] getArgs() {
        return args;
    }
    
    public abstract ExceptionLevel getLevel();
    public abstract boolean isRetryable();
}

/**
 * 异常级别枚举
 */
public enum ExceptionLevel {
    FATAL,      // 致命错误，需要立即处理
    ERROR,      // 错误，影响业务流程
    WARN,       // 警告，不影响主流程
    INFO        // 信息，仅记录
}
```

#### 7.1.2 自定义异常

按照DDD分层架构设计不同层次的异常：

```java
/**
 * 领域异常 - 业务规则违反
 */
public class DomainException extends BaseException {
    
    public DomainException(String errorCode, String errorMessage, Object... args) {
        super(errorCode, errorMessage, args);
    }
    
    public DomainException(String errorCode, String errorMessage, Throwable cause, Object... args) {
        super(errorCode, errorMessage, cause, args);
    }
    
    @Override
    public ExceptionLevel getLevel() {
        return ExceptionLevel.ERROR;
    }
    
    @Override
    public boolean isRetryable() {
        return false; // 业务规则违反通常不可重试
    }
}

/**
 * 应用异常 - 应用层处理异常
 */
public class ApplicationException extends BaseException {
    
    private final boolean retryable;
    
    public ApplicationException(String errorCode, String errorMessage, Object... args) {
        this(errorCode, errorMessage, false, args);
    }
    
    public ApplicationException(String errorCode, String errorMessage, boolean retryable, Object... args) {
        super(errorCode, errorMessage, args);
        this.retryable = retryable;
    }
    
    public ApplicationException(String errorCode, String errorMessage, Throwable cause, Object... args) {
        super(errorCode, errorMessage, cause, args);
        this.retryable = false;
    }
    
    @Override
    public ExceptionLevel getLevel() {
        return ExceptionLevel.ERROR;
    }
    
    @Override
    public boolean isRetryable() {
        return retryable;
    }
}

/**
 * 基础设施异常 - 外部依赖异常
 */
public class InfrastructureException extends BaseException {
    
    public InfrastructureException(String errorCode, String errorMessage, Object... args) {
        super(errorCode, errorMessage, args);
    }
    
    public InfrastructureException(String errorCode, String errorMessage, Throwable cause, Object... args) {
        super(errorCode, errorMessage, cause, args);
    }
    
    @Override
    public ExceptionLevel getLevel() {
        return ExceptionLevel.ERROR;
    }
    
    @Override
    public boolean isRetryable() {
        return true; // 基础设施异常通常可重试
    }
}

/**
 * 验证异常 - 参数验证失败
 */
public class ValidationException extends BaseException {
    
    private final List<FieldError> fieldErrors;
    
    public ValidationException(String errorCode, String errorMessage, List<FieldError> fieldErrors) {
        super(errorCode, errorMessage);
        this.fieldErrors = fieldErrors != null ? fieldErrors : Collections.emptyList();
    }
    
    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
    
    @Override
    public ExceptionLevel getLevel() {
        return ExceptionLevel.WARN;
    }
    
    @Override
    public boolean isRetryable() {
        return false;
    }
    
    public static class FieldError {
        private final String field;
        private final String message;
        private final Object rejectedValue;
        
        public FieldError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }
        
        // getters...
        public String getField() { return field; }
        public String getMessage() { return message; }
        public Object getRejectedValue() { return rejectedValue; }
    }
}

/**
 * 业务异常 - 具体业务场景异常
 */
public class BusinessException extends DomainException {
    
    public BusinessException(String errorCode, String errorMessage, Object... args) {
        super(errorCode, errorMessage, args);
    }
    
    // 用户相关异常
    public static class UserNotFoundException extends BusinessException {
        public UserNotFoundException(Long userId) {
            super("USER_NOT_FOUND", "用户不存在", userId);
        }
    }
    
    public static class UserAlreadyExistsException extends BusinessException {
        public UserAlreadyExistsException(String email) {
            super("USER_ALREADY_EXISTS", "用户已存在", email);
        }
    }
    
    // 订单相关异常
    public static class OrderNotFoundException extends BusinessException {
        public OrderNotFoundException(String orderNo) {
            super("ORDER_NOT_FOUND", "订单不存在", orderNo);
        }
    }
    
    public static class InsufficientStockException extends BusinessException {
        public InsufficientStockException(String productName, int available, int required) {
            super("INSUFFICIENT_STOCK", "库存不足", productName, available, required);
        }
    }
}
```

#### 7.1.3 异常传播

异常在各层之间的传播和转换机制：

```java
/**
 * 异常转换器
 */
@Component
public class ExceptionTranslator {
    
    private static final Logger logger = LoggerFactory.getLogger(ExceptionTranslator.class);
    
    /**
     * 将基础设施异常转换为应用异常
     */
    public ApplicationException translateInfrastructureException(Exception ex) {
        if (ex instanceof DataAccessException) {
            return new ApplicationException("DATA_ACCESS_ERROR", "数据访问异常", true, ex.getMessage());
        }
        
        if (ex instanceof RedisConnectionFailureException) {
            return new ApplicationException("CACHE_ERROR", "缓存服务异常", true, ex.getMessage());
        }
        
        if (ex instanceof HttpClientErrorException) {
            HttpClientErrorException httpEx = (HttpClientErrorException) ex;
            return new ApplicationException("EXTERNAL_SERVICE_ERROR", 
                "外部服务调用失败", true, httpEx.getStatusCode(), httpEx.getResponseBodyAsString());
        }
        
        return new ApplicationException("UNKNOWN_ERROR", "未知异常", false, ex.getMessage());
    }
    
    /**
     * 异常链分析
     */
    public BaseException analyzeExceptionChain(Throwable throwable) {
        Throwable current = throwable;
        
        while (current != null) {
            if (current instanceof BaseException) {
                return (BaseException) current;
            }
            
            // 检查是否为已知的第三方异常
            if (current instanceof ConstraintViolationException) {
                return createValidationException((ConstraintViolationException) current);
            }
            
            if (current instanceof DataIntegrityViolationException) {
                return new ApplicationException("DATA_INTEGRITY_VIOLATION", "数据完整性约束违反");
            }
            
            current = current.getCause();
        }
        
        return new ApplicationException("UNKNOWN_ERROR", "未知异常", throwable.getMessage());
    }
    
    private ValidationException createValidationException(ConstraintViolationException ex) {
        List<ValidationException.FieldError> fieldErrors = ex.getConstraintViolations()
            .stream()
            .map(violation -> new ValidationException.FieldError(
                violation.getPropertyPath().toString(),
                violation.getMessage(),
                violation.getInvalidValue()
            ))
            .collect(Collectors.toList());
            
        return new ValidationException("VALIDATION_ERROR", "参数验证失败", fieldErrors);
    }
}

/**
 * 异常处理切面
 */
@Aspect
@Component
public class ExceptionHandlingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlingAspect.class);
    
    @Autowired
    private ExceptionTranslator exceptionTranslator;
    
    @Around("@within(org.springframework.stereotype.Service)")
    public Object handleServiceExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (BaseException ex) {
            // 业务异常直接抛出
            throw ex;
        } catch (Exception ex) {
            // 转换为应用异常
            BaseException translatedEx = exceptionTranslator.analyzeExceptionChain(ex);
            logger.error("Service exception occurred in {}.{}", 
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(), ex);
            throw translatedEx;
        }
    }
}
```

### 7.2 统一异常处理

#### 7.2.1 全局异常处理器

统一处理应用中的所有异常，提供一致的错误响应：

```java
/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @Autowired
    private MessageSource messageSource;
    
    @Autowired
    private ExceptionMetricsCollector metricsCollector;
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(getLocalizedMessage(ex))
            .traceId(traceId)
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .build();
            
        metricsCollector.recordException(ex);
        
        log.warn("Business exception occurred: code={}, message={}, traceId={}", 
            ex.getErrorCode(), ex.getErrorMessage(), traceId);
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * 处理验证异常
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        
        List<ErrorResponse.FieldError> fieldErrors = ex.getFieldErrors().stream()
            .map(fieldError -> ErrorResponse.FieldError.builder()
                .field(fieldError.getField())
                .message(fieldError.getMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build())
            .collect(Collectors.toList());
            
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(getLocalizedMessage(ex))
            .fieldErrors(fieldErrors)
            .traceId(traceId)
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .build();
            
        metricsCollector.recordException(ex);
        
        log.warn("Validation exception occurred: code={}, fieldErrors={}, traceId={}", 
            ex.getErrorCode(), fieldErrors.size(), traceId);
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * 处理应用异常
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(getLocalizedMessage(ex))
            .traceId(traceId)
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .retryable(ex.isRetryable())
            .build();
            
        metricsCollector.recordException(ex);
        
        log.error("Application exception occurred: code={}, message={}, retryable={}, traceId={}", 
            ex.getErrorCode(), ex.getErrorMessage(), ex.isRetryable(), traceId, ex);
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * 处理基础设施异常
     */
    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ErrorResponse> handleInfrastructureException(InfrastructureException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message("系统暂时不可用，请稍后重试")
            .traceId(traceId)
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .retryable(ex.isRetryable())
            .build();
            
        metricsCollector.recordException(ex);
        
        log.error("Infrastructure exception occurred: code={}, message={}, traceId={}", 
            ex.getErrorCode(), ex.getErrorMessage(), traceId, ex);
            
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }
    
    /**
     * 处理Spring Validation异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> ErrorResponse.FieldError.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build())
            .collect(Collectors.toList());
            
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message("参数验证失败")
            .fieldErrors(fieldErrors)
            .traceId(traceId)
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .build();
            
        log.warn("Method argument validation failed: fieldErrors={}, traceId={}", fieldErrors.size(), traceId);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * 处理未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code("INTERNAL_ERROR")
            .message("系统内部错误")
            .traceId(traceId)
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .build();
            
        metricsCollector.recordUnknownException(ex);
        
        log.error("Unexpected exception occurred: traceId={}", traceId, ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    private String getTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null) {
            traceId = MDC.get("traceId");
        }
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }
    
    private String getLocalizedMessage(BaseException ex) {
        try {
            return messageSource.getMessage(ex.getErrorCode(), ex.getArgs(), LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            return ex.getErrorMessage();
        }
    }
}
```

#### 7.2.2 错误响应格式

统一的错误响应格式定义：

```java
/**
 * 统一错误响应格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    /**
     * 错误码
     */
    private String code;
    
    /**
     * 错误消息
     */
    private String message;
    
    /**
     * 字段验证错误列表
     */
    private List<FieldError> fieldErrors;
    
    /**
     * 请求追踪ID
     */
    private String traceId;
    
    /**
     * 错误发生时间
     */
    private Instant timestamp;
    
    /**
     * 请求路径
     */
    private String path;
    
    /**
     * 是否可重试
     */
    private Boolean retryable;
    
    /**
     * 额外的错误详情
     */
    private Map<String, Object> details;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        /**
         * 字段名
         */
        private String field;
        
        /**
         * 错误消息
         */
        private String message;
        
        /**
         * 被拒绝的值
         */
        private Object rejectedValue;
    }
}

/**
 * 成功响应格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuccessResponse<T> {
    
    /**
     * 响应码
     */
    private String code = "SUCCESS";
    
    /**
     * 响应消息
     */
    private String message = "操作成功";
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 请求追踪ID
     */
    private String traceId;
    
    /**
     * 响应时间
     */
    private Instant timestamp = Instant.now();
    
    public static <T> SuccessResponse<T> of(T data) {
        return SuccessResponse.<T>builder()
            .data(data)
            .build();
    }
    
    public static <T> SuccessResponse<T> of(T data, String message) {
        return SuccessResponse.<T>builder()
            .data(data)
            .message(message)
            .build();
    }
}

/**
 * 响应包装器
 */
@RestControllerAdvice
public class ResponseWrapper implements ResponseBodyAdvice<Object> {
    
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return !returnType.getDeclaringClass().isAnnotationPresent(NoResponseWrapper.class)
            && !returnType.hasMethodAnnotation(NoResponseWrapper.class);
    }
    
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                ServerHttpRequest request, ServerHttpResponse response) {
        
        if (body instanceof ErrorResponse || body instanceof SuccessResponse) {
            return body;
        }
        
        String traceId = getTraceId(request);
        SuccessResponse<Object> successResponse = SuccessResponse.of(body);
        successResponse.setTraceId(traceId);
        
        return successResponse;
    }
    
    private String getTraceId(ServerHttpRequest request) {
        String traceId = request.getHeaders().getFirst("X-Trace-Id");
        if (traceId == null) {
            traceId = MDC.get("traceId");
        }
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }
}

/**
 * 不包装响应的注解
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoResponseWrapper {
}
```

#### 7.2.3 异常日志记录

结构化的异常日志记录：

```java
/**
 * 异常日志记录器
 */
@Component
@Slf4j
public class ExceptionLogger {
    
    private final ObjectMapper objectMapper;
    
    public ExceptionLogger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * 记录异常日志
     */
    public void logException(BaseException exception, HttpServletRequest request) {
        ExceptionLogEntry logEntry = ExceptionLogEntry.builder()
            .traceId(getTraceId(request))
            .exceptionType(exception.getClass().getSimpleName())
            .errorCode(exception.getErrorCode())
            .errorMessage(exception.getErrorMessage())
            .level(exception.getLevel())
            .retryable(exception.isRetryable())
            .requestPath(request.getRequestURI())
            .requestMethod(request.getMethod())
            .userAgent(request.getHeader("User-Agent"))
            .clientIp(getClientIp(request))
            .userId(getCurrentUserId())
            .timestamp(Instant.now())
            .stackTrace(getStackTrace(exception))
            .build();
            
        try {
            String logJson = objectMapper.writeValueAsString(logEntry);
            
            switch (exception.getLevel()) {
                case FATAL:
                    log.error("FATAL_EXCEPTION: {}", logJson);
                    break;
                case ERROR:
                    log.error("ERROR_EXCEPTION: {}", logJson);
                    break;
                case WARN:
                    log.warn("WARN_EXCEPTION: {}", logJson);
                    break;
                case INFO:
                    log.info("INFO_EXCEPTION: {}", logJson);
                    break;
            }
        } catch (Exception e) {
            log.error("Failed to serialize exception log entry", e);
        }
    }
    
    private String getTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        return traceId != null ? traceId : MDC.get("traceId");
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // 忽略获取用户ID的异常
        }
        return null;
    }
    
    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}

/**
 * 异常日志条目
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionLogEntry {
    
    private String traceId;
    private String exceptionType;
    private String errorCode;
    private String errorMessage;
    private ExceptionLevel level;
    private Boolean retryable;
    private String requestPath;
    private String requestMethod;
    private String userAgent;
    private String clientIp;
    private String userId;
    private Instant timestamp;
    private String stackTrace;
}

/**
 * 异常指标收集器
 */
@Component
public class ExceptionMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    private final Counter.Builder exceptionCounter;
    private final Timer.Builder exceptionTimer;
    
    public ExceptionMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.exceptionCounter = Counter.builder("application.exceptions")
            .description("Application exceptions count");
        this.exceptionTimer = Timer.builder("application.exception.duration")
            .description("Exception handling duration");
    }
    
    /**
     * 记录异常指标
     */
    public void recordException(BaseException exception) {
        exceptionCounter
            .tag("type", exception.getClass().getSimpleName())
            .tag("code", exception.getErrorCode())
            .tag("level", exception.getLevel().name())
            .tag("retryable", String.valueOf(exception.isRetryable()))
            .register(meterRegistry)
            .increment();
    }
    
    /**
     * 记录未知异常
     */
    public void recordUnknownException(Exception exception) {
        exceptionCounter
            .tag("type", exception.getClass().getSimpleName())
            .tag("code", "UNKNOWN")
            .tag("level", "ERROR")
            .tag("retryable", "false")
            .register(meterRegistry)
            .increment();
    }
    
    /**
     * 记录异常处理时间
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordTimer(Timer.Sample sample, BaseException exception) {
        sample.stop(exceptionTimer
            .tag("type", exception.getClass().getSimpleName())
            .tag("code", exception.getErrorCode())
            .register(meterRegistry));
    }
}
```

### 7.3 错误码管理

#### 7.3.1 错误码设计

统一的错误码设计规范和管理：

```java
/**
 * 错误码枚举
 */
public enum ErrorCode {
    
    // 系统级错误 (1000-1999)
    SYSTEM_ERROR("1000", "系统错误"),
    INTERNAL_ERROR("1001", "系统内部错误"),
    SERVICE_UNAVAILABLE("1002", "服务不可用"),
    TIMEOUT_ERROR("1003", "请求超时"),
    RATE_LIMIT_EXCEEDED("1004", "请求频率超限"),
    
    // 认证授权错误 (2000-2999)
    UNAUTHORIZED("2000", "未认证"),
    ACCESS_DENIED("2001", "访问被拒绝"),
    TOKEN_EXPIRED("2002", "令牌已过期"),
    TOKEN_INVALID("2003", "令牌无效"),
    PERMISSION_DENIED("2004", "权限不足"),
    
    // 参数验证错误 (3000-3999)
    VALIDATION_ERROR("3000", "参数验证失败"),
    MISSING_PARAMETER("3001", "缺少必需参数"),
    INVALID_PARAMETER("3002", "参数格式错误"),
    PARAMETER_OUT_OF_RANGE("3003", "参数超出范围"),
    
    // 业务逻辑错误 (4000-4999)
    BUSINESS_ERROR("4000", "业务处理失败"),
    RESOURCE_NOT_FOUND("4001", "资源不存在"),
    RESOURCE_ALREADY_EXISTS("4002", "资源已存在"),
    RESOURCE_CONFLICT("4003", "资源冲突"),
    OPERATION_NOT_ALLOWED("4004", "操作不被允许"),
    
    // 用户相关错误 (5000-5999)
    USER_NOT_FOUND("5000", "用户不存在"),
    USER_ALREADY_EXISTS("5001", "用户已存在"),
    USER_DISABLED("5002", "用户已被禁用"),
    PASSWORD_INCORRECT("5003", "密码错误"),
    EMAIL_ALREADY_EXISTS("5004", "邮箱已存在"),
    
    // 订单相关错误 (6000-6999)
    ORDER_NOT_FOUND("6000", "订单不存在"),
    ORDER_STATUS_INVALID("6001", "订单状态无效"),
    INSUFFICIENT_STOCK("6002", "库存不足"),
    PAYMENT_FAILED("6003", "支付失败"),
    ORDER_CANNOT_CANCEL("6004", "订单无法取消"),
    
    // 数据访问错误 (7000-7999)
    DATA_ACCESS_ERROR("7000", "数据访问错误"),
    DATABASE_CONNECTION_ERROR("7001", "数据库连接错误"),
    DATA_INTEGRITY_VIOLATION("7002", "数据完整性约束违反"),
    OPTIMISTIC_LOCK_ERROR("7003", "乐观锁冲突"),
    
    // 外部服务错误 (8000-8999)
    EXTERNAL_SERVICE_ERROR("8000", "外部服务错误"),
    PAYMENT_SERVICE_ERROR("8001", "支付服务错误"),
    SMS_SERVICE_ERROR("8002", "短信服务错误"),
    EMAIL_SERVICE_ERROR("8003", "邮件服务错误"),
    
    // 缓存相关错误 (9000-9999)
    CACHE_ERROR("9000", "缓存错误"),
    CACHE_CONNECTION_ERROR("9001", "缓存连接错误"),
    CACHE_SERIALIZATION_ERROR("9002", "缓存序列化错误");
    
    private final String code;
    private final String message;
    
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public static ErrorCode fromCode(String code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("Unknown error code: " + code);
    }
}

/**
 * 错误码管理器
 */
@Component
public class ErrorCodeManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ErrorCodeManager.class);
    
    @Autowired
    private MessageSource messageSource;
    
    private final Map<String, ErrorCodeInfo> errorCodeRegistry = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initializeErrorCodes() {
        // 注册所有错误码
        for (ErrorCode errorCode : ErrorCode.values()) {
            registerErrorCode(errorCode.getCode(), errorCode.getMessage(), 
                ErrorLevel.ERROR, false, errorCode.name());
        }
        
        logger.info("Initialized {} error codes", errorCodeRegistry.size());
    }
    
    /**
     * 注册错误码
     */
    public void registerErrorCode(String code, String defaultMessage, 
                                ErrorLevel level, boolean retryable, String category) {
        ErrorCodeInfo info = ErrorCodeInfo.builder()
            .code(code)
            .defaultMessage(defaultMessage)
            .level(level)
            .retryable(retryable)
            .category(category)
            .registeredAt(Instant.now())
            .build();
            
        errorCodeRegistry.put(code, info);
    }
    
    /**
     * 获取错误码信息
     */
    public ErrorCodeInfo getErrorCodeInfo(String code) {
        return errorCodeRegistry.get(code);
    }
    
    /**
     * 获取本地化错误消息
     */
    public String getLocalizedMessage(String code, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException e) {
            ErrorCodeInfo info = errorCodeRegistry.get(code);
            return info != null ? info.getDefaultMessage() : "Unknown error";
        }
    }
    
    /**
     * 验证错误码是否存在
     */
    public boolean isValidErrorCode(String code) {
        return errorCodeRegistry.containsKey(code);
    }
    
    /**
     * 获取所有错误码
     */
    public Collection<ErrorCodeInfo> getAllErrorCodes() {
        return errorCodeRegistry.values();
    }
    
    /**
     * 按类别获取错误码
     */
    public List<ErrorCodeInfo> getErrorCodesByCategory(String category) {
        return errorCodeRegistry.values().stream()
            .filter(info -> category.equals(info.getCategory()))
            .collect(Collectors.toList());
    }
}

/**
 * 错误码信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorCodeInfo {
    
    private String code;
    private String defaultMessage;
    private ExceptionLevel level;
    private Boolean retryable;
    private String category;
    private Instant registeredAt;
    private String description;
    private List<String> examples;
}
```

#### 7.3.2 国际化支持

多语言错误消息支持：

```java
/**
 * 国际化配置
 */
@Configuration
public class InternationalizationConfig {
    
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages/errors");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600);
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        return messageSource;
    }
    
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(Arrays.asList(
            Locale.SIMPLIFIED_CHINESE,
            Locale.ENGLISH,
            Locale.JAPANESE
        ));
        resolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        return resolver;
    }
    
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}

/**
 * 国际化消息服务
 */
@Service
public class I18nMessageService {
    
    @Autowired
    private MessageSource messageSource;
    
    /**
     * 获取国际化消息
     */
    public String getMessage(String code, Object... args) {
        return getMessage(code, args, LocaleContextHolder.getLocale());
    }
    
    /**
     * 获取指定语言的消息
     */
    public String getMessage(String code, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException e) {
            return code; // 返回错误码作为默认值
        }
    }
    
    /**
     * 获取多语言消息映射
     */
    public Map<String, String> getMultiLanguageMessages(String code, Object... args) {
        Map<String, String> messages = new HashMap<>();
        
        // 支持的语言列表
        List<Locale> supportedLocales = Arrays.asList(
            Locale.SIMPLIFIED_CHINESE,
            Locale.ENGLISH,
            Locale.JAPANESE
        );
        
        for (Locale locale : supportedLocales) {
            try {
                String message = messageSource.getMessage(code, args, locale);
                messages.put(locale.getLanguage(), message);
            } catch (NoSuchMessageException e) {
                messages.put(locale.getLanguage(), code);
            }
        }
        
        return messages;
    }
    
    /**
     * 检查消息是否存在
     */
    public boolean hasMessage(String code, Locale locale) {
        try {
            messageSource.getMessage(code, null, locale);
            return true;
        } catch (NoSuchMessageException e) {
            return false;
        }
    }
}

/**
 * 错误消息构建器
 */
@Component
public class ErrorMessageBuilder {
    
    @Autowired
    private I18nMessageService i18nMessageService;
    
    /**
     * 构建错误消息
     */
    public String buildErrorMessage(String errorCode, Object... args) {
        return i18nMessageService.getMessage(errorCode, args);
    }
    
    /**
     * 构建详细错误消息
     */
    public ErrorMessage buildDetailedErrorMessage(String errorCode, Object... args) {
        String message = i18nMessageService.getMessage(errorCode, args);
        Map<String, String> multiLanguageMessages = i18nMessageService.getMultiLanguageMessages(errorCode, args);
        
        return ErrorMessage.builder()
            .code(errorCode)
            .message(message)
            .multiLanguageMessages(multiLanguageMessages)
            .timestamp(Instant.now())
            .build();
    }
}

/**
 * 错误消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessage {
    
    private String code;
    private String message;
    private Map<String, String> multiLanguageMessages;
    private Instant timestamp;
}
```

#### 7.3.3 错误码维护

错误码的维护和管理工具：

```java
/**
 * 错误码维护服务
 */
@Service
public class ErrorCodeMaintenanceService {
    
    private static final Logger logger = LoggerFactory.getLogger(ErrorCodeMaintenanceService.class);
    
    @Autowired
    private ErrorCodeManager errorCodeManager;
    
    @Autowired
    private I18nMessageService i18nMessageService;
    
    /**
     * 验证错误码完整性
     */
    public ErrorCodeValidationReport validateErrorCodes() {
        ErrorCodeValidationReport report = new ErrorCodeValidationReport();
        
        Collection<ErrorCodeInfo> allErrorCodes = errorCodeManager.getAllErrorCodes();
        
        for (ErrorCodeInfo errorCode : allErrorCodes) {
            validateSingleErrorCode(errorCode, report);
        }
        
        logger.info("Error code validation completed. Total: {}, Valid: {}, Invalid: {}", 
            report.getTotalCount(), report.getValidCount(), report.getInvalidCount());
            
        return report;
    }
    
    private void validateSingleErrorCode(ErrorCodeInfo errorCode, ErrorCodeValidationReport report) {
        report.incrementTotal();
        
        List<String> issues = new ArrayList<>();
        
        // 检查错误码格式
        if (!isValidErrorCodeFormat(errorCode.getCode())) {
            issues.add("Invalid error code format");
        }
        
        // 检查国际化消息
        List<Locale> supportedLocales = Arrays.asList(
            Locale.SIMPLIFIED_CHINESE,
            Locale.ENGLISH,
            Locale.JAPANESE
        );
        
        for (Locale locale : supportedLocales) {
            if (!i18nMessageService.hasMessage(errorCode.getCode(), locale)) {
                issues.add("Missing message for locale: " + locale.getLanguage());
            }
        }
        
        if (issues.isEmpty()) {
            report.incrementValid();
        } else {
            report.incrementInvalid();
            report.addInvalidErrorCode(errorCode.getCode(), issues);
        }
    }
    
    private boolean isValidErrorCodeFormat(String code) {
        // 错误码应该是4位数字
        return code != null && code.matches("\\d{4}");
    }
    
    /**
     * 生成错误码文档
     */
    public String generateErrorCodeDocumentation() {
        StringBuilder doc = new StringBuilder();
        doc.append("# 错误码文档\n\n");
        
        Map<String, List<ErrorCodeInfo>> categorizedCodes = errorCodeManager.getAllErrorCodes()
            .stream()
            .collect(Collectors.groupingBy(ErrorCodeInfo::getCategory));
            
        for (Map.Entry<String, List<ErrorCodeInfo>> entry : categorizedCodes.entrySet()) {
            doc.append("## ").append(entry.getKey()).append("\n\n");
            doc.append("| 错误码 | 默认消息 | 级别 | 可重试 | 描述 |\n");
            doc.append("|--------|----------|------|--------|------|\n");
            
            for (ErrorCodeInfo errorCode : entry.getValue()) {
                doc.append("| ").append(errorCode.getCode())
                   .append(" | ").append(errorCode.getDefaultMessage())
                   .append(" | ").append(errorCode.getLevel())
                   .append(" | ").append(errorCode.getRetryable())
                   .append(" | ").append(errorCode.getDescription() != null ? errorCode.getDescription() : "")
                   .append(" |\n");
            }
            
            doc.append("\n");
        }
        
        return doc.toString();
    }
    
    /**
     * 导出错误码配置
     */
    public void exportErrorCodeConfiguration(String filePath) throws IOException {
        List<ErrorCodeExport> exports = errorCodeManager.getAllErrorCodes()
            .stream()
            .map(this::convertToExport)
            .collect(Collectors.toList());
            
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File(filePath), exports);
        
        logger.info("Error codes exported to: {}", filePath);
    }
    
    private ErrorCodeExport convertToExport(ErrorCodeInfo errorCode) {
        return ErrorCodeExport.builder()
            .code(errorCode.getCode())
            .defaultMessage(errorCode.getDefaultMessage())
            .level(errorCode.getLevel().name())
            .retryable(errorCode.getRetryable())
            .category(errorCode.getCategory())
            .description(errorCode.getDescription())
            .examples(errorCode.getExamples())
            .build();
    }
}

/**
 * 错误码验证报告
 */
@Data
public class ErrorCodeValidationReport {
    
    private int totalCount = 0;
    private int validCount = 0;
    private int invalidCount = 0;
    private Map<String, List<String>> invalidErrorCodes = new HashMap<>();
    
    public void incrementTotal() {
        totalCount++;
    }
    
    public void incrementValid() {
        validCount++;
    }
    
    public void incrementInvalid() {
        invalidCount++;
    }
    
    public void addInvalidErrorCode(String code, List<String> issues) {
        invalidErrorCodes.put(code, issues);
    }
    
    public boolean isValid() {
        return invalidCount == 0;
    }
}

/**
 * 错误码导出格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorCodeExport {
    
    private String code;
    private String defaultMessage;
    private String level;
    private Boolean retryable;
    private String category;
    private String description;
    private List<String> examples;
}

/**
 * 错误码管理控制器
 */
@RestController
@RequestMapping("/admin/error-codes")
@PreAuthorize("hasRole('ADMIN')")
public class ErrorCodeManagementController {
    
    @Autowired
    private ErrorCodeMaintenanceService maintenanceService;
    
    @Autowired
    private ErrorCodeManager errorCodeManager;
    
    /**
     * 获取所有错误码
     */
    @GetMapping
    public ResponseEntity<List<ErrorCodeInfo>> getAllErrorCodes() {
        List<ErrorCodeInfo> errorCodes = new ArrayList<>(errorCodeManager.getAllErrorCodes());
        return ResponseEntity.ok(errorCodes);
    }
    
    /**
     * 验证错误码
     */
    @PostMapping("/validate")
    public ResponseEntity<ErrorCodeValidationReport> validateErrorCodes() {
        ErrorCodeValidationReport report = maintenanceService.validateErrorCodes();
        return ResponseEntity.ok(report);
    }
    
    /**
     * 生成错误码文档
     */
    @GetMapping("/documentation")
    public ResponseEntity<String> generateDocumentation() {
        String documentation = maintenanceService.generateErrorCodeDocumentation();
        return ResponseEntity.ok(documentation);
    }
    
    /**
     * 导出错误码配置
     */
    @PostMapping("/export")
    public ResponseEntity<String> exportConfiguration(@RequestParam String filePath) {
        try {
            maintenanceService.exportErrorCodeConfiguration(filePath);
            return ResponseEntity.ok("Error codes exported successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Export failed: " + e.getMessage());
        }
    }
}
```

### 7.4 异常监控

#### 7.4.1 异常指标收集

全面的异常指标收集和统计：

```java
/**
 * 异常指标收集器
 */
@Component
public class ExceptionMetricsCollector {
    
    private static final Logger logger = LoggerFactory.getLogger(ExceptionMetricsCollector.class);
    
    private final MeterRegistry meterRegistry;
    private final Counter totalExceptionCounter;
    private final Map<String, Counter> exceptionCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> exceptionTimers = new ConcurrentHashMap<>();
    private final Map<String, Gauge> exceptionRateGauges = new ConcurrentHashMap<>();
    
    // 异常统计数据
    private final AtomicLong totalExceptions = new AtomicLong(0);
    private final Map<String, AtomicLong> exceptionCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> exceptionRates = new ConcurrentHashMap<>();
    
    public ExceptionMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.totalExceptionCounter = Counter.builder("exceptions.total")
            .description("Total number of exceptions")
            .register(meterRegistry);
    }
    
    /**
     * 记录异常
     */
    public void recordException(String exceptionType, String errorCode, 
                              ExceptionLevel level, long duration) {
        // 总异常计数
        totalExceptionCounter.increment();
        totalExceptions.incrementAndGet();
        
        // 按类型计数
        String counterKey = "exceptions.by_type." + exceptionType;
        Counter counter = exceptionCounters.computeIfAbsent(counterKey, key ->
            Counter.builder(key)
                .tag("type", exceptionType)
                .tag("level", level.name())
                .description("Exception count by type")
                .register(meterRegistry)
        );
        counter.increment();
        
        // 按错误码计数
        String errorCodeKey = "exceptions.by_error_code." + errorCode;
        Counter errorCodeCounter = exceptionCounters.computeIfAbsent(errorCodeKey, key ->
            Counter.builder(key)
                .tag("error_code", errorCode)
                .tag("level", level.name())
                .description("Exception count by error code")
                .register(meterRegistry)
        );
        errorCodeCounter.increment();
        
        // 处理时间统计
        String timerKey = "exceptions.duration." + exceptionType;
        Timer timer = exceptionTimers.computeIfAbsent(timerKey, key ->
            Timer.builder(key)
                .tag("type", exceptionType)
                .description("Exception handling duration")
                .register(meterRegistry)
        );
        timer.record(duration, TimeUnit.MILLISECONDS);
        
        // 更新统计数据
        exceptionCounts.computeIfAbsent(exceptionType, k -> new AtomicLong(0)).incrementAndGet();
        
        logger.debug("Recorded exception: type={}, errorCode={}, level={}, duration={}ms", 
            exceptionType, errorCode, level, duration);
    }
    
    /**
     * 获取异常统计
     */
    public ExceptionStatistics getExceptionStatistics() {
        return ExceptionStatistics.builder()
            .totalExceptions(totalExceptions.get())
            .exceptionsByType(getExceptionCountsByType())
            .exceptionRates(calculateExceptionRates())
            .topExceptions(getTopExceptions(10))
            .timestamp(Instant.now())
            .build();
    }
    
    private Map<String, Long> getExceptionCountsByType() {
        return exceptionCounts.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().get()
            ));
    }
    
    private Map<String, Double> calculateExceptionRates() {
        long totalCount = totalExceptions.get();
        if (totalCount == 0) {
            return Collections.emptyMap();
        }
        
        return exceptionCounts.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> (double) entry.getValue().get() / totalCount * 100
            ));
    }
    
    private List<ExceptionSummary> getTopExceptions(int limit) {
        return exceptionCounts.entrySet().stream()
            .sorted(Map.Entry.<String, AtomicLong>comparingByValue(
                (a, b) -> Long.compare(b.get(), a.get())))
            .limit(limit)
            .map(entry -> ExceptionSummary.builder()
                .type(entry.getKey())
                .count(entry.getValue().get())
                .rate(calculateExceptionRates().get(entry.getKey()))
                .build())
            .collect(Collectors.toList());
    }
    
    /**
     * 重置统计数据
     */
    public void resetStatistics() {
        totalExceptions.set(0);
        exceptionCounts.clear();
        exceptionRates.clear();
        logger.info("Exception statistics reset");
    }
}

/**
 * 异常统计数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionStatistics {
    
    private Long totalExceptions;
    private Map<String, Long> exceptionsByType;
    private Map<String, Double> exceptionRates;
    private List<ExceptionSummary> topExceptions;
    private Instant timestamp;
}

/**
 * 异常摘要
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionSummary {
    
    private String type;
    private Long count;
    private Double rate;
}

/**
 * 异常监控配置
 */
@Configuration
@EnableConfigurationProperties(ExceptionMonitoringProperties.class)
public class ExceptionMonitoringConfig {
    
    @Bean
    @ConditionalOnMissingBean
    public ExceptionMetricsCollector exceptionMetricsCollector(MeterRegistry meterRegistry) {
        return new ExceptionMetricsCollector(meterRegistry);
    }
    
    @Bean
    public ExceptionMonitoringAspect exceptionMonitoringAspect(
            ExceptionMetricsCollector metricsCollector,
            ExceptionMonitoringProperties properties) {
        return new ExceptionMonitoringAspect(metricsCollector, properties);
    }
}

/**
 * 异常监控属性配置
 */
@Data
@ConfigurationProperties(prefix = "exception.monitoring")
public class ExceptionMonitoringProperties {
    
    /**
     * 是否启用异常监控
     */
    private boolean enabled = true;
    
    /**
     * 异常阈值配置
     */
    private ThresholdConfig threshold = new ThresholdConfig();
    
    /**
     * 告警配置
     */
    private AlertConfig alert = new AlertConfig();
    
    @Data
    public static class ThresholdConfig {
        /**
         * 异常率阈值（百分比）
         */
        private double errorRate = 5.0;
        
        /**
         * 异常数量阈值
         */
        private long errorCount = 100;
        
        /**
         * 时间窗口（分钟）
         */
        private int timeWindow = 5;
    }
    
    @Data
    public static class AlertConfig {
        /**
         * 是否启用告警
         */
        private boolean enabled = true;
        
        /**
         * 告警接收者
         */
        private List<String> recipients = new ArrayList<>();
        
        /**
         * 告警冷却时间（分钟）
         */
        private int cooldownMinutes = 10;
    }
}
```

#### 7.4.2 监控告警

异常监控告警机制：

```java
/**
 * 异常告警服务
 */
@Service
public class ExceptionAlertService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAlertService.class);
    
    @Autowired
    private ExceptionMetricsCollector metricsCollector;
    
    @Autowired
    private ExceptionMonitoringProperties properties;
    
    @Autowired
    private NotificationService notificationService;
    
    private final Map<String, Instant> lastAlertTimes = new ConcurrentHashMap<>();
    
    /**
     * 检查异常阈值并发送告警
     */
    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void checkExceptionThresholds() {
        if (!properties.isEnabled() || !properties.getAlert().isEnabled()) {
            return;
        }
        
        ExceptionStatistics statistics = metricsCollector.getExceptionStatistics();
        
        // 检查总异常数量
        checkTotalExceptionCount(statistics);
        
        // 检查异常率
        checkExceptionRates(statistics);
        
        // 检查特定异常类型
        checkSpecificExceptions(statistics);
    }
    
    private void checkTotalExceptionCount(ExceptionStatistics statistics) {
        long threshold = properties.getThreshold().getErrorCount();
        long totalExceptions = statistics.getTotalExceptions();
        
        if (totalExceptions > threshold) {
            String alertKey = "total_exceptions";
            if (shouldSendAlert(alertKey)) {
                ExceptionAlert alert = ExceptionAlert.builder()
                    .type(ExceptionAlertType.HIGH_EXCEPTION_COUNT)
                    .title("异常数量过高告警")
                    .message(String.format("系统异常总数 %d 超过阈值 %d", totalExceptions, threshold))
                    .severity(AlertSeverity.HIGH)
                    .statistics(statistics)
                    .timestamp(Instant.now())
                    .build();
                    
                sendAlert(alert);
                updateLastAlertTime(alertKey);
            }
        }
    }
    
    private void checkExceptionRates(ExceptionStatistics statistics) {
        double threshold = properties.getThreshold().getErrorRate();
        
        statistics.getExceptionRates().forEach((type, rate) -> {
            if (rate > threshold) {
                String alertKey = "exception_rate_" + type;
                if (shouldSendAlert(alertKey)) {
                    ExceptionAlert alert = ExceptionAlert.builder()
                        .type(ExceptionAlertType.HIGH_EXCEPTION_RATE)
                        .title("异常率过高告警")
                        .message(String.format("异常类型 %s 的异常率 %.2f%% 超过阈值 %.2f%%", 
                            type, rate, threshold))
                        .severity(AlertSeverity.MEDIUM)
                        .statistics(statistics)
                        .timestamp(Instant.now())
                        .build();
                        
                    sendAlert(alert);
                    updateLastAlertTime(alertKey);
                }
            }
        });
    }
    
    private void checkSpecificExceptions(ExceptionStatistics statistics) {
        // 检查特定的关键异常
        List<String> criticalExceptions = Arrays.asList(
            "DatabaseConnectionException",
            "PaymentServiceException",
            "SecurityException"
        );
        
        statistics.getExceptionsByType().forEach((type, count) -> {
            if (criticalExceptions.contains(type) && count > 0) {
                String alertKey = "critical_exception_" + type;
                if (shouldSendAlert(alertKey)) {
                    ExceptionAlert alert = ExceptionAlert.builder()
                        .type(ExceptionAlertType.CRITICAL_EXCEPTION)
                        .title("关键异常告警")
                        .message(String.format("检测到关键异常 %s，数量: %d", type, count))
                        .severity(AlertSeverity.CRITICAL)
                        .statistics(statistics)
                        .timestamp(Instant.now())
                        .build();
                        
                    sendAlert(alert);
                    updateLastAlertTime(alertKey);
                }
            }
        });
    }
    
    private boolean shouldSendAlert(String alertKey) {
        Instant lastAlertTime = lastAlertTimes.get(alertKey);
        if (lastAlertTime == null) {
            return true;
        }
        
        int cooldownMinutes = properties.getAlert().getCooldownMinutes();
        return lastAlertTime.isBefore(Instant.now().minus(cooldownMinutes, ChronoUnit.MINUTES));
    }
    
    private void sendAlert(ExceptionAlert alert) {
        try {
            notificationService.sendAlert(alert);
            logger.info("Exception alert sent: {}", alert.getTitle());
        } catch (Exception e) {
            logger.error("Failed to send exception alert", e);
        }
    }
    
    private void updateLastAlertTime(String alertKey) {
        lastAlertTimes.put(alertKey, Instant.now());
    }
}

/**
 * 异常告警实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionAlert {
    
    private ExceptionAlertType type;
    private String title;
    private String message;
    private AlertSeverity severity;
    private ExceptionStatistics statistics;
    private Instant timestamp;
}

/**
 * 异常告警类型
 */
public enum ExceptionAlertType {
    HIGH_EXCEPTION_COUNT,
    HIGH_EXCEPTION_RATE,
    CRITICAL_EXCEPTION,
    SYSTEM_UNAVAILABLE
}

/**
 * 告警严重级别
 */
public enum AlertSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * 通知服务
 */
@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private ExceptionMonitoringProperties properties;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SlackService slackService;
    
    /**
     * 发送告警通知
     */
    public void sendAlert(ExceptionAlert alert) {
        List<String> recipients = properties.getAlert().getRecipients();
        
        if (recipients.isEmpty()) {
            logger.warn("No alert recipients configured");
            return;
        }
        
        String subject = String.format("[%s] %s", alert.getSeverity(), alert.getTitle());
        String content = buildAlertContent(alert);
        
        // 发送邮件通知
        for (String recipient : recipients) {
            if (recipient.contains("@")) {
                emailService.sendAlert(recipient, subject, content);
            }
        }
        
        // 发送Slack通知
        slackService.sendAlert(alert);
        
        logger.info("Alert notification sent to {} recipients", recipients.size());
    }
    
    private String buildAlertContent(ExceptionAlert alert) {
        StringBuilder content = new StringBuilder();
        content.append("告警详情:\n");
        content.append("类型: ").append(alert.getType()).append("\n");
        content.append("严重级别: ").append(alert.getSeverity()).append("\n");
        content.append("消息: ").append(alert.getMessage()).append("\n");
        content.append("时间: ").append(alert.getTimestamp()).append("\n\n");
        
        if (alert.getStatistics() != null) {
            content.append("统计信息:\n");
            content.append("总异常数: ").append(alert.getStatistics().getTotalExceptions()).append("\n");
            
            if (!alert.getStatistics().getTopExceptions().isEmpty()) {
                content.append("Top异常:\n");
                alert.getStatistics().getTopExceptions().forEach(exception ->
                    content.append("- ").append(exception.getType())
                           .append(": ").append(exception.getCount())
                           .append(" (").append(String.format("%.2f%%", exception.getRate())).append(")\n")
                );
            }
        }
        
        return content.toString();
    }
}

/**
 * 异常监控切面
 */
@Aspect
@Component
public class ExceptionMonitoringAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(ExceptionMonitoringAspect.class);
    
    private final ExceptionMetricsCollector metricsCollector;
    private final ExceptionMonitoringProperties properties;
    
    public ExceptionMonitoringAspect(ExceptionMetricsCollector metricsCollector,
                                   ExceptionMonitoringProperties properties) {
        this.metricsCollector = metricsCollector;
        this.properties = properties;
    }
    
    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object monitorControllerExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isEnabled()) {
            return joinPoint.proceed();
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            String exceptionType = e.getClass().getSimpleName();
            String errorCode = extractErrorCode(e);
            ExceptionLevel level = determineExceptionLevel(e);
            
            metricsCollector.recordException(exceptionType, errorCode, level, duration);
            
            throw e;
        }
    }
    
    private String extractErrorCode(Exception e) {
        if (e instanceof BaseException) {
            return ((BaseException) e).getErrorCode();
        }
        return "UNKNOWN";
    }
    
    private ExceptionLevel determineExceptionLevel(Exception e) {
        if (e instanceof BusinessException) {
            return ExceptionLevel.WARN;
        } else if (e instanceof ValidationException) {
            return ExceptionLevel.INFO;
        } else if (e instanceof InfrastructureException) {
            return ExceptionLevel.ERROR;
        } else {
            return ExceptionLevel.ERROR;
        }
    }
}
```

#### 7.4.3 异常分析

异常数据分析和报告：

```java
/**
 * 异常分析服务
 */
@Service
public class ExceptionAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAnalysisService.class);
    
    @Autowired
    private ExceptionMetricsCollector metricsCollector;
    
    @Autowired
    private ExceptionRepository exceptionRepository;
    
    /**
     * 生成异常分析报告
     */
    public ExceptionAnalysisReport generateAnalysisReport(LocalDate startDate, LocalDate endDate) {
        logger.info("Generating exception analysis report from {} to {}", startDate, endDate);
        
        // 获取时间范围内的异常数据
        List<ExceptionRecord> exceptions = exceptionRepository.findByDateRange(startDate, endDate);
        
        // 分析异常趋势
        ExceptionTrendAnalysis trendAnalysis = analyzeTrend(exceptions);
        
        // 分析异常分布
        ExceptionDistributionAnalysis distributionAnalysis = analyzeDistribution(exceptions);
        
        // 分析异常根因
        RootCauseAnalysis rootCauseAnalysis = analyzeRootCause(exceptions);
        
        // 生成改进建议
        List<ImprovementSuggestion> suggestions = generateImprovementSuggestions(
            trendAnalysis, distributionAnalysis, rootCauseAnalysis);
        
        return ExceptionAnalysisReport.builder()
            .reportPeriod(ReportPeriod.of(startDate, endDate))
            .trendAnalysis(trendAnalysis)
            .distributionAnalysis(distributionAnalysis)
            .rootCauseAnalysis(rootCauseAnalysis)
            .improvementSuggestions(suggestions)
            .generatedAt(Instant.now())
            .build();
    }
    
    private ExceptionTrendAnalysis analyzeTrend(List<ExceptionRecord> exceptions) {
        // 按日期分组统计
        Map<LocalDate, Long> dailyCounts = exceptions.stream()
            .collect(Collectors.groupingBy(
                record -> record.getOccurredAt().toLocalDate(),
                Collectors.counting()
            ));
        
        // 计算趋势
        List<LocalDate> dates = dailyCounts.keySet().stream()
            .sorted()
            .collect(Collectors.toList());
        
        double trend = calculateTrend(dates, dailyCounts);
        
        return ExceptionTrendAnalysis.builder()
            .dailyCounts(dailyCounts)
            .trend(trend)
            .peakDate(findPeakDate(dailyCounts))
            .averageDailyCount(calculateAverageDailyCount(dailyCounts))
            .build();
    }
    
    private ExceptionDistributionAnalysis analyzeDistribution(List<ExceptionRecord> exceptions) {
        // 按异常类型分布
        Map<String, Long> typeDistribution = exceptions.stream()
            .collect(Collectors.groupingBy(
                ExceptionRecord::getExceptionType,
                Collectors.counting()
            ));
        
        // 按错误码分布
        Map<String, Long> errorCodeDistribution = exceptions.stream()
            .collect(Collectors.groupingBy(
                ExceptionRecord::getErrorCode,
                Collectors.counting()
            ));
        
        // 按服务分布
        Map<String, Long> serviceDistribution = exceptions.stream()
            .collect(Collectors.groupingBy(
                ExceptionRecord::getServiceName,
                Collectors.counting()
            ));
        
        return ExceptionDistributionAnalysis.builder()
            .typeDistribution(typeDistribution)
            .errorCodeDistribution(errorCodeDistribution)
            .serviceDistribution(serviceDistribution)
            .build();
    }
    
    private RootCauseAnalysis analyzeRootCause(List<ExceptionRecord> exceptions) {
        // 分析异常堆栈，找出常见的根因
        Map<String, Long> rootCauses = exceptions.stream()
            .map(this::extractRootCause)
            .collect(Collectors.groupingBy(
                Function.identity(),
                Collectors.counting()
            ));
        
        // 分析异常关联性
        Map<String, List<String>> correlations = analyzeExceptionCorrelations(exceptions);
        
        return RootCauseAnalysis.builder()
            .rootCauses(rootCauses)
            .correlations(correlations)
            .build();
    }
    
    private String extractRootCause(ExceptionRecord exception) {
        String stackTrace = exception.getStackTrace();
        if (stackTrace == null || stackTrace.isEmpty()) {
            return "Unknown";
        }
        
        // 简化的根因提取逻辑
        String[] lines = stackTrace.split("\n");
        for (String line : lines) {
            if (line.contains("at com.example")) {
                return line.trim();
            }
        }
        
        return lines.length > 0 ? lines[0].trim() : "Unknown";
    }
    
    private Map<String, List<String>> analyzeExceptionCorrelations(List<ExceptionRecord> exceptions) {
        // 分析在相近时间内发生的异常，找出可能的关联性
        Map<String, List<String>> correlations = new HashMap<>();
        
        // 按时间窗口分组
        Map<String, List<ExceptionRecord>> timeWindows = exceptions.stream()
            .collect(Collectors.groupingBy(record -> 
                record.getOccurredAt().truncatedTo(ChronoUnit.MINUTES).toString()
            ));
        
        timeWindows.values().forEach(windowExceptions -> {
            if (windowExceptions.size() > 1) {
                Set<String> types = windowExceptions.stream()
                    .map(ExceptionRecord::getExceptionType)
                    .collect(Collectors.toSet());
                
                if (types.size() > 1) {
                    types.forEach(type -> {
                        List<String> relatedTypes = types.stream()
                            .filter(t -> !t.equals(type))
                            .collect(Collectors.toList());
                        correlations.merge(type, relatedTypes, (existing, newList) -> {
                            existing.addAll(newList);
                            return existing;
                        });
                    });
                }
            }
        });
        
        return correlations;
    }
    
    private List<ImprovementSuggestion> generateImprovementSuggestions(
            ExceptionTrendAnalysis trendAnalysis,
            ExceptionDistributionAnalysis distributionAnalysis,
            RootCauseAnalysis rootCauseAnalysis) {
        
        List<ImprovementSuggestion> suggestions = new ArrayList<>();
        
        // 基于趋势分析的建议
        if (trendAnalysis.getTrend() > 0.1) {
            suggestions.add(ImprovementSuggestion.builder()
                .category("趋势")
                .priority(SuggestionPriority.HIGH)
                .title("异常数量呈上升趋势")
                .description("系统异常数量持续增长，建议立即调查根本原因")
                .actionItems(Arrays.asList(
                    "检查最近的代码变更",
                    "分析系统资源使用情况",
                    "审查外部依赖服务状态"
                ))
                .build());
        }
        
        // 基于分布分析的建议
        distributionAnalysis.getTypeDistribution().entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(3)
            .forEach(entry -> {
                suggestions.add(ImprovementSuggestion.builder()
                    .category("分布")
                    .priority(SuggestionPriority.MEDIUM)
                    .title("高频异常类型: " + entry.getKey())
                    .description(String.format("异常类型 %s 出现 %d 次，占比较高", 
                        entry.getKey(), entry.getValue()))
                    .actionItems(Arrays.asList(
                        "优化 " + entry.getKey() + " 的处理逻辑",
                        "增加预防性检查",
                        "改进错误处理机制"
                    ))
                    .build());
            });
        
        return suggestions;
    }
    
    private double calculateTrend(List<LocalDate> dates, Map<LocalDate, Long> dailyCounts) {
        if (dates.size() < 2) {
            return 0.0;
        }
        
        // 简单的线性趋势计算
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = dates.size();
        
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = dailyCounts.getOrDefault(dates.get(i), 0L);
            
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        
        return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    }
    
    private LocalDate findPeakDate(Map<LocalDate, Long> dailyCounts) {
        return dailyCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    private double calculateAverageDailyCount(Map<LocalDate, Long> dailyCounts) {
        return dailyCounts.values().stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
    }
}

/**
 * 异常分析报告
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionAnalysisReport {
    
    private ReportPeriod reportPeriod;
    private ExceptionTrendAnalysis trendAnalysis;
    private ExceptionDistributionAnalysis distributionAnalysis;
    private RootCauseAnalysis rootCauseAnalysis;
    private List<ImprovementSuggestion> improvementSuggestions;
    private Instant generatedAt;
}

/**
 * 异常趋势分析
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionTrendAnalysis {
    
    private Map<LocalDate, Long> dailyCounts;
    private Double trend;
    private LocalDate peakDate;
    private Double averageDailyCount;
}

/**
 * 异常分布分析
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionDistributionAnalysis {
    
    private Map<String, Long> typeDistribution;
    private Map<String, Long> errorCodeDistribution;
    private Map<String, Long> serviceDistribution;
}

/**
 * 根因分析
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RootCauseAnalysis {
    
    private Map<String, Long> rootCauses;
    private Map<String, List<String>> correlations;
}

/**
 * 改进建议
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImprovementSuggestion {
    
    private String category;
    private SuggestionPriority priority;
    private String title;
    private String description;
    private List<String> actionItems;
}

/**
 * 建议优先级
 */
public enum SuggestionPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * 报告周期
 */
@Data
@AllArgsConstructor
public class ReportPeriod {
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    public static ReportPeriod of(LocalDate startDate, LocalDate endDate) {
        return new ReportPeriod(startDate, endDate);
    }
}
```