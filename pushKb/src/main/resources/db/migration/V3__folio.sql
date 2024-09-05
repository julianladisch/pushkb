CREATE TABLE folio_tenant (
	id uuid PRIMARY KEY,
	base_url varchar(200),
	tenant VARCHAR(64),
	auth_type VARCHAR(64) NOT NULL,
	login_user VARCHAR(200),
	login_password VARCHAR(200)
);

CREATE INDEX IF NOT EXISTS folio_tenant_base_url_idx ON folio_tenant (base_url);
CREATE INDEX IF NOT EXISTS folio_tenant_base_url_tenant_idx ON folio_tenant (base_url, tenant);
CREATE INDEX IF NOT EXISTS folio_tenant_auth_type_idx ON folio_tenant (auth_type);

CREATE TABLE folio_destination (
	id uuid PRIMARY KEY,
	destination_type VARCHAR(64) NOT NULL,
	folio_tenant_id uuid,
	CONSTRAINT fk_folio_destination_folio_tenant_id
        FOREIGN KEY (folio_tenant_id) REFERENCES folio_tenant(id)

);

CREATE INDEX IF NOT EXISTS folio_destination_destination_type_idx ON folio_destination (destination_type);
CREATE INDEX IF NOT EXISTS folio_destination_folio_tenant_idx ON folio_destination (folio_tenant_id);
CREATE INDEX IF NOT EXISTS folio_destination_folio_tenant_destination_type_idx ON folio_destination (folio_tenant_id, destination_type);