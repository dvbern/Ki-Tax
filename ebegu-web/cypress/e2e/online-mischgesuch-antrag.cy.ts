import {getUser, normalizeUser, User} from '@dv-e2e/types';
import {
    AntragBetreuungPO, AntragCreationPO, ConfirmDialogPO,
    DossierToolbarPO,
    FaelleListePO,
    FallToolbarPO, FreigabePO,
    NavigationPO,
    TestFaellePO, VerfuegenPO, VerfuegungPO,
} from '@dv-e2e/page-objects';
import {SidenavPO} from '../page-objects/antrag/sidenav.po';

describe('Kibon - Online TS-Anmeldung (Mischgesuch) [Gesuchsteller]', () => {
    const userSuperadmin = getUser('[1-Superadmin] E-BEGU Superuser');
    const userGS = getUser('[5-GS] Emma Gerber');
    const userTS = getUser('[3-SB-TS-Paris] Charlotte Gainsbourg');
    const userSB = getUser('[6-P-Admin-Gemeinde] Gerlinde Hofstetter');
    let gesuchUrl: string;
    let fallnummer: string;

    before(() => {
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
        cy.login(userSuperadmin);
        cy.visit('/#/faelle');

        TestFaellePO.createOnlineTestfall({
            testFall: 'testfall-2',
            periode: '2023/24',
            gemeinde: 'Paris',
            besitzerin: '[5-GS] Emma Gerber',
            betreuungsstatus: 'warten'
        });

        cy.url().then((url) => {
            const parts = new URL(url);
            gesuchUrl = `/${parts.hash}`;
        });
        FallToolbarPO.getFallnummer().then(el$ => {
            fallnummer = el$.text();
        });
    });

    it('shoud create a online ts anmeldung, send mails and verfuegen', () => {
        loginAndGoToGesuch(userGS);
        craeateTsAnmeldungFuerKind1();

        loginAndGoToGesuch(userSuperadmin);
        plaetzeFuerBeideKinderBestaetigen();

        loginAndGoToGesuch(userGS);
        gesuchFreigeben();

        loginAndGoToGesuch(userSuperadmin);
        freigabequittungEinlesen();

        tsAkzeptierenAsUserTs();
        //TODO Überprüfen, ob einen Email versendet wurde => 1. Bestätigung ohne FinSit

        loginAndGoToGesuch(userSB);
        finSitAkzeptieren();
        verfuegenStarten();
        //TODO Überprüfen, ob einen Email versendet wurde => 2. Bestätigung mit FinSit

        loginAndGoToGesuch(userGS);
        createTsAnmeldungFuerKind2();

        loginAndGoToGesuch(userSuperadmin);
        tsAkzeptierenFuerKind2();
        //TODO Überprüfen, ob ein weiteres Email versendet wurde (Anmeldung zweites Kind mit Tarif)

        gesuchVerfuegen();
        checkBetreuungsstatus();
    });

    const craeateTsAnmeldungFuerKind1 = () => {
        SidenavPO.goTo('BETREUUNG');
        AntragBetreuungPO.getBetreuungErstellenButton(0).click();
        AntragBetreuungPO.selectTagesschulBetreuung();
        AntragBetreuungPO.fillTagesschulBetreuungsForm('withValid', 'Paris');
        AntragBetreuungPO.saveBetreuung();

    };

    const createTsAnmeldungFuerKind2 = () => {
        SidenavPO.goTo('BETREUUNG');
        AntragBetreuungPO.getBetreuungErstellenButton(1).should('not.exist');
        AntragBetreuungPO.getAnmeldungErstellenButton(1).click();
        AntragBetreuungPO.fillTagesschulBetreuungsForm('withValid', 'Paris');
        AntragBetreuungPO.saveAndConfirmBetreuung();
        AntragBetreuungPO.getBetreuung(1, 1).should('exist');
    };

    const plaetzeFuerBeideKinderBestaetigen = () => {
        SidenavPO.goTo('BETREUUNG');
        AntragBetreuungPO.getBetreuung(0, 0).click();
        AntragBetreuungPO.getKorrekteKostenBestaetigung().click();
        AntragBetreuungPO.getPlatzBestaetigenButton().click();
        cy.visit(gesuchUrl);
        SidenavPO.goTo('BETREUUNG');
        AntragBetreuungPO.getBetreuung(1, 0).click();
        AntragBetreuungPO.getKorrekteKostenBestaetigung().click();
        AntragBetreuungPO.getPlatzBestaetigenButton().click();
    };

    const tsAkzeptierenAsUserTs = () => {
        cy.login(userTS);
        cy.visit('/#/faelle');
        FaelleListePO.getAntrag(fallnummer).click();
        AntragBetreuungPO.getBetreuung(0, 0).click();
        AntragBetreuungPO.getPlatzAkzeptierenButton().click();
        cy.waitForRequest('PUT', '**/betreuungen/schulamt/akzeptieren', () => {
            ConfirmDialogPO.getConfirmButton().click();
        });
    };

    const tsAkzeptierenFuerKind2 = () => {
        SidenavPO.goTo('BETREUUNG');
        AntragBetreuungPO.getBetreuung(1, 1).click();
        AntragBetreuungPO.getPlatzAkzeptierenButton().click();

        cy.waitForRequest('GET', '**/dossier/fall/**', () => {
            ConfirmDialogPO.getConfirmButton().click();
        });
    };

    const gesuchFreigeben = () => {
        SidenavPO.goTo('DOKUMENTE');
        NavigationPO.saveAndGoNext();

        FreigabePO.getFreigebenButton().click();
        cy.getDownloadUrl(() => {
            cy.waitForRequest('GET', '**/dossier/fall/**', () => {
                ConfirmDialogPO.getConfirmButton().click();
            });
        });
    };

    const freigabequittungEinlesen = () => {
        SidenavPO.goTo('FREIGABE');
        cy.waitForRequest('GET', '**/dossier/fall/**', () => {
            FreigabePO.getFreigabequittungEinscannenSimulierenButton().click();
        });

        SidenavPO.goTo('GESUCH_ERSTELLEN');
        AntragCreationPO.getEingangsdatum().find('input').clear().type('31.07.2023');

        cy.waitForRequest('PUT', '**/gesuche', () => {
            NavigationPO.saveAndGoNext();
        });
    };

    const finSitAkzeptieren = () => {
        SidenavPO.goTo('VERFUEGEN');
        VerfuegenPO.getFinSitAkzeptiert('AKZEPTIERT').click();

        cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
            VerfuegenPO.getGeprueftButton().click();
            ConfirmDialogPO.getConfirmButton().click();
        });
    };

    const verfuegenStarten = () => {
        cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
            VerfuegenPO.getVerfuegenStartenButton().click();
            ConfirmDialogPO.getConfirmButton().click();
        });
    };

    const gesuchVerfuegen = () => {
        betreuungVerfuegen(0, 0);
        betreuungVerfuegen(1, 0);
    };

    const betreuungVerfuegen = (kindIndex: number, betreuungIndex: number) => {
        SidenavPO.goTo('VERFUEGEN');

        cy.waitForRequest('GET', '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**', () => {
            VerfuegenPO.getVerfuegung(kindIndex, betreuungIndex).click();
        });

        VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();

        cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
            VerfuegungPO.getVerfuegenButton().click();
            ConfirmDialogPO.getConfirmButton().click();
        });
    };

    const checkBetreuungsstatus = () => {
        SidenavPO.goTo('VERFUEGEN');
        VerfuegenPO.getBetreuungsstatus(0, 0).should('include.text', 'Verfügt');
        VerfuegenPO.getBetreuungsstatus(0, 1).should('include.text', 'Anmeldung übernommen');
        VerfuegenPO.getBetreuungsstatus(1, 0).should('include.text', 'Verfügt');
        VerfuegenPO.getBetreuungsstatus(1, 1).should('include.text', 'Anmeldung übernommen');
    };

    const loginAndGoToGesuch = (user: User) => {
        cy.login(user);
        cy.visit(gesuchUrl);
    };

});


