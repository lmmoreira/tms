CREATE TABLE company.outbox (
     id UUID,
     published BOOLEAN NOT NULL DEFAULT FALSE,
     content JSONB NOT NULL,
     type VARCHAR(50) NOT NULL,
     PRIMARY KEY (id, published)
) PARTITION BY LIST (published);

CREATE TABLE company.outbox_published PARTITION OF company.outbox FOR VALUES IN (true);
CREATE TABLE company.outbox_not_published PARTITION OF company.outbox FOR VALUES IN (false);


