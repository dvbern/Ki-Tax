import {NavigationPO, TestFaellePO} from '@dv-e2e/page-objects';
import { getUser, normalizeUser } from '@dv-e2e/types';
import {SidenavPO} from '../page-objects/antrag/sidenav.po';

describe('Kibon - mutationen [Gesuchsteller]', () => {
    const userSuperadmin = getUser('[1-Superadmin] E-BEGU Superuser');
    const userGS = getUser('[5-GS] Michael Berger');
    const userSB = getUser('[6-L-Admin-Gemeinde] Gerlinde Bader');
    let gesuchUrl: string;
    before(() => {
        cy.intercept({ resourceType: 'xhr' }, { log: false }); // don't log XHRs
        cy.login(userSuperadmin);
        cy.visit('/#/faelle');

        TestFaellePO.createOnlineTestfall({
            testFall: 'testfall-2',
            periode: '2023/24',
            gemeinde: 'London',
            besitzerin: '[5-GS] Michael Berger',
            betreuungsstatus: 'verfuegt'
        });

        cy.url().then((url) => {
            const parts = new URL(url);
            gesuchUrl = `${parts.pathname}${parts.hash}`;
        });
    });

    it('should be possible to create a mutation with Umzug and Abwesenheit', () => {
        cy.login(userGS);
        cy.visit(gesuchUrl);

        cy.intercept('GET', '**/gemeinde/stammdaten/lite/**').as('mutationReady');
        cy.getByData('toolbar.antrag-mutieren').click();
        cy.wait('@mutationReady');

        NavigationPO.getSaveAndNextButton().contains('Erstellen').click();
        cy.url().should('not.contain', 'CREATE_NEW_MUTATION/ONLINE');

        SidenavPO.goTo('UMZUG');
        cy.getByData('container.hinzufuegen', 'navigation-button').click();

        cy.getByData('container.umzug-0', 'adresseStrasse').type('Test');
        cy.getByData('container.umzug-0', 'adresseHausnummer').type('2');
        cy.getByData('container.umzug-0', 'adressePlz').type('3000');
        cy.getByData('container.umzug-0', 'adresseOrt').type('Bern');
        cy.getByData('container.umzug-0', 'gueltigAb').find('input').type('01.11.2023');
        cy.intercept('GET', '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**').as('goingToAbwesenheit');
        NavigationPO.saveAndGoNext();
        cy.wait('@goingToAbwesenheit');

        cy.getByData('ABWESENHEIT').click();
        cy.getByData('container.erfassen', 'navigation-button').click();
        cy.getByData('kind').select('Tamara Feutz - Weissenstein');
        cy.getByData('abwesenheit-von').find('input').type('01.10.2023');
        cy.getByData('abwesenheit-bis').find('input').type('30.11.2023');

        cy.intercept('GET', '**/gesuche/ausserordentlicheranspruchpossible/**').as('abwesenheitSaved');
        NavigationPO.saveAndGoNext();
        cy.wait('@abwesenheitSaved');

        SidenavPO.goTo('FREIGABE');
        cy.getByData('container.freigeben', 'navigation-button').click();
        cy.getByData('container.confirm', 'navigation-button').click();

        cy.changeLogin(userSB);
        cy.visit(gesuchUrl);

        cy.getByData('toolbar.antrag').click();
        cy.intercept('GET', '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**').as('goToLatestMutation');
        cy.getByData('antrag#1').click();
        cy.wait('@goToLatestMutation');

        SidenavPO.goTo('VERFUEGEN');
        cy.getByData('finSitStatus.radio-value.AKZEPTIERT').click();

        cy.getByData('container.geprueft', 'navigation-button').click();
        cy.intercept('GET', '**/verfuegung/calculate/**').as('checkGeprueft');
        cy.getByData('container.confirm', 'navigation-button').click();
        cy.wait('@checkGeprueft');

        cy.getByData('container.verfuegen', 'navigation-button').click();
        cy.getByData('container.confirm', 'navigation-button').click();
        cy.intercept('GET', '**/verfuegung/calculate/**').as('checkVerfuegen');
        cy.wait('@checkVerfuegen');

        cy.intercept('GET', '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**').as('openingVerfuegung');
        cy.getByData('verfuegung#0-0').click();
        cy.wait('@openingVerfuegung');
        cy.getByData('container.zeitabschnitt#3', 'verguenstigungOhneBeruecksichtigungVollkosten').should('have.text', '0.00');
        cy.getByData('verfuegungs-bemerkungen-kontrolliert').click();
        cy.getByData('container.verfuegen', 'navigation-button').click();
        cy.intercept('PUT', '**/verfuegung/verfuegen/**').as('verfuegungVerfuegen');
        cy.getByData('container.confirm', 'navigation-button').click();
        cy.wait('@verfuegungVerfuegen');
        cy.getByData('container.cancel', 'navigation-button').click();
        cy.getByData('verfuegung#1-0').click();
        cy.getByData('container.verfuegen-verzichten', 'navigation-button').click();
        cy.intercept('POST', '**/verfuegung/schliessenOhneVerfuegen/**').as('ohneVerfuegung');
        cy.getByData('container.confirm', 'navigation-button').click();
        cy.wait('@ohneVerfuegung')

        cy.getByData('verfuegung#0-0', 'betreuungs-status').should('include.text', 'Verfügt');
        cy.getByData('verfuegung#1-0', 'betreuungs-status').should('include.text', 'Geschlossen ohne Verfügung');

        cy.getByData('toolbar.antrag-mutieren').click();
        cy.getByData('fall-creation-eingangsdatum').find('input').type('01.05.2023');
        cy.intercept('GET', '**/gesuche/dossier/**').as('createNewMutation');
        NavigationPO.saveAndGoNext();
        cy.wait('@createNewMutation');

        cy.getByData('toolbar.antrag').click();
        cy.get('[data-test^="antrag#').should('have.length', 3);
        cy.closeMaterialOverlay();

        cy.getByData('toolbar.antrag-loeschen').click();
        cy.intercept('GET', '**/gesuchsperioden/gemeinde/**').as('deletingMutation');
        cy.getByData('container.confirm', 'navigation-button').click();
        cy.wait('@deletingMutation');

        cy.getByData('toolbar.antrag').click();
        cy.get('[data-test^="antrag#').should('have.length', 2);
        cy.closeMaterialOverlay();
    });
});
