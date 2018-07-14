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

import * as moment from 'moment';
import {TSAntragStatus} from './enums/TSAntragStatus';
import {TSAntragTyp} from './enums/TSAntragTyp';
import {TSEingangsart} from './enums/TSEingangsart';
import TSAbstractEntity from './TSAbstractEntity';
import TSDossier from './TSDossier';
import TSGesuchsperiode from './TSGesuchsperiode';

export default class TSAbstractAntragEntity extends TSAbstractEntity {

    private _dossier: TSDossier;
    private _gesuchsperiode: TSGesuchsperiode;
    private _eingangsdatum: moment.Moment;
    private _regelnGultigAb: moment.Moment;
    private _freigabeDatum: moment.Moment;
    private _status: TSAntragStatus;
    private _typ: TSAntragTyp;
    private _eingangsart: TSEingangsart;

    get dossier(): TSDossier {
        return this._dossier;
    }

    set dossier(value: TSDossier) {
        this._dossier = value;
    }

    get gesuchsperiode(): TSGesuchsperiode {
        return this._gesuchsperiode;
    }

    set gesuchsperiode(gesuchsperiode: TSGesuchsperiode) {
        this._gesuchsperiode = gesuchsperiode;
    }

    get eingangsdatum(): moment.Moment {
        return this._eingangsdatum;
    }

    set eingangsdatum(value: moment.Moment) {
        this._eingangsdatum = value;
    }

    get regelnGultigAb(): moment.Moment {
        return this._regelnGultigAb;
    }

    set regelnGultigAb(value: moment.Moment) {
        this._regelnGultigAb = value;
    }

    get freigabeDatum(): moment.Moment {
        return this._freigabeDatum;
    }

    set freigabeDatum(value: moment.Moment) {
        this._freigabeDatum = value;
    }

    get status(): TSAntragStatus {
        return this._status;
    }

    set status(value: TSAntragStatus) {
        this._status = value;
    }

    get typ(): TSAntragTyp {
        return this._typ;
    }

    set typ(value: TSAntragTyp) {
        this._typ = value;
    }

    get eingangsart(): TSEingangsart {
        return this._eingangsart;
    }

    set eingangsart(value: TSEingangsart) {
        this._eingangsart = value;
    }
}
