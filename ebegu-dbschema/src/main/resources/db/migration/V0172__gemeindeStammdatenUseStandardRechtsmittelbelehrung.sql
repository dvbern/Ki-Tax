ALTER TABLE gemeinde_stammdaten
	ADD COLUMN standard_rechtsmittelbelehrung bit NOT NULL;

ALTER TABLE gemeinde_stammdaten_aud
	ADD COLUMN standard_rechtsmittelbelehrung bit;

