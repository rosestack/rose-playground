package io.github.rosestack.billing.enums;

public enum BillingErrorCode {
    INVALID_REFUND_CALLBACK(4001, "invalid refund callback"),
    CALLBACK_UPDATE_FAILED(4002, "callback update failed"),
    IDEMPOTENCY_CONFLICT(4003, "idempotency conflict");

    private final int code;
    private final String message;

    BillingErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}

