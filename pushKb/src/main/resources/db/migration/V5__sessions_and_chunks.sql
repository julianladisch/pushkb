CREATE TABLE push_session (
  id uuid PRIMARY KEY,
  push_task_id uuid,
  created TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ps_push_task ON push_session (push_task_id);
CREATE INDEX IF NOT EXISTS ps_created ON push_session (created);
CREATE INDEX IF NOT EXISTS ps_push_task_created ON push_session (push_task_id, created);

CREATE TABLE push_chunk (
  id uuid PRIMARY KEY,
  push_session_id uuid,
  created TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS pc_push_session ON push_chunk (push_session_id);
CREATE INDEX IF NOT EXISTS pc_created ON push_chunk (created);
CREATE INDEX IF NOT EXISTS pc_push_session_created ON push_chunk (push_session_id, created);