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

const getUmzugHinzufuegenButton = () => {
    return cy.getByData('container.hinzufuegen', 'navigation-button');
};

const getUmzugStrasse = (umzugIndex: number) => {
    return cy.getByData('container.umzug-' + umzugIndex, 'adresseStrasse');
};

const getUmzugHausnummer = (umzugIndex: number) => {
    return cy.getByData('container.umzug-' + umzugIndex, 'adresseHausnummer');
};

const getUmzugPlz = (umzugIndex: number) => {
    return cy.getByData('container.umzug-' + umzugIndex, 'adressePlz');
};

const getUmzugOrt = (umzugIndex: number) => {
    return cy.getByData('container.umzug-' + umzugIndex, 'adresseOrt');
};

const getUmzugGueltigAb = (umzugIndex: number) => {
    return cy.getByData('container.umzug-' + umzugIndex, 'gueltigAb');
};

export const UmzugPO = {
    getUmzugHinzufuegenButton,
    getUmzugStrasse,
    getUmzugHausnummer,
    getUmzugPlz,
    getUmzugOrt,
    getUmzugGueltigAb
};
