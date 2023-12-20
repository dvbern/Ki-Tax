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

import {AntragBetreuungPO, TestFaellePO} from '@dv-e2e/page-objects';
import {getUser} from '@dv-e2e/types';
import {PosteingangPO} from '../page-objects/antrag/posteingang.po';
import {SidenavPO} from '../page-objects/antrag/sidenav.po';
import {VerfuegungPO} from '../page-objects/antrag/verfuegung.po';

describe('Kibon - Testet das Feature der automatischen Abarbeitung von Mutationsmitteilungen, KIBON-3240', () => {

    const superAdmin = getUser('[1-Superadmin] E-BEGU Superuser');
    const sachbearbeitungBGGemeinde = getUser('[6-L-SB-BG] Jörg Keller');
    const sachbearbeitungTSGemeinde = getUser('[6-L-SB-TS] Julien Odermatt');
    const sachbearbeitungKita = getUser('[3-SB-Institution-Kita-Brünnen] Sophie Bergmann');

    const betreuungspensumInMutation = 90;

    beforeEach(() => {
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
        cy.login(superAdmin);
        cy.visit('/#/faelle');
    });

    it('should create and verfuegen mischantrag', () => {
        TestFaellePO.createNewTestFallIn({
            testFall: 'testfall-1',
            gemeinde: 'London',
            periode: '2022/23',
            betreuungsstatus: 'bestaetigt',
            besitzerin: '[5-GS] Heinrich Mueller',
        });
        cy.login('[5-GS] Heinrich Mueller');
        cy.visit('/#/dossier/gesuchstellerDashboard');
        cy.getByData('container.periode.2022/23', 'container.antrag-bearbeiten', 'navigation-button').click();
        cy.waitForRequest('GET', '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**', () => {
            SidenavPO.goTo('BETREUUNG');
        });
        cy.getByData('antrags-daten').then(el$ => el$.data('antrags-id')).as('antragsId');
        AntragBetreuungPO.createNewBetreuung();
        AntragBetreuungPO.selectTagesschulBetreuung();
        AntragBetreuungPO.fillTagesschulBetreuungsForm('withValid', 'London');
        AntragBetreuungPO.saveBetreuung();

        SidenavPO.goTo('DOKUMENTE');

        cy.getByData('container.navigation-save', 'navigation-button').click();
        cy.getByData('container.freigeben', 'navigation-button').click();
        // TODO: extract duplication once KIBON-3208 is merged
        cy.getDownloadUrl(() => {
            cy.waitForRequest('GET', '**/dossier/fall/**', () => {
                cy.getByData('container.confirm', 'navigation-button').click();
            });
        }).then(downloadUrl => {
            return cy.request(downloadUrl)
                .then(response => expect(response.headers['content-disposition']).to.match(/Freigabequittung_.*\.pdf/));
        });
        cy.getByData('fall-toolbar', 'fallnummer').then(a => a.text()).as('fallNummer');
        // !! AS SUPERUSER !!
        // FREIGABEQUITTUNG SCANNEN SIMULIEREN
        {
            cy.changeLogin(superAdmin);
            openGesuchInFreigabe();
            cy.getByData('container.antrag-freigeben-simulieren', 'navigation-button').click();
            SidenavPO.getGesuchStatus().should('have.text', 'Freigegeben');
        }

        // !! AS GEMEINDE SB BG !!
        // Alternatives Datum setzen
        {
            cy.changeLogin(sachbearbeitungBGGemeinde);
            openGesuchInFreigabe();
            SidenavPO.goTo('GESUCH_ERSTELLEN');
            cy.waitForRequest('PUT', '**/gesuche', () => {
                cy.getByData('fall-creation-alternativDatum').type('01.07.2022');
                cy.getByData('container.navigation-save', 'navigation-button').click();
            });

        }
        // !! AS GEMEINDE SB TS !!
        // Betreuung akzeptieren
        {
            cy.changeLogin(sachbearbeitungTSGemeinde);
            openGesuchInFreigabe();
            SidenavPO.goTo('BETREUUNG');
            cy.getByData('betreuung#2', 'betreuungs-status').should('have.text', 'Anmeldung ausgelöst');
            cy.getByData('betreuung#2').click();
            cy.waitForRequest('PUT', '**/betreuungen/schulamt/akzeptieren', () => {
                cy.getByData('container.akzeptieren', 'navigation-button').click();
                cy.getByData('container.confirm', 'navigation-button').click();
            });
            cy.getByData('betreuung#2', 'betreuungs-status').should('have.text', 'Module akzeptiert');
        }

        // !! AS GEMEINDE SB BG !!
        // Gesuch verfügen
        {
            cy.changeLogin(sachbearbeitungBGGemeinde);
            openGesuchInFreigabe();
            cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
                SidenavPO.goTo('VERFUEGEN');
            });
            cy.getByData('finSitStatus.radio-value.AKZEPTIERT').click();
            cy.waitForRequest('PUT', '**/gesuche/status/*/GEPRUEFT', () => {
                cy.getByData('container.geprueft', 'navigation-button').click();
                cy.getByData('container.confirm', 'navigation-button').click();
            });
            SidenavPO.getGesuchStatus().should('have.text', 'Geprüft');

            cy.getByData('container.verfuegen', 'navigation-button').click();
            cy.getByData('verfuegung#0-2', 'betreuungs-status').should('have.text', 'Module akzeptiert');
            cy.waitForRequest('POST', '**/verfuegenStarten/*', () => {
                cy.getByData('container.confirm', 'navigation-button').click();
            });
            SidenavPO.getGesuchStatus().should('have.text', 'Verfügen');
            cy.getByData('verfuegung#0-2', 'betreuungs-status').should('have.text', 'Anmeldung übernommen');

            verfuegeBetreuung(0, 100);
            verfuegeBetreuung(1, 20);

            SidenavPO.getGesuchStatus().should('have.text', 'Verfügt');

            SidenavPO.goTo('VERFUEGEN');
            cy.getByData('verfuegung#0-2').click();
            VerfuegungPO.getAllTarife().should('have.length', 1);
            VerfuegungPO.getVerfuegterTarif(0).should('have.text', '1.84');
        }

        // !!! AS SB BG GEMEINDE !!!
        // CREATE MUTATIONSMITTEILUNG

        {
            cy.changeLogin(sachbearbeitungKita);
            openGesuchInBetreuungen();
            cy.getByData('betreuung#0').click();
            cy.getByData('mutationsmeldung-erstellen').click();
            cy.getByData('betreuungspensum-0').clear().type(betreuungspensumInMutation.toString());
            cy.getByData('mutationsmeldung-senden').click();
            cy.waitForRequest('PUT', '**/mitteilungen/sendbetreuungsmitteilung', () => {
                cy.get('[data-test="container.confirm"]').click();
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

            cy.intercept('applybetreuungsmitteilungsilently').as('betreuungsmitteilungenAutomatischBearbeiten');
            PosteingangPO.getMutationsmitteilungenAutomatischBearbeitenButton().click();
            cy.getByData('confirm.continue').click();
            cy.wait('@betreuungsmitteilungenAutomatischBearbeiten', {timeout: 40000});

            cy.waitForRequest('GET', '**/benutzer/TsOrGemeinde/*', () => {
                cy.get<string>('@fallNummer').then(fallNummer => {
                    cy.getByData('container.automatisch-verfuegte-mitteilungen', `verfuegt#${parseInt(fallNummer, 10)}`)
                        .invoke('removeAttr', 'target').click();
                });
            });

            SidenavPO.getGesuchStatus().should('have.text', 'Verfügt');
            SidenavPO.goTo('VERFUEGEN');
            cy.getByData('verfuegung#0-2', 'betreuungs-status').should('have.text', 'Anmeldung übernommen');
            cy.getByData('verfuegung#0-2').click();
            VerfuegungPO.getAllTarife().should('have.length', 1);
            VerfuegungPO.getVerfuegterTarif(0).should('have.text', '1.84');
        }

        // NEUE MUTATION ERÖFFNEN
        {
            SidenavPO.getGesuchStatus().should('have.text', 'Verfügt');
            cy.waitForRequest('GET', '**/gemeinde/stammdaten/lite/**', () => {
                cy.getByData('toolbar.antrag-mutieren').click();
            });

            cy.waitForRequest('GET', '**/FINANZIELLE_SITUATION_TYP/gemeinde/*/gp/*', () => {
                cy.getByData('fall-creation-eingangsdatum').find('input').type('01.12.2022');
                cy.getByData('container.navigation-save', 'navigation-button').contains('Erstellen').click();
            });
            cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
                SidenavPO.goTo('VERFUEGEN');
            });

            cy.getByData('verfuegung#0-2', 'betreuungs-status').should('have.text', 'Module akzeptiert');
            cy.getByData('verfuegung#0-2').click();
            VerfuegungPO.getAllTarife().should('have.length', 1);
            VerfuegungPO.getVerfuegterTarif(0).should('have.text', '1.84');
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
    cy.getByData(`verfuegung#0-${betreuungNumber}`).click();
    cy.getByData('container.zeitabschnitt#0', 'anspruchberechtigtesPensum')
        .should('include.text', `${expectedAnspruchberechtigtesPensum}%`);

    cy.getByData('verfuegungs-bemerkungen-kontrolliert').click();
    cy.intercept('**/verfuegung/verfuegen/*/*/false/false').as('verfuegen');
    cy.getByData('container.verfuegen', 'navigation-button').click();
    cy.getByData('container.confirm', 'navigation-button').click();
    cy.wait('@verfuegen', {timeout: 40000});
    SidenavPO.goTo('VERFUEGEN');
}
