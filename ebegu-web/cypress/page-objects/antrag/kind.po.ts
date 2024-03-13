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
import {getName} from 'domutils';

// !! -- PAGE OBJECTS -- !!
const getPageTitle = () => {
    return cy.getByData('page-title');
};

const getGeschlecht = (geschlecht: string) => {
 	return cy.getByData(`geschlecht.radio-value.${geschlecht}`);
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

const getObhutAlternierend = (answer: string) => {
    return cy.getByData('container.obhut-alternierend-ausueben', 'radio-value.' + answer);
};

const getFamErgaenzendeBetreuungAnmelden = (answer: string) => {
	return cy.getByData('container.ergaenzende-betreuung-beide', 'radio-value.' + answer);
};

const getSprichtAmtsprache = (answer: string) => {
	return  cy.getByData('sprichtAmtssprache.radio-value.' + answer);
};

const getEinschulungstyp = () => {
	return cy.getByData('einschulung-typ');
};

const getIsPflegekind = () => {
	return cy.getByData('ist-pflegekind', 'checkbox').find('.mdc-checkbox');
};

const getPflaegeEntschaedigungErhalten = (answer: string) => {
	return cy.getByData('container.pflege-entschaedigung-erhalten', 'radio-value.' + answer);
};
const getIntegration = (integrationIndex: number) => {
	return cy.getByData('container.integration#' + integrationIndex);
};

const getIntegrationBedarf = (integrationIndex: number, bedarf: string) => {
	return getIntegration(integrationIndex).findByData( 'radio-value.' + bedarf);
};

const getFachstelle = (integrationIndex: number) => {
	return cy.getByData('fachstelle#' + integrationIndex);
};

const getBetreuungspensumIndikation = (integrationIndex: number) => {
	return cy.getByData('betreuungspensum-fachstelle#' + integrationIndex);
};

const getIntegrationAb = (integrationIndex: number) => {
	return cy.getByData('pensum-gueltig-ab#' + integrationIndex);
};

const getIntegrationBis = (integrationIndex: number) => {
	return cy.getByData('pensum-gueltig-bis#' + integrationIndex);
};

const getAusserordentlicherAnspruchBegruendung = () => {
	return cy.getByData('ausserordentlich-begruendung');
};

const getAusserordentlicherAnspruchPensum = () => {
	return cy.getByData('betreuungspensum-ausserordentlicher-anspruch');
};

const getAusserordentlicherAnspruchAb = () => {
	return cy.getByData('auss-anspruch-datum-ab');
};

const getAusserordentlicherAnspruchBis = () => {
	return cy.getByData('auss-anspruch-datum-bis');
};

// !! -- PAGE ACTIONS -- !!

const createNewKind = () => {
    cy.getByData('container.create-kind', 'navigation-button').click();
};

const fillKindForm = (dataset: keyof typeof FixtureKind) => {
    FixtureKind[dataset](({ kind1 }) => {
        cy.url().should('include', 'kinder/kind');
        cy.wait(2000);
        getGeschlecht(kind1.geschlecht).click();
        getVorname().type(kind1.vorname);
        getNachname().type(kind1.nachname);
        getGeburtsdatum().find('input').type(kind1.geburtsdatum);
        getObhutAlternierend('ja').find('label').click();
        getFamErgaenzendeBetreuungAnmelden('ja').find('label').click();
        getSprichtAmtsprache('ja').click();
        getEinschulungstyp().select(kind1.einschulungstyp);
    });
};

const fillPflegekind = () => {
    getIsPflegekind().click();
    getPflaegeEntschaedigungErhalten('ja').find('label').click();
};

const fillFachstelle = () => {
    getIntegrationBedarf(0, 'SOZIALE_INTEGRATION').find('label').click();
    getFachstelle(0).select(1);
    getBetreuungspensumIndikation(0).type('40');
    getIntegrationAb(0).find('input').type('01.01.2024');
    getIntegrationBis(0).find('input').type('01.01.2025');
};

const fillAusserordentlicherAnspruch = () => {
    getAusserordentlicherAnspruchBegruendung().type('eine ausführliche Begründung');
    getAusserordentlicherAnspruchPensum().type('20');
    getAusserordentlicherAnspruchAb().find('input').type('01.01.2024');
    getAusserordentlicherAnspruchBis().find('input').type('01.01.2025');
};

export const AntragKindPO = {
    // page objects
    getPageTitle,
    getGeschlecht,
    getVorname,
    getNachname,
    getGeburtsdatum,
    getIsPflegekind,
    getPflaegeEntschaedigungErhalten,
    getObhutAlternierend,
    getFamErgaenzendeBetreuungAnmelden,
    getSprichtAmtsprache,
    getEinschulungstyp,
    // page actions
    createNewKind,
    fillKindForm,
    fillPflegekind,
    fillFachstelle,
    fillAusserordentlicherAnspruch,
};
