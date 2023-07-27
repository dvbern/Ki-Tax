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

INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gesuchsperiode_id)
    (
        SELECT UNHEX(REPLACE(UUID(), '-', '')) as id,
            NOW() as timestamp_erstellt,
            NOW() as timestamp_muiert,
            'ebegu' as user_erstellt,
            'ebegu' as user_mutiert,
            '0' as version,
            'EINGEWOEHNUNG_TYP' as einstellungkey,
            'false' as value,
            id as gesuchsperiode_id
        FROM gesuchsperiode
    );

INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gesuchsperiode_id)
    (
        SELECT UNHEX(REPLACE(UUID(), '-', '')) as id,
            NOW() as timestamp_erstellt,
            NOW() as timestamp_muiert,
            'ebegu' as user_erstellt,
            'ebegu' as user_mutiert,
            '0' as version,
            'FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM' as einstellungkey,
            '100' as value,
            id as gesuchsperiode_id
        FROM gesuchsperiode
    );

INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gesuchsperiode_id)
    (
        SELECT UNHEX(REPLACE(UUID(), '-', '')) as id,
            NOW() as timestamp_erstellt,
            NOW() as timestamp_muiert,
            'ebegu' as user_erstellt,
            'ebegu' as user_mutiert,
            '0' as version,
            'FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE' as einstellungkey,
            'VORSCHULALTER' as value,
            id as gesuchsperiode_id
        FROM gesuchsperiode
    );




