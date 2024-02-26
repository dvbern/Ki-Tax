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

import {FinanzielleSituationPO, NavigationPO, TestFaellePO} from '@dv-e2e/page-objects';
import {getUser} from '@dv-e2e/types';
import {SidenavPO} from '../page-objects/antrag/sidenav.po';

describe('kiBon - Features auf der FinSit - Page', () => {
    const superAdmin = getUser('[1-Superadmin] E-BEGU Superuser');
    const sbBGLondon = getUser('[6-L-SB-BG] Jörg Keller');

    let gesuchUrl: string;

    before('create Antrag', () => {
        cy.login(superAdmin);
        cy.visit('/#/faelle');

        TestFaellePO.createPapierTestfall({
            testFall: 'testfall-1',
            gemeinde: 'London',
            betreuungsstatus: 'warten',
            periode: '2024/25',
        });
        cy.url().then((url) => {
            const parts = new URL(url);
            gesuchUrl = `/${parts.hash}`;
        });
    });

    describe('ersatzeinkommen', () => {

        beforeEach(() => {
            cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
            cy.login(sbBGLondon);
            cy.waitForRequest('GET', '**/gesuchsperioden/gemeinde/**', () => {
                cy.visit(gesuchUrl);
            });
            cy.waitForRequest('GET', '**/FINANZIELLE_SITUATION_TYP/**', () => {
                SidenavPO.goTo('FINANZIELLE_SITUATION');
            });
            cy.waitForRequest('POST', '**/calculateTemp', () => {
                NavigationPO.saveAndGoNext();
            });
            // Problem mit den angularJS checkboxen, die manchmal nicht richtig anwählbar sind. Workaround ist kurz zu warten.
            cy.wait(1500);
        });

        it('should not display geschaeftsgewinn and ersatzeinkommen selbststaendigkeit fields, if Selbstständigkeit is not selected',
            () => {

                FinanzielleSituationPO.getShowSelbstaendig().should('exist');
                FinanzielleSituationPO.getShowSelbstaendig().should('not.have.class', 'md-checked');
                FinanzielleSituationPO.getShowSelbstaendig().invoke('attr', 'aria-checked').should('contain', 'false');

                FinanzielleSituationPO.getGeschaeftsgewinnBasisjahr().should('not.exist');
                FinanzielleSituationPO.getGeschaeftsgewinnBasisjahrMinus1().should('not.exist');
                FinanzielleSituationPO.getGeschaeftsgewinnBasisjahrMinus2().should('not.exist');

                FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeit().should('not.exist');

                FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahr().should('not.exist');
                FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1().should('not.exist');
                FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2().should('not.exist');
            });

        it('should display geschaeftsgewinn but not ersatzeinkommen selbststaendigkeit fields, if Selbstständigkeit is selected but ersatzeinkommen is not selected',
            () => {
                FinanzielleSituationPO.getShowSelbstaendig().should('exist');
                FinanzielleSituationPO.getShowSelbstaendig().should('not.have.class', 'md-checked');
                FinanzielleSituationPO.getShowSelbstaendig().invoke('attr', 'aria-checked').should('contain', 'false');

                FinanzielleSituationPO.getShowSelbstaendig().click();
                FinanzielleSituationPO.getShowSelbstaendig().should('have.class', 'md-checked');
                FinanzielleSituationPO.getShowSelbstaendig().invoke('attr', 'aria-checked').should('contain', 'true');

                FinanzielleSituationPO.getGeschaeftsgewinnBasisjahr().should('exist');
                FinanzielleSituationPO.getGeschaeftsgewinnBasisjahrMinus1().should('exist');
                FinanzielleSituationPO.getGeschaeftsgewinnBasisjahrMinus2().should('exist');

                FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeit().should('exist');

                FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja')
                    .should('not.have.class', 'md-checked');
                FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja')
                    .invoke('attr', 'aria-checked')
                    .should('contain', 'false');
                FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahr().should('not.exist');
                FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1().should('not.exist');
                FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2().should('not.exist');
            });

        it('should display geschaeftsgewinn and ersatzeinkommen selbststaendigkeit fields, if Selbstständigkeit and ersatzeinkommen is selected',
            () => {
                FinanzielleSituationPO.getShowSelbstaendig().click();
                FinanzielleSituationPO.getShowSelbstaendig().should('have.class', 'md-checked');
                FinanzielleSituationPO.getShowSelbstaendig().invoke('attr', 'aria-checked').should('contain', 'true');

                FinanzielleSituationPO.getGeschaeftsgewinnBasisjahr().should('exist');
                FinanzielleSituationPO.getGeschaeftsgewinnBasisjahrMinus1().should('exist');
                FinanzielleSituationPO.getGeschaeftsgewinnBasisjahrMinus2().should('exist');

                FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeit().should('exist');
                FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja')
                    .should('not.have.class', 'md-checked');
                FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja')
                    .invoke('attr', 'aria-checked')
                    .should('contain', 'false');

                FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja').click();
                FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja').should('have.class', 'md-checked');
                FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja')
                    .invoke('attr', 'aria-checked')
                    .should('contain', 'true');

                FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahr().should('exist');
                FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1().should('exist');
                FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2().should('exist');
            });

        it('should have ersatzeinkommen basisjahr disabled if ersatzeinkommen in finsit is 0', () => {
            FinanzielleSituationPO.getShowSelbstaendig().click();
            FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja').click();
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahr().should('exist');

            FinanzielleSituationPO.getErsatzeinkommen().should('exist');
            FinanzielleSituationPO.getErsatzeinkommen().find('input').clear().type('0');
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahr().find('input').should('be.disabled');
        });

        it('should have ersatzeinkommen basisjahr disabled if ersatzeinkommen in finsit is not 0, but geschaeftsgewinn basisjahr is empty',
            () => {
                FinanzielleSituationPO.getShowSelbstaendig().click();
                FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja').click();
                FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahr().should('exist');
                FinanzielleSituationPO.getErsatzeinkommen().should('exist');
                FinanzielleSituationPO.getErsatzeinkommen().find('input').clear().type('1');
                FinanzielleSituationPO.getGeschaeftsgewinnBasisjahr().find('input').clear();
                FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahr().find('input').should('be.disabled');
            });

        it('should have ersatzeinkommen basisjahr enabled if ersatzeinkommen in finsit is not 0 and geschaeftsgewinn is not empty',
            () => {
                FinanzielleSituationPO.getShowSelbstaendig().click();
                FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja').click();
                FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahr().should('exist');
                FinanzielleSituationPO.getErsatzeinkommen().find('input').clear().type('1');
                FinanzielleSituationPO.getGeschaeftsgewinnBasisjahr().find('input').type('1');
                FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahr().find('input').should('not.be.disabled');
            });

        it('should have ersatzeinkommen basisjahr minus 1 disabled if geschaeftsgewinn basisjahr minus 1 is empty', () => {
            FinanzielleSituationPO.getShowSelbstaendig().click();
            FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja').click();
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1().should('exist');
            FinanzielleSituationPO.getGeschaeftsgewinnBasisjahrMinus1().find('input').clear();
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1().find('input').should('be.disabled');
        });

        it('should have ersatzeinkommen basisjahr minus 1 enabled if geschaeftsgewinn basisjahr minus 1 is not empty', () => {
            FinanzielleSituationPO.getShowSelbstaendig().click();
            FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja').click();
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1().should('exist');
            FinanzielleSituationPO.getGeschaeftsgewinnBasisjahrMinus1().find('input').clear().type('1');
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1().find('input').should('not.be.disabled');
        });

        it('should have ersatzeinkommen basisjahr minus 2 disabled if geschaeftsgewinn minus 2 is empty', () => {
            FinanzielleSituationPO.getShowSelbstaendig().click();
            FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja').click();
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2().should('exist');
            FinanzielleSituationPO.getGeschaeftsgewinnBasisjahrMinus2().find('input').clear();
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2().find('input').should('be.disabled');
        });

        it('should have ersatzeinkommen basisjahr minus 2 enabled if geschaeftsgewinn minus 2 is not empty', () => {
            FinanzielleSituationPO.getShowSelbstaendig().click();
            FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja').click();
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2().should('exist');
            FinanzielleSituationPO.getGeschaeftsgewinnBasisjahrMinus2().find('input').clear().type('1');
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2().find('input').should('not.be.disabled');
        });

        it('should display error if ersatzeinkommen is selected but all three years are 0', () => {
            FinanzielleSituationPO.getShowSelbstaendig().click();
            FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja').click();
            FinanzielleSituationPO.getGeschaeftsgewinnBasisjahr().find('input').clear().type('1');
            FinanzielleSituationPO.getGeschaeftsgewinnBasisjahrMinus1().find('input').clear().type('2');
            FinanzielleSituationPO.getGeschaeftsgewinnBasisjahrMinus2().find('input').clear().type('3');
            FinanzielleSituationPO.getErsatzeinkommen().find('input').clear().type('1');
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahr().find('input').clear().type('0');
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1().find('input').clear().type('0');
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2().find('input').clear().type('0');
            FinanzielleSituationPO.getAllErsatzeinkommenSelbststaendigkeitZeroErrorMessage().should('exist');
        });

        it('should not display error if ersatzeinkommen is selected and one of the three years is not 0', () => {
            FinanzielleSituationPO.getShowSelbstaendig().click();
            FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja').click();
            FinanzielleSituationPO.getGeschaeftsgewinnBasisjahr().find('input').clear().type('1');
            FinanzielleSituationPO.getGeschaeftsgewinnBasisjahrMinus1().find('input').clear().type('2');
            FinanzielleSituationPO.getGeschaeftsgewinnBasisjahrMinus2().find('input').clear().type('3');
            FinanzielleSituationPO.getErsatzeinkommen().find('input').clear().type('1');
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahr().find('input').clear().type('1');
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1().find('input').clear().type('0');
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2().find('input').clear().type('0');
            FinanzielleSituationPO.getAllErsatzeinkommenSelbststaendigkeitZeroErrorMessage().should('not.exist');
        });

        it('should display error if ersatzeinkommen basisjahr is larger than ersatzeinkommen in finsit', () => {
            FinanzielleSituationPO.getShowSelbstaendig().click();
            FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja').click();
            FinanzielleSituationPO.getGeschaeftsgewinnBasisjahr().find('input').clear().type('1');
            FinanzielleSituationPO.getErsatzeinkommen().find('input').clear().type('1');
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahr().find('input').clear().type('2');
            FinanzielleSituationPO.getErsatzeinkommenInvalidErrorMessage().should('exist');
        });

        it('should not display error if ersatzeinkommen basisjahr is equal than ersatzeinkommen in finsit', () => {
            FinanzielleSituationPO.getShowSelbstaendig().click();
            FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja').click();
            FinanzielleSituationPO.getGeschaeftsgewinnBasisjahr().find('input').clear().type('1');
            FinanzielleSituationPO.getErsatzeinkommen().find('input').clear().type('1');
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahr().find('input').clear().type('1');
            FinanzielleSituationPO.getErsatzeinkommenInvalidErrorMessage().should('not.exist');
        });

        it('should not display error if ersatzeinkommen basisjahr is smaller than ersatzeinkommen in finsit', () => {
            FinanzielleSituationPO.getShowSelbstaendig().click();
            FinanzielleSituationPO.getShowErsatzeinkommenSelbststaendigkeitRadioButton('ja').click();
            FinanzielleSituationPO.getGeschaeftsgewinnBasisjahr().find('input').clear().type('1');
            FinanzielleSituationPO.getErsatzeinkommen().find('input').clear().type('1');
            FinanzielleSituationPO.getErsatzeinkommenSelbststaendigkeitBasisjahr().find('input').clear().type('0');
            FinanzielleSituationPO.getErsatzeinkommenInvalidErrorMessage().should('not.exist');
        });
    })


});
