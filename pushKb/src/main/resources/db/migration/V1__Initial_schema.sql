CREATE TABLE hello_world (
	id uuid PRIMARY KEY,
	test1 varchar(128),
	test2 varchar(128)
);

CREATE TABLE source_record (
	id uuid PRIMARY KEY,
	source varchar(64),
	record_type varchar(64),
	timestamp timestamp,
	jsonRecord jsonb
);