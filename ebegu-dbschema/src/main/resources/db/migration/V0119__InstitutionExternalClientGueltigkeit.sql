ALTER TABLE institution_external_client add COLUMN gueltig_ab DATE NOT NULL;
ALTER TABLE institution_external_client add COLUMN gueltig_bis DATE NOT NULL;

UPDATE institution_external_client set gueltig_ab = '2000-01-01';
UPDATE institution_external_client set gueltig_bis = '9999-12-31';

ALTER TABLE institution_external_client_aud add COLUMN gueltig_ab DATE;
ALTER TABLE institution_external_client_aud add COLUMN gueltig_bis DATE;

UPDATE institution_external_client_aud set gueltig_ab = '2000-01-01';
UPDATE institution_external_client_aud set gueltig_bis = '9999-12-31';