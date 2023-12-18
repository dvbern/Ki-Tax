import {getUser, normalizeUser, User} from '@dv-e2e/types';
import { AnmeldungTagesschulePO} from '@dv-e2e/page-objects';

describe('Kibon - Online TS-Anmeldung (Mischgesuch) [Gesuchsteller]', () => {
    const userSuperadmin = getUser('[1-Superadmin] E-BEGU Superuser');
    const userGS = getUser('[5-GS] Emma Gerber');
    const userTS = getUser('[3-SB-TS-Paris] Charlotte Gainsbourg');
    const userSB = getUser('[6-P-Admin-Gemeinde] Gerlinde Hofstetter');
    let gesuchUrl: string;


    before(() => {
        cy.intercept({ resourceType: 'xhr' }, { log: false }); // don't log XHRs
        cy.login(userSuperadmin);
        cy.visit('/#/faelle');

        cy.getByData('page-menu').click();
        cy.getByData('action-admin.testdaten').click();
        cy.getByData('creationType.warten').find('label').click();
        cy.getByData('gesuchsteller').click();
        cy.getByData(`gesuchsteller.${normalizeUser(userGS)}`).click();
        cy.getByData('gemeinde').click();
        cy.getByData('gemeinde.Paris').click();
        cy.getByData('periode').click();
        cy.getByData('periode.2023/24').click();

        cy.getByData('testfall-2').click();
        cy.get('[data-test="dialog-link"]', { timeout: Cypress.config('defaultCommandTimeout') * 4 }).click();

        cy.url().then((url) => {
            const parts = new URL(url);
            gesuchUrl = `${parts.pathname}${parts.hash}`;
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
        cy.getByData('sidenav.BETREUUNG').click();
        cy.getByData('container.kind#0', 'container.create-betreuung','navigation-button').click();
        AnmeldungTagesschulePO.selectTagesschule();
        AnmeldungTagesschulePO.fillAnmeldungTagesschule();
        AnmeldungTagesschulePO.save();
        cy.waitForRequest('GET', '**/institutionstammdaten/gesuchsperiode/gemeinde/active', () => {
            cy.getByData('container.kind#0', 'container.create-betreuung', 'navigation-button').click();
        });
    };

    const createTsAnmeldungFuerKind2 = () => {
        cy.getByData('sidenav.BETREUUNG').click();
        cy.getByData('container.create-betreuung').should('not.exist');
        cy.getByData('container.kind#1','container.create-tagesschule','navigation-button').click();
        AnmeldungTagesschulePO.fillAnmeldungTagesschule();
        AnmeldungTagesschulePO.save();
        AnmeldungTagesschulePO.confirm();
        cy.getByData('container.kind#1', 'container.betreuung#1').should('exist');
    };

    const plaetzeFuerBeideKinderBestaetigen = () => {
        cy.getByData('sidenav.BETREUUNG').click();
        cy.getByData('container.kind#0', 'container.betreuung#0').click();
        platzBestaetigen();
        cy.visit(gesuchUrl);
        cy.getByData('sidenav.BETREUUNG').click();
        cy.getByData('container.kind#1', 'container.betreuung#0').click();
        platzBestaetigen();
    };

    const tsAkzeptierenAsUserTs = () => {
        cy.login(userTS);
        cy.visit('/#/faelle');
        cy.getByData(`antrag-entry#0`).click();
        cy.getByData('container.betreuung#0').click();

        cy.getByData('container.akzeptieren','navigation-button').click();
        cy.waitForRequest('PUT', '**/betreuungen/schulamt/akzeptieren', () => {
            cy.getByData('container.confirm', 'navigation-button').click();
        });
    };

    const tsAkzeptierenFuerKind2 = () => {
        cy.getByData('sidenav.BETREUUNG').click();
        cy.getByData('container.kind#1','container.betreuung#1').click();
        cy.getByData('container.akzeptieren','navigation-button').click();

        cy.waitForRequest('GET', '**/dossier/fall/**', () => {
            cy.getByData('container.confirm','navigation-button').click();
        });
    };

    const gesuchFreigeben = () => {
        cy.getByData('sidenav.DOKUMENTE').click();
        cy.getByData('container.navigation-save', 'navigation-button').click();

        cy.getByData('container.freigeben', 'navigation-button').click();
        cy.getDownloadUrl(() => {
            cy.waitForRequest('GET', '**/dossier/fall/**', () => {
                cy.getByData('container.confirm', 'navigation-button').click();
            });
        });
    };

    const freigabequittungEinlesen = () => {
        cy.getByData('sidenav.FREIGABE').click();
        cy.waitForRequest('GET', '**/dossier/fall/**', () => {
            cy.getByData('container.antrag-freigeben-simulieren', 'navigation-button').click();
        });

        cy.getByData('sidenav.GESUCH_ERSTELLEN').click();
        cy.getByData('fall-creation-eingangsdatum').find('input').clear().type('31.07.2023');

        cy.waitForRequest('PUT', '**/gesuche', () => {
            cy.getByData('container.navigation-save', 'navigation-button').click();
        });
    };

    const finSitAkzeptieren = () => {
        cy.getByData('sidenav.VERFUEGEN').click();
        cy.getByData('finSitStatus.radio-value.AKZEPTIERT').click();

        cy.getByData('container.geprueft', 'navigation-button').click();
        cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
            cy.getByData('container.confirm', 'navigation-button').click();
        });
    };

    const verfuegenStarten = () => {
        cy.getByData('container.verfuegen', 'navigation-button').click();
        cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
            cy.getByData('container.confirm', 'navigation-button').click();
        });
    };

    const gesuchVerfuegen = () => {
        betreuungVerfuegen('verfuegung#0-0');
        betreuungVerfuegen('verfuegung#1-0');

    };

    const betreuungVerfuegen = (verfuegung: string) => {
        cy.getByData('sidenav.VERFUEGEN').click();

        cy.waitForRequest('GET', '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**', () => {
            cy.getByData(verfuegung).click();
        });

        cy.getByData('verfuegungs-bemerkungen-kontrolliert').click();
        cy.getByData('container.verfuegen', 'navigation-button').click();
        cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
            cy.getByData('container.confirm', 'navigation-button').click();
        });
    };

    const checkBetreuungsstatus = () => {
        cy.getByData('sidenav.VERFUEGEN').click();
        cy.getByData('verfuegung#0-0', 'betreuungs-status').should('include.text', 'Verfügt');
        cy.getByData('verfuegung#0-1', 'betreuungs-status').should('include.text', 'Anmeldung übernommen');
        cy.getByData('verfuegung#1-0', 'betreuungs-status').should('include.text', 'Verfügt');
        cy.getByData('verfuegung#1-1', 'betreuungs-status').should('include.text', 'Anmeldung übernommen');
    };

    const loginAndGoToGesuch = (user : User) => {
        cy.login(user);
        cy.visit(gesuchUrl);
    };

    const platzBestaetigen = () => {
        cy.getByData('korrekte-kosten-bestaetigung').click();
        cy.waitForRequest('PUT', '**/betreuungen/bestaetigen', () => {
            cy.getByData('container.platz-bestaetigen', 'navigation-button').click();
        });
    };
});


