UPDATE protected_characteristics SET last_updated_timestamp = completed_date WHERE case_id IS NULL;
UPDATE protected_characteristics SET last_updated_timestamp = now() + interval '3 months' WHERE case_id IS NOT NULL;
