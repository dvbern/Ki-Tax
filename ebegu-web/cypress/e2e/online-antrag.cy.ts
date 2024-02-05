import {
    EinkommensverschlechterungPO,
    AntragBeschaeftigungspensumPO,
    AntragBetreuungPO,
    AntragKindPO,
    AntragFamSitPO,
    NavigationPO,
    TestFaellePO,
    RegistrationAbschliessenPO,
    GesuchstellendeDashboardPO,
    FinanzielleSituationStartPO,
    FinanzielleSituationPO,
    FinanzielleSituationResultatePO,
    EinkommensverschlechterungResultatePO,
    DokumentePO,
    FallToolbarPO,
    ConfirmDialogPO,
    FreigabePO,
    AntragCreationPO,
    VerfuegungPO,
    VerfuegenPO,
    EinkommensverschlechterungInfoPO,
} from '@dv-e2e/page-objects';
import {FixtureEinkommensverschlechterung, FixtureFamSit, FixtureFinSit} from '@dv-e2e/fixtures';
import {getUser, TestPeriode} from '@dv-e2e/types';
import {GesuchstellendePO} from '../page-objects/antrag/gesuchstellende.po';
import {SidenavPO} from '../page-objects/antrag/sidenav.po';

describe('Kibon - generate Testfälle [Online-Antrag]', () => {
    const userSuperadmin = getUser('[1-Superadmin] E-BEGU Superuser');
    const userGemeinde = getUser('[6-L-SB-Gemeinde] Stefan Weibel');
    const userKita = getUser('[3-SB-Institution-Kita-Brünnen] Sophie Bergmann');
    const userTraegerschaft = getUser('[3-SB-Trägerschaft-Kitas-StadtBern] Agnes Krause');
    const userGS = getUser('[5-GS] Emma Gerber');
    const gesuchsPeriode: {ganze: TestPeriode, anfang: string, ende: string} = { ganze: '2023/24', anfang: '2023', ende: '2024' };

    before(() => {
        cy.intercept({ resourceType: 'xhr' }, { log: false }); // don't log XHRs
        cy.login(userSuperadmin);
        cy.intercept('GET', '**/benutzer/gesuchsteller').as('loadingGesuchsteller');
        cy.visit('#/testdaten');
        cy.wait('@loadingGesuchsteller');
        TestFaellePO.getGesuchstellerFaelleLoeschen().click();
        TestFaellePO.getGesuchstellerInToRemoveFaelle(userGS).click();
        cy.waitForRequest('DELETE', '**/testfaelle/testfallgs/*', () => {
            TestFaellePO.getGesucheLoeschenButton().click();
        });
    });

    it('should register new user for bg', () => {
        cy.login(userGS);
        cy.visit('/#/registration-abschliessen');
        RegistrationAbschliessenPO.getBGBeantragen().click();
        RegistrationAbschliessenPO.getGemeindeSelection().select('London');

        cy.waitForRequest('GET', '**/gemeinde/gemeindeRegistrierung/**', () => {
            RegistrationAbschliessenPO.getRegistrierenButton().click();
        });
        RegistrationAbschliessenPO.getRegistrierungAbschliessenButton().click();
    });

    it('should correctly create a new online antrag', () => {
        cy.viewport('iphone-8');
        const famsitDataset = 'withValid';

        const openAntrag = () => {
            cy.login(userGS);
            cy.visit('/#/dossier/gesuchstellerDashboard');
            cy.intercept('GET', '**/dossier/fall/**').as('openingAntrag');
            cy.waitForRequest('GET', '**/dossier/fall/**', () => {
                GesuchstellendeDashboardPO.getAntragBearbeitenButton(gesuchsPeriode.ganze).click();
            });
        };

        //INIT Antrag
        {
            openAntrag();
            clickSave();
        }

        //Familiensituation
        {
            AntragFamSitPO.getPageTitle().should('have.text', 'Familiensituation');
            cy.url()
                .then((url) => /familiensituation\/(.*)$/.exec(url)[1])
                .as('antragsId');
            AntragFamSitPO.fillFamiliensituationForm('withValid');
            clickSave();
            GesuchstellendePO.fillVerheiratet('withValid');
            clickSave();
        }

        //Kinder
        {
            AntragKindPO.createNewKind();
            AntragKindPO.fillKindForm('withValidBoy');
            clickSave();

            AntragKindPO.createNewKind();
            AntragKindPO.fillKindForm('withValidGirl');
            clickSave();

            AntragKindPO.getPageTitle().should('include.text', 'Kinder');

            cy.waitForRequest('POST', '**/wizard-steps', () => {
                clickSave();
            });
        }

        //Betreuung
        {
            //KITA
            {
                AntragBetreuungPO.createNewBetreuung(0);
                AntragBetreuungPO.fillOnlineKitaBetreuungsForm(famsitDataset, 'London', { mobile: true });
                AntragBetreuungPO.fillKeinePlatzierung();
                AntragBetreuungPO.fillErweiterteBeduerfnisse();
                AntragBetreuungPO.platzBestaetigungAnfordern();
            }

            //TFO
            {
                AntragBetreuungPO.createNewBetreuung(1);
                AntragBetreuungPO.fillOnlineTfoBetreuungsForm('withValid', 'London', { mobile: true });
                AntragBetreuungPO.fillKeinePlatzierung();
                AntragBetreuungPO.getHasErweiterteBeduerfnisse('nein').click();
                AntragBetreuungPO.platzBestaetigungAnfordern();
            }

            AntragBetreuungPO.getPageTitle().should('include.text', 'Betreuung');
            cy.waitForRequest('GET', '**/erwerbspensen/required/**', () => {
                clickSave();
            });
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
                FinanzielleSituationStartPO.fillFinanzielleSituationStartForm('withValid');
                FinanzielleSituationStartPO.saveForm();
            }

            // Finanzielle Situation - GS 1
            {
                FinanzielleSituationPO.getSteuerdatenzugriff('nein').click();
                FinanzielleSituationPO.fillFinanzielleSituationForm('withValid', 'GS1');
                FinanzielleSituationPO.getAutomatischePruefung('nein').click();
                FinanzielleSituationPO.saveFormAndGoNext();
            }

            // Finanzielle Situation - GS 2
            {
                FinanzielleSituationPO.getSteuerdatenzugriff('nein').click();
                FinanzielleSituationPO.fillFinanzielleSituationForm('withValid', 'GS2');
                FinanzielleSituationPO.saveFormAndGoNext();
            }

            // Resultate
            {
                FinanzielleSituationResultatePO.fillFinSitResultate('withValid');
                FixtureFinSit.withValid(({ Resultate }) => {
                    FinanzielleSituationResultatePO.getEinkommenBeiderGesuchsteller()
                        .find('input')
                        .should('have.value', Resultate.einkommenBeiderGesuchsteller);
                    FinanzielleSituationResultatePO.getNettovermoegenFuenfProzent()
                        .find('input')
                        .should('have.value', Resultate.nettovermoegenFuenfProzent);
                    FinanzielleSituationResultatePO.getAnrechenbaresEinkommen().find('input').should('have.value', Resultate.anrechenbaresEinkommen);
                    FinanzielleSituationResultatePO.getAbzuegeBeiderGesuchstellenden()
                        .find('input')
                        .should('have.value', Resultate.abzuegeBeiderGesuchsteller);
                    FinanzielleSituationResultatePO.getMassgebendesEinkommenVorAbzugFamGroesse()
                        .find('input')
                        .should('have.value', Resultate.massgebendesEinkVorAbzFamGr);
                });
            }
            cy.waitForRequest('GET', '**/einkommensverschlechterung/minimalesMassgebendesEinkommen/**', () => {
                clickSave();
            });
        }

        // EINKOMMENSVERSCHLECHTERUNG
        {
            EinkommensverschlechterungInfoPO.fillEinkommensverschlechterungInfoForm('withValid');
            cy.waitForRequest('POST', '**/einkommensverschlechterung/calculateTemp/1', () => {
                clickSave();
            });
            cy.groupBy('Einkommensverschlechterung - Jahr 1', () => {
                EinkommensverschlechterungPO.getPageTitle().should('include.text', gesuchsPeriode.anfang);
                EinkommensverschlechterungPO.fillEinkommensverschlechterungForm('withValid', 'jahr1', 'GS1');
                clickSave();
                FixtureFamSit[famsitDataset](({ GS2 }) => {
                    EinkommensverschlechterungPO.getPageTitle().should('include.text', `${GS2.vorname} ${GS2.nachname}`);
                });
                EinkommensverschlechterungPO.fillEinkommensverschlechterungForm('withValid', 'jahr1', 'GS2');
                clickSave();
            });
            cy.groupBy('Einkommensverschlechterung - Jahr 2', () => {
                EinkommensverschlechterungPO.getPageTitle().should('include.text', gesuchsPeriode.ende);
                EinkommensverschlechterungPO.fillEinkommensverschlechterungForm('withValid', 'jahr2', 'GS1');
                cy.waitForRequest('POST', '**/einkommensverschlechterung/calculateTemp/2', () => {
                    clickSave();
                });
                FixtureFamSit[famsitDataset](({ GS2 }) => {
                    EinkommensverschlechterungPO.getPageTitle().should('include.text', `${GS2.vorname} ${GS2.nachname}`);
                });
                EinkommensverschlechterungPO.fillEinkommensverschlechterungForm('withValid', 'jahr2', 'GS2');
                cy.waitForRequest('POST', '**/finanzielleSituation/calculateTemp', () => {
                    clickSave();
                });
            });
            cy.groupBy('Resultate', () => {
                EinkommensverschlechterungResultatePO.getPageTitle().should('include.text', gesuchsPeriode.anfang);
                EinkommensverschlechterungResultatePO.fillResultateForm('withValid', 'jahr1');
                checkResultateForm('withValid', 'jahr1');
                clickSave();

                EinkommensverschlechterungPO.getPageTitle().should('include.text', gesuchsPeriode.ende);
                EinkommensverschlechterungResultatePO.fillResultateForm('withValid', 'jahr2');
                checkResultateForm('withValid', 'jahr2');
            });
            cy.waitForRequest('GET', '**/dokumente/**', () => {
                clickSave();
            });
        }

        // DOKUMENTE
        {
            // Test upload file
            cy.fixture('documents/small.png').as('smallPng');

            // Upload the file on every <input type=file>, Angular JS file upload makes specific upload difficult:
            // https://github.com/abramenal/cypress-file-upload/tree/main/recipes/angularjs-ng-file-upload
            // https://github.com/danialfarid/ng-file-upload/issues/1140
            // https://github.com/danialfarid/ng-file-upload/issues/1167
            DokumentePO.getAllFileUploads().each(($el, index) => {
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
            cy.waitForRequest('POST', '**/wizard-steps', () => {
                clickSave();
            });
        }

        cy.resetViewport();
        // PLATZBESTAETIGUNG mit Kita SB
        // !!!!!! - New User - !!!!!!
        {
            cy.changeLogin(userKita);
            cy.viewport('macbook-15');

            const goToBetreuungen = () => {
                cy.get('@antragsId').then((antragsId) => cy.visit(`/#/gesuch/familiensituation/${antragsId}`));

                cy.waitForRequest('GET', '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**', () => {
                    SidenavPO.goTo('BETREUUNG');
                });
            };

            goToBetreuungen();
            AntragBetreuungPO.getBetreuung(0,0).click();
            AntragBetreuungPO.fillKitaBetreuungspensumForm('withValid', 'London');
            AntragBetreuungPO.getBetreuungspensumAb(0).find('input').clear().type('01.08.2023');
            AntragBetreuungPO.getBetreuungspensumBis(0).find('input').clear().type('31.07.2024');
            AntragBetreuungPO.platzBestaetigen();

            cy.changeLogin(userTraegerschaft);
            goToBetreuungen();

            cy.waitForRequest('GET', '**/fachstellen/erweiterteBetreuung', () => {
                AntragBetreuungPO.getBetreuung(1, 0).click();
            });

            AntragBetreuungPO.platzAbweisen('Ein sehr legitimer Grund der hier nicht weiter aufgeführt wird.');
        }

        cy.changeLogin(userGS);
        cy.viewport('iphone-8');
        openAntrag();
        // !!!!!! - changed back to previous user - !!!!!!

        // FREIGABE
        {
            FallToolbarPO.getMobileSidenavTrigger().click();
            cy.waitForRequest('GET', '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**', () => {
                SidenavPO.goTo('BETREUUNG');
            });
            AntragBetreuungPO.getBetreuungsstatus(1, 0).should('include.text', 'Abgewiesen');
            AntragBetreuungPO.getBetreuungLoeschenButton(1, 0).click();
            cy.waitForRequest('DELETE', '**/betreuungen/**', () => {
                ConfirmDialogPO.getDvLoadingConfirmButton().click();
            });

            FallToolbarPO.getMobileSidenavTrigger().click();
            SidenavPO.goTo('FREIGABE');
            FreigabePO.getFreigebenButton().click();
            cy.getDownloadUrl(() => {
                cy.waitForRequest('GET', '**/dossier/fall/**', () => {
                    ConfirmDialogPO.getDvLoadingConfirmButton().click();
                });
            }).then(downloadUrl => {
                return cy.request(downloadUrl)
                    .then(response => expect(response.headers['content-disposition']).to.match(/Freigabequittung_.*\.pdf/));
            });
        }

        cy.resetViewport();
        // VERFUEGUNG
        {
            cy.changeLogin(userSuperadmin);
            cy.get('@antragsId').then((antragsId) => cy.visit(`/#/gesuch/freigabe/${antragsId}`));
            cy.waitForRequest('GET', '**/dossier/fall/**', () => {
                FreigabePO.getFreigabequittungEinscannenSimulierenButton().click();
            });
            // attempt to reduce flakyness
            cy.wait(2000);
            SidenavPO.goTo('GESUCH_ERSTELLEN');
            AntragCreationPO.getEingangsdatum().find('input').clear().type('01.07.2023');
            cy.waitForRequest('PUT', '**/gesuche', () => {
                clickSave();
            });

            cy.changeLogin(userGemeinde);
            cy.get('@antragsId').then((antragsId) => cy.visit(`/#/gesuch/freigabe/${antragsId}`));
            clickSave();
            SidenavPO.getGesuchStatus().should('have.text', 'In Bearbeitung');

            VerfuegenPO.finSitAkzeptieren();
            VerfuegenPO.pruefeGesuch();
            SidenavPO.getGesuchStatus().should('have.text', 'Geprüft');

            cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
                VerfuegenPO.getVerfuegenStartenButton().click();
                ConfirmDialogPO.getDvLoadingConfirmButton().click();
            });
            SidenavPO.getGesuchStatus().should('have.text', 'Verfügen');
            VerfuegenPO.getVerfuegung(0, 0).click();
            VerfuegungPO.getAnspruchberechtigtesBetreuungspensum(5).should('include.text', '80%');
            VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();
            cy.waitForRequest('GET', '**/gesuche/dossier/**', () => {
                VerfuegungPO.getVerfuegenButton().click();
                ConfirmDialogPO.getDvLoadingConfirmButton().click();
            });
            SidenavPO.getGesuchStatus().should('have.text', 'Verfügt');
        }
    });
});

function clickSave() {
    NavigationPO.getSaveAndNextButton().should('not.have.a.property', 'disabled');
    NavigationPO.saveAndGoNext();
}

function checkResultateForm(dataset: keyof typeof FixtureEinkommensverschlechterung, jahr: 'jahr1' | 'jahr2') {
    FixtureEinkommensverschlechterung[dataset](({[jahr]: {Resultate}}) => {
        EinkommensverschlechterungResultatePO.getEinkommenBeiderGesuchsteller().find('input').should('have.value', Resultate.einkommenBeiderGesuchsteller);
        EinkommensverschlechterungResultatePO.getEinkommenVorjahrBasis().find('input').should('have.value', Resultate.einkommenVorjahrBasis);
        EinkommensverschlechterungResultatePO.getEinkommenVorjahr().find('input').should('have.value', Resultate.einkommenVorjahr);
    })
}
