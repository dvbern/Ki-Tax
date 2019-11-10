DELETE
FROM outbox_event;

UPDATE verfuegung
SET event_published = FALSE;

UPDATE institution
SET event_published = FALSE;

ALTER TABLE ebegu.verfuegung_zeitabschnitt
	ADD COLUMN verfuegte_anzahl_zeiteinheiten            DECIMAL(19, 2) NULL,
	ADD COLUMN anspruchsberechtigte_anzahl_zeiteinheiten DECIMAL(19, 2) NULL,
	ADD COLUMN zeiteinheit                               VARCHAR(100)   NULL;

ALTER TABLE ebegu.verfuegung_zeitabschnitt_aud
	ADD COLUMN verfuegte_anzahl_zeiteinheiten            DECIMAL(19, 2) NULL,
	ADD COLUMN anspruchsberechtigte_anzahl_zeiteinheiten DECIMAL(19, 2) NULL,
	ADD COLUMN zeiteinheit                               VARCHAR(100)   NULL;
