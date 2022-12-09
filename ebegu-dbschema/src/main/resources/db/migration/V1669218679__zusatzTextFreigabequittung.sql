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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

ALTER TABLE gemeinde_stammdaten CHANGE COLUMN has_zusatz_text has_zusatz_text_verfuegung bit(1) NOT NULL DEFAULT FALSE;
ALTER TABLE gemeinde_stammdaten CHANGE COLUMN zusatz_text zusatz_text_verfuegung text;

ALTER TABLE gemeinde_stammdaten_aud CHANGE COLUMN has_zusatz_text has_zusatz_text_verfuegung bit(1);
ALTER TABLE gemeinde_stammdaten_aud CHANGE COLUMN zusatz_text zusatz_text_verfuegung text;

ALTER TABLE gemeinde_stammdaten
ADD IF NOT EXISTS has_zusatz_text_freigabequittung bit(1) NOT NULL DEFAULT FALSE;

ALTER TABLE gemeinde_stammdaten
ADD IF NOT EXISTS zusatz_text_freigabequittung text;

ALTER TABLE gemeinde_stammdaten_aud
ADD IF NOT EXISTS has_zusatz_text_freigabequittung bit(1);

ALTER TABLE gemeinde_stammdaten_aud
ADD IF NOT EXISTS zusatz_text_freigabequittung text;
