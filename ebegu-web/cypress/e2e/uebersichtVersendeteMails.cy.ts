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
    MainNavigationPO,
    TestFaellePO
} from '@dv-e2e/page-objects';
import {getUser} from '@dv-e2e/types';
import {UebersichtVersendeteMailsPO} from '../page-objects/admin/uebersichtVersendeteMails.po';

const adminUser = getUser('[1-Superadmin] E-BEGU Superuser');

describe('Kibon - generate Tests for uebersichts Versendete Mails calls', () => {
    const userAdminBern = getUser(
        '[2-Admin-Kanton-Bern] Bernhard Röthlisberger'
    );

    it('should access the Uebersicht View as Superadmin', () => {
        cy.login(adminUser);
        cy.visit('/#/faelle');
        MainNavigationPO.getMenuButton().click();
        UebersichtVersendeteMailsPO.getNavigationToMailUebersichtsPage().click();
        cy.url().should('contain', 'uebersichtVersendeteMails');
    });

    it('should not have acces to the Ueberischt View as SB', () => {
        cy.login(userAdminBern);
        cy.visit('/#/faelle');
        MainNavigationPO.getMenuButton().click();
        UebersichtVersendeteMailsPO.getNavigationToMailUebersichtsPage().should(
            'not.exist'
        );
        cy.visit('/#/uebersichtVersendeteMails');
        cy.url().should('contain', 'faelle');
    });
});

describe('Kibon - generate Tests for ubersicht Versendete Mails with Superadmin', () => {
    const validSearchText = 'kiBon Testsystem – Ihr Antrag wurde bearbeitet';
    const invalidSearchText = 'abc123def';
    let fallnummer: string;

    beforeEach(() => {
        cy.login(adminUser);
        cy.visit('/#/uebersichtVersendeteMails');
    });

    it('should display sent Mails', () => {
        TestFaellePO.createOnlineTestfall({
            testFall: 'testfall-1',
            gemeinde: 'London',
            periode: '2023/24',
            betreuungsstatus: 'verfuegt',
            besitzerin: '[5-GS] Jean Chambre'
        });
        FallToolbarPO.getFallnummer().then(el$ => {
            fallnummer = el$.text();
            cy.visit('/#/uebersichtVersendeteMails');
            UebersichtVersendeteMailsPO.getSentMailsSubject()
                .should('contain.text', fallnummer)
                .and('contain.text', validSearchText);
        });
    });

    it('should sort displayed Mails', () => {
        UebersichtVersendeteMailsPO.getVersendeteMailTableContent().click();

        UebersichtVersendeteMailsPO.getVersendeteMailTableContent().then(
            $address => {
                var addresses: any[] = [];
                UebersichtVersendeteMailsPO.getSortedMails().then(elements => {
                    addresses = Array.from(elements).map(el =>
                        el.innerHTML.toLowerCase()
                    );
                    let copy = addresses.filter(el => true);
                    addresses.sort();
                    cy.wrap(copy).should('deep.equal', addresses);
                });
            }
        );
    });

    it('should search existing Mails', () => {
        UebersichtVersendeteMailsPO.getSearchBarSentMails().type(
            validSearchText
        );
        UebersichtVersendeteMailsPO.getSentMailsSubject().should(
            'contain.text',
            validSearchText
        );
    });

    it('should search not existing Mails', () => {
        UebersichtVersendeteMailsPO.getSearchBarSentMails().type(
            invalidSearchText
        );
        UebersichtVersendeteMailsPO.getSentMailsSubject().should('not.exist');
    });

    it('should select Mail amount', () => {
        UebersichtVersendeteMailsPO.getPaginatorMailUebersicht().click();
        UebersichtVersendeteMailsPO.getPaginatorAmountMailUebersicht().click();
        UebersichtVersendeteMailsPO.getSentMailsSubject().should(
            'have.length.within',
            10,
            25
        );
    });

    it('should change Table page', () => {
        UebersichtVersendeteMailsPO.getNextMailsInTablle().click();
        UebersichtVersendeteMailsPO.getNextMailsInTablleCheck().should(
            'not.contain.text',
            '1-10'
        );
    });
});
