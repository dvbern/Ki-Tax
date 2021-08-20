/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

ALTER TABLE institution_stammdaten_betreuungsgutscheine ADD COLUMN IF NOT EXISTS oeffnungstage_pro_jahr INTEGER NOT NULL DEFAULT 0;
ALTER TABLE institution_stammdaten_betreuungsgutscheine_aud ADD COLUMN IF NOT EXISTS oeffnungstage_pro_jahr INTEGER;

ALTER TABLE institution_stammdaten_betreuungsgutscheine
	ALTER COLUMN oeffnungstage_pro_jahr DROP DEFAULT;

ALTER TABLE institution_stammdaten_betreuungsgutscheine ADD COLUMN IF NOT EXISTS auslastung_institutionen DECIMAL(19, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE institution_stammdaten_betreuungsgutscheine_aud ADD COLUMN IF NOT EXISTS auslastung_institutionen DECIMAL(19, 2);

ALTER TABLE institution_stammdaten_betreuungsgutscheine
	ALTER COLUMN auslastung_institutionen DROP DEFAULT;

ALTER TABLE institution_stammdaten_betreuungsgutscheine ADD COLUMN IF NOT EXISTS anzahl_kinder_warteliste DECIMAL(19, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE institution_stammdaten_betreuungsgutscheine_aud ADD COLUMN IF NOT EXISTS anzahl_kinder_warteliste DECIMAL(19, 2);

ALTER TABLE institution_stammdaten_betreuungsgutscheine
	ALTER COLUMN anzahl_kinder_warteliste DROP DEFAULT;

ALTER TABLE institution_stammdaten_betreuungsgutscheine ADD COLUMN IF NOT EXISTS summe_pensum_warteliste DECIMAL(19, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE institution_stammdaten_betreuungsgutscheine_aud ADD COLUMN IF NOT EXISTS summe_pensum_warteliste DECIMAL(19, 2);

ALTER TABLE institution_stammdaten_betreuungsgutscheine
	ALTER COLUMN summe_pensum_warteliste DROP DEFAULT;

ALTER TABLE institution_stammdaten_betreuungsgutscheine ADD COLUMN IF NOT EXISTS dauer_warteliste DECIMAL(19, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE institution_stammdaten_betreuungsgutscheine_aud ADD COLUMN IF NOT EXISTS dauer_warteliste DECIMAL(19, 2);

ALTER TABLE institution_stammdaten_betreuungsgutscheine
	ALTER COLUMN dauer_warteliste DROP DEFAULT;

ALTER TABLE institution_stammdaten_betreuungsgutscheine ADD COLUMN IF NOT EXISTS frueh_eroeffnung BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE institution_stammdaten_betreuungsgutscheine_aud ADD COLUMN IF NOT EXISTS frueh_eroeffnung BOOLEAN;

ALTER TABLE institution_stammdaten_betreuungsgutscheine ADD COLUMN IF NOT EXISTS spaet_eroeffnung BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE institution_stammdaten_betreuungsgutscheine_aud ADD COLUMN IF NOT EXISTS spaet_eroeffnung BOOLEAN;

ALTER TABLE institution_stammdaten_betreuungsgutscheine ADD COLUMN IF NOT EXISTS wochenende_eroeffnung BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE institution_stammdaten_betreuungsgutscheine_aud ADD COLUMN IF NOT EXISTS wochenende_eroeffnung BOOLEAN;

ALTER TABLE institution_stammdaten_betreuungsgutscheine ADD COLUMN IF NOT EXISTS uebernachtung_moeglich BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE institution_stammdaten_betreuungsgutscheine_aud ADD COLUMN IF NOT EXISTS uebernachtung_moeglich BOOLEAN;


ALTER TABLE institution_stammdaten ADD COLUMN IF NOT EXISTS grund_schliessung TEXT;
ALTER TABLE institution_stammdaten_aud ADD COLUMN IF NOT EXISTS grund_schliessung TEXT;

ALTER TABLE institution_stammdaten ADD COLUMN IF NOT EXISTS erinnerung_mail VARCHAR(255);
ALTER TABLE institution_stammdaten_aud ADD COLUMN IF NOT EXISTS erinnerung_mail VARCHAR(255);

ALTER TABLE gemeinde_stammdaten ADD COLUMN IF NOT EXISTS gutschein_selber_ausgestellt BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE gemeinde_stammdaten_aud ADD COLUMN IF NOT EXISTS gutschein_selber_ausgestellt BOOLEAN;

ALTER TABLE gemeinde_stammdaten ADD COLUMN IF NOT EXISTS gemeinde_ausgabestelle_id BINARY(16);
ALTER TABLE gemeinde_stammdaten_aud ADD COLUMN IF NOT EXISTS gemeinde_ausgabestelle_id BINARY(16);

ALTER TABLE zahlungsauftrag
	ADD CONSTRAINT FK_gemeinde_stammdaten_gemeinde_id
		FOREIGN KEY (gemeinde_ausgabestelle_id)
			REFERENCES gemeinde (id);