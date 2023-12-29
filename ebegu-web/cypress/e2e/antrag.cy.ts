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
    AntragPapierPO,
    AntragFamSitPO,
    AntragKindPO,
    AntragBetreuungPO,
    AntragBeschaeftigungspensumPO, NavigationPO,
} from '@dv-e2e/page-objects';
import { FixtureFinSit } from '@dv-e2e/fixtures';
import { getUser } from '@dv-e2e/types';
import {SidenavPO} from '../page-objects/antrag/sidenav.po';

const createNewKindWithAllSettings = () => {
    AntragKindPO.createNewKind();
    AntragKindPO.fillKindForm('withValidBoy');
    AntragKindPO.fillPflegekind();

    cy.getByData('show-fachstelle').click();
    AntragKindPO.fillFachstelle();

    cy.getByData('show-asylwesen').click();
    cy.getByData('zemis-nummer').type('12345672.0');

    cy.getByData('show-ausserordentlicher-anspruch').click();
    AntragKindPO.fillAusserordentlicherAnspruch();

    cy.waitForRequest('PUT', '**/kinder/**', () => {
        NavigationPO.saveAndGoNext();
    });
};

const createNewBetreuungWithAllSettings = () => {
    AntragBetreuungPO.createNewBetreuung();
    AntragBetreuungPO.fillKitaBetreuungsForm('withValid', 'London');
    AntragBetreuungPO.fillKeinePlatzierung();
    AntragBetreuungPO.fillErweiterteBeduerfnisse();
    AntragBetreuungPO.platzBestaetigungAnfordern();
};

describe('Kibon - generate Testfälle [Gemeinde Sachbearbeiter]', () => {
    const userSB = getUser('[6-L-SB-Gemeinde] Stefan Weibel');
    const userKita = getUser('[3-SB-Institution-Kita-Brünnen] Sophie Bergmann');

    beforeEach(() => {
        cy.intercept({ resourceType: 'xhr' }, { log: false }); // don't log XHRs
        cy.login(userSB);
        cy.visit('/#/faelle');
    });

    it('should correctly create a new Papier Antrag', () => {
        // INIT
        {
            AntragPapierPO.createPapierGesuch('withValid');
            cy.getByData('antrags-daten').then((el$) => el$.data('antrags-id')).as('antragsId');
        }

        // FAMILIENSITUATION
        {
            AntragFamSitPO.fillFamiliensituationForm('withValid');
            NavigationPO.saveAndGoNext();
        }

        // KINDER
        {
            createNewKindWithAllSettings();
            cy.getByData('page-title').should('include.text', 'Kinder');

            cy.intercept('POST', '**/wizard-steps').as('goingToBetreuung');
            NavigationPO.saveAndGoNext();
            cy.wait('@goingToBetreuung');
        }

        // BETREUUNG
        {
            createNewBetreuungWithAllSettings();
            cy.getByData('page-title').should('include.text', 'Betreuung');

            cy.intercept('GET', '**/erwerbspensen/required/**').as('goingToBeschaeftigungspensum');
            NavigationPO.saveAndGoNext();
            cy.wait('@goingToBeschaeftigungspensum');
        }

        // BESCHAEFTIGUNGSPENSUM
        {
            AntragBeschaeftigungspensumPO.createBeschaeftigungspensum('GS1', 'withValid');
            AntragBeschaeftigungspensumPO.createBeschaeftigungspensum('GS2', 'withValid');

            NavigationPO.saveAndGoNext();
        }

        // FINANZIELLE VERHAELTNISSE
        {
            // Config
            {
                cy.wait(2000);
                cy.getByData('sozialhilfeBezueger.radio-value.nein').click();
                cy.getByData('iban').type('CH3908704016075473007');
                cy.getByData('kontoinhaber').type('vorname-test1 nachname-test-1');

                cy.intercept('POST', '**/finanzielleSituation/calculateTemp').as('goingToFinSitGS1');
                NavigationPO.saveAndGoNext();
                cy.wait('@goingToFinSitGS1');
            }

            // Finanzielle Situation - GS 1
            {
                // TODO: update EinkommensverschlechterungPO and update it to also support Finanzielle Situation
                FixtureFinSit.withValid(({ GS1 }) => {
                    cy.wait(2000);
                    cy.getByData('nettolohn').find('input').type(GS1.nettolohn);
                    cy.getByData('familienzulage').find('input').type(GS1.familienzulage);
                    cy.getByData('ersatzeinkommen').find('input').type(GS1.ersatzeinkommen);
                    cy.getByData('erhaltene-alimente').find('input').type(GS1.erhalteneAlimente);
                    cy.getByData('brutto-ertraege-vermoegen').find('input').type(GS1.bruttoErtraegeVermoegen);
                    cy.getByData('nettoertraege_erbengemeinschaften').find('input').type(GS1.nettoertraegeErbengemeinschaften);
                    cy.getByData('einkommenInVereinfachtemVerfahrenAbgerechnet1.radio-value.nein').click();
                    cy.getByData('geleistete-alimente').find('input').type(GS1.geleisteteAlimente);
                    cy.getByData('abzug-schuldzinsen').find('input').type(GS1.abzugSchuldzinsen);
                    cy.getByData('gewinnungskosten').find('input').type(GS1.gewinnungskosten);
                });

                cy.intercept('POST', '**/finanzielleSituation/calculateTemp').as('goingToFinSitGS2');
                NavigationPO.saveAndGoNext();
                cy.wait('@goingToFinSitGS2');
            }

            // Finanzielle Situation - GS 2
            {
                // TODO: update EinkommensverschlechterungPO and update it to also support Finanzielle Situation
                FixtureFinSit.withValid(({ GS2 }) => {
                    cy.wait(2000);
                    cy.getByData('nettolohn').find('input').type(GS2.nettolohn);
                    cy.getByData('familienzulage').find('input').type(GS2.familienzulage);
                    cy.getByData('ersatzeinkommen').find('input').type(GS2.ersatzeinkommen);
                    cy.getByData('erhaltene-alimente').find('input').type(GS2.erhalteneAlimente);
                    cy.getByData('brutto-ertraege-vermoegen').find('input').type(GS2.bruttoErtraegeVermoegen);
                    cy.getByData('nettoertraege_erbengemeinschaften').find('input').type(GS2.nettoertraegeErbengemeinschaften);
                    cy.getByData('einkommenInVereinfachtemVerfahrenAbgerechnet1.radio-value.nein').click();
                    cy.getByData('geleistete-alimente').find('input').type(GS2.geleisteteAlimente);
                    cy.getByData('abzug-schuldzinsen').find('input').type(GS2.abzugSchuldzinsen);
                    cy.getByData('gewinnungskosten').find('input').type(GS2.gewinnungskosten);
                });

                cy.intercept('POST', '**/finanzielleSituation/calculateTemp').as('goingToResultate');
                NavigationPO.saveAndGoNext();
                cy.wait('@goingToResultate');
            }

            // Resultate
            {
                // TODO: update EinkommensverschlechterungPO and update it to also support Finanzielle Situation
                FixtureFinSit.withValid(({ Resultate }) => {
                    cy.getByData('einkommenBeiderGesuchsteller')
                        .find('input')
                        .should('have.value', Resultate.einkommenBeiderGesuchsteller);
                    cy.getByData('bruttovermoegen1').find('input').type(Resultate.bruttovermoegen1);
                    cy.getByData('bruttovermoegen2').find('input').type(Resultate.bruttovermoegen2);
                    cy.getByData('schulden1').find('input').type(Resultate.schulden1);
                    cy.getByData('schulden2').find('input').type(Resultate.schulden2);
                    cy.getByData('nettovermoegenFuenfProzent')
                        .find('input')
                        .should('have.value', Resultate.nettovermoegenFuenfProzent);
                    cy.getByData('anrechenbaresEinkommen').find('input').should('have.value', Resultate.anrechenbaresEinkommen);
                    cy.getByData('abzuegeBeiderGesuchsteller')
                        .find('input')
                        .should('have.value', Resultate.abzuegeBeiderGesuchsteller);
                    cy.getByData('massgebendesEinkVorAbzFamGr')
                        .find('input')
                        .should('have.value', Resultate.massgebendesEinkVorAbzFamGr);
                });
            }

            cy.waitForRequest('GET', '**/einkommensverschlechterung/minimalesMassgebendesEinkommen/**', () => {
                NavigationPO.saveAndGoNext();
            });
        }

        // EINKOMMENSVERSCHLECHTERUNG
        {
            cy.waitForRequest('GET', '**/dokumente/**', () => {
                NavigationPO.saveAndGoNext();
            });
        }

        // DOKUMENTE
        {
            // Test upload file
            cy.fixture('documents/small.png').as('smallPng');

            // Upload the file on every <input type=file>, Angular JS file upload makes specific upload difficult:
            // https://github.com/abramenal/cypress-file-upload/tree/main/recipes/angularjs-ng-file-upload
            // https://github.com/danialfarid/ng-file-upload/issues/1140
            // https://github.com/danialfarid/ng-file-upload/issues/1167
            cy.get('input[type="file"][tabindex=0]').each(($el, index) => {
                const upload = `fileUpload#${index}`;
                cy.intercept('POST', '**/upload').as(upload);
                cy.wrap($el).selectFile({ contents: '@smallPng', fileName: `small-${index}.png` }, { force: true });
                return cy.wait(`@${upload}`);
            });

            // TODO: Is flaky, some download requests seem to cancelled
            // cy.get('[data-test^="download-file"').each(($el, index) => {
            //     cy.wrap($el).click();
            // });
            //
            // cy.get('[data-test^="download-file"').each(($el, index) => {
            //     const downloadsFolder = Cypress.config('downloadsFolder');
            //     const fileName = `small-${index}.png`;
            //     const fullPath = path.join(downloadsFolder, fileName);
            //     cy.readFile(fullPath).should('exist');
            //     cy.log('FULL PATH?', fullPath);
            //     cy.task('deleteDownload', { dirPath: downloadsFolder, fileName }, { custom: true });
            //     return cy.readFile(fullPath).should('not.exist');
            // });

            cy.waitForRequest('POST', '**/wizard-steps', () => {
                NavigationPO.saveAndGoNext();
            });
        }

        // PLATZBESTAETIGUNG mit Kita SB
        // !!!!!! - New User - !!!!!!
        {
            cy.changeLogin(userKita);

            cy.get('@antragsId').then((antragsId) => cy.visit(`/#/gesuch/familiensituation/${antragsId}`));
            cy.waitForRequest('GET', '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**', () => {
                SidenavPO.goTo('BETREUUNG');
            });

            cy.getByData('container.betreuung#0').click();

            cy.wait(2000);
            cy.getByData('betreuungspensum-0').type('25');
            cy.getByData('monatliche-betreuungskosten#0').type('1000');
            cy.getByData('betreuung-datum-ab#0').find('input').type('01.01.2023');
            cy.getByData('betreuung-datum-bis#0').find('input').type('31.12.2023');
            cy.getByData('korrekte-kosten-bestaetigung').click();

            cy.getByData('container.platz-bestaetigen', 'navigation-button').click();

            cy.waitForRequest('GET', '**/search/pendenzenBetreuungen', () => {
                cy.getByData('container.confirm', 'navigation-button').click();
            });
        }
        cy.changeLogin(userSB);
        // !!!!!! - changed back to previous user - !!!!!!

        // VERFUEGUNG
        {
            cy.get('@antragsId').then((antragsId) => cy.visit(`/#/gesuch/verfuegen/${antragsId}`));

            cy.getByData('verfuegung#0-0').click();
            cy.getByData('container.zeitabschnitt#5', 'betreuungspensumProzent').should('include.text', '25%');
            cy.getByData('container.zeitabschnitt#6', 'betreuungspensumProzent').should('include.text', '25%');
            cy.getByData('container.zeitabschnitt#7', 'betreuungspensumProzent').should('include.text', '25%');
            cy.getByData('container.zeitabschnitt#8', 'betreuungspensumProzent').should('include.text', '25%');
            cy.getByData('container.zeitabschnitt#9', 'betreuungspensumProzent').should('include.text', '25%');
            cy.getByData('container.zeitabschnitt#10', 'betreuungspensumProzent').should('include.text', '25%');
            cy.getByData('container.zeitabschnitt#11', 'betreuungspensumProzent').should('include.text', '25%');
        }
    });
});
