CREATE TABLE company.company (
     id UUID PRIMARY KEY,
     name VARCHAR(255) NOT NULL
);

CREATE TABLE company.relationship_configuration (
    id UUID PRIMARY KEY,
    parent_id UUID NOT NULL REFERENCES company.company(id),
    child_id UUID REFERENCES company.company(id),
    configuration_key VARCHAR(255) NOT NULL,
    configuration_value JSONB NOT NULL,
    relationship_configuration_parent_id UUID REFERENCES company.relationship_configuration(id),
    UNIQUE (parent_id, child_id, configuration_key, relationship_configuration_parent_id)
);

CREATE INDEX idx_relationship_configuration_parent_id ON company.relationship_configuration(parent_id);
CREATE INDEX idx_relationship_configuration_child_id ON company.relationship_configuration(child_id);
CREATE INDEX idx_relationship_configuration_configuration_parent_id ON company.relationship_configuration(relationship_configuration_parent_id);

