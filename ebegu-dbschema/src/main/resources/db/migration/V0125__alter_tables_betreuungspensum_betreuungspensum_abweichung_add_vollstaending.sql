ALTER TABLE betreuungspensum
	ADD COLUMN vollstaendig BIT NOT NULL DEFAULT TRUE;
ALTER TABLE betreuungspensum
	ALTER COLUMN vollstaendig DROP DEFAULT;

ALTER TABLE betreuungspensum_aud
	ADD COLUMN vollstaendig BIT;
UPDATE betreuungspensum_aud
SET vollstaendig = TRUE;

ALTER TABLE betreuungspensum_abweichung
	ADD COLUMN vollstaendig BIT NOT NULL DEFAULT TRUE;
ALTER TABLE betreuungspensum_abweichung
	ALTER COLUMN vollstaendig DROP DEFAULT;

ALTER TABLE betreuungspensum_abweichung_aud
	ADD COLUMN vollstaendig BIT;
UPDATE betreuungspensum_abweichung_aud
SET vollstaendig = TRUE;

-- the default was not removed in V0113
ALTER TABLE betreuungsmitteilung_pensum
	ALTER COLUMN vollstaendig DROP DEFAULT;
