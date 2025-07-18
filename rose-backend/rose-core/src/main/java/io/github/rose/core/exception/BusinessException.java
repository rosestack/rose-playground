package io.github.rose.core.exception;

import io.github.rose.core.util.FormatUtils;
import io.github.rose.core.util.SpringContextUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.Serial;

import static io.github.rose.core.util.Result.SERVER_ERROR;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BusinessException extends RuntimeException {
    private static final MessageSource MESSAGE_SOURCE = SpringContextUtils.getBean(MessageSource.class);


    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private String code;

    /**
     * 错误码对应的参数
     */
    private Object[] args;

    /**
     * 错误消息
     */
    private String defaultMessage;

    public BusinessException(String code, Object... args) {
        this(code, null, args);
    }

    public BusinessException(String defaultMessage) {
        this(SERVER_ERROR, defaultMessage, null);
    }

    public BusinessException(String code, String defaultMessage, Object... args) {
        this.code = code;
        this.args = args;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage();
    }

    @Override
    public String getMessage() {
        String message = null;
        if (!StringUtils.isEmpty(code)) {
            if (MESSAGE_SOURCE != null) {
                message = MESSAGE_SOURCE.getMessage(code, args, LocaleContextHolder.getLocale());
            } else {
                message = FormatUtils.replacePlaceholders(code, args);
            }
        }
        if (message == null) {
            message = defaultMessage;
        }
        return message;
    }

}