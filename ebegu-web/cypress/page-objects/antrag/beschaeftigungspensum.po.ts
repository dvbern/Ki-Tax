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

import { FixtureBeschaeftigungspensum } from '@dv-e2e/fixtures';

const createBeschaeftigungspensum = (gesuchSteller: 'GS1' | 'GS2', dataset: keyof typeof FixtureBeschaeftigungspensum) => {
    FixtureBeschaeftigungspensum[dataset](data => {
        cy.getByData(`container.add-erwerbungspensum-erwerbspensen${gesuchSteller}`, 'navigation-button').click();
        cy.getByData('bezeichnung').type(data[gesuchSteller].bezeichnung);
        cy.getByData('taetigkeit').select(`string:${data[gesuchSteller].taetigkeit}`);
        cy.getByData('taetigkeit-pensum').type(data[gesuchSteller].taetigkeitPensum);
        cy.getByData('taetigkeit-ab').find('input').type(data[gesuchSteller].taetigkeitAb);

        cy.intercept('GET', '**/erwerbspensen/required/**').as(`reloadingTaetigkeiten${gesuchSteller}`);
        cy.get('[data-test="container.navigation-save"] [data-test="navigation-button"]:not([disaFbled])').click();
        cy.wait(`@reloadingTaetigkeiten${gesuchSteller}`);
    });
};

export const AntragBeschaeftigungspensumPO = {
    createBeschaeftigungspensum,
};
