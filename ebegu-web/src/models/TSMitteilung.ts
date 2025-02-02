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
import {TSMitteilungStatus} from './enums/TSMitteilungStatus';
import {TSMitteilungTeilnehmerTyp} from './enums/TSMitteilungTeilnehmerTyp';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import {TSBenutzer} from './TSBenutzer';
import {TSBetreuung} from './TSBetreuung';
import {TSDossier} from './TSDossier';
import {TSFinanzielleSituation} from './TSFinanzielleSituation';
import {TSInstitution} from './TSInstitution';
import {TSMitteilungTyp} from './enums/TSMitteilungTyp';

export class TSMitteilung extends TSAbstractMutableEntity {
    private _dossier: TSDossier;
    private _betreuung: TSBetreuung;
    private _finanzielleSituation: TSFinanzielleSituation;
    private _senderTyp: TSMitteilungTeilnehmerTyp;
    private _empfaengerTyp: TSMitteilungTeilnehmerTyp;
    private _sender: TSBenutzer;
    private _empfaenger: TSBenutzer;
    private _subject: string;
    private _message: string;
    private _mitteilungStatus: TSMitteilungStatus;
    private _sentDatum: moment.Moment;
    private _institution: TSInstitution;
    private _mitteilungTyp: TSMitteilungTyp;

    public constructor(
        dossier?: TSDossier,
        betreuung?: TSBetreuung,
        finanzielleSituation?: TSFinanzielleSituation,
        senderTyp?: TSMitteilungTeilnehmerTyp,
        empfaengerTyp?: TSMitteilungTeilnehmerTyp,
        sender?: TSBenutzer,
        empfaenger?: TSBenutzer,
        subject?: string,
        message?: string,
        mitteilungStatus?: TSMitteilungStatus,
        sentDatum?: moment.Moment,
        institution?: TSInstitution,
        mitteilungTyp?: TSMitteilungTyp
    ) {
        super();
        this._dossier = dossier;
        this._betreuung = betreuung;
        this._finanzielleSituation = finanzielleSituation;
        this._senderTyp = senderTyp;
        this._empfaengerTyp = empfaengerTyp;
        this._sender = sender;
        this._empfaenger = empfaenger;
        this._subject = subject;
        this._message = message;
        this._mitteilungStatus = mitteilungStatus;
        this._sentDatum = sentDatum;
        this._institution = institution;
        this._mitteilungTyp = mitteilungTyp;
    }

    public get dossier(): TSDossier {
        return this._dossier;
    }

    public set dossier(value: TSDossier) {
        this._dossier = value;
    }

    public get betreuung(): TSBetreuung {
        return this._betreuung;
    }

    public set betreuung(value: TSBetreuung) {
        this._betreuung = value;
    }

    public get finanzielleSituation(): TSFinanzielleSituation {
        return this._finanzielleSituation;
    }

    public set finanzielleSituation(value: TSFinanzielleSituation) {
        this._finanzielleSituation = value;
    }

    public get senderTyp(): TSMitteilungTeilnehmerTyp {
        return this._senderTyp;
    }

    public set senderTyp(value: TSMitteilungTeilnehmerTyp) {
        this._senderTyp = value;
    }

    public get empfaengerTyp(): TSMitteilungTeilnehmerTyp {
        return this._empfaengerTyp;
    }

    public set empfaengerTyp(value: TSMitteilungTeilnehmerTyp) {
        this._empfaengerTyp = value;
    }

    public get sender(): TSBenutzer {
        return this._sender;
    }

    public set sender(value: TSBenutzer) {
        this._sender = value;
    }

    public get empfaenger(): TSBenutzer {
        return this._empfaenger;
    }

    public set empfaenger(value: TSBenutzer) {
        this._empfaenger = value;
    }

    public get subject(): string {
        return this._subject;
    }

    public set subject(value: string) {
        this._subject = value;
    }

    public get message(): string {
        return this._message;
    }

    public set message(value: string) {
        this._message = value;
    }

    public get mitteilungStatus(): TSMitteilungStatus {
        return this._mitteilungStatus;
    }

    public set mitteilungStatus(value: TSMitteilungStatus) {
        this._mitteilungStatus = value;
    }

    public get sentDatum(): moment.Moment {
        return this._sentDatum;
    }

    public set sentDatum(value: moment.Moment) {
        this._sentDatum = value;
    }

    public get verantwortlicher(): string {
        if (this.dossier.getHauptverantwortlicher()) {
            return this.dossier.getHauptverantwortlicher().getFullName();
        }
        return '';
    }

    public get institution(): TSInstitution {
        return this._institution;
    }

    public set institution(value: TSInstitution) {
        this._institution = value;
    }

    public get mitteilungTyp(): TSMitteilungTyp {
        return this._mitteilungTyp;
    }

    public set mitteilungTyp(value: TSMitteilungTyp) {
        this._mitteilungTyp = value;
    }

    public get senderAsString(): string {
        let senderAsString: string;
        if (this.sender.currentBerechtigung.institution) {
            senderAsString = `${this.sender.currentBerechtigung.institution.name}, `;
        } else if (this.sender.currentBerechtigung.traegerschaft) {
            senderAsString = `${this.sender.currentBerechtigung.traegerschaft.name}, `;
        }
        if (senderAsString) {
            return senderAsString + this.sender.getFullName();
        }
        return this.sender.getFullName();
    }

    public isErledigt(): boolean {
        return this.mitteilungStatus === TSMitteilungStatus.ERLEDIGT;
    }

    public isIgnoriert() {
        return this.mitteilungStatus === TSMitteilungStatus.IGNORIERT;
    }

    // Die Neue Veranlagung Mitteilung sind die einzige Mitteilungen im System mit Sender und Empfeanger Typ = JUGENDAMT
    public isNeueVeranlagung(): boolean {
        return (
            this.senderTyp === TSMitteilungTeilnehmerTyp.JUGENDAMT &&
            this.empfaengerTyp === TSMitteilungTeilnehmerTyp.JUGENDAMT
        );
    }
}
