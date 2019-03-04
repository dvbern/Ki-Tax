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
import {TSAnmeldungMutationZustand} from './enums/TSAnmeldungMutationZustand';
import {TSBetreuungsangebotTyp} from './enums/TSBetreuungsangebotTyp';
import {isBetreuungsstatusTSAusgeloest, TSBetreuungsstatus} from './enums/TSBetreuungsstatus';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import TSAbwesenheitContainer from './TSAbwesenheitContainer';
import TSBelegungFerieninsel from './TSBelegungFerieninsel';
import TSBelegungTagesschule from './TSBelegungTagesschule';
import TSBetreuungspensumContainer from './TSBetreuungspensumContainer';
import TSGesuchsperiode from './TSGesuchsperiode';
import TSInstitutionStammdaten from './TSInstitutionStammdaten';
import TSVerfuegung from './TSVerfuegung';
import TSErweiterteBetreuungContainer from './TSErweiterteBetreuungContainer';

export default class TSBetreuung extends TSAbstractMutableEntity {

    private _institutionStammdaten: TSInstitutionStammdaten;
    private _betreuungsstatus: TSBetreuungsstatus = TSBetreuungsstatus.AUSSTEHEND;
    private _betreuungspensumContainers: Array<TSBetreuungspensumContainer> = [];
    private _abwesenheitContainers: Array<TSAbwesenheitContainer> = [];
    private _erweiterteBetreuungContainer: TSErweiterteBetreuungContainer;
    private _grundAblehnung: string;
    private _betreuungNummer: number;
    private _verfuegung: TSVerfuegung;
    private _vertrag: boolean;
    private _keineKesbPlatzierung: boolean;
    private _datumAblehnung: moment.Moment;
    private _datumBestaetigung: moment.Moment;
    private _kindFullname: string;
    private _kindNummer: number;
    private _gesuchId: string;
    private _gesuchsperiode: TSGesuchsperiode;
    private _betreuungMutiert: boolean;
    private _abwesenheitMutiert: boolean;
    private _gueltig: boolean;
    private _belegungTagesschule: TSBelegungTagesschule;
    private _belegungFerieninsel: TSBelegungFerieninsel;
    private _anmeldungMutationZustand: TSAnmeldungMutationZustand;
    private _bgNummer: string;
    private _keineDetailinformationen: boolean = false;

    public constructor() {
        super();
    }

    public get institutionStammdaten(): TSInstitutionStammdaten {
        return this._institutionStammdaten;
    }

    public set institutionStammdaten(value: TSInstitutionStammdaten) {
        this._institutionStammdaten = value;
    }

    public get betreuungsstatus(): TSBetreuungsstatus {
        return this._betreuungsstatus;
    }

    public set betreuungsstatus(value: TSBetreuungsstatus) {
        this._betreuungsstatus = value;
    }

    public get betreuungspensumContainers(): Array<TSBetreuungspensumContainer> {
        return this._betreuungspensumContainers;
    }

    public set betreuungspensumContainers(value: Array<TSBetreuungspensumContainer>) {
        this._betreuungspensumContainers = value;
    }

    public get abwesenheitContainers(): Array<TSAbwesenheitContainer> {
        return this._abwesenheitContainers;
    }

    public set abwesenheitContainers(value: Array<TSAbwesenheitContainer>) {
        this._abwesenheitContainers = value;
    }

    public get erweiterteBetreuungContainer(): TSErweiterteBetreuungContainer {
        return this._erweiterteBetreuungContainer;
    }

    public set erweiterteBetreuungContainer(value: TSErweiterteBetreuungContainer) {
        this._erweiterteBetreuungContainer = value;
    }

    public get grundAblehnung(): string {
        return this._grundAblehnung;
    }

    public set grundAblehnung(value: string) {
        this._grundAblehnung = value;
    }

    public get betreuungNummer(): number {
        return this._betreuungNummer;
    }

    public set betreuungNummer(value: number) {
        this._betreuungNummer = value;
    }

    public get verfuegung(): TSVerfuegung {
        return this._verfuegung;
    }

    public set verfuegung(value: TSVerfuegung) {
        this._verfuegung = value;
    }

    public get vertrag(): boolean {
        return this._vertrag;
    }

    public set vertrag(value: boolean) {
        this._vertrag = value;
    }

    public get keineKesbPlatzierung(): boolean {
        return this._keineKesbPlatzierung;
    }

    public set keineKesbPlatzierung(value: boolean) {
        this._keineKesbPlatzierung = value;
    }

    public get datumAblehnung(): moment.Moment {
        return this._datumAblehnung;
    }

    public set datumAblehnung(value: moment.Moment) {
        this._datumAblehnung = value;
    }

    public get datumBestaetigung(): moment.Moment {
        return this._datumBestaetigung;
    }

    public set datumBestaetigung(value: moment.Moment) {
        this._datumBestaetigung = value;
    }

    public get kindFullname(): string {
        return this._kindFullname;
    }

    public set kindFullname(value: string) {
        this._kindFullname = value;
    }

    public get kindNummer(): number {
        return this._kindNummer;
    }

    public set kindNummer(value: number) {
        this._kindNummer = value;
    }

    public get gesuchId(): string {
        return this._gesuchId;
    }

    public set gesuchId(value: string) {
        this._gesuchId = value;
    }

    public get gesuchsperiode(): TSGesuchsperiode {
        return this._gesuchsperiode;
    }

    public set gesuchsperiode(value: TSGesuchsperiode) {
        this._gesuchsperiode = value;
    }

    public get betreuungMutiert(): boolean {
        return this._betreuungMutiert;
    }

    public set betreuungMutiert(value: boolean) {
        this._betreuungMutiert = value;
    }

    public get abwesenheitMutiert(): boolean {
        return this._abwesenheitMutiert;
    }

    public set abwesenheitMutiert(value: boolean) {
        this._abwesenheitMutiert = value;
    }

    public get gueltig(): boolean {
        return this._gueltig;
    }

    public set gueltig(value: boolean) {
        this._gueltig = value;
    }

    public get belegungTagesschule(): TSBelegungTagesschule {
        return this._belegungTagesschule;
    }

    public set belegungTagesschule(value: TSBelegungTagesschule) {
        this._belegungTagesschule = value;
    }

    public get belegungFerieninsel(): TSBelegungFerieninsel {
        return this._belegungFerieninsel;
    }

    public set belegungFerieninsel(value: TSBelegungFerieninsel) {
        this._belegungFerieninsel = value;
    }

    public get keineDetailinformationen(): boolean {
        return this._keineDetailinformationen;
    }

    public set keineDetailinformationen(value: boolean) {
        this._keineDetailinformationen = value;
    }

    public isAngebotKITA(): boolean {
        return this.isAngebot(TSBetreuungsangebotTyp.KITA);
    }

    public isAngebotTagesschule(): boolean {
        return this.isAngebot(TSBetreuungsangebotTyp.TAGESSCHULE);
    }

    public isAngebotFerieninsel(): boolean {
        return this.isAngebot(TSBetreuungsangebotTyp.FERIENINSEL);
    }

    public isAngebotSchulamt(): boolean {
        return this.isAngebotFerieninsel() || this.isAngebotTagesschule();
    }

    public getAngebotTyp(): TSBetreuungsangebotTyp {
        if (this.institutionStammdaten && this.institutionStammdaten.betreuungsangebotTyp) {
            return this.institutionStammdaten.betreuungsangebotTyp;
        }
        return null;
    }

    private isAngebot(typ: TSBetreuungsangebotTyp): boolean {
        if (this.institutionStammdaten && this.institutionStammdaten.betreuungsangebotTyp) {
            return this.institutionStammdaten.betreuungsangebotTyp === typ;
        }

        return false;
    }

    public isEnabled(): boolean {
        return (!this.hasVorgaenger() || this.isAngebotSchulamt())
            && (this.isBetreuungsstatus(TSBetreuungsstatus.AUSSTEHEND)
                || this.isBetreuungsstatus(TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST));
    }

    public isBetreuungsstatus(status: TSBetreuungsstatus): boolean {
        return this.betreuungsstatus === status;
    }

    public isSchulamtangebotAusgeloest(): boolean {
        return this.isAngebotSchulamt() && isBetreuungsstatusTSAusgeloest(this.betreuungsstatus);
    }

    public get anmeldungMutationZustand(): TSAnmeldungMutationZustand {
        return this._anmeldungMutationZustand;
    }

    public set anmeldungMutationZustand(value: TSAnmeldungMutationZustand) {
        this._anmeldungMutationZustand = value;
    }

    public get bgNummer(): string {
        return this._bgNummer;
    }

    public set bgNummer(value: string) {
        this._bgNummer = value;
    }
}
