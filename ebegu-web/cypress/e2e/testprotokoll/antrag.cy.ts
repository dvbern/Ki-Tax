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
    AntragPapierPO,
    AntragFamSitPO,
    AntragKindPO,
    AntragBetreuungPO,
    AntragBeschaeftigungspensumPO,
    FinanzielleSituationPO,
    FinanzielleSituationStartPO,
    FinanzielleSituationResultatePO,
    NavigationPO,
    SidenavPO,
    VerfuegenPO,
    ConfirmDialogPO
} from '@dv-e2e/page-objects';
import {FixtureFinSit} from '@dv-e2e/fixtures';
import {GemeindeTestFall, getUser} from '@dv-e2e/types';
import {GesuchstellendePO} from '../../page-objects/antrag/gesuchstellende.po';
import {VerfuegungPO} from '../../page-objects/antrag/verfuegung.po';

const gemeinde: GemeindeTestFall = 'London';

const createNewKindWithAllSettings = () => {
    AntragKindPO.createNewKind();
    AntragKindPO.fillKindForm('withValidBoy');
    AntragKindPO.fillPflegekind();

    cy.getByData('show-fachstelle').click();
    cy.wait(1500);
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
    AntragBetreuungPO.fillKitaBetreuungsForm('withValid', gemeinde);
    AntragBetreuungPO.fillKeinePlatzierung();
    AntragBetreuungPO.fillErweiterteBeduerfnisse();
    AntragBetreuungPO.platzBestaetigungAnfordern();
};

describe('Kibon - generate Testfälle [Gemeinde Sachbearbeiter]', () => {
    const userSB = getUser('[6-L-SB-Gemeinde] Stefan Weibel');
    const userKita = getUser('[3-SB-Institution-Kita-Brünnen] Sophie Bergmann');

    it('should correctly create a new Papier Antrag', () => {
        // cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
        cy.login(userSB);
        cy.visit('/');

        // INIT

        AntragPapierPO.createPapierGesuch('withValid');

        // FAMILIENSITUATION

        AntragFamSitPO.fillFamiliensituationForm('withValid');
        cy.wait(1500);
        cy.waitForRequest('POST', '**/wizard-steps', () => {
            NavigationPO.saveAndGoNext();
        });
        GesuchstellendePO.fillVerheiratet('withValid');
        cy.waitForRequest('POST', '**/wizard-steps', () => {
            NavigationPO.saveAndGoNext();
        });

        // KINDER

        createNewKindWithAllSettings();
        AntragKindPO.getPageTitle().should('include.text', 'Kinder');

        cy.waitForRequest('POST', '**/wizard-steps', () => {
            NavigationPO.saveAndGoNext();
        });

        // BETREUUNG

        createNewBetreuungWithAllSettings();
        AntragBetreuungPO.getPageTitle().should('include.text', 'Betreuung');

        cy.waitForRequest('GET', '**/erwerbspensen/required/**', () => {
            NavigationPO.saveAndGoNext();
        });

        // BESCHAEFTIGUNGSPENSUM

        AntragBeschaeftigungspensumPO.createBeschaeftigungspensum(
            'GS1',
            'withValid'
        );
        AntragBeschaeftigungspensumPO.createBeschaeftigungspensum(
            'GS2',
            'withValid'
        );

        NavigationPO.saveAndGoNext();

        // FINANZIELLE VERHAELTNISSE

        // Config

        FinanzielleSituationStartPO.fillFinanzielleSituationStartForm(
            'withValid'
        );
        FinanzielleSituationStartPO.saveForm();

        // Finanzielle Situation - GS 1

        // TODO: update EinkommensverschlechterungPO and update it to also support Finanzielle Situation
        FinanzielleSituationPO.fillFinanzielleSituationForm('withValid', 'GS1');
        FinanzielleSituationPO.saveFormAndGoNext();

        // Finanzielle Situation - GS 2

        // TODO: update EinkommensverschlechterungPO and update it to also support Finanzielle Situation
        FinanzielleSituationPO.fillFinanzielleSituationForm('withValid', 'GS2');
        FinanzielleSituationPO.saveFormAndGoNext();

        // Resultate

        // TODO: update EinkommensverschlechterungPO and update it to also support Finanzielle Situation
        FixtureFinSit.withValid(({Resultate}) => {
            FinanzielleSituationResultatePO.getEinkommenBeiderGesuchsteller()
                .find('input')
                .should('have.value', Resultate.einkommenBeiderGesuchsteller);
            FinanzielleSituationResultatePO.getBruttovermoegenGS1()
                .find('input')
                .type(Resultate.bruttovermoegen1);
            FinanzielleSituationResultatePO.getBruttovermoegenGS2()
                .find('input')
                .type(Resultate.bruttovermoegen2);
            FinanzielleSituationResultatePO.getSchuldenGS1()
                .find('input')
                .type(Resultate.schulden1);
            FinanzielleSituationResultatePO.getSchuldenGS2()
                .find('input')
                .type(Resultate.schulden2);
            FinanzielleSituationResultatePO.getNettovermoegenFuenfProzent()
                .find('input')
                .should('have.value', Resultate.nettovermoegenFuenfProzent);
            FinanzielleSituationResultatePO.getAnrechenbaresEinkommen()
                .find('input')
                .should('have.value', Resultate.anrechenbaresEinkommen);
            FinanzielleSituationResultatePO.getAbzuegeBeiderGesuchstellenden()
                .find('input')
                .should('have.value', Resultate.abzuegeBeiderGesuchsteller);
            FinanzielleSituationResultatePO.getMassgebendesEinkommenVorAbzugFamGroesse()
                .find('input')
                .should('have.value', Resultate.massgebendesEinkVorAbzFamGr);
        });

        cy.waitForRequest(
            'GET',
            '**/einkommensverschlechterung/minimalesMassgebendesEinkommen/**',
            () => {
                NavigationPO.saveAndGoNext();
            }
        );

        // EINKOMMENSVERSCHLECHTERUNG

        cy.waitForRequest('GET', '**/dokumente/**', () => {
            NavigationPO.saveAndGoNext();
        });

        // DOKUMENTE

        // Test upload file
        cy.fixture('documents/small.png').as('smallPng');

        // Upload the file on every <input type=file>, Angular JS file upload makes specific upload difficult:
        // https://github.com/abramenal/cypress-file-upload/tree/main/recipes/angularjs-ng-file-upload
        // https://github.com/danialfarid/ng-file-upload/issues/1140
        // https://github.com/danialfarid/ng-file-upload/issues/1167
        cy.get('input[type="file"][tabindex=0]').each(($el, index) => {
            const upload = `fileUpload#${index}`;
            cy.intercept('POST', '**/upload').as(upload);
            cy.wrap($el).selectFile(
                {contents: '@smallPng', fileName: `small-${index}.png`},
                {force: true}
            );
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

        // PLATZBESTAETIGUNG mit Kita SB
        // !!!!!! - New User - !!!!!!
        SidenavPO.getGesuchsDaten()
            .then(el$ => el$.data('antrags-id'))
            .as('antragsId');
        const antragIdAlias = '@antragsId';

        cy.changeLogin(userKita);
        openGesuchInBetreuungen(antragIdAlias);

        cy.wait(1500);

        AntragBetreuungPO.getBetreuung(0, 0).click();
        AntragBetreuungPO.fillKitaBetreuungspensumForm('withValid', gemeinde);
        AntragBetreuungPO.getErweiterteBeduerfnisseBestaetigt().click();
        AntragBetreuungPO.platzBestaetigen();

        // !!!!!! - changed back to previous user - !!!!!!
        // VERFUEGUNG
        cy.changeLogin(userSB);
        openGesuchInVerfuegung(antragIdAlias);

        SidenavPO.getGesuchStatus().should('have.text', 'In Bearbeitung');

        VerfuegenPO.finSitAkzeptieren();
        VerfuegenPO.pruefeGesuch();
        SidenavPO.getGesuchStatus().should('have.text', 'Geprüft');

        VerfuegenPO.verfuegenStarten();
        SidenavPO.getGesuchStatus().should('have.text', 'Verfügen');

        VerfuegenPO.getVerfuegung(0, 0).click();
        VerfuegungPO.getBetreuungspensumProzent(5).should(
            'include.text',
            '25%'
        );
        VerfuegungPO.getBetreuungspensumProzent(6).should(
            'include.text',
            '25%'
        );
        VerfuegungPO.getBetreuungspensumProzent(7).should(
            'include.text',
            '25%'
        );
        VerfuegungPO.getBetreuungspensumProzent(8).should(
            'include.text',
            '25%'
        );
        VerfuegungPO.getBetreuungspensumProzent(9).should(
            'include.text',
            '25%'
        );
        VerfuegungPO.getBetreuungspensumProzent(10).should(
            'include.text',
            '25%'
        );
        VerfuegungPO.getBetreuungspensumProzent(11).should(
            'include.text',
            '25%'
        );

        VerfuegungPO.nichtEintretenVerfuegen();
        SidenavPO.getGesuchStatus().should('have.text', 'Verfügt');
        VerfuegenPO.getBetreuungsstatus(0, 0).should(
            'have.text',
            'Nicht eingetreten'
        );
    });
});

function openGesuchInBetreuungen(antragIdAlias: string) {
    cy.waitForRequest(
        'GET',
        '**/FINANZIELLE_SITUATION_TYP/gemeinde/*/gp/*',
        () => {
            cy.get(antragIdAlias).then(antragsId => {
                cy.visit(`/#/gesuch/betreuungen/${antragsId}`);
            });
        }
    );
}

function openGesuchInVerfuegung(antragIdAlias: string) {
    cy.get(antragIdAlias).then(antragsId =>
        cy.visit(`/#/gesuch/verfuegen/${antragsId}`)
    );
}
