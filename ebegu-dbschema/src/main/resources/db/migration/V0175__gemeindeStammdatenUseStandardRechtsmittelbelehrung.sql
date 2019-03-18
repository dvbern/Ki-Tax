ALTER TABLE gemeinde_stammdaten
	ADD COLUMN standard_rechtsmittelbelehrung bit NOT NULL DEFAULT TRUE;

ALTER TABLE gemeinde_stammdaten_aud
	ADD COLUMN standard_rechtsmittelbelehrung bit;
