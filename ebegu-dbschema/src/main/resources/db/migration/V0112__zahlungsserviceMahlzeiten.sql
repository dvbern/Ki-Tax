ALTER TABLE zahlung	CHANGE COLUMN institution_id empfaenger_id binary(16) not null;
ALTER TABLE zahlung	CHANGE COLUMN institution_name empfaenger_name varchar(255) not null;

ALTER TABLE zahlung_aud	CHANGE COLUMN institution_id empfaenger_id binary(16);
ALTER TABLE zahlung_aud	CHANGE COLUMN institution_name empfaenger_name varchar(255);