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

const getSubjectInput = () => {
	return cy.getByData('subject');
};

const getNachrichtInput = () => {
	return cy.getByData('nachricht');
};

const getNachrichtSendenButton = () => {
    return cy.getByData('container.senden', 'navigation-button');
};

const getEmpfangendeInput = () => {
	return cy.getByData('empfaenger');
};

const getMitteilung = (mitteilungIndex: number) => {
	return cy.getByData('container.mitteilung#' + mitteilungIndex);
};

const getSubjectOfMitteilung = (mitteilungIndex: number) => {
	return cy.getByData('container.mitteilung#' + mitteilungIndex, 'nachricht-subject');
};

const getInhaltOfMitteilung = (mitteilungIndex: number) => {
	return cy.getByData('container.mitteilung#' + mitteilungIndex, 'nachricht-inhalt');
};

const getMutationsmeldungHinzufuegenButton = (mitteilungsIndex: number) => {
	return cy.getByData('container.mitteilung#' + mitteilungsIndex, 'container.mutationsmeldung-hinzufuegen', 'navigation-button');
};

export const MitteilungenPO = {
    getEmpfangendeInput,
    getSubjectInput,
    getNachrichtInput,
    getNachrichtSendenButton,
    getMitteilung,
    getSubjectOfMitteilung,
    getInhaltOfMitteilung,
    getMutationsmeldungHinzufuegenButton,
};
