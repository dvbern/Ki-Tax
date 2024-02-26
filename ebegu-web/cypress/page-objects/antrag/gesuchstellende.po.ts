/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

import {FixtureFamSit} from '@dv-e2e/fixtures';
import {NavigationPO} from '@dv-e2e/page-objects';

const getGeschlechtOption = (selection: string) => {
	return cy.getByData(`geschlecht.radio-value.${selection}`);
};

const getVorname = () => {
	return cy.getByData('vorname');
};

const getNachname = () => {
	return cy.getByData('nachname');
};

const getGeburtsdatum = () => {
	return cy.getByData('geburtsdatum');
};

const getKorrespondenzsprache = () => {
	return cy.getByData('korrespondenzSprache');
};

const getAdresseStrasse = () => {
    return cy.getByData('container.wohn', 'adresseStrasse');
};

const getAdresseHausnummer = () => {
    return cy.getByData('container.wohn', 'adresseHausnummer');
};

const getAdressePlz = () => {
    return cy.getByData('container.wohn', 'adressePlz');
};

const getAdresseOrt = () => {
    return cy.getByData('container.wohn', 'adresseOrt');
};

const getFormularTitle = () => {
	return cy.getByData('gesuchformular-title');
};

// !! -- PAGE ACTIONS -- !!
const fillVerheiratet = (dataset: keyof typeof FixtureFamSit) => {
    FixtureFamSit[dataset](({GS1}) => {
        fillGS1(GS1);
    });
    NavigationPO.saveAndGoNext();
    getFormularTitle().should('include.text', '2');
    FixtureFamSit[dataset](({GS2}) => {
       fillBaseGesuchsteller(GS2);
    });

};

// TODO: type this
function fillGS1(GS1: any): void {
    fillBaseGesuchsteller(GS1);
    getKorrespondenzsprache().select(GS1.korrespondenzSprache);
    getAdresseStrasse().type(GS1.adresseStrasse);
    getAdresseHausnummer().type(GS1.adresseHausnummer);
    getAdressePlz().type(GS1.adressePlz);
    getAdresseOrt().type(GS1.adresseOrt);
}

const fillBaseGesuchsteller = (GS1: {geschlecht: string, vorname: string, nachname: string, geburtsdatum: string}) => {
    cy.wait(2000);
    getGeschlechtOption(GS1.geschlecht).click();
    getVorname().clear().type(GS1.vorname);
    getNachname().clear().type(GS1.nachname);
    getGeburtsdatum().find('input').type(GS1.geburtsdatum);
};

export const GesuchstellendePO = {
    // page objects
    getGeschlechtOption,
    getVorname,
    getNachname,
    getGeburtsdatum,
    getKorrespondenzsprache,
    getAdresseStrasse,
    getAdresseHausnummer,
    getAdressePlz,
    getAdresseOrt,
    getFormularTitle,
    // page actions
    fillVerheiratet,
};
