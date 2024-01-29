CREATE TABLE source_record (
	id uuid PRIMARY KEY,
	source_id uuid,
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
CREATE INDEX IF NOT EXISTS source_record_source_last_updated_at_source_idx ON source_record (source_id, last_updated_at_source);

CREATE INDEX IF NOT EXISTS source_record_created_idx ON source_record (created);
CREATE INDEX IF NOT EXISTS source_record_updated_idx ON source_record (updated);

CREATE INDEX IF NOT EXISTS source_record_source_updated_idx ON source_record (source_id, updated);

CREATE TABLE gokb_source (
	id uuid PRIMARY KEY,
	gokb_source_type VARCHAR(64) NOT NULL,
	source_url varchar(200)
);

CREATE INDEX IF NOT EXISTS source_gokb_source_type_idx ON gokb_source (gokb_source_type);
CREATE INDEX IF NOT EXISTS source_source_url_idx ON gokb_source (source_url);
CREATE INDEX IF NOT EXISTS source_source_url_gokb_source_type_idx ON gokb_source (source_url, gokb_source_type);
