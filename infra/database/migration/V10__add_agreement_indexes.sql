-- Indexes for company.agreement table to optimize common query patterns

-- Index on source company for finding all agreements where a company is the source
CREATE INDEX idx_agreement_source ON company.agreement(source);

-- Index on destination company for finding all agreements where a company is the destination
CREATE INDEX idx_agreement_destination ON company.agreement(destination);

-- Index on relation_type for filtering by agreement type (e.g., SHIPPER_CARRIER, CARRIER_RECEIVER)
CREATE INDEX idx_agreement_relation_type ON company.agreement(relation_type);

-- Composite index for finding active agreements (where valid_to is NULL or in the future)
-- This supports queries filtering by validity period
CREATE INDEX idx_agreement_active ON company.agreement(valid_from, valid_to) WHERE valid_to IS NULL OR valid_to > now();

-- Index on agreement_id in agreement_condition table for efficient joins
CREATE INDEX idx_agreement_condition_agreement_id ON company.agreement_condition(agreement_id);

-- Index on condition_type for filtering conditions by type
CREATE INDEX idx_agreement_condition_type ON company.agreement_condition(condition_type);
