/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

export enum Permission {
    BENUTZER_EINLADEN = 'BENUTZER_EINLADEN',
    BENUTZER_EINLADEN_AS_GEMEINDE = 'BENUTZER_EINLADEN_AS_GEMEINDE',
    ROLE_TRAEGERSCHAFT = 'ROLE_TRAEGERSCHAFT',
    ROLE_INSTITUTION = 'ROLE_INSTITUTION',
    ROLE_GEMEINDE = 'ROLE_GEMEINDE',
    ROLE_MANDANT = 'ROLE_MANDANT',
    ROLE_BG = 'ROLE_BG', // todo remove in KIBON version 2. use Permission.ROLE_GEMEINDE instead,
    ROLE_TS = 'ROLE_TS', // todo remove in KIBON version 2. use Permission.ROLE_GEMEINDE instead,
    ROLE_SOZIALDIENST = 'ROLE_SOZIALDIENST',
    BENUTZER_FERIENBETREUUNG_EINLADEN = 'BENUTZER_FERIENBETREUUNG_EINLADEN',
}
