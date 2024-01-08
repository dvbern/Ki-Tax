import {getUser} from "@dv-e2e/types";

describe('Kibon - generate Testfälle [Gemeinde Sachbearbeiter]', () => {
    const userSB = getUser('[3-SB-Institution-Kita-Brünnen] Sophie Bergmann');
    const adminUser = getUser('[1-Superadmin] E-BEGU Superuser');
    const monatlichesPensum = '60';
    const monatlicheKosten = '1000';
    const startdatum = '01.04.2023';
    const enddatum = '30.06.2023';
    let fallnummer: string;

    before(() => {
        cy.login(adminUser);
        cy.visit('/#/faelle');
        cy.getByData('page-title').contains('Alle Fälle');
        cy.getByData('page-menu').click();
        cy.getByData('action-admin.testdaten').click();
        cy.getByData('gemeinde').click();
        cy.getByData('gemeinde.London').click();
        cy.getByData('periode').click();
        cy.getByData('periode.2022/23').click();
        cy.getByData('creationType.verfuegt').find('label').click();
        cy.getByData('testfall-1').click();
        cy.intercept('GET', '**/dossier/id//**').as('opengesuch');
        cy.get('[data-test="dialog-link"]', {timeout: Cypress.config('defaultCommandTimeout') * 10}).click();
        cy.wait('@opengesuch');
        cy.getByData('fallnummer').invoke('text').then(value => {
            fallnummer = value;
            cy.getByData('fallnummer').should('contain.text', fallnummer);
        });
    });

    it('should create a prefilled new Testfall Antrag and mutationsmeldung', () => {
        cy.login(userSB);
        cy.visit('/#/faelle');
        cy.getByData('antrag-entry#' + fallnummer).click();
        cy.getByData('sidenav.BETREUUNG').click();
        cy.getByData('container.betreuung#0').click();
        cy.getByData('mutationsmeldung-erstellen').click();
        cy.getByData('betreuungspensum-0').clear().type(monatlichesPensum);
        cy.getByData('monatliche-betreuungskosten#0').clear().type(monatlicheKosten);
        cy.getByData('betreuung-datum-ab#0').clear().type(startdatum);
        cy.getByData('betreuung-datum-bis#0').clear().type(enddatum);
        cy.getByData('mutationsmeldung-senden').click();
        cy.intercept("PUT", '**/mitteilungen/sendbetreuungsmitteilung').as('creatingMutationsmeldung');
        cy.getByData('container.confirm').click();
        cy.wait('@creatingMutationsmeldung');
    });

    it('should accept the Mutationsmeldung', () => {
        cy.login(adminUser);
        cy.visit('/#/faelle');
        cy.getByData('antrag-entry#' + fallnummer).click();
        cy.getByData('toolbar-mitteilungen').click();
        cy.getByData('container.mitteilung#0').click();
        cy.getByData('container.mitteilung#0', 'navigation-button').click();
        cy.intercept("PUT", '**/mitteilungen/applybetreuungsmitteilung/**').as('acceptMutationsmeldung');
        cy.getByData('container.confirm', 'navigation-button').click()
        cy.wait('@acceptMutationsmeldung');
    });

    it('should check if the Mutationsmeldung was accepted', () => {
        cy.login(adminUser);
        cy.visit('/#/faelle');
        cy.getByData('antrag-entry#' + fallnummer).first().click();
        cy.getByData('sidenav.BETREUUNG').click();
        cy.getByData('container.betreuung#1').click();
        cy.getByData('betreuungspensum-0').should('have.value', monatlichesPensum);
        cy.getByData('monatliche-betreuungskosten#0').should('have.value', monatlicheKosten);
        cy.getByData('betreuung-datum-ab#0').find("input").should('have.value', startdatum);
        cy.getByData('betreuung-datum-bis#0').find("input").should('have.value', enddatum);
    });
});
