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

import {
    FallToolbarPO,

} from '@dv-e2e/page-objects';
import {getUser} from '@dv-e2e/types';

const adminUser = getUser('[1-Superadmin] E-BEGU Superuser');

describe('Kibon - generate Tests for uebersichts Versendete Mails calls', () => {
    const userAdminBern = getUser('[2-Admin-Kanton-Bern] Bernhard Röthlisberger');

    it('should access the Uebersicht View as Superadmin', () => {
        cy.login(adminUser);
        cy.visit('/#/faelle');
        cy.getByData('page-menu').click();
        cy.getByData('action-admin.uebersichtVersendeteMails').click();
        cy.url().should('contain', 'uebersichtVersendeteMails');
    });

    it('should not have acces to the Ueberischt View as SB', () => {
        cy.login(userAdminBern);
        cy.visit('/#/faelle');
        cy.getByData('page-menu').click();
        cy.getByData('action-admin.uebersichtVersendeteMails').should('not.exist');
        cy.visit('/#/uebersichtVersendeteMails');
        cy.url().should('contain', 'faelle');
    });

})

describe('Kibon - generate Tests for ubersicht Versendete Mails with Superadmin', () => {
    const validSearchText = 'Unvollständige Unterlagen';
    const invalidSearchText = 'abc123def';
    let fallnummer: string;

    beforeEach(() => {
        cy.login(adminUser);
        cy.visit('/#/uebersichtVersendeteMails');
    })

    it('should display sent Mails', () => {
        cy.visit('/#/testdaten');
        cy.getByData('creationType.bestaetigt').click();
        cy.getByData('gesuchsteller').click();
        cy.getByData('gesuchsteller.Alain-Lehmann').click();
        cy.getByData('gemeinde').click();
        cy.getByData('gemeinde.Ins').click();
        cy.getByData('periode').click();
        cy.getByData('periode.2023/24').click();
        cy.getByData('testfall-1').click();
        cy.wait(3000);
        cy.getByData('dialog-link').click();
        cy.wait(3000);
        cy.getByData('sidenav.DOKUMENTE').click();
        cy.getByData('container.navigation-save').click();
        cy.getByData('container.freigeben').click();
        cy.getByData('container.confirm').click();
        cy.getByData('container.navigation-save').click();
        cy.getByData('sidenav.FREIGABE').click();
        cy.getByData('container.antrag-freigeben-simulieren').click();
        cy.reload();
        cy.getByData('container.navigation-save').click();
        cy.getByData('erste-mahnung').click();
        cy.wait(1000);
        cy.getByData('fristablauf-mahnung').find('input').type('01.01.2025');
        FallToolbarPO.getFallnummer().then(el$ => {
            fallnummer = el$.text();
            cy.getByData('erste-mahnung-auslösen').click();
            cy.wait(2000);
            cy.visit('/#/uebersichtVersendeteMails');
            cy.getByData('sentMailsSubject').should('contain.text', fallnummer).and('contain.text', validSearchText);
        });
    });

    it('should sort displayed Mails', () => {
        cy.get('.cdk-column-empfaengerAdresse > .mat-sort-header-container > .mat-sort-header-arrow').click();

        cy.get('.cdk-column-empfaengerAdresse > .mat-sort-header-container > .mat-sort-header-arrow')
            .then($address => {
                var addresses: any[] = [];
                cy.getByData('sentMailsAdress').then(elements => {
                    addresses = Array.from(elements).map(el => el.innerHTML.toLowerCase());
                    let copy = addresses.filter(el => true);
                    addresses.sort()
                    cy.wrap(copy).should('deep.equal', addresses);
                })
            })
    });

    it('should search existing Mails', () => {
        cy.getByData('SearchBarSentMails').type(validSearchText);
        cy.getByData('sentMailsSubject').should('contain.text', validSearchText);
    });

    it('should search not existing Mails', () => {
        cy.getByData('SearchBarSentMails').type(invalidSearchText);
        cy.getByData('sentMailsSubject').should('not.exist');
    });

    it('should select Mail amount', () => {
        cy.get('#mat-select-value-1').click();
        cy.get('#mat-option-1').click();
        cy.getByData('sentMailsSubject').should('have.length.within', 10, 25);
    });

    it('should change Table page', () => {
        cy.get('.mat-paginator-navigation-next').click();
        cy.get('.mat-paginator-range-label').should('not.contain.text', '1-10');
    });
})
