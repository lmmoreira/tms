CREATE TABLE company.company (
     id UUID PRIMARY KEY,
     version int NOT NULL,
     name VARCHAR(255) NOT NULL,
     cnpj VARCHAR(255) NOT NULL,
     configuration JSONB
);

CREATE TABLE company.company_type (
      company_id UUID REFERENCES company.company(id),
      type VARCHAR(50) NOT NULL,
      PRIMARY KEY (company_id, type)
);