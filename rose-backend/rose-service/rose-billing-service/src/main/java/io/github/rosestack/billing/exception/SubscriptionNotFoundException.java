package io.github.rosestack.billing.exception;

/**
 * 订阅不存在异常
 *
 * @author rose
 */
public class SubscriptionNotFoundException extends BillingException {

    public SubscriptionNotFoundException(String subscriptionId) {
        super("SUBSCRIPTION_NOT_FOUND", "订阅不存在: " + subscriptionId);
    }

    public SubscriptionNotFoundException(String subscriptionId, Throwable cause) {
        super("SUBSCRIPTION_NOT_FOUND", "订阅不存在: " + subscriptionId, cause);
    }
}
