ALTER TABLE betreuung
	ADD COLUMN event_published BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE betreuung_aud
	ADD event_published BOOLEAN;

ALTER TABLE betreuung
	ALTER COLUMN event_published DROP DEFAULT;