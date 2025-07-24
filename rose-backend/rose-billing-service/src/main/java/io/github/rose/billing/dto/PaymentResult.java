package io.github.rose.billing.dto;

import lombok.Data;

import java.util.Map; /**
 * 支付结果对象
 */
@Data
public class PaymentResult {
    private boolean success;
    private String transactionId;
    private String errorMessage;
    private Map<String, Object> gatewayResponse;

    public static PaymentResult success(String transactionId) {
        PaymentResult result = new PaymentResult();
        result.setSuccess(true);
        result.setTransactionId(transactionId);
        return result;
    }

    public static PaymentResult failed(String errorMessage) {
        PaymentResult result = new PaymentResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
