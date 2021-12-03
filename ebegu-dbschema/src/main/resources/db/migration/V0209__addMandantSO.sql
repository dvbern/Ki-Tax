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
SET @mandant_id_solothurn = UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-', ''));

INSERT IGNORE INTO mandant
VALUES (@mandant_id_solothurn, '2021-11-30 00:00:00', '2021-11-30 00:00:00', 'flyway', 'flyway', 0, NULL, 'Kanton Solothurn', false, false);

# APPLICATION PROPERTIES
INSERT IGNORE INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										 version, vorgaenger_id, name, value, mandant_id)
SELECT UNHEX(REPLACE(UUID(), '-', '')), timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
	NULL, name, value, @mandant_id_solothurn
FROM application_property
WHERE NOT EXISTS(SELECT name
				 FROM application_property a_p
				 WHERE mandant_id = @mandant_id_solothurn AND
						 a_p.name = application_property.name);

# BFS Gemeinden
INSERT INTO bfs_gemeinde (id, mandant_id, kanton, bfs_nummer, name, gueltig_ab)
VALUES
(UUID(), @mandant_id_solothurn, 'SO', 2401, 'Egerkingen', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2402, 'Härkingen', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2403, 'Kestenholz', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2404, 'Neuendorf', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2405, 'Niederbuchsiten', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2406, 'Oberbuchsiten', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2407, 'Oensingen', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2408, 'Wolfwil', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2421, 'Aedermannsdorf', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2422, 'Balsthal', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2424, 'Herbetswil', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2425, 'Holderbank (SO)', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2426, 'Laupersdorf', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2427, 'Matzendorf', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2428, 'Mümliswil-Ramiswil', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2430, 'Welschenrohr-Gänsbrunnen', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2445, 'Biezwil', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2455, 'Lüterkofen-Ichertswil', '1961-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2456, 'Lüterswil-Gächliwil', '1995-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2457, 'Messen', '2010-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2461, 'Schnottwil', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2463, 'Unterramsern', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2464, 'Lüsslingen-Nennigkofen', '2013-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2465, 'Buchegg', '2014-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2471, 'Bättwil', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2472, 'Büren (SO)', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2473, 'Dornach', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2474, 'Gempen', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2475, 'Hochwald', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2476, 'Hofstetten-Flüh', '1986-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2477, 'Metzerlen-Mariastein', '2004-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2478, 'Nuglar-St. Pantaleon', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2479, 'Rodersdorf', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2480, 'Seewen', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2481, 'Witterswil', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2491, 'Hauenstein-Ifenthal', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2492, 'Kienberg', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2493, 'Lostorf', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2495, 'Niedergösgen', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2497, 'Obergösgen', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2499, 'Stüsslingen', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2500, 'Trimbach', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2501, 'Winznau', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2502, 'Wisen (SO)', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2503, 'Erlinsbach (SO)', '2006-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2511, 'Aeschi (SO)', '2012-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2513, 'Biberist', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2514, 'Bolken', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2516, 'Deitingen', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2517, 'Derendingen', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2518, 'Etziken', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2519, 'Gerlafingen', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2520, 'Halten', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2523, 'Horriwil', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2524, 'Hüniken', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2525, 'Kriegstetten', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2526, 'Lohn-Ammannsegg', '1993-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2527, 'Luterbach', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2528, 'Obergerlafingen', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2529, 'Oekingen', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2530, 'Recherswil', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2532, 'Subingen', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2534, 'Zuchwil', '1991-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2535, 'Drei Höfe', '2013-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2541, 'Balm bei Günsberg', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2542, 'Bellach', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2543, 'Bettlach', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2544, 'Feldbrunnen-St. Niklaus', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2545, 'Flumenthal', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2546, 'Grenchen', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2547, 'Günsberg', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2548, 'Hubersdorf', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2549, 'Kammersrohr', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2550, 'Langendorf', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2551, 'Lommiswil', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2553, 'Oberdorf (SO)', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2554, 'Riedholz', '2011-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2555, 'Rüttenen', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2556, 'Selzach', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2571, 'Boningen', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2572, 'Däniken', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2573, 'Dulliken', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2574, 'Eppenberg-Wöschnau', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2575, 'Fulenbach', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2576, 'Gretzenbach', '1973-01-01'),
(UUID(), @mandant_id_solothurn, 'SO', 2578, 'Gunzgen', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2579, 'Hägendorf', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2580, 'Kappel (SO)', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2581, 'Olten', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2582, 'Rickenbach (SO)', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2583, 'Schönenwerd', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2584, 'Starrkirch-Wil', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2585, 'Walterswil (SO)', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2586, 'Wangen bei Olten', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2601, 'Solothurn', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2611, 'Bärschwil', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2612, 'Beinwil (SO)', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2613, 'Breitenbach', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2614, 'Büsserach', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2615, 'Erschwil', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2616, 'Fehren', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2617, 'Grindel', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2618, 'Himmelried', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2619, 'Kleinlützel', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2620, 'Meltingen', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2621, 'Nunningen', '1848-09-12'),
(UUID(), @mandant_id_solothurn, 'SO', 2622, 'Zullwil', '1848-09-12');
