ALTER TABLE benutzer
	ADD status VARCHAR(255) NOT NULL DEFAULT 'AKTIV';

UPDATE benutzer
SET status = 'GESPERRT'
WHERE gesperrt = TRUE;

ALTER TABLE benutzer
	ALTER COLUMN status DROP DEFAULT;

ALTER TABLE benutzer
	DROP COLUMN gesperrt;


ALTER TABLE benutzer_aud
	ADD status VARCHAR(255) NOT NULL DEFAULT 'AKTIV';

UPDATE benutzer_aud
SET status = 'GESPERRT'
WHERE gesperrt = TRUE;

ALTER TABLE benutzer_aud
	ALTER COLUMN status DROP DEFAULT;

ALTER TABLE benutzer_aud
	DROP COLUMN gesperrt;


ALTER TABLE berechtigung_history
	ADD status VARCHAR(255) NOT NULL DEFAULT 'AKTIV';

UPDATE berechtigung_history
SET status = 'GESPERRT'
WHERE gesperrt = TRUE;

ALTER TABLE berechtigung_history
	ALTER COLUMN status DROP DEFAULT;

ALTER TABLE berechtigung_history
	DROP COLUMN gesperrt;


ALTER TABLE berechtigung_history_aud
	ADD status VARCHAR(255) NOT NULL DEFAULT 'AKTIV';

UPDATE berechtigung_history_aud
SET status = 'GESPERRT'
WHERE gesperrt = TRUE;

ALTER TABLE berechtigung_history_aud
	ALTER COLUMN status DROP DEFAULT;

ALTER TABLE berechtigung_history_aud
	DROP COLUMN gesperrt;