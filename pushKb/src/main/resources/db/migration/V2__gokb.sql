CREATE TABLE gokb (
	id uuid PRIMARY KEY,
	name VARCHAR(200),
	base_url VARCHAR(200)
);

CREATE INDEX IF NOT EXISTS gokb_base_url_idx ON gokb (base_url);
CREATE INDEX IF NOT EXISTS gokb_name_idx ON gokb (name);

CREATE TABLE gokb_source (
	id uuid PRIMARY KEY,
	gokb_source_type VARCHAR(64) NOT NULL,
	gokb_id uuid,
	name VARCHAR(200),
	pointer TIMESTAMP WITHOUT TIME ZONE,
	last_ingest_started TIMESTAMP WITHOUT TIME ZONE,
	last_ingest_completed TIMESTAMP WITHOUT TIME ZONE,

	CONSTRAINT fk_gokb_source_gokb
        FOREIGN KEY (gokb_id) REFERENCES gokb(id)
);

CREATE INDEX IF NOT EXISTS gokb_source_name_idx ON gokb_source(name);

CREATE INDEX IF NOT EXISTS gokb_source_gokb_source_type_idx ON gokb_source (gokb_source_type);
CREATE INDEX IF NOT EXISTS gokb_source_gokb_idx ON gokb_source (gokb_id);
CREATE INDEX IF NOT EXISTS gokb_source_gokb_gokb_source_type_idx ON gokb_source (gokb_id, gokb_source_type);

CREATE INDEX IF NOT EXISTS gokb_source_pointer_idx ON gokb_source (pointer);
CREATE INDEX IF NOT EXISTS gokb_source_last_ingest_started_idx ON gokb_source (last_ingest_started);
CREATE INDEX IF NOT EXISTS gokb_source_last_ingest_completed_idx ON gokb_source (last_ingest_completed);
