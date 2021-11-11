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

export const LOCALLOGIN_DATA = {
    BE: {
        mandant: {
            id: 'e3736eb8-6eef-40ef-9e52-96ab48d8f220',
            name: 'Kanton Bern',
        },
        traegerschaft: {
            id: 'f9ddee82-81a1-4cda-b273-fb24e9299308',
            name: 'Kitas Stadt Bern',
        },
        institution: {
            id: '1b6f476f-e0f5-4380-9ef6-836d688853a3',
            name: 'Kita Brünnen',
        },
        tagesschule: {
            id: 'f44a68f2-dda2-4bf2-936a-68e20264b610',
            name: 'Tagesschule Paris',
        },
        sozialdienst: {
            id: 'f44a68f2-dda2-4bf2-936a-68e20264b620',
            name: 'BernerSozialdienst',
        },
        gemeinde_paris: {
            id: 'ea02b313-e7c3-4b26-9ef7-e413f4046db2',
            name: 'Paris',
        },
        gemeinde_london: {
            id: '80a8e496-b73c-4a4a-a163-a0b2caf76487',
            name: 'London',
        },
    },
    LU: {
        mandant: {
            id: '485d7483-30a2-11ec-a86f-b89a2ae4a038',
            name: 'Kanton Luzern',
        },
        traegerschaft: {
            id: '31bf2433-30a3-11ec-a86f-b89a2ae4a038',
            name: 'Kitas & Tagis Stadt Luzern',
        },
        institution: {
            id: 'f5ceae4a-30a5-11ec-a86f-b89a2ae4a038',
            name: 'Kita Brünnen LU',
        },
        tagesschule: {
            id: '3db43c9b-30a6-11ec-a86f-b89a2ae4a038',
            name: 'Tagesschule Luzern',
        },
        sozialdienst: {
            id: '7049ec48-30ab-11ec-a86f-b89a2ae4a038',
            name: 'LuzernerSozialdienst',
        },
        gemeinde_paris: {
            id: '6fd6183c-30a2-11ec-a86f-b89a2ae4a038',
            name: 'Luzern',
        },
    },
};

export interface LocalloginDatum {
    mandant: {
        id: string,
        name: string
    };
    traegerschaft: {
        id: string,
        name: string
    };
    institution: {
        id: string,
        name: string
    };
    tagesschule: {
        id: string,
        name: string
    };
    sozialdienst: {
        id: string,
        name: string
    };
    gemeinde_paris: {
        id: string,
        name: string
    };
    gemeinde_london?: {
        id: string,
        name: string
    };
}
