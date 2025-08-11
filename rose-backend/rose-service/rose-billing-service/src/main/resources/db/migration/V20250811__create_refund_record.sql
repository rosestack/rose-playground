-- Create refund_record table
CREATE TABLE IF NOT EXISTS refund_record (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    invoice_id VARCHAR(64) NOT NULL,
    payment_method VARCHAR(64) NOT NULL,
    transaction_id VARCHAR(128) NOT NULL,
    refund_id VARCHAR(128),
    refund_amount DECIMAL(10,2) NOT NULL,
    reason VARCHAR(255),
    status VARCHAR(32) NOT NULL,
    raw_callback TEXT,
    requested_at DATETIME,
    completed_at DATETIME,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_invoice (invoice_id),
    INDEX idx_tx (transaction_id),
    INDEX idx_status (status)
);

