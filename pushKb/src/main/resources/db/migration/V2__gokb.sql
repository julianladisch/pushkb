CREATE TABLE gokb (
	id uuid PRIMARY KEY,
	base_url VARCHAR(200)
);

CREATE INDEX IF NOT EXISTS gokb_base_url_idx ON gokb (base_url);

CREATE TABLE gokb_source (
	id uuid PRIMARY KEY,
	gokb_source_type VARCHAR(64) NOT NULL,
	gokb_id uuid,
	pointer TIMESTAMP WITHOUT TIME ZONE,
	CONSTRAINT fk_gokb_source_gokb
        FOREIGN KEY (gokb_id) REFERENCES gokb(id)
);

CREATE INDEX IF NOT EXISTS source_gokb_source_type_idx ON gokb_source (gokb_source_type);
CREATE INDEX IF NOT EXISTS source_gokb_idx ON gokb_source (gokb_id);
CREATE INDEX IF NOT EXISTS source_gokb_gokb_source_type_idx ON gokb_source (gokb_id, gokb_source_type);
CREATE INDEX IF NOT EXISTS source_pointer_idx ON gokb_source (pointer);
