/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import {TSAbstractEntity} from './TSAbstractEntity';
import {TSEinstellungenFerieninsel} from './TSEinstellungenFerieninsel';
import {TSGemeinde} from './TSGemeinde';

export class TSInstitutionStammdatenFerieninsel extends TSAbstractEntity {
    private _gemeinde: TSGemeinde;
    private _einstellungenFerieninsel: Array<TSEinstellungenFerieninsel>;

    public constructor() {
        super();
    }

    public get gemeinde(): TSGemeinde {
        return this._gemeinde;
    }

    public set gemeinde(value: TSGemeinde) {
        this._gemeinde = value;
    }

    public get einstellungenFerieninsel(): Array<TSEinstellungenFerieninsel> {
        return this._einstellungenFerieninsel;
    }

    public set einstellungenFerieninsel(
        value: Array<TSEinstellungenFerieninsel>
    ) {
        this._einstellungenFerieninsel = value;
    }
}
