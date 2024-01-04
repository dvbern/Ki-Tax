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
import {NavigationPO} from './navigation.po';

// !! -- PAGE OBJECTS -- !!
const getAddErwerbspensumButton = (gesuchSteller: 'GS1' | 'GS2') => {
	return cy.getByData(`container.add-erwerbungspensum-erwerbspensen${gesuchSteller}`, 'navigation-button');
};

const getBezeichnung = () => {
	return cy.getByData('bezeichnung');
};

const getTaetigkeit = () => {
	return cy.getByData('taetigkeit');
};

const getTaetigkeitsPensum = () => {
	return cy.getByData('taetigkeit-pensum');
};

const getTaetigkeitAb = () => {
	return cy.getByData('taetigkeit-ab');
};

// !! -- PAGE ACTIONS -- !!
const createBeschaeftigungspensum = (gesuchSteller: 'GS1' | 'GS2', dataset: keyof typeof FixtureBeschaeftigungspensum) => {
    FixtureBeschaeftigungspensum[dataset](data => {
        getAddErwerbspensumButton(gesuchSteller).click();
        getBezeichnung().type(data[gesuchSteller].bezeichnung);
        getTaetigkeit().select(`string:${data[gesuchSteller].taetigkeit}`);
        getTaetigkeitsPensum().type(data[gesuchSteller].taetigkeitPensum);
        getTaetigkeitAb().find('input').type(data[gesuchSteller].taetigkeitAb);

        cy.waitForRequest('GET', '**/erwerbspensen/required/**', () => {
            NavigationPO.saveAndGoNext();
        });
    });
};

export const AntragBeschaeftigungspensumPO = {
    //page objects
    getAddErwerbspensumButton,
    getBezeichnung,
    getTaetigkeit,
    getTaetigkeitAb,
    getTaetigkeitsPensum,
    // page objects
    createBeschaeftigungspensum,
};
