CREATE TABLE "${my_schema}".tenant_schema_id (
    id uuid PRIMARY KEY,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    tenant_schema_id VARCHAR(255)
);
