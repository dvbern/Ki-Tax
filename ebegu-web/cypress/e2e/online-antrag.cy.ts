import {
    EinkommensverschlechterungPO,
    AntragBeschaeftigungspensumPO,
    AntragBetreuungPO,
    AntragKindPO,
    AntragFamSitPO,
} from '@dv-e2e/page-objects';
import { FixtureFamSit, FixtureFinSit } from '@dv-e2e/fixtures';
import { getUser } from '@dv-e2e/types';

describe('Kibon - generate Testfälle [Online-Antrag]', () => {
    // TODO: Change userKita to Agnes Krause once "Kitas & Tagis Stadt Bern" can view the TFO of this test
    const userKita = getUser('[1-Superadmin] E-BEGU Superuser');
    const userGS = getUser('[5-GS] Emma Gerber');
    const admin = getUser('[1-Superadmin] E-BEGU Superuser');
    const gesuchsPeriode = { ganze: '2023/24', anfang: '2023', ende: '2024' };

    before(() => {
        cy.intercept({ resourceType: 'xhr' }, { log: false }); // don't log XHRs
        cy.login(admin);
        cy.intercept('GET', '**/benutzer/gesuchsteller').as('loadingGesuchsteller');
        cy.visit('#/testdaten');
        cy.wait('@loadingGesuchsteller');
        cy.getByData('gesuchsteller-faelle-loeschen').click();
        cy.getByData('gesuchsteller.Emma Gerber').click();
        cy.getByData('delete-gesuche').click();
    });

    it('should register new user for bg', () => {
        cy.login(userGS);
        cy.visit('/#/registration-abschliessen');
        cy.getByData('bg-beantragen').click();
        cy.getByData('gemeinde').select('London');

        cy.intercept('GET', '**/gemeinde/gemeindeRegistrierung/**').as('goingToRegistrierungAbschliessen');
        cy.getByData('registrieren').click();
        cy.wait('@goingToRegistrierungAbschliessen');
        cy.getByData('registrierung-abschliessen').click();
    });

    it('should correctly create a new online antrag', () => {
        const famsitDataset = 'withValid';

        const openAntrag = () => {
            cy.login(userGS);
            cy.visit('/#/dossier/gesuchstellerDashboard');
            cy.intercept('GET', '**/dossier/fall/**').as('openingAntrag');
            cy.getByData(`container.periode.${gesuchsPeriode.ganze}`, 'navigation-button').click();
            cy.wait('@openingAntrag');
        }

        //INIT Antrag
        {
            openAntrag();
            clickSave();
        }

        //Familiensituation
        {
            cy.getByData('familienstatus.VERHEIRATET').find('label');
            cy.url()
                .then((url) => /familiensituation\/(.*)$/.exec(url)[1])
                .as('antragsId');
            AntragFamSitPO.fillFamiliensituationForm('withValid');
            clickSave();
        }

        //Kinder
        {
            AntragKindPO.createNewKind();
            AntragKindPO.fillKindForm('withValid');
            clickSave();

            AntragKindPO.createNewKind();
            cy.getByData(`geschlecht.radio-value.WEIBLICH`).click();
            cy.getByData('vorname').type('Mia');
            cy.getByData('nachname').type('Test');
            cy.getByData('geburtsdatum').find('input').type('01.01.2019');
            cy.getByData('container.obhut-alternierend-ausueben', 'radio-value.nein').find('label').click();
            cy.getByData('container.ergaenzende-betreuung-beide', 'radio-value.nein').find('label').click();
            clickSave();

            cy.getByData('page-title').should('include.text', 'Kinder');

            cy.intercept('POST', '**/wizard-steps').as('goingToBetreuung');
            clickSave();
            cy.wait('@goingToBetreuung');
        }

        //Betreuung
        {
            //KITA
            {
                AntragBetreuungPO.createNewBetreuung();
                AntragBetreuungPO.fillOnlineKitaBetreuungsForm(famsitDataset);
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
            clickSave();
            cy.wait('@goingToBeschaeftigungspensum');
        }

        // BESCHAEFTIGUNGSPENSUM
        {
            AntragBeschaeftigungspensumPO.createBeschaeftigungspensum('GS1', 'withValid');
            AntragBeschaeftigungspensumPO.createBeschaeftigungspensum('GS2', 'withValid');

            clickSave();
        }

        // FINANZIELLE VERHAELTNISSE
        {
            // Config
            {
                cy.getByData('sozialhilfeBezueger.radio-value.nein').click();
                cy.getByData('iban').type('CH3908704016075473007');
                cy.getByData('kontoinhaber').type('vorname-test1 nachname-test-1');

                cy.intercept('POST', '**/finanzielleSituation/calculateTemp').as('goingToFinSitGS1');
                clickSave();
                cy.wait('@goingToFinSitGS1');
            }

            // Finanzielle Situation - GS 1
            {
                cy.getByData('steuerdatenzugriff.radio-value.nein').click();

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

                cy.getByData('automatischePruefung.radio-value.nein').click();

                cy.intercept('POST', '**/finanzielleSituation/calculateTemp').as('goingToFinSitGS2');
                clickSave();
                cy.wait('@goingToFinSitGS2');
            }

            // Finanzielle Situation - GS 2
            {
                cy.getByData('steuerdatenzugriff.radio-value.nein').click();

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
                clickSave();
                cy.wait('@goingToResultate');
            }

            // Resultate
            {
                FixtureFinSit.withValid(({ Resultate }) => {
                    cy.getByData('einkommenBeiderGesuchsteller')
                        .find('input')
                        .should('have.value', Resultate.einkommenBeiderGesuchsteller);
                    cy.getByData('bruttovermoegen1').find('input').type(Resultate.bruttovermoegen1);
                    cy.getByData('bruttovermoegen2').find('input').type(Resultate.bruttovermoegen2);
                    cy.getByData('schulden1').find('input').type(Resultate.schulden1);
                    cy.getByData('schulden2').find('input').type(Resultate.schulden2);
                    cy.getByData('nettovermoegenFuenfProzent')
                        .find('input')
                        .should('have.value', Resultate.nettovermoegenFuenfProzent);
                    cy.getByData('anrechenbaresEinkommen').find('input').should('have.value', Resultate.anrechenbaresEinkommen);
                    cy.getByData('abzuegeBeiderGesuchsteller')
                        .find('input')
                        .should('have.value', Resultate.abzuegeBeiderGesuchsteller);
                    cy.getByData('massgebendesEinkVorAbzFamGr')
                        .find('input')
                        .should('have.value', Resultate.massgebendesEinkVorAbzFamGr);
                });
            }
            cy.intercept('GET', '**/einkommensverschlechterung/minimalesMassgebendesEinkommen/**').as(
                'goingToEinkommensverschlechterung'
            );
            clickSave();
            cy.wait('@goingToEinkommensverschlechterung');
        }

        // EINKOMMENSVERSCHLECHTERUNG
        {
            cy.getByData('einkommensverschlechterung.radio-value.ja').click();
            cy.getByData('ekv-fuer-basis-jahr-plus#1').click();
            cy.getByData('ekv-fuer-basis-jahr-plus#2').click();
            cy.waitForRequest('POST', '**/einkommensverschlechterung/calculateTemp/1', () => {
                clickSave();
            });
            cy.groupBy('Einkommensverschlechterung - Jahr 1', () => {
                cy.getByData('page-title').should('include.text', gesuchsPeriode.anfang);
                EinkommensverschlechterungPO.fillEinkommensverschlechterungForm('withValid', 'jahr1', 'GS1');
                clickSave();
                FixtureFamSit[famsitDataset](({ GS2 }) => {
                    cy.getByData('page-title').should('include.text', `${GS2.vorname} ${GS2.nachname}`);
                });
                EinkommensverschlechterungPO.fillEinkommensverschlechterungForm('withValid', 'jahr1', 'GS2');
                clickSave();
            });
            cy.groupBy('Einkommensverschlechterung - Jahr 2', () => {
                cy.getByData('page-title').should('include.text', gesuchsPeriode.ende);
                EinkommensverschlechterungPO.fillEinkommensverschlechterungForm('withValid', 'jahr2', 'GS1');
                cy.waitForRequest('POST', '**/einkommensverschlechterung/calculateTemp/2', () => {
                    clickSave();
                });
                FixtureFamSit[famsitDataset](({ GS2 }) => {
                    cy.getByData('page-title').should('include.text', `${GS2.vorname} ${GS2.nachname}`);
                });
                EinkommensverschlechterungPO.fillEinkommensverschlechterungForm('withValid', 'jahr2', 'GS2');
                cy.waitForRequest('POST', '**/finanzielleSituation/calculateTemp', () => {
                    clickSave();
                });
            });
            cy.groupBy('Resultate', () => {
                cy.getByData('page-title').should('include.text', gesuchsPeriode.anfang);
                EinkommensverschlechterungPO.fillResultateForm('withValid', 'jahr1');
                clickSave();

                cy.getByData('page-title').should('include.text', gesuchsPeriode.ende);
                EinkommensverschlechterungPO.fillResultateForm('withValid', 'jahr2');
            });
            cy.intercept('GET', '**/dokumente/**').as('goingToDokumente');
            clickSave();
            cy.wait('@goingToDokumente');
        }

        // DOKUMENTE
        {
            // Test upload file
            cy.fixture('documents/small.png').as('smallPng');

            // Upload the file on every <input type=file>, Angular JS file upload makes specific upload difficult:
            // https://github.com/abramenal/cypress-file-upload/tree/main/recipes/angularjs-ng-file-upload
            // https://github.com/danialfarid/ng-file-upload/issues/1140
            // https://github.com/danialfarid/ng-file-upload/issues/1167
            cy.get('input[type="file"][tabindex=0]').each(($el, index) => {
                const upload = `fileUpload#${index}`;
                cy.intercept('POST', '**/upload').as(upload);
                cy.wrap($el).selectFile(
                    {
                        contents: '@smallPng',
                        fileName: `small-${index}.png`,
                    },
                    { force: true }
                );
                return cy.wait(`@${upload}`);
            });
            cy.intercept('POST', '**/wizard-steps').as('goingToVerfuegungen');
            clickSave();
            cy.wait('@goingToVerfuegungen');
        }

        // PLATZBESTAETIGUNG mit Kita SB
        // !!!!!! - New User - !!!!!!
        {
            cy.changeLogin(userKita);

            const goToBetreuungen = () => {
                cy.get('@antragsId').then((antragsId) => cy.visit(`/#/gesuch/familiensituation/${antragsId}`));

                cy.intercept('GET', '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**').as(
                    `goingToBetreuungWith${userKita}`
                );
                cy.getByData('sidenav.BETREUUNG').click();
                cy.wait(`@goingToBetreuungWith${userKita}`);
            };

            goToBetreuungen();
            cy.getByData('container.betreuung#0').click();
            cy.getByData('korrekte-kosten-bestaetigung').click();
            cy.getByData('container.platz-bestaetigen', 'navigation-button').click();
            cy.waitForRequest('GET', '**/search/pendenzenBetreuungen', () => {
                cy.getByData('container.confirm', 'navigation-button').click();
            });

            goToBetreuungen();
            cy.waitForRequest('GET', '**/fachstellen/erweiterteBetreuung/**', () => {
                cy.getByData('container.betreuung#1').click();
            });
            cy.getByData('grund-ablehnung').click();
            cy.getByData('grund-ablehnung').type('Ein sehr legitimer Grund der hier nicht weiter aufgeführt wird.');
            cy.getByData('grund-ablehnung').should('have.value', 'Ein sehr legitimer Grund der hier nicht weiter aufgeführt wird.');
            cy.waitForRequest('PUT', '**/betreuungen/abweisen', () => {
                cy.getByData('container.platz-abweisen', 'navigation-button').click();
            });
        }

        cy.changeLogin(userGS);
        openAntrag();
        // // !!!!!! - changed back to previous user - !!!!!!


        // FREIGABE
        {
            cy.getByData('sidenav.BETREUUNG').click();
            cy.getByData('container.betreuung#1', 'betreuungs-status').should('include.text', 'Abgewiesen');
            cy.getByData('container.betreuung#1', 'container.delete', 'navigation-button').click();
            cy.waitForRequest('DELETE', '**/betreuungen/**', () => {
                cy.getByData('container.confirm', 'navigation-button').click();
            });

            cy.getByData('sidenav.FREIGABE').click();
            cy.getByData('container.freigeben', 'navigation-button').click();

            cy.window()
                .then((win) => {
                    // Listen for download events (window.open)
                    const promise = getDownloadUrl(win);

                    // Continue once the download URL has been created
                    return cy
                        .getByData('container.confirm', 'navigation-button')
                        .click()
                        .then(() => promise);
                })
                .as('downloadUrl');
        }

        // VERFUEGUNG
        {
            // cy.get('@antragsId').then((antragsId) => cy.visit(`/#/gesuch/verfuegen/${antragsId}`));
            //
            // cy.getByData('verfuegung#0').click();
            // cy.getByData('container.zeitabschnitt#5', 'betreuungspensumProzent').should('include.text', '25%');
            // cy.getByData('container.zeitabschnitt#6', 'betreuungspensumProzent').should('include.text', '25%');
            // cy.getByData('container.zeitabschnitt#7', 'betreuungspensumProzent').should('include.text', '25%');
            // cy.getByData('container.zeitabschnitt#8', 'betreuungspensumProzent').should('include.text', '25%');
            // cy.getByData('container.zeitabschnitt#9', 'betreuungspensumProzent').should('include.text', '25%');
            // cy.getByData('container.zeitabschnitt#10', 'betreuungspensumProzent').should('include.text', '25%');
            // cy.getByData('container.zeitabschnitt#11', 'betreuungspensumProzent').should('include.text', '25%');
        }
    });
});

function clickSave() {
    cy.getByData('container.navigation-save', 'navigation-button').should('not.have.a.property', 'disabled');
    cy.getByData('container.navigation-save', 'navigation-button').click();
}

/**
 * The download is managed by a window.open with target=_blank which then triggers a new window.open to obtain the download URL
 */
const getDownloadUrl = (win: Cypress.AUTWindow) => {
    return new Promise((resolve) => {
        // Mock the first window.open call to render the download preparation page into an iframe
        cy.stub(win, 'open').callsFake((url) => {
            const iframe = win.document.createElement('iframe');
            iframe.src = url;
            win.document.body.appendChild(iframe);
            const newWin = iframe.contentWindow;

            // Mock the second window.open to obtain the download url
            cy.stub(newWin, 'open').callsFake((url) => {
                resolve(url);

                return newWin;
            });

            return newWin;
        });
    });
};
