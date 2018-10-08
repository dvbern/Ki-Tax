ALTER TABLE betreuungspensum
	ADD COLUMN unit_for_display VARCHAR(255) NOT NULL;
ALTER TABLE betreuungspensum_aud
	ADD COLUMN unit_for_display VARCHAR(255);

ALTER TABLE betreuungsmitteilung_pensum
	ADD COLUMN unit_for_display VARCHAR(255) NOT NULL;
ALTER TABLE betreuungsmitteilung_pensum_aud
	ADD COLUMN unit_for_display VARCHAR(255);
