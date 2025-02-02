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
    TestFaellePO,
    VerfuegenPO,
    VerfuegungPO
} from '@dv-e2e/page-objects';
import {getUser} from '@dv-e2e/types';
import {SidenavPO} from '../../page-objects/antrag/sidenav.po';

describe('Kibon - Tagesschule Only [Superadmin]', () => {
    const adminUser = getUser('[1-Superadmin] E-BEGU Superuser');
    const adminGemeindeTSParisUser = getUser('[6-P-Admin-TS] Adrian Schuler');
    let gesuchUrl: string;

    before(() => {
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
        cy.login(adminUser);
        cy.visit('/#/faelle');
        TestFaellePO.createPapierTestfall({
            testFall: 'testfall-1',
            gemeinde: 'Paris',
            periode: '2022/23',
            betreuungsstatus: 'warten'
        });

        // get AntragsId
        cy.url().then(url => {
            const parts = new URL(url);
            gesuchUrl = `/${parts.hash}`;
        });
    });

    it('should create a prefilled new Testfall Antrag', () => {
        // login as Administrator TS der Gemeinde
        cy.login(adminGemeindeTSParisUser);

        // go to the Antrag
        cy.visit(gesuchUrl);
        SidenavPO.goTo('BETREUUNG');

        // delete other betreuung nur ts
        cy.waitForRequest('GET', '**/wizard-steps/**', () => {
            AntragBetreuungPO.getBetreuungLoeschenButton(0, 1).click();
            ConfirmDialogPO.getDvLoadingConfirmButton().click();
        });
        AntragBetreuungPO.getBetreuung(0, 1).should('not.exist');

        cy.waitForRequest('GET', '**/wizard-steps/**', () => {
            AntragBetreuungPO.getBetreuungLoeschenButton(0, 0).click();
            ConfirmDialogPO.getDvLoadingConfirmButton().click();
        });
        AntragBetreuungPO.getBetreuung(0, 0).should('not.exist');

        AntragBetreuungPO.createNewBetreuung(0);

        // Antrag bearbeiten - anmeldung Tagesschule erfassen
        AntragBetreuungPO.selectTagesschulBetreuung();
        AntragBetreuungPO.fillTagesschulBetreuungsForm('withValid', 'Paris');
        AntragBetreuungPO.saveAndConfirmBetreuung();

        // anmeldung akkzeptieren
        AntragBetreuungPO.getBetreuung(0, 0).click();
        AntragBetreuungPO.platzAkzeptieren();

        // Antrag abschliessen
        SidenavPO.goTo('VERFUEGEN');
        VerfuegenPO.finSitAkzeptieren();
        cy.waitForRequest('GET', '**/gesuche/dossier/**', () => {
            VerfuegenPO.getAbschliessenButton().click();
            ConfirmDialogPO.getDvLoadingConfirmButton().click();
        });

        // Control status and tarif are definitiv
        SidenavPO.getGesuchStatus().should('have.text', 'Abgeschlossen');
        VerfuegenPO.getBetreuungsstatus(0, 0).should(
            'have.text',
            'Anmeldung übernommen'
        );
        VerfuegenPO.getVerfuegung(0, 0).click();
        VerfuegungPO.getProvisorischerTarifTitel().should('not.exist');
    });
});
