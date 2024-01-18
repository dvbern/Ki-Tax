import {
    AbwesenheitPo, AntragCreationPO,
    ConfirmDialogPO,
    DossierToolbarPO,
    FreigabePO,
    NavigationPO,
    TestFaellePO,
    UmzugPO, VerfuegenPO,
} from '@dv-e2e/page-objects';
import { getUser, normalizeUser } from '@dv-e2e/types';
import {SidenavPO} from '../page-objects/antrag/sidenav.po';
import {VerfuegungPO} from '../page-objects/antrag/verfuegung.po';

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
            gesuchUrl = `/${parts.hash}`;
        });
    });

    it('should be possible to create a mutation with Umzug and Abwesenheit', () => {
        cy.login(userGS);
        cy.visit(gesuchUrl);

        cy.intercept('GET', '**/gemeinde/stammdaten/lite/**').as('mutationReady');
        DossierToolbarPO.getAntragMutieren().click();
        cy.wait('@mutationReady');

        NavigationPO.getSaveAndNextButton().contains('Erstellen').click();
        cy.url().should('not.contain', 'CREATE_NEW_MUTATION/ONLINE');

        SidenavPO.goTo('UMZUG');
        UmzugPO.getUmzugHinzufuegenButton().click();

        UmzugPO.getUmzugStrasse(0).type('Test');
        UmzugPO.getUmzugHausnummer(0).type('2');
        UmzugPO.getUmzugPlz(0).type('3000');
        UmzugPO.getUmzugOrt(0).type('Bern');
        UmzugPO.getUmzugGueltigAb(0).find('input').type('01.11.2023');
        cy.intercept('GET', '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**').as('goingToAbwesenheit');
        NavigationPO.saveAndGoNext();
        cy.wait('@goingToAbwesenheit');

        SidenavPO.goTo('ABWESENHEIT');
        AbwesenheitPo.getAbwesenheitErfassenButton().click();
        AbwesenheitPo.getKind().select('Tamara Feutz - Weissenstein');
        AbwesenheitPo.getAbwesenheitAb().find('input').type('01.10.2023');
        AbwesenheitPo.getAbwesenheitBis().find('input').type('30.11.2023');

        cy.intercept('GET', '**/gesuche/ausserordentlicheranspruchpossible/**').as('abwesenheitSaved');
        NavigationPO.saveAndGoNext();
        cy.wait('@abwesenheitSaved');

        SidenavPO.goTo('FREIGABE');
        FreigabePO.getFreigebenButton().click();
        ConfirmDialogPO.getConfirmButton().click();
        cy.changeLogin(userSB);
        cy.visit(gesuchUrl);


        DossierToolbarPO.getAntraegeTrigger().click();
        cy.intercept('GET', '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**').as('goToLatestMutation');
        DossierToolbarPO.getAntrag(1).click();
        cy.wait('@goToLatestMutation');

        SidenavPO.goTo('VERFUEGEN');
        VerfuegenPO.getFinSitAkzeptiert('AKZEPTIERT').click();

        VerfuegenPO.getGeprueftButton().click();
        cy.intercept('GET', '**/verfuegung/calculate/**').as('checkGeprueft');
        ConfirmDialogPO.getConfirmButton().click();
        cy.wait('@checkGeprueft');

        VerfuegenPO.getVerfuegenStartenButton().click();
        ConfirmDialogPO.getConfirmButton().click();
        cy.intercept('GET', '**/verfuegung/calculate/**').as('checkVerfuegen');
        cy.wait('@checkVerfuegen');

        cy.intercept('GET', '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**').as('openingVerfuegung');
        VerfuegenPO.getVerfuegung(0,0).click();
        cy.wait('@openingVerfuegung');
        VerfuegungPO.getVerguenstigungOhneBeruecksichtigungVollkosten(3).should('have.text', '0.00');
        VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();
        VerfuegungPO.getVerfuegenButton().click();
        cy.intercept('PUT', '**/verfuegung/verfuegen/**').as('verfuegungVerfuegen');
        ConfirmDialogPO.getConfirmButton().click();
        cy.wait('@verfuegungVerfuegen');
        NavigationPO.getAbbrechenButton().click();
        VerfuegenPO.getVerfuegung(1,0).click();
        VerfuegungPO.getVerfuegenVerzichtenButton().click();
        cy.intercept('POST', '**/verfuegung/schliessenOhneVerfuegen/**').as('ohneVerfuegung');
        ConfirmDialogPO.getConfirmButton().click();
        cy.wait('@ohneVerfuegung');

        VerfuegenPO.getBetreuungsstatus(0,0).should('include.text', 'Verfügt');
        VerfuegenPO.getBetreuungsstatus(1,0).should('include.text', 'Geschlossen ohne Verfügung');

        DossierToolbarPO.getAntragMutieren().click();
        AntragCreationPO.getEingangsdatum().find('input').type('01.05.2023');
        cy.intercept('GET', '**/gesuche/dossier/**').as('createNewMutation');
        NavigationPO.saveAndGoNext();
        cy.wait('@createNewMutation');

        DossierToolbarPO.getAntraegeTrigger().click();
        DossierToolbarPO.getAllAntraegeInDropdown().should('have.length', 3);
        cy.closeMaterialOverlay();

        DossierToolbarPO.getAntragLoeschen().click();
        cy.intercept('GET', '**/gesuchsperioden/gemeinde/**').as('deletingMutation');
        ConfirmDialogPO.getConfirmButton().click();
        cy.wait('@deletingMutation');

        DossierToolbarPO.getAntraegeTrigger().click();
        DossierToolbarPO.getAllAntraegeInDropdown().should('have.length', 2);
        cy.closeMaterialOverlay();
    });
});
