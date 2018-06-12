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

import TSAbstractAntragDTO from './TSAbstractAntragDTO';

export default class TSFallAntragDTO extends TSAbstractAntragDTO {

    private _fallID: string;
    private _dossierId: string;

    public static readonly serverClassName = 'JaxFallAntragDTO';

    constructor(fallID?: string, dossierId?: string, fallNummer?: number, familienName?: string) {
        super(fallNummer, familienName);
        this._fallID = fallID;
        this._dossierId = dossierId;
    }

    public get fallID(): string {
        return this._fallID;
    }

    public set fallID(fallID: string) {
        this._fallID = fallID;
    }

    public get dossierId(): string {
        return this._dossierId;
    }

    public set dossierId(value: string) {
        this._dossierId = value;
    }
}
