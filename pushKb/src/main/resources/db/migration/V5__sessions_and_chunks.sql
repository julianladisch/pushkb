CREATE TABLE push_session (
  id uuid PRIMARY KEY,
  pushable_id uuid, -- references either a TemporaryPushTask or a PushTask
  pushable_type VARCHAR(255) NOT NULL, -- Stores the pushable implementing class
  created TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ps_pushable ON push_session (pushable_id);
CREATE INDEX IF NOT EXISTS ps_pushable_type_pushable_idx ON push_session (pushable_type, pushable_id);
CREATE INDEX IF NOT EXISTS ps_created ON push_session (created);
CREATE INDEX IF NOT EXISTS ps_pushable_created ON push_session (pushable_id, created);
CREATE INDEX IF NOT EXISTS ps_pushable_type_pushable_created ON push_session (pushable_type, pushable_id, created);

CREATE TABLE push_chunk (
  id uuid PRIMARY KEY,
  push_session_id uuid,
  created TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS pc_push_session ON push_chunk (push_session_id);
CREATE INDEX IF NOT EXISTS pc_created ON push_chunk (created);
CREATE INDEX IF NOT EXISTS pc_push_session_created ON push_chunk (push_session_id, created);