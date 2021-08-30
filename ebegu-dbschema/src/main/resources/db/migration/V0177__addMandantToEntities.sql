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

ALTER TABLE traegerschaft ADD COLUMN IF NOT EXISTS mandant_id BINARY(16) NOT NULL DEFAULT (UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')));
ALTER TABLE traegerschaft_aud ADD COLUMN IF NOT EXISTS mandant_id BINARY(16);

ALTER TABLE traegerschaft
	ADD CONSTRAINT FK_traegerschaft_mandant_id
		FOREIGN KEY (mandant_id)
			REFERENCES mandant (id);


ALTER TABLE application_property ADD COLUMN IF NOT EXISTS mandant_id BINARY(16) NOT NULL DEFAULT (UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')));
ALTER TABLE application_property_aud ADD COLUMN IF NOT EXISTS mandant_id BINARY(16);

ALTER TABLE application_property
	ADD CONSTRAINT FK_application_property_mandant_id
		FOREIGN KEY (mandant_id)
			REFERENCES mandant (id);


ALTER TABLE fachstelle ADD COLUMN IF NOT EXISTS mandant_id BINARY(16) NOT NULL DEFAULT (UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')));
ALTER TABLE fachstelle_aud ADD COLUMN IF NOT EXISTS mandant_id BINARY(16);

ALTER TABLE fachstelle
	ADD CONSTRAINT FK_fachstelle_mandant_id
		FOREIGN KEY (mandant_id)
			REFERENCES mandant (id);


ALTER TABLE gesuchsperiode ADD COLUMN IF NOT EXISTS mandant_id BINARY(16) NOT NULL DEFAULT (UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')));
ALTER TABLE gesuchsperiode_aud ADD COLUMN IF NOT EXISTS mandant_id BINARY(16);

ALTER TABLE gesuchsperiode
	ADD CONSTRAINT FK_gesuchsperiode_mandant_id
		FOREIGN KEY (mandant_id)
			REFERENCES mandant (id);


ALTER TABLE lastenausgleich ADD COLUMN IF NOT EXISTS mandant_id BINARY(16) NOT NULL DEFAULT (UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')));
ALTER TABLE lastenausgleich_aud ADD COLUMN IF NOT EXISTS mandant_id BINARY(16);

ALTER TABLE lastenausgleich
	ADD CONSTRAINT FK_lastenausgleich_mandant_id
		FOREIGN KEY (mandant_id)
			REFERENCES mandant (id);

