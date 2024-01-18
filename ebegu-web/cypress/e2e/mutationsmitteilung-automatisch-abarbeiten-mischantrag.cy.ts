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
    AntragBetreuungPO, AntragCreationPO, ConfirmDialogPO, DossierToolbarPO,
    FallToolbarPO,
    FreigabePO,
    GesuchstellendeDashboardPO, MitteilungenResultDialogPO,
    NavigationPO,
    TestFaellePO, VerfuegenPO,
} from '@dv-e2e/page-objects';
import {getUser} from '@dv-e2e/types';
import {PosteingangPO} from '../page-objects/antrag/posteingang.po';
import {SidenavPO} from '../page-objects/antrag/sidenav.po';
import {VerfuegungPO} from '../page-objects/antrag/verfuegung.po';

describe('Kibon - Testet das Feature der automatischen Abarbeitung von Mutationsmitteilungen, KIBON-3240', () => {

    const superAdmin = getUser('[1-Superadmin] E-BEGU Superuser');
    const sachbearbeitungBGGemeinde = getUser('[6-P-SB-BG] Jörg Becker');
    const sachbearbeitungTSGemeinde = getUser('[6-P-SB-TS] Julien Schuler');
    const sachbearbeitungKita = getUser('[3-SB-Institution-Kita-Brünnen] Sophie Bergmann');

    const betreuungspensumInMutation = 90;

    beforeEach(() => {
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
        cy.login(superAdmin);
        cy.visit('/#/faelle');
    });

    it('should create and verfuegen mischantrag', () => {
        TestFaellePO.createOnlineTestfall({
            testFall: 'testfall-1',
            gemeinde: 'Paris',
            periode: '2022/23',
            betreuungsstatus: 'bestaetigt',
            besitzerin: '[5-GS] Heinrich Mueller',
        });
        cy.login('[5-GS] Heinrich Mueller');
        cy.visit('/#/dossier/gesuchstellerDashboard');
        GesuchstellendeDashboardPO.getAntragBearbeitenButton('2022/23').click();
        cy.waitForRequest('GET', '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**', () => {
            SidenavPO.goTo('BETREUUNG');
        });
        SidenavPO.getGesuchsDaten().then(el$ => el$.data('antrags-id')).as('antragsId');
        AntragBetreuungPO.createNewBetreuung();
        AntragBetreuungPO.selectTagesschulBetreuung();
        AntragBetreuungPO.fillTagesschulBetreuungsForm('withValid', 'Paris');
        AntragBetreuungPO.saveBetreuung();

        SidenavPO.goTo('DOKUMENTE');

        NavigationPO.saveAndGoNext();
        FreigabePO.getFreigebenButton().click();
        // TODO: extract duplication once KIBON-3208 is merged
        cy.getDownloadUrl(() => {
            cy.waitForRequest('GET', '**/dossier/fall/**', () => {
                ConfirmDialogPO.getDvLoadingConfirmButton().click();
            });
        }).then(downloadUrl => {
            return cy.request(downloadUrl)
                .then(response => expect(response.headers['content-disposition']).to.match(/Freigabequittung_.*\.pdf/));
        });
        FallToolbarPO.getFallnummer().then(a => a.text()).as('fallNummer');
        // !! AS SUPERUSER !!
        // FREIGABEQUITTUNG SCANNEN SIMULIEREN
        {
            cy.changeLogin(superAdmin);
            openGesuchInFreigabe();
            cy.waitForRequest('POST', '**/freigeben/*/JA/*/SCH/*', () => {
                FreigabePO.getFreigabequittungEinscannenSimulierenButton().click();
            });
            SidenavPO.getGesuchStatus().should('have.text', 'Freigegeben');
        }

        // !! AS GEMEINDE SB BG !!
        // Alternatives Datum setzen
        {
            cy.changeLogin(sachbearbeitungBGGemeinde);
            openGesuchInFreigabe();
            SidenavPO.goTo('GESUCH_ERSTELLEN');
            cy.waitForRequest('PUT', '**/gesuche', () => {
                AntragCreationPO.getAlternativdatum().type('01.07.2022');
                NavigationPO.saveAndGoNext();
            });

        }
        // !! AS GEMEINDE SB TS !!
        // Betreuung akzeptieren
        {
            cy.changeLogin(sachbearbeitungTSGemeinde);
            openGesuchInFreigabe();
            SidenavPO.goTo('BETREUUNG');
            AntragBetreuungPO.getBetreuungsstatus(0,2).should('have.text', 'Anmeldung ausgelöst');
            AntragBetreuungPO.getBetreuung(0,2).click();
            cy.waitForRequest('GET', '**/dossier/id/**', () => {
                AntragBetreuungPO.getPlatzAkzeptierenButton().click();
                ConfirmDialogPO.getDvLoadingConfirmButton().click();
            });
            AntragBetreuungPO.getBetreuungsstatus(0,2).should('have.text', 'Module akzeptiert');
        }

        // !! AS GEMEINDE SB BG !!
        // Gesuch verfügen
        {
            cy.changeLogin(sachbearbeitungBGGemeinde);
            openGesuchInFreigabe();
            cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
                SidenavPO.goTo('VERFUEGEN');
            });
            VerfuegenPO.getFinSitAkzeptiert('AKZEPTIERT').click();
            VerfuegenPO.pruefeGesuch();
            SidenavPO.getGesuchStatus().should('have.text', 'Geprüft');

            VerfuegenPO.getVerfuegenStartenButton().click();
            VerfuegenPO.getBetreuungsstatus(0,2).should('have.text', 'Module akzeptiert');
            cy.waitForRequest('POST', '**/verfuegenStarten/*', () => {
                ConfirmDialogPO.getDvLoadingConfirmButton().click();
            });
            SidenavPO.getGesuchStatus().should('have.text', 'Verfügen');
            VerfuegenPO.getBetreuungsstatus(0,2).should('have.text', 'Anmeldung übernommen');

            verfuegeBetreuung(0, 100);
            verfuegeBetreuung(1, 20);

            SidenavPO.getGesuchStatus().should('have.text', 'Verfügt');

            SidenavPO.goTo('VERFUEGEN');
            VerfuegenPO.getBetreuungsstatus(0,2).click();
            VerfuegungPO.getAllTarife().should('have.length', 1);
            VerfuegungPO.getVerfuegterTarif(0).should('have.text', '1.87');
        }

        // !!! AS SB BG GEMEINDE !!!
        // CREATE MUTATIONSMITTEILUNG

        {
            cy.changeLogin(sachbearbeitungKita);
            openGesuchInBetreuungen();
            AntragBetreuungPO.getBetreuung(0,0).click();
            AntragBetreuungPO.getMutationsmeldungErstellenButton().click();
            AntragBetreuungPO.getBetreuungspensum(0).clear().type(betreuungspensumInMutation.toString());
            AntragBetreuungPO.getMutationsmeldungSendenButton().click();
            cy.waitForRequest('PUT', '**/mitteilungen/sendbetreuungsmitteilung', () => {
                ConfirmDialogPO.getDvLoadingConfirmButton().click();
            });
        }

        // MUTATIONSMITTEILUNG AUTOMATISCH ABARBEITEN

        {
            cy.changeLogin(sachbearbeitungBGGemeinde);
            cy.waitForRequest('POST', '**/mitteilungen/search/*', () => {
                cy.visit('/#/posteingang');
            });
            cy.waitForRequest('POST', '**/mitteilungen/search/*', () => {
                PosteingangPO.getEmpfaengerFilter().find('select').find('option').first().then(firstOption => {
                    PosteingangPO.getEmpfaengerFilter().find('select').select(firstOption.text());
                });
            });

            cy.waitForRequest('POST', 'applybetreuungsmitteilungsilently', () => {
                PosteingangPO.getMutationsmitteilungenAutomatischBearbeitenButton().click();
                ConfirmDialogPO.getSimpleConfirmButton().click();
            }, {waitOptions: {timeout: 40000}});

            cy.waitForRequest('GET', '**/benutzer/TsOrGemeinde/*', () => {
                cy.get<string>('@fallNummer').then(fallNummer => {
                    MitteilungenResultDialogPO.getAutomatischVerfuegteMitteilungForFall(fallNummer)
                        .invoke('removeAttr', 'target')
                        .click();
                });
            });

            SidenavPO.getGesuchStatus().should('have.text', 'Verfügt');
            SidenavPO.goTo('VERFUEGEN');
            VerfuegenPO.getBetreuungsstatus(0, 2).should('have.text', 'Anmeldung übernommen');
            VerfuegenPO.getVerfuegung(0, 2).click();
            VerfuegungPO.getAllTarife().should('have.length', 1);
            VerfuegungPO.getVerfuegterTarif(0).should('have.text', '1.87');
        }

        // NEUE MUTATION ERÖFFNEN
        {
            SidenavPO.getGesuchStatus().should('have.text', 'Verfügt');
            cy.waitForRequest('GET', '**/gemeinde/stammdaten/lite/**', () => {
                DossierToolbarPO.getAntragMutieren().click();
            });

            cy.waitForRequest('GET', '**/FINANZIELLE_SITUATION_TYP/gemeinde/*/gp/*', () => {
                AntragCreationPO.getEingangsdatum().find('input').type('01.12.2022');
                NavigationPO.getSaveAndNextButton().contains('Erstellen').click();
            });
            cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
                SidenavPO.goTo('VERFUEGEN');
            });

            VerfuegenPO.getBetreuungsstatus(0, 2).should('have.text', 'Module akzeptiert');
            VerfuegenPO.getVerfuegung(0 ,2).click();
            VerfuegungPO.getAllTarife().should('have.length', 1);
            VerfuegungPO.getVerfuegterTarif(0).should('have.text', '1.87');
        }

    })
});

function openGesuchInFreigabe() {
    cy.waitForRequest('GET', '**/FREIGABE_QUITTUNG_EINLESEN_REQUIRED/gemeinde/*/gp/*', () => {
        cy.get('@antragsId').then(antragsId => cy.visit(`/#/gesuch/freigabe/${antragsId}`));
    });
}

function openGesuchInBetreuungen() {
    cy.waitForRequest('GET', '**/FINANZIELLE_SITUATION_TYP/gemeinde/*/gp/*', () => {
        cy.get('@antragsId').then(antragsId => cy.visit(`/#/gesuch/betreuungen/${antragsId}`));
    });
}

function verfuegeBetreuung(betreuungNumber: number, expectedAnspruchberechtigtesPensum: number): void {
    VerfuegenPO.getVerfuegung(0, betreuungNumber).click();
    VerfuegungPO.getAnspruchberechtigtesBetreuungspensum(0)
        .should('include.text', `${expectedAnspruchberechtigtesPensum}%`);

    VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();
    cy.waitForRequest('PUT', '**/verfuegung/verfuegen/*/*/false/false', () => {
        VerfuegungPO.getVerfuegenButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    }, {waitOptions: {timeout: 40000}});
    SidenavPO.goTo('VERFUEGEN');
}
