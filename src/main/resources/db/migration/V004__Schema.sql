
ALTER TABLE protected_characteristics ADD COLUMN IF NOT EXISTS dcn_number TEXT;

ALTER TABLE protected_characteristics ADD COLUMN IF NOT EXISTS form_id TEXT;

comment on column protected_characteristics.dcn_number is 'DCN Number for paper channel';
comment on column protected_characteristics.form_id is 'Form type for paper channel';
