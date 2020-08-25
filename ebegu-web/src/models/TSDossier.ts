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

import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import {TSBenutzerNoDetails} from './TSBenutzerNoDetails';
import {TSFall} from './TSFall';
import {TSGemeinde} from './TSGemeinde';

export class TSDossier extends TSAbstractMutableEntity {

    private _fall: TSFall;
    private _gemeinde: TSGemeinde;
    private _verantwortlicherBG: TSBenutzerNoDetails;
    private _verantwortlicherTS: TSBenutzerNoDetails;

    public get fall(): TSFall {
        return this._fall;
    }

    public set fall(value: TSFall) {
        this._fall = value;
    }

    public get gemeinde(): TSGemeinde {
        return this._gemeinde;
    }

    public set gemeinde(value: TSGemeinde) {
        this._gemeinde = value;
    }

    public get verantwortlicherBG(): TSBenutzerNoDetails {
        return this._verantwortlicherBG;
    }

    public set verantwortlicherBG(value: TSBenutzerNoDetails) {
        this._verantwortlicherBG = value;
    }

    public get verantwortlicherTS(): TSBenutzerNoDetails {
        return this._verantwortlicherTS;
    }

    public set verantwortlicherTS(value: TSBenutzerNoDetails) {
        this._verantwortlicherTS = value;
    }

    public getHauptverantwortlicher(): TSBenutzerNoDetails {
        if (this.verantwortlicherBG) {
            return this.verantwortlicherBG;
        }
        return this.verantwortlicherTS;
    }

    public isHauptverantwortlicherTS(): boolean {
        return this.getHauptverantwortlicher() && this.getHauptverantwortlicher() === this.verantwortlicherTS;
    }

    public extractGemeindeName(): string {
        return this.gemeinde ? this.gemeinde.name : '';
    }
}
