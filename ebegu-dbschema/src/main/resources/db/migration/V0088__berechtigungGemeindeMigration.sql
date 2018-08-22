INSERT INTO berechtigung_gemeinde (
	SELECT
		id,
		(SELECT id FROM gemeinde WHERE name = 'Bern')
	FROM berechtigung
	WHERE role in ('ADMIN', 'SACHBEARBEITER_BG', 'JURIST', 'REVISOR', 'STEUERAMT', 'ADMIN_TS', 'SCHULAMT')
);
