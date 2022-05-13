UPDATE application_property
SET value = 'false'
WHERE name = 'SCHNITTSTELLE_EVENTS_AKTIVIERT' AND mandant_id = @mandant_id_luzern;

ALTER TABLE anmeldung_tagesschule
	ADD COLUMN event_published BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE anmeldung_tagesschule_aud
	ADD event_published BOOLEAN;

ALTER TABLE anmeldung_tagesschule
	ALTER COLUMN event_published DROP DEFAULT;