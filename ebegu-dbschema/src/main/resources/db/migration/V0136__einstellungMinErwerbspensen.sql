INSERT INTO einstellung (
	SELECT
		UUID(),
		p.timestamp_erstellt,
		p.timestamp_mutiert,
		p.user_erstellt,
		p.user_mutiert,
		0,
		'MIN_ERWERBSPENSUM_NICHT_EINGESCHULT',
		20,
		NULL,
		p.id,
		NULL

	FROM gesuchsperiode p
);

INSERT INTO einstellung (
	SELECT
		UUID(),
		p.timestamp_erstellt,
		p.timestamp_mutiert,
		p.user_erstellt,
		p.user_mutiert,
		0,
		'MIN_ERWERBSPENSUM_EINGESCHULT',
		40,
		NULL,
		p.id,
		NULL

	FROM gesuchsperiode p
);