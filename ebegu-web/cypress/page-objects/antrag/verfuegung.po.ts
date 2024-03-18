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
import {ConfirmDialogPO} from '../dialogs';

const getBetreuungspensumProzent = (zeitabschnittIndex: number) => {
	return cy.getByData('container.zeitabschnitt#' + zeitabschnittIndex, 'betreuungspensumProzent');
};

const getVerfuegterTarif = (zeitabschnittIndex: number) => {
    return cy.getByData('tagesschul-anmeldung-tarife',
        `tagesschul-anmeldung-zeitabschnitt#${zeitabschnittIndex}`,
        'tagesschul-anmeldung-zeitabschnitt-tarif');
};

const getProvisorischerTarifTitel = () => {
    return cy.getByData('verfuegung-tagesschule-provisorisch');
};

const getAllTarife = () => {
    return cy.getByData('tagesschul-anmeldung-tarife', 'tagesschul-anmeldung-zeitabschnitt-tarif');
};

const getVerguenstigungOhneBeruecksichtigungVollkosten = (zeitabschnittIndex: number) => {
    return cy.getByData('container.zeitabschnitt#' + zeitabschnittIndex, 'verguenstigungOhneBeruecksichtigungVollkosten');
};

const getVerfuegungsBemerkungenKontrolliert = () => {
	return cy.getByData('verfuegungs-bemerkungen-kontrolliert');
};

const getAnspruchberechtigtesBetreuungspensum = (zeitabschnittIndex: number) => {
	return cy.getByData('container.zeitabschnitt#' + zeitabschnittIndex, 'anspruchberechtigtesPensum');
};

const getVerfuegenButton = () => {
	return cy.getByData('container.verfuegen', 'navigation-button');
};

const getVerfuegenVerzichtenButton = () => {
	return cy.getByData('container.verfuegen-verzichten', 'navigation-button');
};

const getNichtEintretenButton = () => {
	return cy.getByData('container.nicht-eintreten', 'navigation-button');
};

// !! -- PAGE ACTIONS -- !!
const nichtEintretenVerfuegen = () => {
    cy.waitForRequest('GET', '**/verfuegung/nichtEintreten/**', () => {
        VerfuegungPO.getNichtEintretenButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });
};

const betreuungKontrollierenAndVerfuegen = () => {
	cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
        getVerfuegungsBemerkungenKontrolliert().click();
        getVerfuegenButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });
};

// In der Mutation müssen wir die VerfügungsBemerkung Checkbox nicht mehr setzen
const betreuungVerfuegen = () => {
	cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
        getVerfuegungsBemerkungenKontrolliert().click();
        getVerfuegenButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });
};

export const VerfuegungPO = {
    // PAGE OBJECTS
    getBetreuungspensumProzent,
    getVerfuegterTarif,
    getProvisorischerTarifTitel,
    getAllTarife,
    getVerguenstigungOhneBeruecksichtigungVollkosten,
    getVerfuegungsBemerkungenKontrolliert,
    getVerfuegenButton,
    getVerfuegenVerzichtenButton,
    getNichtEintretenButton,
    getAnspruchberechtigtesBetreuungspensum,
    // PAGE ACTIONS
    nichtEintretenVerfuegen,
    betreuungKontrollierenAndVerfuegen,
};
