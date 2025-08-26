CREATE TABLE company.agreement (
    id UUID PRIMARY KEY,
    from UUID NOT NULL REFERENCES company.company(id),
    to UUID NOT NULL REFERENCES company.company(id),
    relation_type TEXT NOT NULL,
    configuration JSONB,
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    valid_to TIMESTAMP WITH TIME ZONE NULL
);

CREATE TABLE company.agreement_condition (
    id UUID PRIMARY KEY,
    agreement_id UUID NOT NULL REFERENCES company.agreement(id) ON DELETE CASCADE,
    condition_type TEXT NOT NULL,
    conditions JSONB
);

