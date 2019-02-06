INSERT INTO gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis,
								  status, datum_aktiviert, datum_freischaltung_tagesschule, datum_erster_schultag)
VALUES (UNHEX(REPLACE('0621fb5d-a187-5a91-abaf-8a813c4d263a', '-','')), '2016-05-30 16:39:38', '2016-05-30 16:39:38', 'flyway', 'flyway', 0, NULL, '2017-08-01', '2018-07-31', 'AKTIV',
		NULL, NULL, NULL);