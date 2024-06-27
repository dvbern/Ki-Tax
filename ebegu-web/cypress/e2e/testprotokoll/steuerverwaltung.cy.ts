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
    AntragCreationPO,
    ConfirmDialogPO,
    FaelleListePO,
    FallToolbarPO,
    KommentarPO,
    NavigationPO,
    STVKommentarDialogPO,
    TestFaellePO,
    VerfuegenPO
} from '@dv-e2e/page-objects';
import {getUser} from '@dv-e2e/types';
import {GesuchstellendePO} from '../../page-objects/antrag/gesuchstellende.po';
import {SidenavPO} from '../../page-objects/antrag/sidenav.po';

describe('Kibon - Gesuch zu Steuerverwaltung senden', () => {
    const userSuperadmin = getUser('[1-Superadmin] E-BEGU Superuser');
    const userSteueramt = getUser('[7-L-Steueramt] Rodolfo Iten');
    const userGemeinde = getUser('[6-L-SB-Gemeinde] Stefan Weibel');
    let gesuchUrl: string;
    let fallnummer: string;

    before(() => {
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
        cy.login(userSuperadmin);
        cy.visit('/#/faelle');

        TestFaellePO.createPapierTestfall({
            testFall: 'testfall-2',
            gemeinde: 'London',
            periode: '2023/24',
            betreuungsstatus: 'verfuegt'
        });

        cy.url().then(url => {
            const parts = new URL(url);
            gesuchUrl = `/${parts.hash}`;
        });

        FallToolbarPO.getFallnummer().should('not.be.empty');
        FallToolbarPO.getFallnummer().then(value => {
            fallnummer = value.text();
            FallToolbarPO.getFallnummer().should('contain.text', fallnummer);
        });
    });

    it('should send a gesuch for prüfung to steuerverwaltung and send it back to gemeinde', () => {
        cy.login(userGemeinde);
        cy.visit(gesuchUrl);
        AntragCreationPO.getVerantwortlicher().click();
        AntragCreationPO.getUserOption(userGemeinde, false).click();
        cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
            SidenavPO.goTo('VERFUEGEN');
        });

        VerfuegenPO.getSendToSTVButton().click();
        STVKommentarDialogPO.getSTVKommentarAbsendenButton().should(
            'have.focus'
        );
        STVKommentarDialogPO.getSTVKommentar()
            .focus()
            .type('Wie hoch ist der Nettolohn im Jahr 2022 von Yvonne Feuz?');
        STVKommentarDialogPO.getSTVKommentarAbsendenButton().click();
        SidenavPO.getGesuchStatus().should(
            'contain.text',
            'Prüfung Steuerbüro der Gemeinde'
        );

        cy.changeLogin(userSteueramt);
        cy.visit('/#/pendenzenSteueramt');

        cy.waitForRequest('GET', '**/gesuche/**', () => {
            FaelleListePO.getAntrag(fallnummer).click();
        });
        cy.waitForRequest('GET', '**/gesuche/**', () => {
            SidenavPO.goTo('GESUCHSTELLER');
        });
        SidenavPO.getGesuchStatus().should(
            'contain.text',
            'In Bearbeitung Steuerbüro der Gemeinde'
        );

        KommentarPO.getSTVBemerkungGemeinde().should(
            'have.value',
            'Wie hoch ist der Nettolohn im Jahr 2022 von Yvonne Feuz?'
        );
        GesuchstellendePO.getFormularTitle().should(
            'contain.text',
            'Antragsteller/in'
        );
        GesuchstellendePO.getFormularTitle().should('contain.text', '1');
        NavigationPO.saveAndGoNext();
        GesuchstellendePO.getFormularTitle().should(
            'contain.text',
            'Antragsteller/in'
        );
        GesuchstellendePO.getFormularTitle().should('contain.text', '2');
        NavigationPO.getSaveAndNextButton().should('not.exist');
        // TODO: remove this wait once a solution for textarea issues has been found
        cy.wait(1000);
        KommentarPO.getSTVBemerkung().type(
            "Der Nettolohn beträgt 50'000 CHF im Jahr 2021"
        );
        cy.waitForRequest('POST', '**/search/search', () => {
            KommentarPO.getSTVPruefungZurueckAnGemeindeButton().click();
            ConfirmDialogPO.getDvLoadingConfirmButton().click();
        });
        FaelleListePO.getAntrag(fallnummer).should('not.exist');

        cy.changeLogin(userGemeinde);
        cy.visit('/#/faelle');
        FaelleListePO.getAntrag(fallnummer).should('exist');

        cy.visit('/#/pendenzen');
        FaelleListePO.getAntrag(fallnummer).click();
        SidenavPO.getGesuchStatus().should(
            'contain.text',
            'Geprüft durch Steuerbüro der Gemeinde'
        );

        KommentarPO.getSTVBemerkung().should(
            'have.value',
            "Der Nettolohn beträgt 50'000 CHF im Jahr 2021"
        );
    });
});
