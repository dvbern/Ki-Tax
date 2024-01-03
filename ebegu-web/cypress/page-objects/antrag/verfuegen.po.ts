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

const getVerfuegung = (kindIndex: number, betreuungsIndex: number) => {
    return cy.getByData(`verfuegung#${kindIndex}-${betreuungsIndex}`);
};

const getVerfuegenStartenButton = () => {
	return cy.getByData('container.verfuegen', 'navigation-button');
};

const getGeprueftButton = () => {
	return cy.getByData('container.geprueft', 'navigation-button');
};

const getBetreuungsstatus = (kindIndex: number, betreuungsIndex: number) => {
	return cy.getByData(`verfuegung#${kindIndex}-${betreuungsIndex}`, 'betreuungs-status');
};

const getFinSitAkzeptiert = (status: string) => {
    return cy.getByData('finSitStatus.radio-value.' + status);
};
export const VerfuegenPO = {
    getBetreuungsstatus,
    getFinSitAkzeptiert,
    getGeprueftButton,
    getVerfuegung,
    getVerfuegenStartenButton,
};
