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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

INSERT IGNORE INTO mandant
VALUES (UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', '')), '2021-11-30 00:00:00', '2021-11-30 00:00:00', 'flyway', 'flyway', 0, NULL, 'Kanton Luzern', false, false);
