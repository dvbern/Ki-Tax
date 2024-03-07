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

import {ConfirmDialogPO} from '../dialogs';

const getAbweichung = (index: number) => {
	return cy.getByData('abweichung-' + index);
};

const getAbweichungPensum = (index: number) => {
	return getAbweichung(index).findByData('pensum');
};

const getAbweichungKosten = (index: number) => {
	return getAbweichung(index).findByData('kosten');
};

const getSpeichernButton = () => {
    return cy.getByData('container.save', 'navigation-button');
};

const getFreigebenButton = () => {
    return cy.getByData('container.freigeben', 'navigation-button');
};

// ---------- PAGE ACTIONS ---------------

const abweichungenSpeichern = () => {
	cy.waitForRequest('PUT', '**/betreuungen/betreuung/abweichungen/**', () => {
        getSpeichernButton().click();
    });
    ConfirmDialogPO.getDialogOkButton().click();
};

const fillInAbweichung = (index: number, pensum: number, kosten: number) => {
    cy.wait(500);
    getAbweichungPensum(index).type(pensum.toString());
    getAbweichungKosten(index).type(kosten.toString());
};

const abweichungenFreigeben = () => {
	cy.waitForRequest('PUT', '**/betreuung/abweichungenfreigeben/**', () => {
        getFreigebenButton().click();
    });
};

export const AbweichungenPO = {
    getAbweichung,
    getAbweichungPensum,
    getAbweichungKosten,
    // PAGE ACTIONS
    abweichungenSpeichern,
    abweichungenFreigeben,
    fillInAbweichung,
};
