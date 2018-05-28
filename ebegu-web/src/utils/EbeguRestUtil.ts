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

import TSDokumenteDTO from '../models/dto/TSDokumenteDTO';
import TSFinanzielleSituationResultateDTO from '../models/dto/TSFinanzielleSituationResultateDTO';
import TSQuickSearchResult from '../models/dto/TSQuickSearchResult';
import TSSearchResultEntry from '../models/dto/TSSearchResultEntry';
import {TSAdressetyp} from '../models/enums/TSAdressetyp';
import TSAbstractAntragEntity from '../models/TSAbstractAntragEntity';
import {TSAbstractDateRangedEntity} from '../models/TSAbstractDateRangedEntity';
import TSAbstractEntity from '../models/TSAbstractEntity';
import TSAbstractFinanzielleSituation from '../models/TSAbstractFinanzielleSituation';
import {TSAbstractPensumEntity} from '../models/TSAbstractPensumEntity';
import TSAbstractPersonEntity from '../models/TSAbstractPersonEntity';
import TSAbwesenheit from '../models/TSAbwesenheit';
import TSAbwesenheitContainer from '../models/TSAbwesenheitContainer';
import TSAdresse from '../models/TSAdresse';
import TSAdresseContainer from '../models/TSAdresseContainer';
import TSAntragDTO from '../models/TSAntragDTO';
import TSAntragStatusHistory from '../models/TSAntragStatusHistory';
import TSApplicationProperty from '../models/TSApplicationProperty';
import TSBelegungFerieninsel from '../models/TSBelegungFerieninsel';
import TSBelegungFerieninselTag from '../models/TSBelegungFerieninselTag';
import TSBelegungTagesschule from '../models/TSBelegungTagesschule';
import TSBerechtigung from '../models/TSBerechtigung';
import TSBerechtigungHistory from '../models/TSBerechtigungHistory';
import TSBetreuung from '../models/TSBetreuung';
import TSBetreuungsmitteilung from '../models/TSBetreuungsmitteilung';
import TSBetreuungsmitteilungPensum from '../models/TSBetreuungsmitteilungPensum';
import TSBetreuungspensum from '../models/TSBetreuungspensum';
import TSBetreuungspensumContainer from '../models/TSBetreuungspensumContainer';
import TSDokument from '../models/TSDokument';
import TSDokumentGrund from '../models/TSDokumentGrund';
import TSDownloadFile from '../models/TSDownloadFile';
import TSEbeguParameter from '../models/TSEbeguParameter';
import TSEbeguVorlage from '../models/TSEbeguVorlage';
import TSEinkommensverschlechterung from '../models/TSEinkommensverschlechterung';
import TSEinkommensverschlechterungContainer from '../models/TSEinkommensverschlechterungContainer';
import TSEinkommensverschlechterungInfo from '../models/TSEinkommensverschlechterungInfo';
import TSEinkommensverschlechterungInfoContainer from '../models/TSEinkommensverschlechterungInfoContainer';
import TSErwerbspensum from '../models/TSErwerbspensum';
import TSErwerbspensumContainer from '../models/TSErwerbspensumContainer';
import TSEWKAdresse from '../models/TSEWKAdresse';
import TSEWKBeziehung from '../models/TSEWKBeziehung';
import TSEWKEinwohnercode from '../models/TSEWKEinwohnercode';
import TSEWKPerson from '../models/TSEWKPerson';
import TSEWKResultat from '../models/TSEWKResultat';
import {TSFachstelle} from '../models/TSFachstelle';
import TSFall from '../models/TSFall';
import TSFallAntragDTO from '../models/TSFallAntragDTO';
import TSFamiliensituation from '../models/TSFamiliensituation';
import TSFamiliensituationContainer from '../models/TSFamiliensituationContainer';
import TSFerieninselStammdaten from '../models/TSFerieninselStammdaten';
import TSFerieninselZeitraum from '../models/TSFerieninselZeitraum';
import TSFile from '../models/TSFile';
import TSFinanzielleSituation from '../models/TSFinanzielleSituation';
import TSFinanzielleSituationContainer from '../models/TSFinanzielleSituationContainer';
import TSFinanzModel from '../models/TSFinanzModel';
import TSGesuch from '../models/TSGesuch';
import TSGesuchsperiode from '../models/TSGesuchsperiode';
import TSGesuchsteller from '../models/TSGesuchsteller';
import TSGesuchstellerContainer from '../models/TSGesuchstellerContainer';
import TSInstitution from '../models/TSInstitution';
import TSInstitutionStammdaten from '../models/TSInstitutionStammdaten';
import TSInstitutionStammdatenFerieninsel from '../models/TSInstitutionStammdatenFerieninsel';
import TSInstitutionStammdatenTagesschule from '../models/TSInstitutionStammdatenTagesschule';
import TSKind from '../models/TSKind';
import TSKindContainer from '../models/TSKindContainer';
import TSKindDublette from '../models/TSKindDublette';
import TSMahnung from '../models/TSMahnung';
import {TSMandant} from '../models/TSMandant';
import TSMitteilung from '../models/TSMitteilung';
import TSModulTagesschule from '../models/TSModulTagesschule';
import TSPendenzBetreuung from '../models/TSPendenzBetreuung';
import {TSPensumFachstelle} from '../models/TSPensumFachstelle';
import {TSTraegerschaft} from '../models/TSTraegerschaft';
import TSUser from '../models/TSUser';
import TSVerfuegung from '../models/TSVerfuegung';
import TSVerfuegungZeitabschnitt from '../models/TSVerfuegungZeitabschnitt';
import TSVorlage from '../models/TSVorlage';
import TSWizardStep from '../models/TSWizardStep';
import TSZahlung from '../models/TSZahlung';
import TSZahlungsauftrag from '../models/TSZahlungsauftrag';
import {TSDateRange} from '../models/types/TSDateRange';
import TSLand from '../models/types/TSLand';
import DateUtil from './DateUtil';
import EbeguUtil from './EbeguUtil';
import TSAnmeldungDTO from '../models/TSAnmeldungDTO';
import TSWorkJob from '../models/TSWorkJob';
import TSBatchJobInformation from '../models/TSBatchJobInformation';

export default class EbeguRestUtil {
    static $inject = ['EbeguUtil'];

    /* @ngInject */
    constructor(private ebeguUtil: EbeguUtil) {
    }

    /**
     * Wandelt Data in einen TSApplicationProperty Array um, welches danach zurueckgeliefert wird
     * @param data
     * @returns {TSApplicationProperty[]}
     */
    public parseApplicationProperties(data: any): TSApplicationProperty[] {
        let appProperties: TSApplicationProperty[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                appProperties[i] = this.parseApplicationProperty(new TSApplicationProperty('', ''), data[i]);
            }
        } else {
            appProperties[0] = this.parseApplicationProperty(new TSApplicationProperty('', ''), data);
        }
        return appProperties;
    }

    /**
     * Wandelt die receivedAppProperty in einem parsedAppProperty um.
     * @param parsedAppProperty
     * @param receivedAppProperty
     * @returns {TSApplicationProperty}
     */
    public parseApplicationProperty(parsedAppProperty: TSApplicationProperty, receivedAppProperty: any): TSApplicationProperty {
        this.parseAbstractEntity(parsedAppProperty, receivedAppProperty);
        parsedAppProperty.name = receivedAppProperty.name;
        parsedAppProperty.value = receivedAppProperty.value;
        return parsedAppProperty;
    }

    public parseEbeguParameters(data: any): TSEbeguParameter[] {
        let ebeguParameters: TSEbeguParameter[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                ebeguParameters[i] = this.parseEbeguParameter(new TSEbeguParameter(), data[i]);
            }
        } else {
            ebeguParameters[0] = this.parseEbeguParameter(new TSEbeguParameter(), data);
        }
        return ebeguParameters;
    }

    public parseEbeguParameter(ebeguParameterTS: TSEbeguParameter, receivedEbeguParameter: any): TSEbeguParameter {
        if (receivedEbeguParameter) {
            this.parseDateRangeEntity(ebeguParameterTS, receivedEbeguParameter);
            ebeguParameterTS.name = receivedEbeguParameter.name;
            ebeguParameterTS.value = receivedEbeguParameter.value;
            ebeguParameterTS.proGesuchsperiode = receivedEbeguParameter.proGesuchsperiode;
            return ebeguParameterTS;
        }
        return undefined;
    }

    public ebeguParameterToRestObject(restEbeguParameter: any, ebeguParameter: TSEbeguParameter): TSEbeguParameter {
        if (ebeguParameter) {
            this.abstractDateRangeEntityToRestObject(restEbeguParameter, ebeguParameter);
            restEbeguParameter.name = ebeguParameter.name;
            restEbeguParameter.value = ebeguParameter.value;
            return restEbeguParameter;
        }
        return undefined;
    }

    public parseEbeguVorlages(data: any): TSEbeguVorlage[] {
        let ebeguVorlages: TSEbeguVorlage[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                ebeguVorlages[i] = this.parseEbeguVorlage(new TSEbeguVorlage(), data[i]);
            }
        } else {
            ebeguVorlages[0] = this.parseEbeguVorlage(new TSEbeguVorlage(), data);
        }
        return ebeguVorlages;
    }

    public parseEbeguVorlage(ebeguVorlageTS: TSEbeguVorlage, receivedEbeguVorlage: any): TSEbeguVorlage {
        if (receivedEbeguVorlage) {
            this.parseDateRangeEntity(ebeguVorlageTS, receivedEbeguVorlage);
            ebeguVorlageTS.name = receivedEbeguVorlage.name;
            ebeguVorlageTS.vorlage = this.parseVorlage(new TSVorlage, receivedEbeguVorlage.vorlage);
            ebeguVorlageTS.proGesuchsperiode = receivedEbeguVorlage.proGesuchsperiode;
            return ebeguVorlageTS;
        }
        return undefined;
    }

    public parseVorlage(vorlageTS: TSVorlage, receivedVorlage: any): TSVorlage {
        if (receivedVorlage) {
            this.parseAbstractFileEntity(vorlageTS, receivedVorlage);
            return vorlageTS;
        }
        return undefined;
    }

    public ebeguVorlageToRestObject(restEbeguVorlage: any, ebeguVorlage: TSEbeguVorlage): TSEbeguVorlage {
        if (ebeguVorlage) {
            this.abstractDateRangeEntityToRestObject(restEbeguVorlage, ebeguVorlage);
            restEbeguVorlage.name = ebeguVorlage.name;
            restEbeguVorlage.value = this.vorlageToRestObject({}, ebeguVorlage.vorlage);
            return restEbeguVorlage;
        }
        return undefined;
    }

    public vorlageToRestObject(restVorlage: any, vorlage: TSVorlage): TSVorlage {
        if (vorlage) {
            this.abstractFileEntityToRestObject(restVorlage, vorlage);
            return restVorlage;
        }
        return undefined;
    }

    private parseAbstractFileEntity(fileTS: TSFile, fileFromServer: any) {
        this.parseAbstractEntity(fileTS, fileFromServer);
        fileTS.filename = fileFromServer.filename;
        fileTS.filepfad = fileFromServer.filepfad;
        fileTS.filesize = fileFromServer.filesize;
        return fileTS;
    }

    private abstractFileEntityToRestObject(restObject: any, typescriptObject: TSFile) {
        this.abstractEntityToRestObject(restObject, typescriptObject);
        restObject.filename = typescriptObject.filename;
        restObject.filepfad = typescriptObject.filepfad;
        restObject.filesize = typescriptObject.filesize;
        return restObject;
    }

    private parseAbstractEntity(parsedAbstractEntity: TSAbstractEntity, receivedAbstractEntity: any): void {
        parsedAbstractEntity.id = receivedAbstractEntity.id;
        parsedAbstractEntity.timestampErstellt = DateUtil.localDateTimeToMoment(receivedAbstractEntity.timestampErstellt);
        parsedAbstractEntity.timestampMutiert = DateUtil.localDateTimeToMoment(receivedAbstractEntity.timestampMutiert);
        parsedAbstractEntity.vorgaengerId = receivedAbstractEntity.vorgaengerId;
    }

    private abstractEntityToRestObject(restObject: any, typescriptObject: TSAbstractEntity) {
        restObject.id = typescriptObject.id;
        restObject.vorgaengerId = typescriptObject.vorgaengerId;
        if (typescriptObject.timestampErstellt) {
            restObject.timestampErstellt = DateUtil.momentToLocalDateTime(typescriptObject.timestampErstellt);
        }
        if (typescriptObject.timestampMutiert) {
            restObject.timestampMutiert = DateUtil.momentToLocalDateTime(typescriptObject.timestampMutiert);
        }
    }

    private parseAbstractPersonEntity(personObjectTS: TSAbstractPersonEntity, receivedPersonObject: any): void {
        this.parseAbstractEntity(personObjectTS, receivedPersonObject);
        personObjectTS.vorname = receivedPersonObject.vorname;
        personObjectTS.nachname = receivedPersonObject.nachname;
        personObjectTS.geburtsdatum = DateUtil.localDateToMoment(receivedPersonObject.geburtsdatum);
        personObjectTS.geschlecht = receivedPersonObject.geschlecht;
    }

    private abstractPersonEntitytoRestObject(restPersonObject: any, personObject: TSAbstractPersonEntity): void {
        this.abstractEntityToRestObject(restPersonObject, personObject);
        restPersonObject.vorname = personObject.vorname;
        restPersonObject.nachname = personObject.nachname;
        restPersonObject.geburtsdatum = DateUtil.momentToLocalDate(personObject.geburtsdatum);
        restPersonObject.geschlecht = personObject.geschlecht;
    }

    private abstractDateRangeEntityToRestObject(restObj: any, dateRangedEntity: TSAbstractDateRangedEntity) {
        this.abstractEntityToRestObject(restObj, dateRangedEntity);
        if (dateRangedEntity && dateRangedEntity.gueltigkeit) {
            restObj.gueltigAb = DateUtil.momentToLocalDate(dateRangedEntity.gueltigkeit.gueltigAb);
            restObj.gueltigBis = DateUtil.momentToLocalDate(dateRangedEntity.gueltigkeit.gueltigBis);
        }
    }

    private parseDateRangeEntity(parsedObject: TSAbstractDateRangedEntity, receivedAppProperty: any) {
        this.parseAbstractEntity(parsedObject, receivedAppProperty);
        parsedObject.gueltigkeit = new TSDateRange(DateUtil.localDateToMoment(receivedAppProperty.gueltigAb), DateUtil.localDateToMoment(receivedAppProperty.gueltigBis));
    }

    private abstractPensumEntityToRestObject(restObj: any, pensumEntity: TSAbstractPensumEntity) {
        this.abstractDateRangeEntityToRestObject(restObj, pensumEntity);
        restObj.pensum = pensumEntity.pensum;
    }

    private parseAbstractPensumEntity(betreuungspensumTS: TSAbstractPensumEntity, betreuungspensumFromServer: any) {
        this.parseDateRangeEntity(betreuungspensumTS, betreuungspensumFromServer);
        betreuungspensumTS.pensum = betreuungspensumFromServer.pensum;
    }

    private abstractAntragEntityToRestObject(restObj: any, antragEntity: TSAbstractAntragEntity) {
        this.abstractEntityToRestObject(restObj, antragEntity);
        restObj.fall = this.fallToRestObject({}, antragEntity.fall);
        restObj.gesuchsperiode = this.gesuchsperiodeToRestObject({}, antragEntity.gesuchsperiode);
        restObj.eingangsdatum = DateUtil.momentToLocalDate(antragEntity.eingangsdatum);
        restObj.freigabeDatum = DateUtil.momentToLocalDate(antragEntity.freigabeDatum);
        restObj.status = antragEntity.status;
        restObj.typ = antragEntity.typ;
        restObj.eingangsart = antragEntity.eingangsart;
    }

    private parseAbstractAntragEntity(antragTS: TSAbstractAntragEntity, antragFromServer: any) {
        this.parseAbstractEntity(antragTS, antragFromServer);
        antragTS.fall = this.parseFall(new TSFall(), antragFromServer.fall);
        antragTS.gesuchsperiode = this.parseGesuchsperiode(new TSGesuchsperiode(), antragFromServer.gesuchsperiode);
        antragTS.eingangsdatum = DateUtil.localDateToMoment(antragFromServer.eingangsdatum);
        antragTS.freigabeDatum = DateUtil.localDateToMoment(antragFromServer.freigabeDatum);
        antragTS.status = antragFromServer.status;
        antragTS.typ = antragFromServer.typ;
        antragTS.eingangsart = antragFromServer.eingangsart;
    }

    private adressenListToRestObject(adressen: Array<TSAdresse>): Array<any> {
        let restAdressenList: Array<any> = [];
        if (adressen) {
            for (let i = 0; i < adressen.length; i++) {
                restAdressenList.push(this.adresseToRestObject({}, adressen[i]));
            }
        }
        return restAdressenList;
    }

    public adresseToRestObject(restAdresse: any, adresse: TSAdresse): TSAdresse {
        if (adresse) {
            this.abstractDateRangeEntityToRestObject(restAdresse, adresse);
            restAdresse.strasse = adresse.strasse;
            restAdresse.hausnummer = adresse.hausnummer;
            restAdresse.zusatzzeile = adresse.zusatzzeile;
            restAdresse.plz = adresse.plz;
            restAdresse.ort = adresse.ort;
            restAdresse.land = adresse.land;
            restAdresse.gemeinde = adresse.gemeinde;
            restAdresse.adresseTyp = TSAdressetyp[adresse.adresseTyp];
            restAdresse.nichtInGemeinde = adresse.nichtInGemeinde;
            restAdresse.organisation = adresse.organisation;
            return restAdresse;
        }
        return undefined;

    }

    private parseAdressenList(adressen: Array<any>): Array<TSAdresse> {
        let adressenList: Array<TSAdresse> = [];
        if (adressen) {
            for (let i = 0; i < adressen.length; i++) {
                adressenList.push(this.parseAdresse(new TSAdresse(), adressen[i]));
            }
        }
        return adressenList;
    }

    public parseAdresse(adresseTS: TSAdresse, receivedAdresse: any): TSAdresse {
        if (receivedAdresse) {
            this.parseDateRangeEntity(adresseTS, receivedAdresse);
            adresseTS.strasse = receivedAdresse.strasse;
            adresseTS.hausnummer = receivedAdresse.hausnummer;
            adresseTS.zusatzzeile = receivedAdresse.zusatzzeile;
            adresseTS.plz = receivedAdresse.plz;
            adresseTS.ort = receivedAdresse.ort;
            adresseTS.land = (this.landCodeToTSLand(receivedAdresse.land)) ? this.landCodeToTSLand(receivedAdresse.land).code : undefined;
            adresseTS.gemeinde = receivedAdresse.gemeinde;
            adresseTS.adresseTyp = receivedAdresse.adresseTyp;
            adresseTS.nichtInGemeinde = receivedAdresse.nichtInGemeinde;
            adresseTS.organisation = receivedAdresse.organisation;
            return adresseTS;
        }
        return undefined;
    }

    /**
     * Nimmt den eingegebenen Code und erzeugt ein TSLand Objekt mit dem Code und
     * seine Uebersetzung.
     * @param landCode
     * @returns {any}
     */
    public landCodeToTSLand(landCode: string): TSLand {
        if (landCode) {
            let translationKey = this.landCodeToTSLandCode(landCode);
            return new TSLand(landCode, this.ebeguUtil.translateString(translationKey));
        }
        return undefined;
    }

    /**
     * Fügt das 'Land_' dem eingegebenen Landcode hinzu.
     * @param landCode
     * @returns {any}
     */
    public landCodeToTSLandCode(landCode: string): string {
        if (landCode) {
            if (landCode.lastIndexOf('Land_', 0) !== 0) {
                return 'Land_' + landCode;
            }
        }
        return undefined;
    }

    public gesuchstellerToRestObject(restGesuchsteller: any, gesuchsteller: TSGesuchsteller): any {
        if (gesuchsteller) {
            this.abstractPersonEntitytoRestObject(restGesuchsteller, gesuchsteller);
            restGesuchsteller.mail = gesuchsteller.mail;
            restGesuchsteller.mobile = gesuchsteller.mobile || undefined;
            restGesuchsteller.telefon = gesuchsteller.telefon || undefined;
            restGesuchsteller.telefonAusland = gesuchsteller.telefonAusland || undefined;
            restGesuchsteller.diplomatenstatus = gesuchsteller.diplomatenstatus;
            restGesuchsteller.ewkPersonId = gesuchsteller.ewkPersonId;
            restGesuchsteller.ewkAbfrageDatum = DateUtil.momentToLocalDate(gesuchsteller.ewkAbfrageDatum);
            return restGesuchsteller;
        }
        return undefined;
    }

    public parseGesuchsteller(gesuchstellerTS: TSGesuchsteller, gesuchstellerFromServer: any): TSGesuchsteller {
        if (gesuchstellerFromServer) {
            this.parseAbstractPersonEntity(gesuchstellerTS, gesuchstellerFromServer);
            gesuchstellerTS.mail = gesuchstellerFromServer.mail;
            gesuchstellerTS.mobile = gesuchstellerFromServer.mobile;
            gesuchstellerTS.telefon = gesuchstellerFromServer.telefon;
            gesuchstellerTS.telefonAusland = gesuchstellerFromServer.telefonAusland;
            gesuchstellerTS.diplomatenstatus = gesuchstellerFromServer.diplomatenstatus;
            gesuchstellerTS.ewkPersonId = gesuchstellerFromServer.ewkPersonId;
            gesuchstellerTS.ewkAbfrageDatum = DateUtil.localDateToMoment(gesuchstellerFromServer.ewkAbfrageDatum);
            return gesuchstellerTS;
        }
        return undefined;
    }

    public parseErwerbspensumContainer(erwerbspensumContainer: TSErwerbspensumContainer, ewpContFromServer: any): TSErwerbspensumContainer {
        if (ewpContFromServer) {
            this.parseAbstractEntity(erwerbspensumContainer, ewpContFromServer);
            erwerbspensumContainer.erwerbspensumGS = this.parseErwerbspensum(erwerbspensumContainer.erwerbspensumGS || new TSErwerbspensum(), ewpContFromServer.erwerbspensumGS);
            erwerbspensumContainer.erwerbspensumJA = this.parseErwerbspensum(erwerbspensumContainer.erwerbspensumJA || new TSErwerbspensum(), ewpContFromServer.erwerbspensumJA);
            return erwerbspensumContainer;
        }
        return undefined;
    }

    public erwerbspensumContainerToRestObject(restEwpContainer: any, erwerbspensumContainer: TSErwerbspensumContainer): any {
        if (erwerbspensumContainer) {
            this.abstractEntityToRestObject(restEwpContainer, erwerbspensumContainer);
            restEwpContainer.erwerbspensumGS = this.erwerbspensumToRestObject({}, erwerbspensumContainer.erwerbspensumGS);
            restEwpContainer.erwerbspensumJA = this.erwerbspensumToRestObject({}, erwerbspensumContainer.erwerbspensumJA);
            return restEwpContainer;
        }
        return undefined;
    }

    public parseErwerbspensum(erwerbspensum: TSErwerbspensum, erwerbspensumFromServer: any): TSErwerbspensum {
        if (erwerbspensumFromServer) {
            this.parseAbstractPensumEntity(erwerbspensum, erwerbspensumFromServer);
            erwerbspensum.taetigkeit = erwerbspensumFromServer.taetigkeit;
            erwerbspensum.zuschlagsgrund = erwerbspensumFromServer.zuschlagsgrund;
            erwerbspensum.zuschlagsprozent = erwerbspensumFromServer.zuschlagsprozent;
            erwerbspensum.zuschlagZuErwerbspensum = erwerbspensumFromServer.zuschlagZuErwerbspensum;
            erwerbspensum.bezeichnung = erwerbspensumFromServer.bezeichnung;
            return erwerbspensum;
        } else {
            return undefined;
        }
    }

    public erwerbspensumToRestObject(restErwerbspensum: any, erwerbspensum: TSErwerbspensum): any {
        if (erwerbspensum) {
            this.abstractPensumEntityToRestObject(restErwerbspensum, erwerbspensum);
            restErwerbspensum.taetigkeit = erwerbspensum.taetigkeit;
            restErwerbspensum.zuschlagsgrund = erwerbspensum.zuschlagsgrund;
            restErwerbspensum.zuschlagsprozent = erwerbspensum.zuschlagsprozent;
            restErwerbspensum.zuschlagZuErwerbspensum = erwerbspensum.zuschlagZuErwerbspensum;
            restErwerbspensum.bezeichnung = erwerbspensum.bezeichnung;
            return restErwerbspensum;
        }
        return undefined;
    }

    public familiensituationToRestObject(restFamiliensituation: any, familiensituation: TSFamiliensituation): TSFamiliensituation {
        if (familiensituation) {
            this.abstractEntityToRestObject(restFamiliensituation, familiensituation);
            restFamiliensituation.familienstatus = familiensituation.familienstatus;
            restFamiliensituation.gesuchstellerKardinalitaet = familiensituation.gesuchstellerKardinalitaet;
            restFamiliensituation.gemeinsameSteuererklaerung = familiensituation.gemeinsameSteuererklaerung;
            restFamiliensituation.aenderungPer = DateUtil.momentToLocalDate(familiensituation.aenderungPer);
            restFamiliensituation.sozialhilfeBezueger = familiensituation.sozialhilfeBezueger;
            restFamiliensituation.verguenstigungGewuenscht = familiensituation.verguenstigungGewuenscht;
            return restFamiliensituation;
        }
        return undefined;
    }

    public einkommensverschlechterungInfoContainerToRestObject(restEinkommensverschlechterungInfoContainer: any,
        einkommensverschlechterungInfoContainer: TSEinkommensverschlechterungInfoContainer): TSEinkommensverschlechterungInfoContainer {
        if (einkommensverschlechterungInfoContainer) {
            this.abstractEntityToRestObject(restEinkommensverschlechterungInfoContainer, einkommensverschlechterungInfoContainer);
            if (einkommensverschlechterungInfoContainer.einkommensverschlechterungInfoGS) {
                restEinkommensverschlechterungInfoContainer.einkommensverschlechterungInfoGS =
                    this.einkommensverschlechterungInfoToRestObject({}, einkommensverschlechterungInfoContainer.einkommensverschlechterungInfoGS);
            }
            if (einkommensverschlechterungInfoContainer.einkommensverschlechterungInfoJA) {
                restEinkommensverschlechterungInfoContainer.einkommensverschlechterungInfoJA =
                    this.einkommensverschlechterungInfoToRestObject({}, einkommensverschlechterungInfoContainer.einkommensverschlechterungInfoJA);
            }
            return restEinkommensverschlechterungInfoContainer;
        }
        return undefined;
    }

    public einkommensverschlechterungInfoToRestObject(restEinkommensverschlechterungInfo: any,
        einkommensverschlechterungInfo: TSEinkommensverschlechterungInfo): TSEinkommensverschlechterungInfo {
        if (einkommensverschlechterungInfo) {
            this.abstractEntityToRestObject(restEinkommensverschlechterungInfo, einkommensverschlechterungInfo);
            restEinkommensverschlechterungInfo.einkommensverschlechterung = einkommensverschlechterungInfo.einkommensverschlechterung;
            restEinkommensverschlechterungInfo.ekvFuerBasisJahrPlus1 = einkommensverschlechterungInfo.ekvFuerBasisJahrPlus1;
            restEinkommensverschlechterungInfo.ekvFuerBasisJahrPlus2 = einkommensverschlechterungInfo.ekvFuerBasisJahrPlus2;
            restEinkommensverschlechterungInfo.grundFuerBasisJahrPlus1 = einkommensverschlechterungInfo.grundFuerBasisJahrPlus1;
            restEinkommensverschlechterungInfo.grundFuerBasisJahrPlus2 = einkommensverschlechterungInfo.grundFuerBasisJahrPlus2;
            restEinkommensverschlechterungInfo.stichtagFuerBasisJahrPlus1 = DateUtil.momentToLocalDate(einkommensverschlechterungInfo.stichtagFuerBasisJahrPlus1);
            restEinkommensverschlechterungInfo.stichtagFuerBasisJahrPlus2 = DateUtil.momentToLocalDate(einkommensverschlechterungInfo.stichtagFuerBasisJahrPlus2);
            restEinkommensverschlechterungInfo.gemeinsameSteuererklaerung_BjP1 = einkommensverschlechterungInfo.gemeinsameSteuererklaerung_BjP1;
            restEinkommensverschlechterungInfo.gemeinsameSteuererklaerung_BjP2 = einkommensverschlechterungInfo.gemeinsameSteuererklaerung_BjP2;
            restEinkommensverschlechterungInfo.ekvBasisJahrPlus1Annulliert = einkommensverschlechterungInfo.ekvBasisJahrPlus1Annulliert;
            restEinkommensverschlechterungInfo.ekvBasisJahrPlus2Annulliert = einkommensverschlechterungInfo.ekvBasisJahrPlus2Annulliert;
            return restEinkommensverschlechterungInfo;
        }
        return undefined;
    }

    public parseFamiliensituation(familiensituation: TSFamiliensituation, familiensituationFromServer: any): TSFamiliensituation {
        if (familiensituationFromServer) {
            this.parseAbstractEntity(familiensituation, familiensituationFromServer);
            familiensituation.familienstatus = familiensituationFromServer.familienstatus;
            familiensituation.gesuchstellerKardinalitaet = familiensituationFromServer.gesuchstellerKardinalitaet;
            familiensituation.gemeinsameSteuererklaerung = familiensituationFromServer.gemeinsameSteuererklaerung;
            familiensituation.aenderungPer = DateUtil.localDateToMoment(familiensituationFromServer.aenderungPer);
            familiensituation.sozialhilfeBezueger = familiensituationFromServer.sozialhilfeBezueger;
            familiensituation.verguenstigungGewuenscht = familiensituationFromServer.verguenstigungGewuenscht;
            return familiensituation;
        }
        return undefined;
    }

    public parseFamiliensituationContainer(containerTS: TSFamiliensituationContainer, containerFromServer: any): TSFamiliensituationContainer {
        if (containerFromServer) {
            this.parseAbstractEntity(containerTS, containerFromServer);

            containerTS.familiensituationGS = this.parseFamiliensituation(containerTS.familiensituationGS
                || new TSFamiliensituation(), containerFromServer.familiensituationGS);
            containerTS.familiensituationJA = this.parseFamiliensituation(containerTS.familiensituationJA
                || new TSFamiliensituation(), containerFromServer.familiensituationJA);
            containerTS.familiensituationErstgesuch = this.parseFamiliensituation(containerTS.familiensituationErstgesuch
                || new TSFamiliensituation(), containerFromServer.familiensituationErstgesuch);
            return containerTS;
        }
        return undefined;
    }

    public familiensituationContainerToRestObject(restFamiliensituationContainer: any,
        familiensituationContainer: TSFamiliensituationContainer): TSFamiliensituationContainer {
        if (familiensituationContainer) {
            this.abstractEntityToRestObject(restFamiliensituationContainer, familiensituationContainer);

            if (familiensituationContainer.familiensituationJA) {
                restFamiliensituationContainer.familiensituationJA =
                    this.familiensituationToRestObject({}, familiensituationContainer.familiensituationJA);
            }
            if (familiensituationContainer.familiensituationErstgesuch) {
                restFamiliensituationContainer.familiensituationErstgesuch =
                    this.familiensituationToRestObject({}, familiensituationContainer.familiensituationErstgesuch);
            }
            if (familiensituationContainer.familiensituationGS) {
                restFamiliensituationContainer.familiensituationGS =
                    this.familiensituationToRestObject({}, familiensituationContainer.familiensituationGS);
            }

            return restFamiliensituationContainer;
        }
        return undefined;
    }

    public parseEinkommensverschlechterungInfo(einkommensverschlechterungInfo: TSEinkommensverschlechterungInfo,
        einkommensverschlechterungInfoFromServer: any): TSEinkommensverschlechterungInfo {
        if (einkommensverschlechterungInfoFromServer) {
            this.parseAbstractEntity(einkommensverschlechterungInfo, einkommensverschlechterungInfoFromServer);
            einkommensverschlechterungInfo.einkommensverschlechterung = einkommensverschlechterungInfoFromServer.einkommensverschlechterung;
            einkommensverschlechterungInfo.ekvFuerBasisJahrPlus1 = einkommensverschlechterungInfoFromServer.ekvFuerBasisJahrPlus1;
            einkommensverschlechterungInfo.ekvFuerBasisJahrPlus2 = einkommensverschlechterungInfoFromServer.ekvFuerBasisJahrPlus2;
            einkommensverschlechterungInfo.grundFuerBasisJahrPlus1 = einkommensverschlechterungInfoFromServer.grundFuerBasisJahrPlus1;
            einkommensverschlechterungInfo.grundFuerBasisJahrPlus2 = einkommensverschlechterungInfoFromServer.grundFuerBasisJahrPlus2;
            einkommensverschlechterungInfo.stichtagFuerBasisJahrPlus1 = DateUtil.localDateToMoment(einkommensverschlechterungInfoFromServer.stichtagFuerBasisJahrPlus1);
            einkommensverschlechterungInfo.stichtagFuerBasisJahrPlus2 = DateUtil.localDateToMoment(einkommensverschlechterungInfoFromServer.stichtagFuerBasisJahrPlus2);
            einkommensverschlechterungInfo.gemeinsameSteuererklaerung_BjP1 = einkommensverschlechterungInfoFromServer.gemeinsameSteuererklaerung_BjP1;
            einkommensverschlechterungInfo.gemeinsameSteuererklaerung_BjP2 = einkommensverschlechterungInfoFromServer.gemeinsameSteuererklaerung_BjP2;
            einkommensverschlechterungInfo.ekvBasisJahrPlus1Annulliert = einkommensverschlechterungInfoFromServer.ekvBasisJahrPlus1Annulliert;
            einkommensverschlechterungInfo.ekvBasisJahrPlus2Annulliert = einkommensverschlechterungInfoFromServer.ekvBasisJahrPlus2Annulliert;
            return einkommensverschlechterungInfo;
        }
        return undefined;
    }

    public parseEinkommensverschlechterungInfoContainer(containerTS: TSEinkommensverschlechterungInfoContainer,
        containerFromServer: any): TSEinkommensverschlechterungInfoContainer {
        if (containerFromServer) {
            this.parseAbstractEntity(containerTS, containerFromServer);

            containerTS.einkommensverschlechterungInfoGS = this.parseEinkommensverschlechterungInfo(containerTS.einkommensverschlechterungInfoGS
                || new TSEinkommensverschlechterungInfo(), containerFromServer.einkommensverschlechterungInfoGS);
            containerTS.einkommensverschlechterungInfoJA = this.parseEinkommensverschlechterungInfo(containerTS.einkommensverschlechterungInfoJA
                || new TSEinkommensverschlechterungInfo(), containerFromServer.einkommensverschlechterungInfoJA);
            return containerTS;
        }
        return undefined;
    }

    public fallToRestObject(restFall: any, fall: TSFall): TSFall {
        if (fall) {
            this.abstractEntityToRestObject(restFall, fall);
            restFall.fallNummer = fall.fallNummer;
            restFall.verantwortlicher = this.userToRestObject({}, fall.verantwortlicher);
            restFall.verantwortlicherSCH = this.userToRestObject({}, fall.verantwortlicherSCH);
            restFall.nextNumberKind = fall.nextNumberKind;
            restFall.besitzer = this.userToRestObject({}, fall.besitzer);
            return restFall;
        }
        return undefined;

    }

    public parseFall(fallTS: TSFall, fallFromServer: any): TSFall {
        if (fallFromServer) {
            this.parseAbstractEntity(fallTS, fallFromServer);
            fallTS.fallNummer = fallFromServer.fallNummer;
            fallTS.verantwortlicher = this.parseUser(new TSUser(), fallFromServer.verantwortlicher);
            fallTS.verantwortlicherSCH = this.parseUser(new TSUser(), fallFromServer.verantwortlicherSCH);
            fallTS.nextNumberKind = fallFromServer.nextNumberKind;
            fallTS.besitzer = this.parseUser(new TSUser(), fallFromServer.besitzer);
            return fallTS;
        }
        return undefined;
    }

    public gesuchToRestObject(restGesuch: any, gesuch: TSGesuch): TSGesuch {
        this.abstractAntragEntityToRestObject(restGesuch, gesuch);
        restGesuch.einkommensverschlechterungInfoContainer = this.einkommensverschlechterungInfoContainerToRestObject({}, gesuch.einkommensverschlechterungInfoContainer);
        restGesuch.gesuchsteller1 = this.gesuchstellerContainerToRestObject({}, gesuch.gesuchsteller1);
        restGesuch.gesuchsteller2 = this.gesuchstellerContainerToRestObject({}, gesuch.gesuchsteller2);
        restGesuch.familiensituationContainer = this.familiensituationContainerToRestObject({}, gesuch.familiensituationContainer);
        restGesuch.bemerkungen = gesuch.bemerkungen;
        restGesuch.bemerkungenSTV = gesuch.bemerkungenSTV;
        restGesuch.bemerkungenPruefungSTV = gesuch.bemerkungenPruefungSTV;
        restGesuch.laufnummer = gesuch.laufnummer;
        restGesuch.gesuchBetreuungenStatus = gesuch.gesuchBetreuungenStatus;
        restGesuch.geprueftSTV = gesuch.geprueftSTV;
        restGesuch.hasFSDokument = gesuch.hasFSDokument;
        restGesuch.gesperrtWegenBeschwerde = gesuch.gesperrtWegenBeschwerde;
        restGesuch.datumGewarntNichtFreigegeben = DateUtil.momentToLocalDate(gesuch.datumGewarntNichtFreigegeben);
        restGesuch.datumGewarntFehlendeQuittung = DateUtil.momentToLocalDate(gesuch.datumGewarntFehlendeQuittung);
        restGesuch.timestampVerfuegt = DateUtil.momentToLocalDateTime(gesuch.timestampVerfuegt);
        restGesuch.gueltig = gesuch.gueltig;
        restGesuch.dokumenteHochgeladen = gesuch.dokumenteHochgeladen;
        restGesuch.finSitStatus = gesuch.finSitStatus;
        return restGesuch;
    }

    public parseGesuch(gesuchTS: TSGesuch, gesuchFromServer: any): TSGesuch {
        if (gesuchFromServer) {
            this.parseAbstractAntragEntity(gesuchTS, gesuchFromServer);
            gesuchTS.einkommensverschlechterungInfoContainer = this.parseEinkommensverschlechterungInfoContainer(
                new TSEinkommensverschlechterungInfoContainer(), gesuchFromServer.einkommensverschlechterungInfoContainer);
            gesuchTS.gesuchsteller1 = this.parseGesuchstellerContainer(new TSGesuchstellerContainer(), gesuchFromServer.gesuchsteller1);
            gesuchTS.gesuchsteller2 = this.parseGesuchstellerContainer(new TSGesuchstellerContainer(), gesuchFromServer.gesuchsteller2);
            gesuchTS.familiensituationContainer = this.parseFamiliensituationContainer(new TSFamiliensituationContainer(), gesuchFromServer.familiensituationContainer);
            gesuchTS.kindContainers = this.parseKindContainerList(gesuchFromServer.kindContainers);
            gesuchTS.bemerkungen = gesuchFromServer.bemerkungen;
            gesuchTS.bemerkungenSTV = gesuchFromServer.bemerkungenSTV;
            gesuchTS.bemerkungenPruefungSTV = gesuchFromServer.bemerkungenPruefungSTV;
            gesuchTS.laufnummer = gesuchFromServer.laufnummer;
            gesuchTS.gesuchBetreuungenStatus = gesuchFromServer.gesuchBetreuungenStatus;
            gesuchTS.geprueftSTV = gesuchFromServer.geprueftSTV;
            gesuchTS.hasFSDokument = gesuchFromServer.hasFSDokument;
            gesuchTS.gesperrtWegenBeschwerde = gesuchFromServer.gesperrtWegenBeschwerde;
            gesuchTS.datumGewarntNichtFreigegeben = DateUtil.localDateToMoment(gesuchFromServer.datumGewarntNichtFreigegeben);
            gesuchTS.datumGewarntFehlendeQuittung = DateUtil.localDateToMoment(gesuchFromServer.datumGewarntFehlendeQuittung);
            gesuchTS.timestampVerfuegt = DateUtil.localDateTimeToMoment(gesuchFromServer.timestampVerfuegt);
            gesuchTS.gueltig = gesuchFromServer.gueltig;
            gesuchTS.dokumenteHochgeladen = gesuchFromServer.dokumenteHochgeladen;
            gesuchTS.finSitStatus = gesuchFromServer.finSitStatus;
            return gesuchTS;
        }
        return undefined;
    }

    public fachstelleToRestObject(restFachstelle: any, fachstelle: TSFachstelle): any {
        this.abstractEntityToRestObject(restFachstelle, fachstelle);
        restFachstelle.name = fachstelle.name;
        restFachstelle.beschreibung = fachstelle.beschreibung;
        restFachstelle.behinderungsbestaetigung = fachstelle.behinderungsbestaetigung;
        return restFachstelle;
    }

    public parseFachstellen(data: any): TSFachstelle[] {
        let fachstellen: TSFachstelle[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                fachstellen[i] = this.parseFachstelle(new TSFachstelle(), data[i]);
            }
        } else {
            fachstellen[0] = this.parseFachstelle(new TSFachstelle(), data);
        }
        return fachstellen;
    }

    public parseFachstelle(parsedFachstelle: TSFachstelle, receivedFachstelle: any): TSFachstelle {
        this.parseAbstractEntity(parsedFachstelle, receivedFachstelle);
        parsedFachstelle.name = receivedFachstelle.name;
        parsedFachstelle.beschreibung = receivedFachstelle.beschreibung;
        parsedFachstelle.behinderungsbestaetigung = receivedFachstelle.behinderungsbestaetigung;
        return parsedFachstelle;
    }

    public mandantToRestObject(restMandant: any, mandant: TSMandant): any {
        if (mandant) {
            this.abstractEntityToRestObject(restMandant, mandant);
            restMandant.name = mandant.name;
            return restMandant;
        }
        return undefined;
    }

    public parseMandant(mandantTS: TSMandant, mandantFromServer: any): TSMandant {
        if (mandantFromServer) {
            this.parseAbstractEntity(mandantTS, mandantFromServer);
            mandantTS.name = mandantFromServer.name;
            return mandantTS;
        }
        return undefined;
    }

    public traegerschaftToRestObject(restTragerschaft: any, traegerschaft: TSTraegerschaft): any {
        if (traegerschaft) {
            this.abstractEntityToRestObject(restTragerschaft, traegerschaft);
            restTragerschaft.name = traegerschaft.name;
            restTragerschaft.active = traegerschaft.active;
            restTragerschaft.mail = traegerschaft.mail;
            return restTragerschaft;
        }
        return undefined;
    }

    public parseTraegerschaften(data: Array<any>): TSTraegerschaft[] {
        let traegerschaftenen: TSTraegerschaft[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                traegerschaftenen[i] = this.parseTraegerschaft(new TSTraegerschaft(), data[i]);
            }
        } else {
            traegerschaftenen[0] = this.parseTraegerschaft(new TSTraegerschaft(), data);
        }
        return traegerschaftenen;
    }

    public parseTraegerschaft(traegerschaftTS: TSTraegerschaft, traegerschaftFromServer: any): TSTraegerschaft {
        if (traegerschaftFromServer) {
            this.parseAbstractEntity(traegerschaftTS, traegerschaftFromServer);
            traegerschaftTS.name = traegerschaftFromServer.name;
            traegerschaftTS.active = traegerschaftFromServer.active;
            traegerschaftTS.mail = traegerschaftFromServer.mail;
            return traegerschaftTS;
        }
        return undefined;
    }

    public institutionToRestObject(restInstitution: any, institution: TSInstitution): any {
        if (institution) {
            this.abstractEntityToRestObject(restInstitution, institution);
            restInstitution.name = institution.name;
            restInstitution.mandant = this.mandantToRestObject({}, institution.mandant);
            restInstitution.traegerschaft = this.traegerschaftToRestObject({}, institution.traegerschaft);
            restInstitution.mail = institution.mail;
            return restInstitution;
        }
        return undefined;
    }

    public parseInstitution(institutionTS: TSInstitution, institutionFromServer: any): TSInstitution {
        if (institutionFromServer) {
            this.parseAbstractEntity(institutionTS, institutionFromServer);
            institutionTS.name = institutionFromServer.name;
            institutionTS.mandant = this.parseMandant(new TSMandant(), institutionFromServer.mandant);
            institutionTS.traegerschaft = this.parseTraegerschaft(new TSTraegerschaft(), institutionFromServer.traegerschaft);
            institutionTS.mail = institutionFromServer.mail;
            return institutionTS;
        }
        return undefined;
    }

    public parseInstitutionen(data: Array<any>): TSInstitution[] {
        let institutionen: TSInstitution[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                institutionen[i] = this.parseInstitution(new TSInstitution(), data[i]);
            }
        } else {
            institutionen[0] = this.parseInstitution(new TSInstitution(), data);
        }
        return institutionen;
    }

    public institutionStammdatenToRestObject(restInstitutionStammdaten: any, institutionStammdaten: TSInstitutionStammdaten): any {
        if (institutionStammdaten) {
            this.abstractDateRangeEntityToRestObject(restInstitutionStammdaten, institutionStammdaten);
            restInstitutionStammdaten.iban = institutionStammdaten.iban;
            restInstitutionStammdaten.oeffnungsstunden = institutionStammdaten.oeffnungsstunden;
            restInstitutionStammdaten.oeffnungstage = institutionStammdaten.oeffnungstage;
            restInstitutionStammdaten.betreuungsangebotTyp = institutionStammdaten.betreuungsangebotTyp;
            restInstitutionStammdaten.institution = this.institutionToRestObject({}, institutionStammdaten.institution);
            restInstitutionStammdaten.adresse = this.adresseToRestObject({}, institutionStammdaten.adresse);
            restInstitutionStammdaten.kontoinhaber = institutionStammdaten.kontoinhaber;
            restInstitutionStammdaten.adresseKontoinhaber = this.adresseToRestObject({}, institutionStammdaten.adresseKontoinhaber);
            restInstitutionStammdaten.institutionStammdatenTagesschule = this.institutionStammdatenTagesschuleToRestObject({}, institutionStammdaten.institutionStammdatenTagesschule);
            restInstitutionStammdaten.institutionStammdatenFerieninsel = this.institutionStammdatenFerieninselToRestObject({}, institutionStammdaten.institutionStammdatenFerieninsel);
            return restInstitutionStammdaten;
        }
        return undefined;
    }

    public parseInstitutionStammdaten(institutionStammdatenTS: TSInstitutionStammdaten, institutionStammdatenFromServer: any): TSInstitutionStammdaten {
        if (institutionStammdatenFromServer) {
            this.parseDateRangeEntity(institutionStammdatenTS, institutionStammdatenFromServer);
            institutionStammdatenTS.iban = institutionStammdatenFromServer.iban;
            institutionStammdatenTS.oeffnungsstunden = institutionStammdatenFromServer.oeffnungsstunden;
            institutionStammdatenTS.oeffnungstage = institutionStammdatenFromServer.oeffnungstage;
            institutionStammdatenTS.betreuungsangebotTyp = institutionStammdatenFromServer.betreuungsangebotTyp;
            institutionStammdatenTS.institution = this.parseInstitution(new TSInstitution(), institutionStammdatenFromServer.institution);
            institutionStammdatenTS.adresse = this.parseAdresse(new TSAdresse(), institutionStammdatenFromServer.adresse);
            institutionStammdatenTS.kontoinhaber = institutionStammdatenFromServer.kontoinhaber;
            institutionStammdatenTS.adresseKontoinhaber = this.parseAdresse(new TSAdresse(), institutionStammdatenFromServer.adresseKontoinhaber);
            institutionStammdatenTS.institutionStammdatenTagesschule = this.parseInstitutionStammdatenTagesschule(new TSInstitutionStammdatenTagesschule(),
                institutionStammdatenFromServer.institutionStammdatenTagesschule);
            institutionStammdatenTS.institutionStammdatenFerieninsel = this.parseInstitutionStammdatenFerieninsel(new TSInstitutionStammdatenFerieninsel(),
                institutionStammdatenFromServer.institutionStammdatenFerieninsel);
            return institutionStammdatenTS;
        }
        return undefined;
    }

    public parseInstitutionStammdatenArray(data: Array<any>): TSInstitutionStammdaten[] {
        let institutionStammdaten: TSInstitutionStammdaten[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                institutionStammdaten[i] = this.parseInstitutionStammdaten(new TSInstitutionStammdaten(), data[i]);
            }
        } else {
            institutionStammdaten[0] = this.parseInstitutionStammdaten(new TSInstitutionStammdaten(), data);
        }
        return institutionStammdaten;
    }

    public institutionStammdatenFerieninselToRestObject(restInstitutionStammdatenFerieninsel: any,
        institutionStammdatenFerieninsel: TSInstitutionStammdatenFerieninsel): any {
        if (institutionStammdatenFerieninsel) {
            this.abstractEntityToRestObject(restInstitutionStammdatenFerieninsel, institutionStammdatenFerieninsel);
            restInstitutionStammdatenFerieninsel.ausweichstandortFruehlingsferien = institutionStammdatenFerieninsel.ausweichstandortFruehlingsferien;
            restInstitutionStammdatenFerieninsel.ausweichstandortHerbstferien = institutionStammdatenFerieninsel.ausweichstandortHerbstferien;
            restInstitutionStammdatenFerieninsel.ausweichstandortSommerferien = institutionStammdatenFerieninsel.ausweichstandortSommerferien;
            restInstitutionStammdatenFerieninsel.ausweichstandortSportferien = institutionStammdatenFerieninsel.ausweichstandortSportferien;
            return restInstitutionStammdatenFerieninsel;
        }
        return undefined;
    }

    public parseInstitutionStammdatenFerieninsel(institutionStammdatenFerieninselTS: TSInstitutionStammdatenFerieninsel,
                                                 institutionStammdatenFerieninselFromServer: any): TSInstitutionStammdatenFerieninsel {
        if (institutionStammdatenFerieninselFromServer) {
            this.parseAbstractEntity(institutionStammdatenFerieninselTS, institutionStammdatenFerieninselFromServer);
            institutionStammdatenFerieninselTS.ausweichstandortFruehlingsferien = institutionStammdatenFerieninselFromServer.ausweichstandortFruehlingsferien;
            institutionStammdatenFerieninselTS.ausweichstandortHerbstferien = institutionStammdatenFerieninselFromServer.ausweichstandortHerbstferien;
            institutionStammdatenFerieninselTS.ausweichstandortSommerferien = institutionStammdatenFerieninselFromServer.ausweichstandortSommerferien;
            institutionStammdatenFerieninselTS.ausweichstandortSportferien = institutionStammdatenFerieninselFromServer.ausweichstandortSportferien;
            return institutionStammdatenFerieninselTS;
        }
        return undefined;
    }

    public institutionStammdatenTagesschuleToRestObject(restInstitutionStammdatenTagesschule: any,
        institutionStammdatenTagesschule: TSInstitutionStammdatenTagesschule): any {
        if (institutionStammdatenTagesschule) {
            this.abstractEntityToRestObject(restInstitutionStammdatenTagesschule, institutionStammdatenTagesschule);
            restInstitutionStammdatenTagesschule.moduleTagesschule = this.moduleTagesschuleArrayToRestObject(institutionStammdatenTagesschule.moduleTagesschule);
            return restInstitutionStammdatenTagesschule;
        }
        return undefined;
    }

    public parseInstitutionStammdatenTagesschule(institutionStammdatenTagesschuleTS: TSInstitutionStammdatenTagesschule,
        institutionStammdatenTagesschuleFromServer: any): TSInstitutionStammdatenTagesschule {
        if (institutionStammdatenTagesschuleFromServer) {
            this.parseAbstractEntity(institutionStammdatenTagesschuleTS, institutionStammdatenTagesschuleFromServer);
            institutionStammdatenTagesschuleTS.moduleTagesschule = this.parseModuleTagesschuleArray(institutionStammdatenTagesschuleFromServer.moduleTagesschule);
            return institutionStammdatenTagesschuleTS;
        }
        return undefined;
    }

    public finanzielleSituationContainerToRestObject(restFinanzielleSituationContainer: any,
        finanzielleSituationContainer: TSFinanzielleSituationContainer): TSFinanzielleSituationContainer {
        this.abstractEntityToRestObject(restFinanzielleSituationContainer, finanzielleSituationContainer);
        restFinanzielleSituationContainer.jahr = finanzielleSituationContainer.jahr;
        if (finanzielleSituationContainer.finanzielleSituationGS) {
            restFinanzielleSituationContainer.finanzielleSituationGS = this.finanzielleSituationToRestObject({}, finanzielleSituationContainer.finanzielleSituationGS);
        }
        if (finanzielleSituationContainer.finanzielleSituationJA) {
            restFinanzielleSituationContainer.finanzielleSituationJA = this.finanzielleSituationToRestObject({}, finanzielleSituationContainer.finanzielleSituationJA);
        }
        return restFinanzielleSituationContainer;
    }

    public parseFinanzielleSituationContainer(containerTS: TSFinanzielleSituationContainer, containerFromServer: any): TSFinanzielleSituationContainer {
        if (containerFromServer) {
            this.parseAbstractEntity(containerTS, containerFromServer);
            containerTS.jahr = containerFromServer.jahr;
            containerTS.finanzielleSituationGS = this.parseFinanzielleSituation(containerTS.finanzielleSituationGS || new TSFinanzielleSituation(), containerFromServer.finanzielleSituationGS);
            containerTS.finanzielleSituationJA = this.parseFinanzielleSituation(containerTS.finanzielleSituationJA || new TSFinanzielleSituation(), containerFromServer.finanzielleSituationJA);
            return containerTS;
        }
        return undefined;
    }

    public finanzielleSituationToRestObject(restFinanzielleSituation: any, finanzielleSituation: TSFinanzielleSituation): TSFinanzielleSituation {
        this.abstractfinanzielleSituationToRestObject(restFinanzielleSituation, finanzielleSituation);
        restFinanzielleSituation.nettolohn = finanzielleSituation.nettolohn;
        restFinanzielleSituation.geschaeftsgewinnBasisjahrMinus2 = finanzielleSituation.geschaeftsgewinnBasisjahrMinus2;
        restFinanzielleSituation.geschaeftsgewinnBasisjahrMinus1 = finanzielleSituation.geschaeftsgewinnBasisjahrMinus1;
        return restFinanzielleSituation;
    }

    private abstractfinanzielleSituationToRestObject(restAbstractFinanzielleSituation: any,
        abstractFinanzielleSituation: TSAbstractFinanzielleSituation): TSAbstractFinanzielleSituation {
        this.abstractEntityToRestObject(restAbstractFinanzielleSituation, abstractFinanzielleSituation);
        restAbstractFinanzielleSituation.steuerveranlagungErhalten = abstractFinanzielleSituation.steuerveranlagungErhalten;
        restAbstractFinanzielleSituation.steuererklaerungAusgefuellt = abstractFinanzielleSituation.steuererklaerungAusgefuellt || false;
        restAbstractFinanzielleSituation.familienzulage = abstractFinanzielleSituation.familienzulage;
        restAbstractFinanzielleSituation.ersatzeinkommen = abstractFinanzielleSituation.ersatzeinkommen;
        restAbstractFinanzielleSituation.erhalteneAlimente = abstractFinanzielleSituation.erhalteneAlimente;
        restAbstractFinanzielleSituation.bruttovermoegen = abstractFinanzielleSituation.bruttovermoegen;
        restAbstractFinanzielleSituation.schulden = abstractFinanzielleSituation.schulden;
        restAbstractFinanzielleSituation.geschaeftsgewinnBasisjahr = abstractFinanzielleSituation.geschaeftsgewinnBasisjahr;
        restAbstractFinanzielleSituation.geleisteteAlimente = abstractFinanzielleSituation.geleisteteAlimente;
        return restAbstractFinanzielleSituation;
    }

    public parseAbstractFinanzielleSituation(abstractFinanzielleSituationTS: TSAbstractFinanzielleSituation,
        abstractFinanzielleSituationFromServer: any): TSAbstractFinanzielleSituation {
        if (abstractFinanzielleSituationFromServer) {
            this.parseAbstractEntity(abstractFinanzielleSituationTS, abstractFinanzielleSituationFromServer);
            abstractFinanzielleSituationTS.steuerveranlagungErhalten = abstractFinanzielleSituationFromServer.steuerveranlagungErhalten;
            abstractFinanzielleSituationTS.steuererklaerungAusgefuellt = abstractFinanzielleSituationFromServer.steuererklaerungAusgefuellt;
            abstractFinanzielleSituationTS.familienzulage = abstractFinanzielleSituationFromServer.familienzulage;
            abstractFinanzielleSituationTS.ersatzeinkommen = abstractFinanzielleSituationFromServer.ersatzeinkommen;
            abstractFinanzielleSituationTS.erhalteneAlimente = abstractFinanzielleSituationFromServer.erhalteneAlimente;
            abstractFinanzielleSituationTS.bruttovermoegen = abstractFinanzielleSituationFromServer.bruttovermoegen;
            abstractFinanzielleSituationTS.schulden = abstractFinanzielleSituationFromServer.schulden;
            abstractFinanzielleSituationTS.geschaeftsgewinnBasisjahr = abstractFinanzielleSituationFromServer.geschaeftsgewinnBasisjahr;
            abstractFinanzielleSituationTS.geleisteteAlimente = abstractFinanzielleSituationFromServer.geleisteteAlimente;
            return abstractFinanzielleSituationTS;
        }
        return undefined;
    }

    public parseFinanzielleSituation(finanzielleSituationTS: TSFinanzielleSituation, finanzielleSituationFromServer: any): TSFinanzielleSituation {
        if (finanzielleSituationFromServer) {
            this.parseAbstractFinanzielleSituation(finanzielleSituationTS, finanzielleSituationFromServer);
            finanzielleSituationTS.nettolohn = finanzielleSituationFromServer.nettolohn;
            finanzielleSituationTS.geschaeftsgewinnBasisjahrMinus2 = finanzielleSituationFromServer.geschaeftsgewinnBasisjahrMinus2;
            finanzielleSituationTS.geschaeftsgewinnBasisjahrMinus1 = finanzielleSituationFromServer.geschaeftsgewinnBasisjahrMinus1;
            return finanzielleSituationTS;
        }
        return undefined;
    }

    public finanzielleSituationResultateToRestObject(restFinanzielleSituationResultate: any,
        finanzielleSituationResultateDTO: TSFinanzielleSituationResultateDTO): TSFinanzielleSituationResultateDTO {
        restFinanzielleSituationResultate.geschaeftsgewinnDurchschnittGesuchsteller1 = finanzielleSituationResultateDTO.geschaeftsgewinnDurchschnittGesuchsteller1;
        restFinanzielleSituationResultate.geschaeftsgewinnDurchschnittGesuchsteller2 = finanzielleSituationResultateDTO.geschaeftsgewinnDurchschnittGesuchsteller2;
        restFinanzielleSituationResultate.einkommenBeiderGesuchsteller = finanzielleSituationResultateDTO.einkommenBeiderGesuchsteller;
        restFinanzielleSituationResultate.nettovermoegenFuenfProzent = finanzielleSituationResultateDTO.nettovermoegenFuenfProzent;
        restFinanzielleSituationResultate.anrechenbaresEinkommen = finanzielleSituationResultateDTO.anrechenbaresEinkommen;
        restFinanzielleSituationResultate.abzuegeBeiderGesuchsteller = finanzielleSituationResultateDTO.abzuegeBeiderGesuchsteller;
        restFinanzielleSituationResultate.massgebendesEinkVorAbzFamGr = finanzielleSituationResultateDTO.massgebendesEinkVorAbzFamGr;
        return restFinanzielleSituationResultate;
    }

    public parseFinanzielleSituationResultate(finanzielleSituationResultateDTO: TSFinanzielleSituationResultateDTO,
        finanzielleSituationResultateFromServer: any): TSFinanzielleSituationResultateDTO {
        if (finanzielleSituationResultateFromServer) {
            finanzielleSituationResultateDTO.geschaeftsgewinnDurchschnittGesuchsteller1 = finanzielleSituationResultateFromServer.geschaeftsgewinnDurchschnittGesuchsteller1;
            finanzielleSituationResultateDTO.geschaeftsgewinnDurchschnittGesuchsteller2 = finanzielleSituationResultateFromServer.geschaeftsgewinnDurchschnittGesuchsteller2;
            finanzielleSituationResultateDTO.einkommenBeiderGesuchsteller = finanzielleSituationResultateFromServer.einkommenBeiderGesuchsteller;
            finanzielleSituationResultateDTO.nettovermoegenFuenfProzent = finanzielleSituationResultateFromServer.nettovermoegenFuenfProzent;
            finanzielleSituationResultateDTO.anrechenbaresEinkommen = finanzielleSituationResultateFromServer.anrechenbaresEinkommen;
            finanzielleSituationResultateDTO.abzuegeBeiderGesuchsteller = finanzielleSituationResultateFromServer.abzuegeBeiderGesuchsteller;
            finanzielleSituationResultateDTO.massgebendesEinkVorAbzFamGr = finanzielleSituationResultateFromServer.massgebendesEinkVorAbzFamGr;
            return finanzielleSituationResultateDTO;
        }
        return undefined;
    }

    public einkommensverschlechterungContainerToRestObject(restEinkommensverschlechterungContainer: any,
        einkommensverschlechterungContainer: TSEinkommensverschlechterungContainer): TSEinkommensverschlechterungContainer {
        this.abstractEntityToRestObject(restEinkommensverschlechterungContainer, einkommensverschlechterungContainer);

        if (einkommensverschlechterungContainer.ekvGSBasisJahrPlus1) {
            restEinkommensverschlechterungContainer.ekvGSBasisJahrPlus1 =
                this.einkommensverschlechterungToRestObject({}, einkommensverschlechterungContainer.ekvGSBasisJahrPlus1);
        }
        if (einkommensverschlechterungContainer.ekvGSBasisJahrPlus2) {
            restEinkommensverschlechterungContainer.ekvGSBasisJahrPlus2 =
                this.einkommensverschlechterungToRestObject({}, einkommensverschlechterungContainer.ekvGSBasisJahrPlus2);
        }
        if (einkommensverschlechterungContainer.ekvJABasisJahrPlus1) {
            restEinkommensverschlechterungContainer.ekvJABasisJahrPlus1 =
                this.einkommensverschlechterungToRestObject({}, einkommensverschlechterungContainer.ekvJABasisJahrPlus1);
        }
        if (einkommensverschlechterungContainer.ekvJABasisJahrPlus2) {
            restEinkommensverschlechterungContainer.ekvJABasisJahrPlus2 =
                this.einkommensverschlechterungToRestObject({}, einkommensverschlechterungContainer.ekvJABasisJahrPlus2);
        }

        return restEinkommensverschlechterungContainer;
    }

    public einkommensverschlechterungToRestObject(restEinkommensverschlechterung: any,
        einkommensverschlechterung: TSEinkommensverschlechterung): TSEinkommensverschlechterung {
        this.abstractfinanzielleSituationToRestObject(restEinkommensverschlechterung, einkommensverschlechterung);
        restEinkommensverschlechterung.nettolohnJan = einkommensverschlechterung.nettolohnJan;
        restEinkommensverschlechterung.nettolohnFeb = einkommensverschlechterung.nettolohnFeb;
        restEinkommensverschlechterung.nettolohnMrz = einkommensverschlechterung.nettolohnMrz;
        restEinkommensverschlechterung.nettolohnApr = einkommensverschlechterung.nettolohnApr;
        restEinkommensverschlechterung.nettolohnMai = einkommensverschlechterung.nettolohnMai;
        restEinkommensverschlechterung.nettolohnJun = einkommensverschlechterung.nettolohnJun;
        restEinkommensverschlechterung.nettolohnJul = einkommensverschlechterung.nettolohnJul;
        restEinkommensverschlechterung.nettolohnAug = einkommensverschlechterung.nettolohnAug;
        restEinkommensverschlechterung.nettolohnSep = einkommensverschlechterung.nettolohnSep;
        restEinkommensverschlechterung.nettolohnOkt = einkommensverschlechterung.nettolohnOkt;
        restEinkommensverschlechterung.nettolohnNov = einkommensverschlechterung.nettolohnNov;
        restEinkommensverschlechterung.nettolohnDez = einkommensverschlechterung.nettolohnDez;
        restEinkommensverschlechterung.nettolohnZus = einkommensverschlechterung.nettolohnZus;
        restEinkommensverschlechterung.geschaeftsgewinnBasisjahrMinus1 = einkommensverschlechterung.geschaeftsgewinnBasisjahrMinus1;
        return restEinkommensverschlechterung;
    }

    public parseEinkommensverschlechterungContainer(containerTS: TSEinkommensverschlechterungContainer,
        containerFromServer: any): TSEinkommensverschlechterungContainer {
        if (containerFromServer) {
            this.parseAbstractEntity(containerTS, containerFromServer);

            containerTS.ekvGSBasisJahrPlus1 = this.parseEinkommensverschlechterung(containerTS.ekvGSBasisJahrPlus1 || new TSEinkommensverschlechterung(), containerFromServer.ekvGSBasisJahrPlus1);
            containerTS.ekvGSBasisJahrPlus2 = this.parseEinkommensverschlechterung(containerTS.ekvGSBasisJahrPlus2 || new TSEinkommensverschlechterung(), containerFromServer.ekvGSBasisJahrPlus2);
            containerTS.ekvJABasisJahrPlus1 = this.parseEinkommensverschlechterung(containerTS.ekvJABasisJahrPlus1 || new TSEinkommensverschlechterung(), containerFromServer.ekvJABasisJahrPlus1);
            containerTS.ekvJABasisJahrPlus2 = this.parseEinkommensverschlechterung(containerTS.ekvJABasisJahrPlus2 || new TSEinkommensverschlechterung(), containerFromServer.ekvJABasisJahrPlus2);

            return containerTS;
        }
        return undefined;
    }

    public parseEinkommensverschlechterung(einkommensverschlechterungTS: TSEinkommensverschlechterung,
        einkommensverschlechterungFromServer: any): TSEinkommensverschlechterung {
        if (einkommensverschlechterungFromServer) {
            this.parseAbstractFinanzielleSituation(einkommensverschlechterungTS, einkommensverschlechterungFromServer);
            einkommensverschlechterungTS.nettolohnJan = einkommensverschlechterungFromServer.nettolohnJan;
            einkommensverschlechterungTS.nettolohnFeb = einkommensverschlechterungFromServer.nettolohnFeb;
            einkommensverschlechterungTS.nettolohnMrz = einkommensverschlechterungFromServer.nettolohnMrz;
            einkommensverschlechterungTS.nettolohnApr = einkommensverschlechterungFromServer.nettolohnApr;
            einkommensverschlechterungTS.nettolohnMai = einkommensverschlechterungFromServer.nettolohnMai;
            einkommensverschlechterungTS.nettolohnJun = einkommensverschlechterungFromServer.nettolohnJun;
            einkommensverschlechterungTS.nettolohnJul = einkommensverschlechterungFromServer.nettolohnJul;
            einkommensverschlechterungTS.nettolohnAug = einkommensverschlechterungFromServer.nettolohnAug;
            einkommensverschlechterungTS.nettolohnSep = einkommensverschlechterungFromServer.nettolohnSep;
            einkommensverschlechterungTS.nettolohnOkt = einkommensverschlechterungFromServer.nettolohnOkt;
            einkommensverschlechterungTS.nettolohnNov = einkommensverschlechterungFromServer.nettolohnNov;
            einkommensverschlechterungTS.nettolohnDez = einkommensverschlechterungFromServer.nettolohnDez;
            einkommensverschlechterungTS.nettolohnZus = einkommensverschlechterungFromServer.nettolohnZus;
            einkommensverschlechterungTS.geschaeftsgewinnBasisjahrMinus1 = einkommensverschlechterungFromServer.geschaeftsgewinnBasisjahrMinus1;

            return einkommensverschlechterungTS;
        }
        return undefined;
    }

    public kindContainerToRestObject(restKindContainer: any, kindContainer: TSKindContainer): any {
        this.abstractEntityToRestObject(restKindContainer, kindContainer);
        if (kindContainer.kindGS) {
            restKindContainer.kindGS = this.kindToRestObject({}, kindContainer.kindGS);
        }
        if (kindContainer.kindJA) {
            restKindContainer.kindJA = this.kindToRestObject({}, kindContainer.kindJA);
        }
        restKindContainer.betreuungen = this.betreuungListToRestObject(kindContainer.betreuungen);
        restKindContainer.kindNummer = kindContainer.kindNummer;
        restKindContainer.nextNumberBetreuung = kindContainer.nextNumberBetreuung;
        restKindContainer.kindMutiert = kindContainer.kindMutiert;
        return restKindContainer;
    }

    private kindToRestObject(restKind: any, kind: TSKind): any {
        this.abstractPersonEntitytoRestObject(restKind, kind);
        restKind.wohnhaftImGleichenHaushalt = kind.wohnhaftImGleichenHaushalt;
        restKind.kinderabzug = kind.kinderabzug;
        restKind.mutterspracheDeutsch = kind.mutterspracheDeutsch;
        restKind.einschulung = kind.einschulung;
        restKind.familienErgaenzendeBetreuung = kind.familienErgaenzendeBetreuung;
        if (kind.pensumFachstelle) {
            restKind.pensumFachstelle = this.pensumFachstelleToRestObject({}, kind.pensumFachstelle);
        }
        return restKind;
    }

    public parseKindDubletteList(data: Array<any>): TSKindDublette[] {
        let kindContainerList: TSKindDublette[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                kindContainerList[i] = this.parseKindDublette(new TSKindDublette(), data[i]);
            }
        } else {
            kindContainerList[0] = this.parseKindDublette(new TSKindDublette(), data);
        }
        return kindContainerList;
    }

    public parseKindDublette(kindContainerTS: TSKindDublette, kindContainerFromServer: any): TSKindDublette {
        if (kindContainerFromServer) {
            kindContainerTS.gesuchId = kindContainerFromServer.gesuchId;
            kindContainerTS.fallNummer = kindContainerFromServer.fallNummer;
            kindContainerTS.kindNummerOriginal = kindContainerFromServer.kindNummerOriginal;
            kindContainerTS.kindNummerDublette = kindContainerFromServer.kindNummerDublette;
            return kindContainerTS;
        }
        return undefined;
    }

    public parseKindContainerList(data: Array<any>): TSKindContainer[] {
        let kindContainerList: TSKindContainer[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                kindContainerList[i] = this.parseKindContainer(new TSKindContainer(), data[i]);
            }
        } else {
            kindContainerList[0] = this.parseKindContainer(new TSKindContainer(), data);
        }
        return kindContainerList;
    }

    public parseKindContainer(kindContainerTS: TSKindContainer, kindContainerFromServer: any): TSKindContainer {
        if (kindContainerFromServer) {
            this.parseAbstractEntity(kindContainerTS, kindContainerFromServer);
            kindContainerTS.kindGS = this.parseKind(new TSKind(), kindContainerFromServer.kindGS);
            kindContainerTS.kindJA = this.parseKind(new TSKind(), kindContainerFromServer.kindJA);
            kindContainerTS.betreuungen = this.parseBetreuungList(kindContainerFromServer.betreuungen);
            kindContainerTS.kindNummer = kindContainerFromServer.kindNummer;
            kindContainerTS.nextNumberBetreuung = kindContainerFromServer.nextNumberBetreuung;
            kindContainerTS.kindMutiert = kindContainerFromServer.kindMutiert;
            return kindContainerTS;
        }
        return undefined;
    }

    private parseKind(kindTS: TSKind, kindFromServer: any): TSKind {
        if (kindFromServer) {
            this.parseAbstractPersonEntity(kindTS, kindFromServer);
            kindTS.wohnhaftImGleichenHaushalt = kindFromServer.wohnhaftImGleichenHaushalt;
            kindTS.kinderabzug = kindFromServer.kinderabzug;
            kindTS.mutterspracheDeutsch = kindFromServer.mutterspracheDeutsch;
            kindTS.einschulung = kindFromServer.einschulung;
            kindTS.familienErgaenzendeBetreuung = kindFromServer.familienErgaenzendeBetreuung;
            if (kindFromServer.pensumFachstelle) {
                kindTS.pensumFachstelle = this.parsePensumFachstelle(new TSPensumFachstelle(), kindFromServer.pensumFachstelle);
            }
            return kindTS;
        }
        return undefined;
    }

    private pensumFachstelleToRestObject(restPensumFachstelle: any, pensumFachstelle: TSPensumFachstelle): any {
        this.abstractDateRangeEntityToRestObject(restPensumFachstelle, pensumFachstelle);
        restPensumFachstelle.pensum = pensumFachstelle.pensum;
        if (pensumFachstelle.fachstelle) {
            restPensumFachstelle.fachstelle = this.fachstelleToRestObject({}, pensumFachstelle.fachstelle);
        }
        return restPensumFachstelle;
    }

    private parsePensumFachstelle(pensumFachstelleTS: TSPensumFachstelle, pensumFachstelleFromServer: any): TSPensumFachstelle {
        if (pensumFachstelleFromServer) {
            this.parseDateRangeEntity(pensumFachstelleTS, pensumFachstelleFromServer);
            pensumFachstelleTS.pensum = pensumFachstelleFromServer.pensum;
            if (pensumFachstelleFromServer.fachstelle) {
                pensumFachstelleTS.fachstelle = this.parseFachstelle(new TSFachstelle(), pensumFachstelleFromServer.fachstelle);
            }
            return pensumFachstelleTS;
        }
        return undefined;
    }

    private betreuungListToRestObject(betreuungen: Array<TSBetreuung>): Array<any> {
        let list: any[] = [];
        if (betreuungen) {
            for (let i = 0; i < betreuungen.length; i++) {
                list[i] = this.betreuungToRestObject({}, betreuungen[i]);
            }
        }
        return list;
    }

    public betreuungToRestObject(restBetreuung: any, betreuung: TSBetreuung): any {
        this.abstractEntityToRestObject(restBetreuung, betreuung);
        restBetreuung.betreuungsstatus = betreuung.betreuungsstatus;
        restBetreuung.grundAblehnung = betreuung.grundAblehnung;
        restBetreuung.datumAblehnung = DateUtil.momentToLocalDate(betreuung.datumAblehnung);
        restBetreuung.datumBestaetigung = DateUtil.momentToLocalDate(betreuung.datumBestaetigung);
        restBetreuung.vertrag = betreuung.vertrag;
        restBetreuung.erweiterteBeduerfnisse = betreuung.erweiterteBeduerfnisse;
        if (betreuung.institutionStammdaten) {
            restBetreuung.institutionStammdaten = this.institutionStammdatenToRestObject({}, betreuung.institutionStammdaten);
        }
        if (betreuung.betreuungspensumContainers) {
            restBetreuung.betreuungspensumContainers = [];
            betreuung.betreuungspensumContainers.forEach((betPensCont: TSBetreuungspensumContainer) => {
                restBetreuung.betreuungspensumContainers.push(this.betreuungspensumContainerToRestObject({}, betPensCont));
            });
        }
        if (betreuung.abwesenheitContainers) {
            restBetreuung.abwesenheitContainers = [];
            betreuung.abwesenheitContainers.forEach((abwesenheitCont: TSAbwesenheitContainer) => {
                restBetreuung.abwesenheitContainers.push(this.abwesenheitContainerToRestObject({}, abwesenheitCont));
            });
        }
        restBetreuung.kindFullname = betreuung.kindFullname;
        restBetreuung.kindNummer = betreuung.kindNummer;
        restBetreuung.gesuchId = betreuung.gesuchId;
        restBetreuung.gesuchsperiode = this.gesuchsperiodeToRestObject({}, betreuung.gesuchsperiode);
        restBetreuung.betreuungNummer = betreuung.betreuungNummer;
        restBetreuung.betreuungMutiert = betreuung.betreuungMutiert;
        restBetreuung.abwesenheitMutiert = betreuung.abwesenheitMutiert;
        restBetreuung.gueltig = betreuung.gueltig;
        restBetreuung.belegungTagesschule = this.belegungTagesschuleToRestObject({}, betreuung.belegungTagesschule);
        restBetreuung.belegungFerieninsel = this.belegungFerieninselToRestObject({}, betreuung.belegungFerieninsel);
        restBetreuung.anmeldungMutationZustand = betreuung.anmeldungMutationZustand;
        restBetreuung.keineDetailinformationen = betreuung.keineDetailinformationen;
        return restBetreuung;
    }

    public anmeldungDTOToRestObject(restAngebot: any, angebotDTO: TSAnmeldungDTO): any {
        restAngebot.betreuung = this.betreuungToRestObject({}, angebotDTO.betreuung);
        restAngebot.additionalKindQuestions = angebotDTO.additionalKindQuestions;
        restAngebot.einschulung = angebotDTO.einschulung;
        restAngebot.kindContainerId = angebotDTO.kindContainerId;
        restAngebot.mutterspracheDeutsch = angebotDTO.mutterspracheDeutsch;
        restAngebot.wohnhaftImGleichenHaushalt = angebotDTO.wohnhaftImGleichenHaushalt;
        return restAngebot;

    }

    public betreuungspensumContainerToRestObject(restBetPensCont: any, betPensCont: TSBetreuungspensumContainer): any {
        this.abstractEntityToRestObject(restBetPensCont, betPensCont);
        if (betPensCont.betreuungspensumGS) {
            restBetPensCont.betreuungspensumGS = this.betreuungspensumToRestObject({}, betPensCont.betreuungspensumGS);
        }
        if (betPensCont.betreuungspensumJA) {
            restBetPensCont.betreuungspensumJA = this.betreuungspensumToRestObject({}, betPensCont.betreuungspensumJA);
        }
        return restBetPensCont;
    }

    public abwesenheitContainerToRestObject(restAbwesenheitCont: any, abwesenheitCont: TSAbwesenheitContainer): any {
        this.abstractEntityToRestObject(restAbwesenheitCont, abwesenheitCont);
        if (abwesenheitCont.abwesenheitGS) {
            restAbwesenheitCont.abwesenheitGS = this.abwesenheitToRestObject({}, abwesenheitCont.abwesenheitGS);
        }
        if (abwesenheitCont.abwesenheitJA) {
            restAbwesenheitCont.abwesenheitJA = this.abwesenheitToRestObject({}, abwesenheitCont.abwesenheitJA);
        }
        return restAbwesenheitCont;
    }

    public betreuungspensumToRestObject(restBetreuungspensum: any, betreuungspensum: TSBetreuungspensum): any {
        this.abstractPensumEntityToRestObject(restBetreuungspensum, betreuungspensum);
        if (betreuungspensum.nichtEingetreten !== null) { // wenn es null ist, wird es als null zum Server geschickt und der Server versucht, es zu validieren und wirft eine NPE
            restBetreuungspensum.nichtEingetreten = betreuungspensum.nichtEingetreten;
        }
        return restBetreuungspensum;
    }

    public betreuungsmitteilungPensumToRestObject(restBetreuungspensum: any, betreuungspensum: TSBetreuungsmitteilungPensum): any {
        this.abstractPensumEntityToRestObject(restBetreuungspensum, betreuungspensum);
        return restBetreuungspensum;
    }

    public abwesenheitToRestObject(restAbwesenheit: any, abwesenheit: TSAbwesenheit): any {
        this.abstractDateRangeEntityToRestObject(restAbwesenheit, abwesenheit);
        return restAbwesenheit;
    }

    public parseBetreuungList(betreuungen: Array<any>): TSBetreuung[] {
        let resultList: TSBetreuung[] = [];
        if (betreuungen && Array.isArray(betreuungen)) {
            for (let i = 0; i < betreuungen.length; i++) {
                resultList[i] = this.parseBetreuung(new TSBetreuung(), betreuungen[i]);
            }
        } else {
            resultList[0] = this.parseBetreuung(new TSBetreuung(), betreuungen);
        }
        return resultList;
    }

    public parseBetreuung(betreuungTS: TSBetreuung, betreuungFromServer: any): TSBetreuung {
        if (betreuungFromServer) {
            this.parseAbstractEntity(betreuungTS, betreuungFromServer);
            betreuungTS.grundAblehnung = betreuungFromServer.grundAblehnung;
            betreuungTS.datumAblehnung = DateUtil.localDateToMoment(betreuungFromServer.datumAblehnung);
            betreuungTS.datumBestaetigung = DateUtil.localDateToMoment(betreuungFromServer.datumBestaetigung);
            betreuungTS.vertrag = betreuungFromServer.vertrag;
            betreuungTS.erweiterteBeduerfnisse = betreuungFromServer.erweiterteBeduerfnisse;
            betreuungTS.betreuungsstatus = betreuungFromServer.betreuungsstatus;
            betreuungTS.institutionStammdaten = this.parseInstitutionStammdaten(new TSInstitutionStammdaten(), betreuungFromServer.institutionStammdaten);
            betreuungTS.betreuungspensumContainers = this.parseBetreuungspensumContainers(betreuungFromServer.betreuungspensumContainers);
            betreuungTS.abwesenheitContainers = this.parseAbwesenheitContainers(betreuungFromServer.abwesenheitContainers);
            betreuungTS.betreuungNummer = betreuungFromServer.betreuungNummer;
            betreuungTS.verfuegung = this.parseVerfuegung(new TSVerfuegung(), betreuungFromServer.verfuegung);
            betreuungTS.kindFullname = betreuungFromServer.kindFullname;
            betreuungTS.kindNummer = betreuungFromServer.kindNummer;
            betreuungTS.gesuchId = betreuungFromServer.gesuchId;
            betreuungTS.gesuchsperiode = this.parseGesuchsperiode(new TSGesuchsperiode(), betreuungFromServer.gesuchsperiode);
            betreuungTS.betreuungMutiert = betreuungFromServer.betreuungMutiert;
            betreuungTS.abwesenheitMutiert = betreuungFromServer.abwesenheitMutiert;
            betreuungTS.gueltig = betreuungFromServer.gueltig;
            betreuungTS.belegungTagesschule = this.parseBelegungTagesschule(new TSBelegungTagesschule(), betreuungFromServer.belegungTagesschule);
            betreuungTS.belegungFerieninsel = this.parseBelegungFerieninsel(new TSBelegungFerieninsel(), betreuungFromServer.belegungFerieninsel);
            betreuungTS.anmeldungMutationZustand = betreuungFromServer.anmeldungMutationZustand;
            betreuungTS.keineDetailinformationen = betreuungFromServer.keineDetailinformationen;
            betreuungTS.bgNummer = betreuungFromServer.bgNummer;
            return betreuungTS;
        }
        return undefined;
    }

    public parseBetreuungspensumContainers(data: Array<any>): TSBetreuungspensumContainer[] {
        let betPensContainers: TSBetreuungspensumContainer[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                betPensContainers[i] = this.parseBetreuungspensumContainer(new TSBetreuungspensumContainer(), data[i]);
            }
        } else {
            betPensContainers[0] = this.parseBetreuungspensumContainer(new TSBetreuungspensumContainer(), data);
        }
        return betPensContainers;
    }
    public parseAbwesenheitContainers(data: Array<any>): TSAbwesenheitContainer[] {
        let abwesenheitContainers: TSAbwesenheitContainer[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                abwesenheitContainers[i] = this.parseAbwesenheitContainer(new TSAbwesenheitContainer(), data[i]);
            }
        } else if (data) {
            abwesenheitContainers[0] = this.parseAbwesenheitContainer(new TSAbwesenheitContainer(), data);
        }
        return abwesenheitContainers;
    }

    public parseBetreuungspensumContainer(betPensContainerTS: TSBetreuungspensumContainer, betPensContFromServer: any): TSBetreuungspensumContainer {
        if (betPensContFromServer) {
            this.parseAbstractEntity(betPensContainerTS, betPensContFromServer);
            if (betPensContFromServer.betreuungspensumGS) {
                betPensContainerTS.betreuungspensumGS = this.parseBetreuungspensum(new TSBetreuungspensum(), betPensContFromServer.betreuungspensumGS);
            }
            if (betPensContFromServer.betreuungspensumJA) {
                betPensContainerTS.betreuungspensumJA = this.parseBetreuungspensum(new TSBetreuungspensum(), betPensContFromServer.betreuungspensumJA);
            }
            return betPensContainerTS;
        }
        return undefined;
    }

    public parseAbwesenheitContainer(abwesenheitContainerTS: TSAbwesenheitContainer, abwesenheitContFromServer: any): TSAbwesenheitContainer {
        if (abwesenheitContFromServer) {
            this.parseAbstractEntity(abwesenheitContainerTS, abwesenheitContFromServer);
            if (abwesenheitContFromServer.abwesenheitGS) {
                abwesenheitContainerTS.abwesenheitGS = this.parseAbwesenheit(new TSAbwesenheit(), abwesenheitContFromServer.abwesenheitGS);
            }
            if (abwesenheitContFromServer.abwesenheitJA) {
                abwesenheitContainerTS.abwesenheitJA = this.parseAbwesenheit(new TSAbwesenheit(), abwesenheitContFromServer.abwesenheitJA);
            }
            return abwesenheitContainerTS;
        }
        return undefined;
    }

    public parseBetreuungspensum(betreuungspensumTS: TSBetreuungspensum, betreuungspensumFromServer: any): TSBetreuungspensum {
        if (betreuungspensumFromServer) {
            this.parseAbstractPensumEntity(betreuungspensumTS, betreuungspensumFromServer);
            betreuungspensumTS.nichtEingetreten = betreuungspensumFromServer.nichtEingetreten;
            return betreuungspensumTS;
        }
        return undefined;
    }

    public parseBetreuungsmitteilungPensum(betreuungspensumTS: TSBetreuungsmitteilungPensum, betreuungspensumFromServer: any): TSBetreuungsmitteilungPensum {
        if (betreuungspensumFromServer) {
            this.parseAbstractPensumEntity(betreuungspensumTS, betreuungspensumFromServer);
            return betreuungspensumTS;
        }
        return undefined;
    }

    public parseAbwesenheit(abwesenheitTS: TSAbwesenheit, abwesenheitFromServer: any): TSAbwesenheit {
        if (abwesenheitFromServer) {
            this.parseDateRangeEntity(abwesenheitTS, abwesenheitFromServer);
            return abwesenheitTS;
        }
        return undefined;
    }

    private parseErwerbspensenContainers(data: Array<any>): TSErwerbspensumContainer[] {
        let erwerbspensen: TSErwerbspensumContainer[] = [];
        if (data !== null && data !== undefined) {
            if (Array.isArray(data)) {
                for (let i = 0; i < data.length; i++) {
                    erwerbspensen[i] = this.parseErwerbspensumContainer(new TSErwerbspensumContainer(), data[i]);
                }
            } else {
                erwerbspensen[0] = this.parseErwerbspensumContainer(new TSErwerbspensumContainer(), data);
            }
        }
        return erwerbspensen;
    }

    public gesuchsperiodeToRestObject(restGesuchsperiode: any, gesuchsperiode: TSGesuchsperiode): any {
        if (gesuchsperiode) {
            this.abstractDateRangeEntityToRestObject(restGesuchsperiode, gesuchsperiode);
            restGesuchsperiode.status = gesuchsperiode.status;
            restGesuchsperiode.datumFreischaltungTagesschule = DateUtil.momentToLocalDate(gesuchsperiode.datumFreischaltungTagesschule);
            restGesuchsperiode.datumErsterSchultag = DateUtil.momentToLocalDate(gesuchsperiode.datumErsterSchultag);
            return restGesuchsperiode;
        }
        return undefined;
    }

    public parseGesuchsperiode(gesuchsperiodeTS: TSGesuchsperiode, gesuchsperiodeFromServer: any): TSGesuchsperiode {
        if (gesuchsperiodeFromServer) {
            this.parseDateRangeEntity(gesuchsperiodeTS, gesuchsperiodeFromServer);
            gesuchsperiodeTS.status = gesuchsperiodeFromServer.status;
            gesuchsperiodeTS.datumFreischaltungTagesschule = DateUtil.localDateToMoment(gesuchsperiodeFromServer.datumFreischaltungTagesschule);
            gesuchsperiodeTS.datumErsterSchultag = DateUtil.localDateToMoment(gesuchsperiodeFromServer.datumErsterSchultag);
            return gesuchsperiodeTS;
        }
        return undefined;
    }

    public parseGesuchsperioden(data: any): TSGesuchsperiode[] {
        let gesuchsperioden: TSGesuchsperiode[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                gesuchsperioden[i] = this.parseGesuchsperiode(new TSGesuchsperiode(), data[i]);
            }
        } else {
            gesuchsperioden[0] = this.parseGesuchsperiode(new TSGesuchsperiode(), data);
        }
        return gesuchsperioden;
    }

    public antragDTOToRestObject(restPendenz: any, pendenz: TSAntragDTO): any {
        restPendenz.antragId = pendenz.antragId;
        restPendenz.fallNummer = pendenz.fallNummer;
        restPendenz.familienName = pendenz.familienName;
        restPendenz.angebote = pendenz.angebote;
        restPendenz.antragTyp = pendenz.antragTyp;
        restPendenz.eingangsdatum = DateUtil.momentToLocalDate(pendenz.eingangsdatum);
        restPendenz.eingangsdatumSTV = DateUtil.momentToLocalDate(pendenz.eingangsdatumSTV);
        restPendenz.aenderungsdatum = DateUtil.momentToLocalDateTime(pendenz.aenderungsdatum);
        restPendenz.gesuchsperiodeGueltigAb = DateUtil.momentToLocalDate(pendenz.gesuchsperiodeGueltigAb);
        restPendenz.gesuchsperiodeGueltigBis = DateUtil.momentToLocalDate(pendenz.gesuchsperiodeGueltigBis);
        restPendenz.institutionen = pendenz.institutionen;
        restPendenz.kinder = pendenz.kinder;
        restPendenz.verantwortlicher = pendenz.verantwortlicher;
        restPendenz.verantwortlicherSCH = pendenz.verantwortlicherSCH;
        restPendenz.verantwortlicherUsernameJA = pendenz.verantwortlicherUsernameJA;
        restPendenz.verantwortlicherUsernameSCH = pendenz.verantwortlicherUsernameSCH;
        restPendenz.status = pendenz.status;
        restPendenz.verfuegt = pendenz.verfuegt;
        restPendenz.beschwerdeHaengig = pendenz.beschwerdeHaengig;
        restPendenz.laufnummer = pendenz.laufnummer;
        restPendenz.gesuchBetreuungenStatus = pendenz.gesuchBetreuungenStatus;
        restPendenz.eingangsart = pendenz.eingangsart;
        restPendenz.besitzerUsername = pendenz.besitzerUsername;
        restPendenz.dokumenteHochgeladen = pendenz.dokumenteHochgeladen;
        return restPendenz;
    }

    public parseAntragDTO(antragTS: TSAntragDTO, antragFromServer: any): TSAntragDTO {
        antragTS.antragId = antragFromServer.antragId;
        antragTS.fallNummer = antragFromServer.fallNummer;
        antragTS.familienName = antragFromServer.familienName;
        antragTS.angebote = antragFromServer.angebote;
        antragTS.kinder = antragFromServer.kinder;
        antragTS.antragTyp = antragFromServer.antragTyp;
        antragTS.eingangsdatum = DateUtil.localDateToMoment(antragFromServer.eingangsdatum);
        antragTS.eingangsdatumSTV = DateUtil.localDateToMoment(antragFromServer.eingangsdatumSTV);
        antragTS.aenderungsdatum = DateUtil.localDateTimeToMoment(antragFromServer.aenderungsdatum);
        antragTS.gesuchsperiodeGueltigAb = DateUtil.localDateToMoment(antragFromServer.gesuchsperiodeGueltigAb);
        antragTS.gesuchsperiodeGueltigBis = DateUtil.localDateToMoment(antragFromServer.gesuchsperiodeGueltigBis);
        antragTS.institutionen = antragFromServer.institutionen;
        antragTS.verantwortlicher = antragFromServer.verantwortlicher;
        antragTS.verantwortlicherSCH = antragFromServer.verantwortlicherSCH;
        antragTS.verantwortlicherUsernameJA = antragFromServer.verantwortlicherUsernameJA;
        antragTS.verantwortlicherUsernameSCH = antragFromServer.verantwortlicherUsernameSCH;
        antragTS.status = antragFromServer.status;
        antragTS.verfuegt = antragFromServer.verfuegt;
        antragTS.beschwerdeHaengig = antragFromServer.beschwerdeHaengig;
        antragTS.laufnummer = antragFromServer.laufnummer;
        antragTS.gesuchBetreuungenStatus = antragFromServer.gesuchBetreuungenStatus;
        antragTS.eingangsart = antragFromServer.eingangsart;
        antragTS.besitzerUsername = antragFromServer.besitzerUsername;
        antragTS.dokumenteHochgeladen = antragFromServer.dokumenteHochgeladen;
        return antragTS;
    }

    public parseFallAntragDTO(fallAntragTS: TSFallAntragDTO, antragFromServer: any): TSFallAntragDTO {
        fallAntragTS.fallID = antragFromServer.fallID;
        fallAntragTS.fallNummer = antragFromServer.fallNummer;
        fallAntragTS.familienName = antragFromServer.familienName;
        return fallAntragTS;
    }

    public parseAntragDTOs(data: any): TSAntragDTO[] {
        let pendenzen: TSAntragDTO[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                pendenzen[i] = this.parseAntragDTO(new TSAntragDTO(), data[i]);
            }
        } else {
            pendenzen[0] = this.parseAntragDTO(new TSAntragDTO(), data);
        }
        return pendenzen;
    }

    public parseQuickSearchResult(dataFromServer: any): TSQuickSearchResult {
        if (dataFromServer) {
            let resultEntries: Array<TSSearchResultEntry> = this.parseSearchResultEntries(dataFromServer.resultEntities);
            return new TSQuickSearchResult(resultEntries, dataFromServer.numberOfResults);
        }
        return undefined;
    }

    private parseSearchResultEntries(entries: Array<any>): Array<TSSearchResultEntry> {
        let searchResultEntries: TSSearchResultEntry[] = [];
        if (entries && Array.isArray(entries)) {
            for (let i = 0; i < entries.length; i++) {
                searchResultEntries[i] = this.parseSearchResultEntry(new TSSearchResultEntry(), entries[i]);
            }
        }
        return searchResultEntries;
    }

    private parseSearchResultEntry(entry: TSSearchResultEntry, dataFromServer: any): TSSearchResultEntry {
        entry.additionalInformation = dataFromServer.additionalInformation;
        entry.gesuchID = dataFromServer.gesuchID;
        entry.fallID = dataFromServer.fallID;
        entry.resultId = dataFromServer.resultId;
        entry.text = dataFromServer.text;
        entry.entity = dataFromServer.entity;
        if (dataFromServer.antragDTO) {
            //dataFromServer.antragDTO.typ === TSAntragDTO
            if (this.isFallAntragDTO(dataFromServer.antragDTO)) {
                entry.antragDTO = this.parseFallAntragDTO(new TSFallAntragDTO(), dataFromServer.antragDTO);
            } else {
                entry.antragDTO = this.parseAntragDTO(new TSAntragDTO(), dataFromServer.antragDTO);
            }
        }
        return entry;
    }

    private isFallAntragDTO(antragRestObj: any): boolean {
        if (antragRestObj) {
            return antragRestObj.clazz === TSFallAntragDTO.serverClassName;
        }
        return false;
    }

    public pendenzBetreuungenToRestObject(restPendenz: any, pendenz: TSPendenzBetreuung): any {
        restPendenz.betreuungsNummer = pendenz.betreuungsNummer;
        restPendenz.betreuungsId = pendenz.betreuungsId;
        restPendenz.gesuchId = pendenz.gesuchId;
        restPendenz.kindId = pendenz.kindId;
        restPendenz.name = pendenz.name;
        restPendenz.vorname = pendenz.vorname;
        restPendenz.geburtsdatum = DateUtil.momentToLocalDate(pendenz.geburtsdatum);
        restPendenz.typ = pendenz.typ;
        restPendenz.gesuchsperiode = this.gesuchsperiodeToRestObject({}, pendenz.gesuchsperiode);
        restPendenz.eingangsdatum = DateUtil.momentToLocalDate(pendenz.eingangsdatum);
        restPendenz.eingangsdatumSTV = DateUtil.momentToLocalDate(pendenz.eingangsdatumSTV);
        restPendenz.betreuungsangebotTyp = pendenz.betreuungsangebotTyp;
        restPendenz.institution = pendenz.institution;
        return restPendenz;
    }

    public parsePendenzBetreuungen(pendenzTS: TSPendenzBetreuung, pendenzFromServer: any): TSPendenzBetreuung {
        pendenzTS.betreuungsNummer = pendenzFromServer.betreuungsNummer;
        pendenzTS.betreuungsId = pendenzFromServer.betreuungsId;
        pendenzTS.gesuchId = pendenzFromServer.gesuchId;
        pendenzTS.kindId = pendenzFromServer.kindId;
        pendenzTS.name = pendenzFromServer.name;
        pendenzTS.vorname = pendenzFromServer.vorname;
        pendenzTS.geburtsdatum = pendenzFromServer.geburtsdatum;
        pendenzTS.typ = pendenzFromServer.typ;
        pendenzTS.gesuchsperiode = this.parseGesuchsperiode(new TSGesuchsperiode(), pendenzFromServer.gesuchsperiode);
        pendenzTS.eingangsdatum = DateUtil.localDateToMoment(pendenzFromServer.eingangsdatum);
        pendenzTS.eingangsdatumSTV = DateUtil.localDateToMoment(pendenzFromServer.eingangsdatumSTV);
        pendenzTS.betreuungsangebotTyp = pendenzFromServer.betreuungsangebotTyp;
        pendenzTS.institution = pendenzFromServer.institution;
        return pendenzTS;
    }

    public parsePendenzBetreuungenList(data: any): TSPendenzBetreuung[] {
        let pendenzen: TSPendenzBetreuung[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                pendenzen[i] = this.parsePendenzBetreuungen(new TSPendenzBetreuung(), data[i]);
            }
        } else {
            pendenzen[0] = this.parsePendenzBetreuungen(new TSPendenzBetreuung(), data);
        }
        return pendenzen;
    }

    public userToRestObject(user: any, userTS: TSUser): any {
        if (userTS) {
            user.username = userTS.username;
            user.password = userTS.password;
            user.nachname = userTS.nachname;
            user.vorname = userTS.vorname;
            user.email = userTS.email;
            user.mandant = this.mandantToRestObject({}, userTS.mandant);
            user.gesperrt = userTS.gesperrt;
            if (userTS.berechtigungen) {
                user.berechtigungen = [];
                userTS.berechtigungen.forEach((berecht: TSBerechtigung) => {
                    user.berechtigungen.push(this.berechtigungToRestObject({}, berecht));
                });
                return user;
            }
            return undefined;
        }
    }

    public parseUser(userTS: TSUser, userFromServer: any): TSUser {
        if (userFromServer) {
            userTS.username = userFromServer.username;
            userTS.password = userFromServer.password;
            userTS.nachname = userFromServer.nachname;
            userTS.vorname = userFromServer.vorname;
            userTS.email = userFromServer.email;
            userTS.mandant = this.parseMandant(new TSMandant(), userFromServer.mandant);
            userTS.amt = userFromServer.amt;
            userTS.gesperrt = userFromServer.gesperrt;
            userTS.currentBerechtigung = this.parseBerechtigung(new TSBerechtigung(), userFromServer.currentBerechtigung);
            userTS.berechtigungen = this.parseBerechtigungen(userFromServer.berechtigungen);
            return userTS;
        }
        return undefined;
    }


    public parseBerechtigungen(data: Array<any>): TSBerechtigung[] {
        let berechtigungenList: TSBerechtigung[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                berechtigungenList[i] = this.parseBerechtigung(new TSBerechtigung(), data[i]);
            }
        } else if (data) {
            berechtigungenList[0] = this.parseBerechtigung(new TSBerechtigung(), data);
        }
        return berechtigungenList;
    }

    public parseUserList(data: any): TSUser[] {
        let users: TSUser[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                users[i] = this.parseUser(new TSUser(), data[i]);
            }
        } else {
            users[0] = this.parseUser(new TSUser(), data);
        }
        return users;
    }

    public berechtigungToRestObject(berechtigung: any, berechtigungTS: TSBerechtigung): any {
        if (berechtigungTS) {
            this.abstractDateRangeEntityToRestObject(berechtigung, berechtigungTS);
            berechtigung.role = berechtigungTS.role;
            berechtigung.traegerschaft = this.traegerschaftToRestObject({}, berechtigungTS.traegerschaft);
            berechtigung.institution = this.institutionToRestObject({}, berechtigungTS.institution);
            return berechtigung;
        }
        return undefined;
    }

    public parseBerechtigung(berechtigungTS: TSBerechtigung, berechtigungFromServer: any): TSBerechtigung {
        if (berechtigungFromServer) {
            this.parseDateRangeEntity(berechtigungTS, berechtigungFromServer);
            berechtigungTS.role = berechtigungFromServer.role;
            berechtigungTS.traegerschaft = this.parseTraegerschaft(new TSTraegerschaft(), berechtigungFromServer.traegerschaft);
            berechtigungTS.institution = this.parseInstitution(new TSInstitution(), berechtigungFromServer.institution);
            return berechtigungTS;
        }
        return undefined;
    }

    public parseBerechtigungenList(data: any): TSBerechtigung[] {
        let berechtigungen: TSBerechtigung[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                berechtigungen[i] = this.parseBerechtigung(new TSBerechtigung(), data[i]);
            }
        } else {
            berechtigungen[0] = this.parseBerechtigung(new TSBerechtigung(), data);
        }
        return berechtigungen;
    }

    public parseBerechtigungHistory(historyTS: TSBerechtigungHistory, historyFromServer: any): TSBerechtigungHistory {
        if (historyFromServer) {
            this.parseDateRangeEntity(historyTS, historyFromServer);
            historyTS.userErstellt = historyFromServer.userErstellt;
            historyTS.username = historyFromServer.username;
            historyTS.role = historyFromServer.role;
            historyTS.traegerschaft = this.parseTraegerschaft(new TSTraegerschaft(), historyFromServer.traegerschaft);
            historyTS.institution = this.parseInstitution(new TSInstitution(), historyFromServer.institution);
            historyTS.gesperrt = historyFromServer.gesperrt;
            historyTS.geloescht = historyFromServer.geloescht;
            return historyTS;
        }
        return undefined;
    }

    public parseBerechtigungHistoryList(data: any): TSBerechtigungHistory[] {
        let tsHistoryList: TSBerechtigungHistory[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                tsHistoryList[i] = this.parseBerechtigungHistory(new TSBerechtigungHistory(), data[i]);
            }
        } else {
            tsHistoryList[0] = this.parseBerechtigungHistory(new TSBerechtigungHistory(), data);
        }
        return tsHistoryList;
    }

    parseDokumenteDTO(dokumenteDTO: TSDokumenteDTO, dokumenteFromServer: any): TSDokumenteDTO {
        if (dokumenteFromServer) {
            dokumenteDTO.dokumentGruende = this.parseDokumentGruende(dokumenteFromServer.dokumentGruende);
            return dokumenteDTO;
        }
        return undefined;
    }

    private parseDokumentGruende(dokumentGruende: Array<any>): TSDokumentGrund[] {
        let resultList: TSDokumentGrund[] = [];
        if (dokumentGruende && Array.isArray(dokumentGruende)) {
            for (let i = 0; i < dokumentGruende.length; i++) {
                resultList[i] = this.parseDokumentGrund(new TSDokumentGrund(), dokumentGruende[i]);
            }
        } else {
            resultList[0] = this.parseDokumentGrund(new TSDokumentGrund(), dokumentGruende);
        }
        return resultList;
    }

    parseDokumentGrund(dokumentGrund: TSDokumentGrund, dokumentGrundFromServer: any): TSDokumentGrund {
        if (dokumentGrundFromServer) {
            this.parseAbstractEntity(dokumentGrund, dokumentGrundFromServer);
            dokumentGrund.dokumentGrundTyp = dokumentGrundFromServer.dokumentGrundTyp;
            dokumentGrund.fullName = dokumentGrundFromServer.fullName;
            dokumentGrund.tag = dokumentGrundFromServer.tag;
            dokumentGrund.personNumber = dokumentGrundFromServer.personNumber;
            dokumentGrund.personType = dokumentGrundFromServer.personType;
            dokumentGrund.dokumentTyp = dokumentGrundFromServer.dokumentTyp;
            dokumentGrund.needed = dokumentGrundFromServer.needed;
            dokumentGrund.dokumente = this.parseDokumente(dokumentGrundFromServer.dokumente);
            return dokumentGrund;
        }
        return undefined;
    }

    private parseDokumente(dokumente: Array<any>): TSDokument[] {
        let resultList: TSDokument[] = [];
        if (dokumente && Array.isArray(dokumente)) {
            for (let i = 0; i < dokumente.length; i++) {
                resultList[i] = this.parseDokument(new TSDokument(), dokumente[i]);
            }
        } else {
            resultList[0] = this.parseDokument(new TSDokument(), dokumente);
        }
        return resultList;
    }

    private parseDokument(dokument: TSDokument, dokumentFromServer: any): TSDokument {
        if (dokumentFromServer) {
            this.parseAbstractEntity(dokument, dokumentFromServer);
            dokument.filename = dokumentFromServer.filename;
            dokument.filepfad = dokumentFromServer.filepfad;
            dokument.filesize = dokumentFromServer.filesize;
            dokument.timestampUpload = DateUtil.localDateTimeToMoment(dokumentFromServer.timestampUpload);
            dokument.userUploaded = this.parseUser(new TSUser(), dokumentFromServer.userUploaded);
            return dokument;
        }
        return undefined;
    }

    public dokumentGrundToRestObject(restDokumentGrund: any, dokumentGrundTS: TSDokumentGrund): any {
        if (dokumentGrundTS) {
            this.abstractEntityToRestObject(restDokumentGrund, dokumentGrundTS);
            restDokumentGrund.tag = dokumentGrundTS.tag;
            restDokumentGrund.fullName = dokumentGrundTS.fullName;
            restDokumentGrund.personNumber = dokumentGrundTS.personNumber;
            restDokumentGrund.personType = dokumentGrundTS.personType;
            restDokumentGrund.dokumentGrundTyp = dokumentGrundTS.dokumentGrundTyp;
            restDokumentGrund.dokumentTyp = dokumentGrundTS.dokumentTyp;
            restDokumentGrund.needed = dokumentGrundTS.needed;
            restDokumentGrund.dokumente = this.dokumenteToRestObject(dokumentGrundTS.dokumente);

            return restDokumentGrund;
        }
        return undefined;
    }

    private dokumenteToRestObject(dokumente: Array<TSDokument>): Array<any> {
        let list: any[] = [];
        if (dokumente) {
            for (let i = 0; i < dokumente.length; i++) {
                list[i] = this.dokumentToRestObject({}, dokumente[i]);
            }
        }
        return list;
    }

    private dokumentToRestObject(dokument: any, dokumentTS: TSDokument): any {
        if (dokumentTS) {
            this.abstractEntityToRestObject(dokument, dokumentTS);
            dokument.filename = dokumentTS.filename;
            dokument.filepfad = dokumentTS.filepfad;
            dokument.filesize = dokumentTS.filesize;
            dokument.timestampUpload = DateUtil.momentToLocalDateTime(dokumentTS.timestampUpload);
            return dokument;
        }
        return undefined;
    }

    public parseVerfuegung(verfuegungTS: TSVerfuegung, verfuegungFromServer: any): TSVerfuegung {
        if (verfuegungFromServer) {
            this.parseAbstractEntity(verfuegungTS, verfuegungFromServer);
            verfuegungTS.generatedBemerkungen = verfuegungFromServer.generatedBemerkungen;
            verfuegungTS.manuelleBemerkungen = verfuegungFromServer.manuelleBemerkungen;
            verfuegungTS.zeitabschnitte = this.parseVerfuegungZeitabschnitte(verfuegungFromServer.zeitabschnitte);
            verfuegungTS.kategorieKeinPensum = verfuegungFromServer.kategorieKeinPensum;
            verfuegungTS.kategorieMaxEinkommen = verfuegungFromServer.kategorieMaxEinkommen;
            verfuegungTS.kategorieNichtEintreten = verfuegungFromServer.kategorieNichtEintreten;
            verfuegungTS.kategorieNormal = verfuegungFromServer.kategorieNormal;
            verfuegungTS.kategorieZuschlagZumErwerbspensum = verfuegungFromServer.kategorieZuschlagZumErwerbspensum;
            return verfuegungTS;
        }
        return undefined;
    }

    public verfuegungToRestObject(verfuegung: any, verfuegungTS: TSVerfuegung): any {
        if (verfuegungTS) {
            this.abstractEntityToRestObject(verfuegung, verfuegungTS);
            verfuegung.generatedBemerkungen = verfuegungTS.generatedBemerkungen;
            verfuegung.manuelleBemerkungen = verfuegungTS.manuelleBemerkungen;
            verfuegung.zeitabschnitte = this.zeitabschnittListToRestObject(verfuegungTS.zeitabschnitte);
            verfuegung.kategorieKeinPensum = verfuegungTS.kategorieKeinPensum;
            verfuegung.kategorieMaxEinkommen = verfuegungTS.kategorieMaxEinkommen;
            verfuegung.kategorieNichtEintreten = verfuegungTS.kategorieNichtEintreten;
            verfuegung.kategorieNormal = verfuegungTS.kategorieNormal;
            verfuegung.kategorieZuschlagZumErwerbspensum = verfuegungTS.kategorieZuschlagZumErwerbspensum;
            return verfuegung;
        }
        return undefined;
    }

    private zeitabschnittListToRestObject(zeitabschnitte: Array<TSVerfuegungZeitabschnitt>): Array<any> {
        let list: any[] = [];
        if (zeitabschnitte) {
            for (let i = 0; i < zeitabschnitte.length; i++) {
                list[i] = this.zeitabschnittToRestObject({}, zeitabschnitte[i]);
            }
        }
        return list;
    }

    private parseVerfuegungZeitabschnitte(zeitabschnitte: Array<any>): TSVerfuegungZeitabschnitt[] {
        let resultList: TSVerfuegungZeitabschnitt[] = [];
        if (zeitabschnitte && Array.isArray(zeitabschnitte)) {
            for (let i = 0; i < zeitabschnitte.length; i++) {
                resultList[i] = this.parseVerfuegungZeitabschnitt(new TSVerfuegungZeitabschnitt(), zeitabschnitte[i]);
            }
        } else {
            resultList[0] = this.parseVerfuegungZeitabschnitt(new TSVerfuegungZeitabschnitt(), zeitabschnitte);
        }
        return resultList;
    }

    public zeitabschnittToRestObject(zeitabschnitt: any, zeitabschnittTS: TSVerfuegungZeitabschnitt): any {
        if (zeitabschnittTS) {
            this.abstractDateRangeEntityToRestObject(zeitabschnitt, zeitabschnittTS);
            zeitabschnitt.abzugFamGroesse = zeitabschnittTS.abzugFamGroesse;
            zeitabschnitt.anspruchberechtigtesPensum = zeitabschnittTS.anspruchberechtigtesPensum;
            zeitabschnitt.bgPensum = zeitabschnittTS.bgPensum;
            zeitabschnitt.anspruchspensumRest = zeitabschnittTS.anspruchspensumRest;
            zeitabschnitt.bemerkungen = zeitabschnittTS.bemerkungen;
            zeitabschnitt.betreuungspensum = zeitabschnittTS.betreuungspensum;
            zeitabschnitt.betreuungsstunden = zeitabschnittTS.betreuungsstunden;
            zeitabschnitt.elternbeitrag = zeitabschnittTS.elternbeitrag;
            zeitabschnitt.erwerbspensumGS1 = zeitabschnittTS.erwerbspensumGS1;
            zeitabschnitt.erwerbspensumGS2 = zeitabschnittTS.erwerbspensumGS2;
            zeitabschnitt.fachstellenpensum = zeitabschnittTS.fachstellenpensum;
            zeitabschnitt.massgebendesEinkommenVorAbzugFamgr = zeitabschnittTS.massgebendesEinkommenVorAbzugFamgr;
            zeitabschnitt.famGroesse = zeitabschnittTS.famGroesse;
            zeitabschnitt.zahlungsstatus = zeitabschnittTS.zahlungsstatus;
            zeitabschnitt.vollkosten = zeitabschnittTS.vollkosten;
            zeitabschnitt.einkommensjahr = zeitabschnittTS.einkommensjahr;
            zeitabschnitt.kategorieZuschlagZumErwerbspensum = zeitabschnittTS.kategorieZuschlagZumErwerbspensum;
            zeitabschnitt.kategorieMaxEinkommen = zeitabschnittTS.kategorieMaxEinkommen;
            zeitabschnitt.kategorieKeinPensum = zeitabschnittTS.kategorieKeinPensum;
            zeitabschnitt.zuSpaetEingereicht = zeitabschnittTS.zuSpaetEingereicht;
            zeitabschnitt.sameVerfuegungsdaten = zeitabschnittTS.sameVerfuegungsdaten;
            zeitabschnitt.sameVerguenstigung = zeitabschnittTS.sameVerguenstigung;
            return zeitabschnitt;
        }
        return undefined;
    }

    public parseVerfuegungZeitabschnitt(verfuegungZeitabschnittTS: TSVerfuegungZeitabschnitt, zeitabschnittFromServer: any): TSVerfuegungZeitabschnitt {
        if (zeitabschnittFromServer) {
            this.parseDateRangeEntity(verfuegungZeitabschnittTS, zeitabschnittFromServer);
            verfuegungZeitabschnittTS.abzugFamGroesse = zeitabschnittFromServer.abzugFamGroesse;
            verfuegungZeitabschnittTS.anspruchberechtigtesPensum = zeitabschnittFromServer.anspruchberechtigtesPensum;
            verfuegungZeitabschnittTS.bgPensum = zeitabschnittFromServer.bgPensum;
            verfuegungZeitabschnittTS.anspruchspensumRest = zeitabschnittFromServer.anspruchspensumRest;
            verfuegungZeitabschnittTS.bemerkungen = zeitabschnittFromServer.bemerkungen;
            verfuegungZeitabschnittTS.betreuungspensum = zeitabschnittFromServer.betreuungspensum;
            verfuegungZeitabschnittTS.betreuungsstunden = zeitabschnittFromServer.betreuungsstunden;
            verfuegungZeitabschnittTS.elternbeitrag = zeitabschnittFromServer.elternbeitrag;
            verfuegungZeitabschnittTS.erwerbspensumGS1 = zeitabschnittFromServer.erwerbspensumGS1;
            verfuegungZeitabschnittTS.erwerbspensumGS2 = zeitabschnittFromServer.erwerbspensumGS2;
            verfuegungZeitabschnittTS.fachstellenpensum = zeitabschnittFromServer.fachstellenpensum;
            verfuegungZeitabschnittTS.massgebendesEinkommenVorAbzugFamgr = zeitabschnittFromServer.massgebendesEinkommenVorAbzugFamgr;
            verfuegungZeitabschnittTS.famGroesse = zeitabschnittFromServer.famGroesse;
            verfuegungZeitabschnittTS.zahlungsstatus = zeitabschnittFromServer.zahlungsstatus;
            verfuegungZeitabschnittTS.vollkosten = zeitabschnittFromServer.vollkosten;
            verfuegungZeitabschnittTS.einkommensjahr = zeitabschnittFromServer.einkommensjahr;
            verfuegungZeitabschnittTS.kategorieZuschlagZumErwerbspensum = zeitabschnittFromServer.kategorieZuschlagZumErwerbspensum;
            verfuegungZeitabschnittTS.kategorieMaxEinkommen = zeitabschnittFromServer.kategorieMaxEinkommen;
            verfuegungZeitabschnittTS.kategorieKeinPensum = zeitabschnittFromServer.kategorieKeinPensum;
            verfuegungZeitabschnittTS.zuSpaetEingereicht = zeitabschnittFromServer.zuSpaetEingereicht;
            verfuegungZeitabschnittTS.sameVerfuegungsdaten = zeitabschnittFromServer.sameVerfuegungsdaten;
            verfuegungZeitabschnittTS.sameVerguenstigung = zeitabschnittFromServer.sameVerguenstigung;
            return verfuegungZeitabschnittTS;
        }
        return undefined;
    }

    public parseDownloadFile(tsDownloadFile: TSDownloadFile, downloadFileFromServer: any) {
        if (downloadFileFromServer) {
            this.parseAbstractFileEntity(tsDownloadFile, downloadFileFromServer);
            tsDownloadFile.accessToken = downloadFileFromServer.accessToken;
            return tsDownloadFile;
        }
        return undefined;
    }

    public parseWizardStep(wizardStepTS: TSWizardStep, wizardStepFromServer: any): TSWizardStep {
        this.parseAbstractEntity(wizardStepTS, wizardStepFromServer);
        wizardStepTS.gesuchId = wizardStepFromServer.gesuchId;
        wizardStepTS.wizardStepName = wizardStepFromServer.wizardStepName;
        wizardStepTS.verfuegbar = wizardStepFromServer.verfuegbar;
        wizardStepTS.wizardStepStatus = wizardStepFromServer.wizardStepStatus;
        wizardStepTS.bemerkungen = wizardStepFromServer.bemerkungen;
        return wizardStepTS;
    }

    public wizardStepToRestObject(restWizardStep: any, wizardStep: TSWizardStep): any {
        this.abstractEntityToRestObject(restWizardStep, wizardStep);
        restWizardStep.gesuchId = wizardStep.gesuchId;
        restWizardStep.verfuegbar = wizardStep.verfuegbar;
        restWizardStep.wizardStepName = wizardStep.wizardStepName;
        restWizardStep.wizardStepStatus = wizardStep.wizardStepStatus;
        restWizardStep.bemerkungen = wizardStep.bemerkungen;
        return restWizardStep;
    }

    public parseWizardStepList(data: any): TSWizardStep[] {
        let wizardSteps: TSWizardStep[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                wizardSteps[i] = this.parseWizardStep(new TSWizardStep(), data[i]);
            }
        } else {
            wizardSteps[0] = this.parseWizardStep(new TSWizardStep(), data);
        }
        return wizardSteps;
    }

    public parseAntragStatusHistoryCollection(antragStatusHistoryCollection: Array<any>): TSAntragStatusHistory[] {
        let resultList: TSAntragStatusHistory[] = [];
        if (antragStatusHistoryCollection && Array.isArray(antragStatusHistoryCollection)) {
            for (let i = 0; i < antragStatusHistoryCollection.length; i++) {
                resultList[i] = this.parseAntragStatusHistory(new TSAntragStatusHistory(), antragStatusHistoryCollection[i]);
            }
        } else {
            resultList[0] = this.parseAntragStatusHistory(new TSAntragStatusHistory(), antragStatusHistoryCollection);
        }
        return resultList;
    }

    public parseAntragStatusHistory(antragStatusHistoryTS: TSAntragStatusHistory, antragStatusHistoryFromServer: any): TSAntragStatusHistory {
        this.parseAbstractEntity(antragStatusHistoryTS, antragStatusHistoryFromServer);
        antragStatusHistoryTS.gesuchId = antragStatusHistoryFromServer.gesuchId;
        antragStatusHistoryTS.benutzer = this.parseUser(new TSUser(), antragStatusHistoryFromServer.benutzer);
        antragStatusHistoryTS.timestampVon = DateUtil.localDateTimeToMoment(antragStatusHistoryFromServer.timestampVon);
        antragStatusHistoryTS.timestampBis = DateUtil.localDateTimeToMoment(antragStatusHistoryFromServer.timestampBis);
        antragStatusHistoryTS.status = antragStatusHistoryFromServer.status;
        return antragStatusHistoryTS;
    }

    public antragStatusHistoryToRestObject(restAntragStatusHistory: any, antragStatusHistory: TSAntragStatusHistory): any {
        this.abstractEntityToRestObject(restAntragStatusHistory, antragStatusHistory);
        restAntragStatusHistory.gesuchId = antragStatusHistory.gesuchId;
        restAntragStatusHistory.benutzer = this.userToRestObject({}, antragStatusHistory.benutzer);
        restAntragStatusHistory.timestampVon = DateUtil.momentToLocalDateTime(antragStatusHistory.timestampVon);
        restAntragStatusHistory.timestampBis = DateUtil.momentToLocalDateTime(antragStatusHistory.timestampBis);
        restAntragStatusHistory.status = antragStatusHistory.status;
        return restAntragStatusHistory;
    }

    public mahnungToRestObject(restMahnung: any, tsMahnung: TSMahnung): any {
        if (tsMahnung) {
            this.abstractEntityToRestObject(restMahnung, tsMahnung);
            restMahnung.gesuch = this.gesuchToRestObject({}, tsMahnung.gesuch);
            restMahnung.mahnungTyp = tsMahnung.mahnungTyp;
            restMahnung.datumFristablauf = DateUtil.momentToLocalDate(tsMahnung.datumFristablauf);
            restMahnung.bemerkungen = tsMahnung.bemerkungen;
            restMahnung.timestampAbgeschlossen = DateUtil.momentToLocalDateTime(tsMahnung.timestampAbgeschlossen);
            restMahnung.abgelaufen = tsMahnung.abgelaufen;
            return restMahnung;
        }
        return undefined;
    }

    public parseMahnungen(data: Array<any>): TSMahnung[] {
        let mahnungen: TSMahnung[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                mahnungen[i] = this.parseMahnung(new TSMahnung(), data[i]);
            }
        } else {
            mahnungen[0] = this.parseMahnung(new TSMahnung(), data);
        }
        return mahnungen;
    }

    public parseMahnung(tsMahnung: TSMahnung, mahnungFromServer: any): TSMahnung {
        if (mahnungFromServer) {
            this.parseAbstractEntity(tsMahnung, mahnungFromServer);

            tsMahnung.gesuch = this.parseGesuch(new TSGesuch(), mahnungFromServer.gesuch);
            tsMahnung.mahnungTyp = mahnungFromServer.mahnungTyp;
            tsMahnung.datumFristablauf = DateUtil.localDateToMoment(mahnungFromServer.datumFristablauf);
            tsMahnung.bemerkungen = mahnungFromServer.bemerkungen;
            tsMahnung.timestampAbgeschlossen = DateUtil.localDateTimeToMoment(mahnungFromServer.timestampAbgeschlossen);
            tsMahnung.abgelaufen = mahnungFromServer.abgelaufen;
            return tsMahnung;
        }
        return undefined;
    }

    finanzModelToRestObject(restFinSitModel: any, finSitModel: TSFinanzModel) {
        if (finSitModel) {
            if (finSitModel.finanzielleSituationContainerGS1) {
                restFinSitModel.finanzielleSituationContainerGS1 = this.finanzielleSituationContainerToRestObject({}, finSitModel.finanzielleSituationContainerGS1);
            }
            if (finSitModel.finanzielleSituationContainerGS2) {
                restFinSitModel.finanzielleSituationContainerGS2 = this.finanzielleSituationContainerToRestObject({}, finSitModel.finanzielleSituationContainerGS2);
            }
            if (finSitModel.einkommensverschlechterungContainerGS1) {
                restFinSitModel.einkommensverschlechterungContainerGS1 = this.einkommensverschlechterungContainerToRestObject({}, finSitModel.einkommensverschlechterungContainerGS1);
            }
            if (finSitModel.einkommensverschlechterungContainerGS2) {
                restFinSitModel.einkommensverschlechterungContainerGS2 = this.einkommensverschlechterungContainerToRestObject({}, finSitModel.einkommensverschlechterungContainerGS2);
            }
            if (finSitModel.einkommensverschlechterungInfoContainer) {
                restFinSitModel.einkommensverschlechterungInfoContainer = this.einkommensverschlechterungInfoContainerToRestObject({}, finSitModel.einkommensverschlechterungInfoContainer);
            }
            restFinSitModel.gemeinsameSteuererklaerung = finSitModel.gemeinsameSteuererklaerung;
            return restFinSitModel;
        }
        return undefined;

    }

    public gesuchstellerContainerToRestObject(restGSCont: any, gesuchstellerCont: TSGesuchstellerContainer): any {
        if (gesuchstellerCont) {
            this.abstractEntityToRestObject(restGSCont, gesuchstellerCont);
            restGSCont.adressen = this.adressenContainerListToRestObject(gesuchstellerCont.adressen);
            restGSCont.alternativeAdresse = this.adresseContainerToRestObject({}, gesuchstellerCont.korrespondenzAdresse);
            restGSCont.rechnungsAdresse = this.adresseContainerToRestObject({}, gesuchstellerCont.rechnungsAdresse);
            if (gesuchstellerCont.gesuchstellerGS) {
                restGSCont.gesuchstellerGS = this.gesuchstellerToRestObject({}, gesuchstellerCont.gesuchstellerGS);
            }
            if (gesuchstellerCont.gesuchstellerJA) {
                restGSCont.gesuchstellerJA = this.gesuchstellerToRestObject({}, gesuchstellerCont.gesuchstellerJA);
            }
            if (gesuchstellerCont.finanzielleSituationContainer) {
                restGSCont.finanzielleSituationContainer = this.finanzielleSituationContainerToRestObject({}, gesuchstellerCont.finanzielleSituationContainer);
            }
            if (gesuchstellerCont.einkommensverschlechterungContainer) {
                restGSCont.einkommensverschlechterungContainer = this.einkommensverschlechterungContainerToRestObject({}, gesuchstellerCont.einkommensverschlechterungContainer);
            }
            if (gesuchstellerCont.erwerbspensenContainer) {
                let erwPensenCont: Array<any> = [];
                for (let i = 0; i < gesuchstellerCont.erwerbspensenContainer.length; i++) {
                    erwPensenCont.push(this.erwerbspensumContainerToRestObject({}, gesuchstellerCont.erwerbspensenContainer[i]));
                }
                restGSCont.erwerbspensenContainers = erwPensenCont;
            }
            return restGSCont;
        }
        return undefined;
    }

    public parseGesuchstellerContainer(gesuchstellerContTS: TSGesuchstellerContainer, gesuchstellerContFromServer: any) {
        if (gesuchstellerContFromServer) {
            this.parseAbstractEntity(gesuchstellerContTS, gesuchstellerContFromServer);
            gesuchstellerContTS.gesuchstellerJA = this.parseGesuchsteller(new TSGesuchsteller(), gesuchstellerContFromServer.gesuchstellerJA);
            gesuchstellerContTS.gesuchstellerGS = this.parseGesuchsteller(new TSGesuchsteller(), gesuchstellerContFromServer.gesuchstellerGS);
            gesuchstellerContTS.adressen = this.parseAdressenContainerList(gesuchstellerContFromServer.adressen);
            gesuchstellerContTS.korrespondenzAdresse = this.parseAdresseContainer(
                new TSAdresseContainer(), gesuchstellerContFromServer.alternativeAdresse);
            gesuchstellerContTS.rechnungsAdresse = this.parseAdresseContainer(
                new TSAdresseContainer(), gesuchstellerContFromServer.rechnungsAdresse);
            gesuchstellerContTS.finanzielleSituationContainer = this.parseFinanzielleSituationContainer(
                new TSFinanzielleSituationContainer(), gesuchstellerContFromServer.finanzielleSituationContainer);
            gesuchstellerContTS.einkommensverschlechterungContainer = this.parseEinkommensverschlechterungContainer(
                new TSEinkommensverschlechterungContainer(), gesuchstellerContFromServer.einkommensverschlechterungContainer);
            gesuchstellerContTS.erwerbspensenContainer = this.parseErwerbspensenContainers(gesuchstellerContFromServer.erwerbspensenContainers);
            return gesuchstellerContTS;
        }
        return undefined;
    }

    private adressenContainerListToRestObject(adressen: Array<TSAdresseContainer>) {
        let list: any[] = [];
        if (adressen) {
            for (let i = 0; i < adressen.length; i++) {
                list[i] = this.adresseContainerToRestObject({}, adressen[i]);
            }
        }
        return list;
    }

    private adresseContainerToRestObject(restAddresseCont: any, adresseContTS: TSAdresseContainer): any {
        if (adresseContTS) {
            this.abstractEntityToRestObject(restAddresseCont, adresseContTS);
            restAddresseCont.adresseGS = this.adresseToRestObject({}, adresseContTS.adresseGS);
            restAddresseCont.adresseJA = this.adresseToRestObject({}, adresseContTS.adresseJA);
            return restAddresseCont;
        }
        return undefined;
    }

    private parseAdressenContainerList(adressen: any): Array<TSAdresseContainer> {
        let adressenList: Array<TSAdresseContainer> = [];
        if (adressen) {
            for (let i = 0; i < adressen.length; i++) {
                adressenList.push(this.parseAdresseContainer(new TSAdresseContainer(), adressen[i]));
            }
        }
        return adressenList;
    }

    private parseAdresseContainer(adresseContainerTS: TSAdresseContainer, adresseFromServer: any): TSAdresseContainer {
        if (adresseFromServer) {
            this.parseAbstractEntity(adresseContainerTS, adresseFromServer);
            adresseContainerTS.adresseGS = this.parseAdresse(new TSAdresse(), adresseFromServer.adresseGS);
            adresseContainerTS.adresseJA = this.parseAdresse(new TSAdresse(), adresseFromServer.adresseJA);
            return adresseContainerTS;
        }
        return undefined;
    }

    public parseWorkJobList(jobWrapper: any): Array<TSWorkJob> {
        let workJobList: Array<TSWorkJob> = [];
        if (jobWrapper && jobWrapper.jobs) {    //wrapped jobs
            for (let i = 0; i < jobWrapper.jobs.length; i++) {
                workJobList.push(this.parseWorkJob(new TSWorkJob, jobWrapper.jobs[i]));
            }
        }
        return workJobList;
    }

    private parseWorkJob(tsWorkJob: TSWorkJob, workjobFromServer: any): TSWorkJob {
        if (workjobFromServer) {
            this.parseAbstractEntity(tsWorkJob, workjobFromServer);
            tsWorkJob.startinguser = workjobFromServer.startinguser;
            tsWorkJob.batchJobStatus = workjobFromServer.batchJobStatus;
            tsWorkJob.executionId = workjobFromServer.executionId;
            tsWorkJob.params = workjobFromServer.params;
            tsWorkJob.workJobType = workjobFromServer.workJobType;
            tsWorkJob.resultData = workjobFromServer.resultData;
            tsWorkJob.requestURI = workjobFromServer.requestURI;
            tsWorkJob.execution =  this.parseBatchJobInformation(new TSBatchJobInformation(), workjobFromServer.execution);
            return tsWorkJob;
        }
        return undefined;
    }

    private parseBatchJobInformation(testBatchJobInfo: TSBatchJobInformation, batchJobInfoFromServer: any): TSBatchJobInformation {
        if (batchJobInfoFromServer) {
            testBatchJobInfo.batchStatus = batchJobInfoFromServer.batchStatus;
            testBatchJobInfo.createTime = batchJobInfoFromServer.createTime;
            testBatchJobInfo.endTime = batchJobInfoFromServer.endTime;
            testBatchJobInfo.executionId = batchJobInfoFromServer.executionId;
            testBatchJobInfo.executionId = batchJobInfoFromServer.executionId;
            testBatchJobInfo.jobName = batchJobInfoFromServer.jobName;
            testBatchJobInfo.lastUpdatedTime = batchJobInfoFromServer.lastUpdatedTime;
            testBatchJobInfo.startTime = batchJobInfoFromServer.startTime;
            return testBatchJobInfo;
        }
        return undefined;
    }

    public parseMitteilung(tsMitteilung: TSMitteilung, mitteilungFromServer: any): TSMitteilung {
        if (mitteilungFromServer) {
            this.parseAbstractEntity(tsMitteilung, mitteilungFromServer);
            tsMitteilung.fall = this.parseFall(new TSFall(), mitteilungFromServer.fall);
            if (mitteilungFromServer.betreuung) {
                tsMitteilung.betreuung = this.parseBetreuung(new TSBetreuung(), mitteilungFromServer.betreuung);
            }
            tsMitteilung.senderTyp = mitteilungFromServer.senderTyp;
            tsMitteilung.empfaengerTyp = mitteilungFromServer.empfaengerTyp;
            tsMitteilung.sender = this.parseUser(new TSUser(), mitteilungFromServer.sender);
            tsMitteilung.empfaenger = this.parseUser(new TSUser(), mitteilungFromServer.empfaenger);
            tsMitteilung.subject = mitteilungFromServer.subject;
            tsMitteilung.message = mitteilungFromServer.message;
            tsMitteilung.mitteilungStatus = mitteilungFromServer.mitteilungStatus;
            tsMitteilung.sentDatum = DateUtil.localDateTimeToMoment(mitteilungFromServer.sentDatum);
            return tsMitteilung;
        }
        return undefined;
    }

    public mitteilungToRestObject(restMitteilung: any, tsMitteilung: TSMitteilung): any {
        if (tsMitteilung) {
            this.abstractEntityToRestObject(restMitteilung, tsMitteilung);
            restMitteilung.fall = this.fallToRestObject({}, tsMitteilung.fall);
            if (tsMitteilung.betreuung) {
                restMitteilung.betreuung = this.betreuungToRestObject({}, tsMitteilung.betreuung);
            }
            restMitteilung.senderTyp = tsMitteilung.senderTyp;
            restMitteilung.empfaengerTyp = tsMitteilung.empfaengerTyp;
            restMitteilung.sender = this.userToRestObject({}, tsMitteilung.sender);
            restMitteilung.empfaenger = this.userToRestObject({}, tsMitteilung.empfaenger);
            restMitteilung.subject = tsMitteilung.subject;
            restMitteilung.message = tsMitteilung.message;
            restMitteilung.mitteilungStatus = tsMitteilung.mitteilungStatus;
            restMitteilung.sentDatum = DateUtil.momentToLocalDateTime(tsMitteilung.sentDatum);
            return restMitteilung;
        }
        return undefined;
    }

    public parseMitteilungen(mitteilungen: any): Array<TSMitteilung> {
        let mitteilungenList: Array<TSMitteilung> = [];
        if (mitteilungen) {
            for (let i = 0; i < mitteilungen.length; i++) {
                if (this.isBetreuungsmitteilung(mitteilungen[i])) {
                    mitteilungenList.push(this.parseBetreuungsmitteilung(new TSBetreuungsmitteilung(), mitteilungen[i]));

                } else { // by default normal Mitteilung
                    mitteilungenList.push(this.parseMitteilung(new TSMitteilung(), mitteilungen[i]));
                }
            }
        }
        return mitteilungenList;
    }

    public betreuungsmitteilungToRestObject(restBetreuungsmitteilung: any, tsBetreuungsmitteilung: TSBetreuungsmitteilung): any {
        if (tsBetreuungsmitteilung) {
            this.mitteilungToRestObject(restBetreuungsmitteilung, tsBetreuungsmitteilung);
            restBetreuungsmitteilung.applied = tsBetreuungsmitteilung.applied;
            if (tsBetreuungsmitteilung.betreuungspensen) {
                restBetreuungsmitteilung.betreuungspensen = [];
                tsBetreuungsmitteilung.betreuungspensen.forEach(betreuungspensum => {
                    restBetreuungsmitteilung.betreuungspensen.push(
                        this.betreuungsmitteilungPensumToRestObject({}, betreuungspensum));
                });

            }
        }
        return restBetreuungsmitteilung;
    }

    public parseBetreuungsmitteilung(tsBetreuungsmitteilung: TSBetreuungsmitteilung, betreuungsmitteilungFromServer: any): TSBetreuungsmitteilung {
        if (betreuungsmitteilungFromServer) {
            this.parseMitteilung(tsBetreuungsmitteilung, betreuungsmitteilungFromServer);
            tsBetreuungsmitteilung.applied = betreuungsmitteilungFromServer.applied;
            if (betreuungsmitteilungFromServer.betreuungspensen) {
                tsBetreuungsmitteilung.betreuungspensen = [];
                for (let i = 0; i < betreuungsmitteilungFromServer.betreuungspensen.length; i++) {
                    tsBetreuungsmitteilung.betreuungspensen.push(
                        this.parseBetreuungsmitteilungPensum(new TSBetreuungsmitteilungPensum(), betreuungsmitteilungFromServer.betreuungspensen[i]));
                }

            }
        }
        return tsBetreuungsmitteilung;
    }

    private isBetreuungsmitteilung(mitteilung: any): boolean {
        return mitteilung.betreuungspensen !== undefined;
    }

    public parseZahlungsauftragList(data: any): TSZahlungsauftrag[] {
        let zahlungsauftrag: TSZahlungsauftrag[] = [];
        if (data) {
            for (let i = 0; i < data.length; i++) {
                zahlungsauftrag[i] = this.parseZahlungsauftrag(new TSZahlungsauftrag(), data[i]);
            }
        }
        return zahlungsauftrag;
    }

    public parseZahlungsauftrag(tsZahlungsauftrag: TSZahlungsauftrag, zahlungsauftragFromServer: any): TSZahlungsauftrag {
        if (zahlungsauftragFromServer) {
            this.parseDateRangeEntity(tsZahlungsauftrag, zahlungsauftragFromServer);

            tsZahlungsauftrag.status = zahlungsauftragFromServer.status;
            tsZahlungsauftrag.beschrieb = zahlungsauftragFromServer.beschrieb;
            tsZahlungsauftrag.datumFaellig = DateUtil.localDateToMoment(zahlungsauftragFromServer.datumFaellig);
            tsZahlungsauftrag.datumGeneriert = DateUtil.localDateTimeToMoment(zahlungsauftragFromServer.datumGeneriert);
            tsZahlungsauftrag.betragTotalAuftrag = zahlungsauftragFromServer.betragTotalAuftrag;
            tsZahlungsauftrag.zahlungen = this.parseZahlungen(zahlungsauftragFromServer.zahlungen);

            return tsZahlungsauftrag;
        }
        return undefined;
    }

    public parseZahlungen(data: any): TSZahlung[] {
        let zahlungen: TSZahlung[] = [];
        if (data) {
            for (let i = 0; i < data.length; i++) {
                zahlungen[i] = this.parseZahlung(new TSZahlung(), data[i]);
            }
        }
        return zahlungen;
    }

    public parseZahlung(tsZahlung: TSZahlung, zahlungFromServer: any): TSZahlung {
        if (zahlungFromServer) {
            this.parseAbstractEntity(tsZahlung, zahlungFromServer);

            tsZahlung.betragTotalZahlung = zahlungFromServer.betragTotalZahlung;
            tsZahlung.institutionsName = zahlungFromServer.institutionsName;
            tsZahlung.status = zahlungFromServer.status;

            return tsZahlung;
        }
        return undefined;
    }

    public parseEWKResultat(ewkResultatTS: TSEWKResultat, ewkResultatFromServer: any) {
        if (ewkResultatFromServer) {
            ewkResultatTS.maxResultate = ewkResultatFromServer.maxResultate;
            ewkResultatTS.anzahlResultate = ewkResultatFromServer.anzahlResultate;
            ewkResultatTS.personen = this.parseEWKPersonList(ewkResultatFromServer.personen);
            return ewkResultatTS;
        }
        return undefined;
    }

    private parseEWKPersonList(data: any): TSEWKPerson[] {
        let personen: TSEWKPerson[] = [];
        if (data) {
            for (let i = 0; i < data.length; i++) {
                personen[i] = this.parseEWKPerson(new TSEWKPerson(), data[i]);
            }
        }
        return personen;
    }

    private parseEWKPerson(tsEWKPerson: TSEWKPerson, ewkPersonFromServer: any): TSEWKPerson {
        if (ewkPersonFromServer) {
            tsEWKPerson.personID = ewkPersonFromServer.personID;
            tsEWKPerson.einwohnercodes = this.parseEWKEinwohnercodeList(ewkPersonFromServer.einwohnercodes);
            tsEWKPerson.nachname = ewkPersonFromServer.nachname;
            tsEWKPerson.ledigname = ewkPersonFromServer.ledigname;
            tsEWKPerson.vorname = ewkPersonFromServer.vorname;
            tsEWKPerson.rufname = ewkPersonFromServer.rufname;
            tsEWKPerson.geburtsdatum = DateUtil.localDateToMoment(ewkPersonFromServer.geburtsdatum);
            tsEWKPerson.zuzugsdatum = DateUtil.localDateToMoment(ewkPersonFromServer.zuzugsdatum);
            tsEWKPerson.nationalitaet = ewkPersonFromServer.nationalitaet;
            tsEWKPerson.zivilstand = ewkPersonFromServer.zivilstand;
            tsEWKPerson.zivilstandTxt = ewkPersonFromServer.zivilstandTxt;
            tsEWKPerson.zivilstandsdatum = DateUtil.localDateToMoment(ewkPersonFromServer.zivilstandsdatum);
            tsEWKPerson.geschlecht = ewkPersonFromServer.geschlecht;
            tsEWKPerson.bewilligungsart = ewkPersonFromServer.bewilligungsart;
            tsEWKPerson.bewilligungsartTxt = ewkPersonFromServer.bewilligungsartTxt;
            tsEWKPerson.bewilligungBis = DateUtil.localDateToMoment(ewkPersonFromServer.bewilligungBis);
            tsEWKPerson.adressen = this.parseEWKAdresseList(ewkPersonFromServer.adressen);
            tsEWKPerson.beziehungen = this.parseEWKBeziehungList(ewkPersonFromServer.beziehungen);
            return tsEWKPerson;
        }
        return undefined;
    }

    private parseEWKEinwohnercodeList(data: any): TSEWKEinwohnercode[] {
        let codes: TSEWKEinwohnercode[] = [];
        if (data) {
            for (let i = 0; i < data.length; i++) {
                codes[i] = this.parseEWKEinwohnercode(new TSEWKEinwohnercode(), data[i]);
            }
        }
        return codes;
    }

    private parseEWKEinwohnercode(tsEWKEinwohnercode: TSEWKEinwohnercode, ewkEinwohnercodeFromServer: any): TSEWKEinwohnercode {
        if (ewkEinwohnercodeFromServer) {
            tsEWKEinwohnercode.code = ewkEinwohnercodeFromServer.code;
            tsEWKEinwohnercode.codeTxt = ewkEinwohnercodeFromServer.codeTxt;
            tsEWKEinwohnercode.gueltigVon = DateUtil.localDateToMoment(ewkEinwohnercodeFromServer.gueltigVon);
            tsEWKEinwohnercode.gueltigBis = DateUtil.localDateToMoment(ewkEinwohnercodeFromServer.gueltigBis);
            return tsEWKEinwohnercode;
        }
        return undefined;
    }

    private parseEWKAdresseList(data: any): TSEWKAdresse[] {
        let adressen: TSEWKAdresse[] = [];
        if (data) {
            for (let i = 0; i < data.length; i++) {
                adressen[i] = this.parseEWKAdresse(new TSEWKAdresse(), data[i]);
            }
        }
        return adressen;
    }

    private parseEWKAdresse(tsEWKAdresse: TSEWKAdresse, ewkAdresseFromServer: any): TSEWKAdresse {
        if (ewkAdresseFromServer) {
            tsEWKAdresse.adresstyp = ewkAdresseFromServer.adresstyp;
            tsEWKAdresse.adresstypTxt = ewkAdresseFromServer.adresstypTxt;
            tsEWKAdresse.gueltigVon = DateUtil.localDateToMoment(ewkAdresseFromServer.gueltigVon);
            tsEWKAdresse.gueltigBis = DateUtil.localDateToMoment(ewkAdresseFromServer.gueltigBis);
            tsEWKAdresse.coName = ewkAdresseFromServer.coName;
            tsEWKAdresse.postfach = ewkAdresseFromServer.postfach;
            tsEWKAdresse.bfSGemeinde = ewkAdresseFromServer.bfSGemeinde;
            tsEWKAdresse.strasse = ewkAdresseFromServer.strasse;
            tsEWKAdresse.hausnummer = ewkAdresseFromServer.hausnummer;
            tsEWKAdresse.postleitzahl = ewkAdresseFromServer.postleitzahl;
            tsEWKAdresse.ort = ewkAdresseFromServer.ort;
            tsEWKAdresse.kanton = ewkAdresseFromServer.kanton;
            tsEWKAdresse.land = ewkAdresseFromServer.land;
            return tsEWKAdresse;
        }
        return undefined;
    }

    private parseEWKBeziehungList(data: any): TSEWKBeziehung[] {
        let beziehungen: TSEWKBeziehung[] = [];
        if (data) {
            for (let i = 0; i < data.length; i++) {
                beziehungen[i] = this.parseEWKBeziehung(new TSEWKBeziehung(), data[i]);
            }
        }
        return beziehungen;
    }

    private parseEWKBeziehung(tsEWKBeziehung: TSEWKBeziehung, ewkBeziehungFromServer: any): TSEWKBeziehung {
        if (ewkBeziehungFromServer) {
            tsEWKBeziehung.beziehungstyp = ewkBeziehungFromServer.beziehungstyp;
            tsEWKBeziehung.beziehungstypTxt = ewkBeziehungFromServer.beziehungstypTxt;
            tsEWKBeziehung.personID = ewkBeziehungFromServer.personID;
            tsEWKBeziehung.nachname = ewkBeziehungFromServer.nachname;
            tsEWKBeziehung.ledigname = ewkBeziehungFromServer.ledigname;
            tsEWKBeziehung.vorname = ewkBeziehungFromServer.vorname;
            tsEWKBeziehung.rufname = ewkBeziehungFromServer.rufname;
            tsEWKBeziehung.geburtsdatum = DateUtil.localDateToMoment(ewkBeziehungFromServer.geburtsdatum);
            tsEWKBeziehung.adresse = this.parseEWKAdresse(new TSEWKAdresse(), ewkBeziehungFromServer.adresse);
            return tsEWKBeziehung;
        }
        return undefined;
    }

    public parseModuleTagesschuleArray(data: Array<any>): TSModulTagesschule[] {
        let moduleTagesschule: TSModulTagesschule[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                moduleTagesschule[i] = this.parseModulTagesschule(new TSModulTagesschule(), data[i]);
            }
        } else {
            moduleTagesschule[0] = this.parseModulTagesschule(new TSModulTagesschule(), data);
        }
        return moduleTagesschule;
    }

    private parseModulTagesschule(modulTagesschuleTS: TSModulTagesschule, modulFromServer: any): TSModulTagesschule {
        if (modulFromServer) {
            this.parseAbstractEntity(modulTagesschuleTS, modulFromServer);
            modulTagesschuleTS.modulTagesschuleName = modulFromServer.modulTagesschuleName;
            modulTagesschuleTS.wochentag = modulFromServer.wochentag;
            modulTagesschuleTS.zeitVon = DateUtil.localDateTimeToMoment(modulFromServer.zeitVon);
            modulTagesschuleTS.zeitBis = DateUtil.localDateTimeToMoment(modulFromServer.zeitBis);
            return modulTagesschuleTS;
        }
        return undefined;
    }

    private moduleTagesschuleArrayToRestObject(moduleTagesschule: Array<TSModulTagesschule>): any[] {
        let list: any[] = [];
        if (moduleTagesschule) {
            for (let i = 0; i < moduleTagesschule.length; i++) {
                list[i] = this.modulTagesschuleToRestObject({}, moduleTagesschule[i]);
            }
        }
        return list;
    }

    private modulTagesschuleToRestObject(restModul: any, modulTagesschuleTS: TSModulTagesschule): any {
        if (modulTagesschuleTS) {
            this.abstractEntityToRestObject(restModul, modulTagesschuleTS);
            restModul.modulTagesschuleName = modulTagesschuleTS.modulTagesschuleName;
            restModul.wochentag = modulTagesschuleTS.wochentag;
            restModul.zeitVon = DateUtil.momentToLocalDateTime(modulTagesschuleTS.zeitVon);
            restModul.zeitBis = DateUtil.momentToLocalDateTime(modulTagesschuleTS.zeitBis);
            return restModul;
        }
        return undefined;
    }

    private parseBelegungTagesschule(belegungTS: TSBelegungTagesschule, belegungFromServer: any): TSBelegungTagesschule {
        if (belegungFromServer) {
            this.parseAbstractEntity(belegungTS, belegungFromServer);
            belegungTS.moduleTagesschule = this.parseModuleTagesschuleArray(belegungFromServer.moduleTagesschule);
            belegungTS.eintrittsdatum = DateUtil.localDateToMoment(belegungFromServer.eintrittsdatum);
            return belegungTS;
        }
        return undefined;
    }

    private belegungTagesschuleToRestObject(restBelegung: any, belegungTS: TSBelegungTagesschule): any {
        if (belegungTS) {
            this.abstractEntityToRestObject(restBelegung, belegungTS);
            restBelegung.moduleTagesschule = this.moduleTagesschuleArrayToRestObject(belegungTS.moduleTagesschule);
            restBelegung.eintrittsdatum = DateUtil.momentToLocalDate(belegungTS.eintrittsdatum);
            return restBelegung;
        }
        return undefined;
    }

    public parseFerieninselStammdatenList(data: any): TSFerieninselStammdaten[] {
        let ferieninselStammdatenList: TSFerieninselStammdaten[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                ferieninselStammdatenList[i] = this.parseFerieninselStammdaten(new TSFerieninselStammdaten(), data[i]);
            }
        } else {
            ferieninselStammdatenList[0] = this.parseFerieninselStammdaten(new TSFerieninselStammdaten(), data);
        }
        return ferieninselStammdatenList;
    }

    public parseFerieninselStammdaten(ferieninselStammdatenTS: TSFerieninselStammdaten, receivedFerieninselStammdaten: any): TSFerieninselStammdaten {
        if (receivedFerieninselStammdaten) {
            this.parseAbstractEntity(ferieninselStammdatenTS, receivedFerieninselStammdaten);
            ferieninselStammdatenTS.ferienname = receivedFerieninselStammdaten.ferienname;
            ferieninselStammdatenTS.anmeldeschluss = DateUtil.localDateToMoment(receivedFerieninselStammdaten.anmeldeschluss);
            ferieninselStammdatenTS.gesuchsperiode = this.parseGesuchsperiode(new TSGesuchsperiode(), receivedFerieninselStammdaten.gesuchsperiode);
            if (receivedFerieninselStammdaten.zeitraumList[0]) {
                let firstZeitraum: TSFerieninselZeitraum = new TSFerieninselZeitraum();
                this.parseDateRangeEntity(firstZeitraum, receivedFerieninselStammdaten.zeitraumList[0]);
                ferieninselStammdatenTS.zeitraum = firstZeitraum;
            }
            ferieninselStammdatenTS.zeitraumList = [];
            for (let i = 1; i < receivedFerieninselStammdaten.zeitraumList.length; i++) {
                let zeitraum: TSFerieninselZeitraum = new TSFerieninselZeitraum();
                this.parseDateRangeEntity(zeitraum, receivedFerieninselStammdaten.zeitraumList[i]);
                ferieninselStammdatenTS.zeitraumList.push(zeitraum);
            }
            if (receivedFerieninselStammdaten.potenzielleFerieninselTageFuerBelegung) {
                ferieninselStammdatenTS.potenzielleFerieninselTageFuerBelegung = this.parseBelegungFerieninselTagList(receivedFerieninselStammdaten.potenzielleFerieninselTageFuerBelegung);
            }
            return ferieninselStammdatenTS;
        }
        return undefined;
    }

    public ferieninselStammdatenToRestObject(restFerieninselStammdaten: any, ferieninselStammdatenTS: TSFerieninselStammdaten): any {
        if (ferieninselStammdatenTS) {
            this.abstractEntityToRestObject(restFerieninselStammdaten, ferieninselStammdatenTS);
            restFerieninselStammdaten.ferienname = ferieninselStammdatenTS.ferienname;
            restFerieninselStammdaten.anmeldeschluss = DateUtil.momentToLocalDate(ferieninselStammdatenTS.anmeldeschluss);
            restFerieninselStammdaten.gesuchsperiode = this.gesuchsperiodeToRestObject({}, ferieninselStammdatenTS.gesuchsperiode);
            if (ferieninselStammdatenTS.zeitraum) {
                let firstZeitraum: any = {};
                this.abstractDateRangeEntityToRestObject(firstZeitraum, ferieninselStammdatenTS.zeitraum);
                restFerieninselStammdaten.zeitraumList = [];
                restFerieninselStammdaten.zeitraumList[0] = firstZeitraum;
            }
            if (ferieninselStammdatenTS.zeitraumList) {
                for (let i = 0; i < ferieninselStammdatenTS.zeitraumList.length; i++) {
                    let zeitraum: any = {};
                    this.abstractDateRangeEntityToRestObject(zeitraum, ferieninselStammdatenTS.zeitraumList[i]);
                    restFerieninselStammdaten.zeitraumList[i + 1] = zeitraum;
                }
            }
            return restFerieninselStammdaten;
        }
        return undefined;
    }

    public parseBelegungFerieninselList(data: any): TSBelegungFerieninsel[] {
        let belegungFerieninselList: TSBelegungFerieninsel[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                belegungFerieninselList[i] = this.parseBelegungFerieninsel(new TSBelegungFerieninsel(), data[i]);
            }
        } else {
            belegungFerieninselList[0] = this.parseBelegungFerieninsel(new TSBelegungFerieninsel(), data);
        }
        return belegungFerieninselList;
    }

    public parseBelegungFerieninsel(belegungFerieninselTS: TSBelegungFerieninsel, receivedBelegungFerieninsel: any): TSBelegungFerieninsel {
        if (receivedBelegungFerieninsel) {
            this.parseAbstractEntity(belegungFerieninselTS, receivedBelegungFerieninsel);
            belegungFerieninselTS.ferienname = receivedBelegungFerieninsel.ferienname;
            belegungFerieninselTS.tage = this.parseBelegungFerieninselTagList(receivedBelegungFerieninsel.tage);
            return belegungFerieninselTS;
        }
        return undefined;
    }

    private parseBelegungFerieninselTagList(data: any): TSBelegungFerieninselTag[] {
        let belegungFerieninselTagList: TSBelegungFerieninselTag[] = [];
        if (data && Array.isArray(data)) {
            for (let i = 0; i < data.length; i++) {
                belegungFerieninselTagList[i] = this.parseBelegungFerieninselTag(new TSBelegungFerieninselTag(), data[i]);
            }
        } else {
            belegungFerieninselTagList[0] = this.parseBelegungFerieninselTag(new TSBelegungFerieninselTag(), data);
        }
        return belegungFerieninselTagList;
    }

    private parseBelegungFerieninselTag(belegungFerieninselTagTS: TSBelegungFerieninselTag, receivedBelegungFerieninselTag: any): TSBelegungFerieninselTag {
        if (receivedBelegungFerieninselTag) {
            this.parseAbstractEntity(belegungFerieninselTagTS, receivedBelegungFerieninselTag);
            belegungFerieninselTagTS.tag = DateUtil.localDateToMoment(receivedBelegungFerieninselTag.tag);
            return belegungFerieninselTagTS;
        }
        return undefined;
    }

    public belegungFerieninselToRestObject(restBelegungFerieninsel: any, belegungFerieninselTS: TSBelegungFerieninsel): any {
        if (belegungFerieninselTS) {
            this.abstractEntityToRestObject(restBelegungFerieninsel, belegungFerieninselTS);
            restBelegungFerieninsel.ferienname = belegungFerieninselTS.ferienname;
            restBelegungFerieninsel.tage = [];
            if (belegungFerieninselTS.tage) {
                for (let i = 0; i < belegungFerieninselTS.tage.length; i++) {
                    let tagRest: any = {};
                    this.abstractEntityToRestObject(tagRest, belegungFerieninselTS.tage[i]);
                    tagRest.tag = DateUtil.momentToLocalDate(belegungFerieninselTS.tage[i].tag);
                    restBelegungFerieninsel.tage.push(tagRest);
                }
            }
            return restBelegungFerieninsel;
        }
        return undefined;
    }
}
