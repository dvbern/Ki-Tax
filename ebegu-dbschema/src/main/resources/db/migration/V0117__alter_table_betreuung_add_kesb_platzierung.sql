ALTER TABLE betreuung
  ADD COLUMN keine_kesb_platzierung BIT NOT NULL;

ALTER TABLE betreuung_aud
  ADD COLUMN keine_kesb_platzierung BIT;