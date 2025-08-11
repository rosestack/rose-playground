-- Add idempotency key to refund_record for idempotent refund requests
ALTER TABLE refund_record
    ADD COLUMN idempotency_key VARCHAR(128) NULL AFTER refund_id,
    ADD UNIQUE KEY uk_refund_idempotency (idempotency_key);

