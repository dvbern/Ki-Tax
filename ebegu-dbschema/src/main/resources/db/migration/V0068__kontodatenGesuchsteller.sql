-- add new fields
ALTER TABLE familiensituation ADD COLUMN keine_mahlzeitenverguenstigung_beantragt BIT NOT NULL DEFAULT FALSE;
ALTER TABLE familiensituation_aud ADD COLUMN keine_mahlzeitenverguenstigung_beantragt BIT;

ALTER TABLE familiensituation ADD COLUMN iban VARCHAR(34);
ALTER TABLE familiensituation_aud ADD COLUMN iban VARCHAR(34);

ALTER TABLE familiensituation ADD COLUMN kontoinhaber VARCHAR(255);
ALTER TABLE familiensituation_aud ADD COLUMN kontoinhaber VARCHAR(255);

ALTER TABLE familiensituation ADD COLUMN abweichende_zahlungsadresse BIT NOT NULL DEFAULT FALSE;
ALTER TABLE familiensituation_aud ADD COLUMN abweichende_zahlungsadresse BIT;

ALTER TABLE familiensituation ADD COLUMN zahlungsadresse_id VARCHAR(36);
ALTER TABLE familiensituation_aud ADD COLUMN zahlungsadresse_id VARCHAR(36);

-- add FK on adresse
ALTER TABLE familiensituation
  ADD CONSTRAINT FK_familiensituation_adresse
FOREIGN KEY (zahlungsadresse_id)
REFERENCES adresse(id);