ALTER TABLE betreuung
	ADD COLUMN keine_kesb_platzierung BIT;

ALTER TABLE betreuung_aud
	ADD COLUMN keine_kesb_platzierung BIT;

UPDATE betreuung
SET betreuung.keine_kesb_platzierung = TRUE;

ALTER TABLE betreuung MODIFY keine_kesb_platzierung BIT NOT NULL;