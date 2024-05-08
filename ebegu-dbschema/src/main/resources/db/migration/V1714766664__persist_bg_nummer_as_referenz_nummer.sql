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

-- region betreuung
ALTER TABLE betreuung
	ADD COLUMN IF NOT EXISTS referenz_nummer VARCHAR(100) DEFAULT NULL;

ALTER TABLE betreuung_aud
	ADD COLUMN IF NOT EXISTS referenz_nummer VARCHAR(100) DEFAULT NULL;

UPDATE betreuung b
	INNER JOIN kind_container kc ON b.kind_id = kc.id
	INNER JOIN gesuch g ON kc.gesuch_id = g.id
	INNER JOIN dossier d ON g.dossier_id = d.id
	INNER JOIN fall f ON d.fall_id = f.id
	INNER JOIN gemeinde ON d.gemeinde_id = gemeinde.id
	INNER JOIN gesuchsperiode gp ON g.gesuchsperiode_id = gp.id
SET referenz_nummer = CONCAT_WS('.',
	SUBSTR(gp.gueltig_ab, 3, 2),
	LPAD(f.fall_nummer, 6, '0'),
	LPAD(gemeinde.gemeinde_nummer, 3, '0'),
	kc.kind_nummer,
	betreuung_nummer)
WHERE referenz_nummer IS NULL;

ALTER TABLE betreuung
	ALTER COLUMN referenz_nummer DROP DEFAULT;

CREATE INDEX IF NOT EXISTS IX_betreuung_referenz_nummer ON betreuung(referenz_nummer);
-- endregion

-- region ferieninsel
ALTER TABLE anmeldung_ferieninsel
	ADD COLUMN IF NOT EXISTS referenz_nummer VARCHAR(100) DEFAULT NULL;

ALTER TABLE anmeldung_ferieninsel_aud
	ADD COLUMN IF NOT EXISTS referenz_nummer VARCHAR(100) DEFAULT NULL;

UPDATE anmeldung_ferieninsel b
	INNER JOIN kind_container kc ON b.kind_id = kc.id
	INNER JOIN gesuch g ON kc.gesuch_id = g.id
	INNER JOIN dossier d ON g.dossier_id = d.id
	INNER JOIN fall f ON d.fall_id = f.id
	INNER JOIN gemeinde ON d.gemeinde_id = gemeinde.id
	INNER JOIN gesuchsperiode gp ON g.gesuchsperiode_id = gp.id
SET referenz_nummer = CONCAT_WS('.',
	SUBSTR(gp.gueltig_ab, 3, 2),
	LPAD(f.fall_nummer, 6, '0'),
	LPAD(gemeinde.gemeinde_nummer, 3, '0'),
	kc.kind_nummer,
	betreuung_nummer)
WHERE referenz_nummer IS NULL;

ALTER TABLE anmeldung_ferieninsel
	ALTER COLUMN referenz_nummer DROP DEFAULT;

CREATE INDEX IF NOT EXISTS IX_anmeldung_ferieninsel_referenz_nummer ON anmeldung_ferieninsel(referenz_nummer);
-- endregion

-- region tagesschule
ALTER TABLE anmeldung_tagesschule
	ADD COLUMN IF NOT EXISTS referenz_nummer VARCHAR(100) DEFAULT NULL;

ALTER TABLE anmeldung_tagesschule_aud
	ADD COLUMN IF NOT EXISTS referenz_nummer VARCHAR(100) DEFAULT NULL;

UPDATE anmeldung_tagesschule b
	INNER JOIN kind_container kc ON b.kind_id = kc.id
	INNER JOIN gesuch g ON kc.gesuch_id = g.id
	INNER JOIN dossier d ON g.dossier_id = d.id
	INNER JOIN fall f ON d.fall_id = f.id
	INNER JOIN gemeinde ON d.gemeinde_id = gemeinde.id
	INNER JOIN gesuchsperiode gp ON g.gesuchsperiode_id = gp.id
SET referenz_nummer = CONCAT_WS('.',
	SUBSTR(gp.gueltig_ab, 3, 2),
	LPAD(f.fall_nummer, 6, '0'),
	LPAD(gemeinde.gemeinde_nummer, 3, '0'),
	kc.kind_nummer,
	b.betreuung_nummer)
WHERE referenz_nummer IS NULL;

ALTER TABLE anmeldung_tagesschule
	ALTER COLUMN referenz_nummer DROP DEFAULT;

CREATE INDEX IF NOT EXISTS IX_anmeldung_tagesschule_referenz_nummer ON anmeldung_tagesschule(referenz_nummer);
-- endregion
