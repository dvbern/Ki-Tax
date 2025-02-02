import {
    AntragCreationPO,
    DossierToolbarGesuchstellendePO,
    MainNavigationPO,
    MitteilungenPO,
    NavbarPO,
    PosteingangPO,
    TestFaellePO
} from '@dv-e2e/page-objects';
import {getUser} from '@dv-e2e/types';

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
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
        cy.login(userSuperAdmin);
        cy.visit('/#/faelle');

        TestFaellePO.createOnlineTestfall({
            testFall: 'testfall-2',
            besitzerin: '[5-GS] Michael Berger',
            betreuungsstatus: 'verfuegt',
            periode: '2023/24',
            gemeinde: 'London'
        });

        AntragCreationPO.getEingangsdatum()
            .find('input')
            .should('have.value', '15.2.2016');
        AntragCreationPO.getVerantwortlicher().click();
        AntragCreationPO.getUserOption(userSB, false).click();

        cy.url().then(url => {
            const parts = new URL(url);
            gesuchUrl = `/${parts.hash}`;
        });
    });

    it('Gesuchsteller send Message to Sachbearbeiter', () => {
        cy.viewport('iphone-8');
        cy.login(userGS);

        cy.waitForRequest(
            'GET',
            '**/gesuchsperioden/gemeinde/**',
            () => {
                cy.visit(gesuchUrl);
            },
            {waitOptions: {timeout: 17500}}
        );

        MainNavigationPO.getMobileMenuButton().click();
        DossierToolbarGesuchstellendePO.getMitteilungen().click();

        MitteilungenPO.getSubjectInput().type(subjectGS);
        MitteilungenPO.getNachrichtInput().type(inhaltGS);
        cy.waitForRequest('PUT', '**/mitteilungen/send', () => {
            MitteilungenPO.getNachrichtSendenButton().click();
        });
        cy.resetViewport();
    });

    it('Sachbearbeiter sees message and responds', () => {
        cy.login(userSB);

        // TODO: add support for intercepting multiple requests in custom command
        cy.intercept('GET', '**/mitteilungen/amountnewforuser/**').as(
            'mitteilungCount'
        );
        cy.intercept('GET', '**/gesuchsperioden/gemeinde/**').as(
            'untilReadySB'
        );
        cy.visit(gesuchUrl);
        cy.wait('@untilReadySB', {timeout: 17500});
        cy.wait('@mitteilungCount');

        NavbarPO.getLinkPosteingang().should('include.text', '(1)');
        NavbarPO.getLinkPosteingang().click();

        PosteingangPO.getMitteilung(0).click();

        MitteilungenPO.getSubjectOfMitteilung(0).should(
            'include.text',
            subjectGS
        );
        MitteilungenPO.getMitteilung(0).click();
        MitteilungenPO.getInhaltOfMitteilung(0).should(
            'include.text',
            inhaltGS
        );

        MitteilungenPO.getEmpfangendeInput().select('Antragsteller/in');
        MitteilungenPO.getSubjectInput().type(subjectSB);
        MitteilungenPO.getNachrichtInput().type(inhaltSB);
        cy.waitForRequest('PUT', '**/mitteilungen/send', () => {
            MitteilungenPO.getNachrichtSendenButton().click();
        });
    });

    it('Gesuchsteller sees Sachbearbeiter message', () => {
        cy.viewport('iphone-8');
        cy.login(userGS);

        cy.waitForRequest('GET', '**/amountnew/dossier/**', () => {
            cy.visit('/#/');
        });

        MainNavigationPO.getMobileMenuButton().click();
        DossierToolbarGesuchstellendePO.getMitteilungen().should(
            'include.text',
            '(1)'
        );
        DossierToolbarGesuchstellendePO.getMitteilungen().click();

        MitteilungenPO.getSubjectOfMitteilung(0).should(
            'include.text',
            subjectSB
        );
        MitteilungenPO.getMitteilung(0).click();
        MitteilungenPO.getInhaltOfMitteilung(0).should(
            'include.text',
            inhaltSB
        );
        cy.resetViewport();
    });
});
