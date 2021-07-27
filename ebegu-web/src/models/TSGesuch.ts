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
import {TSAntragStatus} from './enums/TSAntragStatus';
import {TSAntragTyp} from './enums/TSAntragTyp';
import {
    getSchulamtBetreuungsangebotTypValues, isJugendamt,
    isOfAnyBetreuungsangebotTyp,
    TSBetreuungsangebotTyp,
} from './enums/TSBetreuungsangebotTyp';
import {TSBetreuungsstatus} from './enums/TSBetreuungsstatus';
import {TSEingangsart} from './enums/TSEingangsart';
import {TSFinSitStatus} from './enums/TSFinSitStatus';
import {TSGesuchBetreuungenStatus} from './enums/TSGesuchBetreuungenStatus';
import {TSAbstractAntragEntity} from './TSAbstractAntragEntity';
import {TSEinkommensverschlechterungInfo} from './TSEinkommensverschlechterungInfo';
import {TSEinkommensverschlechterungInfoContainer} from './TSEinkommensverschlechterungInfoContainer';
import {TSFamiliensituation} from './TSFamiliensituation';
import {TSFamiliensituationContainer} from './TSFamiliensituationContainer';
import {TSGesuchstellerContainer} from './TSGesuchstellerContainer';
import {TSKindContainer} from './TSKindContainer';

export class TSGesuch extends TSAbstractAntragEntity {

    private _gesuchsteller1: TSGesuchstellerContainer;
    private _gesuchsteller2: TSGesuchstellerContainer;
    private _kindContainers: Array<TSKindContainer>;
    private _familiensituationContainer: TSFamiliensituationContainer;
    private _einkommensverschlechterungInfoContainer: TSEinkommensverschlechterungInfoContainer;
    private _bemerkungen: string;
    private _bemerkungenSTV: string;
    private _bemerkungenPruefungSTV: string;
    private _laufnummer: number;
    private _geprueftSTV: boolean = false;
    private _verfuegungEingeschrieben: boolean = false;
    private _finSitStatus: TSFinSitStatus;
    private _gesperrtWegenBeschwerde: boolean = false;
    private _datumGewarntNichtFreigegeben: moment.Moment;
    private _datumGewarntFehlendeQuittung: moment.Moment;
    private _gesuchBetreuungenStatus: TSGesuchBetreuungenStatus;
    private _dokumenteHochgeladen: boolean = false;

    private _timestampVerfuegt: moment.Moment;
    private _gueltig: boolean;

    // Wir müssen uns merken, dass dies nicht das originalGesuch ist sondern eine Mutations- oder Erneuerungskopie
    // (Wichtig für laden des Gesuchs bei Navigation)
    private _emptyCopy: boolean = false;

    public get gesuchsteller1(): TSGesuchstellerContainer {
        return this._gesuchsteller1;
    }

    public set gesuchsteller1(value: TSGesuchstellerContainer) {
        this._gesuchsteller1 = value;
    }

    public get gesuchsteller2(): TSGesuchstellerContainer {
        return this._gesuchsteller2;
    }

    public set gesuchsteller2(value: TSGesuchstellerContainer) {
        this._gesuchsteller2 = value;
    }

    public get kindContainers(): Array<TSKindContainer> {
        return this._kindContainers;
    }

    public set kindContainers(value: Array<TSKindContainer>) {
        this._kindContainers = value;
    }

    public get familiensituationContainer(): TSFamiliensituationContainer {
        return this._familiensituationContainer;
    }

    public set familiensituationContainer(value: TSFamiliensituationContainer) {
        this._familiensituationContainer = value;
    }

    public get einkommensverschlechterungInfoContainer(): TSEinkommensverschlechterungInfoContainer {
        return this._einkommensverschlechterungInfoContainer;
    }

    public set einkommensverschlechterungInfoContainer(value: TSEinkommensverschlechterungInfoContainer) {
        this._einkommensverschlechterungInfoContainer = value;
    }

    public get bemerkungen(): string {
        return this._bemerkungen;
    }

    public set bemerkungen(value: string) {
        this._bemerkungen = value;
    }

    public get bemerkungenSTV(): string {
        return this._bemerkungenSTV;
    }

    public set bemerkungenSTV(value: string) {
        this._bemerkungenSTV = value;
    }

    public get bemerkungenPruefungSTV(): string {
        return this._bemerkungenPruefungSTV;
    }

    public set bemerkungenPruefungSTV(value: string) {
        this._bemerkungenPruefungSTV = value;
    }

    public get laufnummer(): number {
        return this._laufnummer;
    }

    public set laufnummer(value: number) {
        this._laufnummer = value;
    }

    public get geprueftSTV(): boolean {
        return this._geprueftSTV;
    }

    public set geprueftSTV(value: boolean) {
        this._geprueftSTV = value;
    }

    public get verfuegungEingeschrieben(): boolean {
        return this._verfuegungEingeschrieben;
    }

    public set verfuegungEingeschrieben(value: boolean) {
        this._verfuegungEingeschrieben = value;
    }

    public get gesperrtWegenBeschwerde(): boolean {
        return this._gesperrtWegenBeschwerde;
    }

    public set gesperrtWegenBeschwerde(value: boolean) {
        this._gesperrtWegenBeschwerde = value;
    }

    public get emptyCopy(): boolean {
        return this._emptyCopy;
    }

    public set emptyCopy(value: boolean) {
        this._emptyCopy = value;
    }

    public get datumGewarntNichtFreigegeben(): moment.Moment {
        return this._datumGewarntNichtFreigegeben;
    }

    public set datumGewarntNichtFreigegeben(value: moment.Moment) {
        this._datumGewarntNichtFreigegeben = value;
    }

    public get datumGewarntFehlendeQuittung(): moment.Moment {
        return this._datumGewarntFehlendeQuittung;
    }

    public set datumGewarntFehlendeQuittung(value: moment.Moment) {
        this._datumGewarntFehlendeQuittung = value;
    }

    public get timestampVerfuegt(): moment.Moment {
        return this._timestampVerfuegt;
    }

    public set timestampVerfuegt(value: moment.Moment) {
        this._timestampVerfuegt = value;
    }

    public get gueltig(): boolean {
        return this._gueltig;
    }

    public set gueltig(value: boolean) {
        this._gueltig = value;
    }

    public get gesuchBetreuungenStatus(): TSGesuchBetreuungenStatus {
        return this._gesuchBetreuungenStatus;
    }

    public set gesuchBetreuungenStatus(value: TSGesuchBetreuungenStatus) {
        this._gesuchBetreuungenStatus = value;
    }

    public get dokumenteHochgeladen(): boolean {
        return this._dokumenteHochgeladen;
    }

    public set dokumenteHochgeladen(value: boolean) {
        this._dokumenteHochgeladen = value;
    }

    public isMutation(): boolean {
        return this.typ === TSAntragTyp.MUTATION;
    }

    public isFolgegesuch(): boolean {
        return this.typ === TSAntragTyp.ERNEUERUNGSGESUCH;
    }

    public isOnlineGesuch(): boolean {
        return TSEingangsart.ONLINE === this.eingangsart;
    }

    public get finSitStatus(): TSFinSitStatus {
        return this._finSitStatus;
    }

    public set finSitStatus(value: TSFinSitStatus) {
        this._finSitStatus = value;
    }

    /**
     * Schaut ob der GS1 oder der GS2 mindestens eine umzugsadresse hat
     */
    public isThereAnyUmzug(): boolean {
        if (this.gesuchsteller1 && this.gesuchsteller1.getUmzugAdressen().length > 0) {
            return true;
        }
        return this.gesuchsteller2 && this.gesuchsteller2.getUmzugAdressen().length > 0;
    }

    /**
     * Alle KindContainer in denen das Kind Betreuung benoetigt
     */
    public getKinderWithBetreuungList(): Array<TSKindContainer> {
        const listResult: Array<TSKindContainer> = [];
        if (this.kindContainers) {
            this.kindContainers.forEach(kind => {
                if (kind.kindJA.familienErgaenzendeBetreuung) {
                    listResult.push(kind);
                }
            });
        }
        return listResult;
    }

    /**
     * Returns true when all Betreuungen are of one of the given types.
     * ACHTUNG! Diese Methode gibt auch true zurueck wenn es keine Betreuungen gibt, was nicht immer richtig ist
     */
    private areThereOnlyAngeboteOfType(types: TSBetreuungsangebotTyp[]): boolean {
        const kinderWithBetreuungList = this.getKinderWithBetreuungList();
        if (kinderWithBetreuungList.length <= 0) {
            return false; // no Kind with bedarf
        }
        for (const kind of kinderWithBetreuungList) {
            for (const betreuung of kind.betreuungen) {
                if (betreuung.institutionStammdaten && !isOfAnyBetreuungsangebotTyp(betreuung.institutionStammdaten.betreuungsangebotTyp,
                    types)) {
                    return false;
                }
            }
        }
        return true;
    }

    private areThereNoBetreuungenAtAll(): boolean {
        const kinderWithBetreuungList = this.getKinderWithBetreuungList();
        let noBetreuungAtAll = true;
        for (const kind of kinderWithBetreuungList) {
            if (kind.betreuungen.length > 0) {
                noBetreuungAtAll = false;
            }
        }
        return noBetreuungAtAll;
    }

    /**
     * Returns true when all Betreuungen are of kind SCHULAMT.
     * Returns false also if there are no Kinder with betreuungsbedarf
     */
    public areThereOnlySchulamtAngebote(): boolean {
        return this.areThereOnlyAngeboteOfType(getSchulamtBetreuungsangebotTypValues());
    }

    /**
     * Returns true when all Betreuungen are of kind FERIENINSEL.
     * Returns false also if there are no Kinder with betreuungsbedarf
     */
    public areThereOnlyFerieninsel(): boolean {
        return this.areThereOnlyAngeboteOfType([TSBetreuungsangebotTyp.FERIENINSEL])
            && !this.areThereNoBetreuungenAtAll;
    }

    /**
     * Returns true when all Betreuungen are geschlossen ohne verfuegung
     */
    public areThereOnlyGeschlossenOhneVerfuegung(): boolean {
        const kinderWithBetreuungList = this.getKinderWithBetreuungList();
        if (kinderWithBetreuungList.length <= 0) {
            return false; // no Kind with bedarf
        }
        for (const kind of kinderWithBetreuungList) {
            for (const betreuung of kind.betreuungen) {
                if (betreuung.betreuungsstatus !== TSBetreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG) {
                    return false;
                }
            }
        }
        return true;
    }

    public hasBetreuungInStatusWarten(): boolean {
        return this.checkForBetreuungsstatus(TSBetreuungsstatus.WARTEN);
    }

    public hasProvisorischeBetreuungen(): boolean {
        return this.checkForBetreuungsstatus(TSBetreuungsstatus.UNBEKANNTE_INSTITUTION);
    }

    public hasBerechenbareBetreuungen(): boolean {
        return this.checkForBetreuungsstatus(TSBetreuungsstatus.BESTAETIGT)
            || this.checkForBetreuungsstatus(TSBetreuungsstatus.UNBEKANNTE_INSTITUTION);
    }

    public hasNichtBerechenbareBetreuungen(): boolean {
        return this.checkForBetreuungsstatus(TSBetreuungsstatus.ABGEWIESEN)
            || this.checkForBetreuungsstatus(TSBetreuungsstatus.WARTEN);
    }

    public checkForBetreuungsstatus(status: TSBetreuungsstatus): boolean {
        const kinderWithBetreuungList = this.getKinderWithBetreuungList();
        for (const kind of kinderWithBetreuungList) {
            for (const betreuung of kind.betreuungen) {
                if (betreuung.betreuungsstatus === status) {
                    return true;
                }
            }
        }
        return false;
    }

    public extractFamiliensituationGS(): TSFamiliensituation {
        if (this.familiensituationContainer) {
            return this.familiensituationContainer.familiensituationGS;
        }
        return undefined;
    }

    public extractFamiliensituation(): TSFamiliensituation {
        if (this.familiensituationContainer) {
            return this.familiensituationContainer.familiensituationJA;
        }
        return undefined;
    }

    public extractFamiliensituationErstgesuch(): TSFamiliensituation {
        if (this.familiensituationContainer) {
            return this.familiensituationContainer.familiensituationErstgesuch;
        }
        return undefined;
    }

    public extractEinkommensverschlechterungInfo(): TSEinkommensverschlechterungInfo {
        if (this.einkommensverschlechterungInfoContainer) {
            return this.einkommensverschlechterungInfoContainer.einkommensverschlechterungInfoJA;
        }
        return undefined;
    }

    public canBeFreigegeben(): boolean {
        return this.status === TSAntragStatus.FREIGABEQUITTUNG;
    }

    /**
     * Schaut dass mindestens eine Betreuung erfasst wurde.
     */
    public isThereAnyBetreuung(): boolean {
        const kinderWithBetreuungList = this.getKinderWithBetreuungList();
        for (const kind of kinderWithBetreuungList) {
            if (kind.betreuungen && kind.betreuungen.length > 0) {
                return true;
            }
        }
        return false;
    }

    public extractKindFromKindNumber(kindNumber: number): TSKindContainer | undefined {
        if (this.kindContainers && kindNumber > 0) {
            return this.kindContainers.find(kc => kc.kindNummer === kindNumber);
        }
        return undefined;
    }

    /**
     * Returns true if all kinder have an ausserordentlicher anspruch defined
     */
    public allKindHaveAusserordentlicherAnspruch(): boolean {
        if (this.kindContainers) {
            return this.kindContainers.every(kind => {
                return !!kind.kindJA.pensumAusserordentlicherAnspruch;
            });
        }
        return false;
    }

    /**
     * Gibt true zurueck wenn mindestens ein Kind ein Angebot KITA oder TFO hat
     */
    public hasAnyJugendamtAngebot(): boolean {
        const kinderWithBetreuungList = this.getKinderWithBetreuungList();
        for (const kind of kinderWithBetreuungList) {
            // tslint:disable-next-line:early-exit
            if (kind.betreuungen && kind.betreuungen.length > 0) {
                for (const platz of kind.betreuungen) {
                    if (isJugendamt(platz.getAngebotTyp())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
