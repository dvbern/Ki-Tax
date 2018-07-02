ALTER TABLE dossier DROP dossier_nummer;
ALTER TABLE dossier_aud DROP dossier_nummer;

ALTER TABLE fall DROP next_number_dossier;
ALTER TABLE fall_aud DROP next_number_dossier;

ALTER TABLE mandant ADD next_number_gemeinde INTEGER;
ALTER TABLE mandant_aud ADD next_number_gemeinde INTEGER;
UPDATE mandant SET next_number_gemeinde = 3;
UPDATE mandant SET name = 'Kanton Bern';
ALTER TABLE mandant MODIFY next_number_gemeinde INTEGER NOT NULL;

ALTER TABLE gemeinde ADD gemeinde_nummer BIGINT;
ALTER TABLE gemeinde ADD mandant_id VARCHAR(36);

ALTER TABLE gemeinde_aud ADD gemeinde_nummer BIGINT;
ALTER TABLE gemeinde_aud ADD mandant_id VARCHAR(36);

UPDATE gemeinde SET mandant_id = 'e3736eb8-6eef-40ef-9e52-96ab48d8f220';
UPDATE gemeinde SET gemeinde_nummer = 1 WHERE id = 'ea02b313-e7c3-4b26-9ef7-e413f4046db2';
UPDATE gemeinde SET gemeinde_nummer = 2 WHERE id = '80a8e496-b73c-4a4a-a163-a0b2caf76487';
ALTER TABLE gemeinde MODIFY gemeinde_nummer BIGINT NOT NULL;
ALTER TABLE gemeinde MODIFY mandant_id VARCHAR(36) NOT NULL;

ALTER TABLE gemeinde
	ADD CONSTRAINT FK_gemeinde_mandant_id
FOREIGN KEY (mandant_id)
REFERENCES mandant (id);
