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

import type * as Beschaeftigungspensum from '../fixtures/antrag/beschaeftigungspensum.json';
import type * as Betreuung from '../fixtures/antrag/betreuung.json';
import type * as Einkommensverschlechterung from '../fixtures/antrag/einkommensverschlechterung.json';
import type * as EinkommensverschlechterungInfo from '../fixtures/antrag/einkommensverschlechterung-info.json';
import type * as Kind from '../fixtures/antrag/kind-boy.json';
import type * as FamSit from '../fixtures/antrag/famsit.json';
import type * as FinSit from '../fixtures/antrag/finsit.json';
import type * as Papier from '../fixtures/antrag/papier.json';
import type * as CreateTagesschule from '../fixtures/institution/create-tagesschule.json';
import type * as Tagesschule from '../fixtures/institution/tagesschule.json';

const fromFixture =
    <T, FixturePart extends keyof T = keyof T>(fixture: string, fixturePart: FixturePart) =>
    <R>(fn: (data: T[FixturePart]) => R) =>
        cy.fixture(fixture).then((data: T) => fn(data[fixturePart]));

export const FixtureKind = {
    withValidBoy: fromFixture<typeof Kind>('antrag/kind-boy.json', 'valid'),
    withValidGirl: fromFixture<typeof Kind>('antrag/kind-girl.json', 'valid'),
};

export const FixtureBeschaeftigungspensum = {
    withValid: fromFixture<typeof Beschaeftigungspensum>('antrag/beschaeftigungspensum.json', 'valid'),
};

export const FixtureBetreuung = {
    withValid: fromFixture<typeof Betreuung>('antrag/betreuung.json', 'valid'),
    withSchwyz: fromFixture<typeof Betreuung>('antrag/betreuung.json', 'schwyz'),
};

export const FixtureFamSit = {
    withValid: fromFixture<typeof FamSit>('antrag/famsit.json', 'valid'),
};

export const FixtureFinSit = {
    withValid: fromFixture<typeof FinSit>('antrag/finsit.json', 'valid'),
};

export const FixturePapierAntrag = {
    withValid: fromFixture<typeof Papier>('antrag/papier.json', 'valid'),
};

export const FixtureEinkommensverschlechterung = {
    withValid: fromFixture<typeof Einkommensverschlechterung>('antrag/einkommensverschlechterung.json', 'valid'),
};

export const FixtureEinkommensverschlechterungInfo = {
    withValid: fromFixture<typeof EinkommensverschlechterungInfo>('antrag/einkommensverschlechterung-info.json', 'valid'),
};

export const FixtureCreateTagesschule = {
    withValid: fromFixture<typeof CreateTagesschule>('institution/create-tagesschule.json', 'valid'),
};

export const FixtureTagesschule = {
    withValid: fromFixture<typeof Tagesschule>('institution/tagesschule.json', 'valid'),
};
