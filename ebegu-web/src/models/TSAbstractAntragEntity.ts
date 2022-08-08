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
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import {TSDossier} from './TSDossier';
import {TSGesuchsperiode} from './TSGesuchsperiode';

export class TSAbstractAntragEntity extends TSAbstractMutableEntity {

    private _dossier: TSDossier;
    private _gesuchsperiode: TSGesuchsperiode;
    private _eingangsdatum: moment.Moment;
    private _regelnGueltigAb: moment.Moment;
    private _freigabeDatum: moment.Moment;
    private _status: TSAntragStatus;
    private _typ: TSAntragTyp;
    private _eingangsart: TSEingangsart;
    private _begruendungMutation: string;

    public get dossier(): TSDossier {
        return this._dossier;
    }

    public set dossier(value: TSDossier) {
        this._dossier = value;
    }

    public get gesuchsperiode(): TSGesuchsperiode {
        return this._gesuchsperiode;
    }

    public set gesuchsperiode(gesuchsperiode: TSGesuchsperiode) {
        this._gesuchsperiode = gesuchsperiode;
    }

    public get eingangsdatum(): moment.Moment {
        return this._eingangsdatum;
    }

    public set eingangsdatum(value: moment.Moment) {
        this._eingangsdatum = value;
    }

    public get regelnGueltigAb(): moment.Moment {
        return this._regelnGueltigAb;
    }

    public set regelnGueltigAb(value: moment.Moment) {
        this._regelnGueltigAb = value;
    }

    public get freigabeDatum(): moment.Moment {
        return this._freigabeDatum;
    }

    public set freigabeDatum(value: moment.Moment) {
        this._freigabeDatum = value;
    }

    public get status(): TSAntragStatus {
        return this._status;
    }

    public set status(value: TSAntragStatus) {
        this._status = value;
    }

    public get typ(): TSAntragTyp {
        return this._typ;
    }

    public set typ(value: TSAntragTyp) {
        this._typ = value;
    }

    public get eingangsart(): TSEingangsart {
        return this._eingangsart;
    }

    public set eingangsart(value: TSEingangsart) {
        this._eingangsart = value;
    }

    public get begruendungMutation(): string {
        return this._begruendungMutation;
    }

    public set begruendungMutation(value: string) {
        this._begruendungMutation = value;
    }
}
