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

# bern
INSERT INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE('d3a81288-5cc7-11ec-93ed-f4390979fa3e', '-','')), UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')),'2021-12-14 12:00:00', '2021-12-14 12:00:00', 'flyway', 'flyway', 0, null, 'INFOMA_ZAHLUNGEN', 'false');

# solothurn
INSERT INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE('4d759f85-5cc8-11ec-93ed-f4390979fa3e', '-','')), UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-', '')),'2021-12-14 12:00:00', '2021-12-14 12:00:00', 'flyway', 'flyway', 0, null, 'INFOMA_ZAHLUNGEN', 'false');

# luzern
INSERT INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE('522c58af-5cc8-11ec-93ed-f4390979fa3e', '-','')), UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', '')),'2021-12-14 12:00:00', '2021-12-14 12:00:00', 'flyway', 'flyway', 0, null, 'INFOMA_ZAHLUNGEN', 'true');

ALTER TABLE institution_stammdaten_betreuungsgutscheine ADD COLUMN infoma_kreditorennummer VARCHAR(255);
ALTER TABLE institution_stammdaten_betreuungsgutscheine_aud ADD COLUMN infoma_kreditorennummer VARCHAR(255);

ALTER TABLE institution_stammdaten_betreuungsgutscheine ADD COLUMN infoma_bankcode VARCHAR(255);
ALTER TABLE institution_stammdaten_betreuungsgutscheine_aud ADD COLUMN infoma_bankcode VARCHAR(255);

# rename mahlzeiten auszahlungsdaten
alter table familiensituation drop constraint UK_familiensituation_auszahlungsdaten_id;
alter table familiensituation drop constraint FK_familiensituation_auszahlungsdaten_id;

alter table familiensituation change auszahlungsdaten_id auszahlungsdaten_mahlzeiten binary(16);
alter table familiensituation_aud change auszahlungsdaten_id auszahlungsdaten_mahlzeiten binary(16);

alter table familiensituation
	add constraint UK_familiensituation_auszahlungsdaten_mahlzeiten_id unique (auszahlungsdaten_mahlzeiten_id);

alter table familiensituation
	add constraint FK_familiensituation_auszahlungsdaten_mahlzeiten_id
		foreign key (auszahlungsdaten_mahlzeiten_id)
			references auszahlungsdaten (id);

alter table familiensituation change abweichende_zahlungsadresse abweichende_zahlungsadresse_mahlzeiten BIT NOT NULL DEFAULT FALSE;
alter table familiensituation_aud change abweichende_zahlungsadresse abweichende_zahlungsadresse_mahlzeiten BIT;

# infoma properties familiensituation
alter table familiensituation add column auszahlungsdaten_infoma_id binary(16);
alter table familiensituation_aud add column auszahlungsdaten_infoma_id binary(16);

alter table familiensituation
	add constraint UK_familiensituation_auszahlungsdaten_infoma_id unique (auszahlungsdaten_infoma_id);

alter table familiensituation
	add constraint FK_familiensituation_auszahlungsdaten_infoma_id
		foreign key (auszahlungsdaten_infoma_id)
			references auszahlungsdaten (id);

alter table familiensituation add column abweichende_zahlungsadresse_infoma BIT NOT NULL DEFAULT FALSE;
alter table familiensituation_aud add column abweichende_zahlungsadresse_infoma BIT;

ALTER TABLE familiensituation ADD COLUMN infoma_kreditorennummer VARCHAR(255);
ALTER TABLE familiensituation_aud ADD COLUMN infoma_kreditorennummer VARCHAR(255);

ALTER TABLE familiensituation ADD COLUMN infoma_bankcode VARCHAR(255);
ALTER TABLE familiensituation_aud ADD COLUMN infoma_bankcode VARCHAR(255);

alter table familiensituation add column auszahlung_an_eltern BIT NOT NULL DEFAULT FALSE;
alter table familiensituation_aud add column auszahlung_an_eltern BIT;
