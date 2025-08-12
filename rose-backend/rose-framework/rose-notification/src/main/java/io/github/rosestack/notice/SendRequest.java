package io.github.rosestack.notice;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

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

    // 显式 getter/setter，避免依赖 Lombok 处理器
    public String getRequestId() { return requestId; }
    public String getTarget() { return target; }
    public String getTemplateContent() { return templateContent; }
    public void setTemplateContent(String templateContent) { this.templateContent = templateContent; }
    public List<String> getCc() { return cc; }
    public Map<String, Object> getVariables() { return variables; }
}
