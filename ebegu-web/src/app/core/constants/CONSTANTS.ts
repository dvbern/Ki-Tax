/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

export const CONSTANTS = {
    name: 'EBEGU',
    REST_API: '/ebegu/api/v1/',
    MAX_LENGTH: 255,
    FALLNUMMER_LENGTH: 6,
    GEMEINDENUMMER_LENGTH: 3,
    ID_LENGTH: 36,
    PATTERN_ANY_NUMBER: /-?[0-9]+(\.[0-9]+)?/,
    PATTERN_BETRAG: '([0-9]{0,12})',
    PATTERN_TWO_DECIMALS: '^[0-9]+(\\.[0-9]{1,2})?$',
    PATTERN_PERCENTAGE: '^[0-9][0-9]?$|^100$',
    PATTERN_PHONE: '(0|\\+41|0041)\\s?([\\d]{2})\\s?([\\d]{3})\\s?([\\d]{2})\\s?([\\d]{2})',
    PATTERN_MOBILE: '(0|\\+41|0041)\\s?(74|75|76|77|78|79)\\s?([\\d]{3})\\s?([\\d]{2})\\s?([\\d]{2})',
    PATTERN_EMAIL: '[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}',
    PATTERN_ZEMIS_NUMMER: '(^0?\\d{8}\\.\\d$)|(^0\\d{2}\\.\\d{3}\\.\\d{3}[\\.-]\\d$)',
    INSTITUTIONSSTAMMDATENID_DUMMY_TAGESSCHULE: '199ac4a1-448f-4d4c-b3a6-5aee21f89613',
    ID_UNKNOWN_INSTITUTION_STAMMDATEN_KITA: '00000000-0000-0000-0000-000000000000',
    ID_UNKNOWN_INSTITUTION_STAMMDATEN_TAGESFAMILIE: '00000000-0000-0000-0000-000000000001',
    ID_UNKNOWN_INSTITUTION_STAMMDATEN_TAGESSCHULE: '00000000-0000-0000-0000-000000000002',
    PARTS_OF_BETREUUNGSNUMMER: 5,
    END_OF_TIME_STRING: '31.12.9999',
    DATE_FORMAT: 'DD.MM.YYYY',
    DATE_TIME_FORMAT: 'DD.MM.YYYY HH:mm',
    EARLIEST_DATE_OF_TS_ANMELDUNG: '2020-08-01'
};
// 100% = 20 days => 1% = 0.2 days
export const MULTIPLIER_KITA = 0.2;
// 100% = 220 hours => 1% = 2.2 hours
export const MULTIPLIER_TAGESFAMILIEN = 2.2;

export const DEFAULT_LOCALE = 'de-CH';
export const LOCALSTORAGE_LANGUAGE_KEY = 'kibonLanguage';
export const HEADER_ACCEPT_LANGUAGE = 'Accept-Language';

// Maximale (upload) Filegrösse ist 10MB
export const MAX_FILE_SIZE = 10485760;

export const HTTP_ERROR_CODES = {
    CONFLICT: 409
};
