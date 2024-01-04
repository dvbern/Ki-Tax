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

import {FixtureEinkommensverschlechterung} from '@dv-e2e/fixtures';

// !! -- PAGE OBJECTS -- !!
const getPageTitle = () => {
	return cy.getByData('page-title');
};
const getBruttovermoegenGS1 = () => {
    return cy.getByData('bruttovermoegen1');
};

const getBruttovermoegenGS2 = () => {
    return cy.getByData('bruttovermoegen2');
};

const getSchuldenGS1 = () => {
    return cy.getByData('schulden1');
};

const getSchuldenGS2 = () => {
    return cy.getByData('schulden2');
};

const getEinkommenBeiderGesuchsteller = () => {
    return cy.getByData('einkommen-beider-gesuchsteller');
};

const getEinkommenVorjahrBasis = () => {
    return cy.getByData('einkommen-vorjahr-basis');
};

const getEinkommenVorjahr = () => {
    return cy.getByData('einkommen-vorjahr');
};

const// !! -- PAGE ACTIONS -- !!
    fillResultateForm = (dataset: keyof typeof FixtureEinkommensverschlechterung, jahr: 'jahr1' | 'jahr2') => {
    FixtureEinkommensverschlechterung[dataset](({[jahr]: {Resultate}}) => {
        getBruttovermoegenGS1().find('input').type(Resultate.bruttovermoegen1);
        getBruttovermoegenGS2().find('input').type(Resultate.bruttovermoegen2);
        getSchuldenGS1().find('input').type(Resultate.schulden1);
        getSchuldenGS2().find('input').type(Resultate.schulden2);
    })
};


export const EinkommensverschlechterungResultatePO = {
    getPageTitle,
    getBruttovermoegenGS1,
    getBruttovermoegenGS2,
    getSchuldenGS1,
    getSchuldenGS2,
    getEinkommenBeiderGesuchsteller,
    getEinkommenVorjahrBasis,
    getEinkommenVorjahr,
    // page actions
    fillResultateForm,
};

