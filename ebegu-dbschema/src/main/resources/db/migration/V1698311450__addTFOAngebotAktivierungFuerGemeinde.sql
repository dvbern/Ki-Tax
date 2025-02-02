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
ALTER TABLE gemeinde ADD angebotbgtfo bit NOT NULL default false;
ALTER TABLE gemeinde_aud ADD angebotbgtfo bit;

UPDATE gemeinde SET angebotbgtfo = true
        WHERE id in (select id from gemeinde
                          where mandant_id in
                                (select application_property.mandant_id from application_property where name = 'ANGEBOT_TFO_ENABLED' and value = 'true'));
