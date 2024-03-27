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

// !! -- PAGE OBJECTS -- !!
import {GemeindeFixture} from '@dv-e2e/fixtures';

const getAnschrift = () => {
	return cy.getByData('anschrift');
};

const getEmail = () => {
	return cy.getByData('email');
};

const getStrasse = () => {
	return cy.getByData('strasse');
};

const getHausnummer = () => {
	return cy.getByData('hausnummer');
};

const getPLZ = () => {
	return cy.getByData('plz');
};

const getOrt = () => {
	return cy.getByData('ort');
};

const getTelefon = () => {
	return cy.getByData('telefon');
};

const getStandardVerantwortliche = () => {
	return cy.getByData('standardverantwortliche');
};

const getEditButton = () => {
	return cy.getByData('container.edit', 'navigation-button');
};

const getSaveButton = () => {
	return cy.getByData('container.save', 'navigation-button');
};

const getCancelButton = () => {
	return cy.getByData('container.cancel');
};

// !! -- PAGE ACTIONS -- !!

const fillGemeindeStammdaten = (dataset: keyof typeof GemeindeFixture, gemeinde: string) => {
  GemeindeFixture[dataset]((data) => {
      const stammdaten = data.stammdaten;
      getAnschrift().type(stammdaten.anschrift);
      getEmail().type(`gemeinde-${gemeinde}@mailbucket.dvbern.ch`);
      getStrasse().type(stammdaten.strasse);
      getHausnummer().type(stammdaten.hausnummer);
      getPLZ().type(stammdaten.plz);
      getTelefon().type(stammdaten.telefon);
      getOrt().type(gemeinde);
      getStandardVerantwortliche().click();
      cy.get('mat-option').first().click()
  })
};

export const EditGemeindePO = {
    // PAGE OBJECTS
    getEditButton,
    getSaveButton,
    getCancelButton,
    // PAGE ACTIONS
    fillGemeindeStammdaten
};
