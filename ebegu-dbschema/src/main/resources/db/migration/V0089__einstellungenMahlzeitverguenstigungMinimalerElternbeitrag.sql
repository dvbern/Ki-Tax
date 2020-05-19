-- GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_NEBENMAHLZEIT
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-02-12 00:00:00'              as timestamp_erstellt,
			 '2020-02-12 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_NEBENMAHLZEIT' as einstellung_key,
			 '0.00'								as value,
			 NULL                               as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;
	  
-- GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_HAUPTMAHLZEIT
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-02-12 00:00:00'              as timestamp_erstellt,
			 '2020-02-12 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_HAUPTMAHLZEIT' as einstellung_key,
			 '0.00'								as value,
			 NULL                               as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;
