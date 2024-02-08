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
    AntragFamSitPO,
    BeschaeftigungspensumListPO, DossierToolbarPO,
    GesuchstellendeDashboardPO,
    NavigationPO, RemoveDialogPO,
    SidenavPO,
    TestFaellePO,
} from '@dv-e2e/page-objects';
import {getUser, TestFall, TestPeriode} from '@dv-e2e/types';
import {describe} from 'mocha';
import {TSUnterhaltsvereinbarungAnswer} from '../../../src/models/enums/TSUnterhaltsvereinbarungAnswer';

describe('Kibon - Testet die Fachlichkeit auf der Seite der Erwerbspensen', () => {

    describe('Grundantrag Familiensituation', () => {
        const gesuchstellende = getUser('[5-GS] Heinrich Mueller');
        const periode: TestPeriode = '2024/25';

        describe('Konkubinat ohne Kind', () => {

            beforeEach(() => {
                cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
                createOnlineAntrag(periode, 'testfall-2');
                cy.login(gesuchstellende);
                cy.visit('/');
                cy.waitForRequest('GET', '**/dossier/fall/*', () => {
                    GesuchstellendeDashboardPO.getAntragBearbeitenButton(periode).click();
                });
            });

            describe('konkubinat reaches x years in periode', () => {
                it('should not require beschaeftigungspensum when konkubinat ohne kind reaches x years in periode, keine geteilte obhut and keine unterhaltsvereinbarung',
                    () => {
                        SidenavPO.goTo('FAMILIENSITUATION');
                        // Spezialfall Antrag beenden weil Konkubinat 2-jährig wird
                        AntragFamSitPO.getFamiliensituationsStatus('KONKUBINAT_KEIN_KIND').click();
                        AntragFamSitPO.getKonkubinatStart().clear().type('1.1.2023').blur();
                        // Spezialfall Konkubinat ohne Kind, Nicht geteilte Obhut ohne Unterhaltsvereinbarung
                        AntragFamSitPO.getGeteilteObhutOption('nein').click();
                        AntragFamSitPO.getUnterhaltsvereinbarungOption(TSUnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG)
                            .click();
                        NavigationPO.saveAndGoNext();

                        SidenavPO.getSidenavStepStatus('ERWERBSPENSUM').should('have.class', 'fa-check');
                        SidenavPO.goTo('ERWERBSPENSUM');
                        BeschaeftigungspensumListPO.getGS1().should('exist');
                        BeschaeftigungspensumListPO.getGS2().should('not.exist');
                    });

                it('should require beschaeftigungspensum when konkubinat ohne kind reaches x years in periode, keine geteilte obhut and keine unterhaltsvereinbarung possible',
                    () => {
                        SidenavPO.goTo('FAMILIENSITUATION');
                        // Spezialfall Antrag beenden weil Konkubinat 2-jährig wird
                        AntragFamSitPO.getFamiliensituationsStatus('KONKUBINAT_KEIN_KIND').click();
                        AntragFamSitPO.getKonkubinatStart().clear().type('1.1.2023').blur();
                        // Spezialfall Konkubinat ohne Kind, Nicht geteilte Obhut ohne Unterhaltsvereinbarung
                        AntragFamSitPO.getGeteilteObhutOption('nein').click();
                        AntragFamSitPO.getUnterhaltsvereinbarungOption(TSUnterhaltsvereinbarungAnswer.UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH)
                            .click();
                        NavigationPO.saveAndGoNext();

                        SidenavPO.getSidenavStepStatus('ERWERBSPENSUM').should('have.class', 'fa-check');
                        SidenavPO.goTo('ERWERBSPENSUM');
                        BeschaeftigungspensumListPO.getGS1().should('exist');
                        BeschaeftigungspensumListPO.getGS2().should('exist');
                    });

                it('should require two beschaeftigungspensum when konkubinat ohne kind reaches x years in periode, keine geteilte obhut and unterhaltsvereinbarung',
                    () => {
                        SidenavPO.goTo('FAMILIENSITUATION');
                        // Spezialfall Antrag beenden weil Konkubinat 2-jährig wird
                        AntragFamSitPO.getFamiliensituationsStatus('KONKUBINAT_KEIN_KIND').click();
                        AntragFamSitPO.getKonkubinatStart().clear().type('1.1.2023').blur();
                        // Spezialfall Konkubinat ohne Kind, Nicht geteilte Obhut ohne Unterhaltsvereinbarung
                        AntragFamSitPO.getGeteilteObhutOption('nein').click();
                        AntragFamSitPO.getUnterhaltsvereinbarungOption(TSUnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG)
                            .click();
                        NavigationPO.saveAndGoNext();

                        SidenavPO.getSidenavStepStatus('ERWERBSPENSUM').should('have.class', 'fa-check');
                        SidenavPO.goTo('ERWERBSPENSUM');
                        BeschaeftigungspensumListPO.getGS1().should('exist');
                        BeschaeftigungspensumListPO.getGS2().should('exist');
                    });

                it('should require two beschaeftigungspensum when konkubinat ohne kind reaches x years in periode, geteilte obhut and antrag alleine',
                    () => {
                        SidenavPO.goTo('FAMILIENSITUATION');
                        // Spezialfall Antrag beenden weil Konkubinat 2-jährig wird
                        AntragFamSitPO.getFamiliensituationsStatus('KONKUBINAT_KEIN_KIND').click();
                        AntragFamSitPO.getKonkubinatStart().clear().type('1.1.2023').blur();
                        // Spezialfall Konkubinat ohne Kind, Nicht geteilte Obhut ohne Unterhaltsvereinbarung
                        AntragFamSitPO.getGeteilteObhutOption('nein').click();
                        AntragFamSitPO.getUnterhaltsvereinbarungOption(TSUnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG)
                            .click();
                        NavigationPO.saveAndGoNext();

                        SidenavPO.getSidenavStepStatus('ERWERBSPENSUM').should('have.class', 'fa-check');
                        SidenavPO.goTo('ERWERBSPENSUM');
                        BeschaeftigungspensumListPO.getGS1().should('exist');
                        BeschaeftigungspensumListPO.getGS2().should('exist');
                    });

                it('should require two beschaeftigungspensum when konkubinat ohne kind reaches x years in periode, geteilte obhut and antrag zu zweit',
                    () => {
                        SidenavPO.goTo('FAMILIENSITUATION');
                        // Spezialfall Antrag beenden weil Konkubinat 2-jährig wird
                        AntragFamSitPO.getFamiliensituationsStatus('KONKUBINAT_KEIN_KIND').click();
                        AntragFamSitPO.getKonkubinatStart().clear().type('1.1.2023').blur();
                        // Spezialfall Konkubinat ohne Kind, Nicht geteilte Obhut ohne Unterhaltsvereinbarung
                        AntragFamSitPO.getGeteilteObhutOption('nein').click();
                        AntragFamSitPO.getUnterhaltsvereinbarungOption(TSUnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG)
                            .click();
                        NavigationPO.saveAndGoNext();

                        SidenavPO.getSidenavStepStatus('ERWERBSPENSUM').should('have.class', 'fa-check');
                        SidenavPO.goTo('ERWERBSPENSUM');
                        BeschaeftigungspensumListPO.getGS1().should('exist');
                        BeschaeftigungspensumListPO.getGS2().should('exist');
                    });
            });

            it('should not require beschaeftigungspensum konkubinat ohne kind is less than 2 years', () => {
                SidenavPO.goTo('FAMILIENSITUATION');
                // Spezialfall Antrag beenden weil Konkubinat 2-jährig wird
                AntragFamSitPO.getFamiliensituationsStatus('KONKUBINAT_KEIN_KIND').click();
                AntragFamSitPO.getKonkubinatStart().clear().type('1.10.2022').blur();
                // Spezialfall Konkubinat ohne Kind, Nicht geteilte Obhut ohne Unterhaltsvereinbarung
                AntragFamSitPO.getGeteilteObhutOption('nein').click();
                AntragFamSitPO.getUnterhaltsvereinbarungOption(TSUnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG)
                    .click();
                NavigationPO.saveAndGoNext();

                SidenavPO.getSidenavStepStatus('ERWERBSPENSUM').should('have.class', 'fa-check');
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

                SidenavPO.getSidenavStepStatus('ERWERBSPENSUM').should('have.class', 'fa-check');
                SidenavPO.goTo('ERWERBSPENSUM');
                BeschaeftigungspensumListPO.getGS1().should('exist');
                BeschaeftigungspensumListPO.getGS2().should('exist');
            });
        });

        describe('Alleinstehend', () => {

            beforeEach(() => {
                cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
                createOnlineAntrag(periode, 'testfall-1');
                cy.login(gesuchstellende);
                cy.visit('/');
                cy.waitForRequest('GET', '**/dossier/fall/*', () => {
                    GesuchstellendeDashboardPO.getAntragBearbeitenButton(periode).click();
                });
            });

            it('should only require one erwerbspensum if famsit status Alleinerziehend with shared obhut and alleine stellen',
                () => {
                    SidenavPO.goTo('FAMILIENSITUATION');
                    AntragFamSitPO.getFamiliensituationsStatus('ALLEINERZIEHEND').click();
                    AntragFamSitPO.getGeteilteObhutOption('ja').click();
                    AntragFamSitPO.getGesuchstellendeKardinalitaet('ALLEINE').click();
                    NavigationPO.saveAndGoNext();
                    SidenavPO.getSidenavStepStatus('ERWERBSPENSUM').should('have.class', 'fa-check');

                    SidenavPO.goTo('ERWERBSPENSUM');
                    BeschaeftigungspensumListPO.getGS1().should('exist');
                    BeschaeftigungspensumListPO.getGS2().should('not.exist');
                });

            it('should require two erwerbspensum if famsit status Alleinerziehend with shared obhut and zu zweit stellen', () => {
                SidenavPO.goTo('FAMILIENSITUATION');
                AntragFamSitPO.getFamiliensituationsStatus('ALLEINERZIEHEND').click();
                AntragFamSitPO.getGeteilteObhutOption('ja').click();
                AntragFamSitPO.getGesuchstellendeKardinalitaet('ZU_ZWEIT').click();
                NavigationPO.saveAndGoNext();
                SidenavPO.getSidenavStepStatus('ERWERBSPENSUM').should('have.class', 'fa-close');

                SidenavPO.goTo('ERWERBSPENSUM');
                BeschaeftigungspensumListPO.getGS1().should('exist');
                BeschaeftigungspensumListPO.getGS2().should('exist');
            });

            it('should require one erwerbspensum if famsit status Alleinerziehend with not shared obhut and with no unterhaltsvereinbarung',
                () => {
                    SidenavPO.goTo('FAMILIENSITUATION');
                    AntragFamSitPO.getFamiliensituationsStatus('ALLEINERZIEHEND').click();
                    AntragFamSitPO.getGeteilteObhutOption('nein').click();
                    AntragFamSitPO.getUnterhaltsvereinbarungOption('NEIN_UNTERHALTSVEREINBARUNG').click();
                    NavigationPO.saveAndGoNext();
                    SidenavPO.getSidenavStepStatus('ERWERBSPENSUM').should('have.class', 'fa-check');

                    SidenavPO.goTo('ERWERBSPENSUM');
                    BeschaeftigungspensumListPO.getGS1().should('exist');
                    BeschaeftigungspensumListPO.getGS2().should('not.exist');
                });

            it('should require one erwerbspensum if famsit status Alleinerziehend with not shared obhut and with unterhaltsvereinbarung',
                () => {
                    SidenavPO.goTo('FAMILIENSITUATION');
                    AntragFamSitPO.getFamiliensituationsStatus('ALLEINERZIEHEND').click();
                    AntragFamSitPO.getGeteilteObhutOption('nein').click();
                    AntragFamSitPO.getUnterhaltsvereinbarungOption('JA_UNTERHALTSVEREINBARUNG').click();
                    NavigationPO.saveAndGoNext();
                    SidenavPO.getSidenavStepStatus('ERWERBSPENSUM').should('have.class', 'fa-check');

                    SidenavPO.goTo('ERWERBSPENSUM');
                    BeschaeftigungspensumListPO.getGS1().should('exist');
                    BeschaeftigungspensumListPO.getGS2().should('not.exist');
                });

            it('should only require one erwerbspensum if famsit status Alleinerziehend with not shared obhut and with no unterhaltsvereinbarung possible',
                () => {
                    SidenavPO.goTo('FAMILIENSITUATION');
                    AntragFamSitPO.getFamiliensituationsStatus('ALLEINERZIEHEND').click();
                    AntragFamSitPO.getGeteilteObhutOption('nein').click();
                    AntragFamSitPO.getUnterhaltsvereinbarungOption('UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH').click();
                    AntragFamSitPO.getUnterhaltsvereinbarungNichtMoeglichBegruendung()
                        .clear()
                        .type('Anderer Elternteil unbekannt');
                    NavigationPO.saveAndGoNext();
                    SidenavPO.getSidenavStepStatus('ERWERBSPENSUM').should('have.class', 'fa-check');

                    SidenavPO.goTo('ERWERBSPENSUM');
                    BeschaeftigungspensumListPO.getGS1().should('exist');
                    BeschaeftigungspensumListPO.getGS2().should('not.exist');
                });

        });

        describe('Verheiratet', () => {
            beforeEach(() => {
                cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
                createOnlineAntrag(periode, 'testfall-2');
                cy.login(gesuchstellende);
                cy.visit('/');
                cy.waitForRequest('GET', '**/dossier/fall/*', () => {
                    GesuchstellendeDashboardPO.getAntragBearbeitenButton(periode).click();
                });
            });

            it('should require erwerbspensum gs2', () => {
                SidenavPO.goTo('FAMILIENSITUATION');
                AntragFamSitPO.getFamiliensituationsStatus('VERHEIRATET').click();
                NavigationPO.saveAndGoNext();
                SidenavPO.goTo('ERWERBSPENSUM');
                BeschaeftigungspensumListPO.getGS1().should('exist');
                BeschaeftigungspensumListPO.getGS2().should('exist');
            });
        });

        describe('Konkubinat', () => {
            beforeEach(() => {
                cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
                createOnlineAntrag(periode, 'testfall-2');
                cy.login(gesuchstellende);
                cy.visit('/');
                cy.waitForRequest('GET', '**/dossier/fall/*', () => {
                    GesuchstellendeDashboardPO.getAntragBearbeitenButton(periode).click();
                });
            });

            it('should require erwerbspensum gs2', () => {
                SidenavPO.goTo('FAMILIENSITUATION');
                AntragFamSitPO.getFamiliensituationsStatus('KONKUBINAT').click();
                NavigationPO.saveAndGoNext();
                SidenavPO.goTo('ERWERBSPENSUM');
                BeschaeftigungspensumListPO.getGS1().should('exist');
                BeschaeftigungspensumListPO.getGS2().should('exist');
            });
        });
    });

    describe('Mutation Papierantrag', () => {
        it('should not require beschaeftigungspensum in mutation from verheiratet to konkubinat ohne kind 2 jährig during gp',
            () => {
                cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
                const superAdmin = getUser('[1-Superadmin] E-BEGU Superuser');
                cy.login(superAdmin);
                cy.visit('/');
                TestFaellePO.createPapierTestfall({
                    testFall: 'testfall-2',
                    betreuungsstatus: 'verfuegt',
                    periode: '2024/25',
                    gemeinde: 'London',
                });

                cy.waitForRequest('GET', '**/gesuchsperioden/gemeinde/*', () => {
                    DossierToolbarPO.getAntragMutieren().click();
                });
                AntragCreationPO.getEingangsdatum().find('input').clear().type('1.1.2020');
                NavigationPO.saveAndGoNext();

                AntragFamSitPO.getAenderunPer().clear().type('1.1.2023').blur();
                AntragFamSitPO.getFamiliensituationsStatus('KONKUBINAT_KEIN_KIND').click();
                AntragFamSitPO.getGeteilteObhutOption('nein').click();
                AntragFamSitPO.getUnterhaltsvereinbarungOption(TSUnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG)
                    .click();
                NavigationPO.saveAndGoNext();
                RemoveDialogPO.getRemoveOkButton().click();

                SidenavPO.goTo('ERWERBSPENSUM');
                BeschaeftigungspensumListPO.getGS1().should('exist');
                BeschaeftigungspensumListPO.getGS2().should('not.exist');

            })
    });

});

function createOnlineAntrag(
    periode: '2024/25',
    testFall: TestFall,
): void {
    const superAdmin = getUser('[1-Superadmin] E-BEGU Superuser');
    cy.login(superAdmin);
    cy.visit('/#/faelle');
    TestFaellePO.createOnlineTestfall({
        testFall,
        gemeinde: 'London',
        periode,
        betreuungsstatus: 'bestaetigt',
        besitzerin: '[5-GS] Heinrich Mueller',
    });
}
