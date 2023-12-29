import {TestFaellePO} from '@dv-e2e/page-objects';
import {getUser} from "@dv-e2e/types";
import {SidenavPO} from '../page-objects/antrag/sidenav.po';

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
        TestFaellePO.createPapierTestfall({
            testFall: 'testfall-1',
            betreuungsstatus: 'verfuegt',
            gemeinde: 'London',
            periode: '2022/23'
        });

        cy.getByData('fallnummer').invoke('text').then(value => {
            fallnummer = value;
            cy.getByData('fallnummer').should('contain.text', fallnummer);
        });
    });

    it('should create a prefilled new Testfall Antrag and mutationsmeldung', () => {
        cy.login(userSB);
        cy.visit('/#/faelle');
        cy.getByData('antrag-entry#' + fallnummer).click();
        SidenavPO.goTo('BETREUUNG');
        cy.getByData('container.betreuung#0').click();
        cy.getByData('mutationsmeldung-erstellen').click();
        cy.getByData('betreuungspensum-0').clear().type(monatlichesPensum);
        cy.getByData('monatliche-betreuungskosten#0').clear().type(monatlicheKosten);
        cy.getByData('betreuung-datum-ab#0').find('input').clear().type(startdatum);
        cy.getByData('betreuung-datum-bis#0').find('input').clear().type(enddatum);
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
        SidenavPO.goTo('BETREUUNG');
        cy.getByData('container.betreuung#1').click();
        cy.getByData('betreuungspensum-0').should('have.value', monatlichesPensum);
        cy.getByData('monatliche-betreuungskosten#0').should('have.value', monatlicheKosten);
        cy.getByData('betreuung-datum-ab#0').find("input").should('have.value', startdatum);
        cy.getByData('betreuung-datum-bis#0').find("input").should('have.value', enddatum);
    });
});
