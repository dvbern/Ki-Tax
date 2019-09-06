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

import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';
import TSGemeinde from './TSGemeinde';
import TSModulTagesschule from './TSModulTagesschule';
import TSModulTagesschuleGroup from './TSModulTagesschuleGroup';

export default class TSInstitutionStammdatenTagesschule extends TSAbstractDateRangedEntity {

    private _gemeinde: TSGemeinde;
    private _modulTagesschuleGroups: Array<TSModulTagesschuleGroup>;

    public constructor(moduleTagesschule?: Array<TSModulTagesschuleGroup>) {
        super();
        this._modulTagesschuleGroups = moduleTagesschule;
    }

    public get gemeinde(): TSGemeinde {
        return this._gemeinde;
    }

    public set gemeinde(value: TSGemeinde) {
        this._gemeinde = value;
    }

    public get modulTagesschuleGroups(): Array<TSModulTagesschuleGroup> {
        return this._modulTagesschuleGroups;
    }

    public set modulTagesschuleGroups(value: Array<TSModulTagesschuleGroup>) {
        this._modulTagesschuleGroups = value;
    }

    public getAllModulTagesschule(): Array<TSModulTagesschule> {
        const allModule: TSModulTagesschule[] = [];
        for (const modulTagesschuleGroup of this.modulTagesschuleGroups) {
            for (const modul of modulTagesschuleGroup.module) {
                allModule.push(modul);
            }
        }
        return allModule;
    }
}
