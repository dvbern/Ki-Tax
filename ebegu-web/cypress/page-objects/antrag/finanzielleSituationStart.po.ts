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

// !! -- PAGE OBJECTS -- !!
import {FixtureFinSit} from '@dv-e2e/fixtures';
import {NavigationPO} from './navigation.po';

const getSozialhilfebezueger = (answer: string) => {
    return cy.getByData('sozialhilfeBezueger.radio-value.' + answer);
};

const getIban = () => {
    return cy.getByData('iban');
};

const getKontoinhaberIn = () => {
    return cy.getByData('kontoinhaber');
};

const fillFinanzielleSituationStartForm = (dataset: keyof typeof FixtureFinSit) => {
  FixtureFinSit[dataset](({Start}) => {
      cy.wait(2000);
      getSozialhilfebezueger(Start.sozialhilfebeziehende).click();
      getIban().type(Start.iban);
      getKontoinhaberIn().type(Start.kontoinhaber);
  })
};

const saveForm = () => {
    cy.waitForRequest('POST', '**/finanzielleSituation/calculateTemp', () => {
        NavigationPO.saveAndGoNext();
    });
};

export const FinanzielleSituationStartPO = {
    // page objects
    getSozialhilfebezueger,
    getIban,
    getKontoinhaberIn,
    // page actions
    fillFinanzielleSituationStartForm,
    saveForm,
};
