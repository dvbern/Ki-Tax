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


const getBetreuungspensumProzent = (zeitabschnittIndex: number) => {
	return cy.getByData('container.zeitabschnitt#' + zeitabschnittIndex, 'betreuungspensumProzent');
};

const getVerfuegterTarif = (zeitabschnittIndex: number) => {
    return cy.getByData('tagesschul-anmeldung-tarife',
        `tagesschul-anmeldung-zeitabschnitt#${zeitabschnittIndex}`,
        'tagesschul-anmeldung-zeitabschnitt-tarif');
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

const getVerfuegenButton = () => {
	return cy.getByData('container.verfuegen', 'navigation-button');
};

const getVerfuegenVerzichtenButton = () => {
	return cy.getByData('container.verfuegen-verzichten', 'navigation-button');
};

export const VerfuegungPO = {
    getBetreuungspensumProzent,
    getVerfuegterTarif,
    getAllTarife,
    getVerguenstigungOhneBeruecksichtigungVollkosten,
    getVerfuegungsBemerkungenKontrolliert,
    getVerfuegenButton,
    getVerfuegenVerzichtenButton,
};
