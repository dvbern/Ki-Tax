/*
 * AGPL File-Header
 *
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';
import TSAdresse from './TSAdresse';
import TSBenutzer from './TSBenutzer';
import TSGemeinde from './TSGemeinde';
import {TSDateRange} from './types/TSDateRange';

export default class TSGemeindeStammdaten extends TSAbstractDateRangedEntity {
    private _administratoren: string;
    private _sachbearbeiter: string;
    private _defaultBenutzerBG: TSBenutzer;
    private _defaultBenutzerTS: TSBenutzer;
    private _gemeinde: TSGemeinde;
    private _adresse: TSAdresse;
    private _beschwerdeAdresse: TSAdresse;
    private _keineBeschwerdeAdresse: boolean;
    private _mail: string;
    private _telefon: string;
    private _webseite: string;
    private _korrespondenzspracheDe: boolean;
    private _korrespondenzspracheFr: boolean;
    // ---------- Konfiguration ----------
    private _kontingentierung: boolean;
    private _beguBisUndMitSchulstufe: string;

    public constructor(
        gueltigkeit?: TSDateRange,
    ) {
        super(gueltigkeit);
    }

    public get administratoren(): string {
        return this._administratoren;
    }

    public set administratoren(value: string) {
        this._administratoren = value;
    }

    public get sachbearbeiter(): string {
        return this._sachbearbeiter;
    }

    public set sachbearbeiter(value: string) {
        this._sachbearbeiter = value;
    }

    public get defaultBenutzerBG(): TSBenutzer {
        return this._defaultBenutzerBG;
    }

    public set defaultBenutzerBG(value: TSBenutzer) {
        this._defaultBenutzerBG = value;
    }

    public get defaultBenutzerTS(): TSBenutzer {
        return this._defaultBenutzerTS;
    }

    public set defaultBenutzerTS(value: TSBenutzer) {
        this._defaultBenutzerTS = value;
    }

    public get gemeinde(): TSGemeinde {
        return this._gemeinde;
    }

    public set gemeinde(value: TSGemeinde) {
        this._gemeinde = value;
    }

    public get adresse(): TSAdresse {
        return this._adresse;
    }

    public set adresse(value: TSAdresse) {
        this._adresse = value;
    }

    public get beschwerdeAdresse(): TSAdresse {
        return this._beschwerdeAdresse;
    }

    public set beschwerdeAdresse(value: TSAdresse) {
        this._beschwerdeAdresse = value;
    }

    public get keineBeschwerdeAdresse(): boolean {
        return this._keineBeschwerdeAdresse;
    }

    public set keineBeschwerdeAdresse(value: boolean) {
        this._keineBeschwerdeAdresse = value;
    }

    public get mail(): string {
        return this._mail;
    }

    public set mail(value: string) {
        this._mail = value;
    }

    public get telefon(): string {
        return this._telefon;
    }

    public set telefon(value: string) {
        this._telefon = value;
    }

    public get webseite(): string {
        return this._webseite;
    }

    public set webseite(value: string) {
        this._webseite = value;
    }

    public get korrespondenzspracheDe(): boolean {
        return this._korrespondenzspracheDe;
    }

    public set korrespondenzspracheDe(value: boolean) {
        this._korrespondenzspracheDe = value;
    }

    public get korrespondenzspracheFr(): boolean {
        return this._korrespondenzspracheFr;
    }

    public set korrespondenzspracheFr(value: boolean) {
        this._korrespondenzspracheFr = value;
    }

    public get kontingentierung(): boolean {
        return this._kontingentierung;
    }

    public set kontingentierung(value: boolean) {
        this._kontingentierung = value;
    }

    public get beguBisUndMitSchulstufe(): string {
        return this._beguBisUndMitSchulstufe;
    }

    public set beguBisUndMitSchulstufe(value: string) {
        this._beguBisUndMitSchulstufe = value;
    }

}
