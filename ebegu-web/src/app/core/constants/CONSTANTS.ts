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
    PATTERN_ANY_INT: '^[0-9]+$',
    PATTERN_BETRAG: '([0-9]{0,12})',
    PATTERN_ONE_DECIMALS: '^[0-9]+(\\.[0-9])?$',
    PATTERN_TWO_DECIMALS: '^[0-9]+(\\.[0-9]{1,2})?$',
    PATTERN_PERCENTAGE: '^[0-9][0-9]?$|^100$',
    PATTERN_PHONE: '(0|\\+41|0041)\\s?([\\d]{2})\\s?([\\d]{3})\\s?([\\d]{2})\\s?([\\d]{2})',
    PATTERN_MOBILE: '(0|\\+41|0041)\\s?(74|75|76|77|78|79)\\s?([\\d]{3})\\s?([\\d]{2})\\s?([\\d]{2})',
    PATTERN_EMAIL: '^[a-zA-Z0-9\\.!#$%&\'*+-/=?^_`{|}]{2,}@[a-zA-Z0-9\\.-]{2,}\\.[a-zA-Z]{2,5}$',
    PATTERN_ZEMIS_NUMMER: '(^0?\\d{8}\\.\\d$)|(^0\\d{2}\\.\\d{3}\\.\\d{3}[\\.-]\\d$)',
    PATTERN_HHHMM: '^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$',
    PARTS_OF_BETREUUNGSNUMMER: 5,
    END_OF_TIME_STRING: '31.12.9999',
    START_OF_TIME_STRING: '01.01.1000',
    DATE_FORMAT: 'DD.MM.YYYY',
    DATE_TIME_FORMAT: 'DD.MM.YYYY HH:mm',
    EARLIEST_DATE_OF_TS_ANMELDUNG: '2020-08-01',
    BERN_BFS_NUMMER: 351,
    MANDANT_LOCAL_STORAGE_KEY: 'mandant',
    NUR_LATS_STARTDATUM: '2021-08-01',
    QR_IBAN_PATTERN: /(LI|CH)\d{2}3[01]\d{3}\w{12}/,
    // used for debounce input elements
    KEYUP_TIMEOUT: 700,
    FIRST_YEAR_LASTENAUSGLEICH_WITHOUT_SELBSTBEHALT: 2022
};

export const DEFAULT_LOCALE = 'de-CH';
export const LOCALSTORAGE_LANGUAGE_KEY = 'kibonLanguage';
export const HEADER_ACCEPT_LANGUAGE = 'Accept-Language';

// Maximale (upload) Filegrösse ist 10MB
export const MAX_FILE_SIZE = 10485760;

export const HTTP_CODES = {
    OK: 200,
    BAD_REQUEST: 400,
    UNAUTHORIZED: 401,
    NOT_FOUND: 404,
    FORBIDDEN: 403,
    CONFLICT: 409
};
