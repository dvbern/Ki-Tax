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

import {normalizeUser, User} from '@dv-e2e/types';

const getAntragsDaten = () => {
    return cy.getByData('antrags-daten');
};

const getEingangsdatum = () => {
    return cy.getByData('fall-creation-eingangsdatum');
};

const getAlternativdatum = () => {
    return cy.getByData('fall-creation-alternativDatum');
};

const getAntragBearbeitenButton = () => {
    return cy.getByData('container.antrag-bearbeiten');
};

// TODO: this should probably be on a toolbar po
const getVerantwortlicher = () => {
    return cy.getByData('verantwortlicher');
};

const getUserOption = (user: User, schulamt: boolean) => {
    return cy.getByData(
        `container.${schulamt ? 'schul' : ''}verantwortlicher`,
        `option.${normalizeUser(user)}`
    );
};

const getPageTitle = () => {
    return cy.getByData('page-title');
};

export const AntragCreationPO = {
    getAntragsDaten,
    getAlternativdatum,
    getEingangsdatum,
    getVerantwortlicher,
    getUserOption,
    getPageTitle,
    getAntragBearbeitenButton
};
