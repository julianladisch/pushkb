CREATE TABLE push_task (
  id uuid PRIMARY KEY,
	transform VARCHAR(255) NOT NULL,
	destination_id uuid,
  destination_type VARCHAR(255) NOT NULL,
	source_id uuid, -- References a table dynamically through destinationType
  source_type VARCHAR(255) NOT NULL, -- SourceType here is a CLASS not to be confused with GokbSourceType on GokbSource
	destination_head_pointer TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	last_sent_pointer TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	foot_pointer TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS pt_destination_idx ON push_task (destination_id);
CREATE INDEX IF NOT EXISTS pt_source_idx ON push_task (source_id);
CREATE INDEX IF NOT EXISTS pt_source_destination_idx ON push_task (source_id, destination_id);

CREATE INDEX IF NOT EXISTS pt_source_type_idx ON push_task (source_type);
CREATE INDEX IF NOT EXISTS pt_source_type_source_idx ON push_task (source_type, source_id);

CREATE INDEX IF NOT EXISTS pt_destination_type_source_idx ON push_task (source_type, source_id);
CREATE INDEX IF NOT EXISTS pt_destination_type_destination_idx ON push_task (destination_type, destination_id);

CREATE INDEX IF NOT EXISTS pt_transform_idx ON push_task (transform);

CREATE INDEX IF NOT EXISTS pt_destination_head_pointer_idx ON push_task (destination_head_pointer);
CREATE INDEX IF NOT EXISTS pt_foot_pointer_idx ON push_task (foot_pointer);
CREATE INDEX IF NOT EXISTS pt_destination_head_pointer_foot_pointer_idx ON push_task (destination_head_pointer, foot_pointer);

