INSERT INTO gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, status,
							datum_aktiviert, datum_freischaltung_tagesschule, datum_erster_schultag)
VALUES (
		   UNHEX(REPLACE('0621fb5d-a187-5a91-abaf-8a813c4d263a', '-', ''))
		   , '2018-01-01 00:00:00'
		   , '2018-01-01 00:00:00'
		   , 'flyway'
		   , 'flyway'
		   , 0
		   , null
		   , '2019-08-01'
		   , '2020-07-31'
		   , 'ENTWURF'
		   , null
		   , null
		   , null
		   );
