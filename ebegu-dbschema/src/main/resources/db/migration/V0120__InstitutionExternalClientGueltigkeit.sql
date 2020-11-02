ALTER TABLE institution_external_client add COLUMN gueltig_ab DATE NOT NULL;
ALTER TABLE institution_external_client add COLUMN gueltig_bis DATE NOT NULL;

ALTER TABLE institution_external_client_aud add COLUMN gueltig_ab DATE;
ALTER TABLE institution_external_client_aud add COLUMN gueltig_bis DATE;