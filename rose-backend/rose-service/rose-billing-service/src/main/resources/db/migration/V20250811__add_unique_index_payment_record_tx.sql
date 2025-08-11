-- Add unique index on payment_record.transaction_id to enforce idempotency
ALTER TABLE payment_record
    ADD UNIQUE KEY uk_transaction_id (transaction_id);

