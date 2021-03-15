/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import {TSAbstractEntity} from '../TSAbstractEntity';
import {TSAdresse} from '../TSAdresse';
import {TSGemeinde} from '../TSGemeinde';

export class TSFerienbetreuungAngabenStammdaten extends TSAbstractEntity {

    private _amAngebotBeteiligteGemeinden: TSGemeinde[];
    private _seitWannFerienbetreuungen: string;
    private _traegerschaft: string;
    private _stammdatenAdresse: TSAdresse;
    private _stammdatenKontaktpersonVorname: string;
    private _stammdatenKontaktpersonNachname: string;
    private _stammdatenKontaktpersonFunktion: string;
    private _stammdatenKontaktpersonTelefon: string;
    private _stammdatenKontaktpersonEmail: string;
    private _iban: string;
    private _kontoinhaber: string;
    private _adresseKontoinhaber: TSAdresse;
    private _vermerkAuszahlung: string;

    public get amAngebotBeteiligteGemeinden(): TSGemeinde[] {
        return this._amAngebotBeteiligteGemeinden;
    }

    public set amAngebotBeteiligteGemeinden(value: TSGemeinde[]) {
        this._amAngebotBeteiligteGemeinden = value;
    }

    public get seitWannFerienbetreuungen(): string {
        return this._seitWannFerienbetreuungen;
    }

    public set seitWannFerienbetreuungen(value: string) {
        this._seitWannFerienbetreuungen = value;
    }

    public get traegerschaft(): string {
        return this._traegerschaft;
    }

    public set traegerschaft(value: string) {
        this._traegerschaft = value;
    }

    public get stammdatenAdresse(): TSAdresse {
        return this._stammdatenAdresse;
    }

    public set stammdatenAdresse(value: TSAdresse) {
        this._stammdatenAdresse = value;
    }

    public get stammdatenKontaktpersonVorname(): string {
        return this._stammdatenKontaktpersonVorname;
    }

    public set stammdatenKontaktpersonVorname(value: string) {
        this._stammdatenKontaktpersonVorname = value;
    }

    public get stammdatenKontaktpersonNachname(): string {
        return this._stammdatenKontaktpersonNachname;
    }

    public set stammdatenKontaktpersonNachname(value: string) {
        this._stammdatenKontaktpersonNachname = value;
    }

    public get stammdatenKontaktpersonFunktion(): string {
        return this._stammdatenKontaktpersonFunktion;
    }

    public set stammdatenKontaktpersonFunktion(value: string) {
        this._stammdatenKontaktpersonFunktion = value;
    }

    public get stammdatenKontaktpersonTelefon(): string {
        return this._stammdatenKontaktpersonTelefon;
    }

    public set stammdatenKontaktpersonTelefon(value: string) {
        this._stammdatenKontaktpersonTelefon = value;
    }

    public get stammdatenKontaktpersonEmail(): string {
        return this._stammdatenKontaktpersonEmail;
    }

    public set stammdatenKontaktpersonEmail(value: string) {
        this._stammdatenKontaktpersonEmail = value;
    }

    public get iban(): string {
        return this._iban;
    }

    public set iban(value: string) {
        this._iban = value;
    }

    public get kontoinhaber(): string {
        return this._kontoinhaber;
    }

    public set kontoinhaber(value: string) {
        this._kontoinhaber = value;
    }

    public get adresseKontoinhaber(): TSAdresse {
        return this._adresseKontoinhaber;
    }

    public set adresseKontoinhaber(value: TSAdresse) {
        this._adresseKontoinhaber = value;
    }

    public get vermerkAuszahlung(): string {
        return this._vermerkAuszahlung;
    }

    public set vermerkAuszahlung(value: string) {
        this._vermerkAuszahlung = value;
    }
}
