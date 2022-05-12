/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

UPDATE einstellung SET VALUE='KLASSE9' WHERE einstellung_key='SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE';

/* we need to reset all the einstellung that were settet before the GP creation */

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '18'
WHERE einstellung_key = 'DAUER_BABYTARIF' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = 'true'
WHERE einstellung_key = 'DIPLOMATENSTATUS_DEAKTIVIERT' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = 'true'
WHERE einstellung_key = 'SPRACHE_AMTSPRACHE_DISABLED' AND mandant_identifier = 'LUZERN';

/* and to set all the known properties for luzern */

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = 'true'
WHERE einstellung_key = 'KESB_PLATZIERUNG_DEAKTIVIEREN' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = 'KEINE'
WHERE einstellung_key = 'KINDERABZUG_TYP' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = 'true'
WHERE einstellung_key = 'BESONDERE_BEDUERFNISSE_LUZERN' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = 'true'
WHERE einstellung_key = 'GESCHWISTERNBONUS_AKTIVIERT' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = 'true'
WHERE einstellung_key = 'KITAPLUS_ZUSCHLAG_AKTIVIERT' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = 'true'
WHERE einstellung_key = 'ZEMIS_DISABLED' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '125000'
WHERE einstellung_key = 'MAX_MASSGEBENDES_EINKOMMEN' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '48000'
WHERE einstellung_key = 'MIN_MASSGEBENDES_EINKOMMEN' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '100'
WHERE einstellung_key = 'FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '100'
WHERE einstellung_key = 'FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '15'
WHERE einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_TG' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '246'
WHERE einstellung_key = 'OEFFNUNGSTAGE_KITA' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '246'
WHERE einstellung_key = 'OEFFNUNGSTAGE_TFO' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '0'
WHERE einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '0'
WHERE einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '0'
WHERE einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '0'
WHERE einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = 'LUZERN'
WHERE einstellung_key = 'FINANZIELLE_SITUATION_TYP' AND mandant_identifier = 'LUZERN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = 'SOLOTHURN'
WHERE einstellung_key = 'FINANZIELLE_SITUATION_TYP' AND mandant_identifier = 'SOLOTHURN';

INSERT IGNORE INTO sequence(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, sequence_type, current_value, mandant_id)
VALUES (
	UNHEX(REPLACE('09399d46-537b-11ec-98e8-f4390979fa3e', '-', '')), # id
	'2018-01-01 00:00:00', # timestamp_erstellt
	'2018-01-01 00:00:00', # timestamp_mutiert
	'flyway', # user_erstellt
	'flyway', # user_mutiert
	0, # version
	'FALL_NUMMER', # sequence_type
	100, # current_value
	UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-', '')));

INSERT IGNORE INTO sequence(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, sequence_type, current_value, mandant_id)
VALUES (
	UNHEX(REPLACE('9f413fb6-3262-11ec-a17e-b89a2ae4a038', '-', '')), # id
	'2018-01-01 00:00:00', # timestamp_erstellt
	'2018-01-01 00:00:00', # timestamp_mutiert
	'flyway', # user_erstellt
	'flyway', # user_mutiert
	0, # version
	'FALL_NUMMER', # sequence_type
	100, # current_value
	UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', '')));