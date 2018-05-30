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

import TSAbstractEntity from './TSAbstractEntity';
import TSFall from './TSFall';
import TSUser from './TSUser';
import EbeguUtil from '../utils/EbeguUtil';

export default class TSDossier extends TSAbstractEntity {

    private _fall: TSFall;
    private _dossierNummer: number;
    private _verantwortlicherBG: TSUser;
    private _verantwortlicherTS: TSUser;


    public get fall(): TSFall {
        return this._fall;
    }

    public set fall(value: TSFall) {
        this._fall = value;
    }

    public get dossierNummer(): number {
        return this._dossierNummer;
    }

    public set dossierNummer(value: number) {
        this._dossierNummer = value;
    }

    public get verantwortlicherBG(): TSUser {
        return this._verantwortlicherBG;
    }

    public set verantwortlicherBG(value: TSUser) {
        this._verantwortlicherBG = value;
    }

    public get verantwortlicherTS(): TSUser {
        return this._verantwortlicherTS;
    }

    public set verantwortlicherTS(value: TSUser) {
        this._verantwortlicherTS = value;
    }

    public getHauptverantwortlicher(): TSUser {
        if (this.verantwortlicherBG) {
            return this.verantwortlicherBG;
        }
        return this.verantwortlicherTS;
    }

    public isHauptverantwortlicherSchulamt(): boolean {
        return this.getHauptverantwortlicher() === this.verantwortlicherTS;
    }
}
