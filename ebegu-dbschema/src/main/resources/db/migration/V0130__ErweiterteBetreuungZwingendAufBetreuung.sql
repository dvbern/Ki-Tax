# ErweiterteBetreuungContainer ist neu und zwingend. Fuer jede Betreuung einfuegen
INSERT INTO erweiterte_betreuung_container (
	SELECT
		UUID(),
		b.timestamp_erstellt,
		b.timestamp_mutiert,
		b.user_erstellt,
		b.user_mutiert,
		b.version,
		NULL,
		b.id,
		NULL,
		NULL
	FROM betreuung b
	WHERE b.id NOT IN (
		SELECT e.betreuung_id
		FROM erweiterte_betreuung_container e)
);