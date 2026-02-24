-- Add unique constraint to prevent duplicate active agreements
-- An active agreement is defined as one where valid_to is NULL (permanent) or in the future

-- Create unique partial index to enforce business rule:
-- Only one active agreement can exist for a given source-destination-relation_type combination
-- This prevents accidentally creating duplicate agreements between the same companies
CREATE UNIQUE INDEX idx_agreement_unique_active 
    ON company.agreement(source, destination, relation_type) 
    WHERE valid_to IS NULL OR valid_to > now();
