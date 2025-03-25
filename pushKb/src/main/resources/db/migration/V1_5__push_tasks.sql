CREATE TABLE push_task (
  id uuid PRIMARY KEY,
	transform uuid NOT NULL,
	transform_type VARCHAR(255) NOT NULL,
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
CREATE INDEX IF NOT EXISTS pt_transform_type_idx ON push_task (transform_type);
CREATE INDEX IF NOT EXISTS pt_transform_type_transform_idx ON push_task (transform_type, transform);

CREATE INDEX IF NOT EXISTS pt_destination_head_pointer_idx ON push_task (destination_head_pointer);
CREATE INDEX IF NOT EXISTS pt_foot_pointer_idx ON push_task (foot_pointer);
CREATE INDEX IF NOT EXISTS pt_destination_head_pointer_foot_pointer_idx ON push_task (destination_head_pointer, foot_pointer);

CREATE TABLE temporary_push_task (
  id uuid PRIMARY KEY,
	push_task_id uuid,
	filter_context VARCHAR(255),
	-- THESE SHOULD MAYBE BE A REFERENCED PushTaskPointer OBJECT
	destination_head_pointer TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	last_sent_pointer TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	foot_pointer TIMESTAMP WITHOUT TIME ZONE NOT NULL,

	CONSTRAINT fk_temporary_push_task_push_task
        FOREIGN KEY (push_task_id) REFERENCES push_task(id)
);

CREATE INDEX IF NOT EXISTS tpt_push_task_idx ON temporary_push_task (push_task_id);
CREATE INDEX IF NOT EXISTS tpt_filter_context_idx ON temporary_push_task (filter_context);
CREATE INDEX IF NOT EXISTS tpt_push_task_filter_context_idx ON temporary_push_task (push_task_id, filter_context);
