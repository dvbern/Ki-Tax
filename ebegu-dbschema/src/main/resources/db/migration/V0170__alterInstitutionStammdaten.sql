ALTER TABLE institution_stammdaten
	ADD COLUMN webseite VARCHAR(255);
ALTER TABLE institution_stammdaten
	ADD COLUMN oeffnungszeiten VARCHAR(255);
ALTER TABLE institution_stammdaten
	ADD COLUMN alterskategorie_baby BIT NOT NULL default 0;
ALTER TABLE institution_stammdaten
	ADD COLUMN alterskategorie_vorschule BIT NOT NULL default 0;
ALTER TABLE institution_stammdaten
	ADD COLUMN alterskategorie_kindergarten BIT NOT NULL default 0;
ALTER TABLE institution_stammdaten
	ADD COLUMN alterskategorie_schule BIT NOT NULL default 0;
ALTER TABLE institution_stammdaten
	ADD COLUMN subventionierte_plaetze BIT NOT NULL default 0;
ALTER TABLE institution_stammdaten
	ADD COLUMN anzahl_plaetze DECIMAL(19,2) NOT NULL;
ALTER TABLE institution_stammdaten
	ADD COLUMN anzahl_plaetze_firmen DECIMAL(19,2);

ALTER TABLE institution_stammdaten_aud
	ADD COLUMN webseite VARCHAR(255);
ALTER TABLE institution_stammdaten_aud
	ADD COLUMN oeffnungszeiten VARCHAR(255);
ALTER TABLE institution_stammdaten_aud
	ADD COLUMN alterskategorie_baby BIT;
ALTER TABLE institution_stammdaten_aud
	ADD COLUMN alterskategorie_vorschule BIT;
ALTER TABLE institution_stammdaten_aud
	ADD COLUMN alterskategorie_kindergarten BIT;
ALTER TABLE institution_stammdaten_aud
	ADD COLUMN alterskategorie_schule BIT;
ALTER TABLE institution_stammdaten_aud
	ADD COLUMN subventionierte_plaetze BIT;
ALTER TABLE institution_stammdaten_aud
	ADD COLUMN anzahl_plaetze DECIMAL(19,2);
ALTER TABLE institution_stammdaten_aud
	ADD COLUMN anzahl_plaetze_firmen DECIMAL(19,2);