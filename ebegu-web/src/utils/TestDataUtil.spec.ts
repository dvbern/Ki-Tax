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

import {TSGemeindeStatus} from '../models/enums/TSGemeindeStatus';
import {TSRole} from '../models/enums/TSRole';
import {TSAbstractMutableEntity} from '../models/TSAbstractMutableEntity';
import TSBenutzer from '../models/TSBenutzer';
import TSBerechtigung from '../models/TSBerechtigung';
import TSDossier from '../models/TSDossier';
import TSErwerbspensumContainer from '../models/TSErwerbspensumContainer';
import TSErwerbspensum from '../models/TSErwerbspensum';
import {TSTaetigkeit} from '../models/enums/TSTaetigkeit';
import TSFall from '../models/TSFall';
import TSGemeinde from '../models/TSGemeinde';
import DateUtil from './DateUtil';
import {IHttpBackendService} from 'angular';
import {TSDateRange} from '../models/types/TSDateRange';
import {TSZuschlagsgrund} from '../models/enums/TSZuschlagsgrund';
import TSAbstractEntity from '../models/TSAbstractEntity';
import {TSAbstractDateRangedEntity} from '../models/TSAbstractDateRangedEntity';
import TSWizardStep from '../models/TSWizardStep';
import {TSWizardStepStatus} from '../models/enums/TSWizardStepStatus';
import {TSWizardStepName} from '../models/enums/TSWizardStepName';
import TSVerfuegung from '../models/TSVerfuegung';
import * as moment from 'moment';
import TSGesuchsperiode from '../models/TSGesuchsperiode';
import TSGesuchsteller from '../models/TSGesuchsteller';
import TSAdresse from '../models/TSAdresse';
import TSGesuchstellerContainer from '../models/TSGesuchstellerContainer';
import TSAdresseContainer from '../models/TSAdresseContainer';
import {TSGesuchsperiodeStatus} from '../models/enums/TSGesuchsperiodeStatus';

export default class TestDataUtil {

    public static setAbstractFieldsUndefined(abstractEntity: TSAbstractEntity) {
        abstractEntity.id = undefined;
        abstractEntity.timestampErstellt = undefined;
        abstractEntity.timestampMutiert = undefined;
    }

    public static setAbstractMutableFieldsUndefined(abstractEntity: TSAbstractMutableEntity) {
        this.setAbstractFieldsUndefined(abstractEntity);
        abstractEntity.vorgaengerId = undefined;
    }

    public static createErwerbspensumContainer(): TSErwerbspensumContainer {

        const dummyErwerbspensumContainer: TSErwerbspensumContainer = new TSErwerbspensumContainer();
        dummyErwerbspensumContainer.erwerbspensumGS = this.createErwerbspensum();
        dummyErwerbspensumContainer.erwerbspensumJA = this.createErwerbspensum();
        this.setAbstractMutableFieldsUndefined(dummyErwerbspensumContainer);
        return dummyErwerbspensumContainer;
    }

    static createErwerbspensum(): TSErwerbspensum {
        const dummyErwerbspensum = new TSErwerbspensum();
        dummyErwerbspensum.taetigkeit = TSTaetigkeit.ANGESTELLT;
        dummyErwerbspensum.pensum = 80;
        dummyErwerbspensum.gueltigkeit = new TSDateRange(DateUtil.today(), DateUtil.today().add(7, 'months'));
        dummyErwerbspensum.zuschlagZuErwerbspensum = true;
        dummyErwerbspensum.zuschlagsprozent = 20;
        dummyErwerbspensum.zuschlagsgrund = TSZuschlagsgrund.FIXE_ARBEITSZEITEN;
        dummyErwerbspensum.bezeichnung = undefined;
        this.setAbstractMutableFieldsUndefined(dummyErwerbspensum);
        return dummyErwerbspensum;
    }

    static checkGueltigkeitAndSetIfSame(first: TSAbstractDateRangedEntity, second: TSAbstractDateRangedEntity) {
        // Dieses hack wird gebraucht weil um 2 Moment zu vergleichen kann man nicht einfach equal() benutzen sondern isSame
        expect(first.gueltigkeit.gueltigAb.isSame(second.gueltigkeit.gueltigAb)).toBe(true);
        expect(first.gueltigkeit.gueltigBis.isSame(second.gueltigkeit.gueltigBis)).toBe(true);
        first.gueltigkeit.gueltigAb = second.gueltigkeit.gueltigAb;
        first.gueltigkeit.gueltigBis = second.gueltigkeit.gueltigBis;
    }

    static mockDefaultGesuchModelManagerHttpCalls($httpBackend: IHttpBackendService) {
        $httpBackend.when('GET', '/ebegu/api/v1/gesuchsperioden/unclosed').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/gesuchsperioden/0621fb5d-a187-5a91-abaf-8a813c4d263a').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/wizard-steps').respond({});
        $httpBackend.when('POST', '/ebegu/api/v1/wizard-steps').respond({});
    }

    public static mockLazyGesuchModelManagerHttpCalls($httpBackend: IHttpBackendService) {
        $httpBackend.when('GET', '/ebegu/api/v1/gesuchsperioden/active').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/gesuchsperioden/unclosed').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/fachstellen').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/institutionstammdaten/gesuchsperiode/active?gesuchsperiodeId=0621fb5d-a187-5a91-abaf-8a813c4d263a').respond({});
    }

    public static createWizardStep(gesuchId: string): TSWizardStep {
        const wizardStep: TSWizardStep = new TSWizardStep();
        TestDataUtil.setAbstractMutableFieldsUndefined(wizardStep);
        wizardStep.gesuchId = gesuchId;
        wizardStep.bemerkungen = 'bemerkung';
        wizardStep.wizardStepStatus = TSWizardStepStatus.IN_BEARBEITUNG;
        wizardStep.wizardStepName = TSWizardStepName.BETREUUNG;
        return wizardStep;
    }

    public static createVerfuegung(): TSVerfuegung {
        const verfuegung: TSVerfuegung = new TSVerfuegung();
        TestDataUtil.setAbstractMutableFieldsUndefined(verfuegung);
        verfuegung.id = '123321';
        verfuegung.zeitabschnitte = [];
        return verfuegung;
    }

    public static createGesuchsperiode20162017(): TSGesuchsperiode {
        const gueltigkeit: TSDateRange = new TSDateRange(moment('01.07.2016', 'DD.MM.YYYY'), moment('31.08.2017', 'DD.MM.YYYY'));
        return new TSGesuchsperiode(TSGesuchsperiodeStatus.AKTIV, gueltigkeit);
    }

    public static createGesuchsteller(vorname: string, nachname: string): TSGesuchstellerContainer {
        const gesuchstellerCont: TSGesuchstellerContainer = new TSGesuchstellerContainer();
        const gesuchsteller: TSGesuchsteller = new TSGesuchsteller();
        gesuchsteller.vorname = vorname;
        gesuchsteller.nachname = nachname;
        gesuchstellerCont.gesuchstellerJA = gesuchsteller;
        gesuchstellerCont.adressen = [];
        return gesuchstellerCont;
    }

    public static createAdresse(strasse: string, nummer: string): TSAdresseContainer {
        const adresseCont: TSAdresseContainer = new TSAdresseContainer();
        const adresse: TSAdresse = new TSAdresse();
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
                propertyViolations: [{
                    constraintType: 'PARAMETER',
                    path: 'markAsRead.arg1',
                    message: 'Die Länge des Feldes muss zwischen 36 und 36 sein',
                    value: '8a146418-ab12-456f-9b17-aad6990f51'
                }],
                returnValueViolations: []
            }
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
            }
        };
    }

    public static createGemeindeOstermundigen(): TSGemeinde {
        const gemeinde: TSGemeinde = new TSGemeinde();
        TestDataUtil.setAbstractMutableFieldsUndefined(gemeinde);
        gemeinde.id = '80a8e496-b73c-4a4a-a163-a0b2caf76487';
        gemeinde.name = 'Ostermundigen';
        gemeinde.gemeindeNummer = 2;
        gemeinde.bfsNummer = 363;
        gemeinde.status = TSGemeindeStatus.AKTIV;
        return gemeinde;
    }

    public static createGemeindeBern(): TSGemeinde {
        const gemeinde: TSGemeinde = new TSGemeinde();
        TestDataUtil.setAbstractMutableFieldsUndefined(gemeinde);
        gemeinde.id = 'ea02b313-e7c3-4b26-9ef7-e413f4046db2';
        gemeinde.name = 'Bern';
        gemeinde.gemeindeNummer = 1;
        gemeinde.bfsNummer = 351;
        gemeinde.status = TSGemeindeStatus.AKTIV;
        return gemeinde;
    }

    public static createGemeindeThun(): TSGemeinde {
        const gemeinde: TSGemeinde = new TSGemeinde();
        TestDataUtil.setAbstractMutableFieldsUndefined(gemeinde);
        gemeinde.id = 'ea02b313-e7c3-4b26-9ef7-e413f4046ccc';
        gemeinde.name = 'Thun';
        gemeinde.gemeindeNummer = 3;
        gemeinde.status = TSGemeindeStatus.AKTIV;
        return gemeinde;
    }

    public static createBerechtigung(role: TSRole, createGemeinde: boolean): TSBerechtigung {
        const berechtigung: TSBerechtigung = new TSBerechtigung();
        if (createGemeinde) {
            berechtigung.gemeindeList.push(TestDataUtil.createGemeindeOstermundigen());
        }
        berechtigung.role = role;
        return berechtigung;
    }

    public static createFall(): TSFall {
        const fall: TSFall = new TSFall();
        TestDataUtil.setAbstractMutableFieldsUndefined(fall);
        fall.id = 'ea02b313-e7c3-4b26-9ef7-e413f4046d61';
        return fall;
    }

    public static createDossier(id: string, fall: TSFall): TSDossier {
        const dossier: TSDossier = new TSDossier();
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
        return user;
    }
}
