-- GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-03-31 00:00:00'              as timestamp_erstellt,
			 '2020-03-31 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' as einstellung_key,
			 '20'								as value,
			 NULL                               as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-03-31 00:00:00'              as timestamp_erstellt,
			 '2020-03-31 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT' as einstellung_key,
			 '40'								as value,
			 NULL                               as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;