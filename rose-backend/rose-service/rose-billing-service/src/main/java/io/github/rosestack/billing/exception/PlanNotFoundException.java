package io.github.rosestack.billing.exception;

/**
 * 订阅计划不存在异常
 *
 * @author rose
 */
public class PlanNotFoundException extends BillingException {

    public PlanNotFoundException(String planId) {
        super("PLAN_NOT_FOUND", "订阅计划不存在: " + planId);
    }

    public PlanNotFoundException(String planId, Throwable cause) {
        super("PLAN_NOT_FOUND", "订阅计划不存在: " + planId, cause);
    }
}
