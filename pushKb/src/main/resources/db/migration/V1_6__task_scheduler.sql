-- Taken from https://gitlab.com/knowledge-integration/platform/patterns/taskscheduler
create table duty_cycle_task (
  id UUID PRIMARY KEY,
  monitor_holder UUID,
  task_status varchar(16),
  reference varchar(64), -- Presumably this should be unique...
  last_run timestamp,
  task_interval bigint, -- This was integer previously, but that's not big enough
  task_type varchar(64),
  task_bean_name varchar(64),
  additional_data JSONB
);

create view next_due as select *,  ( last_run + ( INTERVAL '1 millisecond' * task_interval ) ) next_due from duty_cycle_task;

