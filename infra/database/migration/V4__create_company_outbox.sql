CREATE TABLE company.outbox (
     id UUID,
     created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
     published BOOLEAN NOT NULL DEFAULT FALSE,
     content JSONB NOT NULL,
     type VARCHAR(50) NOT NULL,
     aggregate_id TEXT NOT NULL,
     PRIMARY KEY (id, published)
) PARTITION BY LIST (published);

CREATE TABLE company.outbox_published PARTITION OF company.outbox FOR VALUES IN (true);
CREATE TABLE company.outbox_not_published PARTITION OF company.outbox FOR VALUES IN (false);

CREATE INDEX idx_outbox_not_published_aggregate ON company.outbox_not_published (aggregate_id, created_at);
CREATE INDEX idx_outbox_not_published_type ON company.outbox_not_published (type, created_at);

CREATE INDEX idx_outbox_published_aggregate ON company.outbox_published (aggregate_id);
CREATE INDEX idx_outbox_published_type ON company.outbox_published (type);