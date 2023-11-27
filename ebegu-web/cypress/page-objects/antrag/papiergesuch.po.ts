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

import { FixturePapierAntrag } from '@dv-e2e/fixtures';

const createPapierGesuch = (dataset: keyof typeof FixturePapierAntrag) => {
    cy.getByData('fall-eroeffnen').click();
    FixturePapierAntrag[dataset]((data) => {
        cy.getByData('fall-creation-eingangsdatum').find('input').type(data.fallCreationEingangsdatum);
    });
    cy.getByData('gesuchsperioden.2022/23').find('label').click();
    cy.intercept('POST', '**/gesuche');
    cy.intercept('GET', '**/PAPIERGESUCH').as('getPapierGesuch');
    cy.getByData('container.navigation-save', 'navigation-button').click();
    cy.wait('@getPapierGesuch');
};

export const AntragPapierPO = {
    createPapierGesuch,
};
