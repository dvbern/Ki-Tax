INSERT INTO berechtigung_gemeinde (
	SELECT
		id,
		(SELECT id FROM gemeinde WHERE name = 'Bern')
	FROM berechtigung);