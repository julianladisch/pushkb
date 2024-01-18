CREATE TABLE destination (
	id uuid PRIMARY KEY,
	destination_type VARCHAR(64) NOT NULL,
	destination_url varchar(200)
);

CREATE INDEX IF NOT EXISTS destination_destination_type_idx ON destination (destination_type);
CREATE INDEX IF NOT EXISTS destination_destination_url_idx ON destination (destination_url);
CREATE INDEX IF NOT EXISTS destination_destination_type_destination_url_idx ON destination (destination_type, destination_url);

CREATE TABLE destination_source_link (
	id uuid PRIMARY KEY,
	transform VARCHAR(255) NOT NULL,
	destination_id uuid,
	source_id uuid,
	destination_head_pointer TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	last_sent_pointer TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	foot_pointer TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS dsl_destination_idx ON destination_source_link (destination_id);
CREATE INDEX IF NOT EXISTS dsl_source_idx ON destination_source_link (source_id);
CREATE INDEX IF NOT EXISTS dsl_source_destination_idx ON destination_source_link (source_id, destination_id);

CREATE INDEX IF NOT EXISTS dsl_transform_idx ON destination_source_link (transform);
CREATE INDEX IF NOT EXISTS dsl_source_transform_idx ON destination_source_link (source_id, transform);

CREATE INDEX IF NOT EXISTS dsl_destination_head_pointer_idx ON destination_source_link (destination_head_pointer);
CREATE INDEX IF NOT EXISTS dsl_foot_pointer_idx ON destination_source_link (foot_pointer);
CREATE INDEX IF NOT EXISTS dsl_destination_head_pointer_foot_pointer_idx ON destination_source_link (destination_head_pointer, foot_pointer);