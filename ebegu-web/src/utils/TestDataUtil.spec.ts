/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {IHttpBackendService} from 'angular';
import * as moment from 'moment';
import {TSGemeindeStatus} from '../models/enums/TSGemeindeStatus';
import {TSGesuchsperiodeStatus} from '../models/enums/TSGesuchsperiodeStatus';
import {TSRole} from '../models/enums/TSRole';
import {TSTaetigkeit} from '../models/enums/TSTaetigkeit';
import {TSWizardStepName} from '../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../models/enums/TSWizardStepStatus';
import {TSAbstractDateRangedEntity} from '../models/TSAbstractDateRangedEntity';
import {TSAbstractEntity} from '../models/TSAbstractEntity';
import {TSAbstractMutableEntity} from '../models/TSAbstractMutableEntity';
import {TSAdresse} from '../models/TSAdresse';
import {TSAdresseContainer} from '../models/TSAdresseContainer';
import {TSBenutzer} from '../models/TSBenutzer';
import {TSBerechtigung} from '../models/TSBerechtigung';
import {TSDossier} from '../models/TSDossier';
import {TSErwerbspensum} from '../models/TSErwerbspensum';
import {TSErwerbspensumContainer} from '../models/TSErwerbspensumContainer';
import {TSFall} from '../models/TSFall';
import {TSGemeinde} from '../models/TSGemeinde';
import {TSGemeindeKonfiguration} from '../models/TSGemeindeKonfiguration';
import {TSGesuchsperiode} from '../models/TSGesuchsperiode';
import {TSGesuchsteller} from '../models/TSGesuchsteller';
import {TSGesuchstellerContainer} from '../models/TSGesuchstellerContainer';
import {TSMandant} from '../models/TSMandant';
import {TSModulTagesschule} from '../models/TSModulTagesschule';
import {TSModulTagesschuleGroup} from '../models/TSModulTagesschuleGroup';
import {TSVerfuegung} from '../models/TSVerfuegung';
import {TSWizardStep} from '../models/TSWizardStep';
import {TSDateRange} from '../models/types/TSDateRange';
import {DateUtil} from './DateUtil';

// tslint:disable:no-magic-numbers no-identical-functions
export class TestDataUtil {

    public static setAbstractFieldsUndefined(abstractEntity: TSAbstractEntity): void {
        abstractEntity.id = undefined;
        abstractEntity.timestampErstellt = undefined;
        abstractEntity.timestampMutiert = undefined;
        abstractEntity.version = undefined;
    }

    public static setAbstractMutableFieldsUndefined(abstractEntity: TSAbstractMutableEntity): void {
        this.setAbstractFieldsUndefined(abstractEntity);
        abstractEntity.vorgaengerId = undefined;
    }

    public static createErwerbspensumContainer(): TSErwerbspensumContainer {

        const dummyErwerbspensumContainer = new TSErwerbspensumContainer();
        dummyErwerbspensumContainer.erwerbspensumGS = this.createErwerbspensum();
        dummyErwerbspensumContainer.erwerbspensumJA = this.createErwerbspensum();
        this.setAbstractMutableFieldsUndefined(dummyErwerbspensumContainer);
        return dummyErwerbspensumContainer;
    }

    public static createErwerbspensum(): TSErwerbspensum {
        const dummyErwerbspensum = new TSErwerbspensum();
        dummyErwerbspensum.taetigkeit = TSTaetigkeit.ANGESTELLT;
        dummyErwerbspensum.pensum = 100;
        dummyErwerbspensum.gueltigkeit = new TSDateRange(DateUtil.today(), DateUtil.today().add(7, 'months'));
        dummyErwerbspensum.bezeichnung = undefined;
        dummyErwerbspensum.unbezahlterUrlaub = undefined;
        this.setAbstractMutableFieldsUndefined(dummyErwerbspensum);
        return dummyErwerbspensum;
    }

    public static checkGueltigkeitAndSetIfSame(
        first: TSAbstractDateRangedEntity,
        second: TSAbstractDateRangedEntity,
    ): void {
        // Dieses hack wird gebraucht weil um 2 Moment zu vergleichen kann man nicht einfach equal() benutzen sondern
        // isSame
        expect(first.gueltigkeit.gueltigAb.isSame(second.gueltigkeit.gueltigAb)).toBe(true);
        expect(first.gueltigkeit.gueltigBis.isSame(second.gueltigkeit.gueltigBis)).toBe(true);
        first.gueltigkeit.gueltigAb = second.gueltigkeit.gueltigAb;
        first.gueltigkeit.gueltigBis = second.gueltigkeit.gueltigBis;
    }

    public static mockDefaultGesuchModelManagerHttpCalls($httpBackend: IHttpBackendService): void {
        $httpBackend.when('GET', '/ebegu/api/v1/gesuchsperioden/active').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/application-properties/public/all').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/gemeinde/all').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/gesuchsperioden/unclosed').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/gesuchsperioden/0621fb5d-a187-5a91-abaf-8a813c4d263a').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/wizard-steps').respond({});
        $httpBackend.when('POST', '/ebegu/api/v1/wizard-steps').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/fachstellen/anspruch').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/fachstellen/erweiterteBetreuung').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/institutionstammdaten/gesuchsperiode/gemeinde/active').respond({});
    }

    public static mockLazyGesuchModelManagerHttpCalls($httpBackend: IHttpBackendService): void {
        $httpBackend.when('GET', '/ebegu/api/v1/application-properties/public/all').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/gemeinde/all').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/gesuchsperioden/active').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/gesuchsperioden/unclosed').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/fachstellen/anspruch').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/fachstellen/erweiterteBetreuung').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/institutionstammdaten/gesuchsperiode/gemeinde/active').respond({});
    }

    public static createWizardStep(gesuchId: string): TSWizardStep {
        const wizardStep = new TSWizardStep();
        TestDataUtil.setAbstractMutableFieldsUndefined(wizardStep);
        wizardStep.gesuchId = gesuchId;
        wizardStep.bemerkungen = 'bemerkung';
        wizardStep.wizardStepStatus = TSWizardStepStatus.IN_BEARBEITUNG;
        wizardStep.wizardStepName = TSWizardStepName.BETREUUNG;
        return wizardStep;
    }

    public static createVerfuegung(): TSVerfuegung {
        const verfuegung = new TSVerfuegung();
        TestDataUtil.setAbstractMutableFieldsUndefined(verfuegung);
        verfuegung.id = '123321';
        verfuegung.zeitabschnitte = [];
        return verfuegung;
    }

    public static createGesuchsperiode20162017(): TSGesuchsperiode {
        const gueltigkeit = new TSDateRange(moment('01.07.2016', 'DD.MM.YYYY'), moment('31.08.2017', 'DD.MM.YYYY'));
        return new TSGesuchsperiode(TSGesuchsperiodeStatus.AKTIV, gueltigkeit);
    }

    public static createGesuchsteller(vorname: string, nachname: string): TSGesuchstellerContainer {
        const gesuchstellerCont = new TSGesuchstellerContainer();
        const gesuchsteller = new TSGesuchsteller();
        gesuchsteller.vorname = vorname;
        gesuchsteller.nachname = nachname;
        gesuchstellerCont.gesuchstellerJA = gesuchsteller;
        gesuchstellerCont.adressen = [];
        return gesuchstellerCont;
    }

    public static createAdresse(strasse: string, nummer: string): TSAdresseContainer {
        const adresseCont = new TSAdresseContainer();
        const adresse = new TSAdresse();
        adresse.strasse = strasse;
        adresse.hausnummer = nummer;
        adresse.gueltigkeit = TestDataUtil.createGesuchsperiode20162017().gueltigkeit;
        adresseCont.showDatumVon = true;
        adresseCont.adresseJA = adresse;
        return adresseCont;
    }

    public static createDummyForm(): any {
        const form: any = {};
        form.$valid = true;
        form.$setPristine = () => {
        };
        form.$setUntouched = () => {
        };
        return form;
    }

    public static createValidationReport(): any {
        return {
            status: 400,
            data: {
                parameterViolations: [],
                classViolations: [],
                fieldViolations: [],
                propertyViolations: [
                    {
                        constraintType: 'PARAMETER',
                        path: 'markAsRead.arg1',
                        message: 'Die Länge des Feldes muss zwischen 36 und 36 sein',
                        value: '8a146418-ab12-456f-9b17-aad6990f51',
                    },
                ],
                returnValueViolations: [],
            },
        };
    }

    public static createExceptionReport(): any {
        return {
            status: 500,
            data: {
                errorCodeEnum: 'ERROR_ENTITY_NOT_FOUND',
                exceptionName: 'EbeguRuntimeException',
                methodName: 'doTest',
                stackTrace: null,
                translatedMessage: '',
                customMessage: 'test',
                objectId: '44-55-66-77',
                argumentList: null,
            },
        };
    }

    public static createGemeindeLondon(): TSGemeinde {
        const gemeinde = new TSGemeinde();
        TestDataUtil.setAbstractFieldsUndefined(gemeinde);
        gemeinde.id = '80a8e496-b73c-4a4a-a163-a0b2caf76487';
        gemeinde.key = gemeinde.id;
        gemeinde.name = 'London';
        gemeinde.gemeindeNummer = 2;
        gemeinde.bfsNummer = 99999;
        gemeinde.status = TSGemeindeStatus.AKTIV;
        gemeinde.betreuungsgutscheineStartdatum = moment('20160801', 'YYYYMMDD');
        gemeinde.tagesschulanmeldungenStartdatum = moment('20200801', 'YYYYMMDD');
        gemeinde.ferieninselanmeldungenStartdatum = moment('20200801', 'YYYYMMDD');
        gemeinde.angebotBG = true;
        gemeinde.angebotTS = false;
        gemeinde.angebotFI = false;
        gemeinde.gueltigBis = moment('99991231', 'YYYYMMDD');
        return gemeinde;
    }

    public static createGemeindeParis(): TSGemeinde {
        const gemeinde = new TSGemeinde();
        TestDataUtil.setAbstractFieldsUndefined(gemeinde);
        gemeinde.id = 'ea02b313-e7c3-4b26-9ef7-e413f4046db2';
        gemeinde.key = gemeinde.id;
        gemeinde.name = 'Bern';
        gemeinde.gemeindeNummer = 1;
        gemeinde.bfsNummer = 99998;
        gemeinde.status = TSGemeindeStatus.AKTIV;
        gemeinde.betreuungsgutscheineStartdatum = moment('20160801', 'YYYYMMDD');
        gemeinde.tagesschulanmeldungenStartdatum = moment('20200801', 'YYYYMMDD');
        gemeinde.ferieninselanmeldungenStartdatum = moment('20200801', 'YYYYMMDD');
        gemeinde.angebotBG = true;
        gemeinde.angebotTS = false;
        gemeinde.angebotFI = false;
        gemeinde.gueltigBis = moment('99991231', 'YYYYMMDD');
        return gemeinde;
    }

    public static createGemeindeThun(): TSGemeinde {
        const gemeinde = new TSGemeinde();
        TestDataUtil.setAbstractFieldsUndefined(gemeinde);
        gemeinde.id = 'ea02b313-e7c3-4b26-9ef7-e413f4046ccc';
        gemeinde.key = gemeinde.id;
        gemeinde.name = 'Thun';
        gemeinde.gemeindeNummer = 3;
        gemeinde.status = TSGemeindeStatus.AKTIV;
        gemeinde.betreuungsgutscheineStartdatum = moment('20160801', 'YYYYMMDD');
        gemeinde.tagesschulanmeldungenStartdatum = moment('20200801', 'YYYYMMDD');
        gemeinde.ferieninselanmeldungenStartdatum = moment('20200801', 'YYYYMMDD');
        gemeinde.angebotBG = true;
        gemeinde.angebotTS = false;
        gemeinde.angebotFI = false;
        return gemeinde;
    }

    public static createGemeindeKonfiguration(): TSGemeindeKonfiguration {
        const konfiguration = new TSGemeindeKonfiguration();
        konfiguration.gesuchsperiode = this.createGesuchsperiode20162017();
        konfiguration.konfigTagesschuleAktivierungsdatum = undefined;
        konfiguration.konfigTagesschuleErsterSchultag = undefined;
        return konfiguration;
    }

    public static createBerechtigung(role: TSRole, createGemeinde: boolean): TSBerechtigung {
        const berechtigung = new TSBerechtigung();
        if (createGemeinde) {
            berechtigung.gemeindeList.push(TestDataUtil.createGemeindeLondon());
        }
        berechtigung.role = role;
        return berechtigung;
    }

    public static createFall(): TSFall {
        const fall = new TSFall();
        TestDataUtil.setAbstractMutableFieldsUndefined(fall);
        fall.id = 'ea02b313-e7c3-4b26-9ef7-e413f4046d61';
        return fall;
    }

    public static createDossier(id: string, fall: TSFall): TSDossier {
        const dossier = new TSDossier();
        dossier.id = id;
        dossier.fall = fall;
        return dossier;
    }

    public static createSuperadmin(): TSBenutzer {
        const user = new TSBenutzer();
        user.nachname = 'system';
        user.vorname = 'system';
        user.currentBerechtigung = new TSBerechtigung();
        user.currentBerechtigung.role = TSRole.SUPER_ADMIN;
        user.mandant = this.createMandant();
        return user;
    }

    public static createMandant(): TSMandant {
        const mandant = new TSMandant('myMandant');
        TestDataUtil.setAbstractMutableFieldsUndefined(mandant);
        mandant.angebotTS = false;
        mandant.angebotFI = false;
        return mandant;
    }

    public static createModulTagesschuleGroup(): TSModulTagesschuleGroup {
        const tsModul = new TSModulTagesschuleGroup();
        return tsModul;
    }

    public static createModulTagesschule(): TSModulTagesschule {
        const tsModul = new TSModulTagesschule();
        tsModul.wochentag = undefined;
        return tsModul;
    }

    /**
     * Compares both objects checking all properties that are defined. This is useful when comparing mocks because
     * sometimes in a mock we don't need to define all parameters and we just focus in some of them. So using this
     * method we don't need to worry about all those parameters that are not existing in the mock
     */
    public static compareDefinedProperties(objectToCheck: any, expected: any): void {
        Object.keys(expected)
            .filter(value => expected[value])
            .forEach(key => {
                expect(objectToCheck.hasOwnProperty(key)).toBe(true);
                expect(objectToCheck[key]).toEqual(expected[key]);
            });
    }
}
