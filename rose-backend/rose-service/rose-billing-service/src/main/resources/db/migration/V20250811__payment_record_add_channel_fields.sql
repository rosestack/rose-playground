-- Add channel fields and posted flags to payment_record for reconciliation
ALTER TABLE payment_record
    ADD COLUMN channel_status VARCHAR(32) NULL AFTER status,
    ADD COLUMN channel_amount DECIMAL(10,2) NULL AFTER channel_status,
    ADD COLUMN posted TINYINT(1) DEFAULT 0 AFTER channel_amount,
    ADD COLUMN posted_at DATETIME NULL AFTER posted;

