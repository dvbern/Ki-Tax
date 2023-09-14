/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

SET @mandant_id_ar = UNHEX(REPLACE('5b9e6fa4-3991-11ed-a63d-b05cda43de9c', '-', ''));

# Disable Gemeindespezifische Konfigurationen for BG
UPDATE einstellung
	INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id
	INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
SET value = FALSE
WHERE einstellung_key = 'GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN' AND mandant_identifier = 'APPENZELL_AUSSERRHODEN';

# Remove potentially existing gemeindespezifische Konfigurationen for BG
DELETE einstellung
FROM einstellung
	 INNER JOIN gemeinde ON einstellung.gemeinde_id = gemeinde.id
	 INNER JOIN mandant m ON gemeinde.mandant_id = m.id
WHERE einstellung_key = 'MIN_MASSGEBENDES_EINKOMMEN' AND mandant_identifier = 'APPENZELL_AUSSERRHODEN' OR
			einstellung_key = 'MAX_MASSGEBENDES_EINKOMMEN' AND mandant_identifier = 'APPENZELL_AUSSERRHODEN' OR
			einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG' AND
			mandant_identifier = 'APPENZELL_AUSSERRHODEN' OR
			einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD' AND
			mandant_identifier = 'APPENZELL_AUSSERRHODEN' OR
			einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG' AND
			mandant_identifier = 'APPENZELL_AUSSERRHODEN' OR
			einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_ST' AND
			mandant_identifier = 'APPENZELL_AUSSERRHODEN' OR
			einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG' AND
			mandant_identifier = 'APPENZELL_AUSSERRHODEN' OR
			einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD' AND
			mandant_identifier = 'APPENZELL_AUSSERRHODEN' OR
			einstellung_key = 'MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD' AND mandant_identifier = 'APPENZELL_AUSSERRHODEN' OR
			einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_TG' AND mandant_identifier = 'APPENZELL_AUSSERRHODEN' OR
			einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_STD' AND mandant_identifier = 'APPENZELL_AUSSERRHODEN' OR
			einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_TG' AND mandant_identifier = 'APPENZELL_AUSSERRHODEN' OR
			einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_STD' AND mandant_identifier = 'APPENZELL_AUSSERRHODEN';


# Update Kinder haben bis und mit ... Anspruch
UPDATE einstellung
	INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id
	INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
SET value = 'KLASSE1'
WHERE einstellung_key = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' AND m.mandant_identifier = 'APPENZELL_AUSSERRHODEN';

UPDATE einstellung
	INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id
	INNER JOIN mandant m2 ON einstellung.mandant_id = m2.id
SET value = 'KLASSE1'
WHERE einstellung_key = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' AND m2.mandant_identifier = 'APPENZELL_AUSSERRHODEN';

# GP 22/23
INSERT IGNORE INTO gesuchsperiode
VALUES (UUID(), NOW(), NOW(), 'system', 'system', 0, NULL,
		'2022-08-01', '2023-07-31', NULL,
		'ENTWURF', NULL, NULL, NULL,
		NULL, NULL, NULL, @mandant_id_ar, NULL,
		NULL);

UPDATE gesuchsperiode
INNER JOIN mandant ON gesuchsperiode.mandant_id = mandant.id
SET gesuchsperiode.vorgaenger_id = NULL
WHERE mandant_identifier = 'APPENZELL_AUSSERRHODEN' AND status = 'ENTWURF';

SET @gp_ar_2223 = (SELECT gesuchsperiode.id
				   FROM gesuchsperiode
						INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
				   WHERE mandant_identifier = 'APPENZELL_AUSSERRHODEN' AND gesuchsperiode.gueltig_ab = '2022-08-01');

SET @gp_ar_2324 = (SELECT gesuchsperiode.id
				   FROM gesuchsperiode
						INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
				   WHERE mandant_identifier = 'APPENZELL_AUSSERRHODEN' AND gesuchsperiode.gueltig_ab = '2023-08-01');

# Einstellungen für Gesuchsperiode kopieren
INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'system', 'system', 0, einstellung_key, value, NULL, @gp_ar_2223, NULL, erklaerung
FROM einstellung
	 INNER JOIN gesuchsperiode g ON einstellung.gesuchsperiode_id = g.id
	 INNER JOIN mandant m2 ON g.mandant_id = m2.id
WHERE m2.mandant_identifier = 'APPENZELL_AUSSERRHODEN' AND gueltig_ab = '2023-08-01' AND gemeinde_id IS NULL AND
	einstellung.mandant_id IS NULL;


# Gemeinde Einstellungen für Gesuchsperiode kopieren
INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'system', 'system', 0, einstellung_key, value, gemeinde_id, @gp_ar_2223, @mandant_id_ar, erklaerung
FROM einstellung
	 INNER JOIN gesuchsperiode g ON einstellung.gesuchsperiode_id = g.id
	 INNER JOIN mandant m2 ON g.mandant_id = m2.id
WHERE mandant_identifier = 'APPENZELL_AUSSERRHODEN' AND
		gesuchsperiode_id = @gp_ar_2324 AND
	NOT EXISTS(SELECT einstellung_key
			   FROM einstellung e
			   WHERE e.gesuchsperiode_id = @gp_ar_2223 AND
				   e.mandant_id = @mandant_id_ar AND e.einstellung_key = einstellung.einstellung_key) AND
	gemeinde_id IS NULL;