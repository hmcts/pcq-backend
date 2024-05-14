ALTER TABLE protected_characteristics ADD COLUMN IF NOT EXISTS last_updated_timestamp TIMESTAMP;
comment on column protected_characteristics.last_updated_timestamp is 'Last time PCQ was updated';
