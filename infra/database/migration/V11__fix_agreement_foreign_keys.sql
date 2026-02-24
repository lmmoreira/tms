-- Fix foreign key constraints on company.agreement table
-- Change ON DELETE behavior to RESTRICT to prevent accidental deletion of companies with active agreements

-- Drop existing foreign key constraints
ALTER TABLE company.agreement DROP CONSTRAINT agreement_source_fkey;
ALTER TABLE company.agreement DROP CONSTRAINT agreement_destination_fkey;

-- Re-add foreign key constraints with ON DELETE RESTRICT
-- This ensures that companies cannot be deleted if they have agreements (as source or destination)
ALTER TABLE company.agreement 
    ADD CONSTRAINT agreement_source_fkey 
    FOREIGN KEY (source) REFERENCES company.company(id) ON DELETE RESTRICT;

ALTER TABLE company.agreement 
    ADD CONSTRAINT agreement_destination_fkey 
    FOREIGN KEY (destination) REFERENCES company.company(id) ON DELETE RESTRICT;
