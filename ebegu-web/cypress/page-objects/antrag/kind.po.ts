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

import { FixtureKind } from '@dv-e2e/fixtures';

const createNewKind = () => {
    cy.getByData('container.create-kind', 'navigation-button').click();
};

const fillKindForm = (dataset: keyof typeof FixtureKind) => {
    FixtureKind[dataset](({ kind1 }) => {
        cy.getByData(`geschlecht.radio-value.${kind1.geschlecht}`).click();
        cy.getByData('vorname').type(kind1.vorname);
        cy.getByData('nachname').type(kind1.nachname);
        cy.getByData('geburtsdatum').find('input').type(kind1.geburtsdatum);
        cy.getByData('container.obhut-alternierend-ausueben', 'radio-value.ja').find('label').click();
        cy.getByData('container.ergaenzende-betreuung-beide', 'radio-value.ja').find('label').click();
        cy.getByData('sprichtAmtssprache.radio-value.ja').click();
        cy.getByData('einschulung-typ').select(kind1.einschulungstyp);
    });
};

const fillPflegekind = () => {
    cy.getByData('ist-pflegekind', 'checkbox').click();
    cy.getByData('container.pflege-entschaedigung-erhalten', 'radio-value.ja').find('label').click();
};

const fillFachstelle = () => {
    cy.getByData('container.integration#0', 'radio-value.SOZIALE_INTEGRATION').find('label').click();
    cy.getByData('fachstelle#0').select(1);
    cy.getByData('betreuungspensum-fachstelle#0').type('40');
    cy.getByData('pensum-gueltig-ab#0').find('input').type('01.01.2024');
    cy.getByData('pensum-gueltig-bis#0').find('input').type('01.01.2025');
};

const fillAusserordentlicherAnspruch = () => {
    cy.getByData('ausserordentlich-begruendung').type('eine ausführliche Begründung');
    cy.getByData('betreuungspensum-ausserordentlicher-anspruch').type('20');
    cy.getByData('auss-anspruch-datum-ab').find('input').type('01.01.2024');
    cy.getByData('auss-anspruch-datum-bis').find('input').type('01.01.2025');
};

export const AntragKindPO = {
    createNewKind,
    fillKindForm,
    fillPflegekind,
    fillFachstelle,
    fillAusserordentlicherAnspruch,
};
