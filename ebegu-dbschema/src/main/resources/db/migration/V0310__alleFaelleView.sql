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

CREATE TABLE alle_faelle_view (
	antrag_id BINARY(16) NOT NULL,
	dossier_id BINARY(16) NOT NULL,
	fall_id BINARY(16) NOT NULL,
	fallnummer  VARCHAR(255) NOT NULL,
	besitzer_id VARCHAR(255),
	besitzer_username VARCHAR(255),
	gemeinde_id BINARY(16)   NOT NULL,
	gemeinde_name VARCHAR(255) NOT NULL,
	antrag_status VARCHAR(255) NOT NULL,
	antrag_typ VARCHAR(255),
	eingangsart VARCHAR(255) NOT NULL,
	laufNummer INTEGER NOT NULL,
	familien_name VARCHAR(510),
	kinder VARCHAR(1024),
	angebot_typen VARCHAR(255),
	aenderungsdatum DATETIME,
	sozialdienst BOOLEAN NOT NULL DEFAULT false,
	sozialdienst BOOLEAN NOT NULL DEFAULT false,
	interne_pendenz BOOLEAN NOT NULL DEFAULT false,
	dokumente_hochgeladen BOOLEAN NOT NULL DEFAULT false,
	gesuchsperiode_id BINARY(16) NOT NULL,
	gesuchsperiode_string VARCHAR(255) NOT NULL,
	verantwortlicher_bg_id BINARY(16),
    verantwortlicher_bg VARCHAR(255),
	verantwortlicher_ts_id BINARY(16),
    verantwortlicher_ts VARCHAR(255),
	verantwortlicher_gemeinde_id BINARY(16),
    verantwortlicher_gemeinde VARCHAR(255),
	PRIMARY KEY (antrag_id)
);

CREATE INDEX IX_alle_faelle_view_gemeinde_id ON alle_faelle_view(gemeinde_id);
CREATE INDEX IX_alle_faelle_view_gesuchsperiode_id ON alle_faelle_view(gesuchsperiode_id);
CREATE INDEX IX_alle_faelle_view_verantwortlicher_bg_id ON alle_faelle_view(verantwortlicher_bg_id);
CREATE INDEX IX_alle_faelle_view_verantwortlicher_ts_id ON alle_faelle_view(verantwortlicher_ts_id);
CREATE INDEX IX_alle_faelle_view_verantwortlicher_gemeinde_id ON alle_faelle_view(verantwortlicher_gemeinde_id);
CREATE INDEX IX_alle_faelle_view_fall_id ON alle_faelle_view(fall_id);
CREATE INDEX IX_alle_faelle_view_besitzer_id ON alle_faelle_view(besitzer_id);
