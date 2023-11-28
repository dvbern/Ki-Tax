import {getUser} from "@dv-e2e/types";
import {visit} from "jsonc-parser";

describe('Kibon - generate Testfälle [Gemeinde Sachbearbeiter]', () => {
    const userSB = getUser('[3-SB-Institution-Kita-Brünnen] Sophie Bergmann');
    const adminUser = getUser('[1-Superadmin] E-BEGU Superuser');
    const monatlichesPensum = '60';
    const monatlicheKosten = '1000';
    const startdatum = '01.04.2023';
    const enddatum = '30.06.2023';

    it('should create a prefilled new Testfall Antrag and mutationsmeldung', () => {
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
        cy.get('[data-test="dialog-link"]', { timeout: 20000 }).click();
        cy.login(userSB);
        cy.visit('/#/faelle');
        cy.getByData('antrag-entry#0').click();
        cy.getByData('sidenav.BETREUUNG').click();
        cy.getByData('betreuung#0').click();
        cy.getByData('mutationsmeldung-erstellen').click();
        cy.getByData('betreuungspensum-0').clear().type(monatlichesPensum);
        cy.getByData('monatliche-betreuungskosten#0').clear().type(monatlicheKosten);
        cy.getByData('betreuung-datum-ab#0').clear().type(startdatum);
        cy.getByData('betreuung-datum-bis#0').clear().type(enddatum);
        cy.getByData('mutationsmeldung-senden').click();
        cy.intercept("PUT", '**/mitteilungen/sendbetreuungsmitteilung').as('creatingMutationsmeldung');
        cy.get('[data-test="container.confirm"]').click();
        cy.wait('@creatingMutationsmeldung');
    });

    it('should accept the Mutationsmeldung', () => {
        cy.login(adminUser);
        cy.visit('/#/faelle');
        cy.getByData('antrag-entry#0').click();
        cy.getByData('toolbar-mitteilungen').click();
        cy.getByData('container.mitteilung#0').click();
        cy.get('[data-test="container.mitteilung#0"] [data-test="navigation-button"]').click();
        cy.intercept("PUT", '**/mitteilungen/applybetreuungsmitteilung/**').as('acceptMutationsmeldung');
        cy.get('[data-test="container.confirm"] [data-test="navigation-button"]').click();
        cy.wait('@acceptMutationsmeldung');
    });

    it('should check if the Mutationsmeldung was accepted', () => {
        cy.login(adminUser);
        cy.visit('/#/faelle');
        cy.getByData('antrag-entry#0').click();
        cy.getByData('sidenav.BETREUUNG').click();
        cy.getByData('betreuung#1').click();
        cy.getByData('betreuungspensum-0').should('have.value', monatlichesPensum);
        cy.getByData('monatliche-betreuungskosten#0').should('have.value', monatlicheKosten);
        cy.getByData('betreuung-datum-ab#0').find("input").should('have.value', startdatum);
        cy.getByData('betreuung-datum-bis#0').find("input").should('have.value', enddatum);
    });
});
