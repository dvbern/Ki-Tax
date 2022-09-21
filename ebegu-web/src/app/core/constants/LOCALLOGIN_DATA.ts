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
        default_gemeinde: {
            id: 'ea02b313-e7c3-4b26-9ef7-e413f4046db2',
            name: 'Paris',
        },
        second_gemeinde: {
            id: '80a8e496-b73c-4a4a-a163-a0b2caf76487',
            name: 'London',
        },
    },
    LU: {
        mandant: {
            id: '485d7483-30a2-11ec-a86f-b89a2ae4a038',
            name: 'Stadt Luzern',
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
        default_gemeinde: {
            id: '6fd6183c-30a2-11ec-a86f-b89a2ae4a038',
            name: 'Testgemeinde Luzern',
        },
    },
    SO: {
        mandant: {
            id: '7781a6bb-5374-11ec-98e8-f4390979fa3e',
            name: 'Kanton Solothurn',
        },
        traegerschaft: {
            id: '5c537fd1-537b-11ec-98e8-f4390979fa3e',
            name: 'Kitas & Tagis Stadt Solothurn',
        },
        institution: {
            id: '78051383-537e-11ec-98e8-f4390979fa3e',
            name: 'Kita Brünnen SO',
        },
        tagesschule: {
            id: 'bbf7f306-5392-11ec-98e8-f4390979fa3e',
            name: 'Tagesschule Solothurn',
        },
        sozialdienst: {
            id: '1b1b4208-5394-11ec-98e8-f4390979fa3e',
            name: 'SolothurnerSozialdienst',
        },
        default_gemeinde: {
            id: '47c4b3a8-5379-11ec-98e8-f4390979fa3e',
            name: 'Testgemeinde Solothurn',
        },
    },
    AR: {
        mandant: {
            id: '5b9e6fa4-3991-11ed-a63d-b05cda43de9c',
            name: 'Kanton Appenzell Ausserrhoden',
        },
        traegerschaft: {
            id: 'c256ebf1-3999-11ed-a63d-b05cda43de9c',
            name: 'Kitas & Tagis Appenzell Ausserrhoden',
        },
        institution: {
            id: 'caa83a6b-3999-11ed-a63d-b05cda43de9c',
            name: 'Kita Brünnen SO',
        },
        tagesschule: {
            id: '5c136a35-39a9-11ed-a63d-b05cda43de9c',
            name: 'Tagesschule Appenzell Ausserrhoden',
        },
        sozialdienst: {
            id: '1653a0c7-39ab-11ed-a63d-b05cda43de9c',
            name: 'Appenzell Ausserrhodener Sozialdienst',
        },
        default_gemeinde: {
            id: 'b3e44f85-3999-11ed-a63d-b05cda43de9c',
            name: 'Testgemeinde Appenzell Ausserrhoden',
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
    default_gemeinde: {
        id: string,
        name: string
    };
    second_gemeinde?: {
        id: string,
        name: string
    };
}
