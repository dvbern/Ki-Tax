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

import {getUser, User} from '@dv-e2e/types';
import {
    AntragBetreuungPO,
    AntragCreationPO,
    ConfirmDialogPO,
    FallToolbarPO,
    FreigabePO,
    NavigationPO,
    TestFaellePO,
    VerfuegenPO,
    VerfuegungPO
} from '@dv-e2e/page-objects';
import {SidenavPO} from '../../page-objects/antrag/sidenav.po';

describe('Kibon - Online TS-Anmeldung (Mischgesuch) [Gesuchsteller]', () => {
    const userSuperadmin = getUser('[1-Superadmin] E-BEGU Superuser');
    const userGS: User = '[5-GS] Emma Gerber';
    const userTS = getUser('[3-SB-TS-Paris] Charlotte Gainsbourg');
    const userGemeindeBGTS = getUser(
        '[6-P-Admin-Gemeinde] Gerlinde Hofstetter'
    );
    const userTraegerschaft = getUser(
        '[3-SB-Trägerschaft-Kitas-StadtBern] Agnes Krause'
    );
    let gesuchUrl: string;
    let fallnummer: string;
    let antragIdAlias: string;

    before(() => {
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
        cy.login(userSuperadmin);
        cy.visit('/');
        TestFaellePO.createOnlineTestfall({
            testFall: 'testfall-2',
            periode: '2023/24',
            gemeinde: 'Paris',
            besitzerin: userGS,
            betreuungsstatus: 'warten'
        });

        cy.url().then(url => {
            const parts = new URL(url);
            gesuchUrl = `/${parts.hash}`;
        });
        FallToolbarPO.getFallnummer().then(el$ => {
            fallnummer = el$.text();
        });

        SidenavPO.getGesuchsDaten()
            .then(el$ => el$.data('antrags-id'))
            .as('antragsId');
        antragIdAlias = '@antragsId';
    });

    it('shoud create a online ts anmeldung, send mails and verfuegen', () => {
        // GESUCHSTELLER
        changeUserAndOpenBetreuung(userGS, antragIdAlias);
        craeateTsAnmeldungFuerKind1();

        // TRAEGERSCHAFT
        changeUserAndOpenBetreuung(userTraegerschaft, antragIdAlias);
        plaetzeFuerBeideKinderBestaetigen(antragIdAlias);

        // GESUCHSTELLER
        changeUserAndOpenBetreuung(userGS, antragIdAlias);
        gesuchFreigeben();

        // SUPERADMIN
        changeUserAndOpenBetreuung(userSuperadmin, antragIdAlias);
        freigabequittungEinlesen();

        // TAGESSCHULE
        changeUserAndOpenBetreuung(userTS, antragIdAlias);
        tsAkzeptieren(0, 0);
        //TODO Überprüfen, ob einen Email versendet wurde => 1. Bestätigung ohne FinSit

        // GEMEINDE
        changeUserAndOpenBetreuung(userGemeindeBGTS, antragIdAlias);
        finSitAkzeptierenUndPruefen();
        VerfuegenPO.verfuegenStarten();
        //TODO Überprüfen, ob einen Email versendet wurde => 2. Bestätigung mit FinSit

        // GESUCHSTELLER
        changeUserAndOpenBetreuung(userGS, antragIdAlias);
        createTsAnmeldungFuerKind2();

        // TAGESSCHULE
        // Gesuch ist verfügt und wir übernehmen nun statt zu akzeptieren
        changeUserAndOpenBetreuung(userTS, antragIdAlias);
        tsUebernehmen(1, 0);
        //TODO Überprüfen, ob ein weiteres Email versendet wurde (Anmeldung zweites Kind mit Tarif)

        // GEMEINDE
        changeUserAndOpenBetreuung(userGemeindeBGTS, antragIdAlias);
        gesuchVerfuegen();
        checkBetreuungsstatus();
    });

    const craeateTsAnmeldungFuerKind1 = () => {
        SidenavPO.goTo('BETREUUNG');
        AntragBetreuungPO.getBetreuungErstellenButton(0).click();
        AntragBetreuungPO.selectTagesschulBetreuung();
        AntragBetreuungPO.fillTagesschulBetreuungsForm('withValid', 'Paris');
        AntragBetreuungPO.saveBetreuung();
        SidenavPO.getGesuchStatus().should(
            'include.text',
            'Warten auf Platzbestätigung'
        );
    };

    const createTsAnmeldungFuerKind2 = () => {
        SidenavPO.goTo('BETREUUNG');
        AntragBetreuungPO.getBetreuungErstellenButton(1).should('not.exist');
        AntragBetreuungPO.getAnmeldungErstellenButton(1).click();
        AntragBetreuungPO.fillTagesschulBetreuungsForm('withValid', 'Paris');
        AntragBetreuungPO.saveAndConfirmBetreuung();
        AntragBetreuungPO.getBetreuung(1, 1).should('exist');
    };

    const plaetzeFuerBeideKinderBestaetigen = (antragIdAlias: string) => {
        SidenavPO.goTo('BETREUUNG');
        SidenavPO.getGesuchStatus().should(
            'include.text',
            'In Bearbeitung Antragsteller/in'
        );
        AntragBetreuungPO.getBetreuung(0, 0).click();
        cy.wait(1000);
        AntragBetreuungPO.getKorrekteKostenBestaetigung().click();
        AntragBetreuungPO.getPlatzBestaetigenButton().click();
        openGesuchInBetreuung(antragIdAlias);
        AntragBetreuungPO.getBetreuung(1, 0).click();
        cy.wait(1500);
        AntragBetreuungPO.getKorrekteKostenBestaetigung().click();
        cy.wait(1500);
        AntragBetreuungPO.getPlatzBestaetigenButton().click();
    };

    const tsAkzeptieren = (kindIndex: number, betreuungsIndex: number) => {
        AntragBetreuungPO.getBetreuung(kindIndex, betreuungsIndex).click();
        cy.waitForRequest('PUT', '**/betreuungen/schulamt/akzeptieren', () => {
            AntragBetreuungPO.getPlatzAkzeptierenButton().click();
            cy.wait(1500);
            ConfirmDialogPO.getDvLoadingConfirmButton().click();
        });
    };

    const tsUebernehmen = (kindIndex: number, betreuungsIndex: number) => {
        AntragBetreuungPO.getBetreuung(kindIndex, betreuungsIndex).click();
        cy.waitForRequest('PUT', '**/anmeldung/uebernehmen', () => {
            AntragBetreuungPO.getPlatzAkzeptierenButton().click();
            cy.wait(1500);
            ConfirmDialogPO.getDvLoadingConfirmButton().click();
        });
    };

    const gesuchFreigeben = () => {
        SidenavPO.goTo('DOKUMENTE');
        NavigationPO.saveAndGoNext();

        FreigabePO.getFreigebenButton().click();
        cy.getDownloadUrl(() => {
            cy.waitForRequest('GET', '**/dossier/fall/**', () => {
                ConfirmDialogPO.getDvLoadingConfirmButton().click();
            });
        });
    };

    const freigabequittungEinlesen = () => {
        SidenavPO.goTo('FREIGABE');
        SidenavPO.getGesuchStatus().should(
            'include.text',
            'Freigabequittung ausstehend'
        );
        cy.waitForRequest('GET', '**/dossier/fall/**', () => {
            FreigabePO.getFreigabequittungEinscannenSimulierenButton().click();
        });

        SidenavPO.goTo('GESUCH_ERSTELLEN');
        AntragCreationPO.getEingangsdatum()
            .find('input')
            .clear()
            .type('31.07.2023');

        cy.waitForRequest('PUT', '**/gesuche', () => {
            NavigationPO.saveAndGoNext();
        });
    };

    const finSitAkzeptierenUndPruefen = () => {
        SidenavPO.goTo('VERFUEGEN');
        VerfuegenPO.finSitAkzeptieren();
        VerfuegenPO.pruefeGesuch();
    };

    const gesuchVerfuegen = () => {
        betreuungVerfuegen(0, 0);
        betreuungVerfuegen(1, 0);
    };

    const betreuungVerfuegen = (kindIndex: number, betreuungIndex: number) => {
        SidenavPO.goTo('VERFUEGEN');
        cy.waitForRequest(
            'GET',
            '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**',
            () => {
                VerfuegenPO.getVerfuegung(kindIndex, betreuungIndex).click();
            }
        );

        VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();

        cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
            VerfuegungPO.getVerfuegenButton().click();
            ConfirmDialogPO.getDvLoadingConfirmButton().click();
        });
    };

    const checkBetreuungsstatus = () => {
        SidenavPO.goTo('VERFUEGEN');
        SidenavPO.getGesuchStatus().should('include.text', 'Verfügt');
        VerfuegenPO.getBetreuungsstatus(0, 0).should('include.text', 'Verfügt');
        VerfuegenPO.getBetreuungsstatus(0, 1).should(
            'include.text',
            'Anmeldung übernommen'
        );
        VerfuegenPO.getBetreuungsstatus(1, 0).should('include.text', 'Verfügt');
        VerfuegenPO.getBetreuungsstatus(1, 1).should(
            'include.text',
            'Anmeldung übernommen'
        );
    };
});

function openGesuchInBetreuung(antragIdAlias: string) {
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

function changeUserAndOpenBetreuung(user: User, antragIdAlias: string) {
    cy.changeLogin(user);
    openGesuchInBetreuung(antragIdAlias);
}
