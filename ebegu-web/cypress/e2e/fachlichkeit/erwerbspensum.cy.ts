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
    AntragFamSitPO,
    BeschaeftigungspensumListPO,
    GesuchstellendeDashboardPO,
    NavigationPO,
    SidenavPO,
    TestFaellePO,
} from '@dv-e2e/page-objects';
import {getUser, TestPeriode} from '@dv-e2e/types';
import {TSUnterhaltsvereinbarungAnswer} from '../../../src/models/enums/TSUnterhaltsvereinbarungAnswer';

describe('Kibon - Testet die Fachlichkeit auf der Seite der Erwerbspensen', () => {

    const superAdmin = getUser('[1-Superadmin] E-BEGU Superuser');
    const gesuchstellende = getUser('[5-GS] Heinrich Mueller');
    const periode: TestPeriode = '2023/24';

    before(() => {
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
        cy.login(superAdmin);
        cy.visit('/#/faelle');
        TestFaellePO.createOnlineTestfall({
            testFall: 'testfall-2',
            gemeinde: 'London',
            periode: periode,
            betreuungsstatus: 'bestaetigt',
            besitzerin: '[5-GS] Heinrich Mueller',
        });
    });

    beforeEach(() => {
        cy.login(gesuchstellende);
        cy.visit('/');
        cy.waitForRequest('GET', '**/dossier/fall/*', () => {
            GesuchstellendeDashboardPO.getAntragBearbeitenButton(periode).click();
        });
    });

    it('should not require beschaeftigungspensum when gesuch is beendet because of konkubinat ohne kind and keine geteilte obhut',
        () => {
            SidenavPO.goTo('FAMILIENSITUATION');
            // Spezialfall Antrag beenden weil Konkubinat 2-jährig wird
            AntragFamSitPO.getFamiliensituationsStatus('KONKUBINAT_KEIN_KIND').click();
            AntragFamSitPO.getKonkubinatStart().clear().type('1.10.2021').blur();
            // Spezialfall Konkubinat ohne Kind, Nicht geteilte Obhut ohne Unterhaltsvereinbarung
            AntragFamSitPO.getGeteilteObhutOption('nein').find('label').click();
            AntragFamSitPO.getUnterhaltsvereinbarungOption(TSUnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG)
                .find('label')
                .click();
            NavigationPO.saveAndGoNext();

            SidenavPO.goTo('ERWERBSPENSUM');
            BeschaeftigungspensumListPO.getGS1().should('exist');
            BeschaeftigungspensumListPO.getGS2().should('not.exist');
        });

    it('should not require beschaeftigungspensum konkubinat ohne kind is less than 2 years', () => {
        SidenavPO.goTo('FAMILIENSITUATION');
        // Spezialfall Antrag beenden weil Konkubinat 2-jährig wird
        AntragFamSitPO.getFamiliensituationsStatus('KONKUBINAT_KEIN_KIND').click();
        AntragFamSitPO.getKonkubinatStart().clear().type('1.10.2022').blur();
        // Spezialfall Konkubinat ohne Kind, Nicht geteilte Obhut ohne Unterhaltsvereinbarung
        AntragFamSitPO.getGeteilteObhutOption('nein').find('label').click();
        AntragFamSitPO.getUnterhaltsvereinbarungOption(TSUnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG)
            .find('label')
            .click();
        NavigationPO.saveAndGoNext();

        SidenavPO.goTo('ERWERBSPENSUM');
        BeschaeftigungspensumListPO.getGS1().should('exist');
        BeschaeftigungspensumListPO.getGS2().should('not.exist');
    });

    it('should require beschaeftigungspensum when konkubinat ohne kind is more than 2 years', () => {
        SidenavPO.goTo('FAMILIENSITUATION');
        // Spezialfall Antrag beenden weil Konkubinat 2-jährig wird
        AntragFamSitPO.getFamiliensituationsStatus('KONKUBINAT_KEIN_KIND').click();
        AntragFamSitPO.getKonkubinatStart().clear().type('1.10.2020').blur();
        NavigationPO.saveAndGoNext();

        SidenavPO.goTo('ERWERBSPENSUM');
        BeschaeftigungspensumListPO.getGS1().should('exist');
        BeschaeftigungspensumListPO.getGS2().should('exist');
    });

});
