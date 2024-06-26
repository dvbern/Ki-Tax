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

const getFreigebenButton = () => {
    return cy.getByData('container.freigeben', 'navigation-button');
};

const approveCorrectDataValues = () => {
    return cy.getByData('gesuchfreigeben', 'checkbox');
};
const getFreigebenButtonSchwyz = () => {
    return cy.getByData('gesuchfreigeben', 'navigation-button');
};

const getFreigabequittungEinscannenSimulierenButton = () => {
    return cy.getByData('container.antrag-freigeben-simulieren');
};

const freigeben = () => {
    FreigabePO.getFreigebenButton().click();
    cy.getDownloadUrl(() => {
        cy.waitForRequest('GET', '**/dossier/fall/**', () => {
            ConfirmDialogPO.getDvLoadingConfirmButton().click();
        });
    }).then(downloadUrl => {
        return cy
            .request(downloadUrl)
            .then(response =>
                expect(response.headers['content-disposition']).to.match(
                    /Freigabequittung_.*\.pdf/
                )
            );
    });
};

const freigebenSchwyz = () => {
    FreigabePO.approveCorrectDataValues().click();
    FreigabePO.getFreigebenButtonSchwyz().click();
};

export const FreigabePO = {
    // PAGE OBJECTS
    getFreigebenButton,
    getFreigebenButtonSchwyz,
    getFreigabequittungEinscannenSimulierenButton,
    // PAGE ACTIONS
    freigeben,
    freigebenSchwyz,
    approveCorrectDataValues
};
