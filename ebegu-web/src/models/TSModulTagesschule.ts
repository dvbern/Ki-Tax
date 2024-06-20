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

import {CONSTANTS} from '../app/core/constants/CONSTANTS';
import {EbeguUtil} from '../utils/EbeguUtil';
import {TSDayOfWeek} from './enums/TSDayOfWeek';
import {TSAbstractEntity} from './TSAbstractEntity';
import {TSTextRessource} from './TSTextRessource';

export class TSModulTagesschule extends TSAbstractEntity {
    public wochentag: TSDayOfWeek;

    public angemeldet: boolean; // Transient, wird nicht auf Server synchronisiert, bzw. nur die mit angemeldet=true
    public angeboten: boolean;
    public identifier: string;
    public moduleGroupName: string; // Transient, ist nur ergaenzt als info um die gewaehlte Modulen von eine
    // Scolaris Insti zu eine andere zu wieder finden
    public moduleGroupBezeichnung: TSTextRessource; // wie moduleGroupName

    public constructor() {
        super();
        this.identifier = EbeguUtil.generateRandomName(CONSTANTS.ID_LENGTH);
    }

    public static create(wochentag: TSDayOfWeek): TSModulTagesschule {
        const modul = new TSModulTagesschule();
        modul.wochentag = wochentag;
        modul.angeboten = false;
        modul.angemeldet = false;
        return modul;
    }
}
