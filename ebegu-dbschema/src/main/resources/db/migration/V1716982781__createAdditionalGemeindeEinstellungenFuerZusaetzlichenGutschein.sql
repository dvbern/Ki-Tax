/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

-- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TYP
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')) as id,
			 '2020-01-01 00:00:00' as timestamp_erstellt,
			 '2020-01-01 00:00:00' as timestamp_mutiert,
			 'flyway' as user_erstellt,
			 'flyway' as user_mutiert,
			 0 as version,
			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_TYP' as einstellung_key,
			 'PAUSCHAL' as value,
			 NULL as gemeinde_id,
			 gp.id as gesuchsperiode_id,
			 gp.mandant_id as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_KITA_MIN
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')) as id,
			 '2020-01-01 00:00:00' as timestamp_erstellt,
			 '2020-01-01 00:00:00' as timestamp_mutiert,
			 'flyway' as user_erstellt,
			 'flyway' as user_mutiert,
			 0 as version,
			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_KITA_MIN' as einstellung_key,
			 '0' as value,
			 NULL as gemeinde_id,
			 gp.id as gesuchsperiode_id,
			 gp.mandant_id as mandant_id
	  from gesuchsperiode as gp) as tmp;


-- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_KITA_MAX
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')) as id,
			 '2020-01-01 00:00:00' as timestamp_erstellt,
			 '2020-01-01 00:00:00' as timestamp_mutiert,
			 'flyway' as user_erstellt,
			 'flyway' as user_mutiert,
			 0 as version,
			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_KITA_MAX' as einstellung_key,
			 '0' as value,
			 NULL as gemeinde_id,
			 gp.id as gesuchsperiode_id,
			 gp.mandant_id as mandant_id
	  from gesuchsperiode as gp) as tmp;


-- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_TFO_MIN
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')) as id,
			 '2020-01-01 00:00:00' as timestamp_erstellt,
			 '2020-01-01 00:00:00' as timestamp_mutiert,
			 'flyway' as user_erstellt,
			 'flyway' as user_mutiert,
			 0 as version,
			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_TFO_MIN' as einstellung_key,
			 '0' as value,
			 NULL as gemeinde_id,
			 gp.id as gesuchsperiode_id,
			 gp.mandant_id as mandant_id
	  from gesuchsperiode as gp) as tmp;


-- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_TFO_MAX
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')) as id,
			 '2020-01-01 00:00:00' as timestamp_erstellt,
			 '2020-01-01 00:00:00' as timestamp_mutiert,
			 'flyway' as user_erstellt,
			 'flyway' as user_mutiert,
			 0 as version,
			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_TFO_MAX' as einstellung_key,
			 '0' as value,
			 NULL as gemeinde_id,
			 gp.id as gesuchsperiode_id,
			 gp.mandant_id as mandant_id
	  from gesuchsperiode as gp) as tmp;


-- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_MIN_MASSGEBENDES_EINKOMMEN
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')) as id,
			 '2020-01-01 00:00:00' as timestamp_erstellt,
			 '2020-01-01 00:00:00' as timestamp_mutiert,
			 'flyway' as user_erstellt,
			 'flyway' as user_mutiert,
			 0 as version,
			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_MIN_MASSGEBENDES_EINKOMMEN' as einstellung_key,
			 (SELECT value FROM einstellung WHERE einstellung_key = 'MIN_MASSGEBENDES_EINKOMMEN' AND gesuchsperiode_id = gp.id AND gemeinde_id IS NULL) as value,
			 NULL as gemeinde_id,
			 gp.id as gesuchsperiode_id,
			 gp.mandant_id as mandant_id
	  from gesuchsperiode as gp) as tmp;


-- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_TFO_MAX
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')) as id,
			 '2020-01-01 00:00:00' as timestamp_erstellt,
			 '2020-01-01 00:00:00' as timestamp_mutiert,
			 'flyway' as user_erstellt,
			 'flyway' as user_mutiert,
			 0 as version,
			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_MAX_MASSGEBENDES_EINKOMMEN' as einstellung_key,
			 (SELECT value FROM einstellung WHERE einstellung_key = 'MAX_MASSGEBENDES_EINKOMMEN' AND gesuchsperiode_id = gp.id AND gemeinde_id IS NULL),
			 NULL as gemeinde_id,
			 gp.id as gesuchsperiode_id,
			 gp.mandant_id as mandant_id
	  from gesuchsperiode as gp) as tmp;

