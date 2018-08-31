INSERT INTO einstellung (
	SELECT
		p.id,
		p.timestamp_erstellt,
		p.timestamp_mutiert,
		p.user_erstellt,
		p.user_mutiert,
		p.version,
		p.name,
		p.value,
		NULL,
		(
			SELECT g.id
			FROM gesuchsperiode g
			WHERE p.gueltig_ab >= g.gueltig_ab AND p.gueltig_bis <= g.gueltig_bis)
		,
		NULL

	FROM ebegu_parameter p where p.name <> 'PARAM_FIXBETRAG_STADT_PRO_TAG_KITA'
);

INSERT INTO einstellung (
	SELECT
		UUID(),
		p.timestamp_erstellt,
		p.timestamp_mutiert,
		p.user_erstellt,
		p.user_mutiert,
		0,
		'PARAM_FIXBETRAG_STADT_PRO_TAG_KITA_HALBJAHR_1',
		8.00,
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
		'PARAM_FIXBETRAG_STADT_PRO_TAG_KITA_HALBJAHR_2',
		8.00,
		NULL,
		p.id,
		NULL

	FROM gesuchsperiode p
);

DROP TABLE ebegu_parameter;
DROP TABLE ebegu_parameter_aud;

DROP INDEX UK_einstellung ON einstellung;
