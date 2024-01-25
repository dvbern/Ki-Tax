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

import { FixtureFamSit } from '@dv-e2e/fixtures';

const getPageTitle = () => {
	return cy.getByData('page-title');
};
const getFamiliensituationsStatus = (status: string) => {
	return cy.getByData('familienstatus.' + status);
};

const getKonkubinatStart = () => {
	return cy.getByData('container.konkubinat-start').find('input');
};

const getGeteilteObhutOption = (answer: string) => {
	return cy.getByData('container.geteilte-obhut', 'geteilte-obhut-' + answer);
};

const getUnterhaltsvereinbarungOption = (answer:string) => {
	return cy.getByData('container.unterhaltsvereinbarung', answer);
};
const fillFamiliensituationForm = (dataset: keyof typeof FixtureFamSit) => {
    FixtureFamSit[dataset](({ familiensituation }) => {
        getFamiliensituationsStatus(familiensituation.familienstand).find('label').click();
    });
};

export const AntragFamSitPO = {
    //page objects
    getPageTitle,
    getFamiliensituationsStatus,
    getKonkubinatStart,
    getGeteilteObhutOption,
    getUnterhaltsvereinbarungOption,
    //page actions
    fillFamiliensituationForm,
};
