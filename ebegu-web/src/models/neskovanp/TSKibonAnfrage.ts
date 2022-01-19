/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

export class TSKibonAnfrage {
    public antragId: string;

    public zpvNummer: number;

    public gesuchsperiodeBeginnJahr: number;

    public geburtsdatum: moment.Moment;

    public constructor(
        antragId: string,
        zpvNummer: number,
        gesuchsperiodeBeginnJahr: number,
        geburtsdatum: moment.Moment,
    ) {
        this.antragId = antragId;
        this.zpvNummer = zpvNummer;
        this.gesuchsperiodeBeginnJahr = gesuchsperiodeBeginnJahr;
        this.geburtsdatum = geburtsdatum;
    }
}
