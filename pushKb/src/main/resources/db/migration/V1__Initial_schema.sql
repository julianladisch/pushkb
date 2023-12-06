CREATE TABLE hello_world (
	id uuid PRIMARY KEY,
	test1 varchar(128),
	test2 varchar(128)
);

CREATE TABLE source_record (
	id uuid PRIMARY KEY,
	source VARCHAR(64) NOT NULL,
	record_type VARCHAR(64) NOT NULL,
	created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	updated TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	json_record jsonb NOT NULL
);

CREATE INDEX IF NOT EXISTS source_record_source_idx ON source_record (source);

CREATE INDEX IF NOT EXISTS source_record_source_record_type_idx ON source_record (source, record_type);