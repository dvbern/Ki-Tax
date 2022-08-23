/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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
export class BenutzerListFilter {
    private _username: string;
    private _vorname: string;
    private _nachname: string;
    private _email: string;
    private _role: string;
    private _roleGueltigAb: string;
    private _roleGueltigBis: string;
    private _gemeinde: string;
    private _institution: string;
    private _traegerschaft: string;
    private _sozialdienst: string;
    private _status: string;

    public get username(): string {
        return this._username;
    }

    public set username(value: string) {
        this._username = value;
    }

    public get vorname(): string {
        return this._vorname;
    }

    public set vorname(value: string) {
        this._vorname = value;
    }

    public get nachname(): string {
        return this._nachname;
    }

    public set nachname(value: string) {
        this._nachname = value;
    }

    public get email(): string {
        return this._email;
    }

    public set email(value: string) {
        this._email = value;
    }

    public get role(): string {
        return this._role;
    }

    public set role(value: string) {
        this._role = value;
    }

    public get roleGueltigAb(): string {
        return this._roleGueltigAb;
    }

    public set roleGueltigAb(value: string) {
        this._roleGueltigAb = value;
    }

    public get roleGueltigBis(): string {
        return this._roleGueltigBis;
    }

    public set roleGueltigBis(value: string) {
        this._roleGueltigBis = value;
    }

    public get gemeinde(): string {
        return this._gemeinde;
    }

    public set gemeinde(value: string) {
        this._gemeinde = value;
    }

    public get institution(): string {
        return this._institution;
    }

    public set institution(value: string) {
        this._institution = value;
    }

    public get traegerschaft(): string {
        return this._traegerschaft;
    }

    public set traegerschaft(value: string) {
        this._traegerschaft = value;
    }

    public get sozialdienst(): string {
        return this._sozialdienst;
    }

    public set sozialdienst(value: string) {
        this._sozialdienst = value;
    }

    public get status(): string {
        return this._status;
    }

    public set status(value: string) {
        this._status = value;
    }
}
