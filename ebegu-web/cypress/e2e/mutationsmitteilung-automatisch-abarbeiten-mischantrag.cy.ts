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
        TestFaellePO.createNewTestFallIn('testfall-1', 'London', '2022/23', 'bestaetigt', '[5-GS] Heinrich Mueller');
        cy.login('[5-GS] Heinrich Mueller');
        cy.visit('/#/dossier/gesuchstellerDashboard');
        cy.getByData('2022/23', 'container.antrag-bearbeiten', 'navigation-button').click();
        cy.waitForRequest('GET', '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**', () => {
            SidenavPO.goTo('BETREUUNG');
        });
        cy.url()
            .then((url) => /betreuungen\/(.*)$/.exec(url)[1])
            .as('antragsId');
        AntragBetreuungPO.createNewBetreuung();
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
        cy.getByData('fall-toolbar', 'fall-nummer').then(a => a.text()).as('fallNummer');
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
            cy.intercept('**/gesuche').as('updateGesuch');
            cy.getByData('fall-creation-alternativDatum').type('01.07.2022');
            cy.getByData('container.navigation-save', 'navigation-button').click();
            cy.wait('@updateGesuch');

        }
        // !! AS GEMEINDE SB TS !!
        // Betreuung akzeptieren
        {
            cy.changeLogin(sachbearbeitungTSGemeinde);
            openGesuchInFreigabe();
            SidenavPO.goTo('BETREUUNG');
            cy.getByData('betreuung#2', 'betreuungs-status').should('have.text', 'Anmeldung ausgelöst');
            cy.getByData('betreuung#2').click();
            cy.intercept('**//betreuungen/schulamt/akzeptieren').as('anmeldungAkzeptieren');
            cy.getByData('container.akzeptieren', 'navigation-button').click();
            cy.getByData('container.confirm', 'navigation-button').click();
            cy.wait('@anmeldungAkzeptieren');
            cy.getByData('betreuung#2', 'betreuungs-status').should('have.text', 'Module akzeptiert');
        }

        // !! AS GEMEINDE SB BG !!
        // Gesuch verfügen
        {
            cy.changeLogin(sachbearbeitungBGGemeinde);
            openGesuchInFreigabe();
            cy.intercept('**/verfuegung/calculate/**').as('calculateVerfuegung');
            SidenavPO.goTo('VERFUEGEN');
            cy.wait('@calculateVerfuegung');
            cy.getByData('finSitStatus.radio-value.AKZEPTIERT').click();
            cy.intercept('PUT', '**/gesuche/status/*/GEPRUEFT').as('gesuchGeprueft');
            cy.getByData('container.geprueft', 'navigation-button').click();
            cy.getByData('container.confirm', 'navigation-button').click();
            cy.wait('@gesuchGeprueft');
            SidenavPO.getGesuchStatus().should('have.text', 'Geprüft');

            cy.getByData('container.verfuegen', 'navigation-button').click();
            cy.getByData('verfuegung#0-2', 'betreuungs-status').should('have.text', 'Module akzeptiert');
            cy.intercept('**/verfuegenStarten/*').as('verfuegenStarten');
            cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
                cy.getByData('container.confirm', 'navigation-button').click();
            });
            cy.wait('@verfuegenStarten');
            SidenavPO.getGesuchStatus().should('have.text', 'Verfügen');
            cy.getByData('verfuegung#0-2', 'betreuungs-status').should('have.text', 'Anmeldung übernommen');

            verfuegeBetreuung(0, 100);
            verfuegeBetreuung(1, 20);

            SidenavPO.getGesuchStatus().should('have.text', 'Verfügt');

            SidenavPO.goTo('VERFUEGEN');
            cy.getByData('verfuegung#0-2').click();
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
            cy.intercept('PUT', '**/mitteilungen/sendbetreuungsmitteilung').as('creatingMutationsmeldung');
            cy.get('[data-test="container.confirm"]').click();
            cy.wait('@creatingMutationsmeldung');
        }

        // MUTATIONSMITTEILUNG AUTOMATISCH ABARBEITEN

        {
            cy.changeLogin(sachbearbeitungBGGemeinde);
            cy.intercept('**/mitteilungen/search/*').as('searchMitteilungen');
            cy.visit('/#/posteingang');
            cy.wait('@searchMitteilungen');
            cy.get<string>('@fallNummer').then(el => {
                PosteingangPO.getFallFilter().find('input').type(parseInt(el).toString());
            });
            PosteingangPO.getEmpfaengerFilter().find('select').find('option').first().then(firstOption => {
                PosteingangPO.getEmpfaengerFilter().find('select').select(firstOption.text());
            });
            cy.wait('@searchMitteilungen');
            cy.intercept('applybetreuungsmitteilungsilently').as('betreuungsmitteilungenAutomatischBearbeiten');
            PosteingangPO.getMutationsmitteilungenAutomatischBearbeitenButton().click();
            cy.getByData('confirm.continue').click();
            cy.wait('@betreuungsmitteilungenAutomatischBearbeiten', {timeout: 40000});

            cy.intercept('**/benutzer/TsOrGemeinde/*').as('loadTsOrGemeindeUser');
            cy.getByData('automatisch-verfuegte-mitteilungen').find('li').should('have.length', 1);
            cy.getByData('automatisch-verfuegte-mitteilungen').find('li').find('a').invoke('removeAttr', 'target').click();

            cy.wait('@loadTsOrGemeindeUser');
            SidenavPO.getGesuchStatus().should('have.text', 'Verfügt');
            SidenavPO.goTo('VERFUEGEN');
            cy.getByData('verfuegung#0-2').click();
            VerfuegungPO.getVerfuegterTarif(0).should('have.text', '1.84');
        }

        // NEUE MUTATION ERÖFFNEN
        {
            SidenavPO.getGesuchStatus().should('have.text', 'Verfügt');
            cy.intercept('GET', '**/gemeinde/stammdaten/lite/**').as('mutationReady');
            cy.getByData('toolbar.antrag-mutieren').click();
            cy.wait('@mutationReady');

            cy.intercept('**/FINANZIELLE_SITUATION_TYP/gemeinde/*/gp/*').as('mutationErstellen');
            cy.getByData('fall-creation-eingangsdatum').find('input').type('01.12.2022');
            cy.getByData('container.navigation-save', 'navigation-button').contains('Erstellen').click();
            cy.wait('@mutationErstellen');
            SidenavPO.goTo('VERFUEGEN');
            cy.wait('@calculateVerfuegung');

            cy.getByData('verfuegung#0-2').click();
            VerfuegungPO.getVerfuegterTarif(0).should('have.text', '1.84');

        }

    })
});

function openGesuchInFreigabe() {
    cy.intercept('**/FREIGABE_QUITTUNG_EINLESEN_REQUIRED/gemeinde/*/gp/*').as('freigabeQuittungRequiredLoaded');
    cy.get('@antragsId').then(antragsId => cy.visit(`/#/gesuch/freigabe/${antragsId}`));
    cy.wait('@freigabeQuittungRequiredLoaded');
}

function openGesuchInBetreuungen() {
    cy.intercept('**/FINANZIELLE_SITUATION_TYP/gemeinde/*/gp/*').as('finSitTypLoaded');
    cy.get('@antragsId').then(antragsId => cy.visit(`/#/gesuch/betreuungen/${antragsId}`));
    cy.wait('@finSitTypLoaded');
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
