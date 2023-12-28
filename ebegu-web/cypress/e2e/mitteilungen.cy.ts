import {TestFaellePO} from '@dv-e2e/page-objects';
import { getUser, normalizeUser } from '@dv-e2e/types';

describe('Kibon - Test Mitteilungen', () => {
    const userSuperAdmin = getUser('[1-Superadmin] E-BEGU Superuser');
    const userSB = getUser('[6-L-SB-BG] Jörg Keller');
    const userGS = getUser('[5-GS] Michael Berger');
    let gesuchUrl: string;

    const subjectGS: string = 'Frage Gutschein';
    const inhaltGS: string = 'Wieso wurde der Gutschein gekürzt?';
    const subjectSB: string = 'Antwort Gutschein';
    const inhaltSB: string = 'Guten Tag, der Gutschein wurde nicht gekürzt.';

    before(() => {
        cy.resetViewport();
        cy.intercept({ resourceType: 'xhr' }, { log: false }); // don't log XHRs
        cy.login(userSuperAdmin);
        cy.visit('/#/faelle');

        TestFaellePO.createOnlineTestfall({
            testFall: 'testfall-2',
            besitzerin: '[5-GS] Michael Berger',
            betreuungsstatus: 'verfuegt',
            periode: '2023/24',
            gemeinde: 'London'
        });

        cy.getByData('fall-creation-eingangsdatum').find('input').should('have.value', '15.2.2016');
        cy.getByData('verantwortlicher').click();
        cy.getByData(`option.${normalizeUser(userSB)}`).click();

        cy.url().then((url) => {
            const parts = new URL(url);
            gesuchUrl = `${parts.pathname}${parts.hash}`;
        });
    });

    it('Gesuchsteller send Message to Sachbearbeiter', () => {
        cy.viewport('iphone-8');
        cy.login(userGS);

        cy.intercept('GET', '**/gesuchsperioden/gemeinde/**').as('untilReadyGS1');
        cy.visit(gesuchUrl);
        cy.wait('@untilReadyGS1', {timeout: 17500});

        cy.getByData('mobile-menu-button').click();
        cy.getByData('menu.mitteilungen').click();

        cy.getByData('subject').type(subjectGS);
        cy.getByData('nachricht').type(inhaltGS);
        cy.intercept('PUT', '**/mitteilungen/send').as('sendingMitteilung');
        cy.getByData('container.senden', 'navigation-button').click();
        cy.wait('@sendingMitteilung');
        cy.resetViewport();
    });

    it('Sachbearbeiter sees message and responds', () => {
        cy.login(userSB);

        cy.intercept('GET', '**/mitteilungen/amountnewforuser/**').as('mitteilungCount');
        cy.intercept('GET', '**/gesuchsperioden/gemeinde/**').as('untilReadySB');
        cy.visit(gesuchUrl);
        cy.wait('@untilReadySB', {timeout: 17500});
        cy.wait('@mitteilungCount');

        cy.getByData('posteingang-link').should('include.text', '(1)');
        cy.getByData('posteingang-link').click();

        cy.getByData('mitteilung#0').click();

        cy.getByData('container.mitteilung#0', 'nachricht-subject').should('include.text', subjectGS);
        cy.getByData('container.mitteilung#0').click();
        cy.getByData('container.mitteilung#0', 'nachricht-inhalt').should('include.text', inhaltGS);

        cy.getByData('empfaenger').select('Antragsteller/in');
        cy.getByData('subject').type(subjectSB);
        cy.getByData('nachricht').type(inhaltSB);
        cy.intercept('PUT', '**/mitteilungen/send').as('sendingMitteilung');
        cy.getByData('container.senden', 'navigation-button').click();
        cy.wait('@sendingMitteilung');
    });

    it('Gesuchsteller sees Sachbearbeiter message', () => {
        cy.viewport('iphone-8');
        cy.login(userGS);

        cy.intercept('GET', '**/amountnew/dossier/**').as('mitteilungCount');
        cy.visit('/#/');
        cy.wait('@mitteilungCount');

        cy.getByData('mobile-menu-button').click();
        cy.getByData('menu.mitteilungen').should('include.text', '(1)');
        cy.getByData('menu.mitteilungen').click();

        cy.getByData('container.mitteilung#0', 'nachricht-subject').should('include.text', subjectSB);
        cy.getByData('container.mitteilung#0').click();
        cy.getByData('container.mitteilung#0', 'nachricht-inhalt').should('include.text', inhaltSB);
        cy.resetViewport();
    });
});
