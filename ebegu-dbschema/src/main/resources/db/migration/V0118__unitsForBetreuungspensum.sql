ALTER TABLE betreuungspensum
	ADD COLUMN unit_for_display VARCHAR(255) NOT NULL DEFAULT 'PERCENTAGE';
ALTER TABLE betreuungspensum_aud
	ADD COLUMN unit_for_display VARCHAR(255);
ALTER TABLE betreuungspensum
	ALTER unit_for_display DROP DEFAULT;

ALTER TABLE betreuungsmitteilung_pensum
	ADD COLUMN unit_for_display VARCHAR(255) NOT NULL DEFAULT 'PERCENTAGE';
ALTER TABLE betreuungsmitteilung_pensum_aud
	ADD COLUMN unit_for_display VARCHAR(255);
ALTER TABLE betreuungsmitteilung_pensum
	ALTER unit_for_display DROP DEFAULT;
