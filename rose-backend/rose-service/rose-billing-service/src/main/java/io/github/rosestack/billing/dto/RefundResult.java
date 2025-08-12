package io.github.rosestack.billing.dto;

import lombok.Data;

/** 退款结果对象 */
@Data
public class RefundResult {
    private boolean success;
    private String refundId;
    private String errorMessage;

    public static RefundResult success(String refundId) {
        RefundResult result = new RefundResult();
        result.setSuccess(true);
        result.setRefundId(refundId);
        return result;
    }

    public static RefundResult failed(String errorMessage) {
        RefundResult result = new RefundResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
