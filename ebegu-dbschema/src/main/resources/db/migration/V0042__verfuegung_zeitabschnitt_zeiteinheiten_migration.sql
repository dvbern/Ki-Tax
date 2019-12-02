ALTER TABLE ebegu.verfuegung_zeitabschnitt
	CHANGE COLUMN betreuungspensum betreuungspensum_prozent DECIMAL(19, 2) NULL;

ALTER TABLE ebegu.verfuegung_zeitabschnitt
	CHANGE COLUMN betreuungsstunden betreuungspensum_zeiteinheit DECIMAL(19, 2) NULL;

ALTER TABLE ebegu.verfuegung_zeitabschnitt_aud
	CHANGE COLUMN betreuungspensum betreuungspensum_prozent DECIMAL(19, 2) NULL;

ALTER TABLE ebegu.verfuegung_zeitabschnitt_aud
	CHANGE COLUMN betreuungsstunden betreuungspensum_zeiteinheit DECIMAL(19, 2) NULL;


