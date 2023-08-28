CREATE TABLE "${my_schema}".acapy_tenant_config (
    id uuid PRIMARY KEY,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    wallet_id VARCHAR(255),
    bearer_token VARCHAR(255)
);
