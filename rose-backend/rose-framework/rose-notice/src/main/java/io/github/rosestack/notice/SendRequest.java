package io.github.rosestack.notice;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * 通用发送参数对象，所有发送相关参数、元信息均在此封装。
 */
@Data
@Builder
public class SendRequest {
    /**
     * 本次请求唯一标识，必填
     */
    private String requestId;

    /**
     * 目标，如邮箱、手机号、用户ID等，必填
     */
    private String target;

    /**
     * 通知正文内容
     */
    private String templateContent;

    /**
     * 抄送（仅部分渠道支持）
     */
    private List<String> cc;

    private Map<String, Object> variables;
}
