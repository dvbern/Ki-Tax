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

import {
    AddGemeindePO,
    GemeindeListPO,
    MainNavigationPO
} from '@dv-e2e/page-objects';
import {getUser} from '@dv-e2e/types';
import {EditGemeindePO} from '../../page-objects/admin/edit-gemeinde.po';

describe('Einladung einer Gemeinde und Ausfüllen der Stammdaten durch Mandant', () => {
    const userMandant = getUser('[2-SB-Kanton-Bern] Benno Röthlisberger');
    let invitedGemeinde;

    before(() => {
        cy.login(userMandant);
        cy.visit('/#/');
    });

    it('should be possible to invite gemeinde as sb mandant', () => {
        MainNavigationPO.getMenuButton().click();
        MainNavigationPO.getGemeindenLink().click();

        GemeindeListPO.getGemeindeHinzufuegenButton().click();

        AddGemeindePO.getGemeindeSelection()
            .find('option')
            .eq(1)
            .then(firstOption => {
                invitedGemeinde = firstOption.text().trim();
                AddGemeindePO.getGemeindeSelection().select(invitedGemeinde);

                AddGemeindePO.getAdminMail().type(
                    `admin-${invitedGemeinde}@mailbucket.dvbern.ch`
                );

                cy.waitForRequest('POST', '**/gemeinde*', () => {
                    AddGemeindePO.getEinladungSendenButton().click();
                });

                GemeindeListPO.getSearchField().type(invitedGemeinde);
                GemeindeListPO.getSearchItem(0)
                    .find('mat-cell')
                    .eq(1)
                    .should('contain.text', 'Eingeladen');
                GemeindeListPO.getSearchItem(0).click();

                EditGemeindePO.getEditButton().click();
                EditGemeindePO.fillGemeindeStammdaten(
                    'withValid',
                    invitedGemeinde
                );
                cy.waitForRequest('PUT', '**/gemeinde/stammdaten', () => {
                    EditGemeindePO.getSaveButton().click();
                });
                EditGemeindePO.getCancelButton().click();
                GemeindeListPO.getSearchField().clear().type(invitedGemeinde);
                GemeindeListPO.getSearchItem(0)
                    .find('mat-cell')
                    .eq(1)
                    .should('contain.text', 'Eingeladen');
            });
    });
});
