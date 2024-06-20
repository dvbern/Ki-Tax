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

const getNavigationToMailUebersichtsPage = () => {
    return cy.getByData('action-admin.uebersichtVersendeteMails');
};

const getSentMailsSubject = () => {
    return cy.getByData('sentMailsSubject');
};

const getSortedMails = () => {
    return cy.getByData('sentMailsAdress');
};

const getSearchBarSentMails = () => {
    return cy.getByData('SearchBarSentMails');
};

const getPaginatorMailUebersicht = () => {
    return cy.get('#mat-select-value-1');
};

const getPaginatorAmountMailUebersicht = () => {
    return cy.get('#mat-option-1');
};

const getNextMailsInTablle = () => {
    return cy.get('.mat-paginator-navigation-next');
};

const getNextMailsInTablleCheck = () => {
    return cy.get('.mat-paginator-range-label');
};

const getVersendeteMailTableContent = () => {
    return cy.get(
        '.cdk-column-empfaengerAdresse > .mat-sort-header-container > .mat-sort-header-arrow'
    );
};

export const UebersichtVersendeteMailsPO = {
    getNavigationToMailUebersichtsPage,
    getSortedMails,
    getSearchBarSentMails,
    getSentMailsSubject,
    getPaginatorMailUebersicht,
    getPaginatorAmountMailUebersicht,
    getNextMailsInTablle,
    getNextMailsInTablleCheck,
    getVersendeteMailTableContent
};
