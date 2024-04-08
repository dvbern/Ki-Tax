/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import * as moment from 'moment';
import {BenutzerListFilter} from '../admin/component/benutzerListView/dv-benutzer-list/BenutzerListFilter';
import {
    TSFerienbetreuungBerechnung,
} from '../app/gemeinde-antraege/ferienbetreuung/ferienbetreuung-kosten-einnahmen/TSFerienbetreuungBerechnung';
import {TSBenutzerTableFilterDTO} from '../models/dto/TSBenutzerTableFilterDTO';
import {TSDokumenteDTO} from '../models/dto/TSDokumenteDTO';
import {TSFinanzielleSituationAufteilungDTO} from '../models/dto/TSFinanzielleSituationAufteilungDTO';
import {TSFinanzielleSituationResultateDTO} from '../models/dto/TSFinanzielleSituationResultateDTO';
import {TSKitaxResponse} from '../models/dto/TSKitaxResponse';
import {TSQuickSearchResult} from '../models/dto/TSQuickSearchResult';
import {TSSearchResultEntry} from '../models/dto/TSSearchResultEntry';
import {TSAdressetyp} from '../models/enums/TSAdressetyp';
import {TSAnspruchBeschaeftigungAbhaengigkeitTyp} from '../models/enums/TSAnspruchBeschaeftigungAbhaengigkeitTyp';
import {TSBetreuungspensumAbweichungStatus} from '../models/enums/TSBetreuungspensumAbweichungStatus';
import {TSEinschulungTyp} from '../models/enums/TSEinschulungTyp';
import {TSFachstellenTyp} from '../models/enums/TSFachstellenTyp';
import {ferienInselNameOrder} from '../models/enums/TSFerienname';
import {TSFinanzielleSituationTyp} from '../models/enums/TSFinanzielleSituationTyp';
import {TSKinderabzugTyp} from '../models/enums/TSKinderabzugTyp';
import {TSPensumAnzeigeTyp} from '../models/enums/TSPensumAnzeigeTyp';
import {TSGemeindeKennzahlen} from '../models/gemeindeantrag/gemeindekennzahlen/TSGemeindeKennzahlen';
import {TSAnzahlEingeschriebeneKinder} from '../models/gemeindeantrag/TSAnzahlEingeschriebeneKinder';
import {TSDurchschnittKinderProTag} from '../models/gemeindeantrag/TSDurchschnittKinderProTag';
import {TSFerienbetreuungAngaben} from '../models/gemeindeantrag/TSFerienbetreuungAngaben';
import {TSFerienbetreuungAngabenAngebot} from '../models/gemeindeantrag/TSFerienbetreuungAngabenAngebot';
import {TSFerienbetreuungAngabenContainer} from '../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSFerienbetreuungAngabenKostenEinnahmen} from '../models/gemeindeantrag/TSFerienbetreuungAngabenKostenEinnahmen';
import {TSFerienbetreuungAngabenNutzung} from '../models/gemeindeantrag/TSFerienbetreuungAngabenNutzung';
import {TSFerienbetreuungAngabenStammdaten} from '../models/gemeindeantrag/TSFerienbetreuungAngabenStammdaten';
import {TSFerienbetreuungDokument} from '../models/gemeindeantrag/TSFerienbetreuungDokument';
import {TSGemeindeAntrag} from '../models/gemeindeantrag/TSGemeindeAntrag';
import {TSLastenausgleichTagesschuleAngabenGemeinde} from '../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeinde';
import {
    TSLastenausgleichTagesschuleAngabenGemeindeContainer,
} from '../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {
    TSLastenausgleichTagesschuleAngabenInstitution,
} from '../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitution';
import {
    TSLastenausgleichTagesschuleAngabenInstitutionContainer,
} from '../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitutionContainer';
import {TSLastenausgleichTagesschulenStatusHistory} from '../models/gemeindeantrag/TSLastenausgleichTagesschulenStatusHistory';
import {TSOeffnungszeitenTagesschule} from '../models/gemeindeantrag/TSOeffnungszeitenTagesschule';
import {TSKibonAnfrage} from '../models/neskovanp/TSKibonAnfrage';
import {TSSteuerdatenResponse} from '../models/neskovanp/TSSteuerdatenResponse';
import {TSSozialdienst} from '../models/sozialdienst/TSSozialdienst';
import {TSSozialdienstFall} from '../models/sozialdienst/TSSozialdienstFall';
import {TSSozialdienstFallDokument} from '../models/sozialdienst/TSSozialdienstFallDokument';
import {TSSozialdienstStammdaten} from '../models/sozialdienst/TSSozialdienstStammdaten';
import {TSAbstractAntragEntity} from '../models/TSAbstractAntragEntity';
import {TSAbstractDateRangedEntity} from '../models/TSAbstractDateRangedEntity';
import {TSAbstractDecimalPensumEntity} from '../models/TSAbstractDecimalPensumEntity';
import {TSAbstractEntity} from '../models/TSAbstractEntity';
import {TSAbstractFinanzielleSituation} from '../models/TSAbstractFinanzielleSituation';
import {TSAbstractGemeindeStammdaten} from '../models/TSAbstractGemeindeStammdaten';
import {TSAbstractIntegerPensumEntity} from '../models/TSAbstractIntegerPensumEntity';
import {TSAbstractMutableEntity} from '../models/TSAbstractMutableEntity';
import {TSAbstractPersonEntity} from '../models/TSAbstractPersonEntity';
import {TSAbwesenheit} from '../models/TSAbwesenheit';
import {TSAbwesenheitContainer} from '../models/TSAbwesenheitContainer';
import {TSAdresse} from '../models/TSAdresse';
import {TSAdresseContainer} from '../models/TSAdresseContainer';
import {TSAnmeldungDTO} from '../models/TSAnmeldungDTO';
import {TSAnmeldungTagesschuleZeitabschnitt} from '../models/TSAnmeldungTagesschuleZeitabschnitt';
import {TSAntragDTO} from '../models/TSAntragDTO';
import {TSAntragStatusHistory} from '../models/TSAntragStatusHistory';
import {TSApplicationProperty} from '../models/TSApplicationProperty';
import {TSBatchJobInformation} from '../models/TSBatchJobInformation';
import {TSBelegungFerieninsel} from '../models/TSBelegungFerieninsel';
import {TSBelegungFerieninselTag} from '../models/TSBelegungFerieninselTag';
import {TSBelegungTagesschule} from '../models/TSBelegungTagesschule';
import {TSBelegungTagesschuleModul} from '../models/TSBelegungTagesschuleModul';
import {TSBenutzer} from '../models/TSBenutzer';
import {TSBenutzerNoDetails} from '../models/TSBenutzerNoDetails';
import {TSBerechtigung} from '../models/TSBerechtigung';
import {TSBerechtigungHistory} from '../models/TSBerechtigungHistory';
import {TSBetreuung} from '../models/TSBetreuung';
import {TSBetreuungMonitoring} from '../models/TSBetreuungMonitoring';
import {TSBetreuungsmitteilung} from '../models/TSBetreuungsmitteilung';
import {TSBetreuungsmitteilungPensum} from '../models/TSBetreuungsmitteilungPensum';
import {TSBetreuungspensum} from '../models/TSBetreuungspensum';
import {TSBetreuungspensumAbweichung} from '../models/TSBetreuungspensumAbweichung';
import {TSBetreuungspensumContainer} from '../models/TSBetreuungspensumContainer';
import {TSBetreuungsstandort} from '../models/TSBetreuungsstandort';
import {TSBfsGemeinde} from '../models/TSBfsGemeinde';
import {TSDokument} from '../models/TSDokument';
import {TSDokumentGrund} from '../models/TSDokumentGrund';
import {TSDossier} from '../models/TSDossier';
import {TSDownloadFile} from '../models/TSDownloadFile';
import {TSEbeguVorlage} from '../models/TSEbeguVorlage';
import {TSEinkommensverschlechterung} from '../models/TSEinkommensverschlechterung';
import {TSEinkommensverschlechterungContainer} from '../models/TSEinkommensverschlechterungContainer';
import {TSEinkommensverschlechterungInfo} from '../models/TSEinkommensverschlechterungInfo';
import {TSEinkommensverschlechterungInfoContainer} from '../models/TSEinkommensverschlechterungInfoContainer';
import {TSEinstellung} from '../models/TSEinstellung';
import {TSEinstellungenFerieninsel} from '../models/TSEinstellungenFerieninsel';
import {TSEinstellungenTagesschule} from '../models/TSEinstellungenTagesschule';
import {TSErweiterteBetreuung} from '../models/TSErweiterteBetreuung';
import {TSErweiterteBetreuungContainer} from '../models/TSErweiterteBetreuungContainer';
import {TSErwerbspensum} from '../models/TSErwerbspensum';
import {TSErwerbspensumContainer} from '../models/TSErwerbspensumContainer';
import {TSEWKAdresse} from '../models/TSEWKAdresse';
import {TSEWKBeziehung} from '../models/TSEWKBeziehung';
import {TSEWKPerson} from '../models/TSEWKPerson';
import {TSEWKResultat} from '../models/TSEWKResultat';
import {TSExternalClient} from '../models/TSExternalClient';
import {TSExternalClientAssignment} from '../models/TSExternalClientAssignment';
import {TSFachstelle} from '../models/TSFachstelle';
import {TSFall} from '../models/TSFall';
import {TSFallAntragDTO} from '../models/TSFallAntragDTO';
import {TSFamiliensituation} from '../models/TSFamiliensituation';
import {TSFamiliensituationContainer} from '../models/TSFamiliensituationContainer';
import {TSFerieninselStammdaten} from '../models/TSFerieninselStammdaten';
import {TSFerieninselZeitraum} from '../models/TSFerieninselZeitraum';
import {TSFile} from '../models/TSFile';
import {TSFinanzielleSituation} from '../models/TSFinanzielleSituation';
import {TSFinanzielleSituationContainer} from '../models/TSFinanzielleSituationContainer';
import {TSFinanzielleSituationSelbstdeklaration} from '../models/TSFinanzielleSituationSelbstdeklaration';
import {TSFinanzModel} from '../models/TSFinanzModel';
import {TSFinSitZusatzangabenAppenzell} from '../models/TSFinSitZusatzangabenAppenzell';
import {TSGemeinde} from '../models/TSGemeinde';
import {TSGemeindeKonfiguration} from '../models/TSGemeindeKonfiguration';
import {TSGemeindeRegistrierung} from '../models/TSGemeindeRegistrierung';
import {TSGemeindeStammdaten} from '../models/TSGemeindeStammdaten';
import {TSGemeindeStammdatenKorrespondenz} from '../models/TSGemeindeStammdatenKorrespondenz';
import {TSGemeindeStammdatenLite} from '../models/TSGemeindeStammdatenLite';
import {TSGesuch} from '../models/TSGesuch';
import {TSGesuchsperiode} from '../models/TSGesuchsperiode';
import {TSGesuchsteller} from '../models/TSGesuchsteller';
import {TSGesuchstellerContainer} from '../models/TSGesuchstellerContainer';
import {TSInstitution} from '../models/TSInstitution';
import {TSInstitutionExternalClient} from '../models/TSInstitutionExternalClient';
import {TSInstitutionExternalClientAssignment} from '../models/TSInstitutionExternalClientAssignment';
import {TSInstitutionListDTO} from '../models/TSInstitutionListDTO';
import {TSInstitutionStammdaten} from '../models/TSInstitutionStammdaten';
import {TSInstitutionStammdatenBetreuungsgutscheine} from '../models/TSInstitutionStammdatenBetreuungsgutscheine';
import {TSInstitutionStammdatenFerieninsel} from '../models/TSInstitutionStammdatenFerieninsel';
import {TSInstitutionStammdatenSummary} from '../models/TSInstitutionStammdatenSummary';
import {TSInstitutionStammdatenTagesschule} from '../models/TSInstitutionStammdatenTagesschule';
import {TSInstitutionUpdate} from '../models/TSInstitutionUpdate';
import {TSInternePendenz} from '../models/TSInternePendenz';
import {TSKind} from '../models/TSKind';
import {TSKindContainer} from '../models/TSKindContainer';
import {TSKindDublette} from '../models/TSKindDublette';
import {TSLastenausgleich} from '../models/TSLastenausgleich';
import {TSMahnung} from '../models/TSMahnung';
import {TSMandant} from '../models/TSMandant';
import {TSMitteilung} from '../models/TSMitteilung';
import {TSModulTagesschule} from '../models/TSModulTagesschule';
import {TSModulTagesschuleGroup} from '../models/TSModulTagesschuleGroup';
import {TSPendenzBetreuung} from '../models/TSPendenzBetreuung';
import {TSPensumAusserordentlicherAnspruch} from '../models/TSPensumAusserordentlicherAnspruch';
import {TSPensumFachstelle} from '../models/TSPensumFachstelle';
import {TSPublicAppConfig} from '../models/TSPublicAppConfig';
import {TSRueckforderungDokument} from '../models/TSRueckforderungDokument';
import {TSRueckforderungFormular} from '../models/TSRueckforderungFormular';
import {TSRueckforderungMitteilung} from '../models/TSRueckforderungMitteilung';
import {TSSozialhilfeZeitraum} from '../models/TSSozialhilfeZeitraum';
import {TSSozialhilfeZeitraumContainer} from '../models/TSSozialhilfeZeitraumContainer';
import {TSSupportAnfrage} from '../models/TSSupportAnfrage';
import {TSTextRessource} from '../models/TSTextRessource';
import {TSTraegerschaft} from '../models/TSTraegerschaft';
import {TSTsCalculationResult} from '../models/TSTsCalculationResult';
import {TSUnbezahlterUrlaub} from '../models/TSUnbezahlterUrlaub';
import {TSVerfuegung} from '../models/TSVerfuegung';
import {TSVerfuegungZeitabschnitt} from '../models/TSVerfuegungZeitabschnitt';
import {TSVerfuegungZeitabschnittBemerkung} from '../models/TSVerfuegungZeitabschnittBemerkung';
import {TSVorlage} from '../models/TSVorlage';
import {TSWizardStep} from '../models/TSWizardStep';
import {TSWizardStepX} from '../models/TSWizardStepX';
import {TSWorkJob} from '../models/TSWorkJob';
import {TSZahlung} from '../models/TSZahlung';
import {TSZahlungsauftrag} from '../models/TSZahlungsauftrag';
import {TSDateRange} from '../models/types/TSDateRange';
import {TSLand} from '../models/types/TSLand';
import {DateUtil} from './DateUtil';
import {EbeguUtil} from './EbeguUtil';
import {TSAusserordentlicherAnspruchTyp} from '../models/enums/TSAusserordentlicherAnspruchTyp';

export class EbeguRestUtil {

    public constructor() {
    }

    /**
     * Wandelt Data in einen TSApplicationProperty Array um, welches danach zurueckgeliefert wird
     */
    public parseApplicationProperties(data: any): TSApplicationProperty[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseApplicationProperty(new TSApplicationProperty('', ''), item))
            : [this.parseApplicationProperty(new TSApplicationProperty('', ''), data)];
    }

    /**
     * Wandelt die receivedAppProperty in einem parsedAppProperty um.
     */
    public parseApplicationProperty(
        parsedAppProperty: TSApplicationProperty,
        receivedAppProperty: any
    ): TSApplicationProperty {
        this.parseAbstractMutableEntity(parsedAppProperty, receivedAppProperty);
        parsedAppProperty.name = receivedAppProperty.name;
        parsedAppProperty.value = receivedAppProperty.value;
        parsedAppProperty.erklaerung = receivedAppProperty.erklaerung;
        return parsedAppProperty;
    }

    public parseEinstellungList(data: any): TSEinstellung[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseEinstellung(new TSEinstellung(), item))
            : [this.parseEinstellung(new TSEinstellung(), data)];
    }

    public parseEinstellung(tsEinstellung: TSEinstellung, receivedEinstellung: any): TSEinstellung {
        if (receivedEinstellung) {
            this.parseDateRangeEntity(tsEinstellung, receivedEinstellung);
            tsEinstellung.key = receivedEinstellung.key;
            tsEinstellung.value = receivedEinstellung.value;
            tsEinstellung.erklaerung = receivedEinstellung.erklaerung;
            tsEinstellung.gemeindeId = receivedEinstellung.gemeindeId;
            tsEinstellung.gesuchsperiodeId = receivedEinstellung.gesuchsperiodeId;
            // Mandant wird aktuell nicht gemappt
            return tsEinstellung;
        }
        return undefined;
    }

    private einstellungListToRestObject(einstellungListTS: Array<TSEinstellung>): Array<any> {
        return einstellungListTS
            ? einstellungListTS.map(item => this.einstellungToRestObject({}, item))
            : [];
    }

    public einstellungToRestObject(restEinstellung: any, tsEinstellung: TSEinstellung): TSEinstellung {
        if (tsEinstellung) {
            this.abstractDateRangeEntityToRestObject(restEinstellung, tsEinstellung);
            restEinstellung.key = tsEinstellung.key;
            restEinstellung.value = tsEinstellung.value;
            restEinstellung.erklaerung = tsEinstellung.erklaerung;
            restEinstellung.gemeindeId = tsEinstellung.gemeindeId;
            restEinstellung.gesuchsperiodeId = tsEinstellung.gesuchsperiodeId;
            // Mandant wird aktuell nicht gemappt
            return restEinstellung;
        }
        return undefined;
    }

    public parseEbeguVorlages(data: any): TSEbeguVorlage[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseEbeguVorlage(new TSEbeguVorlage(), item))
            : [this.parseEbeguVorlage(new TSEbeguVorlage(), data)];
    }

    public parseEbeguVorlage(ebeguVorlageTS: TSEbeguVorlage, receivedEbeguVorlage: any): TSEbeguVorlage {
        if (receivedEbeguVorlage) {
            this.parseDateRangeEntity(ebeguVorlageTS, receivedEbeguVorlage);
            ebeguVorlageTS.name = receivedEbeguVorlage.name;
            ebeguVorlageTS.vorlage = this.parseVorlage(new TSVorlage(), receivedEbeguVorlage.vorlage);
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
        parsedAbstractEntity.version = receivedAbstractEntity.version;
        parsedAbstractEntity.timestampErstellt =
            DateUtil.localDateTimeToMoment(receivedAbstractEntity.timestampErstellt);
        parsedAbstractEntity.timestampMutiert = DateUtil.localDateTimeToMoment(receivedAbstractEntity.timestampMutiert);
    }

    private abstractEntityToRestObject(restObject: any, typescriptObject: TSAbstractEntity): void {
        restObject.id = typescriptObject.id;
        restObject.version = typescriptObject.version;
        if (typescriptObject.timestampErstellt) {
            restObject.timestampErstellt = DateUtil.momentToLocalDateTime(typescriptObject.timestampErstellt);
        }
        if (typescriptObject.timestampMutiert) {
            restObject.timestampMutiert = DateUtil.momentToLocalDateTime(typescriptObject.timestampMutiert);
        }
    }

    private parseAbstractMutableEntity(
        parsedAbstractEntity: TSAbstractMutableEntity,
        receivedAbstractEntity: any
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

    private abstractPensumEntityToRestObject(restObj: any, pensumEntity: TSAbstractIntegerPensumEntity): void {
        this.abstractDateRangeEntityToRestObject(restObj, pensumEntity);
        restObj.pensum = pensumEntity.pensum;
    }

    private abstractBetreuungspensumEntityToRestObject(
        restObj: any,
        betreuungspensumEntity: TSAbstractDecimalPensumEntity
    ): void {
        this.abstractDateRangeEntityToRestObject(restObj, betreuungspensumEntity);
        restObj.unitForDisplay = betreuungspensumEntity.unitForDisplay;
        restObj.pensum = betreuungspensumEntity.pensum;
        restObj.monatlicheBetreuungskosten = betreuungspensumEntity.monatlicheBetreuungskosten;
        restObj.stuendlicheVollkosten = betreuungspensumEntity.stuendlicheVollkosten;
    }

    private parseAbstractPensumEntity(
        betreuungspensumTS: TSAbstractIntegerPensumEntity,
        betreuungspensumFromServer: any
    ): void {
        this.parseDateRangeEntity(betreuungspensumTS, betreuungspensumFromServer);
        betreuungspensumTS.pensum = betreuungspensumFromServer.pensum;
    }

    private parseAbstractBetreuungspensumEntity(
        betreuungspensumTS: TSAbstractDecimalPensumEntity,
        betreuungspensumFromServer: any
    ): void {
        this.parseDateRangeEntity(betreuungspensumTS, betreuungspensumFromServer);
        betreuungspensumTS.unitForDisplay = betreuungspensumFromServer.unitForDisplay;
        betreuungspensumTS.pensum = betreuungspensumFromServer.pensum;
        betreuungspensumTS.monatlicheBetreuungskosten = betreuungspensumFromServer.monatlicheBetreuungskosten;
        betreuungspensumTS.stuendlicheVollkosten = betreuungspensumFromServer.stuendlicheVollkosten;
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
        restObj.begruendungMutation = antragEntity.begruendungMutation;
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
        antragTS.begruendungMutation = antragFromServer.begruendungMutation;
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
            restAdresse.bfsNummer = adresse.bfsNummer;
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
            adresseTS.bfsNummer = receivedAdresse.bfsNummer;
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
            return new TSLand(landCode, translationKey);
        }
        return undefined;
    }

    /**
     * Fügt das 'Land_' dem eingegebenen Landcode hinzu.
     */
    public landCodeToTSLandCode(landCode: string): string {
        return landCode && landCode.lastIndexOf('Land_', 0) !== 0 ? `Land_${  landCode}` : undefined;
    }

    public gesuchstellerToRestObject(restGesuchsteller: any, gesuchsteller: TSGesuchsteller): any {
        if (gesuchsteller) {
            this.abstractPersonEntitytoRestObject(restGesuchsteller, gesuchsteller);
            restGesuchsteller.mail = gesuchsteller.mail || undefined;
            restGesuchsteller.mobile = gesuchsteller.mobile || undefined;
            restGesuchsteller.telefon = gesuchsteller.telefon || undefined;
            restGesuchsteller.telefonAusland = gesuchsteller.telefonAusland || undefined;
            restGesuchsteller.diplomatenstatus = gesuchsteller.diplomatenstatus;
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
            gesuchstellerTS.korrespondenzSprache = gesuchstellerFromServer.korrespondenzSprache;
            return gesuchstellerTS;
        }
        return undefined;
    }

    public parseErwerbspensumContainer(
        erwerbspensumContainer: TSErwerbspensumContainer,
        ewpContFromServer: any
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

    public erwerbspensumContainerToRestObject(
        restEwpContainer: any,
        erwerbspensumContainer: TSErwerbspensumContainer
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
            erwerbspensum.erwerbspensumInstitution = erwerbspensumFromServer.erwerbspensumInstitution;
            erwerbspensum.bezeichnung = erwerbspensumFromServer.bezeichnung;
            erwerbspensum.unregelmaessigeArbeitszeiten = erwerbspensumFromServer.unregelmaessigeArbeitszeiten;
            erwerbspensum.unbezahlterUrlaub = this.parseUnbezahlterUrlaub(
                new TSUnbezahlterUrlaub(), erwerbspensumFromServer.unbezahlterUrlaub);
            return erwerbspensum;
        }
        return undefined;
    }

    public erwerbspensumToRestObject(restErwerbspensum: any, erwerbspensum: TSErwerbspensum): any {
        if (erwerbspensum) {
            this.abstractPensumEntityToRestObject(restErwerbspensum, erwerbspensum);
            restErwerbspensum.taetigkeit = erwerbspensum.taetigkeit;
            restErwerbspensum.erwerbspensumInstitution = erwerbspensum.erwerbspensumInstitution;
            restErwerbspensum.bezeichnung = erwerbspensum.bezeichnung;
            restErwerbspensum.unregelmaessigeArbeitszeiten = erwerbspensum.unregelmaessigeArbeitszeiten;
            restErwerbspensum.unbezahlterUrlaub = this.unbezahlterUrlaubToRestObject(
                {}, erwerbspensum.unbezahlterUrlaub);
            return restErwerbspensum;
        }
        return undefined;
    }

    public parseUnbezahlterUrlaub(tsUrlaub: TSUnbezahlterUrlaub, urlaubFromServer: any): TSUnbezahlterUrlaub {
        if (urlaubFromServer) {
            this.parseDateRangeEntity(tsUrlaub, urlaubFromServer);
            return tsUrlaub;
        }
        return undefined;
    }

    public unbezahlterUrlaubToRestObject(restUrlaub: any, tsUrlaub: TSUnbezahlterUrlaub): any {
        if (tsUrlaub) {
            this.abstractDateRangeEntityToRestObject(restUrlaub, tsUrlaub);
            return restUrlaub;
        }
        return undefined;
    }

    public familiensituationToRestObject(
        restFamiliensituation: any,
        familiensituation: TSFamiliensituation
    ): TSFamiliensituation {
        if (familiensituation) {
            this.abstractMutableEntityToRestObject(restFamiliensituation, familiensituation);
            restFamiliensituation.familienstatus = familiensituation.familienstatus;
            restFamiliensituation.gemeinsameSteuererklaerung = familiensituation.gemeinsameSteuererklaerung;
            restFamiliensituation.aenderungPer = DateUtil.momentToLocalDate(familiensituation.aenderungPer);
            restFamiliensituation.startKonkubinat = DateUtil.momentToLocalDate(familiensituation.startKonkubinat);
            restFamiliensituation.sozialhilfeBezueger = familiensituation.sozialhilfeBezueger;
            restFamiliensituation.zustaendigeAmtsstelle = familiensituation.zustaendigeAmtsstelle;
            restFamiliensituation.nameBetreuer = familiensituation.nameBetreuer;
            restFamiliensituation.verguenstigungGewuenscht =
                familiensituation.verguenstigungGewuenscht;
            restFamiliensituation.keineMahlzeitenverguenstigungBeantragt =
                familiensituation.keineMahlzeitenverguenstigungBeantragt;
            // keineMahlzeitenverguenstigungBeantragtEditable wird nie vom Client zurueckgenommen
            restFamiliensituation.iban = familiensituation.iban;
            restFamiliensituation.kontoinhaber = familiensituation.kontoinhaber;
            restFamiliensituation.abweichendeZahlungsadresse =
                familiensituation.abweichendeZahlungsadresse;
            restFamiliensituation.zahlungsadresse =
                this.adresseToRestObject({}, familiensituation.zahlungsadresse);
            restFamiliensituation.infomaKreditorennummer = familiensituation.infomaKreditorennummer;
            restFamiliensituation.infomaBankcode = familiensituation.infomaBankcode;
            restFamiliensituation.gesuchstellerKardinalitaet = familiensituation.gesuchstellerKardinalitaet;
            restFamiliensituation.fkjvFamSit = familiensituation.fkjvFamSit;
            restFamiliensituation.minDauerKonkubinat = familiensituation.minDauerKonkubinat;
            restFamiliensituation.geteilteObhut = familiensituation.geteilteObhut;
            restFamiliensituation.unterhaltsvereinbarung = familiensituation.unterhaltsvereinbarung;
            restFamiliensituation.unterhaltsvereinbarungBemerkung = familiensituation.unterhaltsvereinbarungBemerkung;
            restFamiliensituation.partnerIdentischMitVorgesuch = familiensituation.partnerIdentischMitVorgesuch;
            restFamiliensituation.gemeinsamerHaushaltMitObhutsberechtigterPerson =
                familiensituation.gemeinsamerHaushaltMitObhutsberechtigterPerson;
            restFamiliensituation.gemeinsamerHaushaltMitPartner = familiensituation.gemeinsamerHaushaltMitPartner;
            restFamiliensituation.auszahlungAusserhalbVonKibon = familiensituation.auszahlungAusserhalbVonKibon;
            return restFamiliensituation;
        }

        return undefined;
    }

    public einkommensverschlechterungInfoContainerToRestObject(
        restEinkommensverschlechterungInfoContainer: any,
        einkommensverschlechterungInfoContainer: TSEinkommensverschlechterungInfoContainer
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
        einkommensverschlechterungInfo: TSEinkommensverschlechterungInfo
    ): TSEinkommensverschlechterungInfo {
        if (einkommensverschlechterungInfo) {
            this.abstractMutableEntityToRestObject(restEinkommensverschlechterungInfo, einkommensverschlechterungInfo);
            restEinkommensverschlechterungInfo.einkommensverschlechterung =
                einkommensverschlechterungInfo.einkommensverschlechterung;
            restEinkommensverschlechterungInfo.ekvFuerBasisJahrPlus1 =
                einkommensverschlechterungInfo.ekvFuerBasisJahrPlus1;
            restEinkommensverschlechterungInfo.ekvFuerBasisJahrPlus2 =
                einkommensverschlechterungInfo.ekvFuerBasisJahrPlus2;
            restEinkommensverschlechterungInfo.ekvBasisJahrPlus1Annulliert =
                einkommensverschlechterungInfo.ekvBasisJahrPlus1Annulliert;
            restEinkommensverschlechterungInfo.ekvBasisJahrPlus2Annulliert =
                einkommensverschlechterungInfo.ekvBasisJahrPlus2Annulliert;
            return restEinkommensverschlechterungInfo;
        }
        return undefined;
    }

    public parseFamiliensituation(
        familiensituation: TSFamiliensituation,
        familiensituationFromServer: any
    ): TSFamiliensituation {
        if (familiensituationFromServer) {
            this.parseAbstractMutableEntity(familiensituation, familiensituationFromServer);
            familiensituation.familienstatus = familiensituationFromServer.familienstatus;
            familiensituation.gemeinsameSteuererklaerung = familiensituationFromServer.gemeinsameSteuererklaerung;
            familiensituation.aenderungPer = DateUtil.localDateToMoment(familiensituationFromServer.aenderungPer);
            familiensituation.startKonkubinat = DateUtil.localDateToMoment(familiensituationFromServer.startKonkubinat);
            familiensituation.sozialhilfeBezueger = familiensituationFromServer.sozialhilfeBezueger;
            familiensituation.zustaendigeAmtsstelle = familiensituationFromServer.zustaendigeAmtsstelle;
            familiensituation.nameBetreuer = familiensituationFromServer.nameBetreuer;
            familiensituation.verguenstigungGewuenscht =
                familiensituationFromServer.verguenstigungGewuenscht;
            familiensituation.keineMahlzeitenverguenstigungBeantragt =
                familiensituationFromServer.keineMahlzeitenverguenstigungBeantragt;
            familiensituation.keineMahlzeitenverguenstigungBeantragtEditable =
                familiensituationFromServer.keineMahlzeitenverguenstigungBeantragtEditable;
            familiensituation.iban = familiensituationFromServer.iban;
            familiensituation.kontoinhaber = familiensituationFromServer.kontoinhaber;
            familiensituation.abweichendeZahlungsadresse =
                familiensituationFromServer.abweichendeZahlungsadresse;
            familiensituation.zahlungsadresse =
                this.parseAdresse(new TSAdresse(), familiensituationFromServer.zahlungsadresse);
            familiensituation.infomaKreditorennummer = familiensituationFromServer.infomaKreditorennummer;
            familiensituation.infomaBankcode = familiensituationFromServer.infomaBankcode;
            familiensituation.gesuchstellerKardinalitaet = familiensituationFromServer.gesuchstellerKardinalitaet;
            familiensituation.fkjvFamSit = familiensituationFromServer.fkjvFamSit;
            familiensituation.minDauerKonkubinat = familiensituationFromServer.minDauerKonkubinat;
            familiensituation.geteilteObhut = familiensituationFromServer.geteilteObhut;
            familiensituation.unterhaltsvereinbarung = familiensituationFromServer.unterhaltsvereinbarung;
            familiensituation.unterhaltsvereinbarungBemerkung =
                familiensituationFromServer.unterhaltsvereinbarungBemerkung;
            familiensituation.partnerIdentischMitVorgesuch = familiensituationFromServer.partnerIdentischMitVorgesuch;
            familiensituation.gemeinsamerHaushaltMitObhutsberechtigterPerson =
                familiensituationFromServer.gemeinsamerHaushaltMitObhutsberechtigterPerson;
            familiensituation.gemeinsamerHaushaltMitPartner = familiensituationFromServer.gemeinsamerHaushaltMitPartner;
            familiensituation.auszahlungAusserhalbVonKibon = familiensituationFromServer.auszahlungAusserhalbVonKibon;
            return familiensituation;
        }
        return undefined;
    }

    public parseFamiliensituationContainer(
        containerTS: TSFamiliensituationContainer,
        containerFromServer: any
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
            containerTS.sozialhilfeZeitraumContainers =
                this.parseSozialhilfeZeitraumContainers(containerFromServer.sozialhilfeZeitraumContainers);

            return containerTS;
        }
        return undefined;
    }

    public familiensituationContainerToRestObject(
        restFamiliensituationContainer: any,
        familiensituationContainer: TSFamiliensituationContainer
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
            restFamiliensituationContainer.sozialhilfeZeitraumContainers = [];
            if (Array.isArray(familiensituationContainer.sozialhilfeZeitraumContainers)) {
                restFamiliensituationContainer.sozialhilfeZeitraumContainers =
                    familiensituationContainer.sozialhilfeZeitraumContainers.map(
                        szc => this.sozialhilfeZeitraumContainerToRestObject({}, szc));
            }

            return restFamiliensituationContainer;
        }
        return undefined;
    }

    public sozialhilfeZeitraumContainerToRestObject(
        restSozialhilfeZeitraumContainer: any,
        sozialhilfeZeitraumContainer: TSSozialhilfeZeitraumContainer
    ): any {
        if (sozialhilfeZeitraumContainer) {
            this.abstractMutableEntityToRestObject(restSozialhilfeZeitraumContainer, sozialhilfeZeitraumContainer);
            restSozialhilfeZeitraumContainer.sozialhilfeZeitraumGS =
                this.sozialhilfeZeitraumToRestObject({}, sozialhilfeZeitraumContainer.sozialhilfeZeitraumGS);
            restSozialhilfeZeitraumContainer.sozialhilfeZeitraumJA =
                this.sozialhilfeZeitraumToRestObject({}, sozialhilfeZeitraumContainer.sozialhilfeZeitraumJA);
            return restSozialhilfeZeitraumContainer;
        }
        return undefined;
    }

    public sozialhilfeZeitraumToRestObject(
        restSozialhilfeZeitraum: any,
        sozialhilfeZeitraum: TSSozialhilfeZeitraum
    ): any {
        if (sozialhilfeZeitraum) {
            this.abstractDateRangeEntityToRestObject(restSozialhilfeZeitraum, sozialhilfeZeitraum);
            return restSozialhilfeZeitraum;
        }
        return undefined;
    }

    private parseSozialhilfeZeitraumContainers(data: Array<any>): TSSozialhilfeZeitraumContainer[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseSozialhilfeZeitraumContainer(new TSSozialhilfeZeitraumContainer(), item))
            : [this.parseSozialhilfeZeitraumContainer(new TSSozialhilfeZeitraumContainer(), data)];
    }

    public parseSozialhilfeZeitraumContainer(
        sozialhilfeZeitraumContainer: TSSozialhilfeZeitraumContainer,
        sozialhilfeZeitraumContFromServer: any
    ): TSSozialhilfeZeitraumContainer {
        if (sozialhilfeZeitraumContFromServer) {
            this.parseAbstractMutableEntity(sozialhilfeZeitraumContainer, sozialhilfeZeitraumContFromServer);
            sozialhilfeZeitraumContainer.sozialhilfeZeitraumGS =
                this.parseSozialhilfeZeitraum(sozialhilfeZeitraumContainer.sozialhilfeZeitraumGS || new TSSozialhilfeZeitraum(),
                    sozialhilfeZeitraumContFromServer.sozialhilfeZeitraumGS);
            sozialhilfeZeitraumContainer.sozialhilfeZeitraumJA =
                this.parseSozialhilfeZeitraum(sozialhilfeZeitraumContainer.sozialhilfeZeitraumJA || new TSSozialhilfeZeitraum(),
                    sozialhilfeZeitraumContFromServer.sozialhilfeZeitraumJA);
            return sozialhilfeZeitraumContainer;
        }
        return undefined;
    }

    public parseSozialhilfeZeitraum(
        sozialhilfeZeitraum: TSSozialhilfeZeitraum,
        sozialhilfeZeitraumFromServer: any
    ): TSSozialhilfeZeitraum {
        if (sozialhilfeZeitraumFromServer) {
            this.parseDateRangeEntity(sozialhilfeZeitraum, sozialhilfeZeitraumFromServer);
            return sozialhilfeZeitraum;
        }
        return undefined;
    }

    public parseEinkommensverschlechterungInfo(
        einkommensverschlechterungInfo: TSEinkommensverschlechterungInfo,
        einkommensverschlechterungInfoFromServer: any
    ): TSEinkommensverschlechterungInfo {
        if (einkommensverschlechterungInfoFromServer) {
            this.parseAbstractMutableEntity(einkommensverschlechterungInfo, einkommensverschlechterungInfoFromServer);
            einkommensverschlechterungInfo.einkommensverschlechterung =
                einkommensverschlechterungInfoFromServer.einkommensverschlechterung;
            einkommensverschlechterungInfo.ekvFuerBasisJahrPlus1 =
                einkommensverschlechterungInfoFromServer.ekvFuerBasisJahrPlus1;
            einkommensverschlechterungInfo.ekvFuerBasisJahrPlus2 =
                einkommensverschlechterungInfoFromServer.ekvFuerBasisJahrPlus2;
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
        containerFromServer: any
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
            restFall.sozialdienstFall = this.sozialdienstFallToRestObject({}, fall.sozialdienstFall);
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
            fallTS.sozialdienstFall =
                this.parseSozialdienstFall(new TSSozialdienstFall(), fallFromServer.sozialdienstFall);
            return fallTS;
        }
        return undefined;
    }

    private gemeindeListToRestObject(gemeindeListTS: Array<TSGemeinde>): Array<any> {
        return gemeindeListTS
            ? gemeindeListTS
                .map(item => this.gemeindeToRestObject({}, item))
                .filter(gmde => EbeguUtil.isNotNullOrUndefined(gmde))
            : [];
    }

    public parseGemeindeList(data: any): TSGemeinde[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseGemeinde(new TSGemeinde(), item))
            : [this.parseGemeinde(new TSGemeinde(), data)];
    }

    public gemeindeToRestObject(restGemeinde: any, gemeinde: TSGemeinde): any {
        if (gemeinde) {
            this.abstractEntityToRestObject(restGemeinde, gemeinde);
            restGemeinde.name = gemeinde.name;
            restGemeinde.status = gemeinde.status;
            restGemeinde.gemeindeNummer = gemeinde.gemeindeNummer;
            restGemeinde.bfsNummer = gemeinde.bfsNummer;
            restGemeinde.betreuungsgutscheineStartdatum = DateUtil
                .momentToLocalDate(gemeinde.betreuungsgutscheineStartdatum);
            restGemeinde.tagesschulanmeldungenStartdatum = DateUtil
                .momentToLocalDate(gemeinde.tagesschulanmeldungenStartdatum);
            restGemeinde.ferieninselanmeldungenStartdatum = DateUtil
                .momentToLocalDate(gemeinde.ferieninselanmeldungenStartdatum);
            restGemeinde.gueltigBis = gemeinde.gueltigBis === null ? '9999-12-31' :
                DateUtil.momentToLocalDate(gemeinde.gueltigBis);
            restGemeinde.angebotBG = gemeinde.angebotBG;
            restGemeinde.angebotBGTFO = gemeinde.angebotBGTFO;
            restGemeinde.angebotTS = gemeinde.angebotTS;
            restGemeinde.angebotFI = gemeinde.angebotFI;
            restGemeinde.besondereVolksschule = gemeinde.besondereVolksschule;
            restGemeinde.nurLats = gemeinde.nurLats;
            restGemeinde.infomaZahlungen = gemeinde.infomaZahlungen;
            return restGemeinde;
        }
        return undefined;
    }

    public parseGemeinde(gemeindeTS: TSGemeinde, gemeindeFromServer: any): TSGemeinde | undefined {
        if (gemeindeFromServer) {
            this.parseAbstractEntity(gemeindeTS, gemeindeFromServer);
            gemeindeTS.name = gemeindeFromServer.name;
            gemeindeTS.status = gemeindeFromServer.status;
            gemeindeTS.gemeindeNummer = gemeindeFromServer.gemeindeNummer;
            gemeindeTS.bfsNummer = gemeindeFromServer.bfsNummer;
            gemeindeTS.betreuungsgutscheineStartdatum = DateUtil
                .localDateToMoment(gemeindeFromServer.betreuungsgutscheineStartdatum);
            gemeindeTS.tagesschulanmeldungenStartdatum = DateUtil
                .localDateToMoment(gemeindeFromServer.tagesschulanmeldungenStartdatum);
            gemeindeTS.ferieninselanmeldungenStartdatum = DateUtil
                .localDateToMoment(gemeindeFromServer.ferieninselanmeldungenStartdatum);
            gemeindeTS.gueltigBis = gemeindeFromServer.gueltigBis === '9999-12-31' ? null :
                DateUtil.localDateToMoment(gemeindeFromServer.gueltigBis);
            gemeindeTS.angebotBG = gemeindeFromServer.angebotBG;
            gemeindeTS.angebotBGTFO = gemeindeFromServer.angebotBGTFO;
            gemeindeTS.angebotTS = gemeindeFromServer.angebotTS;
            gemeindeTS.angebotFI = gemeindeFromServer.angebotFI;
            gemeindeTS.besondereVolksschule = gemeindeFromServer.besondereVolksschule;
            gemeindeTS.nurLats = gemeindeFromServer.nurLats;
            gemeindeTS.key = gemeindeFromServer.key;
            gemeindeTS.infomaZahlungen = gemeindeFromServer.infomaZahlungen;

            return gemeindeTS;
        }
        return undefined;
    }

    public parseBfsGemeindeList(data: any): TSBfsGemeinde[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseBfsGemeinde(new TSBfsGemeinde(), item))
            : [this.parseBfsGemeinde(new TSBfsGemeinde(), data)];
    }

    public parseBfsGemeinde(gemeindeTS: TSBfsGemeinde, gemeindeFromServer: any): TSBfsGemeinde {
        if (gemeindeFromServer) {
            gemeindeTS.name = gemeindeFromServer.name;
            gemeindeTS.bfsNummer = gemeindeFromServer.bfsNummer;
            return gemeindeTS;
        }
        return undefined;
    }

    public bfsGemeindeListToRestObject(gemeinden: TSBfsGemeinde[]): any[] {
        return gemeinden
            ? gemeinden.map(item => this.bfsGemeindeToRestObject({}, item))
            : [];
    }

    public bfsGemeindeToRestObject(restGemeinde: any, tsGemeinde: TSBfsGemeinde): any {
        this.abstractEntityToRestObject(restGemeinde, tsGemeinde);
        restGemeinde.name = tsGemeinde.name;
        restGemeinde.bfsNummer = tsGemeinde.bfsNummer;
        return restGemeinde;
    }

    // eslint-disable-next-line
    public gemeindeStammdatenToRestObject(restStammdaten: any, stammdaten: TSGemeindeStammdaten): TSGemeindeStammdaten {
        if (stammdaten) {
            this.abstractEntityToRestObject(restStammdaten, stammdaten);

            restStammdaten.defaultBenutzerBG = this.userToRestObject({}, stammdaten.defaultBenutzerBG);
            restStammdaten.defaultBenutzerTS = this.userToRestObject({}, stammdaten.defaultBenutzerTS);
            restStammdaten.defaultBenutzer = this.userToRestObject({}, stammdaten.defaultBenutzer);
            restStammdaten.gemeinde = this.gemeindeToRestObject({}, stammdaten.gemeinde);
            restStammdaten.adresse = this.adresseToRestObject({}, stammdaten.adresse);
            restStammdaten.bgAdresse = this.adresseToRestObject({}, stammdaten.bgAdresse);
            restStammdaten.tsAdresse = this.adresseToRestObject({}, stammdaten.tsAdresse);
            restStammdaten.bgEmail = (stammdaten.bgEmail) ? stammdaten.bgEmail : null;
            restStammdaten.bgTelefon = (stammdaten.bgTelefon) ? stammdaten.bgTelefon : null;
            restStammdaten.tsEmail = (stammdaten.tsEmail) ? stammdaten.tsEmail : null;
            restStammdaten.tsTelefon = (stammdaten.tsTelefon) ? stammdaten.tsTelefon : null;
            if (stammdaten.gemeinde.angebotBG) {
                restStammdaten.beschwerdeAdresse = this.adresseToRestObject({}, stammdaten.beschwerdeAdresse);
            }
            restStammdaten.gemeindeStammdatenKorrespondenz = this.gemeindeStammdatenKorrespondenzToRestObject(
                {},
                stammdaten.gemeindeStammdatenKorrespondenz);
            restStammdaten.mail = stammdaten.mail;
            restStammdaten.telefon = stammdaten.telefon;
            restStammdaten.webseite = stammdaten.webseite;
            restStammdaten.korrespondenzspracheDe = stammdaten.korrespondenzspracheDe;
            restStammdaten.korrespondenzspracheFr = stammdaten.korrespondenzspracheFr;
            restStammdaten.konfigurationsListe =
                this.gemeindeKonfigurationListToRestObject(stammdaten.konfigurationsListe);
            restStammdaten.kontoinhaber = stammdaten.kontoinhaber;
            restStammdaten.bic = stammdaten.bic;
            restStammdaten.iban = stammdaten.iban;
            restStammdaten.standardRechtsmittelbelehrung = stammdaten.standardRechtsmittelbelehrung;
            restStammdaten.benachrichtigungBgEmailAuto = stammdaten.benachrichtigungBgEmailAuto;
            restStammdaten.benachrichtigungTsEmailAuto = stammdaten.benachrichtigungTsEmailAuto;
            restStammdaten.standardDokSignature = stammdaten.standardDokSignature;
            restStammdaten.standardDokTitle = stammdaten.standardDokTitle;
            restStammdaten.standardDokUnterschriftTitel = stammdaten.standardDokUnterschriftTitel;
            restStammdaten.standardDokUnterschriftName = stammdaten.standardDokUnterschriftName;
            restStammdaten.standardDokUnterschriftTitel2 = stammdaten.standardDokUnterschriftTitel2;
            restStammdaten.standardDokUnterschriftName2 = stammdaten.standardDokUnterschriftName2;
            restStammdaten.tsVerantwortlicherNachVerfuegungBenachrichtigen =
                stammdaten.tsVerantwortlicherNachVerfuegungBenachrichtigen;
            restStammdaten.externalClients = stammdaten.externalClients || null;
            restStammdaten.usernameScolaris = stammdaten.usernameScolaris;
            restStammdaten.emailBeiGesuchsperiodeOeffnung = stammdaten.emailBeiGesuchsperiodeOeffnung;
            restStammdaten.hasAltGemeindeKontakt = stammdaten.hasAltGemeindeKontakt;
            restStammdaten.altGemeindeKontaktText = stammdaten.altGemeindeKontaktText;
            restStammdaten.zusatzTextVerfuegung = stammdaten.zusatzTextVerfuegung;
            restStammdaten.hasZusatzTextVerfuegung = stammdaten.hasZusatzTextVerfuegung;
            restStammdaten.zusatzTextFreigabequittung = stammdaten.zusatzTextFreigabequittung;
            restStammdaten.hasZusatzTextFreigabequittung = stammdaten.hasZusatzTextFreigabequittung;

            if (stammdaten.rechtsmittelbelehrung) {
                restStammdaten.rechtsmittelbelehrung =
                    this.textRessourceToRestObject({}, stammdaten.rechtsmittelbelehrung);
            }
            restStammdaten.gutscheinSelberAusgestellt = stammdaten.gutscheinSelberAusgestellt;
            if (stammdaten.gemeindeAusgabestelle) {
                restStammdaten.gemeindeAusgabestelle = this.gemeindeToRestObject({}, stammdaten.gemeindeAusgabestelle);
            }
            restStammdaten.alleBgInstitutionenZugelassen = stammdaten.alleBgInstitutionenZugelassen;
            restStammdaten.zugelasseneBgInstitutionen = stammdaten.zugelasseneBgInstitutionen.map(
                i => this.institutionToRestObject({}, i)
            );

            return restStammdaten;
        }
        return undefined;
    }

    public parseGemeindeStammdaten(
        stammdatenTS: TSGemeindeStammdaten,
        stammdatenFromServer: any
    ): TSGemeindeStammdaten {
        if (stammdatenFromServer) {
            this.parseAbstractGemeindeStammdaten(stammdatenTS, stammdatenFromServer);

            stammdatenTS.administratoren = stammdatenFromServer.administratoren;
            stammdatenTS.sachbearbeiter = stammdatenFromServer.sachbearbeiter;
            stammdatenTS.defaultBenutzerBG = this.parseUser(new TSBenutzer(), stammdatenFromServer.defaultBenutzerBG);
            stammdatenTS.defaultBenutzerTS = this.parseUser(new TSBenutzer(), stammdatenFromServer.defaultBenutzerTS);
            stammdatenTS.defaultBenutzer = this.parseUser(new TSBenutzer(), stammdatenFromServer.defaultBenutzer);
            stammdatenTS.gemeinde = this.parseGemeinde(new TSGemeinde(), stammdatenFromServer.gemeinde);
            stammdatenTS.adresse = this.parseAdresse(new TSAdresse(), stammdatenFromServer.adresse);
            stammdatenTS.beschwerdeAdresse = this.parseAdresse(new TSAdresse(), stammdatenFromServer.beschwerdeAdresse);
            stammdatenTS.gemeindeStammdatenKorrespondenz = this.parseGemeindeStammdatenKorrespondenz(
                new TSGemeindeStammdatenKorrespondenz(),
                stammdatenFromServer.gemeindeStammdatenKorrespondenz);
            stammdatenTS.mail = stammdatenFromServer.mail;
            stammdatenTS.telefon = stammdatenFromServer.telefon;
            stammdatenTS.webseite = stammdatenFromServer.webseite;
            stammdatenTS.korrespondenzspracheDe = stammdatenFromServer.korrespondenzspracheDe;
            stammdatenTS.korrespondenzspracheFr = stammdatenFromServer.korrespondenzspracheFr;
            stammdatenTS.benutzerListeBG = this.parseUserList(stammdatenFromServer.benutzerListeBG);
            stammdatenTS.benutzerListeTS = this.parseUserList(stammdatenFromServer.benutzerListeTS);
            stammdatenTS.konfigurationsListe =
                this.parseGemeindeKonfigurationList(stammdatenFromServer.konfigurationsListe);
            stammdatenTS.kontoinhaber = stammdatenFromServer.kontoinhaber;
            stammdatenTS.bic = stammdatenFromServer.bic;
            stammdatenTS.iban = stammdatenFromServer.iban;
            stammdatenTS.standardRechtsmittelbelehrung = stammdatenFromServer.standardRechtsmittelbelehrung;
            if (stammdatenFromServer.rechtsmittelbelehrung) {
                stammdatenTS.rechtsmittelbelehrung =
                    this.parseTextRessource(
                        new TSTextRessource(),
                        stammdatenFromServer.rechtsmittelbelehrung
                    );
            }
            stammdatenTS.bgAdresse = this.parseAdresse(new TSAdresse(), stammdatenFromServer.bgAdresse);
            stammdatenTS.tsAdresse = this.parseAdresse(new TSAdresse(), stammdatenFromServer.tsAdresse);
            stammdatenTS.bgEmail = stammdatenFromServer.bgEmail;
            stammdatenTS.bgTelefon = stammdatenFromServer.bgTelefon;
            stammdatenTS.tsEmail = stammdatenFromServer.tsEmail;
            stammdatenTS.tsTelefon = stammdatenFromServer.tsTelefon;
            stammdatenTS.benachrichtigungBgEmailAuto = stammdatenFromServer.benachrichtigungBgEmailAuto;
            stammdatenTS.benachrichtigungTsEmailAuto = stammdatenFromServer.benachrichtigungTsEmailAuto;
            stammdatenTS.standardDokSignature = stammdatenFromServer.standardDokSignature;
            stammdatenTS.standardDokTitle = stammdatenFromServer.standardDokTitle;
            stammdatenTS.standardDokUnterschriftTitel = stammdatenFromServer.standardDokUnterschriftTitel;
            stammdatenTS.standardDokUnterschriftName = stammdatenFromServer.standardDokUnterschriftName;
            stammdatenTS.standardDokUnterschriftTitel2 = stammdatenFromServer.standardDokUnterschriftTitel2;
            stammdatenTS.standardDokUnterschriftName2 = stammdatenFromServer.standardDokUnterschriftName2;
            stammdatenTS.tsVerantwortlicherNachVerfuegungBenachrichtigen =
                stammdatenFromServer.tsVerantwortlicherNachVerfuegungBenachrichtigen;
            stammdatenTS.usernameScolaris = stammdatenFromServer.usernameScolaris;
            stammdatenTS.emailBeiGesuchsperiodeOeffnung = stammdatenFromServer.emailBeiGesuchsperiodeOeffnung;
            stammdatenTS.gutscheinSelberAusgestellt = stammdatenFromServer.gutscheinSelberAusgestellt;
            stammdatenTS.zusatzTextVerfuegung = stammdatenFromServer.zusatzTextVerfuegung;
            stammdatenTS.hasZusatzTextVerfuegung = stammdatenFromServer.hasZusatzTextVerfuegung;
            stammdatenTS.zusatzTextFreigabequittung = stammdatenFromServer.zusatzTextFreigabequittung;
            stammdatenTS.hasZusatzTextFreigabequittung = stammdatenFromServer.hasZusatzTextFreigabequittung;
            if (stammdatenFromServer.gemeindeAusgabestelle) {
                stammdatenTS.gemeindeAusgabestelle =
                    this.parseGemeinde(new TSGemeinde(), stammdatenFromServer.gemeindeAusgabestelle);
            }
            stammdatenTS.alleBgInstitutionenZugelassen = stammdatenFromServer.alleBgInstitutionenZugelassen;
            stammdatenTS.zugelasseneBgInstitutionen = this.parseInstitutionen(stammdatenFromServer.zugelasseneBgInstitutionen);
            return stammdatenTS;
        }
        return undefined;
    }

    public parseGemeindeStammdatenLite(
        tsGemeindeStammdatenLite: TSGemeindeStammdatenLite,
        stammdatenFromServer: any
    ): TSGemeindeStammdatenLite {
        if (stammdatenFromServer) {
            this.parseAbstractGemeindeStammdaten(tsGemeindeStammdatenLite, stammdatenFromServer);
            tsGemeindeStammdatenLite.gemeindeName = stammdatenFromServer.gemeindeName;
            return tsGemeindeStammdatenLite;
        }
        return undefined;
    }

    private parseAbstractGemeindeStammdaten(
        tsAbstractGemeindeStammdaten: TSAbstractGemeindeStammdaten,
        stammdatenFromServer: any
    ): void {
        this.parseAbstractEntity(tsAbstractGemeindeStammdaten, stammdatenFromServer);
        tsAbstractGemeindeStammdaten.adresse = this.parseAdresse(new TSAdresse(), stammdatenFromServer.adresse);
        tsAbstractGemeindeStammdaten.konfigurationsListe =
            this.parseGemeindeKonfigurationList(stammdatenFromServer.konfigurationsListe);
        tsAbstractGemeindeStammdaten.mail = stammdatenFromServer.mail;
        tsAbstractGemeindeStammdaten.telefon = stammdatenFromServer.telefon;
        tsAbstractGemeindeStammdaten.webseite = stammdatenFromServer.webseite;
        tsAbstractGemeindeStammdaten.korrespondenzspracheDe = stammdatenFromServer.korrespondenzspracheDe;
        tsAbstractGemeindeStammdaten.korrespondenzspracheFr = stammdatenFromServer.korrespondenzspracheFr;
        tsAbstractGemeindeStammdaten.hasAltGemeindeKontakt = stammdatenFromServer.hasAltGemeindeKontakt;
        tsAbstractGemeindeStammdaten.altGemeindeKontaktText = stammdatenFromServer.altGemeindeKontaktText;
    }

    public gemeindeStammdatenKorrespondenzToRestObject(
        restStammdaten: any,
        stammdaten: TSGemeindeStammdatenKorrespondenz
    ): TSGemeindeStammdatenKorrespondenz {
        if (stammdaten) {
            this.abstractEntityToRestObject(restStammdaten, stammdaten);
            restStammdaten.senderAddressSpacingLeft = stammdaten.senderAddressSpacingLeft;
            restStammdaten.senderAddressSpacingTop = stammdaten.senderAddressSpacingTop;
            restStammdaten.receiverAddressSpacingLeft = stammdaten.receiverAddressSpacingLeft;
            restStammdaten.receiverAddressSpacingTop = stammdaten.receiverAddressSpacingTop;
            restStammdaten.logoWidth = stammdaten.logoWidth;
            restStammdaten.logoSpacingLeft = stammdaten.logoSpacingLeft;
            restStammdaten.logoSpacingTop = stammdaten.logoSpacingTop;
            restStammdaten.standardSignatur = stammdaten.standardSignatur;
            return restStammdaten;
        }
        return undefined;
    }

    public parseGemeindeStammdatenKorrespondenz(
        stammdatenTS: TSGemeindeStammdatenKorrespondenz,
        stammdatenFromServer: any
    ): TSGemeindeStammdatenKorrespondenz {
        if (stammdatenFromServer) {
            this.parseAbstractEntity(stammdatenTS, stammdatenFromServer);
            stammdatenTS.senderAddressSpacingLeft = stammdatenFromServer.senderAddressSpacingLeft;
            stammdatenTS.senderAddressSpacingTop = stammdatenFromServer.senderAddressSpacingTop;
            stammdatenTS.receiverAddressSpacingLeft = stammdatenFromServer.receiverAddressSpacingLeft;
            stammdatenTS.receiverAddressSpacingTop = stammdatenFromServer.receiverAddressSpacingTop;
            stammdatenTS.logoWidth = stammdatenFromServer.logoWidth;
            stammdatenTS.logoSpacingLeft = stammdatenFromServer.logoSpacingLeft;
            stammdatenTS.logoSpacingTop = stammdatenFromServer.logoSpacingTop;
            stammdatenTS.standardSignatur = stammdatenFromServer.standardSignatur;
            stammdatenTS.hasAlternativeLogoTagesschule = stammdatenFromServer.hasAlternativeLogoTagesschule;
            return stammdatenTS;
        }
        return undefined;
    }

    private gemeindeKonfigurationListToRestObject(konfigurationListTS: Array<TSGemeindeKonfiguration>): Array<any> {
        return konfigurationListTS
            ? konfigurationListTS.map(item => this.gemeindeKonfigurationToRestObject({}, item))
            : [];
    }

    public gemeindeKonfigurationToRestObject(
        restKonfiguration: any,
        konfiguration: TSGemeindeKonfiguration
    ): TSGemeindeKonfiguration {
        if (konfiguration) {
            restKonfiguration.gesuchsperiode = this.gesuchsperiodeToRestObject({}, konfiguration.gesuchsperiode);
            restKonfiguration.konfigurationen = this.einstellungListToRestObject(konfiguration.konfigurationen);
            restKonfiguration.ferieninselStammdaten =
                this.ferieninselStammdatenListToRestObject(konfiguration.ferieninselStammdaten);
            return restKonfiguration;
        }
        return undefined;
    }

    public parseGemeindeKonfigurationList(data: any): TSGemeindeKonfiguration[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseGemeindeKonfiguration(new TSGemeindeKonfiguration(), item))
            : [this.parseGemeindeKonfiguration(new TSGemeindeKonfiguration(), data)];
    }

    public parseGemeindeKonfiguration(
        konfigurationTS: TSGemeindeKonfiguration,
        konfigurationFromServer: any
    ): TSGemeindeKonfiguration {
        if (konfigurationFromServer) {
            konfigurationTS.erwerbspensumZuschlagMax =
                konfigurationFromServer.erwerbspensumZuschlagMax;
            konfigurationTS.erwerbspensumMiminumVorschuleMax =
                konfigurationFromServer.erwerbspensumMiminumVorschuleMax;
            konfigurationTS.erwerbspensumMiminumSchulkinderMax =
                konfigurationFromServer.erwerbspensumMiminumSchulkinderMax;
            konfigurationTS.gesuchsperiodeName =
                konfigurationFromServer.gesuchsperiodeName;
            konfigurationTS.gesuchsperiodeStatusName =
                konfigurationFromServer.gesuchsperiodeStatusName;
            konfigurationTS.gesuchsperiode =
                this.parseGesuchsperiode(new TSGesuchsperiode(), konfigurationFromServer.gesuchsperiode);
            konfigurationTS.konfigurationen =
                this.parseEinstellungList(konfigurationFromServer.konfigurationen);
            konfigurationTS.ferieninselStammdaten =
                this.parseFerieninselStammdatenList(konfigurationFromServer.ferieninselStammdaten);
            return konfigurationTS;
        }
        return undefined;
    }

    public dossierToRestObject(restDossier: any, dossier: TSDossier): TSDossier {
        if (dossier) {
            this.abstractMutableEntityToRestObject(restDossier, dossier);
            restDossier.fall = this.fallToRestObject({}, dossier.fall);
            restDossier.gemeinde = this.gemeindeToRestObject({}, dossier.gemeinde);
            restDossier.verantwortlicherBG = this.benutzerNoDetailsToRestObject({}, dossier.verantwortlicherBG);
            restDossier.verantwortlicherTS = this.benutzerNoDetailsToRestObject({}, dossier.verantwortlicherTS);
            restDossier.bemerkungen = dossier.bemerkungen;
            return restDossier;
        }
        return undefined;
    }

    public parseDossierList(data: any): TSDossier[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseDossier(new TSDossier(), item))
            : [this.parseDossier(new TSDossier(), data)];
    }

    public parseDossier(dossierTS: TSDossier, dossierFromServer: any): TSDossier {
        if (dossierFromServer) {
            this.parseAbstractMutableEntity(dossierTS, dossierFromServer);
            dossierTS.fall = this.parseFall(new TSFall(), dossierFromServer.fall);
            dossierTS.gemeinde = this.parseGemeinde(new TSGemeinde(), dossierFromServer.gemeinde);
            dossierTS.verantwortlicherBG =
                this.parseUserNoDetails(new TSBenutzerNoDetails(), dossierFromServer.verantwortlicherBG);
            dossierTS.verantwortlicherTS =
                this.parseUserNoDetails(new TSBenutzerNoDetails(), dossierFromServer.verantwortlicherTS);
            dossierTS.bemerkungen = dossierFromServer.bemerkungen;
            return dossierTS;
        }
        return undefined;
    }

    public alwaysEditablePropertiesToRestObject(restProperties: any, gesuch: TSGesuch): any {
        if (gesuch.gesuchsteller1 && gesuch.gesuchsteller1.gesuchstellerJA) {
            restProperties.gesuchId = gesuch.id;
            restProperties.mailGS1 = gesuch.gesuchsteller1.gesuchstellerJA.mail;
            restProperties.mobileGS1 = gesuch.gesuchsteller1.gesuchstellerJA.mobile;
            restProperties.telefonGS1 = gesuch.gesuchsteller1.gesuchstellerJA.telefon;
            restProperties.telefonAuslandGS1 = gesuch.gesuchsteller1.gesuchstellerJA.telefonAusland;
        }
        if (gesuch.gesuchsteller2 && gesuch.gesuchsteller2.gesuchstellerJA) {
            restProperties.mailGS2 = gesuch.gesuchsteller2.gesuchstellerJA.mail;
            restProperties.mobileGS2 = gesuch.gesuchsteller2.gesuchstellerJA.mobile;
            restProperties.telefonGS2 = gesuch.gesuchsteller2.gesuchstellerJA.telefon;
            restProperties.telefonAuslandGS2 = gesuch.gesuchsteller2.gesuchstellerJA.telefonAusland;
        }

        if (gesuch.familiensituationContainer && gesuch.familiensituationContainer.familiensituationJA) {
            restProperties.keineMahlzeitenverguenstigungBeantragt =
                gesuch.familiensituationContainer.familiensituationJA.keineMahlzeitenverguenstigungBeantragt;
            restProperties.iban = gesuch.familiensituationContainer.familiensituationJA.iban;
            restProperties.kontoinhaber = gesuch.familiensituationContainer.familiensituationJA.kontoinhaber;
            restProperties.abweichendeZahlungsadresse =
                gesuch.familiensituationContainer.familiensituationJA.abweichendeZahlungsadresse;
            restProperties.zahlungsadresse =
                this.adresseToRestObject({},
                    gesuch.familiensituationContainer.familiensituationJA.zahlungsadresse);
        }

        return restProperties;
    }

    public gesuchToRestObject(restGesuch: any, gesuch: TSGesuch): any {
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
        restGesuch.verfuegungEingeschrieben = gesuch.verfuegungEingeschrieben;
        restGesuch.gesperrtWegenBeschwerde = gesuch.gesperrtWegenBeschwerde;
        restGesuch.datumGewarntNichtFreigegeben = DateUtil.momentToLocalDate(gesuch.datumGewarntNichtFreigegeben);
        restGesuch.datumGewarntFehlendeQuittung = DateUtil.momentToLocalDate(gesuch.datumGewarntFehlendeQuittung);
        restGesuch.timestampVerfuegt = DateUtil.momentToLocalDateTime(gesuch.timestampVerfuegt);
        restGesuch.gueltig = gesuch.gueltig;
        restGesuch.dokumenteHochgeladen = gesuch.dokumenteHochgeladen;
        restGesuch.finSitStatus = gesuch.finSitStatus;
        restGesuch.finSitTyp = gesuch.finSitTyp;
        restGesuch.finSitAenderungGueltigAbDatum = DateUtil.momentToLocalDate(gesuch.finSitAenderungGueltigAbDatum);
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
            gesuchTS.verfuegungEingeschrieben = gesuchFromServer.verfuegungEingeschrieben;
            gesuchTS.gesperrtWegenBeschwerde = gesuchFromServer.gesperrtWegenBeschwerde;
            gesuchTS.datumGewarntNichtFreigegeben =
                DateUtil.localDateToMoment(gesuchFromServer.datumGewarntNichtFreigegeben);
            gesuchTS.datumGewarntFehlendeQuittung =
                DateUtil.localDateToMoment(gesuchFromServer.datumGewarntFehlendeQuittung);
            gesuchTS.timestampVerfuegt = DateUtil.localDateTimeToMoment(gesuchFromServer.timestampVerfuegt);
            gesuchTS.gueltig = gesuchFromServer.gueltig;
            gesuchTS.dokumenteHochgeladen = gesuchFromServer.dokumenteHochgeladen;
            gesuchTS.finSitStatus = gesuchFromServer.finSitStatus;
            gesuchTS.finSitTyp = gesuchFromServer.finSitTyp;
            gesuchTS.finSitAenderungGueltigAbDatum = DateUtil.localDateToMoment(gesuchFromServer.finSitAenderungGueltigAbDatum);
            gesuchTS.markiertFuerKontroll = gesuchFromServer.markiertFuerKontroll;
            return gesuchTS;
        }
        return undefined;
    }

    public fachstelleToRestObject(restFachstelle: any, fachstelle: TSFachstelle): any {
        this.abstractMutableEntityToRestObject(restFachstelle, fachstelle);
        restFachstelle.name = fachstelle.name;
        restFachstelle.fachstelleAnspruch = fachstelle.fachstelleAnspruch;
        restFachstelle.fachstelleErweiterteBetreuung = fachstelle.fachstelleErweiterteBetreuung;
        return restFachstelle;
    }

    public parseFachstellen(data: any): TSFachstelle[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseFachstelle(new TSFachstelle(), item))
            : [this.parseFachstelle(new TSFachstelle(), data)];
    }

    public parseFachstelle(parsedFachstelle: TSFachstelle, receivedFachstelle: any): TSFachstelle {
        this.parseAbstractMutableEntity(parsedFachstelle, receivedFachstelle);
        parsedFachstelle.name = receivedFachstelle.name;
        parsedFachstelle.fachstelleAnspruch = receivedFachstelle.fachstelleAnspruch;
        parsedFachstelle.fachstelleErweiterteBetreuung = receivedFachstelle.fachstelleErweiterteBetreuung;
        return parsedFachstelle;
    }

    public mandantToRestObject(restMandant: any, mandant: TSMandant): any {
        if (mandant) {
            this.abstractMutableEntityToRestObject(restMandant, mandant);
            restMandant.name = mandant.name;
            restMandant.mandantIdentifier = mandant.mandantIdentifier;
            return restMandant;
        }
        return undefined;
    }

    public parseMandant(mandantTS: TSMandant, mandantFromServer: any): TSMandant {
        if (mandantFromServer) {
            this.parseAbstractMutableEntity(mandantTS, mandantFromServer);
            mandantTS.name = mandantFromServer.name;
            mandantTS.mandantIdentifier = mandantFromServer.mandantIdentifier;
            return mandantTS;
        }
        return undefined;
    }

    public traegerschaftToRestObject(restTragerschaft: any, traegerschaft: TSTraegerschaft): any {
        if (traegerschaft) {
            this.abstractMutableEntityToRestObject(restTragerschaft, traegerschaft);
            restTragerschaft.name = traegerschaft.name;
            restTragerschaft.active = traegerschaft.active;
            restTragerschaft.email = traegerschaft.email;
            return restTragerschaft;
        }
        return undefined;
    }

    public parseTraegerschaften(data: Array<any>): TSTraegerschaft[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseTraegerschaft(new TSTraegerschaft(), item))
            : [this.parseTraegerschaft(new TSTraegerschaft(), data)];
    }

    public parseTraegerschaft(traegerschaftTS: TSTraegerschaft, traegerschaftFromServer: any): TSTraegerschaft {
        if (traegerschaftFromServer) {
            this.parseAbstractMutableEntity(traegerschaftTS, traegerschaftFromServer);
            traegerschaftTS.name = traegerschaftFromServer.name;
            traegerschaftTS.active = traegerschaftFromServer.active;
            traegerschaftTS.email = traegerschaftFromServer.email;
            traegerschaftTS.institutionCount = traegerschaftFromServer.institutionCount;
            traegerschaftTS.institutionNames = traegerschaftFromServer.institutionNames;
            return traegerschaftTS;
        }
        return undefined;
    }

    public institutionUpdateToRestObject(update: TSInstitutionUpdate): any {
        return {
            name: update.name || null,
            traegerschaftId: update.traegerschaftId || null,
            stammdaten: this.institutionStammdatenToRestObject({}, update.stammdaten) || null,
            institutionExternalClients: this.institutionExternalClientListToRestObject(update.institutionExternalClients)
        };
    }

    public institutionToRestObject(restInstitution: any, institution: TSInstitution): any {
        if (institution) {
            this.abstractMutableEntityToRestObject(restInstitution, institution);
            restInstitution.name = institution.name;
            restInstitution.mandant = this.mandantToRestObject({}, institution.mandant);
            restInstitution.traegerschaft = this.traegerschaftToRestObject({}, institution.traegerschaft);
            restInstitution.status = institution.status;
            restInstitution.stammdatenCheckRequired = institution.stammdatenCheckRequired;
            return restInstitution;
        }
        return undefined;
    }

    public parseInstitution<T extends TSInstitution>(institutionTS: T, institutionFromServer: any): T {
        if (institutionFromServer) {
            this.parseAbstractMutableEntity(institutionTS, institutionFromServer);
            institutionTS.name = institutionFromServer.name;
            institutionTS.mandant = this.parseMandant(new TSMandant(), institutionFromServer.mandant);
            institutionTS.traegerschaft =
                this.parseTraegerschaft(new TSTraegerschaft(), institutionFromServer.traegerschaft);
            institutionTS.status = institutionFromServer.status;
            institutionTS.stammdatenCheckRequired = institutionFromServer.stammdatenCheckRequired;

            if (institutionTS instanceof TSInstitutionListDTO) {
                institutionTS.betreuungsangebotTyp = institutionFromServer.betreuungsangebotTyp;
                institutionTS.gemeinde = this.parseGemeinde(new TSGemeinde(), institutionFromServer.gemeinde);
            }

            return institutionTS;
        }
        return undefined;
    }

    public parseInstitutionen(data: Array<any>): TSInstitution[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseInstitution(new TSInstitution(), item))
            : [this.parseInstitution(new TSInstitution(), data)];
    }

    public parseInstitutionenListDTO(data: Array<any>): TSInstitutionListDTO[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseInstitution(new TSInstitutionListDTO(), item))
            : [this.parseInstitution(new TSInstitutionListDTO(), data)];
    }

    public parseExternalClientAssignment(data: any): TSExternalClientAssignment {
        const tsInstitutionExternalClients = new TSExternalClientAssignment();

        tsInstitutionExternalClients.availableClients = data.availableClients
            .map((client: any) => this.parseExternalClient(client));

        tsInstitutionExternalClients.assignedClients = data.assignedClients
            .map((client: any) => this.parseExternalClient(client));

        return tsInstitutionExternalClients;
    }

    public parseExternalClientList(data: Array<any>): TSExternalClient[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseExternalClient(item))
            : [this.parseExternalClient(data)];
    }

    public parseExternalClient(data: any): TSExternalClient {
        const tsExternalClient = new TSExternalClient();
        this.parseAbstractEntity(tsExternalClient, data);
        tsExternalClient.clientName = data.clientName;
        tsExternalClient.type = data.type;

        return tsExternalClient;
    }

    public institutionStammdatenToRestObject(
        restInstitutionStammdaten: any,
        institutionStammdaten: TSInstitutionStammdatenSummary
    ): any {
        if (institutionStammdaten) {
            this.abstractDateRangeEntityToRestObject(restInstitutionStammdaten, institutionStammdaten);
            restInstitutionStammdaten.betreuungsangebotTyp = institutionStammdaten.betreuungsangebotTyp;
            restInstitutionStammdaten.institution = this.institutionToRestObject({}, institutionStammdaten.institution);
            restInstitutionStammdaten.adresse = this.adresseToRestObject({}, institutionStammdaten.adresse);
            restInstitutionStammdaten.mail = institutionStammdaten.mail;
            restInstitutionStammdaten.telefon = institutionStammdaten.telefon;
            restInstitutionStammdaten.webseite = institutionStammdaten.webseite;
            restInstitutionStammdaten.sendMailWennOffenePendenzen = institutionStammdaten.sendMailWennOffenePendenzen;
            restInstitutionStammdaten.grundSchliessung = institutionStammdaten.grundSchliessung;
            restInstitutionStammdaten.erinnerungMail = institutionStammdaten.erinnerungMail;

            restInstitutionStammdaten.institutionStammdatenBetreuungsgutscheine =
                this.institutionStammdatenBetreuungsgutscheineToRestObject({},
                    institutionStammdaten.institutionStammdatenBetreuungsgutscheine);
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

    private parseInstitutionStammdatenSummary(
        institutionStammdatenTS: TSInstitutionStammdatenSummary,
        institutionStammdatenFromServer: any
    ): TSInstitutionStammdatenSummary {
        if (institutionStammdatenFromServer) {
            this.parseDateRangeEntity(institutionStammdatenTS, institutionStammdatenFromServer);
            institutionStammdatenTS.betreuungsangebotTyp = institutionStammdatenFromServer.betreuungsangebotTyp;
            institutionStammdatenTS.institution =
                this.parseInstitution(new TSInstitution(), institutionStammdatenFromServer.institution);
            institutionStammdatenTS.adresse =
                this.parseAdresse(new TSAdresse(), institutionStammdatenFromServer.adresse);
            institutionStammdatenTS.mail = institutionStammdatenFromServer.mail;
            institutionStammdatenTS.telefon = institutionStammdatenFromServer.telefon;
            institutionStammdatenTS.webseite = institutionStammdatenFromServer.webseite;
            institutionStammdatenTS.oeffnungszeiten = institutionStammdatenFromServer.oeffnungszeiten;
            institutionStammdatenTS.sendMailWennOffenePendenzen =
                institutionStammdatenFromServer.sendMailWennOffenePendenzen;
            institutionStammdatenTS.erinnerungMail = institutionStammdatenFromServer.erinnerungMail;
            institutionStammdatenTS.grundSchliessung = institutionStammdatenFromServer.grundSchliessung;

            institutionStammdatenTS.institutionStammdatenBetreuungsgutscheine =
                this.parseInstitutionStammdatenBetreuungsgutscheine(new TSInstitutionStammdatenBetreuungsgutscheine(),
                    institutionStammdatenFromServer.institutionStammdatenBetreuungsgutscheine);
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

    public parseInstitutionStammdaten(
        institutionStammdatenTS: TSInstitutionStammdaten,
        institutionStammdatenFromServer: any
    ): TSInstitutionStammdaten {
        if (institutionStammdatenFromServer) {
            this.parseInstitutionStammdatenSummary(institutionStammdatenTS, institutionStammdatenFromServer);
            institutionStammdatenTS.administratoren = institutionStammdatenFromServer.administratoren;
            institutionStammdatenTS.sachbearbeiter = institutionStammdatenFromServer.sachbearbeiter;
            return institutionStammdatenTS;
        }
        return undefined;
    }

    public parseInstitutionStammdatenArray(data: Array<any>): TSInstitutionStammdaten[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseInstitutionStammdaten(new TSInstitutionStammdaten(), item))
            : [this.parseInstitutionStammdaten(new TSInstitutionStammdaten(), data)];
    }

    private institutionStammdatenBetreuungsgutscheineToRestObject(
        restInstitutionStammdaten: any,
        institutionStammdaten: TSInstitutionStammdatenBetreuungsgutscheine
    ): any {
        if (institutionStammdaten) {
            this.abstractEntityToRestObject(restInstitutionStammdaten, institutionStammdaten);
            restInstitutionStammdaten.iban = institutionStammdaten.iban;
            restInstitutionStammdaten.kontoinhaber = institutionStammdaten.kontoinhaber;
            restInstitutionStammdaten.alterskategorieBaby = institutionStammdaten.alterskategorieBaby;
            restInstitutionStammdaten.alterskategorieVorschule = institutionStammdaten.alterskategorieVorschule;
            restInstitutionStammdaten.alterskategorieKindergarten = institutionStammdaten.alterskategorieKindergarten;
            restInstitutionStammdaten.alterskategorieSchule = institutionStammdaten.alterskategorieSchule;
            restInstitutionStammdaten.anzahlPlaetze = institutionStammdaten.anzahlPlaetze;
            restInstitutionStammdaten.anzahlPlaetzeFirmen = institutionStammdaten.anzahlPlaetzeFirmen;
            restInstitutionStammdaten.adresseKontoinhaber =
                this.adresseToRestObject({}, institutionStammdaten.adresseKontoinhaber);
            restInstitutionStammdaten.tarifProHauptmahlzeit = institutionStammdaten.tarifProHauptmahlzeit;
            restInstitutionStammdaten.tarifProNebenmahlzeit = institutionStammdaten.tarifProNebenmahlzeit;
            restInstitutionStammdaten.betreuungsstandorte =
                this.betreuungsstandortListToRestObject(institutionStammdaten.betreuungsstandorte);
            restInstitutionStammdaten.oeffnungstage = institutionStammdaten.oeffnungstage.getActiveDaysAsList();
            restInstitutionStammdaten.offenVon = institutionStammdaten.offenVon;
            restInstitutionStammdaten.offenBis = institutionStammdaten.offenBis;
            restInstitutionStammdaten.oeffnungsAbweichungen = institutionStammdaten.oeffnungsAbweichungen;
            restInstitutionStammdaten.alternativeEmailFamilienportal =
                (institutionStammdaten.alternativeEmailFamilienportal) ?
                    institutionStammdaten.alternativeEmailFamilienportal :
                    null;
            restInstitutionStammdaten.oeffnungstageProJahr = institutionStammdaten.oeffnungstageProJahr;
            restInstitutionStammdaten.auslastungInstitutionen = institutionStammdaten.auslastungInstitutionen;
            restInstitutionStammdaten.anzahlKinderWarteliste = institutionStammdaten.anzahlKinderWarteliste;
            restInstitutionStammdaten.summePensumWarteliste = institutionStammdaten.summePensumWarteliste;
            restInstitutionStammdaten.dauerWarteliste = institutionStammdaten.dauerWarteliste;
            restInstitutionStammdaten.fruehEroeffnung = institutionStammdaten.fruehEroeffnung;
            restInstitutionStammdaten.spaetEroeffnung = institutionStammdaten.spaetEroeffnung;
            restInstitutionStammdaten.wochenendeEroeffnung = institutionStammdaten.wochenendeEroeffnung;
            restInstitutionStammdaten.uebernachtungMoeglich = institutionStammdaten.uebernachtungMoeglich;
            restInstitutionStammdaten.infomaKreditorennummer = institutionStammdaten.infomaKreditorennummer;
            restInstitutionStammdaten.infomaBankcode = institutionStammdaten.infomaBankcode;

            return restInstitutionStammdaten;
        }
        return undefined;
    }

    private betreuungsstandortListToRestObject(betreuungsstandorte: Array<TSBetreuungsstandort>): Array<any> {
        return betreuungsstandorte
            ? betreuungsstandorte.map(item => this.betreuungsstandortToRestObject({}, item))
            : [];
    }

    private betreuungsstandortToRestObject(restStandort: any, standort: TSBetreuungsstandort): any {
        this.abstractEntityToRestObject(restStandort, standort);
        restStandort.adresse = this.adresseToRestObject({}, standort.adresse);
        restStandort.mail = standort.mail;
        restStandort.telefon = standort.telefon;
        restStandort.webseite = standort.webseite;
        return restStandort;
    }

    private parseInstitutionStammdatenBetreuungsgutscheine(
        institutionStammdatenTS: TSInstitutionStammdatenBetreuungsgutscheine,
        institutionStammdatenFromServer: any
    ): TSInstitutionStammdatenBetreuungsgutscheine {
        if (institutionStammdatenFromServer) {
            this.parseAbstractEntity(institutionStammdatenTS, institutionStammdatenFromServer);
            institutionStammdatenTS.iban = institutionStammdatenFromServer.iban;
            institutionStammdatenTS.kontoinhaber = institutionStammdatenFromServer.kontoinhaber;
            institutionStammdatenTS.alterskategorieBaby = institutionStammdatenFromServer.alterskategorieBaby;
            institutionStammdatenTS.alterskategorieVorschule = institutionStammdatenFromServer.alterskategorieVorschule;
            institutionStammdatenTS.alterskategorieKindergarten =
                institutionStammdatenFromServer.alterskategorieKindergarten;
            institutionStammdatenTS.alterskategorieSchule = institutionStammdatenFromServer.alterskategorieSchule;
            institutionStammdatenTS.anzahlPlaetze = institutionStammdatenFromServer.anzahlPlaetze;
            institutionStammdatenTS.anzahlPlaetzeFirmen = institutionStammdatenFromServer.anzahlPlaetzeFirmen;
            institutionStammdatenTS.adresseKontoinhaber =
                this.parseAdresse(new TSAdresse(), institutionStammdatenFromServer.adresseKontoinhaber);
            institutionStammdatenTS.tarifProHauptmahlzeit = institutionStammdatenFromServer.tarifProHauptmahlzeit;
            institutionStammdatenTS.tarifProNebenmahlzeit = institutionStammdatenFromServer.tarifProNebenmahlzeit;
            for (const day of institutionStammdatenFromServer.oeffnungstage) {
                institutionStammdatenTS.oeffnungstage.setValueForDay(day, true);
            }
            institutionStammdatenTS.offenVon = institutionStammdatenFromServer.offenVon;
            institutionStammdatenTS.offenBis = institutionStammdatenFromServer.offenBis;
            institutionStammdatenTS.oeffnungsAbweichungen = institutionStammdatenFromServer.oeffnungsAbweichungen;
            institutionStammdatenTS.alternativeEmailFamilienportal =
                institutionStammdatenFromServer.alternativeEmailFamilienportal;
            institutionStammdatenTS.betreuungsstandorte =
                this.parseBetreuungsstandortList(institutionStammdatenFromServer.betreuungsstandorte);
            institutionStammdatenTS.oeffnungstageProJahr = institutionStammdatenFromServer.oeffnungstageProJahr;
            institutionStammdatenTS.auslastungInstitutionen = institutionStammdatenFromServer.auslastungInstitutionen;
            institutionStammdatenTS.anzahlKinderWarteliste = institutionStammdatenFromServer.anzahlKinderWarteliste;
            institutionStammdatenTS.summePensumWarteliste = institutionStammdatenFromServer.summePensumWarteliste;
            institutionStammdatenTS.dauerWarteliste = institutionStammdatenFromServer.dauerWarteliste;
            institutionStammdatenTS.fruehEroeffnung = institutionStammdatenFromServer.fruehEroeffnung;
            institutionStammdatenTS.spaetEroeffnung = institutionStammdatenFromServer.spaetEroeffnung;
            institutionStammdatenTS.wochenendeEroeffnung = institutionStammdatenFromServer.wochenendeEroeffnung;
            institutionStammdatenTS.uebernachtungMoeglich = institutionStammdatenFromServer.uebernachtungMoeglich;
            institutionStammdatenTS.infomaKreditorennummer = institutionStammdatenFromServer.infomaKreditorennummer;
            institutionStammdatenTS.infomaBankcode = institutionStammdatenFromServer.infomaBankcode;

            return institutionStammdatenTS;
        }
        return undefined;
    }

    private parseBetreuungsstandortList(data: Array<any>): Array<any> {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseBetreuungsstandort(new TSBetreuungsstandort(), item))
            : [this.parseBetreuungsstandort(new TSBetreuungsstandort(), data)];
    }

    private parseBetreuungsstandort(betreuungsstandort: TSBetreuungsstandort, betreuungsstandortFromServer: any):
        TSBetreuungsstandort {
        this.parseAbstractEntity(betreuungsstandort, betreuungsstandortFromServer);
        betreuungsstandort.adresse = this.parseAdresse(new TSAdresse(), betreuungsstandortFromServer.adresse);
        betreuungsstandort.webseite = betreuungsstandortFromServer.webseite;
        betreuungsstandort.telefon = betreuungsstandortFromServer.telefon;
        betreuungsstandort.mail = betreuungsstandortFromServer.mail;
        return betreuungsstandort;
    }

    public institutionStammdatenFerieninselToRestObject(
        restInstitutionStammdatenFerieninsel: any,
        institutionStammdatenFerieninsel: TSInstitutionStammdatenFerieninsel
    ): any {
        if (institutionStammdatenFerieninsel) {
            this.abstractEntityToRestObject(restInstitutionStammdatenFerieninsel,
                institutionStammdatenFerieninsel);
            restInstitutionStammdatenFerieninsel.gemeinde =
                this.gemeindeToRestObject({}, institutionStammdatenFerieninsel.gemeinde);

            restInstitutionStammdatenFerieninsel.einstellungenFerieninsel
                =
                this.einstellungenFerieninselArrayToRestObject(institutionStammdatenFerieninsel.einstellungenFerieninsel);

            return restInstitutionStammdatenFerieninsel;
        }
        return undefined;
    }

    private einstellungenFerieninselArrayToRestObject(data: Array<TSEinstellungenFerieninsel>): any[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.einstellungenFerieninselToRestObject({}, item))
            : [];
    }

    private einstellungenFerieninselToRestObject(
        restEinstellung: any, einstellungFerieninselTS: TSEinstellungenFerieninsel
    ): any {
        if (einstellungFerieninselTS) {
            this.abstractEntityToRestObject(restEinstellung, einstellungFerieninselTS);
            restEinstellung.gesuchsperiode =
                this.gesuchsperiodeToRestObject({}, einstellungFerieninselTS.gesuchsperiode);

            restEinstellung.ausweichstandortFruehlingsferien =
                einstellungFerieninselTS.ausweichstandortFruehlingsferien;
            restEinstellung.ausweichstandortHerbstferien =
                einstellungFerieninselTS.ausweichstandortHerbstferien;
            restEinstellung.ausweichstandortSommerferien =
                einstellungFerieninselTS.ausweichstandortSommerferien;
            restEinstellung.ausweichstandortSportferien =
                einstellungFerieninselTS.ausweichstandortSportferien;

            return restEinstellung;
        }
        return undefined;
    }

    public parseInstitutionStammdatenFerieninsel(
        institutionStammdatenFerieninselTS: TSInstitutionStammdatenFerieninsel,
        institutionStammdatenFerieninselFromServer: any
    ): TSInstitutionStammdatenFerieninsel {
        if (institutionStammdatenFerieninselFromServer) {
            this.parseAbstractEntity(institutionStammdatenFerieninselTS,
                institutionStammdatenFerieninselFromServer);
            institutionStammdatenFerieninselTS.gemeinde =
                this.parseGemeinde(new TSGemeinde(), institutionStammdatenFerieninselFromServer.gemeinde);

            institutionStammdatenFerieninselTS.einstellungenFerieninsel =
                this.parseEinstellungenFerieninselArray(institutionStammdatenFerieninselFromServer.einstellungenFerieninsel);

            return institutionStammdatenFerieninselTS;
        }
        return undefined;
    }

    public parseEinstellungenFerieninselArray(data: Array<any>): TSEinstellungenFerieninsel[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseEinstellungenFerieninsel(new TSEinstellungenFerieninsel(), item))
            : [this.parseEinstellungenFerieninsel(new TSEinstellungenFerieninsel(), data)];
    }

    private parseEinstellungenFerieninsel(
        einstellungenFerieninselTS: TSEinstellungenFerieninsel, einstellungFromServer: any
    ): TSEinstellungenFerieninsel {
        if (einstellungFromServer) {
            this.parseAbstractEntity(einstellungenFerieninselTS, einstellungFromServer);
            einstellungenFerieninselTS.gesuchsperiode =
                this.parseGesuchsperiode(new TSGesuchsperiode(), einstellungFromServer.gesuchsperiode);

            einstellungenFerieninselTS.ausweichstandortFruehlingsferien =
                einstellungFromServer.ausweichstandortFruehlingsferien;
            einstellungenFerieninselTS.ausweichstandortHerbstferien =
                einstellungFromServer.ausweichstandortHerbstferien;
            einstellungenFerieninselTS.ausweichstandortSommerferien =
                einstellungFromServer.ausweichstandortSommerferien;
            einstellungenFerieninselTS.ausweichstandortSportferien =
                einstellungFromServer.ausweichstandortSportferien;

            return einstellungenFerieninselTS;
        }
        return undefined;
    }

    public institutionStammdatenTagesschuleToRestObject(
        restInstitutionStammdatenTagesschule: any,
        institutionStammdatenTagesschule: TSInstitutionStammdatenTagesschule
    ): any {
        if (institutionStammdatenTagesschule) {
            this.abstractEntityToRestObject(restInstitutionStammdatenTagesschule,
                institutionStammdatenTagesschule);
            restInstitutionStammdatenTagesschule.gemeinde =
                this.gemeindeToRestObject({}, institutionStammdatenTagesschule.gemeinde);
            restInstitutionStammdatenTagesschule.einstellungenTagesschule =
                this.einstellungenTagesschuleArrayToRestObject(institutionStammdatenTagesschule.einstellungenTagesschule);
            return restInstitutionStammdatenTagesschule;
        }
        return undefined;
    }

    public parseInstitutionStammdatenTagesschule(
        institutionStammdatenTagesschuleTS: TSInstitutionStammdatenTagesschule,
        institutionStammdatenTagesschuleFromServer: any
    ): TSInstitutionStammdatenTagesschule {
        if (institutionStammdatenTagesschuleFromServer) {
            this.parseAbstractEntity(institutionStammdatenTagesschuleTS,
                institutionStammdatenTagesschuleFromServer);
            institutionStammdatenTagesschuleTS.gemeinde =
                this.parseGemeinde(new TSGemeinde(), institutionStammdatenTagesschuleFromServer.gemeinde);
            institutionStammdatenTagesschuleTS.einstellungenTagesschule =
                this.parseEinstellungenTagesschuleArray(institutionStammdatenTagesschuleFromServer.einstellungenTagesschule);
            return institutionStammdatenTagesschuleTS;
        }
        return undefined;
    }

    public finanzielleSituationContainerToRestObject(
        restFinanzielleSituationContainer: any,
        finanzielleSituationContainer: TSFinanzielleSituationContainer
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

    public parseFinanzielleSituationContainer(
        containerTS: TSFinanzielleSituationContainer,
        containerFromServer: any
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

    public finanzielleSituationToRestObject(
        restFinanzielleSituation: any,
        finanzielleSituation: TSFinanzielleSituation
    ): TSFinanzielleSituation {
        this.abstractfinanzielleSituationToRestObject(restFinanzielleSituation, finanzielleSituation);
        restFinanzielleSituation.steuerveranlagungErhalten = finanzielleSituation.steuerveranlagungErhalten;
        restFinanzielleSituation.steuererklaerungAusgefuellt =
            finanzielleSituation.steuererklaerungAusgefuellt || false;
        restFinanzielleSituation.steuerdatenZugriff = finanzielleSituation.steuerdatenZugriff;
        restFinanzielleSituation.geschaeftsgewinnBasisjahrMinus2 = finanzielleSituation.geschaeftsgewinnBasisjahrMinus2;
        restFinanzielleSituation.quellenbesteuert = finanzielleSituation.quellenbesteuert;
        restFinanzielleSituation.gemeinsameStekVorjahr = finanzielleSituation.gemeinsameStekVorjahr;
        restFinanzielleSituation.alleinigeStekVorjahr = finanzielleSituation.alleinigeStekVorjahr;
        restFinanzielleSituation.veranlagt = finanzielleSituation.veranlagt;
        restFinanzielleSituation.veranlagtVorjahr = finanzielleSituation.veranlagtVorjahr;
        restFinanzielleSituation.abzuegeKinderAusbildung = finanzielleSituation.abzuegeKinderAusbildung;
        restFinanzielleSituation.bruttoLohn = finanzielleSituation.bruttoLohn;
        restFinanzielleSituation.unterhaltsBeitraege = finanzielleSituation.unterhaltsBeitraege;
        restFinanzielleSituation.automatischePruefungErlaubt = finanzielleSituation.automatischePruefungErlaubt;
        restFinanzielleSituation.momentanSelbststaendig = finanzielleSituation.momentanSelbststaendig;
        restFinanzielleSituation.ersatzeinkommenSelbststaendigkeitBasisjahrMinus2 =
            finanzielleSituation.ersatzeinkommenSelbststaendigkeitBasisjahrMinus2;

        return restFinanzielleSituation;
    }

    private abstractfinanzielleSituationToRestObject(
        restAbstractFinanzielleSituation: any,
        abstractFinanzielleSituation: TSAbstractFinanzielleSituation
    ): TSAbstractFinanzielleSituation {
        this.abstractMutableEntityToRestObject(restAbstractFinanzielleSituation, abstractFinanzielleSituation);
        restAbstractFinanzielleSituation.nettolohn = abstractFinanzielleSituation.nettolohn;
        restAbstractFinanzielleSituation.familienzulage = abstractFinanzielleSituation.familienzulage;
        restAbstractFinanzielleSituation.ersatzeinkommen = abstractFinanzielleSituation.ersatzeinkommen;
        restAbstractFinanzielleSituation.erhalteneAlimente = abstractFinanzielleSituation.erhalteneAlimente;
        restAbstractFinanzielleSituation.bruttovermoegen = abstractFinanzielleSituation.bruttovermoegen;
        restAbstractFinanzielleSituation.schulden = abstractFinanzielleSituation.schulden;
        restAbstractFinanzielleSituation.geschaeftsgewinnBasisjahr =
            abstractFinanzielleSituation.geschaeftsgewinnBasisjahr;
        restAbstractFinanzielleSituation.geschaeftsgewinnBasisjahrMinus1 =
            abstractFinanzielleSituation.geschaeftsgewinnBasisjahrMinus1;
        restAbstractFinanzielleSituation.geleisteteAlimente = abstractFinanzielleSituation.geleisteteAlimente;
        restAbstractFinanzielleSituation.steuerbaresEinkommen = abstractFinanzielleSituation.steuerbaresEinkommen;
        restAbstractFinanzielleSituation.steuerbaresVermoegen = abstractFinanzielleSituation.steuerbaresVermoegen;
        restAbstractFinanzielleSituation.geschaeftsverlust = abstractFinanzielleSituation.geschaeftsverlust;
        restAbstractFinanzielleSituation.abzuegeLiegenschaft = abstractFinanzielleSituation.abzuegeLiegenschaft;
        restAbstractFinanzielleSituation.einkaeufeVorsorge = abstractFinanzielleSituation.einkaeufeVorsorge;

        restAbstractFinanzielleSituation.gewinnungskosten = abstractFinanzielleSituation.gewinnungskosten;
        restAbstractFinanzielleSituation.einkommenInVereinfachtemVerfahrenAbgerechnet =
            abstractFinanzielleSituation.einkommenInVereinfachtemVerfahrenAbgerechnet;
        restAbstractFinanzielleSituation.amountEinkommenInVereinfachtemVerfahrenAbgerechnet =
            abstractFinanzielleSituation.amountEinkommenInVereinfachtemVerfahrenAbgerechnet;
        restAbstractFinanzielleSituation.nettoVermoegen = abstractFinanzielleSituation.nettoVermoegen;
        restAbstractFinanzielleSituation.nettoertraegeErbengemeinschaft =
            abstractFinanzielleSituation.nettoertraegeErbengemeinschaft;
        restAbstractFinanzielleSituation.abzugSchuldzinsen = abstractFinanzielleSituation.abzugSchuldzinsen;
        restAbstractFinanzielleSituation.bruttoertraegeVermoegen = abstractFinanzielleSituation.bruttoertraegeVermoegen;
        if (EbeguUtil.isNotNullOrUndefined(abstractFinanzielleSituation.selbstdeklaration)) {
            restAbstractFinanzielleSituation.selbstdeklaration = this.finanzielleSituationSelbstdeklarationToRestObject(
                {},
                abstractFinanzielleSituation.selbstdeklaration);
        }
        if (EbeguUtil.isNotNullOrUndefined(abstractFinanzielleSituation.finSitZusatzangabenAppenzell)) {
            restAbstractFinanzielleSituation.finSitZusatzangabenAppenzell = this.finSitZusatzangabenAppenzellToRestObject(
                {},
                abstractFinanzielleSituation.finSitZusatzangabenAppenzell);
        }
        restAbstractFinanzielleSituation.ersatzeinkommenSelbststaendigkeitBasisjahr =
            abstractFinanzielleSituation.ersatzeinkommenSelbststaendigkeitBasisjahr;
        restAbstractFinanzielleSituation.ersatzeinkommenSelbststaendigkeitBasisjahrMinus1 =
            abstractFinanzielleSituation.ersatzeinkommenSelbststaendigkeitBasisjahrMinus1;
        return restAbstractFinanzielleSituation;
    }

    private finanzielleSituationSelbstdeklarationToRestObject(
        restSelbstdeklaration: any,
        selbstdeklaration: TSFinanzielleSituationSelbstdeklaration
    ): TSFinanzielleSituationSelbstdeklaration {

        this.abstractMutableEntityToRestObject(restSelbstdeklaration, selbstdeklaration);
        restSelbstdeklaration.einkunftErwerb = selbstdeklaration.einkunftErwerb;
        restSelbstdeklaration.einkunftVersicherung = selbstdeklaration.einkunftVersicherung;
        restSelbstdeklaration.einkunftWertschriften = selbstdeklaration.einkunftWertschriften;
        restSelbstdeklaration.einkunftUnterhaltsbeitragKinder = selbstdeklaration.einkunftUnterhaltsbeitragKinder;
        restSelbstdeklaration.einkunftUeberige = selbstdeklaration.einkunftUeberige;
        restSelbstdeklaration.einkunftLiegenschaften = selbstdeklaration.einkunftLiegenschaften;
        restSelbstdeklaration.abzugBerufsauslagen = selbstdeklaration.abzugBerufsauslagen;
        restSelbstdeklaration.abzugSchuldzinsen = selbstdeklaration.abzugSchuldzinsen;
        restSelbstdeklaration.abzugUnterhaltsbeitragKinder = selbstdeklaration.abzugUnterhaltsbeitragKinder;
        restSelbstdeklaration.abzugSaeule3A = selbstdeklaration.abzugSaeule3A;
        restSelbstdeklaration.abzugVersicherungspraemien = selbstdeklaration.abzugVersicherungspraemien;
        restSelbstdeklaration.abzugKrankheitsUnfallKosten = selbstdeklaration.abzugKrankheitsUnfallKosten;
        restSelbstdeklaration.sonderabzugErwerbstaetigkeitEhegatten
            = selbstdeklaration.sonderabzugErwerbstaetigkeitEhegatten;
        restSelbstdeklaration.abzugKinderVorschule = selbstdeklaration.abzugKinderVorschule;
        restSelbstdeklaration.abzugKinderSchule = selbstdeklaration.abzugKinderSchule;
        restSelbstdeklaration.abzugEigenbetreuung = selbstdeklaration.abzugEigenbetreuung;
        restSelbstdeklaration.abzugFremdbetreuung = selbstdeklaration.abzugFremdbetreuung;
        restSelbstdeklaration.abzugErwerbsunfaehigePersonen
            = selbstdeklaration.abzugErwerbsunfaehigePersonen;
        restSelbstdeklaration.vermoegen = selbstdeklaration.vermoegen;
        restSelbstdeklaration.abzugSteuerfreierBetragErwachsene
            = selbstdeklaration.abzugSteuerfreierBetragErwachsene;
        restSelbstdeklaration.abzugSteuerfreierBetragKinder
            = selbstdeklaration.abzugSteuerfreierBetragKinder;
        return restSelbstdeklaration;
    }

    private finSitZusatzangabenAppenzellToRestObject(
        restFinanzielleVerhaeltnisse: any,
        finanzielleVerhaeltnisse: TSFinSitZusatzangabenAppenzell
    ): TSFinSitZusatzangabenAppenzell {

        this.abstractMutableEntityToRestObject(restFinanzielleVerhaeltnisse, finanzielleVerhaeltnisse);
        restFinanzielleVerhaeltnisse.saeule3a = finanzielleVerhaeltnisse.saeule3a;
        restFinanzielleVerhaeltnisse.saeule3aNichtBvg = finanzielleVerhaeltnisse.saeule3aNichtBvg;
        restFinanzielleVerhaeltnisse.beruflicheVorsorge = finanzielleVerhaeltnisse.beruflicheVorsorge;
        restFinanzielleVerhaeltnisse.vorjahresverluste = finanzielleVerhaeltnisse.vorjahresverluste;
        restFinanzielleVerhaeltnisse.liegenschaftsaufwand = finanzielleVerhaeltnisse.liegenschaftsaufwand;
        restFinanzielleVerhaeltnisse.einkuenfteBgsa = finanzielleVerhaeltnisse.einkuenfteBgsa;
        restFinanzielleVerhaeltnisse.politischeParteiSpende = finanzielleVerhaeltnisse.politischeParteiSpende;
        restFinanzielleVerhaeltnisse.leistungAnJuristischePersonen = finanzielleVerhaeltnisse.leistungAnJuristischePersonen;
        restFinanzielleVerhaeltnisse.steuerbaresVermoegen = finanzielleVerhaeltnisse.steuerbaresVermoegen;
        restFinanzielleVerhaeltnisse.steuerbaresEinkommen = finanzielleVerhaeltnisse.steuerbaresEinkommen;
        if (EbeguUtil.isNotNullOrUndefined(finanzielleVerhaeltnisse.zusatzangabenPartner)) {
            restFinanzielleVerhaeltnisse.zusatzangabenPartner =
                this.finSitZusatzangabenAppenzellToRestObject({}, finanzielleVerhaeltnisse.zusatzangabenPartner);
        }
        return restFinanzielleVerhaeltnisse;
    }

    public parseAbstractFinanzielleSituation(
        abstractFinanzielleSituationTS: TSAbstractFinanzielleSituation,
        abstractFinanzielleSituationFromServer: any
    ): TSAbstractFinanzielleSituation {
        if (abstractFinanzielleSituationFromServer) {
            this.parseAbstractMutableEntity(abstractFinanzielleSituationTS, abstractFinanzielleSituationFromServer);
            abstractFinanzielleSituationTS.nettolohn = abstractFinanzielleSituationFromServer.nettolohn;
            abstractFinanzielleSituationTS.familienzulage = abstractFinanzielleSituationFromServer.familienzulage;
            abstractFinanzielleSituationTS.ersatzeinkommen = abstractFinanzielleSituationFromServer.ersatzeinkommen;
            abstractFinanzielleSituationTS.erhalteneAlimente = abstractFinanzielleSituationFromServer.erhalteneAlimente;
            abstractFinanzielleSituationTS.bruttovermoegen = abstractFinanzielleSituationFromServer.bruttovermoegen;
            abstractFinanzielleSituationTS.schulden = abstractFinanzielleSituationFromServer.schulden;
            abstractFinanzielleSituationTS.geschaeftsgewinnBasisjahr =
                abstractFinanzielleSituationFromServer.geschaeftsgewinnBasisjahr;
            abstractFinanzielleSituationTS.geschaeftsgewinnBasisjahrMinus1 =
                abstractFinanzielleSituationFromServer.geschaeftsgewinnBasisjahrMinus1;
            abstractFinanzielleSituationTS.geleisteteAlimente =
                abstractFinanzielleSituationFromServer.geleisteteAlimente;
            abstractFinanzielleSituationTS.steuerbaresEinkommen =
                abstractFinanzielleSituationFromServer.steuerbaresEinkommen;
            abstractFinanzielleSituationTS.steuerbaresVermoegen =
                abstractFinanzielleSituationFromServer.steuerbaresVermoegen;
            abstractFinanzielleSituationTS.geschaeftsverlust = abstractFinanzielleSituationFromServer.geschaeftsverlust;
            abstractFinanzielleSituationTS.abzuegeLiegenschaft =
                abstractFinanzielleSituationFromServer.abzuegeLiegenschaft;
            abstractFinanzielleSituationTS.einkaeufeVorsorge = abstractFinanzielleSituationFromServer.einkaeufeVorsorge;
            abstractFinanzielleSituationTS.abzugSchuldzinsen = abstractFinanzielleSituationFromServer.abzugSchuldzinsen;
            abstractFinanzielleSituationTS.nettoertraegeErbengemeinschaft =
                abstractFinanzielleSituationFromServer.nettoertraegeErbengemeinschaft;
            abstractFinanzielleSituationTS.nettoVermoegen = abstractFinanzielleSituationFromServer.nettoVermoegen;
            abstractFinanzielleSituationTS.einkommenInVereinfachtemVerfahrenAbgerechnet =
                abstractFinanzielleSituationFromServer.einkommenInVereinfachtemVerfahrenAbgerechnet;
            abstractFinanzielleSituationTS.amountEinkommenInVereinfachtemVerfahrenAbgerechnet =
                abstractFinanzielleSituationFromServer.amountEinkommenInVereinfachtemVerfahrenAbgerechnet;
            abstractFinanzielleSituationTS.gewinnungskosten = abstractFinanzielleSituationFromServer.gewinnungskosten;
            abstractFinanzielleSituationTS.bruttoertraegeVermoegen =
                abstractFinanzielleSituationFromServer.bruttoertraegeVermoegen;
            abstractFinanzielleSituationTS.steuerdatenAbfrageStatus =
                abstractFinanzielleSituationFromServer.steuerdatenAbfrageStatus;
            abstractFinanzielleSituationTS.selbstdeklaration =
                this.parseFinanzielleSituationSelbstdeklaration(new TSFinanzielleSituationSelbstdeklaration(),
                    abstractFinanzielleSituationFromServer.selbstdeklaration);
            abstractFinanzielleSituationTS.finSitZusatzangabenAppenzell =
                this.parseFinSitZusatzangabenAppenzell(new TSFinSitZusatzangabenAppenzell(),
                    abstractFinanzielleSituationFromServer.finSitZusatzangabenAppenzell);
            abstractFinanzielleSituationTS.ersatzeinkommenSelbststaendigkeitBasisjahr =
                abstractFinanzielleSituationFromServer.ersatzeinkommenSelbststaendigkeitBasisjahr;
            abstractFinanzielleSituationTS.ersatzeinkommenSelbststaendigkeitBasisjahrMinus1 =
                abstractFinanzielleSituationFromServer.ersatzeinkommenSelbststaendigkeitBasisjahrMinus1;

            return abstractFinanzielleSituationTS;
        }
        return undefined;
    }

    public parseFinanzielleSituation(
        finanzielleSituationTS: TSFinanzielleSituation,
        finanzielleSituationFromServer: any
    ): TSFinanzielleSituation {
        if (finanzielleSituationFromServer) {
            this.parseAbstractFinanzielleSituation(finanzielleSituationTS, finanzielleSituationFromServer);
            finanzielleSituationTS.steuerveranlagungErhalten =
                finanzielleSituationFromServer.steuerveranlagungErhalten;
            finanzielleSituationTS.steuererklaerungAusgefuellt =
                finanzielleSituationFromServer.steuererklaerungAusgefuellt;
            finanzielleSituationTS.steuerdatenZugriff =
                finanzielleSituationFromServer.steuerdatenZugriff;
            finanzielleSituationTS.geschaeftsgewinnBasisjahrMinus2 =
                finanzielleSituationFromServer.geschaeftsgewinnBasisjahrMinus2;
            finanzielleSituationTS.quellenbesteuert = finanzielleSituationFromServer.quellenbesteuert;
            finanzielleSituationTS.gemeinsameStekVorjahr = finanzielleSituationFromServer.gemeinsameStekVorjahr;
            finanzielleSituationTS.alleinigeStekVorjahr = finanzielleSituationFromServer.alleinigeStekVorjahr;
            finanzielleSituationTS.veranlagt = finanzielleSituationFromServer.veranlagt;
            finanzielleSituationTS.veranlagtVorjahr = finanzielleSituationFromServer.veranlagtVorjahr;
            finanzielleSituationTS.abzuegeKinderAusbildung = finanzielleSituationFromServer.abzuegeKinderAusbildung;
            finanzielleSituationTS.bruttoLohn = finanzielleSituationFromServer.bruttoLohn;
            finanzielleSituationTS.unterhaltsBeitraege = finanzielleSituationFromServer.unterhaltsBeitraege;
            finanzielleSituationTS.automatischePruefungErlaubt =
                finanzielleSituationFromServer.automatischePruefungErlaubt;
            if (finanzielleSituationFromServer.steuerdatenAbfrageTimestamp) {
                finanzielleSituationTS.steuerdatenAbfrageTimestamp =
                    DateUtil.localDateTimeToMoment(finanzielleSituationFromServer.steuerdatenAbfrageTimestamp);
            }
            finanzielleSituationTS.momentanSelbststaendig = finanzielleSituationFromServer.momentanSelbststaendig;
            finanzielleSituationTS.ersatzeinkommenSelbststaendigkeitBasisjahrMinus2 =
                finanzielleSituationFromServer.ersatzeinkommenSelbststaendigkeitBasisjahrMinus2;
            return finanzielleSituationTS;
        }
        return undefined;
    }

    private parseFinanzielleSituationSelbstdeklaration(
        tsSelbstdeklaration: TSFinanzielleSituationSelbstdeklaration,
        selbstdeklarationFromServer: any
    ): TSFinanzielleSituationSelbstdeklaration {

        if (selbstdeklarationFromServer) {
            this.parseAbstractMutableEntity(tsSelbstdeklaration, selbstdeklarationFromServer);
            tsSelbstdeklaration.einkunftErwerb = selbstdeklarationFromServer.einkunftErwerb;
            tsSelbstdeklaration.einkunftVersicherung = selbstdeklarationFromServer.einkunftVersicherung;
            tsSelbstdeklaration.einkunftWertschriften = selbstdeklarationFromServer.einkunftWertschriften;
            tsSelbstdeklaration.einkunftUnterhaltsbeitragKinder =
                selbstdeklarationFromServer.einkunftUnterhaltsbeitragKinder;
            tsSelbstdeklaration.einkunftUeberige = selbstdeklarationFromServer.einkunftUeberige;
            tsSelbstdeklaration.einkunftLiegenschaften = selbstdeklarationFromServer.einkunftLiegenschaften;
            tsSelbstdeklaration.abzugBerufsauslagen = selbstdeklarationFromServer.abzugBerufsauslagen;
            tsSelbstdeklaration.abzugSchuldzinsen = selbstdeklarationFromServer.abzugSchuldzinsen;
            tsSelbstdeklaration.abzugUnterhaltsbeitragKinder = selbstdeklarationFromServer.abzugUnterhaltsbeitragKinder;
            tsSelbstdeklaration.abzugSaeule3A = selbstdeklarationFromServer.abzugSaeule3A;
            tsSelbstdeklaration.abzugVersicherungspraemien = selbstdeklarationFromServer.abzugVersicherungspraemien;
            tsSelbstdeklaration.abzugKrankheitsUnfallKosten = selbstdeklarationFromServer.abzugKrankheitsUnfallKosten;
            tsSelbstdeklaration.sonderabzugErwerbstaetigkeitEhegatten
                = selbstdeklarationFromServer.sonderabzugErwerbstaetigkeitEhegatten;
            tsSelbstdeklaration.abzugKinderVorschule = selbstdeklarationFromServer.abzugKinderVorschule;
            tsSelbstdeklaration.abzugKinderSchule = selbstdeklarationFromServer.abzugKinderSchule;
            tsSelbstdeklaration.abzugEigenbetreuung = selbstdeklarationFromServer.abzugEigenbetreuung;
            tsSelbstdeklaration.abzugFremdbetreuung = selbstdeklarationFromServer.abzugFremdbetreuung;
            tsSelbstdeklaration.abzugErwerbsunfaehigePersonen
                = selbstdeklarationFromServer.abzugErwerbsunfaehigePersonen;
            tsSelbstdeklaration.vermoegen = selbstdeklarationFromServer.vermoegen;
            tsSelbstdeklaration.abzugSteuerfreierBetragErwachsene
                = selbstdeklarationFromServer.abzugSteuerfreierBetragErwachsene;
            tsSelbstdeklaration.abzugSteuerfreierBetragKinder
                = selbstdeklarationFromServer.abzugSteuerfreierBetragKinder;
            return tsSelbstdeklaration;
        }
        return undefined;
    }

    private parseFinSitZusatzangabenAppenzell(
        tsFinSitZusatzangabenAppenzell: TSFinSitZusatzangabenAppenzell,
        finSitZusatzangabenAppenzellFromServer: any
    ): TSFinSitZusatzangabenAppenzell {

        if (finSitZusatzangabenAppenzellFromServer) {
            this.parseAbstractMutableEntity(tsFinSitZusatzangabenAppenzell, finSitZusatzangabenAppenzellFromServer);
            tsFinSitZusatzangabenAppenzell.saeule3a = finSitZusatzangabenAppenzellFromServer.saeule3a;
            tsFinSitZusatzangabenAppenzell.saeule3aNichtBvg = finSitZusatzangabenAppenzellFromServer.saeule3aNichtBvg;
            tsFinSitZusatzangabenAppenzell.beruflicheVorsorge = finSitZusatzangabenAppenzellFromServer.beruflicheVorsorge;
            tsFinSitZusatzangabenAppenzell.einkuenfteBgsa = finSitZusatzangabenAppenzellFromServer.einkuenfteBgsa;
            tsFinSitZusatzangabenAppenzell.liegenschaftsaufwand = finSitZusatzangabenAppenzellFromServer.liegenschaftsaufwand;
            tsFinSitZusatzangabenAppenzell.vorjahresverluste = finSitZusatzangabenAppenzellFromServer.vorjahresverluste;
            tsFinSitZusatzangabenAppenzell.politischeParteiSpende = finSitZusatzangabenAppenzellFromServer.politischeParteiSpende;
            tsFinSitZusatzangabenAppenzell.leistungAnJuristischePersonen =
                finSitZusatzangabenAppenzellFromServer.leistungAnJuristischePersonen;
            tsFinSitZusatzangabenAppenzell.steuerbaresEinkommen = finSitZusatzangabenAppenzellFromServer.steuerbaresEinkommen;
            tsFinSitZusatzangabenAppenzell.steuerbaresVermoegen = finSitZusatzangabenAppenzellFromServer.steuerbaresVermoegen;
            if (EbeguUtil.isNotNullOrUndefined(finSitZusatzangabenAppenzellFromServer.zusatzangabenPartner)) {
                tsFinSitZusatzangabenAppenzell.zusatzangabenPartner =
                    this.parseFinSitZusatzangabenAppenzell(new TSFinSitZusatzangabenAppenzell(),
                        finSitZusatzangabenAppenzellFromServer.zusatzangabenPartner);
            }

            return tsFinSitZusatzangabenAppenzell;
        }
        return undefined;
    }

    public parseFinanzielleSituationResultate(
        finanzielleSituationResultateDTO: TSFinanzielleSituationResultateDTO,
        finanzielleSituationResultateFromServer: any
    ): TSFinanzielleSituationResultateDTO {
        if (finanzielleSituationResultateFromServer) {
            finanzielleSituationResultateDTO.geschaeftsgewinnDurchschnittGesuchsteller1 =
                finanzielleSituationResultateFromServer.geschaeftsgewinnDurchschnittGesuchsteller1;
            finanzielleSituationResultateDTO.geschaeftsgewinnDurchschnittGesuchsteller2 =
                finanzielleSituationResultateFromServer.geschaeftsgewinnDurchschnittGesuchsteller2;
            finanzielleSituationResultateDTO.einkommenBeiderGesuchsteller =
                finanzielleSituationResultateFromServer.einkommenBeiderGesuchsteller;
            finanzielleSituationResultateDTO.nettovermoegenFuenfProzent =
                finanzielleSituationResultateFromServer.nettovermoegenXProzent;
            finanzielleSituationResultateDTO.anrechenbaresEinkommen =
                finanzielleSituationResultateFromServer.anrechenbaresEinkommen;
            finanzielleSituationResultateDTO.abzuegeBeiderGesuchsteller =
                finanzielleSituationResultateFromServer.abzuegeBeiderGesuchsteller;
            finanzielleSituationResultateDTO.massgebendesEinkVorAbzFamGr =
                finanzielleSituationResultateFromServer.massgebendesEinkVorAbzFamGr;
            finanzielleSituationResultateDTO.massgebendesEinkVorAbzFamGrGS1 =
                finanzielleSituationResultateFromServer.massgebendesEinkVorAbzFamGrGS1;
            finanzielleSituationResultateDTO.massgebendesEinkVorAbzFamGrGS2 =
                finanzielleSituationResultateFromServer.massgebendesEinkVorAbzFamGrGS2;
            finanzielleSituationResultateDTO.einkommenGS1 = finanzielleSituationResultateFromServer.einkommenGS1;
            finanzielleSituationResultateDTO.einkommenGS2 = finanzielleSituationResultateFromServer.einkommenGS2;
            finanzielleSituationResultateDTO.abzuegeGS1 = finanzielleSituationResultateFromServer.abzuegeGS1;
            finanzielleSituationResultateDTO.abzuegeGS2 = finanzielleSituationResultateFromServer.abzuegeGS2;
            finanzielleSituationResultateDTO.vermoegenXPercentAnrechenbarGS1 =
                finanzielleSituationResultateFromServer.vermoegenXPercentAnrechenbarGS1;
            finanzielleSituationResultateDTO.vermoegenXPercentAnrechenbarGS2 =
                finanzielleSituationResultateFromServer.vermoegenXPercentAnrechenbarGS2;
            finanzielleSituationResultateDTO.bruttolohnJahrGS1 =
                finanzielleSituationResultateFromServer.bruttolohnJahrGS1;
            finanzielleSituationResultateDTO.bruttolohnJahrGS2 =
                finanzielleSituationResultateFromServer.bruttolohnJahrGS2;
            return finanzielleSituationResultateDTO;
        }
        return undefined;
    }

    public einkommensverschlechterungContainerToRestObject(
        restEinkommensverschlechterungContainer: any,
        einkommensverschlechterungContainer: TSEinkommensverschlechterungContainer
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
        einkommensverschlechterung: TSEinkommensverschlechterung
    ): TSEinkommensverschlechterung {
        this.abstractfinanzielleSituationToRestObject(restEinkommensverschlechterung, einkommensverschlechterung);
        restEinkommensverschlechterung.bruttolohnAbrechnung1 = einkommensverschlechterung.bruttolohnAbrechnung1;
        restEinkommensverschlechterung.bruttolohnAbrechnung2 = einkommensverschlechterung.bruttolohnAbrechnung2;
        restEinkommensverschlechterung.bruttolohnAbrechnung3 = einkommensverschlechterung.bruttolohnAbrechnung3;
        restEinkommensverschlechterung.extraLohn = einkommensverschlechterung.extraLohn;
        return restEinkommensverschlechterung;
    }

    public parseEinkommensverschlechterungContainer(
        containerTS: TSEinkommensverschlechterungContainer,
        containerFromServer: any
    ): TSEinkommensverschlechterungContainer {
        if (containerFromServer) {
            this.parseAbstractMutableEntity(containerTS, containerFromServer);
            // Achtung, das Argument "new TSEinkommensverschlechterung()" muss immer neu erstellt werden, da es sonst
            // dasselbe Instanz ist.
            containerTS.ekvGSBasisJahrPlus1 =
                this.parseEinkommensverschlechterung(
                    containerTS.ekvGSBasisJahrPlus1 || new TSEinkommensverschlechterung(),
                    containerFromServer.ekvGSBasisJahrPlus1
                );
            containerTS.ekvGSBasisJahrPlus2 =
                this.parseEinkommensverschlechterung(
                    containerTS.ekvGSBasisJahrPlus2 || new TSEinkommensverschlechterung(),
                    containerFromServer.ekvGSBasisJahrPlus2
                );
            containerTS.ekvJABasisJahrPlus1 =
                this.parseEinkommensverschlechterung(
                    containerTS.ekvJABasisJahrPlus1 || new TSEinkommensverschlechterung(),
                    containerFromServer.ekvJABasisJahrPlus1
                );
            containerTS.ekvJABasisJahrPlus2 =
                this.parseEinkommensverschlechterung(
                    containerTS.ekvJABasisJahrPlus2 || new TSEinkommensverschlechterung(),
                    containerFromServer.ekvJABasisJahrPlus2
                );

            return containerTS;
        }
        return undefined;
    }

    public parseEinkommensverschlechterung(
        einkommensverschlechterungTS: TSEinkommensverschlechterung,
        einkommensverschlechterungFromServer: any
    ): TSEinkommensverschlechterung {
        if (!einkommensverschlechterungFromServer) {
            return undefined;
        }
        this.parseAbstractFinanzielleSituation(einkommensverschlechterungTS, einkommensverschlechterungFromServer);
        einkommensverschlechterungTS.bruttolohnAbrechnung1 = einkommensverschlechterungFromServer.bruttolohnAbrechnung1;
        einkommensverschlechterungTS.bruttolohnAbrechnung2 = einkommensverschlechterungFromServer.bruttolohnAbrechnung2;
        einkommensverschlechterungTS.bruttolohnAbrechnung3 = einkommensverschlechterungFromServer.bruttolohnAbrechnung3;
        einkommensverschlechterungTS.extraLohn = einkommensverschlechterungFromServer.extraLohn;
        return einkommensverschlechterungTS;
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
        restKind.kinderabzugErstesHalbjahr = kind.kinderabzugErstesHalbjahr;
        restKind.kinderabzugZweitesHalbjahr = kind.kinderabzugZweitesHalbjahr;
        restKind.isPflegekind = kind.isPflegekind;
        restKind.pflegeEntschaedigungErhalten = kind.pflegeEntschaedigungErhalten;
        restKind.obhutAlternierendAusueben = kind.obhutAlternierendAusueben;
        restKind.gemeinsamesGesuch = kind.gemeinsamesGesuch;
        restKind.inErstausbildung = kind.inErstausbildung;
        restKind.lebtKindAlternierend = kind.lebtKindAlternierend;
        restKind.alimenteErhalten = kind.alimenteErhalten;
        restKind.alimenteBezahlen = kind.alimenteBezahlen;
        restKind.sprichtAmtssprache = kind.sprichtAmtssprache;
        restKind.ausAsylwesen = kind.ausAsylwesen;
        restKind.zemisNummer = kind.zemisNummerStandardFormat;
        restKind.einschulungTyp = kind.einschulungTyp;
        restKind.keinPlatzInSchulhort = kind.keinPlatzInSchulhort;
        restKind.familienErgaenzendeBetreuung = kind.familienErgaenzendeBetreuung;
        restKind.zukunftigeGeburtsdatum = kind.zukunftigeGeburtsdatum;
        restKind.inPruefung = kind.inPruefung;
        restKind.unterhaltspflichtig = kind.unterhaltspflichtig;
        if (kind.pensumFachstellen) {
            restKind.pensumFachstellen = this.pensumFachstellenToRestObject(kind.pensumFachstellen);
        }
        if (kind.pensumAusserordentlicherAnspruch) {
            restKind.pensumAusserordentlicherAnspruch = this.pensumAusserordentlicherAnspruchToRestObject(
                {}, kind.pensumAusserordentlicherAnspruch);
        }
        return restKind;
    }

    public parseKindDubletteList(data: Array<any>): TSKindDublette[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
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
        if (!data) {
            return [];
        }
        return Array.isArray(data)
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
            kindContainerTS.keinSelbstbehaltDurchGemeinde = kindContainerFromServer.keinSelbstbehaltDurchGemeinde;
            kindContainerTS.nextNumberBetreuung = kindContainerFromServer.nextNumberBetreuung;
            kindContainerTS.kindMutiert = kindContainerFromServer.kindMutiert;
            return kindContainerTS;
        }
        return undefined;
    }

    private parseKind(kindTS: TSKind, kindFromServer: any): TSKind {
        if (kindFromServer) {
            this.parseAbstractPersonEntity(kindTS, kindFromServer);
            kindTS.kinderabzugErstesHalbjahr = kindFromServer.kinderabzugErstesHalbjahr;
            kindTS.kinderabzugZweitesHalbjahr = kindFromServer.kinderabzugZweitesHalbjahr;
            kindTS.isPflegekind = kindFromServer.isPflegekind;
            kindTS.pflegeEntschaedigungErhalten = kindFromServer.pflegeEntschaedigungErhalten;
            kindTS.obhutAlternierendAusueben = kindFromServer.obhutAlternierendAusueben;
            kindTS.gemeinsamesGesuch = kindFromServer.gemeinsamesGesuch;
            kindTS.inErstausbildung = kindFromServer.inErstausbildung;
            kindTS.lebtKindAlternierend = kindFromServer.lebtKindAlternierend;
            kindTS.alimenteErhalten = kindFromServer.alimenteErhalten;
            kindTS.alimenteBezahlen = kindFromServer.alimenteBezahlen;
            kindTS.sprichtAmtssprache = kindFromServer.sprichtAmtssprache;
            kindTS.ausAsylwesen = kindFromServer.ausAsylwesen;
            kindTS.zemisNummer = kindFromServer.zemisNummer;
            kindTS.einschulungTyp = kindFromServer.einschulungTyp;
            kindTS.keinPlatzInSchulhort = kindFromServer.keinPlatzInSchulhort;
            kindTS.familienErgaenzendeBetreuung = kindFromServer.familienErgaenzendeBetreuung;
            kindTS.zukunftigeGeburtsdatum = kindFromServer.zukunftigeGeburtsdatum;
            kindTS.inPruefung = kindFromServer.inPruefung;
            kindTS.unterhaltspflichtig = kindFromServer.unterhaltspflichtig;
            if (kindFromServer.pensumFachstellen) {
                kindTS.pensumFachstellen =
                    this.parsePensumFachstellen(kindFromServer.pensumFachstellen);
            }
            if (kindFromServer.pensumAusserordentlicherAnspruch) {
                kindTS.pensumAusserordentlicherAnspruch =
                    this.parsePensumAusserordentlicherAnspruch(
                        new TSPensumAusserordentlicherAnspruch(), kindFromServer.pensumAusserordentlicherAnspruch);
            }
            return kindTS;
        }
        return undefined;
    }

    private pensumFachstellenToRestObject(pensumFachstellen: TSPensumFachstelle[]): any {
        return pensumFachstellen.map(pensumFachstelle => this.pensumFachstelleToRestObject({}, pensumFachstelle));
    }

    private pensumFachstelleToRestObject(restPensumFachstelle: any, pensumFachstelle: TSPensumFachstelle): any {
        this.abstractDateRangeEntityToRestObject(restPensumFachstelle, pensumFachstelle);
        restPensumFachstelle.pensum = pensumFachstelle.pensum;
        restPensumFachstelle.integrationTyp = pensumFachstelle.integrationTyp;
        restPensumFachstelle.gruendeZusatzleistung = pensumFachstelle.gruendeZusatzleistung;
        if (pensumFachstelle.fachstelle) {
            restPensumFachstelle.fachstelle = this.fachstelleToRestObject({}, pensumFachstelle.fachstelle);
        }
        return restPensumFachstelle;
    }

    private parsePensumFachstellen(
        pensumFachstellenFromServer: any[]
    ): TSPensumFachstelle[] {
        return pensumFachstellenFromServer ?
            pensumFachstellenFromServer.map(pensumFachstelleFromServer => this.parsePensumFachstelle(new TSPensumFachstelle(),
                pensumFachstelleFromServer)) :
            [];
    }

    private parsePensumFachstelle(
        pensumFachstelleTS: TSPensumFachstelle,
        pensumFachstelleFromServer: any
    ): TSPensumFachstelle {
        if (pensumFachstelleFromServer) {
            this.parseDateRangeEntity(pensumFachstelleTS, pensumFachstelleFromServer);
            pensumFachstelleTS.pensum = pensumFachstelleFromServer.pensum;
            pensumFachstelleTS.integrationTyp = pensumFachstelleFromServer.integrationTyp;
            pensumFachstelleTS.gruendeZusatzleistung = pensumFachstelleFromServer.gruendeZusatzleistung;
            if (pensumFachstelleFromServer.fachstelle) {
                pensumFachstelleTS.fachstelle =
                    this.parseFachstelle(new TSFachstelle(), pensumFachstelleFromServer.fachstelle);
            }
            return pensumFachstelleTS;
        }
        return undefined;
    }

    private pensumAusserordentlicherAnspruchToRestObject(
        restPensumAusserordentlicherAnspruch: any,
        pensumAusserordentlicherAnspruch: TSPensumAusserordentlicherAnspruch
    ): any {

        this.abstractDateRangeEntityToRestObject(restPensumAusserordentlicherAnspruch,
            pensumAusserordentlicherAnspruch);
        restPensumAusserordentlicherAnspruch.pensum = pensumAusserordentlicherAnspruch.pensum;
        restPensumAusserordentlicherAnspruch.begruendung = pensumAusserordentlicherAnspruch.begruendung;
        return restPensumAusserordentlicherAnspruch;
    }

    private parsePensumAusserordentlicherAnspruch(
        pensumAusserordentlicherAnspruchTS: TSPensumAusserordentlicherAnspruch,
        pensumAusserordentlicherAnspruchFromServer: any
    ): TSPensumAusserordentlicherAnspruch {

        if (pensumAusserordentlicherAnspruchFromServer) {
            this.parseDateRangeEntity(pensumAusserordentlicherAnspruchTS, pensumAusserordentlicherAnspruchFromServer);
            pensumAusserordentlicherAnspruchTS.pensum = pensumAusserordentlicherAnspruchFromServer.pensum;
            pensumAusserordentlicherAnspruchTS.begruendung = pensumAusserordentlicherAnspruchFromServer.begruendung;
            return pensumAusserordentlicherAnspruchTS;
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

        restBetreuung.betreuungspensumAbweichungen =
            this.betreuungspensumAbweichungenToRestObject(betreuung.betreuungspensumAbweichungen);

        if (betreuung.abwesenheitContainers) {
            restBetreuung.abwesenheitContainers = [];
            betreuung.abwesenheitContainers.forEach((abwesenheitCont: TSAbwesenheitContainer) => {
                restBetreuung.abwesenheitContainers.push(this.abwesenheitContainerToRestObject({}, abwesenheitCont));
            });
        }
        if (betreuung.erweiterteBetreuungContainer) {
            restBetreuung.erweiterteBetreuungContainer =
                this.erweiterteBetreuungContainerToRestObject({}, betreuung.erweiterteBetreuungContainer);
        }
        restBetreuung.kindFullname = betreuung.kindFullname;
        restBetreuung.kindNummer = betreuung.kindNummer;
        restBetreuung.kindId = betreuung.kindId;
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
        restBetreuung.eingewoehnung = betreuung.eingewoehnung;
        restBetreuung.auszahlungAnEltern = betreuung.auszahlungAnEltern;
        restBetreuung.begruendungAuszahlungAnInstitution = betreuung.begruendungAuszahlungAnInstitution;
        return restBetreuung;
    }

    public betreuungspensumAbweichungenToRestObject(abweichungen: TSBetreuungspensumAbweichung[]): any {
        let restAbweichungen: any;
        if (abweichungen) {
            restAbweichungen = [];
            // only send Abweichungen with actual Abweichungen
            const filteredAbweichungen =
                abweichungen.filter(element => element.status !== TSBetreuungspensumAbweichungStatus.NONE);

            filteredAbweichungen.forEach((abweichung: TSBetreuungspensumAbweichung) => {
                restAbweichungen.push(this.betreuungspensumAbweichungToRestObject({},
                    abweichung));
            });
        }

        return restAbweichungen;
    }

    public anmeldungDTOToRestObject(restAngebot: any, angebotDTO: TSAnmeldungDTO): any {
        restAngebot.betreuung = this.betreuungToRestObject({}, angebotDTO.betreuung);
        restAngebot.betreuung.erweiterteBetreuungContainer = new TSErweiterteBetreuungContainer();
        restAngebot.betreuung.vertrag = true;
        restAngebot.additionalKindQuestions = angebotDTO.additionalKindQuestions;
        restAngebot.einschulungTyp = angebotDTO.einschulungTyp;
        restAngebot.kindContainerId = angebotDTO.kindContainerId;
        restAngebot.sprichtAmtssprache = angebotDTO.sprichtAmtssprache;
        return restAngebot;

    }

    public betreuungspensumAbweichungToRestObject(restAbweichung: any, abweichung: TSBetreuungspensumAbweichung): any {
        this.abstractBetreuungspensumEntityToRestObject(restAbweichung, abweichung);

        restAbweichung.status = abweichung.status;
        restAbweichung.pensum = restAbweichung.pensum ? restAbweichung.pensum / restAbweichung.multiplier : undefined;
        restAbweichung.monatlicheHauptmahlzeiten = abweichung.monatlicheHauptmahlzeiten;
        restAbweichung.tarifProHauptmahlzeit = abweichung.tarifProHauptmahlzeit;
        restAbweichung.monatlicheNebenmahlzeiten = abweichung.monatlicheNebenmahlzeiten;
        restAbweichung.tarifProNebenmahlzeit = abweichung.tarifProNebenmahlzeit;

        return restAbweichung;
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
        this.abstractBetreuungspensumEntityToRestObject(restBetreuungspensum, betreuungspensum);
        if (betreuungspensum.nichtEingetreten !== null) {
            // wenn es null ist, wird es als null zum Server geschickt und der Server versucht, es zu validieren und
            // wirft eine NPE
            restBetreuungspensum.nichtEingetreten = betreuungspensum.nichtEingetreten;
            restBetreuungspensum.monatlicheHauptmahlzeiten =
                EbeguUtil.isNullOrUndefined(betreuungspensum.monatlicheHauptmahlzeiten) ?
                    0 :
                    betreuungspensum.monatlicheHauptmahlzeiten;
            restBetreuungspensum.monatlicheNebenmahlzeiten =
                EbeguUtil.isNullOrUndefined(betreuungspensum.monatlicheNebenmahlzeiten) ?
                    0 :
                    betreuungspensum.monatlicheNebenmahlzeiten;
            restBetreuungspensum.tarifProHauptmahlzeit =
                EbeguUtil.isNullOrUndefined(betreuungspensum.tarifProHauptmahlzeit) ?
                    0 :
                    betreuungspensum.tarifProHauptmahlzeit;
            restBetreuungspensum.tarifProNebenmahlzeit =
                EbeguUtil.isNullOrUndefined(betreuungspensum.tarifProNebenmahlzeit) ?
                    0 :
                    betreuungspensum.tarifProNebenmahlzeit;
            restBetreuungspensum.unitForDisplay = betreuungspensum.unitForDisplay;
        }
        return restBetreuungspensum;
    }

    public betreuungsmitteilungPensumToRestObject(
        restBetreuungspensum: any,
        betreuungspensum: TSBetreuungsmitteilungPensum
    ): any {
        this.abstractBetreuungspensumEntityToRestObject(restBetreuungspensum, betreuungspensum);
        restBetreuungspensum.monatlicheHauptmahlzeiten = betreuungspensum.monatlicheHauptmahlzeiten;
        restBetreuungspensum.monatlicheNebenmahlzeiten = betreuungspensum.monatlicheNebenmahlzeiten;
        restBetreuungspensum.tarifProHauptmahlzeit = betreuungspensum.tarifProHauptmahlzeit;
        restBetreuungspensum.tarifProNebenmahlzeit = betreuungspensum.tarifProNebenmahlzeit;
        return restBetreuungspensum;
    }

    public abwesenheitToRestObject(restAbwesenheit: any, abwesenheit: TSAbwesenheit): any {
        this.abstractDateRangeEntityToRestObject(restAbwesenheit, abwesenheit);
        return restAbwesenheit;
    }

    public parseBetreuungList(data: Array<any>): TSBetreuung[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
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
            betreuungTS.betreuungsstatus = betreuungFromServer.betreuungsstatus;
            betreuungTS.institutionStammdaten = this.parseInstitutionStammdaten(new TSInstitutionStammdaten(),
                betreuungFromServer.institutionStammdaten);
            betreuungTS.betreuungspensumContainers =
                this.parseBetreuungspensumContainers(betreuungFromServer.betreuungspensumContainers);
            betreuungTS.abwesenheitContainers =
                this.parseAbwesenheitContainers(betreuungFromServer.abwesenheitContainers);
            betreuungTS.erweiterteBetreuungContainer =
                this.parseErweiterteBetreuungContainer(
                    new TSErweiterteBetreuungContainer(),
                    betreuungFromServer.erweiterteBetreuungContainer
                );
            betreuungTS.betreuungNummer = betreuungFromServer.betreuungNummer;
            betreuungTS.verfuegung = this.parseVerfuegung(new TSVerfuegung(), betreuungFromServer.verfuegung);
            betreuungTS.kindFullname = betreuungFromServer.kindFullname;
            betreuungTS.kindNummer = betreuungFromServer.kindNummer;
            betreuungTS.kindId = betreuungFromServer.kindId;
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
            betreuungTS.betreuungspensumAbweichungen =
                this.parseBetreuungspensumAbweichungen(betreuungFromServer.betreuungspensumAbweichungen);
            betreuungTS.anmeldungTagesschuleZeitabschnitts =
                this.parseAnmeldungTagesschuleZeitabschnitts(betreuungFromServer.anmeldungTagesschuleZeitabschnitts);
            betreuungTS.eingewoehnung = betreuungFromServer.eingewoehnung;
            betreuungTS.auszahlungAnEltern = betreuungFromServer.auszahlungAnEltern;
            betreuungTS.begruendungAuszahlungAnInstitution = betreuungFromServer.begruendungAuszahlungAnInstitution;
            betreuungTS.finSitRueckwirkendKorrigiertInThisMutation =
                betreuungFromServer.finSitRueckwirkendKorrigiertInThisMutation;
            return betreuungTS;
        }
        return undefined;
    }

    public parseBetreuungspensumAbweichungen(data: any): TSBetreuungspensumAbweichung[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseBetreuungspensumAbweichung(new TSBetreuungspensumAbweichung(), item))
            : [this.parseBetreuungspensumAbweichung(new TSBetreuungspensumAbweichung(), data)];
    }

    public parseBetreuungspensumAbweichung(
        abweichungTS: TSBetreuungspensumAbweichung,
        abweichungFromServer: any
    ): TSBetreuungspensumAbweichung {
        this.parseAbstractBetreuungspensumEntity(abweichungTS, abweichungFromServer);
        abweichungTS.status = abweichungFromServer.status;
        abweichungTS.vertraglicheKosten = abweichungFromServer.vertraglicheKosten;

        abweichungTS.multiplier = abweichungFromServer.multiplier;

        const pensum = Number((abweichungFromServer.pensum * abweichungFromServer.multiplier).toFixed(2));
        const originalPensum = Number((abweichungFromServer.vertraglichesPensum * abweichungFromServer.multiplier)
            .toFixed(2));

        abweichungTS.vertraglicheHauptmahlzeiten = abweichungFromServer.vertraglicheHauptmahlzeiten;
        abweichungTS.vertraglicheNebenmahlzeiten = abweichungFromServer.vertraglicheNebenmahlzeiten;
        abweichungTS.vertraglicherTarifHaupt = abweichungFromServer.vertraglicherTarifHaupt;
        abweichungTS.vertraglicherTarifNeben = abweichungFromServer.vertraglicherTarifNeben;
        abweichungTS.monatlicheHauptmahlzeiten = abweichungFromServer.monatlicheHauptmahlzeiten;
        abweichungTS.tarifProHauptmahlzeit = this.undefinedOrPositive(abweichungFromServer.tarifProHauptmahlzeit);
        abweichungTS.monatlicheNebenmahlzeiten = abweichungFromServer.monatlicheNebenmahlzeiten;
        abweichungTS.tarifProNebenmahlzeit = this.undefinedOrPositive(abweichungFromServer.tarifProNebenmahlzeit);
        abweichungTS.vertraglichesPensum = originalPensum;
        abweichungTS.pensum = pensum;

        // ugly hack to override @Nonnull Betreuungskostem
        if (abweichungTS.isNew()) {
            abweichungTS.pensum = null;
            abweichungTS.monatlicheBetreuungskosten = null;
        }

        return abweichungTS;
    }

    private undefinedOrPositive(value: number): number | undefined {
        return value > 0 ? value : undefined;
    }

    public parseBetreuungspensumContainers(data: Array<any>): TSBetreuungspensumContainer[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseBetreuungspensumContainer(new TSBetreuungspensumContainer(), item))
            : [this.parseBetreuungspensumContainer(new TSBetreuungspensumContainer(), data)];
    }

    public parseAbwesenheitContainers(data: Array<any>): TSAbwesenheitContainer[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseAbwesenheitContainer(new TSAbwesenheitContainer(), item))
            : [this.parseAbwesenheitContainer(new TSAbwesenheitContainer(), data)];
    }

    public parseBetreuungspensumContainer(
        betPensContainerTS: TSBetreuungspensumContainer,
        betPensContFromServer: any
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

    public parseAbwesenheitContainer(
        abwesenheitContainerTS: TSAbwesenheitContainer,
        abwesenheitContFromServer: any
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

    public parseBetreuungspensum(
        betreuungspensumTS: TSBetreuungspensum,
        betreuungspensumFromServer: any
    ): TSBetreuungspensum {
        if (betreuungspensumFromServer) {
            this.parseAbstractBetreuungspensumEntity(betreuungspensumTS, betreuungspensumFromServer);
            betreuungspensumTS.nichtEingetreten = betreuungspensumFromServer.nichtEingetreten;
            betreuungspensumTS.monatlicheHauptmahlzeiten = betreuungspensumFromServer.monatlicheHauptmahlzeiten;
            betreuungspensumTS.monatlicheNebenmahlzeiten = betreuungspensumFromServer.monatlicheNebenmahlzeiten;
            betreuungspensumTS.tarifProHauptmahlzeit = betreuungspensumFromServer.tarifProHauptmahlzeit;
            betreuungspensumTS.tarifProNebenmahlzeit = betreuungspensumFromServer.tarifProNebenmahlzeit;
            betreuungspensumTS.unitForDisplay = betreuungspensumFromServer.unitForDisplay;
            return betreuungspensumTS;
        }
        return undefined;
    }

    public parseBetreuungsmitteilungPensum(
        betreuungspensumTS: TSBetreuungsmitteilungPensum,
        betreuungspensumFromServer: any
    ): TSBetreuungsmitteilungPensum {
        if (betreuungspensumFromServer) {
            this.parseAbstractBetreuungspensumEntity(betreuungspensumTS, betreuungspensumFromServer);
            betreuungspensumTS.monatlicheHauptmahlzeiten = betreuungspensumFromServer.monatlicheHauptmahlzeiten;
            betreuungspensumTS.monatlicheNebenmahlzeiten = betreuungspensumFromServer.monatlicheNebenmahlzeiten;
            betreuungspensumTS.tarifProHauptmahlzeit = betreuungspensumFromServer.tarifProHauptmahlzeit;
            betreuungspensumTS.tarifProNebenmahlzeit = betreuungspensumFromServer.tarifProNebenmahlzeit;
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

    public erweiterteBetreuungContainerToRestObject(
        restErweiterteBetreuungContainer: any,
        erweiterteBetreuungContainer: TSErweiterteBetreuungContainer
    ): TSErweiterteBetreuungContainer {
        this.abstractMutableEntityToRestObject(restErweiterteBetreuungContainer, erweiterteBetreuungContainer);

        if (erweiterteBetreuungContainer.erweiterteBetreuungGS) {
            restErweiterteBetreuungContainer.erweiterteBetreuungGS =
                this.erweiterteBetreuungToRestObject({}, erweiterteBetreuungContainer.erweiterteBetreuungGS);
        }
        if (erweiterteBetreuungContainer.erweiterteBetreuungJA) {
            restErweiterteBetreuungContainer.erweiterteBetreuungJA =
                this.erweiterteBetreuungToRestObject({}, erweiterteBetreuungContainer.erweiterteBetreuungJA);
        }
        return restErweiterteBetreuungContainer;
    }

    public parseErweiterteBetreuungContainer(
        containerTS: TSErweiterteBetreuungContainer,
        containerFromServer: any
    ): TSErweiterteBetreuungContainer {
        if (containerFromServer) {
            this.parseAbstractMutableEntity(containerTS, containerFromServer);

            containerTS.erweiterteBetreuungGS =
                this.parseErweiterteBetreuung(containerTS.erweiterteBetreuungGS || new TSErweiterteBetreuung(),
                    containerFromServer.erweiterteBetreuungGS);
            containerTS.erweiterteBetreuungJA =
                this.parseErweiterteBetreuung(containerTS.erweiterteBetreuungJA || new TSErweiterteBetreuung(),
                    containerFromServer.erweiterteBetreuungJA);
            return containerTS;
        }
        return undefined;
    }

    public erweiterteBetreuungToRestObject(
        restErweiterteBetreuung: any,
        erweiterteBetreuung: TSErweiterteBetreuung
    ): TSErweiterteBetreuung {
        this.abstractMutableEntityToRestObject(restErweiterteBetreuung, erweiterteBetreuung);
        restErweiterteBetreuung.erweiterteBeduerfnisse = erweiterteBetreuung.erweiterteBeduerfnisse;
        restErweiterteBetreuung.erweiterteBeduerfnisseBestaetigt =
            erweiterteBetreuung.erweiterteBeduerfnisseBestaetigt;
        restErweiterteBetreuung.betreuungInGemeinde = erweiterteBetreuung.betreuungInGemeinde;
        restErweiterteBetreuung.keineKesbPlatzierung = erweiterteBetreuung.keineKesbPlatzierung;
        restErweiterteBetreuung.kitaPlusZuschlag = erweiterteBetreuung.kitaPlusZuschlag;
        restErweiterteBetreuung.kitaPlusZuschlagBestaetigt = erweiterteBetreuung.kitaPlusZuschlagBestaetigt;
        restErweiterteBetreuung.erweitereteBeduerfnisseBetrag = erweiterteBetreuung.erweitereteBeduerfnisseBetrag;
        restErweiterteBetreuung.anspruchFachstelleWennPensumUnterschritten =
            erweiterteBetreuung.anspruchFachstelleWennPensumUnterschritten;
        if (erweiterteBetreuung.fachstelle) {
            restErweiterteBetreuung.fachstelle = this.fachstelleToRestObject({}, erweiterteBetreuung.fachstelle);
        }
        restErweiterteBetreuung.sprachfoerderungBestaetigt = erweiterteBetreuung.sprachfoerderungBestaetigt;
        return restErweiterteBetreuung;
    }

    public parseErweiterteBetreuung(
        erweiterteBetreuungTS: TSErweiterteBetreuung,
        erweiterteBetreuungFromServer: any
    ): TSErweiterteBetreuung {
        if (erweiterteBetreuungFromServer) {
            this.parseAbstractMutableEntity(erweiterteBetreuungFromServer, erweiterteBetreuungTS);
            erweiterteBetreuungTS.erweiterteBeduerfnisse = erweiterteBetreuungFromServer.erweiterteBeduerfnisse;
            erweiterteBetreuungTS.erweiterteBeduerfnisseBestaetigt =
                erweiterteBetreuungFromServer.erweiterteBeduerfnisseBestaetigt;
            erweiterteBetreuungTS.keineKesbPlatzierung = erweiterteBetreuungFromServer.keineKesbPlatzierung;
            erweiterteBetreuungTS.kitaPlusZuschlag = erweiterteBetreuungFromServer.kitaPlusZuschlag;
            erweiterteBetreuungTS.kitaPlusZuschlagBestaetigt = erweiterteBetreuungFromServer.kitaPlusZuschlagBestaetigt;
            erweiterteBetreuungTS.betreuungInGemeinde = erweiterteBetreuungFromServer.betreuungInGemeinde;
            erweiterteBetreuungTS.anspruchFachstelleWennPensumUnterschritten =
                erweiterteBetreuungFromServer.anspruchFachstelleWennPensumUnterschritten;
            erweiterteBetreuungTS.erweitereteBeduerfnisseBetrag =
                erweiterteBetreuungFromServer.erweitereteBeduerfnisseBetrag;
            if (erweiterteBetreuungFromServer.fachstelle) {
                erweiterteBetreuungTS.fachstelle =
                    this.parseFachstelle(new TSFachstelle(), erweiterteBetreuungFromServer.fachstelle);
            }
            erweiterteBetreuungTS.sprachfoerderungBestaetigt = erweiterteBetreuungFromServer.sprachfoerderungBestaetigt;

            return erweiterteBetreuungTS;
        }
        return undefined;
    }

    private parseErwerbspensenContainers(data: Array<any>): TSErwerbspensumContainer[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseErwerbspensumContainer(new TSErwerbspensumContainer(), item))
            : [this.parseErwerbspensumContainer(new TSErwerbspensumContainer(), data)];
    }

    public gesuchsperiodeToRestObject(restGesuchsperiode: any, gesuchsperiode: TSGesuchsperiode): any {
        if (gesuchsperiode) {
            this.abstractDateRangeEntityToRestObject(restGesuchsperiode, gesuchsperiode);
            restGesuchsperiode.status = gesuchsperiode.status;
            return restGesuchsperiode;
        }
        return undefined;
    }

    public parseGesuchsperiode(gesuchsperiodeTS: TSGesuchsperiode, gesuchsperiodeFromServer: any
    ): TSGesuchsperiode | undefined {
        if (gesuchsperiodeFromServer) {
            this.parseDateRangeEntity(gesuchsperiodeTS, gesuchsperiodeFromServer);
            gesuchsperiodeTS.status = gesuchsperiodeFromServer.status;
            return gesuchsperiodeTS;
        }
        return undefined;
    }

    public parseGesuchsperioden(data: any): TSGesuchsperiode[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
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
        restPendenz.internePendenz = pendenz.internePendenz;
        restPendenz.dokumenteHochgeladen = pendenz.dokumenteHochgeladen;
        restPendenz.fallId = pendenz.fallId;
        restPendenz.gemeindeId = pendenz.gemeindeId;
        restPendenz.isSozialdienst = pendenz.isSozialdienst;
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
        antragTS.internePendenz = antragFromServer.internePendenz;
        antragTS.internePendenzAbgelaufen = antragFromServer.internePendenzAbgelaufen;
        antragTS.dokumenteHochgeladen = antragFromServer.dokumenteHochgeladen;
        antragTS.gemeinde = antragFromServer.gemeinde;
        antragTS.fallId = antragFromServer.fallId;
        antragTS.gemeindeId = antragFromServer.gemeindeId;
        antragTS.isSozialdienst = antragFromServer.isSozialdienst;
        antragTS.begruendungMutation = antragFromServer.begruendungMutation;
        antragTS.gesuchsperiodeString = antragFromServer.gesuchsperiodeString;
        return antragTS;
    }

    public parseFallAntragDTO(fallAntragTS: TSFallAntragDTO, antragFromServer: any): TSFallAntragDTO {
        fallAntragTS.fallId = antragFromServer.fallId;
        fallAntragTS.dossierId = antragFromServer.dossierId;
        fallAntragTS.fallNummer = antragFromServer.fallNummer;
        fallAntragTS.familienName = antragFromServer.familienName;
        return fallAntragTS;
    }

    public parseAntragDTOs(data: any): TSAntragDTO[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
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
        if (!data) {
            return [];
        }
        return Array.isArray(data)
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

    public benutzerNoDetailsToRestObject(user: any, userTS: TSBenutzerNoDetails): TSBenutzerNoDetails {
        if (!userTS) {
            return undefined;
        }
        user.username = userTS.username;
        user.nachname = userTS.nachname;
        user.vorname = userTS.vorname;
        user.gemeindeIds = userTS.gemeindeIds;
        return user;
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
            userTS.status = userFromServer.status;
            userTS.currentBerechtigung =
                this.parseBerechtigung(new TSBerechtigung(), userFromServer.currentBerechtigung);
            userTS.berechtigungen = this.parseBerechtigungen(userFromServer.berechtigungen);
            return userTS;
        }
        return undefined;
    }

    public parseUserNoDetails(userTS: TSBenutzerNoDetails, userFromServer: any): TSBenutzerNoDetails {
        if (userFromServer) {
            userTS.nachname = userFromServer.nachname;
            userTS.vorname = userFromServer.vorname;
            userTS.username = userFromServer.username;
            userTS.gemeindeIds = userFromServer.gemeindeIds;
            return userTS;
        }
        return undefined;
    }

    public parseBerechtigungen(data: Array<any>): TSBerechtigung[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseBerechtigung(new TSBerechtigung(), item))
            : [this.parseBerechtigung(new TSBerechtigung(), data)];
    }

    public parseUserList(data: any): TSBenutzer[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseUser(new TSBenutzer(), item))
            : [this.parseUser(new TSBenutzer(), data)];
    }

    public parseUserNoDetailsList(data: any): TSBenutzerNoDetails[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseUserNoDetails(new TSBenutzerNoDetails(), item))
            : [this.parseUserNoDetails(new TSBenutzerNoDetails(), data)];
    }

    public berechtigungToRestObject(berechtigung: any, berechtigungTS: TSBerechtigung): any {
        if (berechtigungTS) {
            this.abstractDateRangeEntityToRestObject(berechtigung, berechtigungTS);
            berechtigung.role = berechtigungTS.role;
            berechtigung.traegerschaft = this.traegerschaftToRestObject({}, berechtigungTS.traegerschaft);
            berechtigung.institution = this.institutionToRestObject({}, berechtigungTS.institution);
            // Gemeinden
            berechtigung.gemeindeList = this.gemeindeListToRestObject(berechtigungTS.gemeindeList);
            berechtigung.sozialdienst = this.sozialdienstToRestObject({}, berechtigungTS.sozialdienst);
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
            berechtigungTS.sozialdienst =
                this.parseSozialdienst(new TSSozialdienst(), berechtigungFromServer.sozialdienst);
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
            historyTS.sozialdienst = this.parseSozialdienst(new TSSozialdienst(), historyFromServer.sozialdienst);
            return historyTS;
        }
        return undefined;
    }

    public parseBerechtigungHistoryList(data: any): TSBerechtigungHistory[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
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
        if (!data) {
            return [];
        }
        return Array.isArray(data)
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
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseDokument(new TSDokument(), item))
            : [this.parseDokument(new TSDokument(), data)];
    }

    private parseDokument(dokument: TSDokument, dokumentFromServer: any): TSDokument {
        if (dokumentFromServer) {
            this.parseTSFileDokument(dokument, dokumentFromServer);
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
            verfuegungTS.veraenderungVerguenstigungGegenueberVorgaenger =
                verfuegungFromServer.veraenderungVerguenstigungGegenueberVorgaenger;
            verfuegungTS.ignorable = verfuegungFromServer.ignorable;
            verfuegungTS.korrekturAusbezahltEltern = verfuegungFromServer.korrekturAusbezahltEltern;
            verfuegungTS.korrekturAusbezahltInstitution = verfuegungFromServer.korrekturAusbezahltInstitution;
            return verfuegungTS;
        }
        return undefined;
    }

    private parseVerfuegungZeitabschnitte(data: Array<any>): TSVerfuegungZeitabschnitt[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseVerfuegungZeitabschnitt(new TSVerfuegungZeitabschnitt(), item))
            : [this.parseVerfuegungZeitabschnitt(new TSVerfuegungZeitabschnitt(), data)];
    }

    public parseVerfuegungZeitabschnitt(
        verfuegungZeitabschnittTS: TSVerfuegungZeitabschnitt,
        zeitabschnittFromServer: any
    ): TSVerfuegungZeitabschnitt {
        if (zeitabschnittFromServer) {
            this.parseDateRangeEntity(verfuegungZeitabschnittTS, zeitabschnittFromServer);
            verfuegungZeitabschnittTS.abzugFamGroesse = zeitabschnittFromServer.abzugFamGroesse;
            verfuegungZeitabschnittTS.anspruchberechtigtesPensum = zeitabschnittFromServer.anspruchberechtigtesPensum;
            verfuegungZeitabschnittTS.anspruchsberechtigteAnzahlZeiteinheiten =
                zeitabschnittFromServer.anspruchsberechtigteAnzahlZeiteinheiten;
            verfuegungZeitabschnittTS.anspruchspensumRest = zeitabschnittFromServer.anspruchspensumRest;
            verfuegungZeitabschnittTS.betreuungspensumProzent = zeitabschnittFromServer.betreuungspensumProzent;
            verfuegungZeitabschnittTS.betreuungspensumZeiteinheit = zeitabschnittFromServer.betreuungspensumZeiteinheit;
            verfuegungZeitabschnittTS.bgPensum = zeitabschnittFromServer.bgPensum;
            verfuegungZeitabschnittTS.einkommensjahr = zeitabschnittFromServer.einkommensjahr;
            verfuegungZeitabschnittTS.elternbeitrag = zeitabschnittFromServer.elternbeitrag;
            verfuegungZeitabschnittTS.erwerbspensumGS1 = zeitabschnittFromServer.erwerbspensumGS1;
            verfuegungZeitabschnittTS.erwerbspensumGS2 = zeitabschnittFromServer.erwerbspensumGS2;
            verfuegungZeitabschnittTS.famGroesse = zeitabschnittFromServer.famGroesse;
            verfuegungZeitabschnittTS.kategorieKeinPensum = zeitabschnittFromServer.kategorieKeinPensum;
            verfuegungZeitabschnittTS.kategorieMaxEinkommen = zeitabschnittFromServer.kategorieMaxEinkommen;
            verfuegungZeitabschnittTS.massgebendesEinkommenVorAbzugFamgr =
                zeitabschnittFromServer.massgebendesEinkommenVorAbzugFamgr;
            verfuegungZeitabschnittTS.minimalerElternbeitrag = zeitabschnittFromServer.minimalerElternbeitrag;
            verfuegungZeitabschnittTS.minimalerElternbeitragGekuerzt =
                zeitabschnittFromServer.minimalerElternbeitragGekuerzt;
            verfuegungZeitabschnittTS.minimalesEwpUnterschritten = zeitabschnittFromServer.minimalesEwpUnterschritten;
            verfuegungZeitabschnittTS.sameAusbezahlteVerguenstigung =
                zeitabschnittFromServer.sameAusbezahlteVerguenstigung;
            verfuegungZeitabschnittTS.sameAusbezahlteMahlzeiten = zeitabschnittFromServer.sameAusbezahlteMahlzeiten;
            verfuegungZeitabschnittTS.sameVerfuegteMahlzeitenVerguenstigung =
                zeitabschnittFromServer.sameVerfuegteMahlzeitenVerguenstigung;
            verfuegungZeitabschnittTS.sameVerfuegteVerfuegungsrelevanteDaten =
                zeitabschnittFromServer.sameVerfuegteVerfuegungsrelevanteDaten;
            verfuegungZeitabschnittTS.verfuegteAnzahlZeiteinheiten =
                zeitabschnittFromServer.verfuegteAnzahlZeiteinheiten;
            verfuegungZeitabschnittTS.verguenstigung = zeitabschnittFromServer.verguenstigung;
            verfuegungZeitabschnittTS.verguenstigungProZeiteinheit =
                zeitabschnittFromServer.verguenstigungProZeiteinheit;
            verfuegungZeitabschnittTS.verguenstigungOhneBeruecksichtigungMinimalbeitrag =
                zeitabschnittFromServer.verguenstigungOhneBeruecksichtigungMinimalbeitrag;
            verfuegungZeitabschnittTS.verguenstigungOhneBeruecksichtigungVollkosten =
                zeitabschnittFromServer.verguenstigungOhneBeruecksichtigungVollkosten;
            verfuegungZeitabschnittTS.vollkosten = zeitabschnittFromServer.vollkosten;
            verfuegungZeitabschnittTS.zahlungsstatusInstitution = zeitabschnittFromServer.zahlungsstatusInstitution;
            verfuegungZeitabschnittTS.zahlungsstatusAntragsteller =
                zeitabschnittFromServer.zahlungsstatusAntragsteller;
            verfuegungZeitabschnittTS.zeiteinheit = zeitabschnittFromServer.zeiteinheit;
            verfuegungZeitabschnittTS.zuSpaetEingereicht = zeitabschnittFromServer.zuSpaetEingereicht;
            verfuegungZeitabschnittTS.tsCalculationResultMitPaedagogischerBetreuung =
                this.parseTsCalculationResult(zeitabschnittFromServer.tsCalculationResultMitPaedagogischerBetreuung);
            verfuegungZeitabschnittTS.tsCalculationResultOhnePaedagogischerBetreuung =
                this.parseTsCalculationResult(zeitabschnittFromServer.tsCalculationResultOhnePaedagogischerBetreuung);
            verfuegungZeitabschnittTS.verguenstigungMahlzeitTotal = zeitabschnittFromServer.verguenstigungMahlzeitTotal;
            verfuegungZeitabschnittTS.auszahlungAnEltern = zeitabschnittFromServer.auszahlungAnEltern;
            verfuegungZeitabschnittTS.beitragshoeheProzent = zeitabschnittFromServer.beitragshoeheProzent;

            if (zeitabschnittFromServer.verfuegungZeitabschnittBemerkungList) {
                zeitabschnittFromServer.verfuegungZeitabschnittBemerkungList.forEach((bemerkung: any) => {
                    verfuegungZeitabschnittTS.bemerkungen.push(
                        this.parseVerfuegungZeitabschnittBemerkung(bemerkung));
                });
            }

            return verfuegungZeitabschnittTS;
        }
        return undefined;
    }

    private parseVerfuegungZeitabschnittBemerkung(zeitabschnittBemerkungFromServer: any): TSVerfuegungZeitabschnittBemerkung {
        if (zeitabschnittBemerkungFromServer) {
            const result = new TSVerfuegungZeitabschnittBemerkung();
            this.parseDateRangeEntity(zeitabschnittBemerkungFromServer, result);
            result.bemerkung = zeitabschnittBemerkungFromServer.bemerkung;
            return result;
        }
        return undefined;
    }

    public parseTsCalculationResult(
        resultFromServer: any
    ): TSTsCalculationResult {
        if (resultFromServer) {
            const resultTS = new TSTsCalculationResult();
            resultTS.betreuungszeitProWoche = resultFromServer.betreuungszeitProWoche;
            resultTS.betreuungszeitProWocheFormatted = resultFromServer.betreuungszeitProWocheFormatted;
            resultTS.verpflegungskosten = resultFromServer.verpflegungskosten;
            resultTS.verpflegungskostenVerguenstigt = resultFromServer.verpflegungskostenVerguenstigt;
            resultTS.gebuehrProStunde = resultFromServer.gebuehrProStunde;
            resultTS.totalKostenProWoche = resultFromServer.totalKostenProWoche;
            return resultTS;
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
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseWizardStep(new TSWizardStep(), item))
            : [this.parseWizardStep(new TSWizardStep(), data)];
    }

    public parseAntragStatusHistoryCollection(data: Array<any>): TSAntragStatusHistory[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseAntragStatusHistory(new TSAntragStatusHistory(), item))
            : [this.parseAntragStatusHistory(new TSAntragStatusHistory(), data)];
    }

    public parseAntragStatusHistory(
        antragStatusHistoryTS: TSAntragStatusHistory,
        antragStatusHistoryFromServer: any
    ): TSAntragStatusHistory {
        this.parseAbstractMutableEntity(antragStatusHistoryTS, antragStatusHistoryFromServer);
        antragStatusHistoryTS.gesuchId = antragStatusHistoryFromServer.gesuchId;
        antragStatusHistoryTS.benutzer = this.parseUser(new TSBenutzer(), antragStatusHistoryFromServer.benutzer);
        antragStatusHistoryTS.timestampVon = DateUtil.localDateTimeToMoment(antragStatusHistoryFromServer.timestampVon);
        antragStatusHistoryTS.timestampBis = DateUtil.localDateTimeToMoment(antragStatusHistoryFromServer.timestampBis);
        antragStatusHistoryTS.status = antragStatusHistoryFromServer.status;
        return antragStatusHistoryTS;
    }

    public antragStatusHistoryToRestObject(
        restAntragStatusHistory: any,
        antragStatusHistory: TSAntragStatusHistory
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
        if (!data) {
            return [];
        }
        return Array.isArray(data)
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
            if (finSitModel.familienSituation) {
                restFinSitModel.familiensituation =
                    this.familiensituationToRestObject({}, finSitModel.familienSituation);
            }
            restFinSitModel.finanzielleSituationTyp = finSitModel.finanzielleSituationTyp;
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

    public parseGesuchstellerContainer(
        gesuchstellerContTS: TSGesuchstellerContainer,
        gesuchstellerContFromServer: any
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
            ? jobWrapper.jobs.map((item: any) => this.parseWorkJob(new TSWorkJob(), item))
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

    private parseBatchJobInformation(
        testBatchJobInfo: TSBatchJobInformation,
        batchJobInfoFromServer: any
    ): TSBatchJobInformation {
        if (!batchJobInfoFromServer) {
            return undefined;
        }

        testBatchJobInfo.batchStatus = batchJobInfoFromServer.batchStatus;
        testBatchJobInfo.createTime = DateUtil.localDateTimeToMoment(batchJobInfoFromServer.createTime);
        testBatchJobInfo.endTime = DateUtil.localDateTimeToMoment(batchJobInfoFromServer.endTime);
        testBatchJobInfo.executionId = batchJobInfoFromServer.executionId;
        testBatchJobInfo.jobName = batchJobInfoFromServer.jobName;
        testBatchJobInfo.lastUpdatedTime = DateUtil.localDateTimeToMoment(batchJobInfoFromServer.lastUpdatedTime);
        testBatchJobInfo.startTime = DateUtil.localDateTimeToMoment(batchJobInfoFromServer.startTime);

        return testBatchJobInfo;
    }

    public parseMitteilung(tsMitteilung: TSMitteilung, mitteilungFromServer: any): TSMitteilung {
        if (mitteilungFromServer) {
            this.parseAbstractMutableEntity(tsMitteilung, mitteilungFromServer);
            tsMitteilung.dossier = this.parseDossier(new TSDossier(), mitteilungFromServer.dossier);
            if (mitteilungFromServer.betreuung) {
                tsMitteilung.betreuung = this.parseBetreuung(new TSBetreuung(), mitteilungFromServer.betreuung);
            }
            if (mitteilungFromServer.finanzielleSituation) {
                tsMitteilung.finanzielleSituation = this.parseFinanzielleSituation(
                    new TSFinanzielleSituation(),
                    mitteilungFromServer.finanzielleSituation
                );
            }
            if (mitteilungFromServer.institution) {
                tsMitteilung.institution = this.parseInstitution(new TSInstitution(), mitteilungFromServer.institution);
            }
            tsMitteilung.senderTyp = mitteilungFromServer.senderTyp;
            tsMitteilung.empfaengerTyp = mitteilungFromServer.empfaengerTyp;
            tsMitteilung.sender = this.parseUser(new TSBenutzer(), mitteilungFromServer.sender);
            tsMitteilung.empfaenger = this.parseUser(new TSBenutzer(), mitteilungFromServer.empfaenger);
            tsMitteilung.subject = mitteilungFromServer.subject;
            tsMitteilung.message = mitteilungFromServer.message;
            tsMitteilung.mitteilungStatus = mitteilungFromServer.mitteilungStatus;
            tsMitteilung.sentDatum = DateUtil.localDateTimeToMoment(mitteilungFromServer.sentDatum);
            tsMitteilung.mitteilungTyp = mitteilungFromServer.mitteilungTyp;
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
            if (tsMitteilung.institution) {
                restMitteilung.institution = this.institutionToRestObject({}, tsMitteilung.institution);
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

        return mitteilungen.map(m => this.isBetreuungsmitteilung(m) ?
                this.parseBetreuungsmitteilung(new TSBetreuungsmitteilung(), m) :
                this.parseMitteilung(new TSMitteilung(), m));
    }

    public betreuungsmitteilungToRestObject(
        restBetreuungsmitteilung: any,
        tsBetreuungsmitteilung: TSBetreuungsmitteilung
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

    public parseBetreuungsmitteilung(
        tsBetreuungsmitteilung: TSBetreuungsmitteilung,
        betreuungsmitteilungFromServer: any
    ): TSBetreuungsmitteilung {
        if (betreuungsmitteilungFromServer) {
            this.parseMitteilung(tsBetreuungsmitteilung, betreuungsmitteilungFromServer);
            tsBetreuungsmitteilung.applied = betreuungsmitteilungFromServer.applied;
            tsBetreuungsmitteilung.errorMessage = betreuungsmitteilungFromServer.errorMessage;

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
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseZahlungsauftrag(new TSZahlungsauftrag(), item))
            : [];
    }

    public parseZahlungsauftrag(
        tsZahlungsauftrag: TSZahlungsauftrag,
        zahlungsauftragFromServer: any
    ): TSZahlungsauftrag {
        if (zahlungsauftragFromServer) {
            this.parseDateRangeEntity(tsZahlungsauftrag, zahlungsauftragFromServer);

            tsZahlungsauftrag.zahlungslaufTyp = zahlungsauftragFromServer.zahlungslaufTyp;
            tsZahlungsauftrag.status = zahlungsauftragFromServer.status;
            tsZahlungsauftrag.beschrieb = zahlungsauftragFromServer.beschrieb;
            tsZahlungsauftrag.datumFaellig = DateUtil.localDateToMoment(zahlungsauftragFromServer.datumFaellig);
            tsZahlungsauftrag.datumGeneriert = DateUtil.localDateTimeToMoment(zahlungsauftragFromServer.datumGeneriert);
            tsZahlungsauftrag.betragTotalAuftrag = zahlungsauftragFromServer.betragTotalAuftrag;
            tsZahlungsauftrag.hasNegativeZahlungen = zahlungsauftragFromServer.hasNegativeZahlungen;
            tsZahlungsauftrag.zahlungen = this.parseZahlungen(zahlungsauftragFromServer.zahlungen);
            tsZahlungsauftrag.gemeinde = this.parseGemeinde(new TSGemeinde(), zahlungsauftragFromServer.gemeinde);

            return tsZahlungsauftrag;
        }
        return undefined;
    }

    public parseZahlungen(data: any): TSZahlung[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseZahlung(new TSZahlung(), item))
            : [];
    }

    public parseZahlung(tsZahlung: TSZahlung, zahlungFromServer: any): TSZahlung {
        if (zahlungFromServer) {
            this.parseAbstractMutableEntity(tsZahlung, zahlungFromServer);

            tsZahlung.betragTotalZahlung = zahlungFromServer.betragTotalZahlung;
            tsZahlung.empfaengerName = zahlungFromServer.empfaengerName;
            tsZahlung.betreuungsangebotTyp = zahlungFromServer.betreuungsangebotTyp;
            tsZahlung.status = zahlungFromServer.status;

            return tsZahlung;
        }
        return undefined;
    }

    public parseEWKResultat(ewkResultatTS: TSEWKResultat, ewkResultatFromServer: any): any {
        if (ewkResultatFromServer) {
            ewkResultatTS.personen = this.parseEWKPersonList(ewkResultatFromServer.personen);
            return ewkResultatTS;
        }
        return undefined;
    }

    private parseEWKPersonList(data: any): TSEWKPerson[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseEWKPerson(new TSEWKPerson(), item))
            : [];
    }

    private parseEWKPerson(tsEWKPerson: TSEWKPerson, ewkPersonFromServer: any): TSEWKPerson {
        if (ewkPersonFromServer) {
            tsEWKPerson.personID = ewkPersonFromServer.personID;
            tsEWKPerson.nachname = ewkPersonFromServer.nachname;
            tsEWKPerson.vorname = ewkPersonFromServer.vorname;
            tsEWKPerson.geburtsdatum = DateUtil.localDateToMoment(ewkPersonFromServer.geburtsdatum);
            tsEWKPerson.zuzugsdatum = DateUtil.localDateToMoment(ewkPersonFromServer.zuzugsdatum);
            tsEWKPerson.wegzugsdatum = DateUtil.localDateToMoment(ewkPersonFromServer.wegzugsdatum);
            tsEWKPerson.zivilstand = ewkPersonFromServer.zivilstand;
            tsEWKPerson.zivilstandsdatum = DateUtil.localDateToMoment(ewkPersonFromServer.zivilstandsdatum);
            tsEWKPerson.geschlecht = ewkPersonFromServer.geschlecht;
            tsEWKPerson.adresse = this.parseEWKAdresse(new TSEWKAdresse(), ewkPersonFromServer.adresse);
            tsEWKPerson.beziehungen = this.parseEWKBeziehungList(ewkPersonFromServer.beziehungen);
            tsEWKPerson.gesuchsteller = ewkPersonFromServer.gesuchsteller;
            tsEWKPerson.kind = ewkPersonFromServer.kind;
            tsEWKPerson.haushalt = ewkPersonFromServer.haushalt;
            tsEWKPerson.nichtGefunden = ewkPersonFromServer.nichtGefunden;
            return tsEWKPerson;
        }
        return undefined;
    }

    private parseEWKAdresse(tsEWKAdresse: TSEWKAdresse, ewkAdresseFromServer: any): TSEWKAdresse {
        if (ewkAdresseFromServer) {
            tsEWKAdresse.adresszusatz1 = ewkAdresseFromServer.adresszusatz1;
            tsEWKAdresse.adresszusatz2 = ewkAdresseFromServer.adresszusatz2;
            tsEWKAdresse.hausnummer = ewkAdresseFromServer.hausnummer;
            tsEWKAdresse.wohnungsnummer = ewkAdresseFromServer.wohnungsnummer;
            tsEWKAdresse.strasse = ewkAdresseFromServer.strasse;
            tsEWKAdresse.postleitzahl = ewkAdresseFromServer.postleitzahl;
            tsEWKAdresse.ort = ewkAdresseFromServer.ort;
            tsEWKAdresse.gebiet = ewkAdresseFromServer.gebiet;
            return tsEWKAdresse;
        }
        return undefined;
    }

    private parseEWKBeziehungList(data: any): TSEWKBeziehung[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseEWKBeziehung(new TSEWKBeziehung(), item))
            : [];
    }

    private parseEWKBeziehung(tsEWKBeziehung: TSEWKBeziehung, ewkBeziehungFromServer: any): TSEWKBeziehung {
        if (ewkBeziehungFromServer) {
            tsEWKBeziehung.beziehungstyp = ewkBeziehungFromServer.beziehungstyp;
            tsEWKBeziehung.personID = ewkBeziehungFromServer.personID;
            tsEWKBeziehung.nachname = ewkBeziehungFromServer.nachname;
            tsEWKBeziehung.vorname = ewkBeziehungFromServer.vorname;
            tsEWKBeziehung.geburtsdatum = DateUtil.localDateToMoment(ewkBeziehungFromServer.geburtsdatum);
            tsEWKBeziehung.adresse = this.parseEWKAdresse(new TSEWKAdresse(), ewkBeziehungFromServer.adresse);
            return tsEWKBeziehung;
        }
        return undefined;
    }

    public parseModuleTagesschuleArray(data: Array<any>): TSModulTagesschule[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseModulTagesschule(new TSModulTagesschule(), item))
            : [this.parseModulTagesschule(new TSModulTagesschule(), data)];
    }

    private parseModulTagesschule(modulTagesschuleTS: TSModulTagesschule, modulFromServer: any): TSModulTagesschule {
        if (modulFromServer) {
            this.parseAbstractEntity(modulTagesschuleTS, modulFromServer);
            modulTagesschuleTS.wochentag = modulFromServer.wochentag;
            return modulTagesschuleTS;
        }
        return undefined;
    }

    public parseEinstellungenTagesschuleArray(data: Array<any>): TSEinstellungenTagesschule[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseEinstellungenTagesschule(new TSEinstellungenTagesschule(), item))
            : [this.parseEinstellungenTagesschule(new TSEinstellungenTagesschule(), data)];
    }

    private parseEinstellungenTagesschule(
        einstellungenTagesschuleTS: TSEinstellungenTagesschule, einstellungFromServer: any
    ): TSEinstellungenTagesschule {
        if (einstellungFromServer) {
            this.parseAbstractEntity(einstellungenTagesschuleTS, einstellungFromServer);
            einstellungenTagesschuleTS.gesuchsperiode =
                this.parseGesuchsperiode(new TSGesuchsperiode(), einstellungFromServer.gesuchsperiode);
            einstellungenTagesschuleTS.modulTagesschuleTyp = einstellungFromServer.modulTagesschuleTyp;
            einstellungenTagesschuleTS.modulTagesschuleGroups =
                this.parseModuleTagesschuleGroupsArray(einstellungFromServer.modulTagesschuleGroups);
            einstellungenTagesschuleTS.erlaeuterung = einstellungFromServer.erlaeuterung;
            einstellungenTagesschuleTS.tagi = einstellungFromServer.tagi;
            return einstellungenTagesschuleTS;
        }
        return undefined;
    }

    private einstellungenTagesschuleArrayToRestObject(data: Array<TSEinstellungenTagesschule>): any[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.einstellungenTagesschuleToRestObject({}, item))
            : [];
    }

    private einstellungenTagesschuleToRestObject(
        restEinstellung: any, einstellungTagesschuleTS: TSEinstellungenTagesschule
    ): any {
        if (einstellungTagesschuleTS) {
            this.abstractEntityToRestObject(restEinstellung, einstellungTagesschuleTS);
            restEinstellung.gesuchsperiode =
                this.gesuchsperiodeToRestObject({}, einstellungTagesschuleTS.gesuchsperiode);
            restEinstellung.modulTagesschuleTyp = einstellungTagesschuleTS.modulTagesschuleTyp;
            restEinstellung.modulTagesschuleGroups =
                this.moduleTagesschuleGroupsArrayToRestObject(einstellungTagesschuleTS.modulTagesschuleGroups);
            restEinstellung.erlaeuterung = einstellungTagesschuleTS.erlaeuterung;
            restEinstellung.tagi = einstellungTagesschuleTS.tagi;
            return restEinstellung;
        }
        return undefined;
    }

    public parseModuleTagesschuleGroupsArray(data: Array<any>): TSModulTagesschuleGroup[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseModulTagesschuleGroup(new TSModulTagesschuleGroup(), item))
            : [this.parseModulTagesschuleGroup(new TSModulTagesschuleGroup(), data)];
    }

    private parseModulTagesschuleGroup(modulTagesschuleGroupTS: TSModulTagesschuleGroup, modulGroupFromServer: any
    ): TSModulTagesschuleGroup {
        if (modulGroupFromServer) {
            this.parseAbstractEntity(modulTagesschuleGroupTS, modulGroupFromServer);
            modulTagesschuleGroupTS.modulTagesschuleName = modulGroupFromServer.modulTagesschuleName;
            modulTagesschuleGroupTS.identifier = modulGroupFromServer.identifier;
            modulTagesschuleGroupTS.fremdId = modulGroupFromServer.fremdId;
            if (modulGroupFromServer.bezeichnung) {
                modulTagesschuleGroupTS.bezeichnung = this.parseTextRessource(
                    new TSTextRessource(), modulGroupFromServer.bezeichnung);
            }
            modulTagesschuleGroupTS.zeitVon = modulGroupFromServer.zeitVon;
            modulTagesschuleGroupTS.zeitBis = modulGroupFromServer.zeitBis;
            modulTagesschuleGroupTS.verpflegungskosten = modulGroupFromServer.verpflegungskosten;
            modulTagesschuleGroupTS.intervall = modulGroupFromServer.intervall;
            modulTagesschuleGroupTS.wirdPaedagogischBetreut = modulGroupFromServer.wirdPaedagogischBetreut;
            modulTagesschuleGroupTS.reihenfolge = modulGroupFromServer.reihenfolge;

            modulTagesschuleGroupTS.module =
                this.parseModuleTagesschuleArray(modulGroupFromServer.module);

            return modulTagesschuleGroupTS;
        }
        return undefined;
    }

    private moduleTagesschuleGroupsArrayToRestObject(data: Array<TSModulTagesschuleGroup>): any[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.modulTagesschuleGroupToRestObject({}, item))
            : [];
    }

    private modulTagesschuleGroupToRestObject(restModulGroup: any, modulTagesschuleGroupTS: TSModulTagesschuleGroup
    ): any {
        if (modulTagesschuleGroupTS) {
            this.abstractEntityToRestObject(restModulGroup, modulTagesschuleGroupTS);
            restModulGroup.modulTagesschuleName = modulTagesschuleGroupTS.modulTagesschuleName;
            restModulGroup.identifier = modulTagesschuleGroupTS.identifier;
            restModulGroup.fremdId = modulTagesschuleGroupTS.fremdId;
            if (modulTagesschuleGroupTS.bezeichnung) {
                restModulGroup.bezeichnung = this.textRessourceToRestObject({}, modulTagesschuleGroupTS.bezeichnung);
            }
            restModulGroup.zeitVon = modulTagesschuleGroupTS.zeitVon;
            restModulGroup.zeitBis = modulTagesschuleGroupTS.zeitBis;
            restModulGroup.verpflegungskosten = modulTagesschuleGroupTS.verpflegungskosten;
            restModulGroup.intervall = modulTagesschuleGroupTS.intervall;
            restModulGroup.wirdPaedagogischBetreut = modulTagesschuleGroupTS.wirdPaedagogischBetreut;
            restModulGroup.reihenfolge = modulTagesschuleGroupTS.reihenfolge;
            restModulGroup.module = this.moduleTagesschuleArrayToRestObject(modulTagesschuleGroupTS.module);
            return restModulGroup;
        }
        return undefined;
    }

    private moduleTagesschuleArrayToRestObject(data: Array<TSModulTagesschule>): any[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.modulTagesschuleToRestObject({}, item))
            : [];
    }

    private modulTagesschuleToRestObject(restModul: any, modulTagesschuleTS: TSModulTagesschule): any {
        if (modulTagesschuleTS) {
            this.abstractEntityToRestObject(restModul, modulTagesschuleTS);
            restModul.wochentag = modulTagesschuleTS.wochentag;
            return restModul;
        }
        return undefined;
    }

    private parseBelegungTagesschule(
        belegungTS: TSBelegungTagesschule,
        belegungFromServer: any
    ): TSBelegungTagesschule {
        if (belegungFromServer) {
            this.parseAbstractMutableEntity(belegungTS, belegungFromServer);
            belegungTS.belegungTagesschuleModule =
                this.parseBelegungTagesschuleModulList(belegungFromServer.belegungTagesschuleModule);
            belegungTS.eintrittsdatum = DateUtil.localDateToMoment(belegungFromServer.eintrittsdatum);
            belegungTS.abholungTagesschule = belegungFromServer.abholungTagesschule;
            belegungTS.fleischOption = belegungFromServer.fleischOption;
            belegungTS.notfallnummer = belegungFromServer.notfallnummer;
            belegungTS.allergienUndUnvertraeglichkeiten = belegungFromServer.allergienUndUnvertraeglichkeiten;
            belegungTS.planKlasse = belegungFromServer.planKlasse;
            belegungTS.abweichungZweitesSemester = belegungFromServer.abweichungZweitesSemester;
            belegungTS.keineKesbPlatzierung = belegungFromServer.keineKesbPlatzierung;
            belegungTS.bemerkung = belegungFromServer.bemerkung;
            return belegungTS;
        }
        return undefined;
    }

    private belegungTagesschuleToRestObject(restBelegung: any, belegungTS: TSBelegungTagesschule): any {
        if (belegungTS) {
            this.abstractMutableEntityToRestObject(restBelegung, belegungTS);
            restBelegung.belegungTagesschuleModule =
                this.belegungTagesschuleModulArrayToRestObject(belegungTS.belegungTagesschuleModule);
            restBelegung.eintrittsdatum = DateUtil.momentToLocalDate(belegungTS.eintrittsdatum);
            restBelegung.abholungTagesschule = belegungTS.abholungTagesschule;
            restBelegung.fleischOption = belegungTS.fleischOption;
            restBelegung.notfallnummer = belegungTS.notfallnummer;
            restBelegung.allergienUndUnvertraeglichkeiten = belegungTS.allergienUndUnvertraeglichkeiten;
            restBelegung.planKlasse = belegungTS.planKlasse;
            restBelegung.abweichungZweitesSemester = belegungTS.abweichungZweitesSemester;
            restBelegung.keineKesbPlatzierung = belegungTS.keineKesbPlatzierung;
            restBelegung.bemerkung = belegungTS.bemerkung;
            return restBelegung;
        }
        return undefined;
    }

    public parseBelegungTagesschuleModulList(data: any): TSBelegungTagesschuleModul[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseBelegungTagesschuleModul(new TSBelegungTagesschuleModul(), item))
            : [this.parseBelegungTagesschuleModul(new TSBelegungTagesschuleModul(), data)];
    }

    private parseBelegungTagesschuleModul(
        belegungModulTS: TSBelegungTagesschuleModul,
        belegungModulFromServer: any
    ): TSBelegungTagesschuleModul {
        if (belegungModulFromServer) {
            this.parseAbstractEntity(belegungModulTS, belegungModulFromServer);
            belegungModulTS.intervall = belegungModulFromServer.intervall;
            belegungModulTS.modulTagesschule =
                this.parseModulTagesschule(new TSModulTagesschule(), belegungModulFromServer.modulTagesschule);
            return belegungModulTS;
        }
        return undefined;
    }

    private belegungTagesschuleModulArrayToRestObject(data: Array<TSBelegungTagesschuleModul>): any[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.belegungTagesschuleModulToRestObject({}, item))
            : [];
    }

    private belegungTagesschuleModulToRestObject(
        restBelegungModul: any,
        belegungModulTS: TSBelegungTagesschuleModul
    ): any {
        if (belegungModulTS) {
            this.abstractEntityToRestObject(restBelegungModul, belegungModulTS);
            restBelegungModul.intervall = belegungModulTS.intervall;
            restBelegungModul.modulTagesschule =
                this.modulTagesschuleToRestObject({}, belegungModulTS.modulTagesschule);
            return restBelegungModul;
        }
        return undefined;
    }

    public parseFerieninselStammdatenList(data: any): TSFerieninselStammdaten[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseFerieninselStammdaten(new TSFerieninselStammdaten(), item))
                .sort((a, b) => ferienInselNameOrder(a.ferienname) - ferienInselNameOrder(b.ferienname))
            : [this.parseFerieninselStammdaten(new TSFerieninselStammdaten(), data)];
    }

    public parseFerieninselStammdaten(
        ferieninselStammdatenTS: TSFerieninselStammdaten,
        receivedFerieninselStammdaten: any
    ): TSFerieninselStammdaten {
        if (receivedFerieninselStammdaten) {
            this.parseAbstractMutableEntity(ferieninselStammdatenTS, receivedFerieninselStammdaten);
            ferieninselStammdatenTS.ferienname = receivedFerieninselStammdaten.ferienname;
            ferieninselStammdatenTS.anmeldeschluss =
                DateUtil.localDateToMoment(receivedFerieninselStammdaten.anmeldeschluss);
            ferieninselStammdatenTS.ferienActive = receivedFerieninselStammdaten.ferienActive;

            ferieninselStammdatenTS.zeitraumList = [];
            for (const zeitraum of receivedFerieninselStammdaten.zeitraumList) {
                const zeitraumTS = new TSFerieninselZeitraum();
                this.parseDateRangeEntity(zeitraumTS, zeitraum);
                ferieninselStammdatenTS.zeitraumList.push(zeitraumTS);
            }

            if (ferieninselStammdatenTS.zeitraumList.length < 1) {
                const emptyZeitraum = new TSFerieninselZeitraum();
                emptyZeitraum.gueltigkeit = new TSDateRange();
                ferieninselStammdatenTS.zeitraumList.push(emptyZeitraum);
            }

            const tage = receivedFerieninselStammdaten.potenzielleFerieninselTageFuerBelegung;
            if (tage) {
                ferieninselStammdatenTS.potenzielleFerieninselTageFuerBelegung =
                    this.parseBelegungFerieninselTagList(tage);
            }

            const tageMorgenmodul = receivedFerieninselStammdaten.potenzielleFerieninselTageFuerBelegungMorgenmodul;
            if (tageMorgenmodul) {
                ferieninselStammdatenTS.potenzielleFerieninselTageFuerBelegungMorgenmodul =
                    this.parseBelegungFerieninselTagList(tageMorgenmodul);
            }
            return ferieninselStammdatenTS;
        }
        return undefined;
    }

    public ferieninselStammdatenListToRestObject(ferieninselStammdatenList: TSFerieninselStammdaten[]): Array<any> {
        return ferieninselStammdatenList
            ? ferieninselStammdatenList.map(item => this.ferieninselStammdatenToRestObject({}, item))
            : [];
    }

    public ferieninselStammdatenToRestObject(
        restFerieninselStammdaten: any,
        ferieninselStammdatenTS: TSFerieninselStammdaten
    ): any {
        if (ferieninselStammdatenTS) {
            this.abstractMutableEntityToRestObject(restFerieninselStammdaten, ferieninselStammdatenTS);
            restFerieninselStammdaten.ferienname = ferieninselStammdatenTS.ferienname;
            restFerieninselStammdaten.anmeldeschluss =
                DateUtil.momentToLocalDate(ferieninselStammdatenTS.anmeldeschluss);

            if (ferieninselStammdatenTS.zeitraumList &&
                (ferieninselStammdatenTS.zeitraumList.length > 1 ||
                    (ferieninselStammdatenTS.zeitraumList.length === 1 &&
                        ferieninselStammdatenTS.zeitraumList[0].gueltigkeit &&
                        ferieninselStammdatenTS.zeitraumList[0].gueltigkeit.gueltigAb))) {
                restFerieninselStammdaten.zeitraumList = [];
                for (let i = 0; i < ferieninselStammdatenTS.zeitraumList.length; i++) {
                    const zeitraum: any = {};
                    this.abstractDateRangeEntityToRestObject(zeitraum, ferieninselStammdatenTS.zeitraumList[i]);
                    restFerieninselStammdaten.zeitraumList[i] = zeitraum;
                }
            }
            return restFerieninselStammdaten;
        }
        return undefined;
    }

    public parseBelegungFerieninsel(
        belegungFerieninselTS: TSBelegungFerieninsel,
        receivedBelegungFerieninsel: any
    ): TSBelegungFerieninsel {
        if (receivedBelegungFerieninsel) {
            this.parseAbstractMutableEntity(belegungFerieninselTS, receivedBelegungFerieninsel);
            belegungFerieninselTS.ferienname = receivedBelegungFerieninsel.ferienname;
            belegungFerieninselTS.notfallAngaben = receivedBelegungFerieninsel.notfallAngaben;
            belegungFerieninselTS.tage = this.parseBelegungFerieninselTagList(receivedBelegungFerieninsel.tage);
            belegungFerieninselTS.tageMorgenmodul = this.parseBelegungFerieninselTagList(
                receivedBelegungFerieninsel.tageMorgenmodul
            );
            return belegungFerieninselTS;
        }
        return undefined;
    }

    private parseBelegungFerieninselTagList(data: any): TSBelegungFerieninselTag[] {
        if (!data) {
            return [];
        }
        const tage = Array.isArray(data)
            ? data.map(item => this.parseBelegungFerieninselTag(new TSBelegungFerieninselTag(), item))
            : [this.parseBelegungFerieninselTag(new TSBelegungFerieninselTag(), data)];

        tage.sort((a: TSBelegungFerieninselTag, b: TSBelegungFerieninselTag) => a.tag.valueOf() - b.tag.valueOf());

        return tage;
    }

    private parseBelegungFerieninselTag(
        belegungFerieninselTagTS: TSBelegungFerieninselTag,
        receivedBelegungFerieninselTag: any
    ): TSBelegungFerieninselTag {
        if (receivedBelegungFerieninselTag) {
            this.parseAbstractMutableEntity(belegungFerieninselTagTS, receivedBelegungFerieninselTag);
            belegungFerieninselTagTS.tag = DateUtil.localDateToMoment(receivedBelegungFerieninselTag.tag);
            return belegungFerieninselTagTS;
        }
        return undefined;
    }

    public belegungFerieninselToRestObject(
        restBelegungFerieninsel: any,
        belegungFerieninselTS: TSBelegungFerieninsel
    ): any {
        if (belegungFerieninselTS) {
            this.abstractMutableEntityToRestObject(restBelegungFerieninsel, belegungFerieninselTS);
            restBelegungFerieninsel.ferienname = belegungFerieninselTS.ferienname;
            restBelegungFerieninsel.notfallAngaben = belegungFerieninselTS.notfallAngaben;
            restBelegungFerieninsel.tage = [];
            restBelegungFerieninsel.tageMorgenmodul = [];
            if (Array.isArray(belegungFerieninselTS.tage)) {
                belegungFerieninselTS.tage.forEach(t => {
                    const tagRest: any = {};
                    this.abstractMutableEntityToRestObject(tagRest, t);
                    tagRest.tag = DateUtil.momentToLocalDate(t.tag);
                    restBelegungFerieninsel.tage.push(tagRest);
                });
            }
            if (Array.isArray(belegungFerieninselTS.tageMorgenmodul)) {
                belegungFerieninselTS.tageMorgenmodul.forEach(t => {
                    const tagRest: any = {};
                    this.abstractMutableEntityToRestObject(tagRest, t);
                    tagRest.tag = DateUtil.momentToLocalDate(t.tag);
                    restBelegungFerieninsel.tageMorgenmodul.push(tagRest);
                });
            }
            return restBelegungFerieninsel;
        }
        return undefined;
    }

    public textRessourceToRestObject(restTextRessource: any, textRessource: TSTextRessource): TSTextRessource {
        if (textRessource) {
            this.abstractEntityToRestObject(restTextRessource, textRessource);
            restTextRessource.textDeutsch = textRessource.textDeutsch;
            restTextRessource.textFranzoesisch = textRessource.textFranzoesisch;
        }

        return restTextRessource;
    }

    public parseTextRessource(textRessourceTS: TSTextRessource, textRessourceFromServer: any): TSTextRessource {
        if (textRessourceFromServer) {
            this.parseAbstractMutableEntity(textRessourceTS, textRessourceFromServer);
            textRessourceTS.textDeutsch = textRessourceFromServer.textDeutsch;
            textRessourceTS.textFranzoesisch = textRessourceFromServer.textFranzoesisch;
        }

        return textRessourceTS;
    }

    public supportAnfrageToRestObject(supportRest: any, supportTS: TSSupportAnfrage): any {
        if (supportTS) {
            supportRest.id = supportTS.id;
            supportRest.beschreibung = supportTS.beschreibung;
            return supportRest;
        }
        return undefined;
    }

    public parsePublicAppConfig(data: any): TSPublicAppConfig {
        if (!data) {
            return undefined;
        }
        const publicAppConfigTS = new TSPublicAppConfig();
        publicAppConfigTS.currentNode = data.currentNode;
        publicAppConfigTS.devmode = data.devmode;
        publicAppConfigTS.whitelist = data.whitelist;
        publicAppConfigTS.dummyMode = data.dummyMode;
        publicAppConfigTS.sentryEnvName = data.sentryEnvName;
        publicAppConfigTS.backgroundColor = data.backgroundColor;
        publicAppConfigTS.primaryColor = data.primaryColor;
        publicAppConfigTS.primaryColorDark = data.primaryColorDark;
        publicAppConfigTS.primaryColorLight = data.primaryColorLight;
        publicAppConfigTS.logoFileName = data.logoFileName;
        publicAppConfigTS.logoFileNameWhite = data.logoFileNameWhite;
        publicAppConfigTS.zahlungentestmode = data.zahlungentestmode;
        publicAppConfigTS.personenSucheDisabled = data.personenSucheDisabled;
        publicAppConfigTS.kitaxHost = data.kitaxHost;
        publicAppConfigTS.kitaxEndpoint = data.kitaxEndpoint;
        publicAppConfigTS.notverordnungDefaultEinreichefristOeffentlich =
            data.notverordnungDefaultEinreichefristOeffentlich;
        publicAppConfigTS.notverordnungDefaultEinreichefristPrivat = data.notverordnungDefaultEinreichefristPrivat;
        publicAppConfigTS.lastenausgleichTagesschulenAktiv = data.lastenausgleichTagesschulenAktiv;
        publicAppConfigTS.gemeindeKennzahlenAktiv = data.gemeindeKennzahlenAktiv;
        publicAppConfigTS.ferienbetreuungAktiv = data.ferienbetreuungAktiv;
        publicAppConfigTS.lastenausgleichAktiv = data.lastenausgleichAktiv;
        publicAppConfigTS.mulitmandantAktiv = data.multimandantAktiviert;
        publicAppConfigTS.angebotTSActivated = data.angebotTSActivated;
        publicAppConfigTS.angebotFIActivated = data.angebotFIActivated;
        publicAppConfigTS.angebotTFOActivated = data.angebotTFOActivated;
        publicAppConfigTS.angebotMittagstischActivated = data.angebotMittagstischEnabled;
        publicAppConfigTS.infomaZahlungen = data.infomaZahlungen;
        publicAppConfigTS.frenchEnabled = data.frenchEnabled;
        publicAppConfigTS.geresEnabledForMandant = data.geresEnabledForMandant;
        publicAppConfigTS.ebeguKibonAnfrageTestGuiEnabled = data.ebeguKibonAnfrageTestGuiEnabled;
        publicAppConfigTS.steuerschnittstelleAktivAb = moment(data.steuerschnittstelleAktivAb);
        publicAppConfigTS.zusatzinformationenInstitution = data.zusatzinformationenInstitution;
        publicAppConfigTS.institutionenDurchGemeindenEinladen = data.institutionenDurchGemeindenEinladen;
        publicAppConfigTS.activatedDemoFeatures = data.activatedDemoFeatures;
        publicAppConfigTS.checkboxAuszahlungInZukunft = data.checkboxAuszahlungInZukunft;
        publicAppConfigTS.erlaubenInstitutionenZuWaehlen = data.erlaubenInstitutionenZuWaehlen;
        publicAppConfigTS.auszahlungAnEltern = data.auszahlungAnEltern;
        return publicAppConfigTS;
    }

    public parseGemeindeRegistrierungList(data: unknown): TSGemeindeRegistrierung[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseGemeindeRegistrierung(new TSGemeindeRegistrierung(), item))
            : [this.parseGemeindeRegistrierung(new TSGemeindeRegistrierung(), data)];
    }

    private parseGemeindeRegistrierung(
        gemeindeRegistrierungTS: TSGemeindeRegistrierung,
        gemeindeRegistrierung: any
    ): TSGemeindeRegistrierung {
        if (gemeindeRegistrierung) {
            gemeindeRegistrierungTS.id = gemeindeRegistrierung.id;
            gemeindeRegistrierungTS.name = gemeindeRegistrierung.name;
            gemeindeRegistrierungTS.verbundId = gemeindeRegistrierung.verbundId;
            gemeindeRegistrierungTS.verbundName = gemeindeRegistrierung.verbundName;
            return gemeindeRegistrierungTS;
        }
        return undefined;
    }

    public parseLastenausgleichList(data: any): TSLastenausgleich[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseLastenausgleich(new TSLastenausgleich(), item))
            : [];
    }

    public parseLastenausgleich(
        tsLastenausgleich: TSLastenausgleich,
        receivedLastenausgleich: any
    ): TSLastenausgleich {
        this.parseAbstractEntity(tsLastenausgleich, receivedLastenausgleich);
        tsLastenausgleich.jahr = receivedLastenausgleich.jahr;
        tsLastenausgleich.totalAlleGemeinden = receivedLastenausgleich.totalAlleGemeinden;
        return tsLastenausgleich;
    }

    public parseAnmeldungTagesschuleZeitabschnitts(data: Array<any>): TSAnmeldungTagesschuleZeitabschnitt[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(
                item => this.parseAnmeldungTagesschuleZeitabschnitt(new TSAnmeldungTagesschuleZeitabschnitt(), item))
            : [this.parseAnmeldungTagesschuleZeitabschnitt(new TSAnmeldungTagesschuleZeitabschnitt(), data)];
    }

    public parseAnmeldungTagesschuleZeitabschnitt(
        anmeldungTagesschuleZeitabschnittTS: TSAnmeldungTagesschuleZeitabschnitt,
        anmeldungTagesschuleZeitabschnittFromServer: any
    ): TSAnmeldungTagesschuleZeitabschnitt {
        if (anmeldungTagesschuleZeitabschnittFromServer) {
            this.parseDateRangeEntity(anmeldungTagesschuleZeitabschnittTS, anmeldungTagesschuleZeitabschnittFromServer);
            anmeldungTagesschuleZeitabschnittTS.betreuungsminutenProWoche =
                anmeldungTagesschuleZeitabschnittFromServer.betreuungsminutenProWoche;
            anmeldungTagesschuleZeitabschnittTS.betreuungsstundenProWoche =
                anmeldungTagesschuleZeitabschnittFromServer.betreuungsstundenProWoche;
            anmeldungTagesschuleZeitabschnittTS.gebuehrProStunde =
                anmeldungTagesschuleZeitabschnittFromServer.gebuehrProStunde;
            anmeldungTagesschuleZeitabschnittTS.massgebendesEinkommenInklAbzugFamgr =
                anmeldungTagesschuleZeitabschnittFromServer.massgebendesEinkommenInklAbzugFamgr;
            anmeldungTagesschuleZeitabschnittTS.pedagogischBetreut =
                anmeldungTagesschuleZeitabschnittFromServer.pedagogischBetreut;
            anmeldungTagesschuleZeitabschnittTS.totalKostenProWoche =
                anmeldungTagesschuleZeitabschnittFromServer.totalKostenProWoche;
            anmeldungTagesschuleZeitabschnittTS.verpflegungskosten =
                anmeldungTagesschuleZeitabschnittFromServer.verpflegungskosten;
            return anmeldungTagesschuleZeitabschnittTS;
        }
        return undefined;
    }

    public parseKitaxResponse(response: any): TSKitaxResponse {

        const kitaxResponse = new TSKitaxResponse();

        kitaxResponse.url = response.url;
        kitaxResponse.fallNummer = response.fallNr;

        return kitaxResponse;
    }

    public parseRueckforderungFormularList(data: any): TSRueckforderungFormular[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(
                item => this.parseRueckforderungFormular(new TSRueckforderungFormular(), item))
            : [this.parseRueckforderungFormular(new TSRueckforderungFormular(), data)];
    }

    public parseRueckforderungFormular(
        rueckforderungFormular: TSRueckforderungFormular,
        rueckforderungFormularFromServer: any
    ): TSRueckforderungFormular {

        this.parseAbstractEntity(rueckforderungFormular, rueckforderungFormularFromServer);

        rueckforderungFormular.institutionStammdaten = this.parseInstitutionStammdaten(new TSInstitutionStammdaten(),
            rueckforderungFormularFromServer.institutionStammdaten);
        rueckforderungFormular.rueckforderungMitteilungen =
            this.parseRueckforderungMitteilungList(rueckforderungFormularFromServer.rueckforderungMitteilungen);
        rueckforderungFormular.verantwortlicherName = rueckforderungFormularFromServer.verantwortlicherName;
        rueckforderungFormular.uncheckedDocuments = rueckforderungFormularFromServer.uncheckedDocuments;
        rueckforderungFormular.status = rueckforderungFormularFromServer.status;
        rueckforderungFormular.hasBeenProvisorisch = rueckforderungFormularFromServer.hasBeenProvisorisch;
        rueckforderungFormular.stufe1KantonKostenuebernahmeAnzahlStunden =
            rueckforderungFormularFromServer.stufe1KantonKostenuebernahmeAnzahlStunden;
        rueckforderungFormular.stufe1InstitutionKostenuebernahmeAnzahlStunden =
            rueckforderungFormularFromServer.stufe1InstitutionKostenuebernahmeAnzahlStunden;
        rueckforderungFormular.stufe2KantonKostenuebernahmeAnzahlStunden =
            rueckforderungFormularFromServer.stufe2KantonKostenuebernahmeAnzahlStunden;
        rueckforderungFormular.stufe2InstitutionKostenuebernahmeAnzahlStunden =
            rueckforderungFormularFromServer.stufe2InstitutionKostenuebernahmeAnzahlStunden;
        rueckforderungFormular.stufe1KantonKostenuebernahmeAnzahlTage =
            rueckforderungFormularFromServer.stufe1KantonKostenuebernahmeAnzahlTage;
        rueckforderungFormular.stufe1InstitutionKostenuebernahmeAnzahlTage =
            rueckforderungFormularFromServer.stufe1InstitutionKostenuebernahmeAnzahlTage;
        rueckforderungFormular.stufe2KantonKostenuebernahmeAnzahlTage =
            rueckforderungFormularFromServer.stufe2KantonKostenuebernahmeAnzahlTage;
        rueckforderungFormular.stufe2InstitutionKostenuebernahmeAnzahlTage =
            rueckforderungFormularFromServer.stufe2InstitutionKostenuebernahmeAnzahlTage;
        rueckforderungFormular.stufe1KantonKostenuebernahmeBetreuung =
            rueckforderungFormularFromServer.stufe1KantonKostenuebernahmeBetreuung;
        rueckforderungFormular.stufe1InstitutionKostenuebernahmeBetreuung =
            rueckforderungFormularFromServer.stufe1InstitutionKostenuebernahmeBetreuung;
        rueckforderungFormular.stufe2KantonKostenuebernahmeBetreuung =
            rueckforderungFormularFromServer.stufe2KantonKostenuebernahmeBetreuung;
        rueckforderungFormular.stufe2InstitutionKostenuebernahmeBetreuung =
            rueckforderungFormularFromServer.stufe2InstitutionKostenuebernahmeBetreuung;
        rueckforderungFormular.stufe1FreigabeBetrag = rueckforderungFormularFromServer.stufe1FreigabeBetrag;
        rueckforderungFormular.stufe1FreigabeDatum =
            DateUtil.localDateTimeToMoment(rueckforderungFormularFromServer.stufe1FreigabeDatum);
        rueckforderungFormular.stufe1FreigabeAusbezahltAm =
            DateUtil.localDateTimeToMoment(rueckforderungFormularFromServer.stufe1FreigabeAusbezahltAm);
        rueckforderungFormular.stufe2VerfuegungBetrag = rueckforderungFormularFromServer.stufe2VerfuegungBetrag;
        rueckforderungFormular.stufe2VerfuegungDatum =
            DateUtil.localDateTimeToMoment(rueckforderungFormularFromServer.stufe2VerfuegungDatum);
        rueckforderungFormular.stufe2VerfuegungAusbezahltAm =
            DateUtil.localDateTimeToMoment(rueckforderungFormularFromServer.stufe2VerfuegungAusbezahltAm);
        rueckforderungFormular.institutionTyp = rueckforderungFormularFromServer.institutionTyp;
        rueckforderungFormular.extendedEinreichefrist =
            DateUtil.localDateToMoment(rueckforderungFormularFromServer.extendedEinreichefrist);
        rueckforderungFormular.relevantEinreichungsfrist =
            DateUtil.localDateToMoment(rueckforderungFormularFromServer.relevantEinreichungsfrist);
        rueckforderungFormular.betragEntgangeneElternbeitraege =
            rueckforderungFormularFromServer.betragEntgangeneElternbeitraege;
        rueckforderungFormular.betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten =
            rueckforderungFormularFromServer.betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten;
        rueckforderungFormular.anzahlNichtAngeboteneEinheiten =
            rueckforderungFormularFromServer.anzahlNichtAngeboteneEinheiten;
        rueckforderungFormular.kurzarbeitBeantragt = rueckforderungFormularFromServer.kurzarbeitBeantragt;
        rueckforderungFormular.kurzarbeitBetrag = rueckforderungFormularFromServer.kurzarbeitBetrag;
        rueckforderungFormular.kurzarbeitDefinitivVerfuegt =
            rueckforderungFormularFromServer.kurzarbeitDefinitivVerfuegt;
        rueckforderungFormular.kurzarbeitKeinAntragBegruendung =
            rueckforderungFormularFromServer.kurzarbeitKeinAntragBegruendung;
        rueckforderungFormular.kurzarbeitSonstiges = rueckforderungFormularFromServer.kurzarbeitSonstiges;
        rueckforderungFormular.coronaErwerbsersatzBeantragt =
            rueckforderungFormularFromServer.coronaErwerbsersatzBeantragt;
        rueckforderungFormular.coronaErwerbsersatzBetrag = rueckforderungFormularFromServer.coronaErwerbsersatzBetrag;
        rueckforderungFormular.coronaErwerbsersatzDefinitivVerfuegt =
            rueckforderungFormularFromServer.coronaErwerbsersatzDefinitivVerfuegt;
        rueckforderungFormular.coronaErwerbsersatzKeinAntragBegruendung =
            rueckforderungFormularFromServer.coronaErwerbsersatzKeinAntragBegruendung;
        rueckforderungFormular.coronaErwerbsersatzSonstiges =
            rueckforderungFormularFromServer.coronaErwerbsersatzSonstiges;
        rueckforderungFormular.korrespondenzSprache = rueckforderungFormularFromServer.korrespondenzSprache;
        rueckforderungFormular.bemerkungFuerVerfuegung = rueckforderungFormularFromServer.bemerkungFuerVerfuegung;
        rueckforderungFormular.beschwerdeBetrag = rueckforderungFormularFromServer.beschwerdeBetrag;
        rueckforderungFormular.beschwerdeBemerkung = rueckforderungFormularFromServer.beschwerdeBemerkung;
        rueckforderungFormular.beschwerdeAusbezahltAm =
            DateUtil.localDateTimeToMoment(rueckforderungFormularFromServer.beschwerdeAusbezahltAm);
        return rueckforderungFormular;
    }

    public parseRueckforderungMitteilungList(data: Array<any>): TSRueckforderungMitteilung[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(
                item => this.parseRueckforderungMitteilung(new TSRueckforderungMitteilung(), item))
            : [this.parseRueckforderungMitteilung(new TSRueckforderungMitteilung(), data)];
    }

    public parseRueckforderungMitteilung(
        rueckforderungMitteilung: TSRueckforderungMitteilung,
        rueckforderungMitteilungFromServer: any
    ): TSRueckforderungMitteilung {
        this.parseAbstractEntity(rueckforderungMitteilung, rueckforderungMitteilungFromServer);
        rueckforderungMitteilung.betreff = rueckforderungMitteilungFromServer.betreff;
        rueckforderungMitteilung.inhalt = rueckforderungMitteilungFromServer.inhalt;
        rueckforderungMitteilung.sendeDatum =
            DateUtil.localDateTimeToMoment(rueckforderungMitteilungFromServer.sendeDatum);
        return rueckforderungMitteilung;
    }

    public rueckforderungFormularToRestObject(
        rueckforderungFormularRest: any,
        rueckforderungFormularTS: TSRueckforderungFormular
    ): any {
        this.abstractEntityToRestObject(rueckforderungFormularRest, rueckforderungFormularTS);
        rueckforderungFormularRest.institutionStammdaten =
            this.institutionStammdatenToRestObject({}, rueckforderungFormularTS.institutionStammdaten);
        rueckforderungFormularRest.rueckforderungMitteilungen =
            this.rueckforderungMitteilungListToRestObject(rueckforderungFormularTS.rueckforderungMitteilungen);
        rueckforderungFormularRest.status = rueckforderungFormularTS.status;
        rueckforderungFormularRest.hasBeenProvisorisch = rueckforderungFormularTS.hasBeenProvisorisch;
        rueckforderungFormularRest.stufe1KantonKostenuebernahmeAnzahlStunden =
            rueckforderungFormularTS.stufe1KantonKostenuebernahmeAnzahlStunden;
        rueckforderungFormularRest.stufe1InstitutionKostenuebernahmeAnzahlStunden =
            rueckforderungFormularTS.stufe1InstitutionKostenuebernahmeAnzahlStunden;
        rueckforderungFormularRest.stufe2KantonKostenuebernahmeAnzahlStunden =
            rueckforderungFormularTS.stufe2KantonKostenuebernahmeAnzahlStunden;
        rueckforderungFormularRest.stufe2InstitutionKostenuebernahmeAnzahlStunden =
            rueckforderungFormularTS.stufe2InstitutionKostenuebernahmeAnzahlStunden;
        rueckforderungFormularRest.stufe1KantonKostenuebernahmeAnzahlTage =
            rueckforderungFormularTS.stufe1KantonKostenuebernahmeAnzahlTage;
        rueckforderungFormularRest.stufe1InstitutionKostenuebernahmeAnzahlTage =
            rueckforderungFormularTS.stufe1InstitutionKostenuebernahmeAnzahlTage;
        rueckforderungFormularRest.stufe2KantonKostenuebernahmeAnzahlTage =
            rueckforderungFormularTS.stufe2KantonKostenuebernahmeAnzahlTage;
        rueckforderungFormularRest.stufe2InstitutionKostenuebernahmeAnzahlTage =
            rueckforderungFormularTS.stufe2InstitutionKostenuebernahmeAnzahlTage;
        rueckforderungFormularRest.stufe1KantonKostenuebernahmeBetreuung =
            rueckforderungFormularTS.stufe1KantonKostenuebernahmeBetreuung;
        rueckforderungFormularRest.stufe1InstitutionKostenuebernahmeBetreuung =
            rueckforderungFormularTS.stufe1InstitutionKostenuebernahmeBetreuung;
        rueckforderungFormularRest.stufe2KantonKostenuebernahmeBetreuung =
            rueckforderungFormularTS.stufe2KantonKostenuebernahmeBetreuung;
        rueckforderungFormularRest.stufe2InstitutionKostenuebernahmeBetreuung =
            rueckforderungFormularTS.stufe2InstitutionKostenuebernahmeBetreuung;
        rueckforderungFormularRest.stufe1FreigabeBetrag = rueckforderungFormularTS.stufe1FreigabeBetrag;
        rueckforderungFormularRest.stufe1FreigabeDatum =
            DateUtil.momentToLocalDateTime(rueckforderungFormularTS.stufe1FreigabeDatum);
        // stufe1FreigabeAusbezahltAm darf nie vom Client uebernommen werden, es muss Clientseitig gesetzt werden
        rueckforderungFormularRest.stufe2VerfuegungBetrag = rueckforderungFormularTS.stufe2VerfuegungBetrag;
        rueckforderungFormularRest.stufe2VerfuegungDatum =
            DateUtil.momentToLocalDateTime(rueckforderungFormularTS.stufe2VerfuegungDatum);
        // stufe2VerfuegungAusbezahltAm darf nie vom Client uebernommen werden, es muss Clientseitig gesetzt werden
        rueckforderungFormularRest.institutionTyp = rueckforderungFormularTS.institutionTyp;
        rueckforderungFormularRest.extendedEinreichefrist =
            DateUtil.momentToLocalDate(rueckforderungFormularTS.extendedEinreichefrist);
        rueckforderungFormularRest.relevantEinreichungsfrist =
            DateUtil.momentToLocalDate(rueckforderungFormularTS.relevantEinreichungsfrist);
        rueckforderungFormularRest.betragEntgangeneElternbeitraege =
            rueckforderungFormularTS.betragEntgangeneElternbeitraege;
        rueckforderungFormularRest.betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten =
            rueckforderungFormularTS.betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten;
        rueckforderungFormularRest.anzahlNichtAngeboteneEinheiten =
            rueckforderungFormularTS.anzahlNichtAngeboteneEinheiten;
        rueckforderungFormularRest.kurzarbeitBeantragt = rueckforderungFormularTS.kurzarbeitBeantragt;
        rueckforderungFormularRest.kurzarbeitBetrag = rueckforderungFormularTS.kurzarbeitBetrag;
        rueckforderungFormularRest.kurzarbeitDefinitivVerfuegt = rueckforderungFormularTS.kurzarbeitDefinitivVerfuegt;
        rueckforderungFormularRest.kurzarbeitKeinAntragBegruendung =
            rueckforderungFormularTS.kurzarbeitKeinAntragBegruendung;
        rueckforderungFormularRest.kurzarbeitSonstiges = rueckforderungFormularTS.kurzarbeitSonstiges;
        rueckforderungFormularRest.coronaErwerbsersatzBeantragt =
            rueckforderungFormularTS.coronaErwerbsersatzBeantragt;
        rueckforderungFormularRest.coronaErwerbsersatzBetrag = rueckforderungFormularTS.coronaErwerbsersatzBetrag;
        rueckforderungFormularRest.coronaErwerbsersatzDefinitivVerfuegt =
            rueckforderungFormularTS.coronaErwerbsersatzDefinitivVerfuegt;
        rueckforderungFormularRest.coronaErwerbsersatzKeinAntragBegruendung =
            rueckforderungFormularTS.coronaErwerbsersatzKeinAntragBegruendung;
        rueckforderungFormularRest.coronaErwerbsersatzSonstiges =
            rueckforderungFormularTS.coronaErwerbsersatzSonstiges;
        rueckforderungFormularRest.korrespondenzSprache = rueckforderungFormularTS.korrespondenzSprache;
        rueckforderungFormularRest.bemerkungFuerVerfuegung = rueckforderungFormularTS.bemerkungFuerVerfuegung;
        rueckforderungFormularRest.beschwerdeBetrag = rueckforderungFormularTS.beschwerdeBetrag;
        rueckforderungFormularRest.beschwerdeBemerkung = rueckforderungFormularTS.beschwerdeBemerkung;
        return rueckforderungFormularRest;
    }

    public rueckforderungMitteilungListToRestObject(rueckforderungMitteilungenList: TSRueckforderungMitteilung[]): Array<any> {

        return rueckforderungMitteilungenList ? rueckforderungMitteilungenList.map(
            item => this.rueckforderungMitteilungToRestObject({}, item)) : [];
    }

    public rueckforderungMitteilungToRestObject(
        rueckforderungMitteilungRest: any,
        rueckforderungMitteilungTS: TSRueckforderungMitteilung
    ): any {
        this.abstractEntityToRestObject(rueckforderungMitteilungRest, rueckforderungMitteilungTS);
        rueckforderungMitteilungRest.betreff = rueckforderungMitteilungTS.betreff;
        rueckforderungMitteilungRest.inhalt = rueckforderungMitteilungTS.inhalt;
        rueckforderungMitteilungRest.sendeDatum = DateUtil.momentToLocalDateTime(rueckforderungMitteilungTS.sendeDatum);
        return rueckforderungMitteilungRest;
    }

    public parseRueckforderungDokumente(data: any): TSRueckforderungDokument[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseRueckforderungDokument(new TSRueckforderungDokument(), item))
            : [this.parseRueckforderungDokument(new TSRueckforderungDokument(), data)];
    }

    public parseRueckforderungDokument(
        dokument: TSRueckforderungDokument,
        dokumentFromServer: any
    ): TSRueckforderungDokument {
        if (dokumentFromServer) {
            this.parseTSFileDokument(dokument, dokumentFromServer);
            dokument.timestampUpload = DateUtil.localDateTimeToMoment(dokumentFromServer.timestampUpload);
            dokument.rueckforderungDokumentTyp = dokumentFromServer.rueckforderungDokumentTyp;
            return dokument;
        }
        return undefined;
    }

    public parseInstitutionExternalClientAssignment(data: any): TSInstitutionExternalClientAssignment {
        const tsInstitutionExternalClients = new TSInstitutionExternalClientAssignment();

        tsInstitutionExternalClients.availableClients = data.availableClients
            .map((client: any) => this.parseExternalClient(client));

        tsInstitutionExternalClients.assignedClients = data.assignedClients
            .map((client: any) => this.parseInstitutionExternalClient(client));

        return tsInstitutionExternalClients;
    }

    public parseInstitutionExternalClient(data: any): TSInstitutionExternalClient {
        const tsInstitutionExternalClient =
            new TSInstitutionExternalClient(this.parseExternalClient(data.externalClient));
        const ab = DateUtil.localDateToMoment(data.gueltigAb);
        const bis = DateUtil.localDateToMoment(data.gueltigBis);
        tsInstitutionExternalClient.gueltigkeit = new TSDateRange(ab, bis);
        return tsInstitutionExternalClient;
    }

    public institutionExternalClientListToRestObject(institutionExternalClientList: TSInstitutionExternalClient[]): Array<any> {
        return institutionExternalClientList ? institutionExternalClientList.map(
            item => this.institutionExternalClientToRestObject({}, item)) : undefined;
    }

    public institutionExternalClientToRestObject(
        institutionExternalClientRest: any,
        institutionExternalClientTS: TSInstitutionExternalClient
    ): any {
        institutionExternalClientRest.externalClient =
            this.externalClientToRestObject({}, institutionExternalClientTS.externalClient);
        if (institutionExternalClientTS.gueltigkeit) {
            institutionExternalClientRest.gueltigAb =
                DateUtil.momentToLocalDate(institutionExternalClientTS.gueltigkeit.gueltigAb);
            institutionExternalClientRest.gueltigBis =
                DateUtil.momentToLocalDate(institutionExternalClientTS.gueltigkeit.gueltigBis);
        }
        return institutionExternalClientRest;
    }

    public externalClientToRestObject(
        externalClientRest: any,
        externalClientTS: TSExternalClient
    ): any {
        this.abstractEntityToRestObject(externalClientRest, externalClientTS);
        externalClientRest.clientName = externalClientTS.clientName;
        externalClientRest.type = externalClientTS.type;
        return externalClientRest;
    }

    public parseGemeindeAntragList(data: Array<any>): TSGemeindeAntrag[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseGemeindeAntrag(new TSGemeindeAntrag(), item))
            : [this.parseGemeindeAntrag(new TSGemeindeAntrag(), data)];
    }

    public parseGemeindeAntrag(gemeindeAntragTS: TSGemeindeAntrag, gemeindeAntragFromServer: any): TSGemeindeAntrag {
        if (gemeindeAntragFromServer) {
            this.parseAbstractEntity(gemeindeAntragTS, gemeindeAntragFromServer);
            gemeindeAntragTS.gemeindeAntragTyp = gemeindeAntragFromServer.gemeindeAntragTyp;
            gemeindeAntragTS.gesuchsperiode =
                this.parseGesuchsperiode(new TSGesuchsperiode(), gemeindeAntragFromServer.gesuchsperiode);
            gemeindeAntragTS.gemeinde =
                this.parseGemeinde(new TSGemeinde(), gemeindeAntragFromServer.gemeinde);
            gemeindeAntragTS.statusString = gemeindeAntragFromServer.statusString;
            gemeindeAntragTS.antragAbgeschlossen = gemeindeAntragFromServer.antragAbgeschlossen;
            if (EbeguUtil.isNotNullOrUndefined(gemeindeAntragFromServer.verantwortlicher)) {
                gemeindeAntragTS.verantworlicher =
                    this.parseUserNoDetails(new TSBenutzerNoDetails(), gemeindeAntragFromServer.verantwortlicher);
            }
            return gemeindeAntragTS;
        }
        return undefined;
    }

    public parseLastenausgleichTagesschuleAngabenGemeindeContainer(
        gemeindeContainerTS: TSLastenausgleichTagesschuleAngabenGemeindeContainer,
        gemeindeContainerFromServer: any
    ): TSLastenausgleichTagesschuleAngabenGemeindeContainer {
        if (gemeindeContainerFromServer) {
            this.parseAbstractEntity(gemeindeContainerTS, gemeindeContainerFromServer);
            gemeindeContainerTS.status = gemeindeContainerFromServer.status;
            gemeindeContainerTS.gesuchsperiode =
                this.parseGesuchsperiode(new TSGesuchsperiode(), gemeindeContainerFromServer.gesuchsperiode);
            gemeindeContainerTS.gemeinde =
                this.parseGemeinde(new TSGemeinde(), gemeindeContainerFromServer.gemeinde);
            gemeindeContainerTS.alleAngabenInKibonErfasst = gemeindeContainerFromServer.alleAngabenInKibonErfasst;
            gemeindeContainerTS.internerKommentar = gemeindeContainerFromServer.internerKommentar;
            gemeindeContainerTS.angabenDeklaration =
                this.parseLastenausgleichTagesschuleAngabenGemeinde(
                    new TSLastenausgleichTagesschuleAngabenGemeinde(), gemeindeContainerFromServer.angabenDeklaration);
            gemeindeContainerTS.angabenKorrektur =
                this.parseLastenausgleichTagesschuleAngabenGemeinde(
                    new TSLastenausgleichTagesschuleAngabenGemeinde(), gemeindeContainerFromServer.angabenKorrektur);
            gemeindeContainerTS.angabenInstitutionContainers =
                this.parseLastenausgleichTagesschuleAngabenInstitutionContainerList(
                    gemeindeContainerFromServer.angabenInstitutionContainers
                );
            gemeindeContainerTS.verantwortlicher =
                this.parseUserNoDetails(new TSBenutzerNoDetails(), gemeindeContainerFromServer.verantwortlicher);
            gemeindeContainerTS.betreuungsstundenPrognose = gemeindeContainerFromServer.betreuungsstundenPrognose;
            gemeindeContainerTS.bemerkungenBetreuungsstundenPrognose =
                gemeindeContainerFromServer.bemerkungenBetreuungsstundenPrognose;
            return gemeindeContainerTS;
        }
        return undefined;
    }

    public lastenausgleichTagesschuleAngabenGemeindeContainerToRestObject(
        restGemeindeContainer: any,
        tsGemeindeContainer: TSLastenausgleichTagesschuleAngabenGemeindeContainer
    ): TSLastenausgleichTagesschuleAngabenGemeindeContainer {
        if (tsGemeindeContainer) {
            this.abstractEntityToRestObject(restGemeindeContainer, tsGemeindeContainer);
            restGemeindeContainer.status = tsGemeindeContainer.status;
            restGemeindeContainer.gesuchsperiode =
                this.gesuchsperiodeToRestObject({}, tsGemeindeContainer.gesuchsperiode);
            restGemeindeContainer.gemeinde =
                this.gemeindeToRestObject({}, tsGemeindeContainer.gemeinde);
            restGemeindeContainer.alleAngabenInKibonErfasst = tsGemeindeContainer.alleAngabenInKibonErfasst;
            restGemeindeContainer.internerKommentar = tsGemeindeContainer.internerKommentar;
            restGemeindeContainer.angabenDeklaration =
                this.lastenausgleichTagesschuleAngabenGemeindeToRestObject(
                    {}, tsGemeindeContainer.angabenDeklaration);
            restGemeindeContainer.angabenKorrektur =
                this.lastenausgleichTagesschuleAngabenGemeindeToRestObject(
                    {}, tsGemeindeContainer.angabenKorrektur);
            restGemeindeContainer.angabenInstitutionContainers =
                this.lastenausgleichTagesschuleAngabenInstitutionContainerListToRestObject(
                    tsGemeindeContainer.angabenInstitutionContainers);
            restGemeindeContainer.verantwortlicher =
                this.benutzerNoDetailsToRestObject({}, restGemeindeContainer.verantwortlicher);
            return restGemeindeContainer;
        }
        return undefined;
    }

    public parseLastenausgleichTagesschuleAngabenGemeinde(
        gemeindeTS: TSLastenausgleichTagesschuleAngabenGemeinde,
        gemeindeFromServer: any
    ): TSLastenausgleichTagesschuleAngabenGemeinde {
        if (gemeindeFromServer) {
            this.parseAbstractEntity(gemeindeTS, gemeindeFromServer);

            gemeindeTS.status = gemeindeFromServer.status;
            // A: Allgemeine Angaben
            gemeindeTS.bedarfBeiElternAbgeklaert =
                gemeindeFromServer.bedarfBeiElternAbgeklaert;
            gemeindeTS.angebotFuerFerienbetreuungVorhanden =
                gemeindeFromServer.angebotFuerFerienbetreuungVorhanden;
            gemeindeTS.angebotVerfuegbarFuerAlleSchulstufen =
                gemeindeFromServer.angebotVerfuegbarFuerAlleSchulstufen;
            gemeindeTS.begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen =
                gemeindeFromServer.begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen;
            // B: Abrechnung
            gemeindeTS.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse =
                gemeindeFromServer.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse;
            gemeindeTS.geleisteteBetreuungsstundenBesondereBeduerfnisse =
                gemeindeFromServer.geleisteteBetreuungsstundenBesondereBeduerfnisse;
            gemeindeTS.geleisteteBetreuungsstundenBesondereVolksschulangebot =
                gemeindeFromServer.geleisteteBetreuungsstundenBesondereVolksschulangebot;
            gemeindeTS.davonStundenZuNormlohnMehrAls50ProzentAusgebildete =
                gemeindeFromServer.davonStundenZuNormlohnMehrAls50ProzentAusgebildete;
            gemeindeTS.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete =
                gemeindeFromServer.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete;
            gemeindeTS.einnahmenElterngebuehren =
                gemeindeFromServer.einnahmenElterngebuehren;
            gemeindeTS.einnahmenElterngebuehrenVolksschulangebot =
                gemeindeFromServer.einnahmenElterngebuehrenVolksschulangebot;
            gemeindeTS.ersteRateAusbezahlt =
                gemeindeFromServer.ersteRateAusbezahlt;
            gemeindeTS.tagesschuleTeilweiseGeschlossen =
                gemeindeFromServer.tagesschuleTeilweiseGeschlossen;
            gemeindeTS.rueckerstattungenElterngebuehrenSchliessung =
                gemeindeFromServer.rueckerstattungenElterngebuehrenSchliessung;
            // C: Kostenbeteiligung Gemeinde
            gemeindeTS.gesamtKostenTagesschule =
                gemeindeFromServer.gesamtKostenTagesschule;
            gemeindeTS.einnnahmenVerpflegung =
                gemeindeFromServer.einnnahmenVerpflegung;
            gemeindeTS.einnahmenSubventionenDritter =
                gemeindeFromServer.einnahmenSubventionenDritter;
            gemeindeTS.ueberschussErzielt =
                gemeindeFromServer.ueberschussErzielt;
            gemeindeTS.ueberschussVerwendung =
                gemeindeFromServer.ueberschussVerwendung;
            // D: Angaben zu weiteren Kosten und Ertraegen
            gemeindeTS.bemerkungenWeitereKostenUndErtraege =
                gemeindeFromServer.bemerkungenWeitereKostenUndErtraege;
            // E: Kontrollfragen
            gemeindeTS.betreuungsstundenDokumentiertUndUeberprueft =
                gemeindeFromServer.betreuungsstundenDokumentiertUndUeberprueft;
            gemeindeTS.betreuungsstundenDokumentiertUndUeberprueftBemerkung =
                gemeindeFromServer.betreuungsstundenDokumentiertUndUeberprueftBemerkung;
            gemeindeTS.elterngebuehrenGemaessVerordnungBerechnet =
                gemeindeFromServer.elterngebuehrenGemaessVerordnungBerechnet;
            gemeindeTS.elterngebuehrenGemaessVerordnungBerechnetBemerkung =
                gemeindeFromServer.elterngebuehrenGemaessVerordnungBerechnetBemerkung;
            gemeindeTS.einkommenElternBelegt =
                gemeindeFromServer.einkommenElternBelegt;
            gemeindeTS.einkommenElternBelegtBemerkung =
                gemeindeFromServer.einkommenElternBelegtBemerkung;
            gemeindeTS.maximalTarif =
                gemeindeFromServer.maximalTarif;
            gemeindeTS.maximalTarifBemerkung =
                gemeindeFromServer.maximalTarifBemerkung;
            gemeindeTS.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal =
                gemeindeFromServer.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal;
            gemeindeTS.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung =
                gemeindeFromServer.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung;
            gemeindeTS.ausbildungenMitarbeitendeBelegt =
                gemeindeFromServer.ausbildungenMitarbeitendeBelegt;
            gemeindeTS.ausbildungenMitarbeitendeBelegtBemerkung =
                gemeindeFromServer.ausbildungenMitarbeitendeBelegtBemerkung;
            // Bemerkungen
            gemeindeTS.bemerkungen =
                gemeindeFromServer.bemerkungen;
            gemeindeTS.bemerkungStarkeVeraenderung =
                gemeindeFromServer.bemerkungStarkeVeraenderung;
            // Berechnungen
            gemeindeTS.lastenausgleichberechtigteBetreuungsstunden =
                gemeindeFromServer.lastenausgleichberechtigteBetreuungsstunden;
            gemeindeTS.davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet =
                gemeindeFromServer.davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet;
            gemeindeTS.davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet =
                gemeindeFromServer.davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet;
            gemeindeTS.normlohnkostenBetreuungBerechnet =
                gemeindeFromServer.normlohnkostenBetreuungBerechnet;
            gemeindeTS.lastenausgleichsberechtigerBetrag =
                gemeindeFromServer.lastenausgleichsberechtigerBetrag;
            gemeindeTS.kostenbeitragGemeinde =
                gemeindeFromServer.kostenbeitragGemeinde;
            gemeindeTS.kostenueberschussGemeinde =
                gemeindeFromServer.kostenueberschussGemeinde;
            gemeindeTS.erwarteterKostenbeitragGemeinde =
                gemeindeFromServer.erwarteterKostenbeitragGemeinde;
            gemeindeTS.schlusszahlung =
                gemeindeFromServer.schlusszahlung;
            return gemeindeTS;
        }
        return undefined;
    }

    public lastenausgleichTagesschuleAngabenGemeindeToRestObject(
        restAngabenGemeinde: any,
        tsAngabenGemeinde: TSLastenausgleichTagesschuleAngabenGemeinde
    ): any {
        if (tsAngabenGemeinde) {
            this.abstractEntityToRestObject(restAngabenGemeinde, tsAngabenGemeinde);

            restAngabenGemeinde.status = tsAngabenGemeinde.status;
            // A: Allgemeine Angaben
            restAngabenGemeinde.bedarfBeiElternAbgeklaert =
                tsAngabenGemeinde.bedarfBeiElternAbgeklaert;
            restAngabenGemeinde.angebotFuerFerienbetreuungVorhanden =
                tsAngabenGemeinde.angebotFuerFerienbetreuungVorhanden;
            restAngabenGemeinde.angebotVerfuegbarFuerAlleSchulstufen =
                tsAngabenGemeinde.angebotVerfuegbarFuerAlleSchulstufen;
            restAngabenGemeinde.begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen =
                tsAngabenGemeinde.begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen;
            // B: Abrechnung
            restAngabenGemeinde.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse =
                tsAngabenGemeinde.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse;
            restAngabenGemeinde.geleisteteBetreuungsstundenBesondereBeduerfnisse =
                tsAngabenGemeinde.geleisteteBetreuungsstundenBesondereBeduerfnisse;
            restAngabenGemeinde.geleisteteBetreuungsstundenBesondereVolksschulangebot =
                tsAngabenGemeinde.geleisteteBetreuungsstundenBesondereVolksschulangebot;
            restAngabenGemeinde.davonStundenZuNormlohnMehrAls50ProzentAusgebildete =
                tsAngabenGemeinde.davonStundenZuNormlohnMehrAls50ProzentAusgebildete;
            restAngabenGemeinde.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete =
                tsAngabenGemeinde.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete;
            restAngabenGemeinde.einnahmenElterngebuehren =
                tsAngabenGemeinde.einnahmenElterngebuehren;
            restAngabenGemeinde.einnahmenElterngebuehrenVolksschulangebot =
                tsAngabenGemeinde.einnahmenElterngebuehrenVolksschulangebot;
            restAngabenGemeinde.tagesschuleTeilweiseGeschlossen =
                tsAngabenGemeinde.tagesschuleTeilweiseGeschlossen;
            restAngabenGemeinde.rueckerstattungenElterngebuehrenSchliessung =
                tsAngabenGemeinde.rueckerstattungenElterngebuehrenSchliessung;
            restAngabenGemeinde.ersteRateAusbezahlt =
                tsAngabenGemeinde.ersteRateAusbezahlt;
            // C: Kostenbeteiligung Gemeinde
            restAngabenGemeinde.gesamtKostenTagesschule =
                tsAngabenGemeinde.gesamtKostenTagesschule;
            restAngabenGemeinde.einnnahmenVerpflegung =
                tsAngabenGemeinde.einnnahmenVerpflegung;
            restAngabenGemeinde.einnahmenSubventionenDritter =
                tsAngabenGemeinde.einnahmenSubventionenDritter;
            restAngabenGemeinde.ueberschussErzielt =
                tsAngabenGemeinde.ueberschussErzielt;
            restAngabenGemeinde.ueberschussVerwendung =
                tsAngabenGemeinde.ueberschussVerwendung;
            // D: Angaben zu weiteren Kosten und Ertraegen
            restAngabenGemeinde.bemerkungenWeitereKostenUndErtraege =
                tsAngabenGemeinde.bemerkungenWeitereKostenUndErtraege;
            // E: Kontrollfragen
            restAngabenGemeinde.betreuungsstundenDokumentiertUndUeberprueft =
                tsAngabenGemeinde.betreuungsstundenDokumentiertUndUeberprueft;
            restAngabenGemeinde.betreuungsstundenDokumentiertUndUeberprueftBemerkung =
                tsAngabenGemeinde.betreuungsstundenDokumentiertUndUeberprueftBemerkung;
            restAngabenGemeinde.elterngebuehrenGemaessVerordnungBerechnet =
                tsAngabenGemeinde.elterngebuehrenGemaessVerordnungBerechnet;
            restAngabenGemeinde.elterngebuehrenGemaessVerordnungBerechnetBemerkung =
                tsAngabenGemeinde.elterngebuehrenGemaessVerordnungBerechnetBemerkung;
            restAngabenGemeinde.einkommenElternBelegt =
                tsAngabenGemeinde.einkommenElternBelegt;
            restAngabenGemeinde.einkommenElternBelegtBemerkung =
                tsAngabenGemeinde.einkommenElternBelegtBemerkung;
            restAngabenGemeinde.maximalTarif =
                tsAngabenGemeinde.maximalTarif;
            restAngabenGemeinde.maximalTarifBemerkung =
                tsAngabenGemeinde.maximalTarifBemerkung;
            restAngabenGemeinde.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal =
                tsAngabenGemeinde.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal;
            restAngabenGemeinde.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung =
                tsAngabenGemeinde.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung;
            restAngabenGemeinde.ausbildungenMitarbeitendeBelegt =
                tsAngabenGemeinde.ausbildungenMitarbeitendeBelegt;
            restAngabenGemeinde.ausbildungenMitarbeitendeBelegtBemerkung =
                tsAngabenGemeinde.ausbildungenMitarbeitendeBelegtBemerkung;
            // Bemerkungen
            restAngabenGemeinde.bemerkungen =
                tsAngabenGemeinde.bemerkungen;
            restAngabenGemeinde.bemerkungStarkeVeraenderung =
                tsAngabenGemeinde.bemerkungStarkeVeraenderung;
            // Berechnungen
            restAngabenGemeinde.lastenausgleichberechtigteBetreuungsstunden =
                tsAngabenGemeinde.lastenausgleichberechtigteBetreuungsstunden;
            restAngabenGemeinde.davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet =
                tsAngabenGemeinde.davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet;
            restAngabenGemeinde.davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet =
                tsAngabenGemeinde.davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet;
            restAngabenGemeinde.normlohnkostenBetreuungBerechnet =
                tsAngabenGemeinde.normlohnkostenBetreuungBerechnet;
            restAngabenGemeinde.lastenausgleichsberechtigerBetrag =
                tsAngabenGemeinde.lastenausgleichsberechtigerBetrag;
            restAngabenGemeinde.kostenbeitragGemeinde =
                tsAngabenGemeinde.kostenbeitragGemeinde;
            restAngabenGemeinde.kostenueberschussGemeinde =
                tsAngabenGemeinde.kostenueberschussGemeinde;
            restAngabenGemeinde.erwarteterKostenbeitragGemeinde =
                tsAngabenGemeinde.erwarteterKostenbeitragGemeinde;
            restAngabenGemeinde.schlusszahlung =
                tsAngabenGemeinde.schlusszahlung;
            return restAngabenGemeinde;
        }
        return undefined;
    }

    public parseLastenausgleichTagesschuleAngabenInstitutionContainerList(
        data: Array<any>
    ): TSLastenausgleichTagesschuleAngabenInstitutionContainer[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ?
            data.map(
                item => this.parseLastenausgleichTagesschuleAngabenInstitutionContainer(
                    new TSLastenausgleichTagesschuleAngabenInstitutionContainer(),
                    item))
            :
            [
                this.parseLastenausgleichTagesschuleAngabenInstitutionContainer(
                    new TSLastenausgleichTagesschuleAngabenInstitutionContainer(),
                    data)
            ];
    }

    public parseLastenausgleichTagesschuleAngabenInstitutionContainer(
        institutionContainerTS: TSLastenausgleichTagesschuleAngabenInstitutionContainer,
        institutionContainerFromServer: any
    ): TSLastenausgleichTagesschuleAngabenInstitutionContainer {
        if (institutionContainerFromServer) {
            this.parseAbstractEntity(institutionContainerTS, institutionContainerFromServer);
            institutionContainerTS.status = institutionContainerFromServer.status;
            institutionContainerTS.institution =
                this.parseInstitution(new TSInstitution(), institutionContainerFromServer.institution);
            institutionContainerTS.angabenDeklaration =
                this.parseLastenausgleichTagesschuleAngabenInstitution(
                    new TSLastenausgleichTagesschuleAngabenInstitution(),
                    institutionContainerFromServer.angabenDeklaration);
            institutionContainerTS.angabenKorrektur =
                this.parseLastenausgleichTagesschuleAngabenInstitution(
                    new TSLastenausgleichTagesschuleAngabenInstitution(),
                    institutionContainerFromServer.angabenKorrektur);
            return institutionContainerTS;
        }
        return undefined;
    }

    private lastenausgleichTagesschuleAngabenInstitutionContainerListToRestObject(
        tsInstitutionContainerList: Array<TSLastenausgleichTagesschuleAngabenInstitutionContainer>
    ): Array<any> {
        return tsInstitutionContainerList
            ?
            tsInstitutionContainerList.map(
                item => this.lastenausgleichTagesschuleAngabenInstitutionContainerToRestObject(
                    {},
                    item))
            :
            [];
    }

    public lastenausgleichTagesschuleAngabenInstitutionContainerToRestObject(
        restInstitutionContainer: any,
        tsInstitutionContainer: TSLastenausgleichTagesschuleAngabenInstitutionContainer
    ): any {
        if (tsInstitutionContainer) {
            this.abstractEntityToRestObject(restInstitutionContainer, tsInstitutionContainer);
            restInstitutionContainer.status = tsInstitutionContainer.status;
            restInstitutionContainer.institution =
                this.institutionToRestObject({}, tsInstitutionContainer.institution);
            restInstitutionContainer.angabenDeklaration =
                this.lastenausgleichTagesschuleAngabenInstitutionToRestObject({},
                    tsInstitutionContainer.angabenDeklaration);
            restInstitutionContainer.angabenKorrektur =
                this.lastenausgleichTagesschuleAngabenInstitutionToRestObject({},
                    tsInstitutionContainer.angabenKorrektur);
            return restInstitutionContainer;
        }
        return undefined;
    }

    public parseLastenausgleichTagesschuleAngabenInstitution(
        angabenInstitutionTS: TSLastenausgleichTagesschuleAngabenInstitution,
        angabenInstitutionFromServer: any
    ): TSLastenausgleichTagesschuleAngabenInstitution | undefined {
        if (angabenInstitutionFromServer) {
            this.parseAbstractEntity(angabenInstitutionTS, angabenInstitutionFromServer);
            // A: Informationen zur Tagesschule
            angabenInstitutionTS.isLehrbetrieb = angabenInstitutionFromServer.isLehrbetrieb;
            // B: Quantitative Angaben
            angabenInstitutionTS.anzahlEingeschriebeneKinder = angabenInstitutionFromServer.anzahlEingeschriebeneKinder;
            angabenInstitutionTS.anzahlEingeschriebeneKinderKindergarten =
                angabenInstitutionFromServer.anzahlEingeschriebeneKinderKindergarten;
            angabenInstitutionTS.anzahlEingeschriebeneKinderSekundarstufe =
                angabenInstitutionFromServer.anzahlEingeschriebeneKinderSekundarstufe;
            angabenInstitutionTS.anzahlEingeschriebeneKinderPrimarstufe =
                angabenInstitutionFromServer.anzahlEingeschriebeneKinderPrimarstufe;
            angabenInstitutionTS.anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen =
                angabenInstitutionFromServer.anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen;
            angabenInstitutionTS.anzahlEingeschriebeneKinderVolksschulangebot =
                angabenInstitutionFromServer.anzahlEingeschriebeneKinderVolksschulangebot;
            angabenInstitutionTS.anzahlEingeschriebeneKinderBasisstufe =
                angabenInstitutionFromServer.anzahlEingeschriebeneKinderBasisstufe;
            angabenInstitutionTS.durchschnittKinderProTagFruehbetreuung =
                angabenInstitutionFromServer.durchschnittKinderProTagFruehbetreuung;
            angabenInstitutionTS.durchschnittKinderProTagMittag =
                angabenInstitutionFromServer.durchschnittKinderProTagMittag;
            angabenInstitutionTS.durchschnittKinderProTagNachmittag1 =
                angabenInstitutionFromServer.durchschnittKinderProTagNachmittag1;
            angabenInstitutionTS.durchschnittKinderProTagNachmittag2 =
                angabenInstitutionFromServer.durchschnittKinderProTagNachmittag2;
            angabenInstitutionTS.betreuungsstundenEinschliesslichBesondereBeduerfnisse =
                angabenInstitutionFromServer.betreuungsstundenEinschliesslichBesondereBeduerfnisse;
            // C: Qualitative Vorgaben der Tagesschuleverordnung
            angabenInstitutionTS.schuleAufBasisOrganisatorischesKonzept =
                angabenInstitutionFromServer.schuleAufBasisOrganisatorischesKonzept;
            angabenInstitutionTS.schuleAufBasisPaedagogischesKonzept =
                angabenInstitutionFromServer.schuleAufBasisPaedagogischesKonzept;
            angabenInstitutionTS.raeumlicheVoraussetzungenEingehalten =
                angabenInstitutionFromServer.raeumlicheVoraussetzungenEingehalten;
            angabenInstitutionTS.betreuungsverhaeltnisEingehalten =
                angabenInstitutionFromServer.betreuungsverhaeltnisEingehalten;
            angabenInstitutionTS.ernaehrungsGrundsaetzeEingehalten =
                angabenInstitutionFromServer.ernaehrungsGrundsaetzeEingehalten;
            // Bemerkungen
            angabenInstitutionTS.bemerkungen = angabenInstitutionFromServer.bemerkungen;

            angabenInstitutionTS.oeffnungszeiten =
                this.parseOeffnungszeitenTagesschuleList(angabenInstitutionFromServer.oeffnungszeiten);

            return angabenInstitutionTS;
        }
        return undefined;
    }

    private parseOeffnungszeitenTagesschuleList(oeffnungszeiten: any): TSOeffnungszeitenTagesschule[] {
        if (!oeffnungszeiten) {
            return [];
        }
        return Array.isArray(oeffnungszeiten)
            ? oeffnungszeiten.map(item => this.parseOeffnungszeitenTagesschule(item))
            : [this.parseOeffnungszeitenTagesschule(oeffnungszeiten)];
    }

    private parseOeffnungszeitenTagesschule(oeffnungszeiten: any): TSOeffnungszeitenTagesschule {
        const oeffnungszeitenTagesschule = new TSOeffnungszeitenTagesschule();
        oeffnungszeitenTagesschule.type = oeffnungszeiten.type;
        oeffnungszeitenTagesschule.montag = oeffnungszeiten.montag;
        oeffnungszeitenTagesschule.dienstag = oeffnungszeiten.dienstag;
        oeffnungszeitenTagesschule.mittwoch = oeffnungszeiten.mittwoch;
        oeffnungszeitenTagesschule.donnerstag = oeffnungszeiten.donnerstag;
        oeffnungszeitenTagesschule.freitag = oeffnungszeiten.freitag;
        return oeffnungszeitenTagesschule;
    }

    public lastenausgleichTagesschuleAngabenInstitutionToRestObject(
        restAngabenInstitution: any,
        tsAngabenInstitution: TSLastenausgleichTagesschuleAngabenInstitution
    ): any {
        if (tsAngabenInstitution) {
            this.abstractEntityToRestObject(restAngabenInstitution, tsAngabenInstitution);
            // A: Informationen zur Tagesschule
            restAngabenInstitution.isLehrbetrieb = tsAngabenInstitution.isLehrbetrieb;
            // B: Quantitative Angaben
            restAngabenInstitution.anzahlEingeschriebeneKinder = tsAngabenInstitution.anzahlEingeschriebeneKinder;
            restAngabenInstitution.anzahlEingeschriebeneKinderKindergarten =
                tsAngabenInstitution.anzahlEingeschriebeneKinderKindergarten;
            restAngabenInstitution.anzahlEingeschriebeneKinderSekundarstufe =
                tsAngabenInstitution.anzahlEingeschriebeneKinderSekundarstufe;
            restAngabenInstitution.anzahlEingeschriebeneKinderPrimarstufe =
                tsAngabenInstitution.anzahlEingeschriebeneKinderPrimarstufe;
            restAngabenInstitution.anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen =
                tsAngabenInstitution.anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen;
            restAngabenInstitution.anzahlEingeschriebeneKinderVolksschulangebot =
                tsAngabenInstitution.anzahlEingeschriebeneKinderVolksschulangebot;
            restAngabenInstitution.anzahlEingeschriebeneKinderBasisstufe =
                tsAngabenInstitution.anzahlEingeschriebeneKinderBasisstufe;
            restAngabenInstitution.durchschnittKinderProTagFruehbetreuung =
                tsAngabenInstitution.durchschnittKinderProTagFruehbetreuung;
            restAngabenInstitution.durchschnittKinderProTagMittag = tsAngabenInstitution.durchschnittKinderProTagMittag;
            restAngabenInstitution.durchschnittKinderProTagNachmittag1 =
                tsAngabenInstitution.durchschnittKinderProTagNachmittag1;
            restAngabenInstitution.durchschnittKinderProTagNachmittag2 =
                tsAngabenInstitution.durchschnittKinderProTagNachmittag2;
            restAngabenInstitution.betreuungsstundenEinschliesslichBesondereBeduerfnisse =
                tsAngabenInstitution.betreuungsstundenEinschliesslichBesondereBeduerfnisse;
            // C: Qualitative Vorgaben der Tagesschuleverordnung
            restAngabenInstitution.schuleAufBasisOrganisatorischesKonzept =
                tsAngabenInstitution.schuleAufBasisOrganisatorischesKonzept;
            restAngabenInstitution.schuleAufBasisPaedagogischesKonzept =
                tsAngabenInstitution.schuleAufBasisPaedagogischesKonzept;
            restAngabenInstitution.raeumlicheVoraussetzungenEingehalten =
                tsAngabenInstitution.raeumlicheVoraussetzungenEingehalten;
            restAngabenInstitution.betreuungsverhaeltnisEingehalten =
                tsAngabenInstitution.betreuungsverhaeltnisEingehalten;
            restAngabenInstitution.ernaehrungsGrundsaetzeEingehalten =
                tsAngabenInstitution.ernaehrungsGrundsaetzeEingehalten;
            // Bemerkungen
            restAngabenInstitution.bemerkungen = tsAngabenInstitution.bemerkungen;

            restAngabenInstitution.oeffnungszeiten = tsAngabenInstitution.oeffnungszeiten;

            return restAngabenInstitution;
        }
        return undefined;
    }

    public parseWizardStepXList(data: any): TSWizardStepX[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseWizardStepX(item))
            : [this.parseWizardStepX(data)];
    }

    public parseWizardStepX(data: any): TSWizardStepX {
        const wizardStep = new TSWizardStepX();
        wizardStep.stepName = data.stepName;
        wizardStep.wizardTyp = data.wizardTyp;
        wizardStep.disabled = data.disabled;
        wizardStep.status = data.status;
        return wizardStep;
    }

    public sozialdienstStammdatenToRestObject(
        restStammdaten: any,
        stammdaten: TSSozialdienstStammdaten
    ): any {
        if (stammdaten) {
            this.abstractEntityToRestObject(restStammdaten, stammdaten);

            restStammdaten.sozialdienst = this.sozialdienstToRestObject({}, stammdaten.sozialdienst);
            restStammdaten.adresse = this.adresseToRestObject({}, stammdaten.adresse);
            restStammdaten.mail = stammdaten.mail;
            restStammdaten.telefon = stammdaten.telefon;
            restStammdaten.webseite = stammdaten.webseite;
            return restStammdaten;
        }
        return undefined;
    }

    public parseSozialdienstStammdaten(
        stammdatenTS: TSSozialdienstStammdaten,
        stammdatenFromServer: any
    ): TSSozialdienstStammdaten | undefined {
        if (stammdatenFromServer) {
            this.parseAbstractEntity(stammdatenTS, stammdatenFromServer);
            stammdatenTS.sozialdienst = this.parseSozialdienst(new TSSozialdienst(), stammdatenFromServer.sozialdienst);
            stammdatenTS.adresse = this.parseAdresse(new TSAdresse(), stammdatenFromServer.adresse);
            stammdatenTS.mail = stammdatenFromServer.mail;
            stammdatenTS.telefon = stammdatenFromServer.telefon;
            stammdatenTS.webseite = stammdatenFromServer.webseite;
            return stammdatenTS;
        }
        return undefined;
    }

    public sozialdienstToRestObject(restSozialdienst: any, sozialdienst: TSSozialdienst): any {
        if (sozialdienst) {
            this.abstractEntityToRestObject(restSozialdienst, sozialdienst);
            restSozialdienst.name = sozialdienst.name;
            restSozialdienst.status = sozialdienst.status;
            return restSozialdienst;
        }
        return undefined;
    }

    public parseSozialdienst(sozialdienstTS: TSSozialdienst, sozialdienstFromServer: any): TSSozialdienst | undefined {
        if (sozialdienstFromServer) {
            this.parseAbstractEntity(sozialdienstTS, sozialdienstFromServer);
            sozialdienstTS.name = sozialdienstFromServer.name;
            sozialdienstTS.status = sozialdienstFromServer.status;
            return sozialdienstTS;
        }
        return undefined;
    }

    private sozialdienstListToRestObject(sozialdienstListTS: Array<TSSozialdienst>): Array<any> {
        return sozialdienstListTS
            ? sozialdienstListTS
                .map(item => this.sozialdienstToRestObject({}, item))
                .filter(szd => EbeguUtil.isNotNullOrUndefined(szd))
            : [];
    }

    public parseSozialdienstList(data: any): TSSozialdienst[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseSozialdienst(new TSSozialdienst(), item))
            : [this.parseSozialdienst(new TSSozialdienst(), data)];
    }

    public ferienbetreuungContainerToRestObject(
        restContainer: any,
        containerTS: TSFerienbetreuungAngabenContainer
    ): any {
        if (!containerTS) {
            return undefined;
        }
        this.abstractEntityToRestObject(restContainer, containerTS);
        restContainer.status = containerTS.status;
        restContainer.gemeinde = this.gemeindeToRestObject({}, containerTS.gemeinde);
        restContainer.gesuchsperiode = this.gesuchsperiodeToRestObject({}, containerTS.gesuchsperiode);
        restContainer.angabenDeklaration = this.ferienbetreuungToRestObject({}, containerTS.angabenDeklaration);
        restContainer.angabenKorrektur = this.ferienbetreuungToRestObject({}, containerTS.angabenKorrektur);
        restContainer.internerKommentar = containerTS.internerKommentar;
        restContainer.verantwortlicher = this.benutzerNoDetailsToRestObject({}, containerTS.verantwortlicher);
        return restContainer;
    }

    private ferienbetreuungToRestObject(restFerienbetreuung: any, ferienbetreuungTS: TSFerienbetreuungAngaben): any {
        if (!ferienbetreuungTS) {
            return undefined;
        }
        this.abstractEntityToRestObject(restFerienbetreuung, ferienbetreuungTS);
        restFerienbetreuung.stammdaten =
            this.ferienbetreuungStammdatenToRestObject({}, ferienbetreuungTS.stammdaten);
        restFerienbetreuung.angebot = this.ferienbetreuungAngebotToRestObject({}, ferienbetreuungTS.angebot);
        restFerienbetreuung.nutzung = this.ferienbetreuungNutzungToRestObject({}, ferienbetreuungTS.nutzung);
        restFerienbetreuung.kostenEinnahmen =
            this.ferienbetreuungKostenEinnahmenToRestObject({}, ferienbetreuungTS.kostenEinnahmen);
        restFerienbetreuung.berechnungen =
            this.parseFerienbetreuungBerechnungenToRestObject({}, ferienbetreuungTS.berechnungen);

        return restFerienbetreuung;
        // never send kantonsbeitrag and gemeindebeitrag to server
    }

    public ferienbetreuungStammdatenToRestObject(
        restStammdaten: any,
        stammdatenTS: TSFerienbetreuungAngabenStammdaten
    ): any {
        if (!stammdatenTS) {
            return undefined;
        }
        this.abstractEntityToRestObject(restStammdaten, stammdatenTS);
        restStammdaten.amAngebotBeteiligteGemeinden = stammdatenTS.amAngebotBeteiligteGemeinden;
        restStammdaten.seitWannFerienbetreuungen = DateUtil.momentToLocalDate(stammdatenTS.seitWannFerienbetreuungen);
        restStammdaten.traegerschaft = stammdatenTS.traegerschaft;
        restStammdaten.stammdatenAdresse = this.adresseToRestObject({}, stammdatenTS.stammdatenAdresse);
        restStammdaten.stammdatenKontaktpersonVorname = stammdatenTS.stammdatenKontaktpersonVorname;
        restStammdaten.stammdatenKontaktpersonNachname = stammdatenTS.stammdatenKontaktpersonNachname;
        restStammdaten.stammdatenKontaktpersonFunktion = stammdatenTS.stammdatenKontaktpersonFunktion;
        restStammdaten.stammdatenKontaktpersonTelefon = stammdatenTS.stammdatenKontaktpersonTelefon
            ? stammdatenTS.stammdatenKontaktpersonTelefon : null;
        restStammdaten.stammdatenKontaktpersonEmail = stammdatenTS.stammdatenKontaktpersonEmail
            ? stammdatenTS.stammdatenKontaktpersonEmail : null;
        restStammdaten.iban = (!!stammdatenTS.iban) ? stammdatenTS.iban : null;
        restStammdaten.kontoinhaber = stammdatenTS.kontoinhaber;
        restStammdaten.adresseKontoinhaber = this.adresseToRestObject({}, stammdatenTS.adresseKontoinhaber);
        restStammdaten.vermerkAuszahlung = stammdatenTS.vermerkAuszahlung;
        return restStammdaten;
    }

    public ferienbetreuungAngebotToRestObject(restAngebot: any, angebotTS: TSFerienbetreuungAngabenAngebot): any {
        if (!angebotTS) {
            return undefined;
        }
        this.abstractEntityToRestObject(restAngebot, angebotTS);
        restAngebot.angebot = angebotTS.angebot;
        restAngebot.angebotKontaktpersonVorname = angebotTS.angebotKontaktpersonVorname;
        restAngebot.angebotKontaktpersonNachname = angebotTS.angebotKontaktpersonNachname;
        restAngebot.angebotAdresse = this.adresseToRestObject({}, angebotTS.angebotAdresse);
        restAngebot.anzahlFerienwochenHerbstferien = angebotTS.anzahlFerienwochenHerbstferien;
        restAngebot.anzahlFerienwochenWinterferien = angebotTS.anzahlFerienwochenWinterferien;
        restAngebot.anzahlFerienwochenSportferien = angebotTS.anzahlFerienwochenSportferien;
        restAngebot.anzahlFerienwochenFruehlingsferien = angebotTS.anzahlFerienwochenFruehlingsferien;
        restAngebot.anzahlFerienwochenSommerferien = angebotTS.anzahlFerienwochenSommerferien;
        restAngebot.anzahlTage = angebotTS.anzahlTage;
        restAngebot.bemerkungenAnzahlFerienwochen = angebotTS.bemerkungenAnzahlFerienwochen;
        restAngebot.anzahlStundenProBetreuungstag = angebotTS.anzahlStundenProBetreuungstag;
        restAngebot.betreuungErfolgtTagsueber = angebotTS.betreuungErfolgtTagsueber;
        restAngebot.bemerkungenOeffnungszeiten = angebotTS.bemerkungenOeffnungszeiten;
        angebotTS.finanziellBeteiligteGemeinden.sort();
        restAngebot.finanziellBeteiligteGemeinden = angebotTS.finanziellBeteiligteGemeinden;
        restAngebot.gemeindeFuehrtAngebotSelber = angebotTS.gemeindeFuehrtAngebotSelber;
        restAngebot.gemeindeFuehrtAngebotInKooperation = angebotTS.gemeindeFuehrtAngebotInKooperation;
        restAngebot.gemeindeBeauftragtExterneAnbieter = angebotTS.gemeindeBeauftragtExterneAnbieter;
        restAngebot.angebotVereineUndPrivateIntegriert = angebotTS.angebotVereineUndPrivateIntegriert;
        restAngebot.bemerkungenKooperation = angebotTS.bemerkungenKooperation;
        restAngebot.leitungDurchPersonMitAusbildung = angebotTS.leitungDurchPersonMitAusbildung;
        restAngebot.betreuungDurchPersonenMitErfahrung = angebotTS.betreuungDurchPersonenMitErfahrung;
        restAngebot.anzahlKinderAngemessen = angebotTS.anzahlKinderAngemessen;
        restAngebot.betreuungsschluessel = angebotTS.betreuungsschluessel;
        restAngebot.bemerkungenPersonal = angebotTS.bemerkungenPersonal;
        restAngebot.fixerTarifKinderDerGemeinde = angebotTS.fixerTarifKinderDerGemeinde;
        restAngebot.einkommensabhaengigerTarifKinderDerGemeinde =
            angebotTS.einkommensabhaengigerTarifKinderDerGemeinde;
        restAngebot.tagesschuleTarifGiltFuerFerienbetreuung = angebotTS.tagesschuleTarifGiltFuerFerienbetreuung;
        restAngebot.ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet =
            angebotTS.ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet;
        restAngebot.kinderAusAnderenGemeindenZahlenAnderenTarif =
            angebotTS.kinderAusAnderenGemeindenZahlenAnderenTarif;
        restAngebot.bemerkungenTarifsystem = angebotTS.bemerkungenTarifsystem;
        return restAngebot;
    }

    public ferienbetreuungNutzungToRestObject(restNutzung: any, nutzungTS: TSFerienbetreuungAngabenNutzung): any {
        if (!nutzungTS) {
            return undefined;
        }
        this.abstractEntityToRestObject(restNutzung, nutzungTS);
        restNutzung.anzahlBetreuungstageKinderBern = nutzungTS.anzahlBetreuungstageKinderBern;
        restNutzung.betreuungstageKinderDieserGemeinde = nutzungTS.betreuungstageKinderDieserGemeinde;
        restNutzung.betreuungstageKinderDieserGemeindeSonderschueler =
            nutzungTS.betreuungstageKinderDieserGemeindeSonderschueler;
        restNutzung.davonBetreuungstageKinderAndererGemeinden = nutzungTS.davonBetreuungstageKinderAndererGemeinden;
        restNutzung.davonBetreuungstageKinderAndererGemeindenSonderschueler =
            nutzungTS.davonBetreuungstageKinderAndererGemeindenSonderschueler;
        restNutzung.anzahlBetreuteKinder = nutzungTS.anzahlBetreuteKinder;
        restNutzung.anzahlBetreuteKinderSonderschueler = nutzungTS.anzahlBetreuteKinderSonderschueler;
        restNutzung.anzahlBetreuteKinder1Zyklus = nutzungTS.anzahlBetreuteKinder1Zyklus;
        restNutzung.anzahlBetreuteKinder2Zyklus = nutzungTS.anzahlBetreuteKinder2Zyklus;
        restNutzung.anzahlBetreuteKinder3Zyklus = nutzungTS.anzahlBetreuteKinder3Zyklus;
        return restNutzung;
    }

    public ferienbetreuungKostenEinnahmenToRestObject(
        restKostenEinnahmen: any,
        kostenEinnahmenTS: TSFerienbetreuungAngabenKostenEinnahmen
    ): any {
        if (!kostenEinnahmenTS) {
            return undefined;
        }
        this.abstractEntityToRestObject(restKostenEinnahmen, kostenEinnahmenTS);
        restKostenEinnahmen.personalkosten = kostenEinnahmenTS.personalkosten;
        restKostenEinnahmen.personalkostenLeitungAdmin = kostenEinnahmenTS.personalkostenLeitungAdmin;
        restKostenEinnahmen.sachkosten = kostenEinnahmenTS.sachkosten;
        restKostenEinnahmen.verpflegungskosten = kostenEinnahmenTS.verpflegungskosten;
        restKostenEinnahmen.weitereKosten = kostenEinnahmenTS.weitereKosten;
        restKostenEinnahmen.bemerkungenKosten = kostenEinnahmenTS.bemerkungenKosten;
        restKostenEinnahmen.elterngebuehren = kostenEinnahmenTS.elterngebuehren;
        restKostenEinnahmen.weitereEinnahmen = kostenEinnahmenTS.weitereEinnahmen;
        restKostenEinnahmen.sockelbeitrag = kostenEinnahmenTS.sockelbeitrag;
        restKostenEinnahmen.beitraegeNachAnmeldungen = kostenEinnahmenTS.beitraegeNachAnmeldungen;
        restKostenEinnahmen.vorfinanzierteKantonsbeitraege = kostenEinnahmenTS.vorfinanzierteKantonsbeitraege;
        restKostenEinnahmen.eigenleistungenGemeinde = kostenEinnahmenTS.eigenleistungenGemeinde;
        return restKostenEinnahmen;
    }

    public parseFerienbetreuungContainer(containerTS: TSFerienbetreuungAngabenContainer, containerFromServer: any):
        TSFerienbetreuungAngabenContainer | undefined {

        if (!containerFromServer) {
            return undefined;
        }
        this.parseAbstractEntity(containerTS, containerFromServer);
        containerTS.status = containerFromServer.status;
        containerTS.gemeinde = this.parseGemeinde(new TSGemeinde(), containerFromServer.gemeinde);
        containerTS.gesuchsperiode =
            this.parseGesuchsperiode(new TSGesuchsperiode(), containerFromServer.gesuchsperiode);
        containerTS.angabenDeklaration =
            this.parseFerienbetreuung(new TSFerienbetreuungAngaben(), containerFromServer.angabenDeklaration);
        containerTS.angabenKorrektur =
            this.parseFerienbetreuung(new TSFerienbetreuungAngaben(), containerFromServer.angabenKorrektur);
        containerTS.internerKommentar = containerFromServer.internerKommentar;
        containerTS.verantwortlicher =
            this.parseUserNoDetails(new TSBenutzerNoDetails(), containerFromServer.verantwortlicher);
        return containerTS;
    }

    private parseFerienbetreuung(ferienbetreuungTS: TSFerienbetreuungAngaben, ferienbetreuungFromServer: any):
        TSFerienbetreuungAngaben | undefined {

        if (!ferienbetreuungFromServer) {
            return undefined;
        }
        this.parseAbstractEntity(ferienbetreuungTS, ferienbetreuungFromServer);
        ferienbetreuungTS.stammdaten = this.parseFerienbetreuungStammdaten(
            new TSFerienbetreuungAngabenStammdaten(),
            ferienbetreuungFromServer.stammdaten
        );
        ferienbetreuungTS.angebot = this.parseFerienbetreuungAngebot(
            new TSFerienbetreuungAngabenAngebot(),
            ferienbetreuungFromServer.angebot
        );
        ferienbetreuungTS.nutzung = this.parseFerienbetreuungNutzung(
            new TSFerienbetreuungAngabenNutzung(),
            ferienbetreuungFromServer.nutzung
        );
        ferienbetreuungTS.kostenEinnahmen = this.parseFerienbetreuungKostenEinnahmen(
            new TSFerienbetreuungAngabenKostenEinnahmen(),
            ferienbetreuungFromServer.kostenEinnahmen
        );
        ferienbetreuungTS.berechnungen = this.parseFerienbetreuungBerechnung(
            new TSFerienbetreuungBerechnung(),
            ferienbetreuungFromServer.berechnungen
        );
        ferienbetreuungTS.kantonsbeitrag = ferienbetreuungFromServer.kantonsbeitrag;
        ferienbetreuungTS.gemeindebeitrag = ferienbetreuungFromServer.gemeindebeitrag;
        return ferienbetreuungTS;
    }

    public parseFerienbetreuungStammdaten(stammdatenTS: TSFerienbetreuungAngabenStammdaten, stammdatenFromServer: any):
        TSFerienbetreuungAngabenStammdaten | undefined {

        if (!stammdatenFromServer) {
            return undefined;
        }
        this.parseAbstractEntity(stammdatenTS, stammdatenFromServer);
        stammdatenTS.status = stammdatenFromServer.status;
        stammdatenFromServer.amAngebotBeteiligteGemeinden.sort();
        stammdatenTS.amAngebotBeteiligteGemeinden = stammdatenFromServer.amAngebotBeteiligteGemeinden;
        stammdatenTS.seitWannFerienbetreuungen =
            DateUtil.localDateToMoment(stammdatenFromServer.seitWannFerienbetreuungen);
        stammdatenTS.traegerschaft = stammdatenFromServer.traegerschaft;
        stammdatenTS.stammdatenAdresse = this.parseAdresse(new TSAdresse(), stammdatenFromServer.stammdatenAdresse);
        stammdatenTS.stammdatenKontaktpersonVorname = stammdatenFromServer.stammdatenKontaktpersonVorname;
        stammdatenTS.stammdatenKontaktpersonNachname = stammdatenFromServer.stammdatenKontaktpersonNachname;
        stammdatenTS.stammdatenKontaktpersonFunktion = stammdatenFromServer.stammdatenKontaktpersonFunktion;
        stammdatenTS.stammdatenKontaktpersonTelefon = stammdatenFromServer.stammdatenKontaktpersonTelefon;
        stammdatenTS.stammdatenKontaktpersonEmail = stammdatenFromServer.stammdatenKontaktpersonEmail;
        stammdatenTS.iban = stammdatenFromServer.iban;
        stammdatenTS.kontoinhaber = stammdatenFromServer.kontoinhaber;
        stammdatenTS.adresseKontoinhaber =
            this.parseAdresse(new TSAdresse(), stammdatenFromServer.adresseKontoinhaber);
        stammdatenTS.vermerkAuszahlung = stammdatenFromServer.vermerkAuszahlung;
        return stammdatenTS;
    }

    public parseFerienbetreuungAngebot(angebotTS: TSFerienbetreuungAngabenAngebot, angebotFromServer: any):
        TSFerienbetreuungAngabenAngebot | undefined {

        if (!angebotFromServer) {
            return undefined;
        }
        this.parseAbstractEntity(angebotTS, angebotFromServer);
        angebotTS.status = angebotFromServer.status;
        angebotTS.angebot = angebotFromServer.angebot;
        angebotTS.angebotKontaktpersonVorname = angebotFromServer.angebotKontaktpersonVorname;
        angebotTS.angebotKontaktpersonNachname = angebotFromServer.angebotKontaktpersonNachname;
        angebotTS.angebotAdresse = this.parseAdresse(new TSAdresse(), angebotFromServer.angebotAdresse);
        angebotTS.anzahlFerienwochenHerbstferien = angebotFromServer.anzahlFerienwochenHerbstferien;
        angebotTS.anzahlFerienwochenWinterferien = angebotFromServer.anzahlFerienwochenWinterferien;
        angebotTS.anzahlFerienwochenSportferien = angebotFromServer.anzahlFerienwochenSportferien;
        angebotTS.anzahlFerienwochenFruehlingsferien = angebotFromServer.anzahlFerienwochenFruehlingsferien;
        angebotTS.anzahlFerienwochenSommerferien = angebotFromServer.anzahlFerienwochenSommerferien;
        angebotTS.anzahlTage = angebotFromServer.anzahlTage;
        angebotTS.bemerkungenAnzahlFerienwochen = angebotFromServer.bemerkungenAnzahlFerienwochen;
        angebotTS.anzahlStundenProBetreuungstag = angebotFromServer.anzahlStundenProBetreuungstag;
        angebotTS.betreuungErfolgtTagsueber = angebotFromServer.betreuungErfolgtTagsueber;
        angebotTS.bemerkungenOeffnungszeiten = angebotFromServer.bemerkungenOeffnungszeiten;
        angebotFromServer.finanziellBeteiligteGemeinden.sort();
        angebotTS.finanziellBeteiligteGemeinden = angebotFromServer.finanziellBeteiligteGemeinden;
        angebotTS.gemeindeFuehrtAngebotSelber = angebotFromServer.gemeindeFuehrtAngebotSelber;
        angebotTS.gemeindeFuehrtAngebotInKooperation = angebotFromServer.gemeindeFuehrtAngebotInKooperation;
        angebotTS.gemeindeBeauftragtExterneAnbieter = angebotFromServer.gemeindeBeauftragtExterneAnbieter;
        angebotTS.angebotVereineUndPrivateIntegriert = angebotFromServer.angebotVereineUndPrivateIntegriert;
        angebotTS.bemerkungenKooperation = angebotFromServer.bemerkungenKooperation;
        angebotTS.leitungDurchPersonMitAusbildung = angebotFromServer.leitungDurchPersonMitAusbildung;
        angebotTS.betreuungDurchPersonenMitErfahrung = angebotFromServer.betreuungDurchPersonenMitErfahrung;
        angebotTS.anzahlKinderAngemessen = angebotFromServer.anzahlKinderAngemessen;
        angebotTS.betreuungsschluessel = angebotFromServer.betreuungsschluessel;
        angebotTS.bemerkungenPersonal = angebotFromServer.bemerkungenPersonal;
        angebotTS.fixerTarifKinderDerGemeinde = angebotFromServer.fixerTarifKinderDerGemeinde;
        angebotTS.einkommensabhaengigerTarifKinderDerGemeinde =
            angebotFromServer.einkommensabhaengigerTarifKinderDerGemeinde;
        angebotTS.tagesschuleTarifGiltFuerFerienbetreuung =
            angebotFromServer.tagesschuleTarifGiltFuerFerienbetreuung;
        angebotTS.ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet =
            angebotFromServer.ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet;
        angebotTS.kinderAusAnderenGemeindenZahlenAnderenTarif =
            angebotFromServer.kinderAusAnderenGemeindenZahlenAnderenTarif;
        angebotTS.bemerkungenTarifsystem = angebotFromServer.bemerkungenTarifsystem;
        return angebotTS;
    }

    public parseFerienbetreuungNutzung(nutzungTS: TSFerienbetreuungAngabenNutzung, nutzungFromServer: any):
        TSFerienbetreuungAngabenNutzung | undefined {

        if (!nutzungFromServer) {
            return undefined;
        }
        this.parseAbstractEntity(nutzungTS, nutzungFromServer);
        nutzungTS.status = nutzungFromServer.status;
        nutzungTS.anzahlBetreuungstageKinderBern = nutzungFromServer.anzahlBetreuungstageKinderBern;
        nutzungTS.betreuungstageKinderDieserGemeinde = nutzungFromServer.betreuungstageKinderDieserGemeinde;
        nutzungTS.betreuungstageKinderDieserGemeindeSonderschueler =
            nutzungFromServer.betreuungstageKinderDieserGemeindeSonderschueler;
        nutzungTS.davonBetreuungstageKinderAndererGemeinden =
            nutzungFromServer.davonBetreuungstageKinderAndererGemeinden;
        nutzungTS.davonBetreuungstageKinderAndererGemeindenSonderschueler =
            nutzungFromServer.davonBetreuungstageKinderAndererGemeindenSonderschueler;
        nutzungTS.anzahlBetreuteKinder = nutzungFromServer.anzahlBetreuteKinder;
        nutzungTS.anzahlBetreuteKinderSonderschueler = nutzungFromServer.anzahlBetreuteKinderSonderschueler;
        nutzungTS.anzahlBetreuteKinder1Zyklus = nutzungFromServer.anzahlBetreuteKinder1Zyklus;
        nutzungTS.anzahlBetreuteKinder2Zyklus = nutzungFromServer.anzahlBetreuteKinder2Zyklus;
        nutzungTS.anzahlBetreuteKinder3Zyklus = nutzungFromServer.anzahlBetreuteKinder3Zyklus;
        return nutzungTS;
    }

    public parseFerienbetreuungKostenEinnahmen(
        kostenEinnahmenTS: TSFerienbetreuungAngabenKostenEinnahmen,
        kostenEinnahmenFromServer: any
    ): TSFerienbetreuungAngabenKostenEinnahmen | undefined {

        if (!kostenEinnahmenFromServer) {
            return undefined;
        }
        this.parseAbstractEntity(kostenEinnahmenTS, kostenEinnahmenFromServer);
        kostenEinnahmenTS.status = kostenEinnahmenFromServer.status;
        kostenEinnahmenTS.personalkosten = kostenEinnahmenFromServer.personalkosten;
        kostenEinnahmenTS.personalkostenLeitungAdmin = kostenEinnahmenFromServer.personalkostenLeitungAdmin;
        kostenEinnahmenTS.sachkosten = kostenEinnahmenFromServer.sachkosten;
        kostenEinnahmenTS.verpflegungskosten = kostenEinnahmenFromServer.verpflegungskosten;
        kostenEinnahmenTS.weitereKosten = kostenEinnahmenFromServer.weitereKosten;
        kostenEinnahmenTS.bemerkungenKosten = kostenEinnahmenFromServer.bemerkungenKosten;
        kostenEinnahmenTS.elterngebuehren = kostenEinnahmenFromServer.elterngebuehren;
        kostenEinnahmenTS.weitereEinnahmen = kostenEinnahmenFromServer.weitereEinnahmen;
        kostenEinnahmenTS.sockelbeitrag = kostenEinnahmenFromServer.sockelbeitrag;
        kostenEinnahmenTS.beitraegeNachAnmeldungen = kostenEinnahmenFromServer.beitraegeNachAnmeldungen;
        kostenEinnahmenTS.vorfinanzierteKantonsbeitraege = kostenEinnahmenFromServer.vorfinanzierteKantonsbeitraege;
        kostenEinnahmenTS.eigenleistungenGemeinde = kostenEinnahmenFromServer.eigenleistungenGemeinde;
        return kostenEinnahmenTS;
    }

    public parseFerienbetreuungBerechnung(
        berechnungTS: TSFerienbetreuungBerechnung,
        berechnungFromServer: any
    ): TSFerienbetreuungBerechnung | undefined {

        if (!berechnungFromServer) {
            return undefined;
        }

        this.parseAbstractEntity(berechnungTS, berechnungFromServer);
        berechnungTS.totalKosten = berechnungFromServer.totalKosten;
        berechnungTS.totalLeistungenLeistungsvertrag = berechnungFromServer.totalLeistungenLeistungsvertrag;
        berechnungTS.betreuungstageKinderDieserGemeindeMinusSonderschueler =
            berechnungFromServer.betreuungstageKinderDieserGemeindeMinusSonderschueler;
        berechnungTS.betreuungstageKinderAndererGemeindeMinusSonderschueler =
            berechnungFromServer._betreuungstageKinderAndererGemeindeMinusSonderschueler;
        berechnungTS.totalKantonsbeitrag = berechnungFromServer.totalKantonsbeitrag;
        berechnungTS.beitragFuerKinderDerAnbietendenGemeinde =
            berechnungFromServer.beitragFuerKinderDerAnbietendenGemeinde;
        berechnungTS.beteiligungZuTief = berechnungFromServer.beteiligungZuTief;
        return berechnungTS;
    }

    public parseFerienbetreuungBerechnungenToRestObject(restBerechnung: any, berechnung: TSFerienbetreuungBerechnung): any {
        this.abstractEntityToRestObject(restBerechnung, berechnung);
        restBerechnung.totalKosten = berechnung.totalKosten;
        restBerechnung.betreuungstageKinderDieserGemeindeMinusSonderschueler =
            berechnung.betreuungstageKinderDieserGemeindeMinusSonderschueler;
        restBerechnung.betreuungstageKinderAndererGemeindeMinusSonderschueler =
            berechnung.betreuungstageKinderAndererGemeindeMinusSonderschueler;
        restBerechnung.totalKantonsbeitrag = berechnung.totalKantonsbeitrag;
        restBerechnung.totalEinnahmen = berechnung.totalEinnahmen;
        restBerechnung.beitragKinderAnbietendenGemeinde = berechnung.beitragFuerKinderDerAnbietendenGemeinde;
        restBerechnung.beteiligungAnbietendenGemeinde = berechnung.beteiligungDurchAnbietendeGemeinde;
        restBerechnung.beteiligungZuTief = berechnung.beteiligungZuTief;

        return restBerechnung;
    }

    public parseFerienbetreuungDokumente(data: any): TSFerienbetreuungDokument[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseFerienbetreuungDokument(new TSFerienbetreuungDokument(), item))
            : [this.parseFerienbetreuungDokument(new TSFerienbetreuungDokument(), data)];
    }

    public parseFerienbetreuungDokument(
        dokument: TSFerienbetreuungDokument,
        dokumentFromServer: any
    ): TSFerienbetreuungDokument {
        if (!dokumentFromServer) {
            return undefined;
        }
        this.parseTSFileDokument(dokument, dokumentFromServer);
        dokument.timestampUpload = DateUtil.localDateTimeToMoment(dokumentFromServer.timestampUpload);
        return dokument;
    }

    public sozialdienstFallToRestObject(
        restSozialdienstFall: any,
        sozialdienstFall: TSSozialdienstFall
    ): any {
        if (sozialdienstFall) {
            this.abstractEntityToRestObject(restSozialdienstFall, sozialdienstFall);

            restSozialdienstFall.sozialdienst = this.sozialdienstToRestObject({}, sozialdienstFall.sozialdienst);
            restSozialdienstFall.adresse = this.adresseToRestObject({}, sozialdienstFall.adresse);
            restSozialdienstFall.name = sozialdienstFall.name;
            restSozialdienstFall.vorname = sozialdienstFall.vorname;
            restSozialdienstFall.geburtsdatum = DateUtil.momentToLocalDate(sozialdienstFall.geburtsdatum);
            restSozialdienstFall.status = sozialdienstFall.status;
            restSozialdienstFall.nameGs2 = sozialdienstFall.nameGs2;
            restSozialdienstFall.vornameGs2 = sozialdienstFall.vornameGs2;
            restSozialdienstFall.geburtsdatumGs2 = DateUtil.momentToLocalDate(sozialdienstFall.geburtsdatumGs2);
            return restSozialdienstFall;
        }
        return undefined;
    }

    public parseSozialdienstFall(
        sozialdienstFallTS: TSSozialdienstFall,
        sozialdienstFallFromServer: any
    ): TSSozialdienstFall | undefined {
        if (sozialdienstFallFromServer) {
            this.parseAbstractEntity(sozialdienstFallTS, sozialdienstFallFromServer);
            sozialdienstFallTS.sozialdienst =
                this.parseSozialdienst(new TSSozialdienst(), sozialdienstFallFromServer.sozialdienst);
            sozialdienstFallTS.adresse = this.parseAdresse(new TSAdresse(), sozialdienstFallFromServer.adresse);
            sozialdienstFallTS.name = sozialdienstFallFromServer.name;
            sozialdienstFallTS.vorname = sozialdienstFallFromServer.vorname;
            sozialdienstFallTS.geburtsdatum = DateUtil.localDateToMoment(sozialdienstFallFromServer.geburtsdatum);
            sozialdienstFallTS.nameGs2 = sozialdienstFallFromServer.nameGs2;
            sozialdienstFallTS.vornameGs2 = sozialdienstFallFromServer.vornameGs2;
            sozialdienstFallTS.geburtsdatumGs2 = DateUtil.localDateToMoment(sozialdienstFallFromServer.geburtsdatumGs2);
            sozialdienstFallTS.status = sozialdienstFallFromServer.status;
            return sozialdienstFallTS;
        }
        return undefined;
    }

    public parseAnzahlEingeschriebeneKinder(
        anzahlEingeschriebeneKinder: TSAnzahlEingeschriebeneKinder,
        restAnzahlEingeschriebeneKinder: any
    ): TSAnzahlEingeschriebeneKinder {
        anzahlEingeschriebeneKinder.overall = restAnzahlEingeschriebeneKinder.overall;
        anzahlEingeschriebeneKinder.vorschulalter = restAnzahlEingeschriebeneKinder.vorschulalter;
        anzahlEingeschriebeneKinder.kindergarten = restAnzahlEingeschriebeneKinder.kindergarten;
        anzahlEingeschriebeneKinder.primarstufe = restAnzahlEingeschriebeneKinder.primarstufe;
        anzahlEingeschriebeneKinder.sekundarstufe = restAnzahlEingeschriebeneKinder.sekundarstufe;
        return anzahlEingeschriebeneKinder;
    }

    public parseDurchschnittKinderProTag(
        tsDurchschnittKinderProTag: TSDurchschnittKinderProTag,
        restDurchschnittKinderProTag: any
    ): TSDurchschnittKinderProTag {
        tsDurchschnittKinderProTag.fruehbetreuung = restDurchschnittKinderProTag.fruehbetreuung;
        tsDurchschnittKinderProTag.mittagsbetreuung = restDurchschnittKinderProTag.mittagsbetreuung;
        tsDurchschnittKinderProTag.nachmittagsbetreuung1 = restDurchschnittKinderProTag.nachmittagsbetreuung1;
        tsDurchschnittKinderProTag.nachmittagsbetreuung2 = restDurchschnittKinderProTag.nachmittagsbetreuung2;
        return tsDurchschnittKinderProTag;
    }

    public parseSozialdienstFallDokumente(data: any): TSSozialdienstFallDokument[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseSozialdienstFallDokument(new TSSozialdienstFallDokument(), item))
            : [this.parseSozialdienstFallDokument(new TSSozialdienstFallDokument(), data)];
    }

    public parseSozialdienstFallDokument(
        vollMachtDokument: TSSozialdienstFallDokument,
        dokumentFromServer: any
    ): TSSozialdienstFallDokument {
        if (!dokumentFromServer) {
            return undefined;
        }
        this.parseTSFileDokument(vollMachtDokument, dokumentFromServer);
        vollMachtDokument.timestampUpload = DateUtil.localDateTimeToMoment(dokumentFromServer.timestampUpload);
        return vollMachtDokument;
    }

    private parseTSFileDokument(dokument: TSFile, dokumentFromServer: any): TSFile {
        this.parseAbstractMutableEntity(dokument, dokumentFromServer);
        dokument.filename = dokumentFromServer.filename;
        dokument.filepfad = dokumentFromServer.filepfad;
        dokument.filesize = dokumentFromServer.filesize;
        return dokument;
    }

    public parseTSBetreuungMonitoringList(data: any): TSBetreuungMonitoring[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseTSBetreuungMonitoring(new TSBetreuungMonitoring(), item))
            : [this.parseTSBetreuungMonitoring(new TSBetreuungMonitoring(), data)];
    }

    private parseTSBetreuungMonitoring(
        betreuungMonitoring: TSBetreuungMonitoring,
        betreuungMonitoringFromServer: any
    ): TSBetreuungMonitoring {
        this.parseAbstractEntity(betreuungMonitoring, betreuungMonitoringFromServer);
        betreuungMonitoring.refNummer = betreuungMonitoringFromServer.refNummer;
        betreuungMonitoring.benutzer = betreuungMonitoringFromServer.benutzer;
        betreuungMonitoring.infoText = betreuungMonitoringFromServer.infoText;
        betreuungMonitoring.timestamp = DateUtil.localDateTimeToMoment(betreuungMonitoringFromServer.timestamp);
        return betreuungMonitoring;
    }

    public parseLatsHistoryList(data: Array<any>): TSLastenausgleichTagesschulenStatusHistory[] {
        if (!data) {
            return [];
        }
        return Array.isArray(data)
            ? data.map(item => this.parseLatsHistory(new TSLastenausgleichTagesschulenStatusHistory(), item))
            : [this.parseLatsHistory(new TSLastenausgleichTagesschulenStatusHistory(), data)];
    }

    public parseLatsHistory(
        historyTS: TSLastenausgleichTagesschulenStatusHistory,
        historyFromServer: any
    ): TSLastenausgleichTagesschulenStatusHistory {
        this.parseAbstractEntity(historyTS, historyFromServer);
        historyTS.containerId = historyFromServer.containerId;
        historyTS.benutzer = this.parseUser(new TSBenutzer(), historyFromServer.benutzer);
        historyTS.timestampVon = DateUtil.localDateTimeToMoment(historyFromServer.timestampVon);
        historyTS.timestampBis = DateUtil.localDateTimeToMoment(historyFromServer.timestampBis);
        historyTS.status = historyFromServer.status;
        return historyTS;
    }

    public internePendenzToRestObject(internePendenzRest: any, internePendenz: TSInternePendenz): any {
        if (!internePendenz) {
            return undefined;
        }
        this.abstractEntityToRestObject(internePendenzRest, internePendenz);
        internePendenzRest.gesuch = this.gesuchToRestObject({}, internePendenz.gesuch);
        internePendenzRest.termin = DateUtil.momentToLocalDate(internePendenz.termin);
        internePendenzRest.text = internePendenz.text;
        internePendenzRest.erledigt = internePendenz.erledigt;
        return internePendenzRest;
    }

    public parseInternePendenz(internePendenz: TSInternePendenz, internePendentFromServer: any): TSInternePendenz {
        if (!internePendentFromServer) {
            return undefined;
        }
        this.parseAbstractEntity(internePendenz, internePendentFromServer);
        internePendenz.gesuch = this.parseGesuch(new TSGesuch(), internePendentFromServer.gesuch);
        internePendenz.termin = DateUtil.localDateToMoment(internePendentFromServer.termin);
        internePendenz.text = internePendentFromServer.text;
        internePendenz.erledigt = internePendentFromServer.erledigt;
        return internePendenz;
    }

    public gemeindeKennzahlenToRestObject(gemeindeKennzahlenRest: any, gemeindeKennzahlen: TSGemeindeKennzahlen): any {
        if (!gemeindeKennzahlen) {
            return undefined;
        }
        this.abstractEntityToRestObject(gemeindeKennzahlenRest, gemeindeKennzahlen);

        gemeindeKennzahlenRest.gemeinde = this.gemeindeToRestObject({}, gemeindeKennzahlen.gemeinde);
        gemeindeKennzahlenRest.gesuchsperiode = this.gesuchsperiodeToRestObject({}, gemeindeKennzahlen.gesuchsperiode);

        gemeindeKennzahlenRest.nachfrageErfuellt = gemeindeKennzahlen.nachfrageErfuellt;
        gemeindeKennzahlenRest.gemeindeKontingentiert = gemeindeKennzahlen.gemeindeKontingentiert;
        gemeindeKennzahlenRest.nachfrageAnzahl = gemeindeKennzahlen.nachfrageAnzahl;
        gemeindeKennzahlenRest.nachfrageDauer = gemeindeKennzahlen.nachfrageDauer;
        gemeindeKennzahlenRest.limitierungTfo = gemeindeKennzahlen.limitierungTfo;

        return gemeindeKennzahlenRest;
    }

    public parseGemeindeKennzahlen(
        gemeindeKennzahlen: TSGemeindeKennzahlen,
        gemeindeKennzahlenFromServer: any
    ): TSGemeindeKennzahlen {
        if (!gemeindeKennzahlenFromServer) {
            return undefined;
        }
        this.parseAbstractEntity(gemeindeKennzahlen, gemeindeKennzahlenFromServer);

        gemeindeKennzahlen.gemeinde = this.parseGemeinde(new TSGemeinde(), gemeindeKennzahlenFromServer.gemeinde);
        gemeindeKennzahlen.gesuchsperiode =
            this.parseGesuchsperiode(new TSGesuchsperiode(), gemeindeKennzahlenFromServer.gesuchsperiode);
        gemeindeKennzahlen.status = gemeindeKennzahlenFromServer.status;

        gemeindeKennzahlen.nachfrageErfuellt = gemeindeKennzahlenFromServer.nachfrageErfuellt;
        gemeindeKennzahlen.gemeindeKontingentiert = gemeindeKennzahlenFromServer.gemeindeKontingentiert;
        gemeindeKennzahlen.nachfrageAnzahl = gemeindeKennzahlenFromServer.nachfrageAnzahl;
        gemeindeKennzahlen.nachfrageDauer = gemeindeKennzahlenFromServer.nachfrageDauer;
        gemeindeKennzahlen.limitierungTfo = gemeindeKennzahlenFromServer.limitierungTfo;

        return gemeindeKennzahlen;
    }

    public parseFinanzielleSituationTyp(typ: any): TSFinanzielleSituationTyp {
        if (Object.values(TSFinanzielleSituationTyp).includes(typ)) {
            return typ as TSFinanzielleSituationTyp;
        }
        throw new Error(`FinanzielleSituationTyp ${typ} not defined`);
    }

    public parseAnspruchBeschaeftigungAbhaengigkeitTyp(typ: any): TSAnspruchBeschaeftigungAbhaengigkeitTyp {
        if (Object.values(TSAnspruchBeschaeftigungAbhaengigkeitTyp).includes(typ.value)) {
            return typ.value as TSAnspruchBeschaeftigungAbhaengigkeitTyp;
        }
        throw new Error(`TSAnspruchBeschaeftigungAbhaengigkeitTyp ${typ} not defined`);
    }

    public parsePensumAnzeigeTyp(typ: any): TSPensumAnzeigeTyp {
        if (Object.values(TSPensumAnzeigeTyp).includes(typ.value)) {
            return typ.value as TSPensumAnzeigeTyp;
        }
        throw new Error(`TSPensumAnzeigeTyp ${typ.value} not defined`);
    }

    public parseKinderabzugTyp(typ: any): TSKinderabzugTyp {
        if (Object.values(TSKinderabzugTyp).includes(typ)) {
            return typ as TSKinderabzugTyp;
        }
        throw new Error(`TSKinderabzugTyp ${typ} not defined`);
    }

    public parseFachstellenTyp(typ: any): TSFachstellenTyp {
        if (Object.values(TSFachstellenTyp).includes(typ)) {
            return typ as TSFachstellenTyp;
        }
        throw new Error(`TSFachstellenTyp ${typ} not defined`);
    }

    public parseAusserordentlicherAnspruchTyp(typ: any): TSAusserordentlicherAnspruchTyp {
        if (Object.values(TSAusserordentlicherAnspruchTyp).includes(typ)) {
            return typ as TSAusserordentlicherAnspruchTyp;
        }
        throw new Error(`TSAusserordentlicherAnspruchTyp ${typ} not defined`);
    }

    public parseEinschulungTyp(typ: any): TSEinschulungTyp {
        if (Object.values(TSEinschulungTyp).includes(typ)) {
            return typ as TSEinschulungTyp;
        }
        throw new Error(`TSEinschulungTyp ${typ} not defined`);
    }

    public parseSteuerdatenResponse(
        tsSteuerdatenResponse: TSSteuerdatenResponse,
        steuerdatenResponseFromServer: any
    ): TSSteuerdatenResponse {
        tsSteuerdatenResponse.zpvNrAntragsteller = steuerdatenResponseFromServer.zpvNrAntragsteller;
        tsSteuerdatenResponse.geburtsdatumAntragsteller =
            DateUtil.localDateTimeToMoment(steuerdatenResponseFromServer.geburtsdatumAntragsteller);
        tsSteuerdatenResponse.kiBonAntragID = steuerdatenResponseFromServer.kiBonAntragID;
        tsSteuerdatenResponse.beginnGesuchsperiode = steuerdatenResponseFromServer.beginnGesuchsperiode;
        tsSteuerdatenResponse.zpvNrDossiertraeger = steuerdatenResponseFromServer.zpvNrDossiertraeger;
        tsSteuerdatenResponse.geburtsdatumDossiertraeger =
            DateUtil.localDateTimeToMoment(steuerdatenResponseFromServer.geburtsdatumDossiertraeger);
        tsSteuerdatenResponse.zpvNrPartner = steuerdatenResponseFromServer.zpvNrPartner;
        tsSteuerdatenResponse.geburtsdatumPartner =
            DateUtil.localDateTimeToMoment(steuerdatenResponseFromServer.geburtsdatumPartner);
        tsSteuerdatenResponse.fallId = steuerdatenResponseFromServer.fallId;
        tsSteuerdatenResponse.antwortdatum = DateUtil.localDateTimeToMoment(steuerdatenResponseFromServer.antwortdatum);
        tsSteuerdatenResponse.synchroneAntwort = steuerdatenResponseFromServer.synchroneAntwort;
        tsSteuerdatenResponse.veranlagungsstand = steuerdatenResponseFromServer.veranlagungsstand;
        tsSteuerdatenResponse.unterjaehrigerFall = steuerdatenResponseFromServer.unterjaehrigerFall;
        tsSteuerdatenResponse.erwerbseinkommenUnselbstaendigkeitDossiertraeger =
            steuerdatenResponseFromServer.erwerbseinkommenUnselbstaendigkeitDossiertraeger;
        tsSteuerdatenResponse.erwerbseinkommenUnselbstaendigkeitPartner =
            steuerdatenResponseFromServer.erwerbseinkommenUnselbstaendigkeitPartner;
        tsSteuerdatenResponse.steuerpflichtigesErsatzeinkommenDossiertraeger =
            steuerdatenResponseFromServer.steuerpflichtigesErsatzeinkommenDossiertraeger;
        tsSteuerdatenResponse.steuerpflichtigesErsatzeinkommenPartner =
            steuerdatenResponseFromServer.steuerpflichtigesErsatzeinkommenPartner;
        tsSteuerdatenResponse.erhalteneUnterhaltsbeitraegeDossiertraeger =
            steuerdatenResponseFromServer.erhalteneUnterhaltsbeitraegeDossiertraeger;
        tsSteuerdatenResponse.erhalteneUnterhaltsbeitraegePartner =
            steuerdatenResponseFromServer.erhalteneUnterhaltsbeitraegePartner;
        tsSteuerdatenResponse.ausgewiesenerGeschaeftsertragDossiertraeger =
            steuerdatenResponseFromServer.ausgewiesenerGeschaeftsertragDossiertraeger;
        tsSteuerdatenResponse.ausgewiesenerGeschaeftsertragPartner =
            steuerdatenResponseFromServer.ausgewiesenerGeschaeftsertragPartner;
        tsSteuerdatenResponse.ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger =
            steuerdatenResponseFromServer.ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger;
        tsSteuerdatenResponse.ausgewiesenerGeschaeftsertragVorperiodePartner =
            steuerdatenResponseFromServer.ausgewiesenerGeschaeftsertragVorperiodePartner;
        tsSteuerdatenResponse.ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger =
            steuerdatenResponseFromServer.ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger;
        tsSteuerdatenResponse.ausgewiesenerGeschaeftsertragVorperiode2Partner =
            steuerdatenResponseFromServer.ausgewiesenerGeschaeftsertragVorperiode2Partner;
        tsSteuerdatenResponse.weitereSteuerbareEinkuenfteDossiertraeger =
            steuerdatenResponseFromServer.weitereSteuerbareEinkuenfteDossiertraeger;
        tsSteuerdatenResponse.weitereSteuerbareEinkuenftePartner =
            steuerdatenResponseFromServer.weitereSteuerbareEinkuenftePartner;
        tsSteuerdatenResponse.bruttoertraegeAusLiegenschaften =
            steuerdatenResponseFromServer.bruttoertraegeAusLiegenschaften;
        tsSteuerdatenResponse.bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme =
            steuerdatenResponseFromServer.bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme;
        tsSteuerdatenResponse.nettoertraegeAusEgmeDossiertraeger =
            steuerdatenResponseFromServer.nettoertraegeAusEgmeDossiertraeger;
        tsSteuerdatenResponse.nettoertraegeAusEgmePartner = steuerdatenResponseFromServer.nettoertraegeAusEgmePartner;
        tsSteuerdatenResponse.geleisteteUnterhaltsbeitraege =
            steuerdatenResponseFromServer.geleisteteUnterhaltsbeitraege;
        tsSteuerdatenResponse.schuldzinsen = steuerdatenResponseFromServer.schuldzinsen;
        tsSteuerdatenResponse.gewinnungskostenBeweglichesVermoegen =
            steuerdatenResponseFromServer.gewinnungskostenBeweglichesVermoegen;
        tsSteuerdatenResponse.liegenschaftsAbzuege = steuerdatenResponseFromServer.liegenschaftsAbzuege;
        tsSteuerdatenResponse.nettovermoegen = steuerdatenResponseFromServer.nettovermoegen;
        return tsSteuerdatenResponse;
    }

    public kibonAnfrageToRestObject(restKibonAnfrage: any, kibonAnfrage: TSKibonAnfrage): any {
        restKibonAnfrage.antragId = kibonAnfrage.antragId;
        restKibonAnfrage.geburtsdatum = DateUtil.momentToLocalDate(kibonAnfrage.geburtsdatum);
        restKibonAnfrage.gesuchsperiodeBeginnJahr = kibonAnfrage.gesuchsperiodeBeginnJahr;
        restKibonAnfrage.zpvNummer = kibonAnfrage.zpvNummer;
        return restKibonAnfrage;
    }

    public aufteilungDTOToRestObject(aufteilung: TSFinanzielleSituationAufteilungDTO): any {
        const restObj: any = {};
        restObj.bruttoertraegeVermoegenGS1 = aufteilung.bruttoertraegeVermoegen.gs1;
        restObj.abzugSchuldzinsenGS1 = aufteilung.abzugSchuldzinsen.gs1;
        restObj.gewinnungskostenGS1 = aufteilung.gewinnungskosten.gs1;
        restObj.geleisteteAlimenteGS1 = aufteilung.geleisteteAlimente.gs1;
        restObj.nettovermoegenGS1 = aufteilung.nettovermoegen.gs1;
        restObj.nettoertraegeErbengemeinschaftGS1 = aufteilung.nettoertraegeErbengemeinschaft.gs1;
        restObj.bruttoertraegeVermoegenGS2 = aufteilung.bruttoertraegeVermoegen.gs2;
        restObj.abzugSchuldzinsenGS2 = aufteilung.abzugSchuldzinsen.gs2;
        restObj.gewinnungskostenGS2 = aufteilung.gewinnungskosten.gs2;
        restObj.geleisteteAlimenteGS2 = aufteilung.geleisteteAlimente.gs2;
        restObj.nettovermoegenGS2 = aufteilung.nettovermoegen.gs2;
        restObj.nettoertraegeErbengemeinschaftGS2 = aufteilung.nettoertraegeErbengemeinschaft.gs2;
        return restObj;
    }

    public benutzerTableFilterDTOToRestObject(dto: TSBenutzerTableFilterDTO): any {
        return {
            pagination: dto.pagination.toPaginationDTO(),
            search: {
                predicateObject: this.benutzerListFilterToRestObject(dto.search)
            },
            sort: {
                predicate: dto.sort.active,
                reverse: dto.sort.direction === 'asc'
            }
        };
    }

    private benutzerListFilterToRestObject(filter: BenutzerListFilter): any {
        return {
            username: filter.username,
            vorname: filter.vorname,
            nachname: filter.nachname,
            email: filter.email,
            role: filter.role,
            roleGueltigAb: filter.roleGueltigAb,
            roleGueltigBis: filter.roleGueltigBis,
            gemeinde: filter.gemeinde,
            institution: filter.institution,
            traegerschaft: filter.traegerschaft,
            sozialdienst: filter.sozialdienst,
            status: filter.status
        };
    }
}
