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
import {TSAbstractMutableEntity} from '../models/TSAbstractMutableEntity';
import {TSAbstractPensumEntity} from '../models/TSAbstractPensumEntity';
import TSAbstractPersonEntity from '../models/TSAbstractPersonEntity';
import TSAbwesenheit from '../models/TSAbwesenheit';
import TSAbwesenheitContainer from '../models/TSAbwesenheitContainer';
import TSAdresse from '../models/TSAdresse';
import TSAdresseContainer from '../models/TSAdresseContainer';
import TSAnmeldungDTO from '../models/TSAnmeldungDTO';
import TSAntragDTO from '../models/TSAntragDTO';
import TSAntragStatusHistory from '../models/TSAntragStatusHistory';
import TSApplicationProperty from '../models/TSApplicationProperty';
import TSBatchJobInformation from '../models/TSBatchJobInformation';
import TSBelegungFerieninsel from '../models/TSBelegungFerieninsel';
import TSBelegungFerieninselTag from '../models/TSBelegungFerieninselTag';
import TSBelegungTagesschule from '../models/TSBelegungTagesschule';
import TSBenutzer from '../models/TSBenutzer';
import TSBerechtigung from '../models/TSBerechtigung';
import TSBerechtigungHistory from '../models/TSBerechtigungHistory';
import TSBetreuung from '../models/TSBetreuung';
import TSBetreuungsmitteilung from '../models/TSBetreuungsmitteilung';
import TSBetreuungsmitteilungPensum from '../models/TSBetreuungsmitteilungPensum';
import TSBetreuungspensum from '../models/TSBetreuungspensum';
import TSBetreuungspensumContainer from '../models/TSBetreuungspensumContainer';
import TSDokument from '../models/TSDokument';
import TSDokumentGrund from '../models/TSDokumentGrund';
import TSDossier from '../models/TSDossier';
import TSDownloadFile from '../models/TSDownloadFile';
import TSEbeguVorlage from '../models/TSEbeguVorlage';
import TSEinkommensverschlechterung from '../models/TSEinkommensverschlechterung';
import TSEinkommensverschlechterungContainer from '../models/TSEinkommensverschlechterungContainer';
import TSEinkommensverschlechterungInfo from '../models/TSEinkommensverschlechterungInfo';
import TSEinkommensverschlechterungInfoContainer from '../models/TSEinkommensverschlechterungInfoContainer';
import TSEinstellung from '../models/TSEinstellung';
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
import TSGemeinde from '../models/TSGemeinde';
import TSGemeindeKonfiguration from '../models/TSGemeindeKonfiguration';
import TSGemeindeStammdaten from '../models/TSGemeindeStammdaten';
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
import TSVerfuegung from '../models/TSVerfuegung';
import TSVerfuegungZeitabschnitt from '../models/TSVerfuegungZeitabschnitt';
import TSVorlage from '../models/TSVorlage';
import TSWizardStep from '../models/TSWizardStep';
import TSWorkJob from '../models/TSWorkJob';
import TSZahlung from '../models/TSZahlung';
import TSZahlungsauftrag from '../models/TSZahlungsauftrag';
import {TSDateRange} from '../models/types/TSDateRange';
import TSLand from '../models/types/TSLand';
import DateUtil from './DateUtil';
import EbeguUtil from './EbeguUtil';

export default class EbeguRestUtil {
    public static $inject = ['EbeguUtil'];

    public constructor(private readonly ebeguUtil: EbeguUtil) {
    }

    /**
     * Wandelt Data in einen TSApplicationProperty Array um, welches danach zurueckgeliefert wird
     */
    public parseApplicationProperties(data: any): TSApplicationProperty[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseApplicationProperty(new TSApplicationProperty('', ''), item))
            : [this.parseApplicationProperty(new TSApplicationProperty('', ''), data)];
    }

    /**
     * Wandelt die receivedAppProperty in einem parsedAppProperty um.
     */
    public parseApplicationProperty(parsedAppProperty: TSApplicationProperty,
                                    receivedAppProperty: any,
    ): TSApplicationProperty {
        this.parseAbstractMutableEntity(parsedAppProperty, receivedAppProperty);
        parsedAppProperty.name = receivedAppProperty.name;
        parsedAppProperty.value = receivedAppProperty.value;
        return parsedAppProperty;
    }

    public parseEinstellungList(data: any): TSEinstellung[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseEinstellung(new TSEinstellung(), item))
            : [this.parseEinstellung(new TSEinstellung(), data)];
    }

    public parseEinstellung(tsEinstellung: TSEinstellung, receivedEinstellung: any): TSEinstellung {
        if (receivedEinstellung) {
            this.parseDateRangeEntity(tsEinstellung, receivedEinstellung);
            tsEinstellung.key = receivedEinstellung.key;
            tsEinstellung.value = receivedEinstellung.value;
            tsEinstellung.gemeindeId = receivedEinstellung.gemeindeId;
            tsEinstellung.gesuchsperiodeId = receivedEinstellung.gesuchsperiodeId;
            // Mandant wird aktuell nicht gemappt
            return tsEinstellung;
        }
        return undefined;
    }

    public einstellungToRestObject(restEinstellung: any, tsEinstellung: TSEinstellung): TSEinstellung {
        if (tsEinstellung) {
            this.abstractDateRangeEntityToRestObject(restEinstellung, tsEinstellung);
            restEinstellung.key = tsEinstellung.key;
            restEinstellung.value = tsEinstellung.value;
            restEinstellung.gemeindeId = tsEinstellung.gemeindeId;
            restEinstellung.gesuchsperiodeId = tsEinstellung.gesuchsperiodeId;
            // Mandant wird aktuell nicht gemappt
            return restEinstellung;
        }
        return undefined;
    }

    public parseEbeguVorlages(data: any): TSEbeguVorlage[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseEbeguVorlage(new TSEbeguVorlage(), item))
            : [this.parseEbeguVorlage(new TSEbeguVorlage(), data)];
    }

    public parseEbeguVorlage(ebeguVorlageTS: TSEbeguVorlage, receivedEbeguVorlage: any): TSEbeguVorlage {
        if (receivedEbeguVorlage) {
            this.parseDateRangeEntity(ebeguVorlageTS, receivedEbeguVorlage);
            ebeguVorlageTS.name = receivedEbeguVorlage.name;
            ebeguVorlageTS.vorlage = this.parseVorlage(new TSVorlage(), receivedEbeguVorlage.vorlage);
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

    private parseAbstractFileEntity(fileTS: TSFile, fileFromServer: any): TSFile {
        this.parseAbstractMutableEntity(fileTS, fileFromServer);
        fileTS.filename = fileFromServer.filename;
        fileTS.filepfad = fileFromServer.filepfad;
        fileTS.filesize = fileFromServer.filesize;
        return fileTS;
    }

    private abstractFileEntityToRestObject(restObject: any, typescriptObject: TSFile): any {
        this.abstractMutableEntityToRestObject(restObject, typescriptObject);
        restObject.filename = typescriptObject.filename;
        restObject.filepfad = typescriptObject.filepfad;
        restObject.filesize = typescriptObject.filesize;
        return restObject;
    }

    private parseAbstractEntity(parsedAbstractEntity: TSAbstractEntity, receivedAbstractEntity: any): void {
        parsedAbstractEntity.id = receivedAbstractEntity.id;
        parsedAbstractEntity.timestampErstellt =
            DateUtil.localDateTimeToMoment(receivedAbstractEntity.timestampErstellt);
        parsedAbstractEntity.timestampMutiert = DateUtil.localDateTimeToMoment(receivedAbstractEntity.timestampMutiert);
    }

    private abstractEntityToRestObject(restObject: any, typescriptObject: TSAbstractEntity): void {
        restObject.id = typescriptObject.id;
        if (typescriptObject.timestampErstellt) {
            restObject.timestampErstellt = DateUtil.momentToLocalDateTime(typescriptObject.timestampErstellt);
        }
        if (typescriptObject.timestampMutiert) {
            restObject.timestampMutiert = DateUtil.momentToLocalDateTime(typescriptObject.timestampMutiert);
        }
    }

    private parseAbstractMutableEntity(parsedAbstractEntity: TSAbstractMutableEntity,
                                       receivedAbstractEntity: any,
    ): void {
        this.parseAbstractEntity(parsedAbstractEntity, receivedAbstractEntity);
        parsedAbstractEntity.vorgaengerId = receivedAbstractEntity.vorgaengerId;
    }

    private abstractMutableEntityToRestObject(restObject: any, typescriptObject: TSAbstractMutableEntity): void {
        this.abstractEntityToRestObject(restObject, typescriptObject);
        restObject.vorgaengerId = typescriptObject.vorgaengerId;
    }

    private parseAbstractPersonEntity(personObjectTS: TSAbstractPersonEntity, receivedPersonObject: any): void {
        this.parseAbstractMutableEntity(personObjectTS, receivedPersonObject);
        personObjectTS.vorname = receivedPersonObject.vorname;
        personObjectTS.nachname = receivedPersonObject.nachname;
        personObjectTS.geburtsdatum = DateUtil.localDateToMoment(receivedPersonObject.geburtsdatum);
        personObjectTS.geschlecht = receivedPersonObject.geschlecht;
    }

    private abstractPersonEntitytoRestObject(restPersonObject: any, personObject: TSAbstractPersonEntity): void {
        this.abstractMutableEntityToRestObject(restPersonObject, personObject);
        restPersonObject.vorname = personObject.vorname;
        restPersonObject.nachname = personObject.nachname;
        restPersonObject.geburtsdatum = DateUtil.momentToLocalDate(personObject.geburtsdatum);
        restPersonObject.geschlecht = personObject.geschlecht;
    }

    private abstractDateRangeEntityToRestObject(restObj: any, dateRangedEntity: TSAbstractDateRangedEntity): void {
        this.abstractMutableEntityToRestObject(restObj, dateRangedEntity);
        if (dateRangedEntity && dateRangedEntity.gueltigkeit) {
            restObj.gueltigAb = DateUtil.momentToLocalDate(dateRangedEntity.gueltigkeit.gueltigAb);
            restObj.gueltigBis = DateUtil.momentToLocalDate(dateRangedEntity.gueltigkeit.gueltigBis);
        }
    }

    private parseDateRangeEntity(parsedObject: TSAbstractDateRangedEntity, receivedAppProperty: any): void {
        this.parseAbstractMutableEntity(parsedObject, receivedAppProperty);
        const ab = DateUtil.localDateToMoment(receivedAppProperty.gueltigAb);
        const bis =
            DateUtil.localDateToMoment(receivedAppProperty.gueltigBis);
        parsedObject.gueltigkeit = new TSDateRange(ab, bis);
    }

    private abstractPensumEntityToRestObject(restObj: any, pensumEntity: TSAbstractPensumEntity): void {
        this.abstractDateRangeEntityToRestObject(restObj, pensumEntity);
        restObj.pensum = pensumEntity.pensum;
    }

    private parseAbstractPensumEntity(
        betreuungspensumTS: TSAbstractPensumEntity,
        betreuungspensumFromServer: any,
    ): void {
        this.parseDateRangeEntity(betreuungspensumTS, betreuungspensumFromServer);
        betreuungspensumTS.pensum = betreuungspensumFromServer.pensum;
    }

    private abstractAntragEntityToRestObject(restObj: any, antragEntity: TSAbstractAntragEntity): void {
        this.abstractMutableEntityToRestObject(restObj, antragEntity);
        restObj.dossier = this.dossierToRestObject({}, antragEntity.dossier);
        restObj.gesuchsperiode = this.gesuchsperiodeToRestObject({}, antragEntity.gesuchsperiode);
        restObj.eingangsdatum = DateUtil.momentToLocalDate(antragEntity.eingangsdatum);
        restObj.regelnGueltigAb = DateUtil.momentToLocalDate(antragEntity.regelnGueltigAb);
        restObj.freigabeDatum = DateUtil.momentToLocalDate(antragEntity.freigabeDatum);
        restObj.status = antragEntity.status;
        restObj.typ = antragEntity.typ;
        restObj.eingangsart = antragEntity.eingangsart;
    }

    private parseAbstractAntragEntity(antragTS: TSAbstractAntragEntity, antragFromServer: any): void {
        this.parseAbstractMutableEntity(antragTS, antragFromServer);
        antragTS.dossier = this.parseDossier(new TSDossier(), antragFromServer.dossier);
        antragTS.gesuchsperiode = this.parseGesuchsperiode(new TSGesuchsperiode(), antragFromServer.gesuchsperiode);
        antragTS.eingangsdatum = DateUtil.localDateToMoment(antragFromServer.eingangsdatum);
        antragTS.regelnGueltigAb = DateUtil.localDateToMoment(antragFromServer.regelnGueltigAb);
        antragTS.freigabeDatum = DateUtil.localDateToMoment(antragFromServer.freigabeDatum);
        antragTS.status = antragFromServer.status;
        antragTS.typ = antragFromServer.typ;
        antragTS.eingangsart = antragFromServer.eingangsart;
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

    public parseAdresse(adresseTS: TSAdresse, receivedAdresse: any): TSAdresse {
        if (receivedAdresse) {
            this.parseDateRangeEntity(adresseTS, receivedAdresse);
            adresseTS.strasse = receivedAdresse.strasse;
            adresseTS.hausnummer = receivedAdresse.hausnummer;
            adresseTS.zusatzzeile = receivedAdresse.zusatzzeile;
            adresseTS.plz = receivedAdresse.plz;
            adresseTS.ort = receivedAdresse.ort;
            adresseTS.land = (this.landCodeToTSLand(receivedAdresse.land)) ?
                this.landCodeToTSLand(receivedAdresse.land).code :
                undefined;
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
     */
    public landCodeToTSLand(landCode: string): TSLand {
        if (landCode) {
            const translationKey = this.landCodeToTSLandCode(landCode);
            return new TSLand(landCode, this.ebeguUtil.translateString(translationKey));
        }
        return undefined;
    }

    /**
     * Fügt das 'Land_' dem eingegebenen Landcode hinzu.
     */
    public landCodeToTSLandCode(landCode: string): string {
        return landCode && landCode.lastIndexOf('Land_', 0) !== 0 ? 'Land_' + landCode : undefined;
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
            restGesuchsteller.korrespondenzSprache = gesuchsteller.korrespondenzSprache;
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
            gesuchstellerTS.korrespondenzSprache = gesuchstellerFromServer.korrespondenzSprache;
            return gesuchstellerTS;
        }
        return undefined;
    }

    public parseErwerbspensumContainer(erwerbspensumContainer: TSErwerbspensumContainer,
                                       ewpContFromServer: any,
    ): TSErwerbspensumContainer {
        if (ewpContFromServer) {
            this.parseAbstractMutableEntity(erwerbspensumContainer, ewpContFromServer);
            erwerbspensumContainer.erwerbspensumGS =
                this.parseErwerbspensum(erwerbspensumContainer.erwerbspensumGS || new TSErwerbspensum(),
                    ewpContFromServer.erwerbspensumGS);
            erwerbspensumContainer.erwerbspensumJA =
                this.parseErwerbspensum(erwerbspensumContainer.erwerbspensumJA || new TSErwerbspensum(),
                    ewpContFromServer.erwerbspensumJA);
            return erwerbspensumContainer;
        }
        return undefined;
    }

    public erwerbspensumContainerToRestObject(restEwpContainer: any,
                                              erwerbspensumContainer: TSErwerbspensumContainer,
    ): any {
        if (erwerbspensumContainer) {
            this.abstractMutableEntityToRestObject(restEwpContainer, erwerbspensumContainer);
            restEwpContainer.erwerbspensumGS =
                this.erwerbspensumToRestObject({}, erwerbspensumContainer.erwerbspensumGS);
            restEwpContainer.erwerbspensumJA =
                this.erwerbspensumToRestObject({}, erwerbspensumContainer.erwerbspensumJA);
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
        }
        return undefined;
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

    public familiensituationToRestObject(restFamiliensituation: any,
                                         familiensituation: TSFamiliensituation,
    ): TSFamiliensituation {
        if (familiensituation) {
            this.abstractMutableEntityToRestObject(restFamiliensituation, familiensituation);
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

    public einkommensverschlechterungInfoContainerToRestObject(
        restEinkommensverschlechterungInfoContainer: any,
        einkommensverschlechterungInfoContainer: TSEinkommensverschlechterungInfoContainer,
    ): TSEinkommensverschlechterungInfoContainer {
        if (einkommensverschlechterungInfoContainer) {
            this.abstractMutableEntityToRestObject(restEinkommensverschlechterungInfoContainer,
                einkommensverschlechterungInfoContainer);
            if (einkommensverschlechterungInfoContainer.einkommensverschlechterungInfoGS) {
                restEinkommensverschlechterungInfoContainer.einkommensverschlechterungInfoGS =
                    this.einkommensverschlechterungInfoToRestObject({},
                        einkommensverschlechterungInfoContainer.einkommensverschlechterungInfoGS);
            }
            if (einkommensverschlechterungInfoContainer.einkommensverschlechterungInfoJA) {
                restEinkommensverschlechterungInfoContainer.einkommensverschlechterungInfoJA =
                    this.einkommensverschlechterungInfoToRestObject({},
                        einkommensverschlechterungInfoContainer.einkommensverschlechterungInfoJA);
            }
            return restEinkommensverschlechterungInfoContainer;
        }
        return undefined;
    }

    public einkommensverschlechterungInfoToRestObject(
        restEinkommensverschlechterungInfo: any,
        einkommensverschlechterungInfo: TSEinkommensverschlechterungInfo,
    ): TSEinkommensverschlechterungInfo {
        if (einkommensverschlechterungInfo) {
            this.abstractMutableEntityToRestObject(restEinkommensverschlechterungInfo, einkommensverschlechterungInfo);
            restEinkommensverschlechterungInfo.einkommensverschlechterung =
                einkommensverschlechterungInfo.einkommensverschlechterung;
            restEinkommensverschlechterungInfo.ekvFuerBasisJahrPlus1 =
                einkommensverschlechterungInfo.ekvFuerBasisJahrPlus1;
            restEinkommensverschlechterungInfo.ekvFuerBasisJahrPlus2 =
                einkommensverschlechterungInfo.ekvFuerBasisJahrPlus2;
            restEinkommensverschlechterungInfo.grundFuerBasisJahrPlus1 =
                einkommensverschlechterungInfo.grundFuerBasisJahrPlus1;
            restEinkommensverschlechterungInfo.grundFuerBasisJahrPlus2 =
                einkommensverschlechterungInfo.grundFuerBasisJahrPlus2;
            restEinkommensverschlechterungInfo.stichtagFuerBasisJahrPlus1 =
                DateUtil.momentToLocalDate(einkommensverschlechterungInfo.stichtagFuerBasisJahrPlus1);
            restEinkommensverschlechterungInfo.stichtagFuerBasisJahrPlus2 =
                DateUtil.momentToLocalDate(einkommensverschlechterungInfo.stichtagFuerBasisJahrPlus2);
            restEinkommensverschlechterungInfo.gemeinsameSteuererklaerung_BjP1 =
                einkommensverschlechterungInfo.gemeinsameSteuererklaerung_BjP1;
            restEinkommensverschlechterungInfo.gemeinsameSteuererklaerung_BjP2 =
                einkommensverschlechterungInfo.gemeinsameSteuererklaerung_BjP2;
            restEinkommensverschlechterungInfo.ekvBasisJahrPlus1Annulliert =
                einkommensverschlechterungInfo.ekvBasisJahrPlus1Annulliert;
            restEinkommensverschlechterungInfo.ekvBasisJahrPlus2Annulliert =
                einkommensverschlechterungInfo.ekvBasisJahrPlus2Annulliert;
            return restEinkommensverschlechterungInfo;
        }
        return undefined;
    }

    public parseFamiliensituation(familiensituation: TSFamiliensituation,
                                  familiensituationFromServer: any,
    ): TSFamiliensituation {
        if (familiensituationFromServer) {
            this.parseAbstractMutableEntity(familiensituation, familiensituationFromServer);
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

    public parseFamiliensituationContainer(containerTS: TSFamiliensituationContainer,
                                           containerFromServer: any,
    ): TSFamiliensituationContainer {
        if (containerFromServer) {
            this.parseAbstractMutableEntity(containerTS, containerFromServer);

            containerTS.familiensituationGS = this.parseFamiliensituation(containerTS.familiensituationGS
                || new TSFamiliensituation(), containerFromServer.familiensituationGS);
            containerTS.familiensituationJA = this.parseFamiliensituation(containerTS.familiensituationJA
                || new TSFamiliensituation(), containerFromServer.familiensituationJA);
            containerTS.familiensituationErstgesuch =
                this.parseFamiliensituation(containerTS.familiensituationErstgesuch
                    || new TSFamiliensituation(), containerFromServer.familiensituationErstgesuch);
            return containerTS;
        }
        return undefined;
    }

    public familiensituationContainerToRestObject(
        restFamiliensituationContainer: any,
        familiensituationContainer: TSFamiliensituationContainer,
    ): TSFamiliensituationContainer {
        if (familiensituationContainer) {
            this.abstractMutableEntityToRestObject(restFamiliensituationContainer, familiensituationContainer);

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

    public parseEinkommensverschlechterungInfo(
        einkommensverschlechterungInfo: TSEinkommensverschlechterungInfo,
        einkommensverschlechterungInfoFromServer: any,
    ): TSEinkommensverschlechterungInfo {
        if (einkommensverschlechterungInfoFromServer) {
            this.parseAbstractMutableEntity(einkommensverschlechterungInfo, einkommensverschlechterungInfoFromServer);
            einkommensverschlechterungInfo.einkommensverschlechterung =
                einkommensverschlechterungInfoFromServer.einkommensverschlechterung;
            einkommensverschlechterungInfo.ekvFuerBasisJahrPlus1 =
                einkommensverschlechterungInfoFromServer.ekvFuerBasisJahrPlus1;
            einkommensverschlechterungInfo.ekvFuerBasisJahrPlus2 =
                einkommensverschlechterungInfoFromServer.ekvFuerBasisJahrPlus2;
            einkommensverschlechterungInfo.grundFuerBasisJahrPlus1 =
                einkommensverschlechterungInfoFromServer.grundFuerBasisJahrPlus1;
            einkommensverschlechterungInfo.grundFuerBasisJahrPlus2 =
                einkommensverschlechterungInfoFromServer.grundFuerBasisJahrPlus2;
            einkommensverschlechterungInfo.stichtagFuerBasisJahrPlus1 =
                DateUtil.localDateToMoment(einkommensverschlechterungInfoFromServer.stichtagFuerBasisJahrPlus1);
            einkommensverschlechterungInfo.stichtagFuerBasisJahrPlus2 =
                DateUtil.localDateToMoment(einkommensverschlechterungInfoFromServer.stichtagFuerBasisJahrPlus2);
            einkommensverschlechterungInfo.gemeinsameSteuererklaerung_BjP1 =
                einkommensverschlechterungInfoFromServer.gemeinsameSteuererklaerung_BjP1;
            einkommensverschlechterungInfo.gemeinsameSteuererklaerung_BjP2 =
                einkommensverschlechterungInfoFromServer.gemeinsameSteuererklaerung_BjP2;
            einkommensverschlechterungInfo.ekvBasisJahrPlus1Annulliert =
                einkommensverschlechterungInfoFromServer.ekvBasisJahrPlus1Annulliert;
            einkommensverschlechterungInfo.ekvBasisJahrPlus2Annulliert =
                einkommensverschlechterungInfoFromServer.ekvBasisJahrPlus2Annulliert;
            return einkommensverschlechterungInfo;
        }
        return undefined;
    }

    public parseEinkommensverschlechterungInfoContainer(
        containerTS: TSEinkommensverschlechterungInfoContainer,
        containerFromServer: any,
    ): TSEinkommensverschlechterungInfoContainer {
        if (containerFromServer) {
            this.parseAbstractMutableEntity(containerTS, containerFromServer);

            containerTS.einkommensverschlechterungInfoGS =
                this.parseEinkommensverschlechterungInfo(containerTS.einkommensverschlechterungInfoGS
                    || new TSEinkommensverschlechterungInfo(), containerFromServer.einkommensverschlechterungInfoGS);
            containerTS.einkommensverschlechterungInfoJA =
                this.parseEinkommensverschlechterungInfo(containerTS.einkommensverschlechterungInfoJA
                    || new TSEinkommensverschlechterungInfo(), containerFromServer.einkommensverschlechterungInfoJA);
            return containerTS;
        }
        return undefined;
    }

    public fallToRestObject(restFall: any, fall: TSFall): TSFall {
        if (fall) {
            this.abstractMutableEntityToRestObject(restFall, fall);
            restFall.fallNummer = fall.fallNummer;
            restFall.besitzer = this.userToRestObject({}, fall.besitzer);
            return restFall;
        }
        return undefined;

    }

    public parseFall(fallTS: TSFall, fallFromServer: any): TSFall {
        if (fallFromServer) {
            this.parseAbstractMutableEntity(fallTS, fallFromServer);
            fallTS.fallNummer = fallFromServer.fallNummer;
            fallTS.nextNumberKind = fallFromServer.nextNumberKind;
            fallTS.besitzer = this.parseUser(new TSBenutzer(), fallFromServer.besitzer);
            return fallTS;
        }
        return undefined;
    }

    private gemeindeListToRestObject(gemeindeListTS: Array<TSGemeinde>): Array<any> {
        return gemeindeListTS
            ? gemeindeListTS.map(item => this.gemeindeToRestObject({}, item))
            : [];
    }

    public gemeindeToRestObject(restGemeinde: any, gemeinde: TSGemeinde): TSGemeinde {
        if (gemeinde) {
            this.abstractEntityToRestObject(restGemeinde, gemeinde);
            restGemeinde.name = gemeinde.name;
            restGemeinde.status = gemeinde.status;
            restGemeinde.gemeindeNummer = gemeinde.gemeindeNummer;
            restGemeinde.bfsNummer = gemeinde.bfsNummer;
            restGemeinde.betreuungsgutscheineStartdatum = DateUtil
                .momentToLocalDate(gemeinde.betreuungsgutscheineStartdatum);
            return restGemeinde;
        }
        return undefined;
    }

    public parseGemeindeList(data: any): TSGemeinde[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseGemeinde(new TSGemeinde(), item))
            : [this.parseGemeinde(new TSGemeinde(), data)];
    }

    public parseGemeinde(gemeindeTS: TSGemeinde, gemeindeFromServer: any): TSGemeinde {
        if (gemeindeFromServer) {
            this.parseAbstractEntity(gemeindeTS, gemeindeFromServer);
            gemeindeTS.name = gemeindeFromServer.name;
            gemeindeTS.status = gemeindeFromServer.status;
            gemeindeTS.gemeindeNummer = gemeindeFromServer.gemeindeNummer;
            gemeindeTS.bfsNummer = gemeindeFromServer.bfsNummer;
            gemeindeTS.betreuungsgutscheineStartdatum = DateUtil
                .localDateToMoment(gemeindeFromServer.betreuungsgutscheineStartdatum);
            return gemeindeTS;
        }
        return undefined;
    }

    public gemeindeStammdatenToRestObject(restStammdaten: any, stammdaten: TSGemeindeStammdaten): TSGemeindeStammdaten {
        if (stammdaten) {
            this.abstractEntityToRestObject(restStammdaten, stammdaten);

            restStammdaten.administratoren = stammdaten.administratoren;
            restStammdaten.sachbearbeiter = stammdaten.sachbearbeiter;
            restStammdaten.defaultBenutzerBG = this.userToRestObject({}, stammdaten.defaultBenutzerBG);
            restStammdaten.defaultBenutzerTS = this.userToRestObject({}, stammdaten.defaultBenutzerTS);
            restStammdaten.gemeinde = this.gemeindeToRestObject({}, stammdaten.gemeinde);
            restStammdaten.adresse = this.adresseToRestObject({}, stammdaten.adresse);
            restStammdaten.beschwerdeAdresse = this.adresseToRestObject({}, stammdaten.beschwerdeAdresse);
            restStammdaten.keineBeschwerdeAdresse = stammdaten.keineBeschwerdeAdresse;
            restStammdaten.mail = stammdaten.mail;
            restStammdaten.telefon = stammdaten.telefon;
            restStammdaten.webseite = stammdaten.webseite;
            restStammdaten.korrespondenzspracheDe = stammdaten.korrespondenzspracheDe;
            restStammdaten.korrespondenzspracheFr = stammdaten.korrespondenzspracheFr;
            restStammdaten.konfigurationsListe = stammdaten.konfigurationsListe;

            return restStammdaten;
        }
        return undefined;
    }

    public parseGemeindeStammdaten(stammdatenTS: TSGemeindeStammdaten,
                                   stammdatenFromServer: any): TSGemeindeStammdaten {
        if (stammdatenFromServer) {
            this.parseAbstractEntity(stammdatenTS, stammdatenFromServer);

            stammdatenTS.administratoren = stammdatenFromServer.administratoren;
            stammdatenTS.sachbearbeiter = stammdatenFromServer.sachbearbeiter;
            stammdatenTS.defaultBenutzerBG = this.parseUser(new TSBenutzer(), stammdatenFromServer.defaultBenutzerBG);
            stammdatenTS.defaultBenutzerTS = this.parseUser(new TSBenutzer(), stammdatenFromServer.defaultBenutzerTS);
            stammdatenTS.gemeinde = this.parseGemeinde(new TSGemeinde(), stammdatenFromServer.gemeinde);
            stammdatenTS.adresse = this.parseAdresse(new TSAdresse(), stammdatenFromServer.adresse);
            stammdatenTS.beschwerdeAdresse = this.parseAdresse(new TSAdresse(), stammdatenFromServer.beschwerdeAdresse);
            stammdatenTS.keineBeschwerdeAdresse = stammdatenFromServer.keineBeschwerdeAdresse;
            stammdatenTS.mail = stammdatenFromServer.mail;
            stammdatenTS.telefon = stammdatenFromServer.telefon;
            stammdatenTS.webseite = stammdatenFromServer.webseite;
            stammdatenTS.korrespondenzspracheDe = stammdatenFromServer.korrespondenzspracheDe;
            stammdatenTS.korrespondenzspracheFr = stammdatenFromServer.korrespondenzspracheFr;
            stammdatenTS.logoUrl = stammdatenFromServer.logoUrl;
            stammdatenTS.benutzerListeBG = stammdatenFromServer.benutzerListeBG;
            stammdatenTS.benutzerListeTS = stammdatenFromServer.benutzerListeTS;
            stammdatenTS.konfigurationsListe = stammdatenFromServer.konfigurationsListe;

            return stammdatenTS;
        }
        return undefined;
    }

    public dossierToRestObject(restDossier: any, dossier: TSDossier): TSDossier {
        if (dossier) {
            this.abstractMutableEntityToRestObject(restDossier, dossier);
            restDossier.fall = this.fallToRestObject({}, dossier.fall);
            restDossier.gemeinde = this.gemeindeToRestObject({}, dossier.gemeinde);
            restDossier.verantwortlicherBG = this.userToRestObject({}, dossier.verantwortlicherBG);
            restDossier.verantwortlicherTS = this.userToRestObject({}, dossier.verantwortlicherTS);
            return restDossier;
        }
        return undefined;
    }

    public parseDossierList(data: any): TSDossier[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseDossier(new TSDossier(), item))
            : [this.parseDossier(new TSDossier(), data)];
    }

    public parseDossier(dossierTS: TSDossier, dossierFromServer: any): TSDossier {
        if (dossierFromServer) {
            this.parseAbstractMutableEntity(dossierTS, dossierFromServer);
            dossierTS.fall = this.parseFall(new TSFall(), dossierFromServer.fall);
            dossierTS.gemeinde = this.parseGemeinde(new TSGemeinde(), dossierFromServer.gemeinde);
            dossierTS.verantwortlicherBG = this.parseUser(new TSBenutzer(), dossierFromServer.verantwortlicherBG);
            dossierTS.verantwortlicherTS = this.parseUser(new TSBenutzer(), dossierFromServer.verantwortlicherTS);
            return dossierTS;
        }
        return undefined;
    }

    public gesuchToRestObject(restGesuch: any, gesuch: TSGesuch): TSGesuch {
        this.abstractAntragEntityToRestObject(restGesuch, gesuch);
        restGesuch.einkommensverschlechterungInfoContainer =
            this.einkommensverschlechterungInfoContainerToRestObject({},
                gesuch.einkommensverschlechterungInfoContainer);
        restGesuch.gesuchsteller1 = this.gesuchstellerContainerToRestObject({}, gesuch.gesuchsteller1);
        restGesuch.gesuchsteller2 = this.gesuchstellerContainerToRestObject({}, gesuch.gesuchsteller2);
        restGesuch.familiensituationContainer =
            this.familiensituationContainerToRestObject({}, gesuch.familiensituationContainer);
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
                new TSEinkommensverschlechterungInfoContainer(),
                gesuchFromServer.einkommensverschlechterungInfoContainer);
            gesuchTS.gesuchsteller1 =
                this.parseGesuchstellerContainer(new TSGesuchstellerContainer(), gesuchFromServer.gesuchsteller1);
            gesuchTS.gesuchsteller2 =
                this.parseGesuchstellerContainer(new TSGesuchstellerContainer(), gesuchFromServer.gesuchsteller2);
            gesuchTS.familiensituationContainer =
                this.parseFamiliensituationContainer(new TSFamiliensituationContainer(),
                    gesuchFromServer.familiensituationContainer);
            gesuchTS.kindContainers = this.parseKindContainerList(gesuchFromServer.kindContainers);
            gesuchTS.bemerkungen = gesuchFromServer.bemerkungen;
            gesuchTS.bemerkungenSTV = gesuchFromServer.bemerkungenSTV;
            gesuchTS.bemerkungenPruefungSTV = gesuchFromServer.bemerkungenPruefungSTV;
            gesuchTS.laufnummer = gesuchFromServer.laufnummer;
            gesuchTS.gesuchBetreuungenStatus = gesuchFromServer.gesuchBetreuungenStatus;
            gesuchTS.geprueftSTV = gesuchFromServer.geprueftSTV;
            gesuchTS.hasFSDokument = gesuchFromServer.hasFSDokument;
            gesuchTS.gesperrtWegenBeschwerde = gesuchFromServer.gesperrtWegenBeschwerde;
            gesuchTS.datumGewarntNichtFreigegeben =
                DateUtil.localDateToMoment(gesuchFromServer.datumGewarntNichtFreigegeben);
            gesuchTS.datumGewarntFehlendeQuittung =
                DateUtil.localDateToMoment(gesuchFromServer.datumGewarntFehlendeQuittung);
            gesuchTS.timestampVerfuegt = DateUtil.localDateTimeToMoment(gesuchFromServer.timestampVerfuegt);
            gesuchTS.gueltig = gesuchFromServer.gueltig;
            gesuchTS.dokumenteHochgeladen = gesuchFromServer.dokumenteHochgeladen;
            gesuchTS.finSitStatus = gesuchFromServer.finSitStatus;
            return gesuchTS;
        }
        return undefined;
    }

    public fachstelleToRestObject(restFachstelle: any, fachstelle: TSFachstelle): any {
        this.abstractMutableEntityToRestObject(restFachstelle, fachstelle);
        restFachstelle.name = fachstelle.name;
        restFachstelle.beschreibung = fachstelle.beschreibung;
        restFachstelle.behinderungsbestaetigung = fachstelle.behinderungsbestaetigung;
        return restFachstelle;
    }

    public parseFachstellen(data: any): TSFachstelle[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseFachstelle(new TSFachstelle(), item))
            : [this.parseFachstelle(new TSFachstelle(), data)];
    }

    public parseFachstelle(parsedFachstelle: TSFachstelle, receivedFachstelle: any): TSFachstelle {
        this.parseAbstractMutableEntity(parsedFachstelle, receivedFachstelle);
        parsedFachstelle.name = receivedFachstelle.name;
        parsedFachstelle.beschreibung = receivedFachstelle.beschreibung;
        parsedFachstelle.behinderungsbestaetigung = receivedFachstelle.behinderungsbestaetigung;
        return parsedFachstelle;
    }

    public mandantToRestObject(restMandant: any, mandant: TSMandant): any {
        if (mandant) {
            this.abstractMutableEntityToRestObject(restMandant, mandant);
            restMandant.name = mandant.name;
            return restMandant;
        }
        return undefined;
    }

    public parseMandant(mandantTS: TSMandant, mandantFromServer: any): TSMandant {
        if (mandantFromServer) {
            this.parseAbstractMutableEntity(mandantTS, mandantFromServer);
            mandantTS.name = mandantFromServer.name;
            return mandantTS;
        }
        return undefined;
    }

    public traegerschaftToRestObject(restTragerschaft: any, traegerschaft: TSTraegerschaft): any {
        if (traegerschaft) {
            this.abstractMutableEntityToRestObject(restTragerschaft, traegerschaft);
            restTragerschaft.name = traegerschaft.name;
            restTragerschaft.active = traegerschaft.active;
            restTragerschaft.mail = traegerschaft.mail;
            return restTragerschaft;
        }
        return undefined;
    }

    public parseTraegerschaften(data: Array<any>): TSTraegerschaft[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseTraegerschaft(new TSTraegerschaft(), item))
            : [this.parseTraegerschaft(new TSTraegerschaft(), data)];
    }

    public parseTraegerschaft(traegerschaftTS: TSTraegerschaft, traegerschaftFromServer: any): TSTraegerschaft {
        if (traegerschaftFromServer) {
            this.parseAbstractMutableEntity(traegerschaftTS, traegerschaftFromServer);
            traegerschaftTS.name = traegerschaftFromServer.name;
            traegerschaftTS.active = traegerschaftFromServer.active;
            traegerschaftTS.mail = traegerschaftFromServer.mail;
            return traegerschaftTS;
        }
        return undefined;
    }

    public institutionToRestObject(restInstitution: any, institution: TSInstitution): any {
        if (institution) {
            this.abstractMutableEntityToRestObject(restInstitution, institution);
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
            this.parseAbstractMutableEntity(institutionTS, institutionFromServer);
            institutionTS.name = institutionFromServer.name;
            institutionTS.mandant = this.parseMandant(new TSMandant(), institutionFromServer.mandant);
            institutionTS.traegerschaft =
                this.parseTraegerschaft(new TSTraegerschaft(), institutionFromServer.traegerschaft);
            institutionTS.mail = institutionFromServer.mail;
            return institutionTS;
        }
        return undefined;
    }

    public parseInstitutionen(data: Array<any>): TSInstitution[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseInstitution(new TSInstitution(), item))
            : [this.parseInstitution(new TSInstitution(), data)];
    }

    public institutionStammdatenToRestObject(restInstitutionStammdaten: any,
                                             institutionStammdaten: TSInstitutionStammdaten,
    ): any {
        if (institutionStammdaten) {
            this.abstractDateRangeEntityToRestObject(restInstitutionStammdaten, institutionStammdaten);
            restInstitutionStammdaten.iban = institutionStammdaten.iban;
            restInstitutionStammdaten.oeffnungsstunden = institutionStammdaten.oeffnungsstunden;
            restInstitutionStammdaten.oeffnungstage = institutionStammdaten.oeffnungstage;
            restInstitutionStammdaten.betreuungsangebotTyp = institutionStammdaten.betreuungsangebotTyp;
            restInstitutionStammdaten.institution = this.institutionToRestObject({}, institutionStammdaten.institution);
            restInstitutionStammdaten.adresse = this.adresseToRestObject({}, institutionStammdaten.adresse);
            restInstitutionStammdaten.kontoinhaber = institutionStammdaten.kontoinhaber;
            restInstitutionStammdaten.adresseKontoinhaber =
                this.adresseToRestObject({}, institutionStammdaten.adresseKontoinhaber);
            restInstitutionStammdaten.institutionStammdatenTagesschule =
                this.institutionStammdatenTagesschuleToRestObject({},
                    institutionStammdaten.institutionStammdatenTagesschule);
            restInstitutionStammdaten.institutionStammdatenFerieninsel =
                this.institutionStammdatenFerieninselToRestObject({},
                    institutionStammdaten.institutionStammdatenFerieninsel);
            return restInstitutionStammdaten;
        }
        return undefined;
    }

    public parseInstitutionStammdaten(institutionStammdatenTS: TSInstitutionStammdaten,
                                      institutionStammdatenFromServer: any,
    ): TSInstitutionStammdaten {
        if (institutionStammdatenFromServer) {
            this.parseDateRangeEntity(institutionStammdatenTS, institutionStammdatenFromServer);
            institutionStammdatenTS.iban = institutionStammdatenFromServer.iban;
            institutionStammdatenTS.oeffnungsstunden = institutionStammdatenFromServer.oeffnungsstunden;
            institutionStammdatenTS.oeffnungstage = institutionStammdatenFromServer.oeffnungstage;
            institutionStammdatenTS.betreuungsangebotTyp = institutionStammdatenFromServer.betreuungsangebotTyp;
            institutionStammdatenTS.institution =
                this.parseInstitution(new TSInstitution(), institutionStammdatenFromServer.institution);
            institutionStammdatenTS.adresse =
                this.parseAdresse(new TSAdresse(), institutionStammdatenFromServer.adresse);
            institutionStammdatenTS.kontoinhaber = institutionStammdatenFromServer.kontoinhaber;
            institutionStammdatenTS.adresseKontoinhaber =
                this.parseAdresse(new TSAdresse(), institutionStammdatenFromServer.adresseKontoinhaber);
            institutionStammdatenTS.institutionStammdatenTagesschule =
                this.parseInstitutionStammdatenTagesschule(new TSInstitutionStammdatenTagesschule(),
                    institutionStammdatenFromServer.institutionStammdatenTagesschule);
            institutionStammdatenTS.institutionStammdatenFerieninsel =
                this.parseInstitutionStammdatenFerieninsel(new TSInstitutionStammdatenFerieninsel(),
                    institutionStammdatenFromServer.institutionStammdatenFerieninsel);
            return institutionStammdatenTS;
        }
        return undefined;
    }

    public parseInstitutionStammdatenArray(data: Array<any>): TSInstitutionStammdaten[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseInstitutionStammdaten(new TSInstitutionStammdaten(), item))
            : [this.parseInstitutionStammdaten(new TSInstitutionStammdaten(), data)];
    }

    public institutionStammdatenFerieninselToRestObject(
        restInstitutionStammdatenFerieninsel: any,
        institutionStammdatenFerieninsel: TSInstitutionStammdatenFerieninsel,
    ): any {
        if (institutionStammdatenFerieninsel) {
            this.abstractMutableEntityToRestObject(restInstitutionStammdatenFerieninsel,
                institutionStammdatenFerieninsel);
            restInstitutionStammdatenFerieninsel.ausweichstandortFruehlingsferien =
                institutionStammdatenFerieninsel.ausweichstandortFruehlingsferien;
            restInstitutionStammdatenFerieninsel.ausweichstandortHerbstferien =
                institutionStammdatenFerieninsel.ausweichstandortHerbstferien;
            restInstitutionStammdatenFerieninsel.ausweichstandortSommerferien =
                institutionStammdatenFerieninsel.ausweichstandortSommerferien;
            restInstitutionStammdatenFerieninsel.ausweichstandortSportferien =
                institutionStammdatenFerieninsel.ausweichstandortSportferien;
            return restInstitutionStammdatenFerieninsel;
        }
        return undefined;
    }

    public parseInstitutionStammdatenFerieninsel(
        institutionStammdatenFerieninselTS: TSInstitutionStammdatenFerieninsel,
        institutionStammdatenFerieninselFromServer: any,
    ): TSInstitutionStammdatenFerieninsel {
        if (institutionStammdatenFerieninselFromServer) {
            this.parseAbstractMutableEntity(institutionStammdatenFerieninselTS,
                institutionStammdatenFerieninselFromServer);
            institutionStammdatenFerieninselTS.ausweichstandortFruehlingsferien =
                institutionStammdatenFerieninselFromServer.ausweichstandortFruehlingsferien;
            institutionStammdatenFerieninselTS.ausweichstandortHerbstferien =
                institutionStammdatenFerieninselFromServer.ausweichstandortHerbstferien;
            institutionStammdatenFerieninselTS.ausweichstandortSommerferien =
                institutionStammdatenFerieninselFromServer.ausweichstandortSommerferien;
            institutionStammdatenFerieninselTS.ausweichstandortSportferien =
                institutionStammdatenFerieninselFromServer.ausweichstandortSportferien;
            return institutionStammdatenFerieninselTS;
        }
        return undefined;
    }

    public institutionStammdatenTagesschuleToRestObject(
        restInstitutionStammdatenTagesschule: any,
        institutionStammdatenTagesschule: TSInstitutionStammdatenTagesschule,
    ): any {
        if (institutionStammdatenTagesschule) {
            this.abstractMutableEntityToRestObject(restInstitutionStammdatenTagesschule,
                institutionStammdatenTagesschule);
            restInstitutionStammdatenTagesschule.moduleTagesschule =
                this.moduleTagesschuleArrayToRestObject(institutionStammdatenTagesschule.moduleTagesschule);
            return restInstitutionStammdatenTagesschule;
        }
        return undefined;
    }

    public parseInstitutionStammdatenTagesschule(
        institutionStammdatenTagesschuleTS: TSInstitutionStammdatenTagesschule,
        institutionStammdatenTagesschuleFromServer: any,
    ): TSInstitutionStammdatenTagesschule {
        if (institutionStammdatenTagesschuleFromServer) {
            this.parseAbstractMutableEntity(institutionStammdatenTagesschuleTS,
                institutionStammdatenTagesschuleFromServer);
            institutionStammdatenTagesschuleTS.moduleTagesschule =
                this.parseModuleTagesschuleArray(institutionStammdatenTagesschuleFromServer.moduleTagesschule);
            return institutionStammdatenTagesschuleTS;
        }
        return undefined;
    }

    public finanzielleSituationContainerToRestObject(
        restFinanzielleSituationContainer: any,
        finanzielleSituationContainer: TSFinanzielleSituationContainer,
    ): TSFinanzielleSituationContainer {
        this.abstractMutableEntityToRestObject(restFinanzielleSituationContainer, finanzielleSituationContainer);
        restFinanzielleSituationContainer.jahr = finanzielleSituationContainer.jahr;
        if (finanzielleSituationContainer.finanzielleSituationGS) {
            restFinanzielleSituationContainer.finanzielleSituationGS =
                this.finanzielleSituationToRestObject({}, finanzielleSituationContainer.finanzielleSituationGS);
        }
        if (finanzielleSituationContainer.finanzielleSituationJA) {
            restFinanzielleSituationContainer.finanzielleSituationJA =
                this.finanzielleSituationToRestObject({}, finanzielleSituationContainer.finanzielleSituationJA);
        }
        return restFinanzielleSituationContainer;
    }

    public parseFinanzielleSituationContainer(containerTS: TSFinanzielleSituationContainer,
                                              containerFromServer: any,
    ): TSFinanzielleSituationContainer {
        if (containerFromServer) {
            this.parseAbstractMutableEntity(containerTS, containerFromServer);
            containerTS.jahr = containerFromServer.jahr;
            containerTS.finanzielleSituationGS =
                this.parseFinanzielleSituation(containerTS.finanzielleSituationGS || new TSFinanzielleSituation(),
                    containerFromServer.finanzielleSituationGS);
            containerTS.finanzielleSituationJA =
                this.parseFinanzielleSituation(containerTS.finanzielleSituationJA || new TSFinanzielleSituation(),
                    containerFromServer.finanzielleSituationJA);
            return containerTS;
        }
        return undefined;
    }

    public finanzielleSituationToRestObject(restFinanzielleSituation: any,
                                            finanzielleSituation: TSFinanzielleSituation,
    ): TSFinanzielleSituation {
        this.abstractfinanzielleSituationToRestObject(restFinanzielleSituation, finanzielleSituation);
        restFinanzielleSituation.nettolohn = finanzielleSituation.nettolohn;
        restFinanzielleSituation.geschaeftsgewinnBasisjahrMinus2 = finanzielleSituation.geschaeftsgewinnBasisjahrMinus2;
        restFinanzielleSituation.geschaeftsgewinnBasisjahrMinus1 = finanzielleSituation.geschaeftsgewinnBasisjahrMinus1;
        return restFinanzielleSituation;
    }

    private abstractfinanzielleSituationToRestObject(
        restAbstractFinanzielleSituation: any,
        abstractFinanzielleSituation: TSAbstractFinanzielleSituation,
    ): TSAbstractFinanzielleSituation {
        this.abstractMutableEntityToRestObject(restAbstractFinanzielleSituation, abstractFinanzielleSituation);
        restAbstractFinanzielleSituation.steuerveranlagungErhalten =
            abstractFinanzielleSituation.steuerveranlagungErhalten;
        restAbstractFinanzielleSituation.steuererklaerungAusgefuellt =
            abstractFinanzielleSituation.steuererklaerungAusgefuellt || false;
        restAbstractFinanzielleSituation.familienzulage = abstractFinanzielleSituation.familienzulage;
        restAbstractFinanzielleSituation.ersatzeinkommen = abstractFinanzielleSituation.ersatzeinkommen;
        restAbstractFinanzielleSituation.erhalteneAlimente = abstractFinanzielleSituation.erhalteneAlimente;
        restAbstractFinanzielleSituation.bruttovermoegen = abstractFinanzielleSituation.bruttovermoegen;
        restAbstractFinanzielleSituation.schulden = abstractFinanzielleSituation.schulden;
        restAbstractFinanzielleSituation.geschaeftsgewinnBasisjahr =
            abstractFinanzielleSituation.geschaeftsgewinnBasisjahr;
        restAbstractFinanzielleSituation.geleisteteAlimente = abstractFinanzielleSituation.geleisteteAlimente;
        return restAbstractFinanzielleSituation;
    }

    public parseAbstractFinanzielleSituation(
        abstractFinanzielleSituationTS: TSAbstractFinanzielleSituation,
        abstractFinanzielleSituationFromServer: any,
    ): TSAbstractFinanzielleSituation {
        if (abstractFinanzielleSituationFromServer) {
            this.parseAbstractMutableEntity(abstractFinanzielleSituationTS, abstractFinanzielleSituationFromServer);
            abstractFinanzielleSituationTS.steuerveranlagungErhalten =
                abstractFinanzielleSituationFromServer.steuerveranlagungErhalten;
            abstractFinanzielleSituationTS.steuererklaerungAusgefuellt =
                abstractFinanzielleSituationFromServer.steuererklaerungAusgefuellt;
            abstractFinanzielleSituationTS.familienzulage = abstractFinanzielleSituationFromServer.familienzulage;
            abstractFinanzielleSituationTS.ersatzeinkommen = abstractFinanzielleSituationFromServer.ersatzeinkommen;
            abstractFinanzielleSituationTS.erhalteneAlimente = abstractFinanzielleSituationFromServer.erhalteneAlimente;
            abstractFinanzielleSituationTS.bruttovermoegen = abstractFinanzielleSituationFromServer.bruttovermoegen;
            abstractFinanzielleSituationTS.schulden = abstractFinanzielleSituationFromServer.schulden;
            abstractFinanzielleSituationTS.geschaeftsgewinnBasisjahr =
                abstractFinanzielleSituationFromServer.geschaeftsgewinnBasisjahr;
            abstractFinanzielleSituationTS.geleisteteAlimente =
                abstractFinanzielleSituationFromServer.geleisteteAlimente;
            return abstractFinanzielleSituationTS;
        }
        return undefined;
    }

    public parseFinanzielleSituation(finanzielleSituationTS: TSFinanzielleSituation,
                                     finanzielleSituationFromServer: any,
    ): TSFinanzielleSituation {
        if (finanzielleSituationFromServer) {
            this.parseAbstractFinanzielleSituation(finanzielleSituationTS, finanzielleSituationFromServer);
            finanzielleSituationTS.nettolohn = finanzielleSituationFromServer.nettolohn;
            finanzielleSituationTS.geschaeftsgewinnBasisjahrMinus2 =
                finanzielleSituationFromServer.geschaeftsgewinnBasisjahrMinus2;
            finanzielleSituationTS.geschaeftsgewinnBasisjahrMinus1 =
                finanzielleSituationFromServer.geschaeftsgewinnBasisjahrMinus1;
            return finanzielleSituationTS;
        }
        return undefined;
    }

    public finanzielleSituationResultateToRestObject(
        restFinanzielleSituationResultate: any,
        finanzielleSituationResultateDTO: TSFinanzielleSituationResultateDTO,
    ): TSFinanzielleSituationResultateDTO {
        restFinanzielleSituationResultate.geschaeftsgewinnDurchschnittGesuchsteller1 =
            finanzielleSituationResultateDTO.geschaeftsgewinnDurchschnittGesuchsteller1;
        restFinanzielleSituationResultate.geschaeftsgewinnDurchschnittGesuchsteller2 =
            finanzielleSituationResultateDTO.geschaeftsgewinnDurchschnittGesuchsteller2;
        restFinanzielleSituationResultate.einkommenBeiderGesuchsteller =
            finanzielleSituationResultateDTO.einkommenBeiderGesuchsteller;
        restFinanzielleSituationResultate.nettovermoegenFuenfProzent =
            finanzielleSituationResultateDTO.nettovermoegenFuenfProzent;
        restFinanzielleSituationResultate.anrechenbaresEinkommen =
            finanzielleSituationResultateDTO.anrechenbaresEinkommen;
        restFinanzielleSituationResultate.abzuegeBeiderGesuchsteller =
            finanzielleSituationResultateDTO.abzuegeBeiderGesuchsteller;
        restFinanzielleSituationResultate.massgebendesEinkVorAbzFamGr =
            finanzielleSituationResultateDTO.massgebendesEinkVorAbzFamGr;
        return restFinanzielleSituationResultate;
    }

    public parseFinanzielleSituationResultate(
        finanzielleSituationResultateDTO: TSFinanzielleSituationResultateDTO,
        finanzielleSituationResultateFromServer: any,
    ): TSFinanzielleSituationResultateDTO {
        if (finanzielleSituationResultateFromServer) {
            finanzielleSituationResultateDTO.geschaeftsgewinnDurchschnittGesuchsteller1 =
                finanzielleSituationResultateFromServer.geschaeftsgewinnDurchschnittGesuchsteller1;
            finanzielleSituationResultateDTO.geschaeftsgewinnDurchschnittGesuchsteller2 =
                finanzielleSituationResultateFromServer.geschaeftsgewinnDurchschnittGesuchsteller2;
            finanzielleSituationResultateDTO.einkommenBeiderGesuchsteller =
                finanzielleSituationResultateFromServer.einkommenBeiderGesuchsteller;
            finanzielleSituationResultateDTO.nettovermoegenFuenfProzent =
                finanzielleSituationResultateFromServer.nettovermoegenFuenfProzent;
            finanzielleSituationResultateDTO.anrechenbaresEinkommen =
                finanzielleSituationResultateFromServer.anrechenbaresEinkommen;
            finanzielleSituationResultateDTO.abzuegeBeiderGesuchsteller =
                finanzielleSituationResultateFromServer.abzuegeBeiderGesuchsteller;
            finanzielleSituationResultateDTO.massgebendesEinkVorAbzFamGr =
                finanzielleSituationResultateFromServer.massgebendesEinkVorAbzFamGr;
            return finanzielleSituationResultateDTO;
        }
        return undefined;
    }

    public einkommensverschlechterungContainerToRestObject(
        restEinkommensverschlechterungContainer: any,
        einkommensverschlechterungContainer: TSEinkommensverschlechterungContainer,
    ): TSEinkommensverschlechterungContainer {
        this.abstractMutableEntityToRestObject(restEinkommensverschlechterungContainer,
            einkommensverschlechterungContainer);

        if (einkommensverschlechterungContainer.ekvGSBasisJahrPlus1) {
            restEinkommensverschlechterungContainer.ekvGSBasisJahrPlus1 =
                this.einkommensverschlechterungToRestObject({},
                    einkommensverschlechterungContainer.ekvGSBasisJahrPlus1);
        }
        if (einkommensverschlechterungContainer.ekvGSBasisJahrPlus2) {
            restEinkommensverschlechterungContainer.ekvGSBasisJahrPlus2 =
                this.einkommensverschlechterungToRestObject({},
                    einkommensverschlechterungContainer.ekvGSBasisJahrPlus2);
        }
        if (einkommensverschlechterungContainer.ekvJABasisJahrPlus1) {
            restEinkommensverschlechterungContainer.ekvJABasisJahrPlus1 =
                this.einkommensverschlechterungToRestObject({},
                    einkommensverschlechterungContainer.ekvJABasisJahrPlus1);
        }
        if (einkommensverschlechterungContainer.ekvJABasisJahrPlus2) {
            restEinkommensverschlechterungContainer.ekvJABasisJahrPlus2 =
                this.einkommensverschlechterungToRestObject({},
                    einkommensverschlechterungContainer.ekvJABasisJahrPlus2);
        }

        return restEinkommensverschlechterungContainer;
    }

    public einkommensverschlechterungToRestObject(
        restEinkommensverschlechterung: any,
        einkommensverschlechterung: TSEinkommensverschlechterung,
    ): TSEinkommensverschlechterung {
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
        restEinkommensverschlechterung.geschaeftsgewinnBasisjahrMinus1 =
            einkommensverschlechterung.geschaeftsgewinnBasisjahrMinus1;
        return restEinkommensverschlechterung;
    }

    public parseEinkommensverschlechterungContainer(
        containerTS: TSEinkommensverschlechterungContainer,
        containerFromServer: any,
    ): TSEinkommensverschlechterungContainer {
        if (containerFromServer) {
            this.parseAbstractMutableEntity(containerTS, containerFromServer);
            const empty = new TSEinkommensverschlechterung();
            containerTS.ekvGSBasisJahrPlus1 =
                this.parseEinkommensverschlechterung(containerTS.ekvGSBasisJahrPlus1 || empty,
                    containerFromServer.ekvGSBasisJahrPlus1);
            containerTS.ekvGSBasisJahrPlus2 =
                this.parseEinkommensverschlechterung(containerTS.ekvGSBasisJahrPlus2 || empty,
                    containerFromServer.ekvGSBasisJahrPlus2);
            containerTS.ekvJABasisJahrPlus1 =
                this.parseEinkommensverschlechterung(containerTS.ekvJABasisJahrPlus1 || empty,
                    containerFromServer.ekvJABasisJahrPlus1);
            containerTS.ekvJABasisJahrPlus2 =
                this.parseEinkommensverschlechterung(containerTS.ekvJABasisJahrPlus2 || empty,
                    containerFromServer.ekvJABasisJahrPlus2);

            return containerTS;
        }
        return undefined;
    }

    public parseEinkommensverschlechterung(
        einkommensverschlechterungTS: TSEinkommensverschlechterung,
        einkommensverschlechterungFromServer: any,
    ): TSEinkommensverschlechterung {
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
            einkommensverschlechterungTS.geschaeftsgewinnBasisjahrMinus1 =
                einkommensverschlechterungFromServer.geschaeftsgewinnBasisjahrMinus1;

            return einkommensverschlechterungTS;
        }
        return undefined;
    }

    public kindContainerToRestObject(restKindContainer: any, kindContainer: TSKindContainer): any {
        this.abstractMutableEntityToRestObject(restKindContainer, kindContainer);
        if (kindContainer.kindGS) {
            restKindContainer.kindGS = this.kindToRestObject({}, kindContainer.kindGS);
        }
        if (kindContainer.kindJA) {
            restKindContainer.kindJA = this.kindToRestObject({}, kindContainer.kindJA);
        }
        restKindContainer.betreuungen = this.betreuungListToRestObject(kindContainer.betreuungen);
        restKindContainer.kindNummer = kindContainer.kindNummer;
        restKindContainer.kindMutiert = kindContainer.kindMutiert;
        return restKindContainer;
    }

    private kindToRestObject(restKind: any, kind: TSKind): any {
        this.abstractPersonEntitytoRestObject(restKind, kind);
        restKind.kinderabzug = kind.kinderabzug;
        restKind.mutterspracheDeutsch = kind.mutterspracheDeutsch;
        restKind.einschulungTyp = kind.einschulungTyp;
        restKind.familienErgaenzendeBetreuung = kind.familienErgaenzendeBetreuung;
        if (kind.pensumFachstelle) {
            restKind.pensumFachstelle = this.pensumFachstelleToRestObject({}, kind.pensumFachstelle);
        }
        return restKind;
    }

    public parseKindDubletteList(data: Array<any>): TSKindDublette[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseKindDublette(new TSKindDublette(), item))
            : [this.parseKindDublette(new TSKindDublette(), data)];
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
        return data && Array.isArray(data)
            ? data.map(item => this.parseKindContainer(new TSKindContainer(), item))
            : [this.parseKindContainer(new TSKindContainer(), data)];
    }

    public parseKindContainer(kindContainerTS: TSKindContainer, kindContainerFromServer: any): TSKindContainer {
        if (kindContainerFromServer) {
            this.parseAbstractMutableEntity(kindContainerTS, kindContainerFromServer);
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
            kindTS.kinderabzug = kindFromServer.kinderabzug;
            kindTS.mutterspracheDeutsch = kindFromServer.mutterspracheDeutsch;
            kindTS.einschulungTyp = kindFromServer.einschulungTyp;
            kindTS.familienErgaenzendeBetreuung = kindFromServer.familienErgaenzendeBetreuung;
            if (kindFromServer.pensumFachstelle) {
                kindTS.pensumFachstelle =
                    this.parsePensumFachstelle(new TSPensumFachstelle(), kindFromServer.pensumFachstelle);
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

    private parsePensumFachstelle(pensumFachstelleTS: TSPensumFachstelle,
                                  pensumFachstelleFromServer: any,
    ): TSPensumFachstelle {
        if (pensumFachstelleFromServer) {
            this.parseDateRangeEntity(pensumFachstelleTS, pensumFachstelleFromServer);
            pensumFachstelleTS.pensum = pensumFachstelleFromServer.pensum;
            if (pensumFachstelleFromServer.fachstelle) {
                pensumFachstelleTS.fachstelle =
                    this.parseFachstelle(new TSFachstelle(), pensumFachstelleFromServer.fachstelle);
            }
            return pensumFachstelleTS;
        }
        return undefined;
    }

    private betreuungListToRestObject(betreuungen: Array<TSBetreuung>): Array<any> {
        return betreuungen
            ? betreuungen.map(item => this.betreuungToRestObject({}, item))
            : [];
    }

    public betreuungToRestObject(restBetreuung: any, betreuung: TSBetreuung): any {
        this.abstractMutableEntityToRestObject(restBetreuung, betreuung);
        restBetreuung.betreuungsstatus = betreuung.betreuungsstatus;
        restBetreuung.grundAblehnung = betreuung.grundAblehnung;
        restBetreuung.datumAblehnung = DateUtil.momentToLocalDate(betreuung.datumAblehnung);
        restBetreuung.datumBestaetigung = DateUtil.momentToLocalDate(betreuung.datumBestaetigung);
        restBetreuung.vertrag = betreuung.vertrag;
        restBetreuung.keineKesbPlatzierung = betreuung.keineKesbPlatzierung;
        restBetreuung.erweiterteBeduerfnisse = betreuung.erweiterteBeduerfnisse;
        if (betreuung.institutionStammdaten) {
            restBetreuung.institutionStammdaten =
                this.institutionStammdatenToRestObject({}, betreuung.institutionStammdaten);
        }
        if (betreuung.betreuungspensumContainers) {
            restBetreuung.betreuungspensumContainers = [];
            betreuung.betreuungspensumContainers.forEach((betPensCont: TSBetreuungspensumContainer) => {
                restBetreuung.betreuungspensumContainers.push(this.betreuungspensumContainerToRestObject({},
                    betPensCont));
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
        restAngebot.einschulungTyp = angebotDTO.einschulungTyp;
        restAngebot.kindContainerId = angebotDTO.kindContainerId;
        restAngebot.mutterspracheDeutsch = angebotDTO.mutterspracheDeutsch;
        return restAngebot;

    }

    public betreuungspensumContainerToRestObject(restBetPensCont: any, betPensCont: TSBetreuungspensumContainer): any {
        this.abstractMutableEntityToRestObject(restBetPensCont, betPensCont);
        if (betPensCont.betreuungspensumGS) {
            restBetPensCont.betreuungspensumGS = this.betreuungspensumToRestObject({}, betPensCont.betreuungspensumGS);
        }
        if (betPensCont.betreuungspensumJA) {
            restBetPensCont.betreuungspensumJA = this.betreuungspensumToRestObject({}, betPensCont.betreuungspensumJA);
        }
        return restBetPensCont;
    }

    public abwesenheitContainerToRestObject(restAbwesenheitCont: any, abwesenheitCont: TSAbwesenheitContainer): any {
        this.abstractMutableEntityToRestObject(restAbwesenheitCont, abwesenheitCont);
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
        if (betreuungspensum.nichtEingetreten !== null) {
            // wenn es null ist, wird es als null zum Server geschickt und der Server versucht, es zu validieren und
            // wirft eine NPE
            restBetreuungspensum.nichtEingetreten = betreuungspensum.nichtEingetreten;
        }
        restBetreuungspensum.monatlicheBetreuungskosten = betreuungspensum.monatlicheBetreuungskosten;
        return restBetreuungspensum;
    }

    public betreuungsmitteilungPensumToRestObject(restBetreuungspensum: any,
                                                  betreuungspensum: TSBetreuungsmitteilungPensum,
    ): any {
        this.abstractPensumEntityToRestObject(restBetreuungspensum, betreuungspensum);
        return restBetreuungspensum;
    }

    public abwesenheitToRestObject(restAbwesenheit: any, abwesenheit: TSAbwesenheit): any {
        this.abstractDateRangeEntityToRestObject(restAbwesenheit, abwesenheit);
        return restAbwesenheit;
    }

    public parseBetreuungList(data: Array<any>): TSBetreuung[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseBetreuung(new TSBetreuung(), item))
            : [this.parseBetreuung(new TSBetreuung(), data)];
    }

    public parseBetreuung(betreuungTS: TSBetreuung, betreuungFromServer: any): TSBetreuung {
        if (betreuungFromServer) {
            this.parseAbstractMutableEntity(betreuungTS, betreuungFromServer);
            betreuungTS.grundAblehnung = betreuungFromServer.grundAblehnung;
            betreuungTS.datumAblehnung = DateUtil.localDateToMoment(betreuungFromServer.datumAblehnung);
            betreuungTS.datumBestaetigung = DateUtil.localDateToMoment(betreuungFromServer.datumBestaetigung);
            betreuungTS.vertrag = betreuungFromServer.vertrag;
            betreuungTS.keineKesbPlatzierung = betreuungFromServer.keineKesbPlatzierung;
            betreuungTS.erweiterteBeduerfnisse = betreuungFromServer.erweiterteBeduerfnisse;
            betreuungTS.betreuungsstatus = betreuungFromServer.betreuungsstatus;
            betreuungTS.institutionStammdaten = this.parseInstitutionStammdaten(new TSInstitutionStammdaten(),
                betreuungFromServer.institutionStammdaten);
            betreuungTS.betreuungspensumContainers =
                this.parseBetreuungspensumContainers(betreuungFromServer.betreuungspensumContainers);
            betreuungTS.abwesenheitContainers =
                this.parseAbwesenheitContainers(betreuungFromServer.abwesenheitContainers);
            betreuungTS.betreuungNummer = betreuungFromServer.betreuungNummer;
            betreuungTS.verfuegung = this.parseVerfuegung(new TSVerfuegung(), betreuungFromServer.verfuegung);
            betreuungTS.kindFullname = betreuungFromServer.kindFullname;
            betreuungTS.kindNummer = betreuungFromServer.kindNummer;
            betreuungTS.gesuchId = betreuungFromServer.gesuchId;
            betreuungTS.gesuchsperiode =
                this.parseGesuchsperiode(new TSGesuchsperiode(), betreuungFromServer.gesuchsperiode);
            betreuungTS.betreuungMutiert = betreuungFromServer.betreuungMutiert;
            betreuungTS.abwesenheitMutiert = betreuungFromServer.abwesenheitMutiert;
            betreuungTS.gueltig = betreuungFromServer.gueltig;
            betreuungTS.belegungTagesschule =
                this.parseBelegungTagesschule(new TSBelegungTagesschule(), betreuungFromServer.belegungTagesschule);
            betreuungTS.belegungFerieninsel =
                this.parseBelegungFerieninsel(new TSBelegungFerieninsel(), betreuungFromServer.belegungFerieninsel);
            betreuungTS.anmeldungMutationZustand = betreuungFromServer.anmeldungMutationZustand;
            betreuungTS.keineDetailinformationen = betreuungFromServer.keineDetailinformationen;
            betreuungTS.bgNummer = betreuungFromServer.bgNummer;
            return betreuungTS;
        }
        return undefined;
    }

    public parseBetreuungspensumContainers(data: Array<any>): TSBetreuungspensumContainer[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseBetreuungspensumContainer(new TSBetreuungspensumContainer(), item))
            : [this.parseBetreuungspensumContainer(new TSBetreuungspensumContainer(), data)];
    }

    public parseAbwesenheitContainers(data: Array<any>): TSAbwesenheitContainer[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseAbwesenheitContainer(new TSAbwesenheitContainer(), item))
            : [this.parseAbwesenheitContainer(new TSAbwesenheitContainer(), data)];
    }

    public parseBetreuungspensumContainer(betPensContainerTS: TSBetreuungspensumContainer,
                                          betPensContFromServer: any,
    ): TSBetreuungspensumContainer {
        if (betPensContFromServer) {
            this.parseAbstractMutableEntity(betPensContainerTS, betPensContFromServer);
            if (betPensContFromServer.betreuungspensumGS) {
                betPensContainerTS.betreuungspensumGS =
                    this.parseBetreuungspensum(new TSBetreuungspensum(), betPensContFromServer.betreuungspensumGS);
            }
            if (betPensContFromServer.betreuungspensumJA) {
                betPensContainerTS.betreuungspensumJA =
                    this.parseBetreuungspensum(new TSBetreuungspensum(), betPensContFromServer.betreuungspensumJA);
            }
            return betPensContainerTS;
        }
        return undefined;
    }

    public parseAbwesenheitContainer(abwesenheitContainerTS: TSAbwesenheitContainer,
                                     abwesenheitContFromServer: any,
    ): TSAbwesenheitContainer {
        if (abwesenheitContFromServer) {
            this.parseAbstractMutableEntity(abwesenheitContainerTS, abwesenheitContFromServer);
            if (abwesenheitContFromServer.abwesenheitGS) {
                abwesenheitContainerTS.abwesenheitGS =
                    this.parseAbwesenheit(new TSAbwesenheit(), abwesenheitContFromServer.abwesenheitGS);
            }
            if (abwesenheitContFromServer.abwesenheitJA) {
                abwesenheitContainerTS.abwesenheitJA =
                    this.parseAbwesenheit(new TSAbwesenheit(), abwesenheitContFromServer.abwesenheitJA);
            }
            return abwesenheitContainerTS;
        }
        return undefined;
    }

    public parseBetreuungspensum(betreuungspensumTS: TSBetreuungspensum,
                                 betreuungspensumFromServer: any,
    ): TSBetreuungspensum {
        if (betreuungspensumFromServer) {
            this.parseAbstractPensumEntity(betreuungspensumTS, betreuungspensumFromServer);
            betreuungspensumTS.nichtEingetreten = betreuungspensumFromServer.nichtEingetreten;
            betreuungspensumTS.monatlicheBetreuungskosten = betreuungspensumFromServer.monatlicheBetreuungskosten;
            return betreuungspensumTS;
        }
        return undefined;
    }

    public parseBetreuungsmitteilungPensum(betreuungspensumTS: TSBetreuungsmitteilungPensum,
                                           betreuungspensumFromServer: any,
    ): TSBetreuungsmitteilungPensum {
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
        return data && Array.isArray(data)
            ? data.map(item => this.parseErwerbspensumContainer(new TSErwerbspensumContainer(), item))
            : [this.parseErwerbspensumContainer(new TSErwerbspensumContainer(), data)];
    }

    public gesuchsperiodeToRestObject(restGesuchsperiode: any, gesuchsperiode: TSGesuchsperiode): any {
        if (gesuchsperiode) {
            this.abstractDateRangeEntityToRestObject(restGesuchsperiode, gesuchsperiode);
            restGesuchsperiode.status = gesuchsperiode.status;
            restGesuchsperiode.datumFreischaltungTagesschule =
                DateUtil.momentToLocalDate(gesuchsperiode.datumFreischaltungTagesschule);
            restGesuchsperiode.datumErsterSchultag = DateUtil.momentToLocalDate(gesuchsperiode.datumErsterSchultag);
            return restGesuchsperiode;
        }
        return undefined;
    }

    public parseGesuchsperiode(gesuchsperiodeTS: TSGesuchsperiode, gesuchsperiodeFromServer: any): TSGesuchsperiode {
        if (gesuchsperiodeFromServer) {
            this.parseDateRangeEntity(gesuchsperiodeTS, gesuchsperiodeFromServer);
            gesuchsperiodeTS.status = gesuchsperiodeFromServer.status;
            gesuchsperiodeTS.datumFreischaltungTagesschule =
                DateUtil.localDateToMoment(gesuchsperiodeFromServer.datumFreischaltungTagesschule);
            gesuchsperiodeTS.datumErsterSchultag =
                DateUtil.localDateToMoment(gesuchsperiodeFromServer.datumErsterSchultag);
            return gesuchsperiodeTS;
        }
        return undefined;
    }

    public parseGesuchsperioden(data: any): TSGesuchsperiode[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseGesuchsperiode(new TSGesuchsperiode(), item))
            : [this.parseGesuchsperiode(new TSGesuchsperiode(), data)];
    }

    public antragDTOToRestObject(restPendenz: any, pendenz: TSAntragDTO): any {
        restPendenz.antragId = pendenz.antragId;
        restPendenz.fallNummer = pendenz.fallNummer;
        restPendenz.dossierId = pendenz.dossierId;
        restPendenz.familienName = pendenz.familienName;
        restPendenz.angebote = pendenz.angebote;
        restPendenz.antragTyp = pendenz.antragTyp;
        restPendenz.eingangsdatum = DateUtil.momentToLocalDate(pendenz.eingangsdatum);
        restPendenz.regelnGueltigAb = DateUtil.momentToLocalDate(pendenz.regelnGueltigAb);
        restPendenz.eingangsdatumSTV = DateUtil.momentToLocalDate(pendenz.eingangsdatumSTV);
        restPendenz.aenderungsdatum = DateUtil.momentToLocalDateTime(pendenz.aenderungsdatum);
        restPendenz.gesuchsperiodeGueltigAb = DateUtil.momentToLocalDate(pendenz.gesuchsperiodeGueltigAb);
        restPendenz.gesuchsperiodeGueltigBis = DateUtil.momentToLocalDate(pendenz.gesuchsperiodeGueltigBis);
        restPendenz.institutionen = pendenz.institutionen;
        restPendenz.kinder = pendenz.kinder;
        restPendenz.verantwortlicherBG = pendenz.verantwortlicherBG;
        restPendenz.verantwortlicherTS = pendenz.verantwortlicherTS;
        restPendenz.verantwortlicherUsernameBG = pendenz.verantwortlicherUsernameBG;
        restPendenz.verantwortlicherUsernameTS = pendenz.verantwortlicherUsernameTS;
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
        antragTS.dossierId = antragFromServer.dossierId;
        antragTS.familienName = antragFromServer.familienName;
        antragTS.angebote = antragFromServer.angebote;
        antragTS.kinder = antragFromServer.kinder;
        antragTS.antragTyp = antragFromServer.antragTyp;
        antragTS.eingangsdatum = DateUtil.localDateToMoment(antragFromServer.eingangsdatum);
        antragTS.regelnGueltigAb = DateUtil.localDateToMoment(antragFromServer.regelnGueltigAb);
        antragTS.eingangsdatumSTV = DateUtil.localDateToMoment(antragFromServer.eingangsdatumSTV);
        antragTS.aenderungsdatum = DateUtil.localDateTimeToMoment(antragFromServer.aenderungsdatum);
        antragTS.gesuchsperiodeGueltigAb = DateUtil.localDateToMoment(antragFromServer.gesuchsperiodeGueltigAb);
        antragTS.gesuchsperiodeGueltigBis = DateUtil.localDateToMoment(antragFromServer.gesuchsperiodeGueltigBis);
        antragTS.institutionen = antragFromServer.institutionen;
        antragTS.verantwortlicherBG = antragFromServer.verantwortlicherBG;
        antragTS.verantwortlicherTS = antragFromServer.verantwortlicherTS;
        antragTS.verantwortlicherUsernameBG = antragFromServer.verantwortlicherUsernameBG;
        antragTS.verantwortlicherUsernameTS = antragFromServer.verantwortlicherUsernameTS;
        antragTS.status = antragFromServer.status;
        antragTS.verfuegt = antragFromServer.verfuegt;
        antragTS.beschwerdeHaengig = antragFromServer.beschwerdeHaengig;
        antragTS.laufnummer = antragFromServer.laufnummer;
        antragTS.gesuchBetreuungenStatus = antragFromServer.gesuchBetreuungenStatus;
        antragTS.eingangsart = antragFromServer.eingangsart;
        antragTS.besitzerUsername = antragFromServer.besitzerUsername;
        antragTS.dokumenteHochgeladen = antragFromServer.dokumenteHochgeladen;
        antragTS.gemeinde = antragFromServer.gemeinde;
        return antragTS;
    }

    public parseFallAntragDTO(fallAntragTS: TSFallAntragDTO, antragFromServer: any): TSFallAntragDTO {
        fallAntragTS.fallID = antragFromServer.fallID;
        fallAntragTS.dossierId = antragFromServer.dossierId;
        fallAntragTS.fallNummer = antragFromServer.fallNummer;
        fallAntragTS.familienName = antragFromServer.familienName;
        return fallAntragTS;
    }

    public parseAntragDTOs(data: any): TSAntragDTO[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseAntragDTO(new TSAntragDTO(), item))
            : [this.parseAntragDTO(new TSAntragDTO(), data)];
    }

    public parseQuickSearchResult(dataFromServer: any): TSQuickSearchResult {
        if (dataFromServer) {
            const resultEntries = this.parseSearchResultEntries(dataFromServer.resultEntities);
            return new TSQuickSearchResult(resultEntries, dataFromServer.numberOfResults);
        }
        return undefined;
    }

    private parseSearchResultEntries(data: Array<any>): Array<TSSearchResultEntry> {
        return data && Array.isArray(data)
            ? data.map(item => this.parseSearchResultEntry(new TSSearchResultEntry(), item))
            : [];
    }

    private parseSearchResultEntry(entry: TSSearchResultEntry, dataFromServer: any): TSSearchResultEntry {
        entry.additionalInformation = dataFromServer.additionalInformation;
        entry.gesuchID = dataFromServer.gesuchID;
        entry.fallID = dataFromServer.fallID;
        entry.resultId = dataFromServer.resultId;
        entry.text = dataFromServer.text;
        entry.entity = dataFromServer.entity;
        entry.dossierId = dataFromServer.dossierId;
        if (dataFromServer.antragDTO) {
            // dataFromServer.antragDTO.typ === TSAntragDTO
            entry.antragDTO = this.isFallAntragDTO(dataFromServer.antragDTO) ?
                this.parseFallAntragDTO(new TSFallAntragDTO(), dataFromServer.antragDTO) :
                this.parseAntragDTO(new TSAntragDTO(), dataFromServer.antragDTO);
        }
        return entry;
    }

    private isFallAntragDTO(antragRestObj: any): boolean {
        if (antragRestObj) {
            return antragRestObj.clazz === TSFallAntragDTO.SERVER_CLASS_NAME;
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
        pendenzTS.gemeinde = pendenzFromServer.gemeinde;
        return pendenzTS;
    }

    public parsePendenzBetreuungenList(data: any): TSPendenzBetreuung[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parsePendenzBetreuungen(new TSPendenzBetreuung(), item))
            : [this.parsePendenzBetreuungen(new TSPendenzBetreuung(), data)];
    }

    public userToRestObject(user: any, userTS: TSBenutzer): TSBenutzer | undefined {
        if (!userTS) {
            return undefined;
        }

        user.username = userTS.username;
        user.externalUUID = userTS.externalUUID;
        user.password = userTS.password;
        user.nachname = userTS.nachname;
        user.vorname = userTS.vorname;
        user.email = userTS.email;
        user.mandant = this.mandantToRestObject({}, userTS.mandant);
        user.status = userTS.status;
        if (userTS.berechtigungen) {
            user.berechtigungen = [];
            userTS.berechtigungen.forEach((berecht: TSBerechtigung) => {
                user.berechtigungen.push(this.berechtigungToRestObject({}, berecht));
            });
            return user;
        }

        // TODO why is there only a return value when we have a berechtigung? Throw an error here?
        return undefined;
    }

    public parseUser(userTS: TSBenutzer, userFromServer: any): TSBenutzer {
        if (userFromServer) {
            userTS.username = userFromServer.username;
            userTS.externalUUID = userFromServer.externalUUID;
            userTS.password = userFromServer.password;
            userTS.nachname = userFromServer.nachname;
            userTS.vorname = userFromServer.vorname;
            userTS.email = userFromServer.email;
            userTS.mandant = this.parseMandant(new TSMandant(), userFromServer.mandant);
            userTS.amt = userFromServer.amt;
            userTS.status = userFromServer.status;
            userTS.currentBerechtigung =
                this.parseBerechtigung(new TSBerechtigung(), userFromServer.currentBerechtigung);
            userTS.berechtigungen = this.parseBerechtigungen(userFromServer.berechtigungen);
            return userTS;
        }
        return undefined;
    }

    public parseBerechtigungen(data: Array<any>): TSBerechtigung[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseBerechtigung(new TSBerechtigung(), item))
            : [this.parseBerechtigung(new TSBerechtigung(), data)];
    }

    public parseUserList(data: any): TSBenutzer[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseUser(new TSBenutzer(), item))
            : [this.parseUser(new TSBenutzer(), data)];
    }

    public berechtigungToRestObject(berechtigung: any, berechtigungTS: TSBerechtigung): any {
        if (berechtigungTS) {
            this.abstractDateRangeEntityToRestObject(berechtigung, berechtigungTS);
            berechtigung.role = berechtigungTS.role;
            berechtigung.traegerschaft = this.traegerschaftToRestObject({}, berechtigungTS.traegerschaft);
            berechtigung.institution = this.institutionToRestObject({}, berechtigungTS.institution);
            // Gemeinden
            berechtigung.gemeindeList = this.gemeindeListToRestObject(berechtigungTS.gemeindeList);
            return berechtigung;
        }
        return undefined;
    }

    public parseBerechtigung(berechtigungTS: TSBerechtigung, berechtigungFromServer: any): TSBerechtigung {
        if (berechtigungFromServer) {
            this.parseDateRangeEntity(berechtigungTS, berechtigungFromServer);
            berechtigungTS.role = berechtigungFromServer.role;
            berechtigungTS.traegerschaft =
                this.parseTraegerschaft(new TSTraegerschaft(), berechtigungFromServer.traegerschaft);
            berechtigungTS.institution = this.parseInstitution(new TSInstitution(), berechtigungFromServer.institution);
            // Gemeinden
            berechtigungTS.gemeindeList = this.parseGemeindeList(berechtigungFromServer.gemeindeList);
            return berechtigungTS;
        }
        return undefined;
    }

    public parseBerechtigungHistory(historyTS: TSBerechtigungHistory, historyFromServer: any): TSBerechtigungHistory {
        if (historyFromServer) {
            this.parseDateRangeEntity(historyTS, historyFromServer);
            historyTS.userErstellt = historyFromServer.userErstellt;
            historyTS.username = historyFromServer.username;
            historyTS.role = historyFromServer.role;
            historyTS.traegerschaft = this.parseTraegerschaft(new TSTraegerschaft(), historyFromServer.traegerschaft);
            historyTS.institution = this.parseInstitution(new TSInstitution(), historyFromServer.institution);
            historyTS.gemeinden = historyFromServer.gemeinden;
            historyTS.status = historyFromServer.status;
            historyTS.geloescht = historyFromServer.geloescht;
            return historyTS;
        }
        return undefined;
    }

    public parseBerechtigungHistoryList(data: any): TSBerechtigungHistory[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseBerechtigungHistory(new TSBerechtigungHistory(), item))
            : [this.parseBerechtigungHistory(new TSBerechtigungHistory(), data)];
    }

    public parseDokumenteDTO(dokumenteDTO: TSDokumenteDTO, dokumenteFromServer: any): TSDokumenteDTO {
        if (dokumenteFromServer) {
            dokumenteDTO.dokumentGruende = this.parseDokumentGruende(dokumenteFromServer.dokumentGruende);
            return dokumenteDTO;
        }
        return undefined;
    }

    private parseDokumentGruende(data: Array<any>): TSDokumentGrund[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseDokumentGrund(new TSDokumentGrund(), item))
            : [this.parseDokumentGrund(new TSDokumentGrund(), data)];
    }

    public parseDokumentGrund(dokumentGrund: TSDokumentGrund, dokumentGrundFromServer: any): TSDokumentGrund {
        if (dokumentGrundFromServer) {
            this.parseAbstractMutableEntity(dokumentGrund, dokumentGrundFromServer);
            dokumentGrund.dokumentGrundTyp = dokumentGrundFromServer.dokumentGrundTyp;
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

    private parseDokumente(data: Array<any>): TSDokument[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseDokument(new TSDokument(), item))
            : [this.parseDokument(new TSDokument(), data)];
    }

    private parseDokument(dokument: TSDokument, dokumentFromServer: any): TSDokument {
        if (dokumentFromServer) {
            this.parseAbstractMutableEntity(dokument, dokumentFromServer);
            dokument.filename = dokumentFromServer.filename;
            dokument.filepfad = dokumentFromServer.filepfad;
            dokument.filesize = dokumentFromServer.filesize;
            dokument.timestampUpload = DateUtil.localDateTimeToMoment(dokumentFromServer.timestampUpload);
            dokument.userUploaded = this.parseUser(new TSBenutzer(), dokumentFromServer.userUploaded);
            return dokument;
        }
        return undefined;
    }

    public dokumentGrundToRestObject(restDokumentGrund: any, dokumentGrundTS: TSDokumentGrund): any {
        if (dokumentGrundTS) {
            this.abstractMutableEntityToRestObject(restDokumentGrund, dokumentGrundTS);
            restDokumentGrund.tag = dokumentGrundTS.tag;
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

    private dokumenteToRestObject(data: Array<TSDokument>): Array<any> {
        return data && Array.isArray(data)
            ? data.map(item => this.dokumentToRestObject({}, item))
            : [];
    }

    private dokumentToRestObject(dokument: any, dokumentTS: TSDokument): any {
        if (dokumentTS) {
            this.abstractMutableEntityToRestObject(dokument, dokumentTS);
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
            this.parseAbstractMutableEntity(verfuegungTS, verfuegungFromServer);
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
            this.abstractMutableEntityToRestObject(verfuegung, verfuegungTS);
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

    private zeitabschnittListToRestObject(data: Array<TSVerfuegungZeitabschnitt>): Array<any> {
        return data && Array.isArray(data)
            ? data.map(item => this.zeitabschnittToRestObject({}, item))
            : [];
    }

    private parseVerfuegungZeitabschnitte(data: Array<any>): TSVerfuegungZeitabschnitt[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseVerfuegungZeitabschnitt(new TSVerfuegungZeitabschnitt(), item))
            : [this.parseVerfuegungZeitabschnitt(new TSVerfuegungZeitabschnitt(), data)];
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

    public parseVerfuegungZeitabschnitt(verfuegungZeitabschnittTS: TSVerfuegungZeitabschnitt,
                                        zeitabschnittFromServer: any,
    ): TSVerfuegungZeitabschnitt {
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
            verfuegungZeitabschnittTS.massgebendesEinkommenVorAbzugFamgr =
                zeitabschnittFromServer.massgebendesEinkommenVorAbzugFamgr;
            verfuegungZeitabschnittTS.famGroesse = zeitabschnittFromServer.famGroesse;
            verfuegungZeitabschnittTS.zahlungsstatus = zeitabschnittFromServer.zahlungsstatus;
            verfuegungZeitabschnittTS.vollkosten = zeitabschnittFromServer.vollkosten;
            verfuegungZeitabschnittTS.einkommensjahr = zeitabschnittFromServer.einkommensjahr;
            verfuegungZeitabschnittTS.kategorieZuschlagZumErwerbspensum =
                zeitabschnittFromServer.kategorieZuschlagZumErwerbspensum;
            verfuegungZeitabschnittTS.kategorieMaxEinkommen = zeitabschnittFromServer.kategorieMaxEinkommen;
            verfuegungZeitabschnittTS.kategorieKeinPensum = zeitabschnittFromServer.kategorieKeinPensum;
            verfuegungZeitabschnittTS.zuSpaetEingereicht = zeitabschnittFromServer.zuSpaetEingereicht;
            verfuegungZeitabschnittTS.sameVerfuegungsdaten = zeitabschnittFromServer.sameVerfuegungsdaten;
            verfuegungZeitabschnittTS.sameVerguenstigung = zeitabschnittFromServer.sameVerguenstigung;
            return verfuegungZeitabschnittTS;
        }
        return undefined;
    }

    public parseDownloadFile(tsDownloadFile: TSDownloadFile, downloadFileFromServer: any): any {
        if (downloadFileFromServer) {
            this.parseAbstractFileEntity(tsDownloadFile, downloadFileFromServer);
            tsDownloadFile.accessToken = downloadFileFromServer.accessToken;
            return tsDownloadFile;
        }
        return undefined;
    }

    public parseWizardStep(wizardStepTS: TSWizardStep, wizardStepFromServer: any): TSWizardStep {
        this.parseAbstractMutableEntity(wizardStepTS, wizardStepFromServer);
        wizardStepTS.gesuchId = wizardStepFromServer.gesuchId;
        wizardStepTS.wizardStepName = wizardStepFromServer.wizardStepName;
        wizardStepTS.verfuegbar = wizardStepFromServer.verfuegbar;
        wizardStepTS.wizardStepStatus = wizardStepFromServer.wizardStepStatus;
        wizardStepTS.bemerkungen = wizardStepFromServer.bemerkungen;
        return wizardStepTS;
    }

    public wizardStepToRestObject(restWizardStep: any, wizardStep: TSWizardStep): any {
        this.abstractMutableEntityToRestObject(restWizardStep, wizardStep);
        restWizardStep.gesuchId = wizardStep.gesuchId;
        restWizardStep.verfuegbar = wizardStep.verfuegbar;
        restWizardStep.wizardStepName = wizardStep.wizardStepName;
        restWizardStep.wizardStepStatus = wizardStep.wizardStepStatus;
        restWizardStep.bemerkungen = wizardStep.bemerkungen;
        return restWizardStep;
    }

    public parseWizardStepList(data: any): TSWizardStep[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseWizardStep(new TSWizardStep(), item))
            : [this.parseWizardStep(new TSWizardStep(), data)];
    }

    public parseAntragStatusHistoryCollection(data: Array<any>): TSAntragStatusHistory[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseAntragStatusHistory(new TSAntragStatusHistory(), item))
            : [this.parseAntragStatusHistory(new TSAntragStatusHistory(), data)];
    }

    public parseAntragStatusHistory(antragStatusHistoryTS: TSAntragStatusHistory,
                                    antragStatusHistoryFromServer: any,
    ): TSAntragStatusHistory {
        this.parseAbstractMutableEntity(antragStatusHistoryTS, antragStatusHistoryFromServer);
        antragStatusHistoryTS.gesuchId = antragStatusHistoryFromServer.gesuchId;
        antragStatusHistoryTS.benutzer = this.parseUser(new TSBenutzer(), antragStatusHistoryFromServer.benutzer);
        antragStatusHistoryTS.timestampVon = DateUtil.localDateTimeToMoment(antragStatusHistoryFromServer.timestampVon);
        antragStatusHistoryTS.timestampBis = DateUtil.localDateTimeToMoment(antragStatusHistoryFromServer.timestampBis);
        antragStatusHistoryTS.status = antragStatusHistoryFromServer.status;
        return antragStatusHistoryTS;
    }

    public antragStatusHistoryToRestObject(restAntragStatusHistory: any,
                                           antragStatusHistory: TSAntragStatusHistory,
    ): any {
        this.abstractMutableEntityToRestObject(restAntragStatusHistory, antragStatusHistory);
        restAntragStatusHistory.gesuchId = antragStatusHistory.gesuchId;
        restAntragStatusHistory.benutzer = this.userToRestObject({}, antragStatusHistory.benutzer);
        restAntragStatusHistory.timestampVon = DateUtil.momentToLocalDateTime(antragStatusHistory.timestampVon);
        restAntragStatusHistory.timestampBis = DateUtil.momentToLocalDateTime(antragStatusHistory.timestampBis);
        restAntragStatusHistory.status = antragStatusHistory.status;
        return restAntragStatusHistory;
    }

    public mahnungToRestObject(restMahnung: any, tsMahnung: TSMahnung): any {
        if (tsMahnung) {
            this.abstractMutableEntityToRestObject(restMahnung, tsMahnung);
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
        return data && Array.isArray(data)
            ? data.map(item => this.parseMahnung(new TSMahnung(), item))
            : [this.parseMahnung(new TSMahnung(), data)];
    }

    public parseMahnung(tsMahnung: TSMahnung, mahnungFromServer: any): TSMahnung {
        if (mahnungFromServer) {
            this.parseAbstractMutableEntity(tsMahnung, mahnungFromServer);

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

    public finanzModelToRestObject(restFinSitModel: any, finSitModel: TSFinanzModel): any {
        if (finSitModel) {
            if (finSitModel.finanzielleSituationContainerGS1) {
                restFinSitModel.finanzielleSituationContainerGS1 =
                    this.finanzielleSituationContainerToRestObject({}, finSitModel.finanzielleSituationContainerGS1);
            }
            if (finSitModel.finanzielleSituationContainerGS2) {
                restFinSitModel.finanzielleSituationContainerGS2 =
                    this.finanzielleSituationContainerToRestObject({}, finSitModel.finanzielleSituationContainerGS2);
            }
            if (finSitModel.einkommensverschlechterungContainerGS1) {
                restFinSitModel.einkommensverschlechterungContainerGS1 =
                    this.einkommensverschlechterungContainerToRestObject({},
                        finSitModel.einkommensverschlechterungContainerGS1);
            }
            if (finSitModel.einkommensverschlechterungContainerGS2) {
                restFinSitModel.einkommensverschlechterungContainerGS2 =
                    this.einkommensverschlechterungContainerToRestObject({},
                        finSitModel.einkommensverschlechterungContainerGS2);
            }
            if (finSitModel.einkommensverschlechterungInfoContainer) {
                restFinSitModel.einkommensverschlechterungInfoContainer =
                    this.einkommensverschlechterungInfoContainerToRestObject({},
                        finSitModel.einkommensverschlechterungInfoContainer);
            }
            restFinSitModel.gemeinsameSteuererklaerung = finSitModel.gemeinsameSteuererklaerung;
            return restFinSitModel;
        }
        return undefined;

    }

    public gesuchstellerContainerToRestObject(restGSCont: any, gesuchstellerCont: TSGesuchstellerContainer): any {
        if (gesuchstellerCont) {
            this.abstractMutableEntityToRestObject(restGSCont, gesuchstellerCont);
            restGSCont.adressen = this.adressenContainerListToRestObject(gesuchstellerCont.adressen);
            restGSCont.alternativeAdresse =
                this.adresseContainerToRestObject({}, gesuchstellerCont.korrespondenzAdresse);
            restGSCont.rechnungsAdresse = this.adresseContainerToRestObject({}, gesuchstellerCont.rechnungsAdresse);
            if (gesuchstellerCont.gesuchstellerGS) {
                restGSCont.gesuchstellerGS = this.gesuchstellerToRestObject({}, gesuchstellerCont.gesuchstellerGS);
            }
            if (gesuchstellerCont.gesuchstellerJA) {
                restGSCont.gesuchstellerJA = this.gesuchstellerToRestObject({}, gesuchstellerCont.gesuchstellerJA);
            }
            if (gesuchstellerCont.finanzielleSituationContainer) {
                restGSCont.finanzielleSituationContainer =
                    this.finanzielleSituationContainerToRestObject({}, gesuchstellerCont.finanzielleSituationContainer);
            }
            if (gesuchstellerCont.einkommensverschlechterungContainer) {
                restGSCont.einkommensverschlechterungContainer =
                    this.einkommensverschlechterungContainerToRestObject({},
                        gesuchstellerCont.einkommensverschlechterungContainer);
            }
            restGSCont.erwerbspensenContainers = [];
            if (Array.isArray(gesuchstellerCont.erwerbspensenContainer)) {
                restGSCont.erwerbspensenContainers =
                    gesuchstellerCont.erwerbspensenContainer.map(ec => this.erwerbspensumContainerToRestObject({}, ec));
            }
            return restGSCont;
        }
        return undefined;
    }

    public parseGesuchstellerContainer(gesuchstellerContTS: TSGesuchstellerContainer,
                                       gesuchstellerContFromServer: any,
    ): any {
        if (gesuchstellerContFromServer) {
            this.parseAbstractMutableEntity(gesuchstellerContTS, gesuchstellerContFromServer);
            gesuchstellerContTS.gesuchstellerJA =
                this.parseGesuchsteller(new TSGesuchsteller(), gesuchstellerContFromServer.gesuchstellerJA);
            gesuchstellerContTS.gesuchstellerGS =
                this.parseGesuchsteller(new TSGesuchsteller(), gesuchstellerContFromServer.gesuchstellerGS);
            gesuchstellerContTS.adressen = this.parseAdressenContainerList(gesuchstellerContFromServer.adressen);
            gesuchstellerContTS.korrespondenzAdresse = this.parseAdresseContainer(
                new TSAdresseContainer(), gesuchstellerContFromServer.alternativeAdresse);
            gesuchstellerContTS.rechnungsAdresse = this.parseAdresseContainer(
                new TSAdresseContainer(), gesuchstellerContFromServer.rechnungsAdresse);
            gesuchstellerContTS.finanzielleSituationContainer = this.parseFinanzielleSituationContainer(
                new TSFinanzielleSituationContainer(), gesuchstellerContFromServer.finanzielleSituationContainer);
            gesuchstellerContTS.einkommensverschlechterungContainer = this.parseEinkommensverschlechterungContainer(
                new TSEinkommensverschlechterungContainer(),
                gesuchstellerContFromServer.einkommensverschlechterungContainer);
            gesuchstellerContTS.erwerbspensenContainer =
                this.parseErwerbspensenContainers(gesuchstellerContFromServer.erwerbspensenContainers);
            return gesuchstellerContTS;
        }
        return undefined;
    }

    private adressenContainerListToRestObject(adressen: Array<TSAdresseContainer>): any[] {
        return adressen
            ? adressen.map(item => this.adresseContainerToRestObject({}, item))
            : [];
    }

    private adresseContainerToRestObject(restAddresseCont: any, adresseContTS: TSAdresseContainer): any {
        if (adresseContTS) {
            this.abstractMutableEntityToRestObject(restAddresseCont, adresseContTS);
            restAddresseCont.adresseGS = this.adresseToRestObject({}, adresseContTS.adresseGS);
            restAddresseCont.adresseJA = this.adresseToRestObject({}, adresseContTS.adresseJA);
            return restAddresseCont;
        }
        return undefined;
    }

    private parseAdressenContainerList(adressen: any): Array<TSAdresseContainer> {
        return adressen
            ? adressen.map((item: any) => this.parseAdresseContainer(new TSAdresseContainer(), item))
            : [];
    }

    private parseAdresseContainer(adresseContainerTS: TSAdresseContainer, adresseFromServer: any): TSAdresseContainer {
        if (adresseFromServer) {
            this.parseAbstractMutableEntity(adresseContainerTS, adresseFromServer);
            adresseContainerTS.adresseGS = this.parseAdresse(new TSAdresse(), adresseFromServer.adresseGS);
            adresseContainerTS.adresseJA = this.parseAdresse(new TSAdresse(), adresseFromServer.adresseJA);
            return adresseContainerTS;
        }
        return undefined;
    }

    public parseWorkJobList(jobWrapper: any): Array<TSWorkJob> {
        return jobWrapper && jobWrapper.jobs
            ? jobWrapper.map((item: any) => this.parseWorkJob(new TSWorkJob(), item))
            : [];
    }

    private parseWorkJob(tsWorkJob: TSWorkJob, workjobFromServer: any): TSWorkJob {
        if (workjobFromServer) {
            this.parseAbstractMutableEntity(tsWorkJob, workjobFromServer);
            tsWorkJob.startinguser = workjobFromServer.startinguser;
            tsWorkJob.batchJobStatus = workjobFromServer.batchJobStatus;
            tsWorkJob.executionId = workjobFromServer.executionId;
            tsWorkJob.params = workjobFromServer.params;
            tsWorkJob.workJobType = workjobFromServer.workJobType;
            tsWorkJob.resultData = workjobFromServer.resultData;
            tsWorkJob.requestURI = workjobFromServer.requestURI;
            tsWorkJob.execution =
                this.parseBatchJobInformation(new TSBatchJobInformation(), workjobFromServer.execution);
            return tsWorkJob;
        }
        return undefined;
    }

    private parseBatchJobInformation(testBatchJobInfo: TSBatchJobInformation,
                                     batchJobInfoFromServer: any,
    ): TSBatchJobInformation {
        if (!batchJobInfoFromServer) {
            return undefined;
        }

        testBatchJobInfo.batchStatus = batchJobInfoFromServer.batchStatus;
        testBatchJobInfo.createTime = batchJobInfoFromServer.createTime;
        testBatchJobInfo.endTime = batchJobInfoFromServer.endTime;
        testBatchJobInfo.executionId = batchJobInfoFromServer.executionId;
        testBatchJobInfo.jobName = batchJobInfoFromServer.jobName;
        testBatchJobInfo.lastUpdatedTime = batchJobInfoFromServer.lastUpdatedTime;
        testBatchJobInfo.startTime = batchJobInfoFromServer.startTime;

        return testBatchJobInfo;
    }

    public parseMitteilung(tsMitteilung: TSMitteilung, mitteilungFromServer: any): TSMitteilung {
        if (mitteilungFromServer) {
            this.parseAbstractMutableEntity(tsMitteilung, mitteilungFromServer);
            tsMitteilung.dossier = this.parseDossier(new TSDossier(), mitteilungFromServer.dossier);
            if (mitteilungFromServer.betreuung) {
                tsMitteilung.betreuung = this.parseBetreuung(new TSBetreuung(), mitteilungFromServer.betreuung);
            }
            tsMitteilung.senderTyp = mitteilungFromServer.senderTyp;
            tsMitteilung.empfaengerTyp = mitteilungFromServer.empfaengerTyp;
            tsMitteilung.sender = this.parseUser(new TSBenutzer(), mitteilungFromServer.sender);
            tsMitteilung.empfaenger = this.parseUser(new TSBenutzer(), mitteilungFromServer.empfaenger);
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
            this.abstractMutableEntityToRestObject(restMitteilung, tsMitteilung);
            restMitteilung.dossier = this.dossierToRestObject({}, tsMitteilung.dossier);
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

    public parseMitteilungen(mitteilungen: Array<any>): Array<TSMitteilung> {
        if (!Array.isArray(mitteilungen)) {
            return [];
        }

        return mitteilungen.map(m => {
            return this.isBetreuungsmitteilung(m) ?
                this.parseBetreuungsmitteilung(new TSBetreuungsmitteilung(), m) :
                this.parseMitteilung(new TSMitteilung(), m);
        });
    }

    public betreuungsmitteilungToRestObject(restBetreuungsmitteilung: any,
                                            tsBetreuungsmitteilung: TSBetreuungsmitteilung,
    ): any {
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

    public parseBetreuungsmitteilung(tsBetreuungsmitteilung: TSBetreuungsmitteilung,
                                     betreuungsmitteilungFromServer: any,
    ): TSBetreuungsmitteilung {
        if (betreuungsmitteilungFromServer) {
            this.parseMitteilung(tsBetreuungsmitteilung, betreuungsmitteilungFromServer);
            tsBetreuungsmitteilung.applied = betreuungsmitteilungFromServer.applied;
            if (Array.isArray(betreuungsmitteilungFromServer.betreuungspensen)) {
                tsBetreuungsmitteilung.betreuungspensen = betreuungsmitteilungFromServer.betreuungspensen
                    .map((bp: any) => this.parseBetreuungsmitteilungPensum(new TSBetreuungsmitteilungPensum(), bp));
            }
        }
        return tsBetreuungsmitteilung;
    }

    private isBetreuungsmitteilung(mitteilung: any): boolean {
        return mitteilung.betreuungspensen !== undefined;
    }

    public parseZahlungsauftragList(data: any): TSZahlungsauftrag[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseZahlungsauftrag(new TSZahlungsauftrag(), item))
            : [];
    }

    public parseZahlungsauftrag(tsZahlungsauftrag: TSZahlungsauftrag,
                                zahlungsauftragFromServer: any,
    ): TSZahlungsauftrag {
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
        return data && Array.isArray(data)
            ? data.map(item => this.parseZahlung(new TSZahlung(), item))
            : [];
    }

    public parseZahlung(tsZahlung: TSZahlung, zahlungFromServer: any): TSZahlung {
        if (zahlungFromServer) {
            this.parseAbstractMutableEntity(tsZahlung, zahlungFromServer);

            tsZahlung.betragTotalZahlung = zahlungFromServer.betragTotalZahlung;
            tsZahlung.institutionsName = zahlungFromServer.institutionsName;
            tsZahlung.status = zahlungFromServer.status;

            return tsZahlung;
        }
        return undefined;
    }

    public parseEWKResultat(ewkResultatTS: TSEWKResultat, ewkResultatFromServer: any): any {
        if (ewkResultatFromServer) {
            ewkResultatTS.maxResultate = ewkResultatFromServer.maxResultate;
            ewkResultatTS.anzahlResultate = ewkResultatFromServer.anzahlResultate;
            ewkResultatTS.personen = this.parseEWKPersonList(ewkResultatFromServer.personen);
            return ewkResultatTS;
        }
        return undefined;
    }

    private parseEWKPersonList(data: any): TSEWKPerson[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseEWKPerson(new TSEWKPerson(), item))
            : [];
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
        return data && Array.isArray(data)
            ? data.map(item => this.parseEWKEinwohnercode(new TSEWKEinwohnercode(), item))
            : [];
    }

    private parseEWKEinwohnercode(tsEWKEinwohnercode: TSEWKEinwohnercode,
                                  ewkEinwohnercodeFromServer: any,
    ): TSEWKEinwohnercode {
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
        return data && Array.isArray(data)
            ? data.map(item => this.parseEWKAdresse(new TSEWKAdresse(), item))
            : [];
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
        return data && Array.isArray(data)
            ? data.map(item => this.parseEWKBeziehung(new TSEWKBeziehung(), item))
            : [];
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
        return data && Array.isArray(data)
            ? data.map(item => this.parseModulTagesschule(new TSModulTagesschule(), item))
            : [this.parseModulTagesschule(new TSModulTagesschule(), data)];
    }

    private parseModulTagesschule(modulTagesschuleTS: TSModulTagesschule, modulFromServer: any): TSModulTagesschule {
        if (modulFromServer) {
            this.parseAbstractMutableEntity(modulTagesschuleTS, modulFromServer);
            modulTagesschuleTS.modulTagesschuleName = modulFromServer.modulTagesschuleName;
            modulTagesschuleTS.wochentag = modulFromServer.wochentag;
            modulTagesschuleTS.zeitVon = DateUtil.localDateTimeToMoment(modulFromServer.zeitVon);
            modulTagesschuleTS.zeitBis = DateUtil.localDateTimeToMoment(modulFromServer.zeitBis);
            return modulTagesschuleTS;
        }
        return undefined;
    }

    private moduleTagesschuleArrayToRestObject(data: Array<TSModulTagesschule>): any[] {
        return data && Array.isArray(data)
            ? data.map(item => this.modulTagesschuleToRestObject({}, item))
            : [];
    }

    private modulTagesschuleToRestObject(restModul: any, modulTagesschuleTS: TSModulTagesschule): any {
        if (modulTagesschuleTS) {
            this.abstractMutableEntityToRestObject(restModul, modulTagesschuleTS);
            restModul.modulTagesschuleName = modulTagesschuleTS.modulTagesschuleName;
            restModul.wochentag = modulTagesschuleTS.wochentag;
            restModul.zeitVon = DateUtil.momentToLocalDateTime(modulTagesschuleTS.zeitVon);
            restModul.zeitBis = DateUtil.momentToLocalDateTime(modulTagesschuleTS.zeitBis);
            return restModul;
        }
        return undefined;
    }

    private parseBelegungTagesschule(belegungTS: TSBelegungTagesschule,
                                     belegungFromServer: any,
    ): TSBelegungTagesschule {
        if (belegungFromServer) {
            this.parseAbstractMutableEntity(belegungTS, belegungFromServer);
            belegungTS.moduleTagesschule = this.parseModuleTagesschuleArray(belegungFromServer.moduleTagesschule);
            belegungTS.eintrittsdatum = DateUtil.localDateToMoment(belegungFromServer.eintrittsdatum);
            return belegungTS;
        }
        return undefined;
    }

    private belegungTagesschuleToRestObject(restBelegung: any, belegungTS: TSBelegungTagesschule): any {
        if (belegungTS) {
            this.abstractMutableEntityToRestObject(restBelegung, belegungTS);
            restBelegung.moduleTagesschule = this.moduleTagesschuleArrayToRestObject(belegungTS.moduleTagesschule);
            restBelegung.eintrittsdatum = DateUtil.momentToLocalDate(belegungTS.eintrittsdatum);
            return restBelegung;
        }
        return undefined;
    }

    public parseFerieninselStammdatenList(data: any): TSFerieninselStammdaten[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseFerieninselStammdaten(new TSFerieninselStammdaten(), item))
            : [this.parseFerieninselStammdaten(new TSFerieninselStammdaten(), data)];
    }

    public parseFerieninselStammdaten(ferieninselStammdatenTS: TSFerieninselStammdaten,
                                      receivedFerieninselStammdaten: any,
    ): TSFerieninselStammdaten {
        if (receivedFerieninselStammdaten) {
            this.parseAbstractMutableEntity(ferieninselStammdatenTS, receivedFerieninselStammdaten);
            ferieninselStammdatenTS.ferienname = receivedFerieninselStammdaten.ferienname;
            ferieninselStammdatenTS.anmeldeschluss =
                DateUtil.localDateToMoment(receivedFerieninselStammdaten.anmeldeschluss);
            ferieninselStammdatenTS.gesuchsperiode =
                this.parseGesuchsperiode(new TSGesuchsperiode(), receivedFerieninselStammdaten.gesuchsperiode);
            if (receivedFerieninselStammdaten.zeitraumList[0]) {
                const firstZeitraum = new TSFerieninselZeitraum();
                this.parseDateRangeEntity(firstZeitraum, receivedFerieninselStammdaten.zeitraumList[0]);
                ferieninselStammdatenTS.zeitraum = firstZeitraum;
            }
            ferieninselStammdatenTS.zeitraumList = [];
            for (let i = 1; i < receivedFerieninselStammdaten.zeitraumList.length; i++) {
                const zeitraum = new TSFerieninselZeitraum();
                this.parseDateRangeEntity(zeitraum, receivedFerieninselStammdaten.zeitraumList[i]);
                ferieninselStammdatenTS.zeitraumList.push(zeitraum);
            }
            const tage = receivedFerieninselStammdaten.potenzielleFerieninselTageFuerBelegung;
            if (tage) {
                ferieninselStammdatenTS.potenzielleFerieninselTageFuerBelegung =
                    this.parseBelegungFerieninselTagList(tage);
            }
            return ferieninselStammdatenTS;
        }
        return undefined;
    }

    public ferieninselStammdatenToRestObject(restFerieninselStammdaten: any,
                                             ferieninselStammdatenTS: TSFerieninselStammdaten,
    ): any {
        if (ferieninselStammdatenTS) {
            this.abstractMutableEntityToRestObject(restFerieninselStammdaten, ferieninselStammdatenTS);
            restFerieninselStammdaten.ferienname = ferieninselStammdatenTS.ferienname;
            restFerieninselStammdaten.anmeldeschluss =
                DateUtil.momentToLocalDate(ferieninselStammdatenTS.anmeldeschluss);
            restFerieninselStammdaten.gesuchsperiode =
                this.gesuchsperiodeToRestObject({}, ferieninselStammdatenTS.gesuchsperiode);
            if (ferieninselStammdatenTS.zeitraum) {
                const firstZeitraum: any = {};
                this.abstractDateRangeEntityToRestObject(firstZeitraum, ferieninselStammdatenTS.zeitraum);
                restFerieninselStammdaten.zeitraumList = [];
                restFerieninselStammdaten.zeitraumList[0] = firstZeitraum;
            }
            if (ferieninselStammdatenTS.zeitraumList) {
                for (let i = 0; i < ferieninselStammdatenTS.zeitraumList.length; i++) {
                    const zeitraum: any = {};
                    this.abstractDateRangeEntityToRestObject(zeitraum, ferieninselStammdatenTS.zeitraumList[i]);
                    restFerieninselStammdaten.zeitraumList[i + 1] = zeitraum;
                }
            }
            return restFerieninselStammdaten;
        }
        return undefined;
    }

    public parseBelegungFerieninsel(belegungFerieninselTS: TSBelegungFerieninsel,
                                    receivedBelegungFerieninsel: any,
    ): TSBelegungFerieninsel {
        if (receivedBelegungFerieninsel) {
            this.parseAbstractMutableEntity(belegungFerieninselTS, receivedBelegungFerieninsel);
            belegungFerieninselTS.ferienname = receivedBelegungFerieninsel.ferienname;
            belegungFerieninselTS.tage = this.parseBelegungFerieninselTagList(receivedBelegungFerieninsel.tage);
            return belegungFerieninselTS;
        }
        return undefined;
    }

    private parseBelegungFerieninselTagList(data: any): TSBelegungFerieninselTag[] {
        return data && Array.isArray(data)
            ? data.map(item => this.parseBelegungFerieninselTag(new TSBelegungFerieninselTag(), item))
            : [this.parseBelegungFerieninselTag(new TSBelegungFerieninselTag(), data)];
    }

    private parseBelegungFerieninselTag(belegungFerieninselTagTS: TSBelegungFerieninselTag,
                                        receivedBelegungFerieninselTag: any,
    ): TSBelegungFerieninselTag {
        if (receivedBelegungFerieninselTag) {
            this.parseAbstractMutableEntity(belegungFerieninselTagTS, receivedBelegungFerieninselTag);
            belegungFerieninselTagTS.tag = DateUtil.localDateToMoment(receivedBelegungFerieninselTag.tag);
            return belegungFerieninselTagTS;
        }
        return undefined;
    }

    public belegungFerieninselToRestObject(restBelegungFerieninsel: any,
                                           belegungFerieninselTS: TSBelegungFerieninsel,
    ): any {
        if (belegungFerieninselTS) {
            this.abstractMutableEntityToRestObject(restBelegungFerieninsel, belegungFerieninselTS);
            restBelegungFerieninsel.ferienname = belegungFerieninselTS.ferienname;
            restBelegungFerieninsel.tage = [];
            if (Array.isArray(belegungFerieninselTS.tage)) {
                belegungFerieninselTS.tage.forEach(t => {
                    const tagRest: any = {};
                    this.abstractMutableEntityToRestObject(tagRest, t);
                    tagRest.tag = DateUtil.momentToLocalDate(t.tag);
                    restBelegungFerieninsel.tage.push(tagRest);
                });
            }
            return restBelegungFerieninsel;
        }
        return undefined;
    }
}
