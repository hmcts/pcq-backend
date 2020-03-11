#!/usr/bin/env bash

set -e

if [ -z "$PCQ_DB" ] || [ -z "$PCQ_DB_USERNAME" ] || [ -z "$PCQ_DB_PASSWORD" ]; then
  echo "ERROR: Missing environment variable. Set value for 'PCQ_DB', 'PCQ_DB_USERNAME' and 'PCQ_DB_PASSWORD'."
  exit 1
fi

# Create roles and databases
psql -v ON_ERROR_STOP=1 --username postgres --set USERNAME=$PCQ_DB_USERNAME --set PASSWORD=$PCQ_DB_PASSWORD <<-EOSQL
  CREATE USER :USERNAME WITH PASSWORD ':PASSWORD';
EOSQL

psql -v ON_ERROR_STOP=1 --username postgres --set USERNAME=$PCQ_DB_USERNAME --set PASSWORD=$PCQ_DB_PASSWORD --set DATABASE=$PCQ_DB <<-EOSQL
  CREATE DATABASE :DATABASE
    WITH OWNER = :USERNAME
    ENCODING = 'UTF-8'
    CONNECTION LIMIT = -1;
    ALTER SCHEMA public OWNER TO :USERNAME;
EOSQL

psql -v ON_ERROR_STOP=1 --username $PCQ_DB_USERNAME $PCQ_DB <<-EOSQL
  CREATE TABLE protected_characteristics (
   pcq_id integer PRIMARY KEY,
   case_id TEXT,
   party_id TEXT,
   channel SMALLINT NOT NULL,
   completed_date TIMESTAMP NOT NULL,
   service_id integer NOT NULL,
   actor SMALLINT NOT NULL,
   version_number SMALLINT NOT NULL,
   dob_provided SMALLINT,
   dob DATE,
   language_main SMALLINT,
   language_other TEXT,
   english_language_level SMALLINT,
   sex SMALLINT,
   gender_different SMALLINT,
   gender_other TEXT,
   sexuality SMALLINT,
   sexuality_other TEXT,
   marriage SMALLINT,
   ethnicity SMALLINT,
   ethnicity_other TEXT,
   religion SMALLINT,
   religion_other TEXT,
   disability_conditions SMALLINT,
   disability_impact SMALLINT,
   disability_vision SMALLINT,
   disability_hearing SMALLINT,
   disability_mobility SMALLINT,
   disability_dexterity SMALLINT,
   disability_learning SMALLINT,
   disability_memory SMALLINT,
   disability_mental_health SMALLINT,
   disability_stamina SMALLINT,
   disability_social SMALLINT,
   disability_condition_other TEXT,
   disability_none SMALLINT,
   pregnancy SMALLINT
  );

  comment on column protected_characteristics.pcq_id is 'Unique PCQ Id';
  comment on column protected_characteristics.case_id is 'Case Reference Number (stored as Encrypted value)';
  comment on column protected_characteristics.party_id is 'Party Id of the PCQ provider (stored as Encrypted value)';
  comment on column protected_characteristics.channel is 'Number';
  comment on column protected_characteristics.completed_date is 'Date when the PCQ was completed';
  comment on column protected_characteristics.service_id is 'Identifies the particular service invoking PCQs';
  comment on column protected_characteristics.actor is 'Type of role for the person completing the survey';
  comment on column protected_characteristics.version_number is 'Identifies which set of questions are being asked, for easier data analysis';
  comment on column protected_characteristics.dob_provided is 'Flags if the DOB question was answered';
  comment on column protected_characteristics.dob is 'DOB for the PCQ provider';
  comment on column protected_characteristics.language_main is 'Main language';
  comment on column protected_characteristics.language_other is 'Main language other';
  comment on column protected_characteristics.english_language_level is 'How well can you speak English';
  comment on column protected_characteristics.sex is 'What is your sex?';
  comment on column protected_characteristics.gender_different is 'Is your gender the same as the sex registered at birth?';
  comment on column protected_characteristics.gender_other is 'Gender identified by PCQ provider';
  comment on column protected_characteristics.sexuality is 'Sexuality';
  comment on column protected_characteristics.sexuality_other is 'Sexuality';
  comment on column protected_characteristics.marriage is 'Are you married or in civil partnership?';
  comment on column protected_characteristics.ethnicity is 'What is your Ethnic Group';
  comment on column protected_characteristics.ethnicity_other is 'Ethnic Group Other';
  comment on column protected_characteristics.religion is 'What is your religion?';
  comment on column protected_characteristics.religion_other is 'Religion Other';
  comment on column protected_characteristics.disability_conditions is 'Any physical or mental health conditions?';
  comment on column protected_characteristics.disability_impact is 'Do any of your condition or illnesses reduce your ability?';
  comment on column protected_characteristics.disability_vision is 'Disability Vision';
  comment on column protected_characteristics.disability_hearing is 'Disability Hearing';
  comment on column protected_characteristics.disability_mobility is 'Disability Mobility';
  comment on column protected_characteristics.disability_dexterity is 'Disability Dexterity';
  comment on column protected_characteristics.disability_learning is 'Disability Learning';
  comment on column protected_characteristics.disability_memory is 'Disability Memory';
  comment on column protected_characteristics.disability_mental_health is 'Mental Health';
  comment on column protected_characteristics.disability_stamina is 'Disability Stamina';
  comment on column protected_characteristics.disability_social is 'Disability Social';
  comment on column protected_characteristics.disability_condition_other is 'Details of other disabilities not listed';
  comment on column protected_characteristics.disability_none is 'Disability None';
  comment on column protected_characteristics.pregnancy is 'Are you pregnant or have you been pregnant in last year?';

EOSQL
  echo "Database $service: Created"

