package io.github.rosestack.billing.payment;

public enum PaymentMethod {
    ALIPAY,
    WECHAT,
    STRIPE;

    public static boolean isValid(String name) {
        if (name == null) return false;
        try {
            PaymentMethod.valueOf(name);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}

