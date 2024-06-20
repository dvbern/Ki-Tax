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

import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';

export class TSFinSitZusatzangabenAppenzell extends TSAbstractMutableEntity {
    private _saeule3a: number;
    private _saeule3aNichtBvg: number;
    private _beruflicheVorsorge: number;
    private _liegenschaftsaufwand: number;
    private _einkuenfteBgsa: number;
    private _vorjahresverluste: number;
    private _politischeParteiSpende: number;
    private _leistungAnJuristischePersonen: number;
    private _steuerbaresEinkommen: number;
    private _steuerbaresVermoegen: number;
    private _zusatzangabenPartner: TSFinSitZusatzangabenAppenzell;

    public get saeule3a(): number {
        return this._saeule3a;
    }

    public set saeule3a(value: number) {
        this._saeule3a = value;
    }

    public get saeule3aNichtBvg(): number {
        return this._saeule3aNichtBvg;
    }

    public set saeule3aNichtBvg(value: number) {
        this._saeule3aNichtBvg = value;
    }

    public get beruflicheVorsorge(): number {
        return this._beruflicheVorsorge;
    }

    public set beruflicheVorsorge(value: number) {
        this._beruflicheVorsorge = value;
    }

    public get liegenschaftsaufwand(): number {
        return this._liegenschaftsaufwand;
    }

    public set liegenschaftsaufwand(value: number) {
        this._liegenschaftsaufwand = value;
    }

    public get einkuenfteBgsa(): number {
        return this._einkuenfteBgsa;
    }

    public set einkuenfteBgsa(value: number) {
        this._einkuenfteBgsa = value;
    }

    public get vorjahresverluste(): number {
        return this._vorjahresverluste;
    }

    public set vorjahresverluste(value: number) {
        this._vorjahresverluste = value;
    }

    public get politischeParteiSpende(): number {
        return this._politischeParteiSpende;
    }

    public set politischeParteiSpende(value: number) {
        this._politischeParteiSpende = value;
    }

    public get leistungAnJuristischePersonen(): number {
        return this._leistungAnJuristischePersonen;
    }

    public set leistungAnJuristischePersonen(value: number) {
        this._leistungAnJuristischePersonen = value;
    }

    public get zusatzangabenPartner(): TSFinSitZusatzangabenAppenzell {
        return this._zusatzangabenPartner;
    }

    public set zusatzangabenPartner(value: TSFinSitZusatzangabenAppenzell) {
        this._zusatzangabenPartner = value;
    }

    public get steuerbaresVermoegen(): number {
        return this._steuerbaresVermoegen;
    }

    public set steuerbaresVermoegen(value: number) {
        this._steuerbaresVermoegen = value;
    }
    public get steuerbaresEinkommen(): number {
        return this._steuerbaresEinkommen;
    }

    public set steuerbaresEinkommen(value: number) {
        this._steuerbaresEinkommen = value;
    }
}
