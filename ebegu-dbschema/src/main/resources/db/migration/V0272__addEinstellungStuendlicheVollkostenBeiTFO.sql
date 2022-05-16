
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gesuchsperiode_id)
	(
		SELECT UNHEX(REPLACE(UUID(), '-', '')) AS id,
			NOW() AS timestamp_erstellt,
			NOW() AS timestamp_muiert,
			'ebegu' AS user_erstellt,
			'ebegu' AS user_mutiert,
			'0' AS version,
			'STUENDLICHE_VOLLKOSTEN_BEI_TFO' AS einstellungkey,
			'false' AS value,
			id AS gesuchsperiode_id
		FROM gesuchsperiode
	);

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
SET value = 'true'
	WHERE einstellung_key = 'STUENDLICHE_VOLLKOSTEN_BEI_TFO' AND mandant_identifier = 'LUZERN';