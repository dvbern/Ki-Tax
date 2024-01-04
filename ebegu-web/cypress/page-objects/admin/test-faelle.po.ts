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

import {
    normalizeUser,
    TestBetreuungsstatus,
    TestFall,
    TestGesuchstellende,
    TestPeriode,
    GemeindeTestFall,
    User,
} from '@dv-e2e/types';
import {MainNavigationPO} from '../navigation';

// !! -- PAGE OBJECTS -- !!
const getPageTitle = () => {
	return cy.getByData('page-title');
};

const getGemeindeSelection = () => {
	return cy.getByData('gemeinde');
};

const getGemeindeOption = (gemeinde: GemeindeTestFall) => {
	return cy.getByData(`gemeinde.${gemeinde}`);
};

const getPeriodeSelection = () => {
	return cy.getByData('periode');
};

const getPeriodeOption = (periode: TestPeriode) => {
	return cy.getByData(`periode.${periode}`);
};

const getBetreuungsstatus = (betreuungsstatus: TestBetreuungsstatus) => {
	return cy.getByData(`creationType.${betreuungsstatus}`);
};

const getTestfall = (testfall: TestFall) => {
	return cy.getByData(testfall);
};

const getBesitzerinSelection = () => {
	return cy.getByData(`gesuchsteller`);
};

const getBesitzerinOption = (besitzerin: User) => {
	return cy.getByData(`gesuchsteller.${normalizeUser(besitzerin)}`);
};

const getFallLink = () => {
	return cy.get('[data-test="dialog-link"]', {timeout: Cypress.config('defaultCommandTimeout') * 50});
};

const getGesuchstellerFaelleLoeschen = () => {
    return cy.getByData('gesuchsteller-faelle-loeschen');
};

const getGesuchstellerIn = (user: User) => {
    return cy.getByData('gesuchsteller.' + normalizeUser(user));
};

const getGesuchstellerInToRemoveFaelle = (user: User) => {
    return cy.getByData('gesuchsteller.' + normalizeUser(user) + '-loeschen');
};

const getGesucheLoeschenButton = () => {
    return cy.getByData('delete-gesuche');
};

// !! -- PAGE ACTIONS -- !!

const createPapierTestfall = (data: {testFall: TestFall, gemeinde: GemeindeTestFall, periode: TestPeriode, betreuungsstatus: TestBetreuungsstatus, }) => {
    navigateToTestfaelle();
    getGemeindeSelection().click();
    getGemeindeOption(data.gemeinde).click();
    getPeriodeSelection().click();
    getPeriodeOption(data.periode).click();
    getBetreuungsstatus(data.betreuungsstatus).find('label').click();
    createAndOpenTestfall(data.testFall);
};

const createOnlineTestfall = (data: {
    testFall: TestFall;
    gemeinde: GemeindeTestFall;
    periode: TestPeriode;
    betreuungsstatus: TestBetreuungsstatus;
    besitzerin: TestGesuchstellende;
}) => {
    navigateToTestfaelle();
    getGemeindeSelection().click();
    getGemeindeOption(data.gemeinde).click();
    getPeriodeSelection().click();
    getPeriodeOption(data.periode).click();
    getBetreuungsstatus(data.betreuungsstatus).find('label').click();
    getBesitzerinSelection().click();
    getBesitzerinOption(data.besitzerin).click();
    createAndOpenTestfall(data.testFall);
};

function navigateToTestfaelle(): void {
    MainNavigationPO.getMenuButton().click();
    MainNavigationPO.getTestdatenLink().click();
}

function createAndOpenTestfall(testfall: TestFall) {
    cy.waitForRequest('GET','**/dossier/id//**', () => {
        getTestfall(testfall).click();
        getFallLink().click();
    });
}


export const TestFaellePO = {
    // page objects
    getGemeindeSelection,
    getGemeindeOption,
    getPeriodeSelection,
    getPeriodeOption,
    getBesitzerinSelection,
    getBesitzerinOption,
    getBetreuungsstatus,
    getTestfall,
    getFallLink,
    getGesuchstellerFaelleLoeschen,
    getGesuchstellerIn,
    getGesuchstellerInToRemoveFaelle,
    getGesucheLoeschenButton,
    // page actions
    createPapierTestfall,
    createOnlineTestfall,
};
