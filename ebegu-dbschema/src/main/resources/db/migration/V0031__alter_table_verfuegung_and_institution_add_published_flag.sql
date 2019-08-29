ALTER TABLE verfuegung
	ADD COLUMN event_published BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE verfuegung_aud
	ADD event_published BOOLEAN;

ALTER TABLE verfuegung
	ALTER COLUMN event_published DROP DEFAULT;


ALTER TABLE institution
	ADD COLUMN event_published BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE institution_aud
	ADD event_published BOOLEAN;

ALTER TABLE institution
	ALTER COLUMN event_published DROP DEFAULT;

-- In FlyWay migration 0015 two institutions are inserted without name, which is invalid according to BeanValidation,
-- thus set some name to avoid ConstraintViolationException when publishing
UPDATE institution SET name = 'Unbekannte Kita'
	WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''));

UPDATE institution SET name = 'Unbekannte TFO'
	WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', ''));
