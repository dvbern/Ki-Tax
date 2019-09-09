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

import {TSDayOfWeek} from './enums/TSDayOfWeek';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';

export default class TSModulTagesschule extends TSAbstractMutableEntity {

    public wochentag: TSDayOfWeek;

    public angemeldet: boolean; // Transient, wird nicht auf Server synchronisiert, bzw. nur die mit angemeldet=true
    public angeboten: boolean;

    /**
     * Prueft ob beide Module gleich sind. Sie sind glech wenn wochentag und modulTagesschuleName gleich sind.
     * Die ZeitVon und ZeitBis spielt keine Rolle in diesem Fall, da so Module unterschiedlichen Institutionen
     * verglichen werden koennen.
     */
    public isSameModul(modulTagesschule: TSModulTagesschule): boolean {
        return modulTagesschule
            && this.id === modulTagesschule.id
            && this.wochentag === modulTagesschule.wochentag;
    }

    public uniqueId(): string {
        return this.id
            + this.wochentag;
    }
}
