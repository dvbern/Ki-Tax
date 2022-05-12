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


UPDATE gesuchsperiode set vorgaenger_id = null where vorgaenger_id is not NULL;

UPDATE mandant set ebegu.mandant.activated = true where ebegu.mandant.mandant_identifier='SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = 'false'
WHERE einstellung_key = 'FREIGABE_QUITTUNG_EINLESEN_REQUIRED' AND mandant_identifier = 'SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '7'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_SCHULE_PRO_STD' AND mandant_identifier = 'SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '18'
WHERE einstellung_key = 'DAUER_BABYTARIF' AND mandant_identifier = 'SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '160000'
WHERE einstellung_key = 'MAX_MASSGEBENDES_EINKOMMEN' AND mandant_identifier = 'SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '40000'
WHERE einstellung_key = 'MIN_MASSGEBENDES_EINKOMMEN' AND mandant_identifier = 'SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '7'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_SCHULE_PRO_STD' AND mandant_identifier = 'SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '70'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_SCHULE_PRO_TG' AND mandant_identifier = 'SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '9.5'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD' AND mandant_identifier = 'SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '95'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG' AND mandant_identifier = 'SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '14'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD' AND mandant_identifier = 'SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '140'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG' AND mandant_identifier = 'SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '3'
WHERE einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_STD' AND mandant_identifier = 'SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '30'
WHERE einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_TG' AND mandant_identifier = 'SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '240'
WHERE einstellung_key = 'OEFFNUNGSTAGE_KITA' AND mandant_identifier = 'SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '240'
WHERE einstellung_key = 'OEFFNUNGSTAGE_TFO' AND mandant_identifier = 'SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '10'
WHERE einstellung_key = 'OEFFNUNGSSTUNDEN_TFO' AND mandant_identifier = 'SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '6'
WHERE einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_STD' AND mandant_identifier = 'SOLOTHURN';

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	SET value = '60'
WHERE einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_TG' AND mandant_identifier = 'SOLOTHURN';


