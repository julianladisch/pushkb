CREATE TABLE folio_destination (
	id uuid PRIMARY KEY,
	destination_url varchar(200),
	tenant VARCHAR(64),
	login_user VARCHAR(200),
	login_password VARCHAR(200)
);

CREATE INDEX IF NOT EXISTS folio_destination_destination_url_idx ON folio_destination (destination_url);

CREATE TABLE folio_destination_error (
	id uuid PRIMARY KEY,
	destination uuid,
	code VARCHAR(200),
	message VARCHAR(200)
);

CREATE INDEX IF NOT EXISTS folio_destination_error_destination_idx ON folio_destination_error (destination);
CREATE INDEX IF NOT EXISTS folio_destination_error_code_idx ON folio_destination_error (code);
CREATE INDEX IF NOT EXISTS folio_destination_error_destination_code_idx ON folio_destination_error (destination, code);