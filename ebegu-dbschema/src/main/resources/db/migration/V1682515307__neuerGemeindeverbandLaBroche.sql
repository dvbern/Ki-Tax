
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
-- 10050	Schulgemeindeverband Communauté scolaire de La Baroche
INSERT IGNORE INTO bfs_gemeinde (id, mandant_id, kanton, bfs_nummer, name, gueltig_ab) VALUES
	('b2da2de8-e435-11ed-9f94-00090ffe0001', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')), 'BE', 10050, 'Communauté scolaire de La Baroche', '2010-01-01');
