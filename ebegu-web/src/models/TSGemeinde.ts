/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import * as moment from 'moment';
import {TSGemeindeStatus} from './enums/TSGemeindeStatus';
import {TSAbstractEntity} from './TSAbstractEntity';

export class TSGemeinde extends TSAbstractEntity {
    public name: string;
    public gemeindeNummer: number;
    public bfsNummer: number;
    public status: TSGemeindeStatus;
    public betreuungsgutscheineStartdatum: moment.Moment;
    public tagesschulanmeldungenStartdatum: moment.Moment;
    public ferieninselanmeldungenStartdatum: moment.Moment;
    public angebotBG: boolean;
    public angebotTS: boolean;
    public angebotFI: boolean;
    public key: string;

    public isAtLeastOneAngebotSelected(): boolean {
        const hasAngebot =
            this.angebotBG || this.angebotTS || this.angebotFI;
        return hasAngebot;
    }
}
