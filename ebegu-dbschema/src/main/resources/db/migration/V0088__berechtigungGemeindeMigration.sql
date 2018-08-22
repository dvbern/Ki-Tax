INSERT INTO berechtigung_gemeinde (
	SELECT
		id,
		(SELECT id FROM gemeinde WHERE name = 'Bern')
	FROM berechtigung
	WHERE role in ('ADMIN', 'SACHBEARBEITER_JA', 'JURIST', 'REVISOR', 'STEUERAMT', 'ADMINISTRATOR_SCHULAMT', 'SCHULAMT')
);
