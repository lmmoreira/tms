CREATE TABLE company.outbox (
     id UUID,
     created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
     status TEXT NOT NULL,
     content JSONB NOT NULL,
     type VARCHAR(50) NOT NULL,
     aggregate_id UUID NOT NULL,
     PRIMARY KEY (id, status)
) PARTITION BY LIST (status);

CREATE TABLE company.outbox_not_published PARTITION OF company.outbox FOR VALUES IN ('NEW', 'PROCESSING', 'FAILED');
CREATE TABLE company.outbox_published PARTITION OF company.outbox FOR VALUES IN ('PUBLISHED');

CREATE INDEX idx_outbox_not_published_status ON company.outbox_not_published (status, created_at);
CREATE INDEX idx_outbox_not_published_aggregate ON company.outbox_not_published (aggregate_id, created_at);
CREATE INDEX idx_outbox_not_published_type ON company.outbox_not_published (type, created_at);

CREATE INDEX idx_outbox_published_aggregate ON company.outbox_published (aggregate_id, created_at);
CREATE INDEX idx_outbox_published_type ON company.outbox_published (type, created_at);