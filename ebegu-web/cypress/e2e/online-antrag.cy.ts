import {AntragBeschaeftigungspensumPO, AntragBetreuungPO, AntragFamSitPO, AntragKindPO} from '@dv-e2e/page-objects';
import {FixtureFinSit} from '@dv-e2e/fixtures';

describe('Kibon - generate Testfälle [Online-Antrag]', () => {
    const userGS = 'Emma-Gerber';
    const admin = 'E-BEGU-Superuser';
    beforeEach(() => {
        cy.intercept({ resourceType: 'xhr' }, { log: false }); // don't log XHRs
    });

    // Delete Gesuch for Gesuchsteller if Existing
    it('delete existing gesuche for gesuchsteller', () => {
        cy.login(admin);
        cy.visit('#/testdaten');
        cy.getByData('gesuchsteller').click();
        cy.getByData('gesuchsteller.Emma Gerber').click();
        cy.getByData('delete-gesuche').click();
    });

    it ('should registrade new user for bg', () => {
        cy.login(userGS);
        cy.visit('/#/registration-abschliessen');
        cy.getByData('bg-beantragen').click();
        cy.getByData('gemeinde').select('London');

        cy.intercept('GET', '**/gemeinde/gemeindeRegistrierung/**').as('goingToRegistrierungAbschliessen');
        cy.getByData('registrieren').click();
        cy.wait('@goingToRegistrierungAbschliessen');
        cy.getByData('registrierung-abschliessen').click();
    });

    it ('should correctly create a new online antrag', () => {
        //INIT Antrag
        {
            cy.login(userGS);
            cy.visit('/#/dossier/gesuchstellerDashboard');
            cy.getByData('container.periode.2023/24', 'navigation-button').click();
            cy.getByData('container.navigation-save', 'navigation-button').click();
        }

        //Familiensituation
        {
            AntragFamSitPO.fillFamiliensituationForm('withValid');
            clickSave();
        }

        //Kinder
        {
            AntragKindPO.createNewKind();
            AntragKindPO.fillKindForm('withValid');
            cy.getByData('container.navigation-save', 'navigation-button').click();

            AntragKindPO.createNewKind();
            cy.getByData(`geschlecht.radio-value.WEIBLICH`).click();
            cy.getByData('vorname').type('Mia');
            cy.getByData('nachname').type('Test');
            cy.getByData('geburtsdatum').type('01.01.2019');
            cy.getByData('container.obhut-alternierend-ausueben', 'radio-value.nein').find('label').click();
            cy.getByData('container.ergaenzende-betreuung-beide', 'radio-value.nein').find('label').click();
            cy.getByData('container.navigation-save', 'navigation-button').click();

            cy.getByData('page-title').should('include.text', 'Kinder');

            cy.intercept('POST', '**/wizard-steps').as('goingToBetreuung');
            cy.getByData('container.navigation-save', 'navigation-button').click();
            cy.wait('@goingToBetreuung');
        }
        //Betreuung
        {
            //KITA
            {
                AntragBetreuungPO.createNewBetreuung();
                AntragBetreuungPO.fillOnlineKitaBetreuungsForm('withValid');
                AntragBetreuungPO.fillKeinePlatzierung();
                AntragBetreuungPO.fillErweiterteBeduerfnisse();
                AntragBetreuungPO.platzBestaetigungAnfordern();
            }

            //TFO
            {
                AntragBetreuungPO.createNewBetreuung();
                AntragBetreuungPO.fillOnlineTfoBetreuungsForm('withValid');
                AntragBetreuungPO.fillKeinePlatzierung();
                cy.getByData('erweiterteBeduerfnisse.radio-value.nein').click();
                AntragBetreuungPO.platzBestaetigungAnfordern();


            }

            cy.getByData('page-title').should('include.text', 'Betreuung');
            cy.intercept('GET', '**/erwerbspensen/required/**').as('goingToBeschaeftigungspensum');
            cy.getByData('container.navigation-save', 'navigation-button').click();
            cy.wait('@goingToBeschaeftigungspensum');
        }

        // BESCHAEFTIGUNGSPENSUM
        {
            AntragBeschaeftigungspensumPO.createBeschaeftigungspensum('GS1', 'withValid');
            AntragBeschaeftigungspensumPO.createBeschaeftigungspensum('GS2', 'withValid');

            cy.getByData('container.navigation-save', 'navigation-button').click();
        }

        // FINANZIELLE VERHAELTNISSE
        {
            // Config
            {
                cy.getByData('sozialhilfeBezueger.radio-value.nein').click();
                cy.getByData('iban').type('CH3908704016075473007');
                cy.getByData('kontoinhaber').type('vorname-test1 nachname-test-1');

                cy.intercept('POST', '**/finanzielleSituation/calculateTemp').as('goingToFinSitGS1');
                cy.getByData('container.navigation-save', 'navigation-button').click();
                cy.wait('@goingToFinSitGS1');
            }

            // Finanzielle Situation - GS 1
            {
                FixtureFinSit.withValid(({ GS1 }) => {
                    cy.getByData('nettolohn').find('input').type(GS1.nettolohn);
                    cy.getByData('familienzulage').find('input').type(GS1.familienzulage);
                    cy.getByData('ersatzeinkommen').find('input').type(GS1.ersatzeinkommen);
                    cy.getByData('erhaltene-alimente').find('input').type(GS1.erhalteneAlimente);
                    cy.getByData('brutto-ertraege-vermoegen').find('input').type(GS1.bruttoErtraegeVermoegen);
                    cy.getByData('nettoertraege_erbengemeinschaften').find('input').type(GS1.nettoertraegeErbengemeinschaften);
                    cy.getByData('einkommenInVereinfachtemVerfahrenAbgerechnet1.radio-value.nein').click();
                    cy.getByData('geleistete-alimente').find('input').type(GS1.geleisteteAlimente);
                    cy.getByData('abzug-schuldzinsen').find('input').type(GS1.abzugSchuldzinsen);
                    cy.getByData('gewinnungskosten').find('input').type(GS1.gewinnungskosten);
                });

                cy.intercept('POST', '**/finanzielleSituation/calculateTemp').as('goingToFinSitGS2');
                cy.getByData('container.navigation-save', 'navigation-button').click();
                cy.wait('@goingToFinSitGS2');
            }

            // Finanzielle Situation - GS 2
            {
                FixtureFinSit.withValid(({ GS2 }) => {
                    cy.getByData('nettolohn').find('input').type(GS2.nettolohn);
                    cy.getByData('familienzulage').find('input').type(GS2.familienzulage);
                    cy.getByData('ersatzeinkommen').find('input').type(GS2.ersatzeinkommen);
                    cy.getByData('erhaltene-alimente').find('input').type(GS2.erhalteneAlimente);
                    cy.getByData('brutto-ertraege-vermoegen').find('input').type(GS2.bruttoErtraegeVermoegen);
                    cy.getByData('nettoertraege_erbengemeinschaften').find('input').type(GS2.nettoertraegeErbengemeinschaften);
                    cy.getByData('einkommenInVereinfachtemVerfahrenAbgerechnet1.radio-value.nein').click();
                    cy.getByData('geleistete-alimente').find('input').type(GS2.geleisteteAlimente);
                    cy.getByData('abzug-schuldzinsen').find('input').type(GS2.abzugSchuldzinsen);
                    cy.getByData('gewinnungskosten').find('input').type(GS2.gewinnungskosten);
                });

                cy.intercept('POST', '**/finanzielleSituation/calculateTemp').as('goingToResultate');
                cy.getByData('container.navigation-save', 'navigation-button').click();
                cy.wait('@goingToResultate');
            }

            // Resultate
            {
                FixtureFinSit.withValid(({ Resultate }) => {
                    cy.getByData('einkommenBeiderGesuchsteller').find('input').should('have.value', Resultate.einkommenBeiderGesuchsteller);
                    cy.getByData('bruttovermoegen1').find('input').type(Resultate.bruttovermoegen1);
                    cy.getByData('bruttovermoegen2').find('input').type(Resultate.bruttovermoegen2);
                    cy.getByData('schulden1').find('input').type(Resultate.schulden1);
                    cy.getByData('schulden2').find('input').type(Resultate.schulden2);
                    cy.getByData('nettovermoegenFuenfProzent').find('input').should('have.value', Resultate.nettovermoegenFuenfProzent);
                    cy.getByData('anrechenbaresEinkommen').find('input').should('have.value', Resultate.anrechenbaresEinkommen);
                    cy.getByData('abzuegeBeiderGesuchsteller').find('input').should('have.value', Resultate.abzuegeBeiderGesuchsteller);
                    cy.getByData('massgebendesEinkVorAbzFamGr').find('input').should('have.value', Resultate.massgebendesEinkVorAbzFamGr);
                });
            }

            cy.intercept('GET', '**/einkommensverschlechterung/minimalesMassgebendesEinkommen/**').as('goingToEinkommensverschlechterung');
            cy.getByData('container.navigation-save', 'navigation-button').click();
            cy.wait('@goingToEinkommensverschlechterung');
        }

    });
});

function clickSave() {
    cy.getByData('container.navigation-save', 'navigation-button').should('not.have.a.property', 'disabled');
    cy.getByData('container.navigation-save', 'navigation-button').click();
}
