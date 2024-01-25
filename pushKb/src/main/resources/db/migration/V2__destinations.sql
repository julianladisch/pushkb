CREATE TABLE folio_destination (
	id uuid PRIMARY KEY,
	destination_url varchar(200),
	tenant VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS folio_destination_destination_url_idx ON folio_destination (destination_url);
