package io.github.rosestack.billing.exception;

/**
 * 账单不存在异常
 *
 * @author rose
 */
public class InvoiceNotFoundException extends BillingException {

    public InvoiceNotFoundException(String invoiceId) {
        super("INVOICE_NOT_FOUND", "账单不存在: " + invoiceId);
    }

    public InvoiceNotFoundException(String invoiceId, Throwable cause) {
        super("INVOICE_NOT_FOUND", "账单不存在: " + invoiceId, cause);
    }
}
