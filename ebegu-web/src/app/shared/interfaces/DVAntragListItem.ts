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
import {TSBetreuungsangebotTyp} from '../../../models/enums/betreuung/TSBetreuungsangebotTyp';
import {TSBenutzerNoDetails} from '../../../models/TSBenutzerNoDetails';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';

export interface DVAntragListItem {
    fallNummer?: number;
    dossierId?: string;
    antragId?: string;
    gemeinde?: string;
    status?: string;
    familienName?: string;
    kinder?: string[];
    antragTyp?: string;
    periode?: TSGesuchsperiode;
    periodenString?: string;
    aenderungsdatum?: moment.Moment;
    internePendenz?: boolean;
    dokumenteHochgeladen?: boolean;
    angebote?: TSBetreuungsangebotTyp[];
    institutionen?: string[];
    verantwortlicheTS?: string;
    verantwortlicheBG?: string;
    isSozialdienst?: boolean;
    antragAbgeschlossen?: boolean;
    verantwortlicherGemeindeantraege?: TSBenutzerNoDetails;

    hasBesitzer?(): boolean;
}
