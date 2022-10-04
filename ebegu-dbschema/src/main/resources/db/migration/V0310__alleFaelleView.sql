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
	antrag_id BINARY(16)   NOT NULL,
	dossier_id BINARY(16)   NOT NULL,
	antrag_status VARCHAR(255) NOT NULL,
	interne_pendenz BOOLEAN NOT NULL DEFAULT false,
	dokumente_hochgeladen BOOLEAN NOT NULL DEFAULT false,
	fallnummer  VARCHAR(255) NOT NULL,
	gemeinde_id BINARY(16)   NOT NULL,
	gemeinde_name VARCHAR(255) NOT NULL,
	gesuchsperiode_id BINARY(16) NOT NULL,
	gesuchsperiode_string VARCHAR(255) NOT NULL,
    verantwortlicher_bg VARCHAR(255),
    verantwortlicher_bg_id BINARY(16),
    verantwortlicher_ts VARCHAR(255),
    verantwortlicher_ts_id BINARY(16),
    antrag_typen VARCHAR(255),
    verantwortlicher_gemeinde VARCHAR(255),
    verantwortlicher_gemiende_id BINARY(16),
    familien_name VARCHAR(510),
    kinder VARCHAR(510),
	aenderungsdatum DATETIME,
	angebote VARCHAR(255),
	sozialdienst BOOLEAN NOT NULL DEFAULT false,
	has_besitzer BOOLEAN NOT NULL DEFAULT false,
);

