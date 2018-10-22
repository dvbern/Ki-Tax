ALTER TABLE betreuungsmitteilung_pensum
	ADD COLUMN monatliche_betreuungskosten DECIMAL(19, 2) NOT NULL;
ALTER TABLE betreuungsmitteilung_pensum_aud
	ADD COLUMN monatliche_betreuungskosten DECIMAL(19, 2);
