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

import { FixtureFamSit } from '@dv-e2e/fixtures';

const fillFamiliensituationForm = (dataset: keyof typeof FixtureFamSit) => {
    FixtureFamSit[dataset](({ GS1, GS2 }) => {
        cy.getByData('familienstatus.VERHEIRATET').find('label').click();
        cy.getByData('container.navigation-save', 'navigation-button').click();
        cy.getByData(`geschlecht.radio-value.${GS1.geschlecht}`).click();
        cy.getByData('vorname').type(GS1.vorname);
        cy.getByData('nachname').type(GS1.nachname);
        cy.getByData('geburtsdatum').find('input').type(GS1.geburtsdatum);
        cy.getByData('korrespondenzSprache').select(GS1.korrespondenzSprache);
        cy.getByData('container.wohn', 'adresseStrasse').type(GS1.adresseStrasse);
        cy.getByData('container.wohn', 'adresseHausnummer').type(GS1.adresseHausnummer);
        cy.getByData('container.wohn', 'adressePlz').type(GS1.adressePlz);
        cy.getByData('container.wohn', 'adresseOrt').type(GS1.adresseOrt);
        cy.getByData('container.navigation-save', 'navigation-button').click();
        cy.getByData('gesuchformular-title').should('include.text', '2');
        cy.getByData(`geschlecht.radio-value.${GS2.geschlecht}`).click();
        cy.getByData('vorname').type(GS2.vorname);
        cy.getByData('nachname').type(GS2.nachname);
        cy.getByData('geburtsdatum').find('input').type(GS2.geburtsdatum);
    });
};

export const AntragFamSitPO = {
    fillFamiliensituationForm,
};
