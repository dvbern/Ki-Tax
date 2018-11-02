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

import * as moment from 'moment';
import {CONSTANTS} from '../app/core/constants/CONSTANTS';
import {TSAdressetyp} from '../models/enums/TSAdressetyp';
import {TSAntragTyp} from '../models/enums/TSAntragTyp';
import {TSBetreuungsangebotTyp} from '../models/enums/TSBetreuungsangebotTyp';
import {TSBetreuungsstatus} from '../models/enums/TSBetreuungsstatus';
import {TSGeschlecht} from '../models/enums/TSGeschlecht';
import {TSGesuchsperiodeStatus} from '../models/enums/TSGesuchsperiodeStatus';
import {TSPensumUnits} from '../models/enums/TSPensumUnits';
import {TSVerfuegungZeitabschnittZahlungsstatus} from '../models/enums/TSVerfuegungZeitabschnittZahlungsstatus';
import TSAbwesenheit from '../models/TSAbwesenheit';
import TSAbwesenheitContainer from '../models/TSAbwesenheitContainer';
import TSAdresse from '../models/TSAdresse';
import TSAntragDTO from '../models/TSAntragDTO';
import TSBetreuung from '../models/TSBetreuung';
import TSBetreuungspensum from '../models/TSBetreuungspensum';
import TSBetreuungspensumContainer from '../models/TSBetreuungspensumContainer';
import TSDossier from '../models/TSDossier';
import TSErweiterteBetreuungContainer from '../models/TSErweiterteBetreuungContainer';
import TSErwerbspensum from '../models/TSErwerbspensum';
import {TSFachstelle} from '../models/TSFachstelle';
import TSFall from '../models/TSFall';
import TSFamiliensituation from '../models/TSFamiliensituation';
import TSFamiliensituationContainer from '../models/TSFamiliensituationContainer';
import TSGesuch from '../models/TSGesuch';
import TSGesuchsperiode from '../models/TSGesuchsperiode';
import TSGesuchsteller from '../models/TSGesuchsteller';
import TSGesuchstellerContainer from '../models/TSGesuchstellerContainer';
import TSInstitution from '../models/TSInstitution';
import TSInstitutionStammdaten from '../models/TSInstitutionStammdaten';
import TSInstitutionStammdatenTagesschule from '../models/TSInstitutionStammdatenTagesschule';
import {TSMandant} from '../models/TSMandant';
import TSModulTagesschule from '../models/TSModulTagesschule';
import {TSTraegerschaft} from '../models/TSTraegerschaft';
import TSVerfuegung from '../models/TSVerfuegung';
import TSVerfuegungZeitabschnitt from '../models/TSVerfuegungZeitabschnitt';
import {TSDateRange} from '../models/types/TSDateRange';
import DateUtil from './DateUtil';
import EbeguRestUtil from './EbeguRestUtil';
import EbeguUtil from './EbeguUtil';
import TestDataUtil from './TestDataUtil.spec';
import IProvideService = angular.auto.IProvideService;

// tslint:disable:no-big-function
describe('EbeguRestUtil', () => {

    let ebeguRestUtil: EbeguRestUtil;
    let today: moment.Moment;

    const pensum25 = 25;
    const pensum50 = 50;
    const monatlicheBetreuungskosten200 = 200.2;
    const monatlicheBetreuungskosten500 = 500.5;

    beforeEach(angular.mock.module('pascalprecht.translate'));

    beforeEach(angular.mock.module(($provide: IProvideService) => {
        $provide.value('CONSTANTS', CONSTANTS);
        $provide.service('EbeguUtil', EbeguUtil);
    }));

    beforeEach(angular.mock.inject($injector => {
        ebeguRestUtil = new EbeguRestUtil($injector.get('EbeguUtil'));
        today = DateUtil.today();
    }));

    describe('API Usage', () => {
        describe('parseAdresse()', () => {
            it('should transfrom Adresse Rest Objects', () => {
                const adresse = new TSAdresse();
                adresse.adresseTyp = TSAdressetyp.WOHNADRESSE;
                TestDataUtil.setAbstractMutableFieldsUndefined(adresse);
                adresse.gemeinde = 'Testingen';
                adresse.land = 'CH';
                adresse.ort = 'Testort';
                adresse.strasse = 'Teststrasse';
                adresse.hausnummer = '1';
                adresse.zusatzzeile = 'co test';
                adresse.plz = '3014';
                adresse.id = '1234567';
                adresse.organisation = 'Test AG';
                adresse.gueltigkeit = new TSDateRange(today, today);

                const restAdresse: any = ebeguRestUtil.adresseToRestObject({}, adresse);
                expect(restAdresse).toBeDefined();
                const adr = ebeguRestUtil.parseAdresse(new TSAdresse(), restAdresse);
                expect(adr).toBeDefined();
                expect(adresse.gemeinde).toEqual(adr.gemeinde);
                TestDataUtil.checkGueltigkeitAndSetIfSame(adr, adresse);
                expect(adresse).toEqual(adr);

            });
        });
        describe('parseGesuchsteller()', () => {
            it('should transfrom TSGesuchsteller to REST Obj and back', () => {
                const myGesuchsteller = createGesuchsteller();
                TestDataUtil.setAbstractMutableFieldsUndefined(myGesuchsteller);
                myGesuchsteller.gesuchstellerGS = undefined;
                myGesuchsteller.gesuchstellerJA.telefon = ''; // Ein leerer String im Telefon muss auch behandelt werden
                const restGesuchsteller = ebeguRestUtil.gesuchstellerContainerToRestObject({}, myGesuchsteller);
                expect(restGesuchsteller).toBeDefined();
                const transformedPers: TSGesuchstellerContainer = ebeguRestUtil.parseGesuchstellerContainer(
                    new TSGesuchstellerContainer(), restGesuchsteller);
                expect(transformedPers).toBeDefined();
                expect(myGesuchsteller.gesuchstellerJA.nachname).toEqual(transformedPers.gesuchstellerJA.nachname);

                expect(transformedPers.gesuchstellerJA.telefon).toBeUndefined(); // der leere String wurde in undefined
                                                                                 // umgewandelt deswegen muessen wir
                                                                                 // hier
                                                                                 // undefined zurueckbekommen
                transformedPers.gesuchstellerJA.telefon = ''; // um das Objekt zu validieren, muessen wird das Telefon
                                                              // wieder auf '' setzen

                expect(myGesuchsteller).toEqual(transformedPers);

            });
        });
        describe('parseFachstelle()', () => {
            it('should transform TSFachstelle to REST object and back', () => {
                const myFachstelle = new TSFachstelle('Fachstelle_name', 'Beschreibung', true);
                TestDataUtil.setAbstractMutableFieldsUndefined(myFachstelle);

                const restFachstelle = ebeguRestUtil.fachstelleToRestObject({}, myFachstelle);
                expect(restFachstelle).toBeDefined();
                expect(restFachstelle.name).toEqual(myFachstelle.name);
                expect(restFachstelle.beschreibung).toEqual(myFachstelle.beschreibung);
                expect(restFachstelle.behinderungsbestaetigung).toEqual(myFachstelle.behinderungsbestaetigung);

                const transformedFachstelle = ebeguRestUtil.parseFachstelle(new TSFachstelle(), restFachstelle);
                expect(transformedFachstelle).toBeDefined();
                expect(transformedFachstelle).toEqual(myFachstelle);
            });
        });
        describe('parseGesuch()', () => {
            it('should transform TSGesuch to REST object and back', () => {
                const myGesuch = new TSGesuch();
                TestDataUtil.setAbstractMutableFieldsUndefined(myGesuch);
                myGesuch.einkommensverschlechterungInfoContainer = undefined;
                const fall = new TSFall();
                TestDataUtil.setAbstractMutableFieldsUndefined(fall);
                const dossier = new TSDossier();
                TestDataUtil.setAbstractMutableFieldsUndefined(dossier);
                myGesuch.dossier = dossier;
                myGesuch.dossier.fall = fall;
                myGesuch.dossier.fall.besitzer = undefined;
                const gesuchsteller = createGesuchsteller();
                gesuchsteller.gesuchstellerGS = undefined;
                TestDataUtil.setAbstractMutableFieldsUndefined(gesuchsteller);
                myGesuch.gesuchsteller1 = gesuchsteller;
                myGesuch.gesuchsteller2 = gesuchsteller;
                const gesuchsperiode = new TSGesuchsperiode();
                TestDataUtil.setAbstractMutableFieldsUndefined(gesuchsperiode);
                gesuchsperiode.gueltigkeit = new TSDateRange(undefined, undefined);
                myGesuch.gesuchsperiode = gesuchsperiode;
                const familiensituation = new TSFamiliensituation();
                TestDataUtil.setAbstractMutableFieldsUndefined(familiensituation);
                myGesuch.familiensituationContainer = new TSFamiliensituationContainer();
                myGesuch.familiensituationContainer.familiensituationJA = familiensituation;
                TestDataUtil.setAbstractMutableFieldsUndefined(myGesuch.familiensituationContainer);
                myGesuch.kindContainers = [];
                myGesuch.einkommensverschlechterungInfoContainer = undefined;
                myGesuch.bemerkungen = undefined;
                myGesuch.typ = undefined;

                const restGesuch = ebeguRestUtil.gesuchToRestObject({}, myGesuch);
                expect(restGesuch).toBeDefined();

                const transformedGesuch = ebeguRestUtil.parseGesuch(new TSGesuch(), restGesuch);
                expect(transformedGesuch).toBeDefined();

                expect(transformedGesuch.einkommensverschlechterungInfoContainer)
                    .toEqual(myGesuch.einkommensverschlechterungInfoContainer);
                expect(transformedGesuch.dossier.fall).toEqual(myGesuch.dossier.fall);
                expect(transformedGesuch.gesuchsteller1).toEqual(myGesuch.gesuchsteller1);
                expect(transformedGesuch.gesuchsteller2).toEqual(myGesuch.gesuchsteller2);
                expect(transformedGesuch.gesuchsperiode).toEqual(myGesuch.gesuchsperiode);
                expect(transformedGesuch.familiensituationContainer).toEqual(myGesuch.familiensituationContainer);
                expect(transformedGesuch.kindContainers).toEqual(myGesuch.kindContainers);
                expect(transformedGesuch.einkommensverschlechterungInfoContainer)
                    .toEqual(myGesuch.einkommensverschlechterungInfoContainer);
                expect(transformedGesuch.bemerkungen).toEqual(myGesuch.bemerkungen);
                expect(transformedGesuch.laufnummer).toEqual(myGesuch.laufnummer);
                expect(transformedGesuch.typ).toEqual(myGesuch.typ);
            });
        });
        describe('parseMandant()', () => {
            it('should transform TSMandant to REST object and back', () => {
                const myMandant = new TSMandant('myMandant');
                TestDataUtil.setAbstractMutableFieldsUndefined(myMandant);

                const restMandant = ebeguRestUtil.mandantToRestObject({}, myMandant);
                expect(restMandant).toBeDefined();
                expect(restMandant.name).toEqual(myMandant.name);

                const transformedMandant = ebeguRestUtil.parseMandant(new TSMandant(), restMandant);
                expect(transformedMandant).toBeDefined();
                expect(transformedMandant).toEqual(myMandant);
            });
        });
        describe('parseTraegerschaft()', () => {
            it('should transform TSTraegerschaft to REST object and back', () => {
                const myTraegerschaft = new TSTraegerschaft('myTraegerschaft');
                TestDataUtil.setAbstractMutableFieldsUndefined(myTraegerschaft);

                const restTraegerschaft = ebeguRestUtil.traegerschaftToRestObject({}, myTraegerschaft);
                expect(restTraegerschaft).toBeDefined();
                expect(restTraegerschaft.name).toEqual(myTraegerschaft.name);

                const traegerschaft = new TSTraegerschaft();
                const transformedTraegerschaft = ebeguRestUtil.parseTraegerschaft(traegerschaft, restTraegerschaft);
                expect(transformedTraegerschaft).toBeDefined();
                expect(transformedTraegerschaft).toEqual(myTraegerschaft);
            });
        });
        describe('parseInstitution()', () => {
            it('should transform TSInstitution to REST object and back', () => {
                const myInstitution = createInstitution();

                const restInstitution = ebeguRestUtil.institutionToRestObject({}, myInstitution);
                expect(restInstitution).toBeDefined();
                expect(restInstitution.name).toEqual(myInstitution.name);
                expect(restInstitution.traegerschaft.name).toEqual(myInstitution.traegerschaft.name);
                expect(restInstitution.mandant.name).toEqual(myInstitution.mandant.name);

                const transformedInstitution = ebeguRestUtil.parseInstitution(new TSInstitution(), restInstitution);
                expect(transformedInstitution).toBeDefined();
                expect(transformedInstitution).toEqual(myInstitution);
            });
        });
        describe('parseBetreuung()', () => {
            it('should transform TSBetreuung to REST object and back', () => {
                const instStam = new TSInstitutionStammdaten('iban',
                    TSBetreuungsangebotTyp.KITA,
                    createInstitution(),
                    undefined,
                    new TSDateRange(DateUtil.today(), DateUtil.today()));
                TestDataUtil.setAbstractMutableFieldsUndefined(instStam);

                const tsBetreuungspensumGS = new TSBetreuungspensum(
                    TSPensumUnits.PERCENTAGE,
                    false,
                    monatlicheBetreuungskosten500,
                    pensum25,
                    new TSDateRange(DateUtil.today(), DateUtil.today()));
                TestDataUtil.setAbstractMutableFieldsUndefined(tsBetreuungspensumGS);
                const tsBetreuungspensumJA = new TSBetreuungspensum(
                    TSPensumUnits.PERCENTAGE,
                    false,
                    monatlicheBetreuungskosten200,
                    pensum50,
                    new TSDateRange(DateUtil.today(), DateUtil.today()));
                TestDataUtil.setAbstractMutableFieldsUndefined(tsBetreuungspensumJA);
                const tsBetreuungspensumContainer = new TSBetreuungspensumContainer(
                    tsBetreuungspensumGS,
                    tsBetreuungspensumJA);
                TestDataUtil.setAbstractMutableFieldsUndefined(tsBetreuungspensumContainer);
                const betContainers = [tsBetreuungspensumContainer];

                const tsAbwesenheitGS = new TSAbwesenheit(new TSDateRange(today, today));
                const tsAbwesenheitJA = new TSAbwesenheit(new TSDateRange(today, today));
                const tsAbwesenheitContainer = new TSAbwesenheitContainer(tsAbwesenheitGS,
                    tsAbwesenheitJA);
                const abwesenheitContainers = [tsAbwesenheitContainer];
                const erweiterteBetreuungContainer = new TSErweiterteBetreuungContainer();

                const betreuung = new TSBetreuung(instStam,
                    TSBetreuungsstatus.AUSSTEHEND,
                    betContainers,
                    abwesenheitContainers,
                    erweiterteBetreuungContainer,
                    2);
                TestDataUtil.setAbstractMutableFieldsUndefined(betreuung);

                const restBetreuung = ebeguRestUtil.betreuungToRestObject({}, betreuung);

                expect(restBetreuung).toBeDefined();
                expect(restBetreuung.betreuungsstatus).toEqual(TSBetreuungsstatus.AUSSTEHEND);
                expect(restBetreuung.institutionStammdaten.iban).toEqual(betreuung.institutionStammdaten.iban);
                expect(restBetreuung.betreuungspensumContainers).toBeDefined();
                expect(restBetreuung.betreuungspensumContainers.length)
                    .toEqual(betreuung.betreuungspensumContainers.length);
                expect(restBetreuung.betreuungspensumContainers[0].betreuungspensumGS.pensum)
                    .toBe(betreuung.betreuungspensumContainers[0].betreuungspensumGS.pensum);
                expect(restBetreuung.betreuungspensumContainers[0].betreuungspensumJA.pensum)
                    .toBe(betreuung.betreuungspensumContainers[0].betreuungspensumJA.pensum);

                const transformedBetreuung = ebeguRestUtil.parseBetreuung(new TSBetreuung(),
                    restBetreuung);

                expect(transformedBetreuung).toBeDefined();
                TestDataUtil.checkGueltigkeitAndSetIfSame(transformedBetreuung.betreuungspensumContainers[0].betreuungspensumGS,
                    betreuung.betreuungspensumContainers[0].betreuungspensumGS);
                TestDataUtil.checkGueltigkeitAndSetIfSame(transformedBetreuung.betreuungspensumContainers[0].betreuungspensumJA,
                    betreuung.betreuungspensumContainers[0].betreuungspensumJA);
                TestDataUtil.checkGueltigkeitAndSetIfSame(transformedBetreuung.institutionStammdaten,
                    betreuung.institutionStammdaten);
                expect(transformedBetreuung.betreuungsstatus).toEqual(betreuung.betreuungsstatus);
                expect(transformedBetreuung.betreuungNummer).toEqual(betreuung.betreuungNummer);
                expect(transformedBetreuung.betreuungspensumContainers[0])
                    .toEqual(betreuung.betreuungspensumContainers[0]);
            });
        });
        describe('parseBetreuungspensum', () => {
            it('should transform TSBetreuungspensum to REST object and back', () => {
                const betreuungspensum = new TSBetreuungspensum(
                    TSPensumUnits.PERCENTAGE,
                    false,
                    monatlicheBetreuungskosten200,
                    pensum25,
                    new TSDateRange(DateUtil.today(), DateUtil.today()));
                TestDataUtil.setAbstractMutableFieldsUndefined(betreuungspensum);

                const restBetreuungspensum = ebeguRestUtil.betreuungspensumToRestObject({}, betreuungspensum);
                expect(restBetreuungspensum).toBeDefined();
                expect(restBetreuungspensum.pensum).toEqual(betreuungspensum.pensum);

                const transformedBetreuungspensum = ebeguRestUtil.parseBetreuungspensum(new TSBetreuungspensum(),
                    restBetreuungspensum);

                expect(transformedBetreuungspensum).toBeDefined();
                TestDataUtil.checkGueltigkeitAndSetIfSame(transformedBetreuungspensum, betreuungspensum);
                expect(transformedBetreuungspensum).toEqual(betreuungspensum);
            });
        });
        describe('parseInstitutionStammdaten()', () => {
            it('should transform TSInstitutionStammdaten to REST object and back', () => {
                const myInstitution = createInstitution();
                const tsInstStammdatenTagesschule = new TSInstitutionStammdatenTagesschule();
                const tsModul = new TSModulTagesschule();
                TestDataUtil.setAbstractMutableFieldsUndefined(tsModul);
                tsInstStammdatenTagesschule.moduleTagesschule = [tsModul];
                tsInstStammdatenTagesschule.gueltigkeit = new TSDateRange();
                TestDataUtil.setAbstractMutableFieldsUndefined(tsInstStammdatenTagesschule);
                const myInstitutionStammdaten = new TSInstitutionStammdaten('iban',
                    TSBetreuungsangebotTyp.KITA,
                    myInstitution,
                    undefined,
                    new TSDateRange(DateUtil.today(), DateUtil.today()),
                    '',
                    undefined,
                    tsInstStammdatenTagesschule);
                TestDataUtil.setAbstractMutableFieldsUndefined(myInstitutionStammdaten);

                const restInstitutionStammdaten = ebeguRestUtil.institutionStammdatenToRestObject({},
                    myInstitutionStammdaten);
                expect(restInstitutionStammdaten).toBeDefined();
                expect(restInstitutionStammdaten.iban).toEqual(myInstitutionStammdaten.iban);
                expect(restInstitutionStammdaten.gueltigAb).toEqual(DateUtil.momentToLocalDate(myInstitutionStammdaten.gueltigkeit.gueltigAb));
                expect(restInstitutionStammdaten.gueltigBis).toEqual(DateUtil.momentToLocalDate(myInstitutionStammdaten.gueltigkeit.gueltigBis));
                expect(restInstitutionStammdaten.betreuungsangebotTyp).toEqual(myInstitutionStammdaten.betreuungsangebotTyp);
                expect(restInstitutionStammdaten.institution.name).toEqual(myInstitutionStammdaten.institution.name);
                expect(restInstitutionStammdaten.institutionStammdatenTagesschule).toBeDefined();
                expect(restInstitutionStammdaten.institutionStammdatenTagesschule.moduleTagesschule).toBeDefined();
                expect(restInstitutionStammdaten.institutionStammdatenTagesschule.moduleTagesschule.length).toBe(1);
                expect(restInstitutionStammdaten.institutionStammdatenTagesschule.moduleTagesschule[0].wochentag)
                    .toBeUndefined();

                const transformedInstitutionStammdaten = ebeguRestUtil.parseInstitutionStammdaten(new TSInstitutionStammdaten(),
                    restInstitutionStammdaten);

                TestDataUtil.checkGueltigkeitAndSetIfSame(transformedInstitutionStammdaten, myInstitutionStammdaten);
                expect(transformedInstitutionStammdaten).toEqual(myInstitutionStammdaten);
            });
        });
        describe('parseErwerbspensenContainer()', () => {
            it('should transform TSErwerbspensum to REST object and back', () => {
                const erwerbspensumContainer = TestDataUtil.createErwerbspensumContainer();
                const erwerbspensumJA = erwerbspensumContainer.erwerbspensumJA;

                const restErwerbspensum = ebeguRestUtil.erwerbspensumToRestObject({},
                    erwerbspensumContainer.erwerbspensumJA);
                expect(restErwerbspensum).toBeDefined();
                expect(restErwerbspensum.taetigkeit).toEqual(erwerbspensumJA.taetigkeit);
                expect(restErwerbspensum.pensum).toEqual(erwerbspensumJA.pensum);
                expect(restErwerbspensum.gueltigAb)
                    .toEqual(DateUtil.momentToLocalDate(erwerbspensumJA.gueltigkeit.gueltigAb));
                expect(restErwerbspensum.gueltigBis)
                    .toEqual(DateUtil.momentToLocalDate(erwerbspensumJA.gueltigkeit.gueltigBis));
                expect(restErwerbspensum.zuschlagZuErwerbspensum).toEqual(erwerbspensumJA.zuschlagZuErwerbspensum);
                expect(restErwerbspensum.zuschlagsprozent).toEqual(erwerbspensumJA.zuschlagsprozent);
                expect(restErwerbspensum.zuschlagsgrund).toEqual(erwerbspensumJA.zuschlagsgrund);

                const transformedErwerbspensum = ebeguRestUtil.parseErwerbspensum(new TSErwerbspensum(),
                    restErwerbspensum);

                TestDataUtil.checkGueltigkeitAndSetIfSame(transformedErwerbspensum, erwerbspensumJA);
                expect(transformedErwerbspensum).toEqual(erwerbspensumJA);
            });
        });
        describe('parseGesuchsperiode()', () => {
            it('should transfrom TSGesuchsperiode to REST Obj and back', () => {
                const myGesuchsperiode = new TSGesuchsperiode(TSGesuchsperiodeStatus.AKTIV,
                    new TSDateRange(undefined, undefined));
                TestDataUtil.setAbstractMutableFieldsUndefined(myGesuchsperiode);

                const restGesuchsperiode = ebeguRestUtil.gesuchsperiodeToRestObject({}, myGesuchsperiode);
                expect(restGesuchsperiode).toBeDefined();

                const transformedGesuchsperiode = ebeguRestUtil.parseGesuchsperiode(new TSGesuchsperiode(),
                    restGesuchsperiode);
                expect(transformedGesuchsperiode).toBeDefined();
                expect(myGesuchsperiode.status).toBe(TSGesuchsperiodeStatus.AKTIV);
                expect(myGesuchsperiode).toEqual(transformedGesuchsperiode);

            });
        });
        describe('parseAntragDTO()', () => {
            it('should transform TSAntragDTO to REST Obj and back', () => {
                const tsGesuchsperiode = new TSGesuchsperiode(TSGesuchsperiodeStatus.AKTIV,
                    new TSDateRange(undefined, undefined));
                TestDataUtil.setAbstractMutableFieldsUndefined(tsGesuchsperiode);
                const fallNummer = 123;
                const myPendenz = new TSAntragDTO('id1', fallNummer, 'name', TSAntragTyp.ERSTGESUCH, DateUtil.today(),
                    undefined, DateUtil.now(), [TSBetreuungsangebotTyp.KITA], ['Inst1, Inst2'], 'Juan Arbolado');

                const restPendenz = ebeguRestUtil.antragDTOToRestObject({}, myPendenz);
                expect(restPendenz).toBeDefined();

                const transformedPendenz = ebeguRestUtil.parseAntragDTO(new TSAntragDTO(), restPendenz);
                expect(transformedPendenz).toBeDefined();
                expect(transformedPendenz.eingangsdatum.isSame(myPendenz.eingangsdatum)).toBe(true);
                transformedPendenz.eingangsdatum = myPendenz.eingangsdatum;
                expect(transformedPendenz.aenderungsdatum.isSame(myPendenz.aenderungsdatum)).toBe(true);
                transformedPendenz.aenderungsdatum = myPendenz.aenderungsdatum;
                expect(transformedPendenz).toEqual(myPendenz);
            });
        });
        describe('parseVerfuegung()', () => {
            it('should transform a REST Verfuegung to TS Obj', () => {
                const restVerfuegung: any = {};
                restVerfuegung.generatedBemerkungen = 'generated';
                restVerfuegung.manuelleBemerkungen = 'manuell';
                restVerfuegung.zeitabschnitte = {};

                const verfuegungTS = ebeguRestUtil.parseVerfuegung(new TSVerfuegung(), restVerfuegung);

                expect(verfuegungTS).toBeDefined();
                expect(verfuegungTS.generatedBemerkungen).toEqual(restVerfuegung.generatedBemerkungen);
                expect(verfuegungTS.manuelleBemerkungen).toEqual(restVerfuegung.manuelleBemerkungen);
                expect(verfuegungTS.zeitabschnitte).toBeDefined();
            });
        });
        describe('parseVerfuegungZeitabschnitt()', () => {
            it('should transform a REST VerfuegungZeitabschnitt to TS Obj', () => {
                const restVerfuegungZeitabschnitt: any = {};
                restVerfuegungZeitabschnitt.abzugFamGroesse = 1;
                restVerfuegungZeitabschnitt.anspruchberechtigtesPensum = 2;
                restVerfuegungZeitabschnitt.anspruchspensumRest = 3;
                restVerfuegungZeitabschnitt.betreuungspensum = 5;
                restVerfuegungZeitabschnitt.betreuungsstunden = 6;
                restVerfuegungZeitabschnitt.elternbeitrag = 7;
                restVerfuegungZeitabschnitt.erwerbspensumGS1 = 8;
                restVerfuegungZeitabschnitt.erwerbspensumGS2 = 9;
                restVerfuegungZeitabschnitt.fachstellenpensum = 10;
                restVerfuegungZeitabschnitt.massgebendesEinkommenVorAbzugFamgr = 11;
                restVerfuegungZeitabschnitt.vollkosten = 12;
                restVerfuegungZeitabschnitt.bemerkungen = 'bemerkung1';
                restVerfuegungZeitabschnitt.zahlungsstatus = TSVerfuegungZeitabschnittZahlungsstatus.NEU;

                const verfuegungTS = ebeguRestUtil.parseVerfuegungZeitabschnitt(new TSVerfuegungZeitabschnitt(),
                    restVerfuegungZeitabschnitt);

                expect(verfuegungTS).toBeDefined();
                expect(verfuegungTS.abzugFamGroesse).toEqual(restVerfuegungZeitabschnitt.abzugFamGroesse);
                expect(verfuegungTS.anspruchberechtigtesPensum)
                    .toEqual(restVerfuegungZeitabschnitt.anspruchberechtigtesPensum);
                expect(verfuegungTS.anspruchspensumRest).toEqual(restVerfuegungZeitabschnitt.anspruchspensumRest);
                expect(verfuegungTS.betreuungspensum).toEqual(restVerfuegungZeitabschnitt.betreuungspensum);
                expect(verfuegungTS.betreuungsstunden).toEqual(restVerfuegungZeitabschnitt.betreuungsstunden);
                expect(verfuegungTS.elternbeitrag).toEqual(restVerfuegungZeitabschnitt.elternbeitrag);
                expect(verfuegungTS.erwerbspensumGS1).toEqual(restVerfuegungZeitabschnitt.erwerbspensumGS1);
                expect(verfuegungTS.erwerbspensumGS2).toEqual(restVerfuegungZeitabschnitt.erwerbspensumGS2);
                expect(verfuegungTS.fachstellenpensum).toEqual(restVerfuegungZeitabschnitt.fachstellenpensum);
                expect(verfuegungTS.massgebendesEinkommenVorAbzugFamgr)
                    .toEqual(restVerfuegungZeitabschnitt.massgebendesEinkommenVorAbzugFamgr);
                expect(verfuegungTS.vollkosten).toEqual(restVerfuegungZeitabschnitt.vollkosten);
                expect(verfuegungTS.bemerkungen).toEqual(restVerfuegungZeitabschnitt.bemerkungen);
                expect(verfuegungTS.zahlungsstatus).toEqual(restVerfuegungZeitabschnitt.zahlungsstatus);
            });
        });
    });

    function createInstitution(): TSInstitution {
        const traegerschaft = new TSTraegerschaft('myTraegerschaft');
        TestDataUtil.setAbstractMutableFieldsUndefined(traegerschaft);
        const mandant = new TSMandant('myMandant');
        TestDataUtil.setAbstractMutableFieldsUndefined(mandant);
        const myInstitution = new TSInstitution('myInstitution', traegerschaft, mandant);
        TestDataUtil.setAbstractMutableFieldsUndefined(myInstitution);
        return myInstitution;
    }

    function createGesuchsteller(): TSGesuchstellerContainer {
        const myGesuchstellerCont = new TSGesuchstellerContainer();
        TestDataUtil.setAbstractMutableFieldsUndefined(myGesuchstellerCont);
        myGesuchstellerCont.id = 'containerID';
        const myGesuchsteller = new TSGesuchsteller();
        TestDataUtil.setAbstractMutableFieldsUndefined(myGesuchsteller);
        myGesuchsteller.vorname = 'Til';
        myGesuchsteller.nachname = 'TestGesuchsteller';
        myGesuchsteller.id = 'mytestid';
        myGesuchsteller.timestampErstellt = undefined;
        myGesuchsteller.timestampMutiert = undefined;
        myGesuchsteller.geschlecht = TSGeschlecht.MAENNLICH;
        myGesuchsteller.telefon = '+41 76 300 12 34';
        myGesuchsteller.mobile = '+41 76 300 12 34';
        myGesuchsteller.mail = 'Til.Testgesuchsteller@example.com';
        myGesuchstellerCont.korrespondenzAdresse = undefined;
        myGesuchstellerCont.rechnungsAdresse = undefined;
        myGesuchstellerCont.adressen = [];
        myGesuchstellerCont.finanzielleSituationContainer = undefined;
        myGesuchstellerCont.einkommensverschlechterungContainer = undefined;
        myGesuchstellerCont.gesuchstellerJA = myGesuchsteller;
        return myGesuchstellerCont;
    }
});
