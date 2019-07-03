INSERT INTO einstellung (
	id,
	timestamp_erstellt,
	timestamp_mutiert,
	user_erstellt,
	user_mutiert,
	version,
	einstellung_key,
	value,
	gemeinde_id,
	gesuchsperiode_id,
	mandant_id)
VALUES (
	UNHEX(REPLACE(UUID(), '-', '')),
	'2019-06-28 00:00:00',
	'2019-06-28 00:00:00',
	'flyway',
	'flyway',
	0,
	'TAGESSCHULE_ENABLED_FOR_MANDANT',
	'false',
	NULL,
	(select id from gesuchsperiode limit 1),
	NULL);
