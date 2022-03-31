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

INSERT INTO fachstelle (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, fachstelle_anspruch, fachstelle_erweiterte_betreuung, gueltig_ab, gueltig_bis, mandant_id) VALUES (UNHEX(REPLACE('5923ded4-d2ea-11e8-a8d5-f2801f1b9fcd', '-', '')), '2022-02-03 00:00:00', '2022-02-03 00:00:00', 'flyway', 'flyway', 0, null, 'HEILPAEDOGOGISCHER_DIENST', false, true,  '2022-08-01', '9999-12-31', UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-', '')));
INSERT INTO fachstelle (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, fachstelle_anspruch, fachstelle_erweiterte_betreuung, gueltig_ab, gueltig_bis, mandant_id) VALUES (UNHEX(REPLACE('5923ded4-d2ea-11e8-a8d5-f2801f1b9ff1', '-', '')), '2022-02-03 00:00:00', '2022-02-03 00:00:00', 'flyway', 'flyway', 0, null, 'ZUSTAENDIGE_SOZIALREGION', false, true,  '2022-08-01', '9999-12-31', UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-', '')));

