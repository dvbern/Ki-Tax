/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {TSAbstractEntity} from './TSAbstractEntity';
import {TSAdresse} from './TSAdresse';

export class TSZahlungsinformationen extends TSAbstractEntity {

    private _iban: string;
    private _kontoinhaber: string;
    private _abweichendeZahlungsadresse: boolean;
    private _zahlungsadresse: TSAdresse;
    private _keineMahlzeitenverguenstigungBeantragt: boolean;

    public constructor() {
        super();
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

    public get abweichendeZahlungsadresse(): boolean {
        return this._abweichendeZahlungsadresse;
    }

    public set abweichendeZahlungsadresse(value: boolean) {
        this._abweichendeZahlungsadresse = value;
    }

    public get zahlungsadresse(): TSAdresse {
        return this._zahlungsadresse;
    }

    public set zahlungsadresse(value: TSAdresse) {
        this._zahlungsadresse = value;
    }

    public get keineMahlzeitenverguenstigungBeantragt(): boolean {
        return this._keineMahlzeitenverguenstigungBeantragt;
    }

    public set keineMahlzeitenverguenstigungBeantragt(value: boolean) {
        this._keineMahlzeitenverguenstigungBeantragt = value;
    }
}
