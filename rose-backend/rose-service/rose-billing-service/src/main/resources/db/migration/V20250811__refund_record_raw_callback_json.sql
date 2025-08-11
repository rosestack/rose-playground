-- Alter refund_record.raw_callback to JSON type (MySQL 5.7+)
ALTER TABLE refund_record
    MODIFY COLUMN raw_callback JSON NULL;
