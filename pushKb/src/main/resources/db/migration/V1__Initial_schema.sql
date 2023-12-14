CREATE TABLE source (
	id uuid PRIMARY KEY,
	code VARCHAR(64) NOT NULL,
	source_type VARCHAR(64) NOT NULL,
	source_url varchar(200)
);

CREATE INDEX IF NOT EXISTS source_code_idx ON source (code);
CREATE INDEX IF NOT EXISTS source_source_type_idx ON source (source_type);
CREATE INDEX IF NOT EXISTS source_source_url_idx ON source (source_url);
CREATE INDEX IF NOT EXISTS source_code_source_type_idx ON source (code, source_type);
CREATE INDEX IF NOT EXISTS source_source_url_code_source_type_idx ON source (source_url, code, source_type);

CREATE TABLE source_record (
	id uuid PRIMARY KEY,
	source_id uuid,
	CONSTRAINT fk_source FOREIGN KEY (source_id) REFERENCES source(id),
	source_uuid VARCHAR(64),
	created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	updated TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	last_updated_at_source TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	json_record jsonb NOT NULL
);

CREATE INDEX IF NOT EXISTS source_record_source_idx ON source_record (source_id);
CREATE INDEX IF NOT EXISTS source_record_source_uuid_idx ON source_record (source_uuid);
CREATE INDEX IF NOT EXISTS source_record_source_source_uuid_idx ON source_record (source_id, source_uuid);

CREATE INDEX IF NOT EXISTS source_record_last_updated_at_source_idx ON source_record (last_updated_at_source);

CREATE INDEX IF NOT EXISTS source_record_created_idx ON source_record (created);
CREATE INDEX IF NOT EXISTS source_record_updated_idx ON source_record (updated);