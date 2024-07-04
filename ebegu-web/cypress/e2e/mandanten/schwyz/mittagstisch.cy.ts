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
    AbweichungenPO,
    AntragBetreuungPO,
    ConfirmDialogPO,
    DossierToolbarPO,
    FreigabePO,
    MitteilungenPO,
    SidenavPO,
    TestFaellePO,
    VerfuegenPO,
    VerfuegungPO
} from '@dv-e2e/page-objects';
import {GemeindeTestFall, getUser, TestGesuchstellende} from '@dv-e2e/types';
import {MANDANTS} from '../../../../src/app/core/constants/MANDANTS';

describe('Mittagstisch Anmeldung', () => {
    const besitzerin: TestGesuchstellende = '[5-GS] Heinrich Mueller';
    const userGS = getUser(besitzerin);
    const userSB = getUser('[6-P-SB-BG] Jörg Becker');
    const userSuperadmin = getUser('[1-Superadmin] E-BEGU Superuser');
    const userTraegerschaft = getUser(
        '[3-SB-Trägerschaft-Kitas-StadtBern] Agnes Krause'
    );
    const gemeinde: GemeindeTestFall = 'Testgemeinde Schwyz';

    it('should be possible to make and verfuegen a mittagstisch anmeldung', () => {
        cy.changeMandant(MANDANTS.SCHWYZ);
        //SUPER ADMIN
        cy.login(userSuperadmin);
        cy.visit('/');
        TestFaellePO.createOnlineTestfall({
            testFall: 'testfall-1',
            betreuungsstatus: 'warten',
            besitzerin: besitzerin,
            gemeinde,
            periode: '2024/25'
        });
        SidenavPO.getGesuchsDaten()
            .then(el$ => el$.data('antrags-id'))
            .as('antragsId');
        const antragIdAlias = '@antragsId';

        // GESUCHSTELLER
        cy.changeLogin(userGS);
        openGesuchInBetreuungen(antragIdAlias);
        AntragBetreuungPO.getBetreuungLoeschenButton(0, 0).click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
        AntragBetreuungPO.getBetreuungLoeschenButton(0, 1).click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
        AntragBetreuungPO.getBetreuungErstellenButton(0).click();
        AntragBetreuungPO.fillMittagstischBetreuungsForm(
            'withSchwyz',
            gemeinde
        );
        AntragBetreuungPO.platzBestaetigungAnfordern();

        // TRAEGERSCHAFT
        cy.changeLogin(userTraegerschaft);
        openGesuchInBetreuungen(antragIdAlias);
        AntragBetreuungPO.getBetreuung(0, 0).click();
        AntragBetreuungPO.fillMittagstischBetreuungenForm(
            'withSchwyz',
            gemeinde
        );
        AntragBetreuungPO.platzBestaetigen();

        // GESUCHSTELLER
        cy.changeLogin(userGS);
        openGesuchInFreigabeSchwyz(antragIdAlias);
        FreigabePO.freigebenSchwyz();
        cy.wait(1500);

        // SACHBEARBEITER
        cy.changeLogin(userSB);
        openGesuchInFreigabeSchwyz(antragIdAlias);
        SidenavPO.goTo('VERFUEGEN');
        verfuegen();

        // TRAEGERSCHAFT
        cy.changeLogin(userTraegerschaft);
        openGesuchInBetreuungen(antragIdAlias);
        AntragBetreuungPO.getBetreuung(0, 0).click();
        AntragBetreuungPO.getMutationsmeldungErstellenButton().click();
        AntragBetreuungPO.getMonatlicheHauptmahlzeiten(0).clear().type('12');
        AntragBetreuungPO.getMutationsmeldungSendenButton().click();
        cy.waitForRequest(
            'PUT',
            '**/mitteilungen/sendbetreuungsmitteilung',
            () => {
                ConfirmDialogPO.getDvLoadingConfirmButton().click();
            }
        );

        // SACHBEARBEITER
        cy.changeLogin(userSB);
        openGesuchInBetreuungen(antragIdAlias);
        cy.wait(1500);
        cy.waitForRequest('GET', '**/mitteilungen/**', () => {
            DossierToolbarPO.getMitteilungen().click();
        });
        cy.wait(1500);
        MitteilungenPO.getMitteilung(0).click();
        MitteilungenPO.getMutationsmeldungHinzufuegenButton(0).click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
        DossierToolbarPO.getAntraegeTrigger().click();
        DossierToolbarPO.getAntrag(1).click();
        SidenavPO.goTo('BETREUUNG');
        AntragBetreuungPO.getBetreuung(0, 0).click();
        AntragBetreuungPO.getMonatlicheHauptmahlzeiten(0).should(
            'have.value',
            '12'
        );
        SidenavPO.getGesuchsDaten()
            .then(el$ => el$.data('antrags-id'))
            .as('mutation1');
        const mutationIdAlias = '@mutation1';
        SidenavPO.goTo('VERFUEGEN');
        verfuegen();
        cy.changeMandant(MANDANTS.BERN);
    });
});

function openGesuchInFreigabe(antragIdAlias: string) {
    cy.waitForRequest(
        'GET',
        '**/FREIGABE_QUITTUNG_EINLESEN_REQUIRED/gemeinde/*/gp/*',
        () => {
            cy.get(antragIdAlias).then(antragsId =>
                cy.visit(`/#/gesuch/freigabe/${antragsId}`)
            );
        }
    );
}
function openGesuchInFreigabeSchwyz(antragIdAlias: string) {
    cy.get(antragIdAlias).then(antragsId =>
        cy.visit(`/#/gesuch/freigabe/${antragsId}`)
    );
}

function openGesuchInBetreuungen(antragIdAlias: string) {
    cy.waitForRequest(
        'GET',
        '**/FINANZIELLE_SITUATION_TYP/gemeinde/*/gp/*',
        () => {
            cy.get(antragIdAlias).then(antragsId =>
                cy.visit(`/#/gesuch/betreuungen/${antragsId}`)
            );
        }
    );
}

function verfuegen(): void {
    VerfuegenPO.finSitAkzeptieren();
    VerfuegenPO.pruefeGesuch();
    VerfuegenPO.verfuegenStarten();
    VerfuegenPO.getVerfuegung(0, 0).click();
    VerfuegungPO.betreuungKontrollierenAndVerfuegen();
}
