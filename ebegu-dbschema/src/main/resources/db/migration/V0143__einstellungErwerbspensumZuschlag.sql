INSERT INTO einstellung (
	SELECT
		UUID(),
		p.timestamp_erstellt,
		p.timestamp_mutiert,
		p.user_erstellt,
		p.user_mutiert,
		0,
		'ERWERBSPENSUM_ZUSCHLAG',
		20,
		NULL,
		p.id,
		NULL

	FROM gesuchsperiode p
);