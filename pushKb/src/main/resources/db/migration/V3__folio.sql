CREATE TABLE folio_destination (
	id uuid PRIMARY KEY,
	destination_url varchar(200),
	tenant VARCHAR(64),
	auth_type VARCHAR(64) NOT NULL,
	login_user VARCHAR(200),
	login_password VARCHAR(200)
);

CREATE INDEX IF NOT EXISTS folio_destination_destination_url_idx ON folio_destination (destination_url);
CREATE INDEX IF NOT EXISTS folio_destination_destination_url_tenant_idx ON folio_destination (destination_url, tenant);
CREATE INDEX IF NOT EXISTS folio_destination_auth_type_idx ON folio_destination (auth_type);
