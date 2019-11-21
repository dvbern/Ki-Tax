DELETE
FROM outbox_event;

ALTER TABLE outbox_event
	ADD COLUMN avro_schema LONGTEXT NOT NULL;

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

# instead of migrating the assignement, just unsed it: each institution can be assigned to the client manually in the GUI
DELETE
FROM institution_external_client;
