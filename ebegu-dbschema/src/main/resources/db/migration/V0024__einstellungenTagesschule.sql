INSERT INTO einstellung (id,
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
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2019-06-28 00:00:00'              as timestamp_erstellt,
			 '2019-06-28 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' as einstellung_key,
			 gp.gueltig_ab                      as value,
			 NULL                               as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 NULL                               as mandant_id
	  from gesuchsperiode as gp) as tmp;

INSERT INTO einstellung (id,
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
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2019-06-28 00:00:00'              as timestamp_erstellt,
			 '2019-06-28 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' as einstellung_key,
			 gp.gueltig_ab                      as value,
			 NULL                               as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 NULL                               as mandant_id
	  from gesuchsperiode as gp) as tmp;


alter table gesuchsperiode
	drop column datum_freischaltung_tagesschule;

alter table gesuchsperiode
	drop column datum_erster_schultag;
