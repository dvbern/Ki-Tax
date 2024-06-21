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
    AntragBetreuungPO,
    ConfirmDialogPO,
    DossierToolbarPO,
    FaelleListePO,
    FallToolbarPO,
    MitteilungenPO,
    TestFaellePO
} from '@dv-e2e/page-objects';
import {getUser} from '@dv-e2e/types';
import {SidenavPO} from '../../page-objects/antrag/sidenav.po';

describe('Kibon - generate Testfälle [Gemeinde Sachbearbeiter]', () => {
    const userSB = getUser('[3-SB-Institution-Kita-Brünnen] Sophie Bergmann');
    const adminUser = getUser('[1-Superadmin] E-BEGU Superuser');
    const sachbearbeiterGemeindeUser = getUser(
        '[6-L-SB-Gemeinde] Stefan Weibel'
    );
    const monatlichesPensum = '60';
    const monatlicheKosten = '1000';
    const startdatum = '01.04.2023';
    const enddatum = '30.06.2023';
    let fallnummer: string;

    before(() => {
        cy.login(adminUser);
        cy.visit('/#/faelle');
        TestFaellePO.createPapierTestfall({
            testFall: 'testfall-1',
            betreuungsstatus: 'verfuegt',
            gemeinde: 'London',
            periode: '2022/23'
        });
        FallToolbarPO.getFallnummer().should('not.be.empty');
        FallToolbarPO.getFallnummer().then(value => {
            fallnummer = value.text();
            FallToolbarPO.getFallnummer().should('contain.text', fallnummer);
        });
    });

    it('should create a prefilled new Testfall Antrag and mutationsmeldung', () => {
        cy.login(userSB);
        cy.visit('/#/faelle');
        FaelleListePO.getAntrag(fallnummer).click();
        SidenavPO.goTo('BETREUUNG');
        AntragBetreuungPO.getBetreuung(0, 0).click();
        AntragBetreuungPO.getMutationsmeldungErstellenButton().click();
        AntragBetreuungPO.getBetreuungspensum(0)
            .clear()
            .type(monatlichesPensum);
        AntragBetreuungPO.getMonatlicheBetreuungskosten(0)
            .clear()
            .type(monatlicheKosten);
        AntragBetreuungPO.getBetreuungspensumAb(0)
            .find('input')
            .clear()
            .type(startdatum);
        AntragBetreuungPO.getBetreuungspensumBis(0)
            .find('input')
            .clear()
            .type(enddatum);
        AntragBetreuungPO.getMutationsmeldungSendenButton().click();
        cy.waitForRequest(
            'PUT',
            '**/mitteilungen/sendbetreuungsmitteilung',
            () => {
                ConfirmDialogPO.getDvLoadingConfirmButton().click();
            }
        );
        DossierToolbarPO.getMitteilungen().click();
        MitteilungenPO.getMitteilung(0).should('exist');
        MitteilungenPO.getMitteilung(0).click();
        MitteilungenPO.getInhaltOfMitteilung(0)
            .should('include.text', '1’000')
            .and('include.text', monatlichesPensum)
            .and('include.text', startdatum)
            .and('include.text', enddatum);
    });

    it('should accept the Mutationsmeldung', () => {
        cy.login(sachbearbeiterGemeindeUser);
        cy.visit('/#/faelle');
        FaelleListePO.getAntrag(fallnummer).click();
        FallToolbarPO.getFallnummer().should('not.be.empty');
        FallToolbarPO.getFallnummer().should('contain.text', fallnummer);
        DossierToolbarPO.getMitteilungen().click();
        MitteilungenPO.getMitteilung(0).click();
        MitteilungenPO.getMutationsmeldungHinzufuegenButton(0).click();
        cy.waitForRequest(
            'PUT',
            '**/mitteilungen/applybetreuungsmitteilung/**',
            () => {
                ConfirmDialogPO.getDvLoadingConfirmButton().click();
            }
        );
    });

    it('should check if the Mutationsmeldung was accepted', () => {
        cy.login(sachbearbeiterGemeindeUser);
        cy.visit('/#/faelle');
        FaelleListePO.getAntrag(fallnummer).first().click();
        SidenavPO.goTo('BETREUUNG');
        AntragBetreuungPO.getBetreuung(0, 1).click();
        AntragBetreuungPO.getMonatlicheBetreuungskosten(0).should(
            'have.value',
            monatlicheKosten
        );
        AntragBetreuungPO.getBetreuungspensumAb(0)
            .find('input')
            .should('have.value', startdatum);
        AntragBetreuungPO.getBetreuungspensumBis(0)
            .find('input')
            .should('have.value', enddatum);
    });
});
