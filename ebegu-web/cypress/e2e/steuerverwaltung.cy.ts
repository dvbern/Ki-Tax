import {getUser, normalizeUser} from '@dv-e2e/types';

describe('Kibon - Gesuch zu Steuerverwaltung senden', () => {
    const userSuperadmin = getUser('[1-Superadmin] E-BEGU Superuser');
    const userSteueramt = getUser('[7-L-Steueramt] Rodolfo Iten');
    const userGemeinde = getUser('[6-L-SB-Gemeinde] Stefan Weibel');
    let gesuchUrl: string;
    let fallnummer: string;

    before(() => {
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
        cy.login(userSuperadmin);
        cy.visit('/#/faelle');

        cy.getByData('page-menu').click();
        cy.getByData('action-admin.testdaten').click();
        cy.getByData('creationType.verfuegt').find('label').click();
        cy.getByData('gemeinde').click();
        cy.getByData('gemeinde.London').click();
        cy.getByData('periode').click();
        cy.getByData('periode.2023/24').click();

        cy.getByData('testfall-2').click();

        cy.intercept('GET', '**/dossier/id//**').as('opengesuch');
        cy.get('[data-test="dialog-link"]', {timeout: Cypress.config('defaultCommandTimeout') * 6}).click();
        cy.wait('@opengesuch');

        cy.url().then((url) => {
            const parts = new URL(url);
            gesuchUrl = `${parts.pathname}${parts.hash}`;
        });

        cy.getByData('fallnummer').should('not.be.empty');
        cy.getByData('fallnummer').invoke('text').then(value => {
            fallnummer = value;
            cy.getByData('fallnummer').should('contain.text', fallnummer);
        });
    });

    it('should send a gesuch for prüfung to steuerverwaltung and send it back to gemeinde', () => {
        cy.login(userGemeinde);
        cy.visit(gesuchUrl);
        cy.getByData('verantwortlicher').click();
        cy.getByData('container.verantwortlicher',`option.${normalizeUser(userGemeinde)}`).click();
        cy.intercept('GET', '**/verfuegung/calculate/**').as('verfuegenView');
        cy.getByData('sidenav.VERFUEGEN').click();
        cy.wait('@verfuegenView');
        cy.getByData('container.send-to-stv', 'navigation-button').click();
        cy.getByData('kommentar-absenden').should('have.focus');
        cy.getByData('kommentar-for-stv').focus().type('Wie hoch ist der Nettolohn im Jahr 2022 von Yvonne Feuz?');
        cy.getByData('kommentar-absenden').click();
        cy.getByData('gesuch.status').should('contain.text', 'Prüfung Steuerbüro der Gemeinde');

        cy.changeLogin(userSteueramt);
        cy.visit('/#/pendenzenSteueramt');
        cy.getByData(`antrag-entry#${fallnummer}`).click();
        cy.getByData('gesuch.status').should('contain.text', 'In Bearbeitung Steuerbüro der Gemeinde');

        cy.getByData('bemerkungen-gemeinde').should('have.value', 'Wie hoch ist der Nettolohn im Jahr 2022 von Yvonne Feuz?');
        cy.getByData('container.navigation-save').click();
        cy.getByData('gesuchformular-title').should('contain.text', 'Antragsteller/in');
        cy.getByData('gesuchformular-title').should('contain.text', '1');
        cy.getByData('container.navigation-save').click();
        cy.getByData('gesuchformular-title').should('contain.text', 'Antragsteller/in');
        cy.getByData('gesuchformular-title').should('contain.text', '2');
        cy.getByData('container.navigation-save').should('not.exist');
        // TODO: remove this wait once a solution for textarea issues has been found
        cy.wait(1000);
        cy.getByData('bemerkungen-stv').type("Der Nettolohn beträgt 50'000 CHF im Jahr 2021");
        cy.getByData('container.zurueck-an-gemeinde').click();
        cy.intercept('POST', '**/search/search').as('searchCompleted');
        cy.getByData('container.confirm').click();
        cy.wait('@searchCompleted');
        cy.getByData(`antrag-entry#${fallnummer}`).should('not.exist');

        cy.changeLogin(userGemeinde);
        cy.visit('/#/faelle');
        cy.getByData(`antrag-entry#${fallnummer}`).should('exist');

        cy.visit('/#/pendenzen');
        cy.getByData(`antrag-entry#${fallnummer}`).click();
        cy.getByData('gesuch.status').should('contain.text', 'Geprüft durch Steuerbüro der Gemeinde');
        cy.getByData('bemerkungen-stv').should('have.value', "Der Nettolohn beträgt 50'000 CHF im Jahr 2021");
    });
});
