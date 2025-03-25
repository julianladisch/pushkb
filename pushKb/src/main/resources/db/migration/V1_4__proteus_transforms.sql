CREATE TABLE proteus_transform (
  id uuid PRIMARY KEY,
  slug VARCHAR(255) NOT NULL,
	name VARCHAR(255) NOT NULL,
	source VARCHAR(255) NOT NULL,
	spec_file VARCHAR(255),
	spec jsonb,
);

CREATE INDEX IF NOT EXISTS ptr_name_idx ON proteus_transform (name);
CREATE INDEX IF NOT EXISTS ptr_source_idx ON proteus_transform (source);

CREATE INDEX IF NOT EXISTS ptr_source_name_idx ON proteus_transform (source, name);
CREATE INDEX IF NOT EXISTS ptr_spec_file_idx ON proteus_transform (spec_file);
CREATE INDEX IF NOT EXISTS ptr_source_spec_file_idx ON proteus_transform (source, spec_file);

CREATE INDEX IF NOT EXISTS ptr_spec_idx ON proteus_transform (spec);
CREATE INDEX IF NOT EXISTS ptr_source_spec_idx ON proteus_transform (source, spec);
