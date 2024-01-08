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

import {TestFaellePO} from '@dv-e2e/page-objects';
import {getUser} from '@dv-e2e/types';
import {SidenavPO} from '../page-objects/antrag/sidenav.po';

describe('kiBon - Features auf der FinSit - Page', () => {
    const superAdmin = getUser('[1-Superadmin] E-BEGU Superuser');
    const sbBGLondon = getUser('[6-L-SB-BG] Jörg Keller');

    let gesuchUrl: string;

    before('create Antrag', () => {
        cy.login(superAdmin);
        cy.visit('/#/faelle');

        TestFaellePO.createNewPapierTestFallIn({
            testFall: 'testfall-1',
            gemeinde: 'London',
            betreuungsstatus: 'warten',
            periode: '2024/25',
        });
        cy.url().then((url) => {
            const parts = new URL(url);
            gesuchUrl = `${parts.pathname}${parts.hash}`;
        });
    });

    beforeEach(() => {
        cy.login(sbBGLondon);
        cy.waitForRequest('GET', '**/gesuchsperioden/gemeinde/**', () => {
            cy.visit(gesuchUrl);
        });
        cy.waitForRequest('GET', '**/FINANZIELLE_SITUATION_TYP/**', () => {
            SidenavPO.goTo('FINANZIELLE_SITUATION');
        });
        cy.waitForRequest('POST', '**/calculateTemp', () => {
            cy.getByData('container.navigation-save', 'navigation-button').click();
        });
    });

    it('should not display geschaeftsgewinn and ersatzeinkommen selbststaendigkeit fields, if Selbstständigkeit is not selected',
        () => {
            cy.getByData('show-selbstaendig').should('exist');
            cy.getByData('show-selbstaendig').should('not.have.class', 'md-checked');
            cy.getByData('show-selbstaendig').invoke('attr', 'aria-checked').should('contain', 'false');

            cy.getByData('geschaeftsgewinn-basisjahr').should('not.exist');
            cy.getByData('geschaeftsgewinn-basisjahr-minus-1').should('not.exist');
            cy.getByData('geschaeftsgewinn-basisjahr-minus-2').should('not.exist');

            cy.getByData('show-ersatzeinkommen-selbststaendigkeit').should('not.exist');
            cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr').should('not.exist');
            cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-1').should('not.exist');
            cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-2').should('not.exist');
        });

    it('should display geschaeftsgewinn but not ersatzeinkommen selbststaendigkeit fields, if Selbstständigkeit is selected but ersatzeinkommen is not selected',
        () => {
            cy.getByData('show-selbstaendig').should('exist');
            cy.getByData('show-selbstaendig').should('not.have.class', 'md-checked');
            cy.getByData('show-selbstaendig').invoke('attr', 'aria-checked').should('contain', 'false');

            cy.getByData('show-selbstaendig').click();
            cy.getByData('show-selbstaendig').should('have.class', 'md-checked');
            cy.getByData('show-selbstaendig').invoke('attr', 'aria-checked').should('contain', 'true');

            cy.getByData('geschaeftsgewinn-basisjahr').should('exist');
            cy.getByData('geschaeftsgewinn-basisjahr-minus-1').should('exist');
            cy.getByData('geschaeftsgewinn-basisjahr-minus-2').should('exist');

            cy.getByData('show-ersatzeinkommen-selbststaendigkeit').should('exist');
            cy.getByData('show-ersatzeinkommen-selbststaendigkeit').should('not.have.class', 'md-checked');
            cy.getByData('show-ersatzeinkommen-selbststaendigkeit').invoke('attr', 'aria-checked').should('contain', 'false');
            cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr').should('not.exist');
            cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-1').should('not.exist');
            cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-2').should('not.exist');
        });

    it('should display geschaeftsgewinn and ersatzeinkommen selbststaendigkeit fields, if Selbstständigkeit and ersatzeinkommen is selected',
        () => {
            cy.getByData('show-selbstaendig').click();
            cy.getByData('show-selbstaendig').should('have.class', 'md-checked');
            cy.getByData('show-selbstaendig').invoke('attr', 'aria-checked').should('contain', 'true');

            cy.getByData('geschaeftsgewinn-basisjahr').should('exist');
            cy.getByData('geschaeftsgewinn-basisjahr-minus-1').should('exist');
            cy.getByData('geschaeftsgewinn-basisjahr-minus-2').should('exist');

            cy.getByData('show-ersatzeinkommen-selbststaendigkeit').should('exist');
            cy.getByData('show-ersatzeinkommen-selbststaendigkeit').should('not.have.class', 'md-checked');
            cy.getByData('show-ersatzeinkommen-selbststaendigkeit').invoke('attr', 'aria-checked').should('contain', 'false');

            cy.getByData('show-ersatzeinkommen-selbststaendigkeit').click();
            cy.getByData('show-ersatzeinkommen-selbststaendigkeit').should('have.class', 'md-checked');
            cy.getByData('show-ersatzeinkommen-selbststaendigkeit').invoke('attr', 'aria-checked').should('contain', 'true');

            cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr').should('exist');
            cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-1').should('exist');
            cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-2').should('exist');
        });

    it('should have ersatzeinkommen basisjahr disabled if ersatzeinkommen in finsit is 0', () => {
        cy.getByData('show-selbstaendig').click();
        cy.getByData('show-ersatzeinkommen-selbststaendigkeit').click();
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr').should('exist');
        cy.getByData('ersatzeinkommen').should('exist');
        cy.getByData('ersatzeinkommen').find('input').clear().type('0');
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr').find('input').should('be.disabled');
    });

    it('should have ersatzeinkommen basisjahr disabled if ersatzeinkommen in finsit is not 0, but geschaeftsgewinn basisjahr is empty',
        () => {
            cy.getByData('show-selbstaendig').click();
            cy.getByData('show-ersatzeinkommen-selbststaendigkeit').click();
            cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr').should('exist');
            cy.getByData('ersatzeinkommen').should('exist');
            cy.getByData('ersatzeinkommen').find('input').clear().type('1');
            cy.getByData('geschaeftsgewinn-basisjahr').find('input').clear();
            cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr').find('input').should('be.disabled');
        });

    it('should have ersatzeinkommen basisjahr enabled if ersatzeinkommen in finsit is not 0 and geschaeftsgewinn is not empty',
        () => {
            cy.getByData('show-selbstaendig').click();
            cy.getByData('show-ersatzeinkommen-selbststaendigkeit').click();
            cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr').should('exist');
            cy.getByData('ersatzeinkommen').find('input').clear().type('1');
            cy.getByData('geschaeftsgewinn-basisjahr').find('input').type('1');
            cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr').find('input').should('not.be.disabled');
        });

    it('should have ersatzeinkommen basisjahr minus 1 disabled if geschaeftsgewinn basisjahr minus 1 is empty', () => {
        cy.getByData('show-selbstaendig').click();
        cy.getByData('show-ersatzeinkommen-selbststaendigkeit').click();
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-1').should('exist');
        cy.getByData('geschaeftsgewinn-basisjahr-minus-1').find('input').clear();
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-1').find('input').should('be.disabled');
    });

    it('should have ersatzeinkommen basisjahr minus 1 enabled if geschaeftsgewinn basisjahr minus 1 is not empty', () => {
        cy.getByData('show-selbstaendig').click();
        cy.getByData('show-ersatzeinkommen-selbststaendigkeit').click();
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-1').should('exist');
        cy.getByData('geschaeftsgewinn-basisjahr-minus-1').find('input').clear().type('1');
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-1').find('input').should('not.be.disabled');
    });

    it('should have ersatzeinkommen basisjahr minus 2 disabled if geschaeftsgewinn minus 2 is empty', () => {
        cy.getByData('show-selbstaendig').click();
        cy.getByData('show-ersatzeinkommen-selbststaendigkeit').click();
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-2').should('exist');
        cy.getByData('geschaeftsgewinn-basisjahr-minus-2').find('input').clear();
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-2').find('input').should('be.disabled');
    });

    it('should have ersatzeinkommen basisjahr minus 2 enabled if geschaeftsgewinn minus 2 is not empty', () => {
        cy.getByData('show-selbstaendig').click();
        cy.getByData('show-ersatzeinkommen-selbststaendigkeit').click();
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-2').should('exist');
        cy.getByData('geschaeftsgewinn-basisjahr-minus-2').find('input').clear().type('1');
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-2').find('input').should('not.be.disabled');
    });

    it('should display error if ersatzeinkommen is selected but all three years are 0', () => {
        cy.getByData('show-selbstaendig').click();
        cy.getByData('show-ersatzeinkommen-selbststaendigkeit').click();
        cy.getByData('geschaeftsgewinn-basisjahr').find('input').clear().type('1');
        cy.getByData('geschaeftsgewinn-basisjahr-minus-1').find('input').clear().type('2');
        cy.getByData('geschaeftsgewinn-basisjahr-minus-2').find('input').clear().type('3');
        cy.getByData('ersatzeinkommen').find('input').clear().type('1');
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr').find('input').clear().type('0');
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-1').find('input').clear().type('0');
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-2').find('input').clear().type('0');
        cy.getByData('all-ersatzeinkommen-selbststaendigkeit-zero-error-message').should('exist');
    });

    it('should not display error if ersatzeinkommen is selected and one of the three years is not 0', () => {
        cy.getByData('show-selbstaendig').click();
        cy.getByData('show-ersatzeinkommen-selbststaendigkeit').click();
        cy.getByData('geschaeftsgewinn-basisjahr').find('input').clear().type('1');
        cy.getByData('geschaeftsgewinn-basisjahr-minus-1').find('input').clear().type('2');
        cy.getByData('geschaeftsgewinn-basisjahr-minus-2').find('input').clear().type('3');
        cy.getByData('ersatzeinkommen').find('input').clear().type('1');
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr').find('input').clear().type('1');
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-1').find('input').clear().type('0');
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-2').find('input').clear().type('0');
        cy.getByData('all-ersatzeinkommen-selbststaendigkeit-zero-error-message').should('not.exist');
    });

    it('should display error if ersatzeinkommen basisjahr is larger than ersatzeinkommen in finsit', () => {
        cy.getByData('show-selbstaendig').click();
        cy.getByData('show-ersatzeinkommen-selbststaendigkeit').click();
        cy.getByData('geschaeftsgewinn-basisjahr').find('input').clear().type('1');
        cy.getByData('ersatzeinkommen').find('input').clear().type('1');
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr').find('input').clear().type('2');
        cy.getByData('ersatzeinkommen-invalid-error-message').should('exist');
    });

    it('should not display error if ersatzeinkommen basisjahr is equal than ersatzeinkommen in finsit', () => {
        cy.getByData('show-selbstaendig').click();
        cy.getByData('show-ersatzeinkommen-selbststaendigkeit').click();
        cy.getByData('geschaeftsgewinn-basisjahr').find('input').clear().type('1');
        cy.getByData('ersatzeinkommen').find('input').clear().type('1');
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr').find('input').clear().type('1');
        cy.getByData('ersatzeinkommen-invalid-error-message').should('not.exist');
    });

    it('should not display error if ersatzeinkommen basisjahr is smaller than ersatzeinkommen in finsit', () => {
        cy.getByData('show-selbstaendig').click();
        cy.getByData('show-ersatzeinkommen-selbststaendigkeit').click();
        cy.getByData('geschaeftsgewinn-basisjahr').find('input').clear().type('1');
        cy.getByData('ersatzeinkommen').find('input').clear().type('1');
        cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr').find('input').clear().type('0');
        cy.getByData('ersatzeinkommen-invalid-error-message').should('not.exist');
    });
});
