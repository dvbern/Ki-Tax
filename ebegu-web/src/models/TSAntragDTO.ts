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

import {TSBetreuungsangebotTyp} from './enums/TSBetreuungsangebotTyp';
import {TSAntragTyp} from './enums/TSAntragTyp';
import {TSAntragStatus} from './enums/TSAntragStatus';
import {TSEingangsart} from './enums/TSEingangsart';
import * as moment from 'moment';
import {TSGesuchBetreuungenStatus} from './enums/TSGesuchBetreuungenStatus';
import TSAbstractAntragDTO from './TSAbstractAntragDTO';

export default class TSAntragDTO extends TSAbstractAntragDTO {
    private static readonly YEAR_2000 = 2000;

    private _antragId: string;
    private _antragTyp: TSAntragTyp;
    private _eingangsart: TSEingangsart;
    private _eingangsdatum: moment.Moment;
    private _regelnGueltigAb: moment.Moment;
    private _eingangsdatumSTV: moment.Moment;
    private _aenderungsdatum: moment.Moment;
    private _verantwortlicherBG: string;
    private _verantwortlicherTS: string;
    private _verantwortlicherUsernameBG: string;
    private _verantwortlicherUsernameTS: string;
    private _besitzerUsername: string;
    private _angebote: Array<TSBetreuungsangebotTyp>;
    private _institutionen: Array<string>;
    private _kinder: Array<string>;
    private _status: TSAntragStatus;
    private _gesuchsperiodeGueltigAb: moment.Moment;
    private _gesuchsperiodeGueltigBis: moment.Moment;
    private _verfuegt: boolean;
    private _beschwerdeHaengig: boolean;
    private _laufnummer: number;
    private _gesuchBetreuungenStatus: TSGesuchBetreuungenStatus;
    private _dokumenteHochgeladen: boolean;
    private _gemeinde: string;

    public constructor(antragId?: string,
                       fallNummer?: number,
                       familienName?: string,
                       antragTyp?: TSAntragTyp,
                       eingangsdatum?: moment.Moment,
                       eingangsdatumSTV?: moment.Moment,
                       aenderungsdatum?: moment.Moment,
                       angebote?: Array<TSBetreuungsangebotTyp>,
                       institutionen?: Array<string>,
                       verantwortlicherBG?: string,
                       verantwortlicherTS?: string,
                       status?: TSAntragStatus,
                       gesuchsperiodeGueltigAb?: moment.Moment,
                       gesuchsperiodeGueltigBis?: moment.Moment,
                       verfuegt?: boolean,
                       laufnummer?: number,
                       besitzerUsername?: string,
                       eingangsart?: TSEingangsart,
                       beschwerdeHaengig?: boolean,
                       kinder?: Array<string>,
                       gesuchBetreuungenStatus?: TSGesuchBetreuungenStatus,
                       dokumenteHochgeladen?: boolean,
                       verantwortlicherUsernameBG?: string,
                       verantwortlicherUsernameTS?: string,
                       dossierId?: string,
                       gemeinde?: string) {

        super(fallNummer, dossierId, familienName);
        this._antragId = antragId;
        this._antragTyp = antragTyp;
        this._eingangsdatum = eingangsdatum;
        this._eingangsdatumSTV = eingangsdatumSTV;
        this._aenderungsdatum = aenderungsdatum;
        this._angebote = angebote;
        this._institutionen = institutionen;
        this._verantwortlicherBG = verantwortlicherBG;
        this._verantwortlicherTS = verantwortlicherTS;
        this._status = status;
        this._gesuchsperiodeGueltigAb = gesuchsperiodeGueltigAb;
        this._gesuchsperiodeGueltigBis = gesuchsperiodeGueltigBis;
        this._verfuegt = verfuegt;
        this._laufnummer = laufnummer;
        this._besitzerUsername = besitzerUsername;
        this._eingangsart = eingangsart;
        this._beschwerdeHaengig = beschwerdeHaengig;
        this._kinder = kinder;
        this._gesuchBetreuungenStatus = gesuchBetreuungenStatus;
        this._dokumenteHochgeladen = dokumenteHochgeladen;
        this._verantwortlicherUsernameBG = verantwortlicherUsernameBG;
        this._verantwortlicherUsernameTS = verantwortlicherUsernameTS;
        this._gemeinde = gemeinde;
        this._regelnGueltigAb = undefined;
    }

    public get antragId(): string {
        return this._antragId;
    }

    public set antragId(value: string) {
        this._antragId = value;
    }

    public get antragTyp(): TSAntragTyp {
        return this._antragTyp;
    }

    public set antragTyp(value: TSAntragTyp) {
        this._antragTyp = value;
    }

    public get eingangsdatum(): moment.Moment {
        return this._eingangsdatum;
    }

    public set eingangsdatum(value: moment.Moment) {
        this._eingangsdatum = value;
    }

    public get regelnGueltigAb(): moment.Moment {
        return this._regelnGueltigAb;
    }

    public set regelnGueltigAb(value: moment.Moment) {
        this._regelnGueltigAb = value;
    }

    public get eingangsdatumSTV(): moment.Moment {
        return this._eingangsdatumSTV;
    }

    public set eingangsdatumSTV(value: moment.Moment) {
        this._eingangsdatumSTV = value;
    }

    public get aenderungsdatum(): moment.Moment {
        return this._aenderungsdatum;
    }

    public set aenderungsdatum(value: moment.Moment) {
        this._aenderungsdatum = value;
    }

    public get angebote(): Array<TSBetreuungsangebotTyp> {
        return this._angebote;
    }

    public set angebote(value: Array<TSBetreuungsangebotTyp>) {
        this._angebote = value;
    }

    public get institutionen(): Array<string> {
        return this._institutionen;
    }

    public set institutionen(value: Array<string>) {
        this._institutionen = value;
    }

    public get verantwortlicherBG(): string {
        return this._verantwortlicherBG;
    }

    public set verantwortlicherBG(value: string) {
        this._verantwortlicherBG = value;
    }

    public get verantwortlicherTS(): string {
        return this._verantwortlicherTS;
    }

    public set verantwortlicherTS(value: string) {
        this._verantwortlicherTS = value;
    }

    public get verantwortlicherUsernameBG(): string {
        return this._verantwortlicherUsernameBG;
    }

    public set verantwortlicherUsernameBG(value: string) {
        this._verantwortlicherUsernameBG = value;
    }

    public get verantwortlicherUsernameTS(): string {
        return this._verantwortlicherUsernameTS;
    }

    public set verantwortlicherUsernameTS(value: string) {
        this._verantwortlicherUsernameTS = value;
    }

    public get status(): TSAntragStatus {
        return this._status;
    }

    public set status(value: TSAntragStatus) {
        this._status = value;
    }

    public get gesuchsperiodeGueltigAb(): moment.Moment {
        return this._gesuchsperiodeGueltigAb;
    }

    public set gesuchsperiodeGueltigAb(value: moment.Moment) {
        this._gesuchsperiodeGueltigAb = value;
    }

    public get gesuchsperiodeGueltigBis(): moment.Moment {
        return this._gesuchsperiodeGueltigBis;
    }

    public set gesuchsperiodeGueltigBis(value: moment.Moment) {
        this._gesuchsperiodeGueltigBis = value;
    }

    public get verfuegt(): boolean {
        return this._verfuegt;
    }

    public set verfuegt(value: boolean) {
        this._verfuegt = value;
    }

    public get laufnummer(): number {
        return this._laufnummer;
    }

    public set laufnummer(value: number) {
        this._laufnummer = value;
    }

    public get gesuchsperiodeString(): string {
        if (this._gesuchsperiodeGueltigAb && this._gesuchsperiodeGueltigBis) {
            return this._gesuchsperiodeGueltigAb.year() + '/'
                + (this._gesuchsperiodeGueltigBis.year() - TSAntragDTO.YEAR_2000);
        }
        return undefined;
    }

    public get eingangsart(): TSEingangsart {
        return this._eingangsart;
    }

    public set eingangsart(value: TSEingangsart) {
        this._eingangsart = value;
    }

    public get besitzerUsername(): string {
        return this._besitzerUsername;
    }

    public set besitzerUsername(value: string) {
        this._besitzerUsername = value;
    }

    public hasBesitzer(): boolean {
        return this._besitzerUsername !== undefined && this.besitzerUsername !== null;
    }

    public get beschwerdeHaengig(): boolean {
        return this._beschwerdeHaengig;
    }

    public set beschwerdeHaengig(value: boolean) {
        this._beschwerdeHaengig = value;
    }

    public get kinder(): Array<string> {
        return this._kinder;
    }

    public set kinder(value: Array<string>) {
        this._kinder = value;
    }

    public get dokumenteHochgeladen(): boolean {
        return this._dokumenteHochgeladen;
    }

    public set dokumenteHochgeladen(value: boolean) {
        this._dokumenteHochgeladen = value;
    }

    public get gemeinde(): string {
        return this._gemeinde;
    }

    public set gemeinde(value: string) {
        this._gemeinde = value;
    }

    public canBeFreigegeben(): boolean {
        return this.status === TSAntragStatus.FREIGABEQUITTUNG;
    }

    public hasAnySchulamtAngebot(): boolean {
        for (const angebot of this.angebote) {
            if (TSBetreuungsangebotTyp.TAGESSCHULE === angebot || TSBetreuungsangebotTyp.FERIENINSEL === angebot) {
                return true;
            }
        }
        return false;
    }

    public hasOnlyFerieninsel(): boolean {
        for (const angebot of this.angebote) {
            if (TSBetreuungsangebotTyp.FERIENINSEL !== angebot) {
                return false;
            }
        }
        return true;
    }

    public hasAnyJugendamtAngebot(): boolean {
        for (const angebot of this.angebote) {
            if (TSBetreuungsangebotTyp.TAGESSCHULE !== angebot && TSBetreuungsangebotTyp.FERIENINSEL !== angebot) {
                return true;
            }
        }
        return false;
    }

    public get gesuchBetreuungenStatus(): TSGesuchBetreuungenStatus {
        return this._gesuchBetreuungenStatus;
    }

    public set gesuchBetreuungenStatus(value: TSGesuchBetreuungenStatus) {
        this._gesuchBetreuungenStatus = value;
    }
}
