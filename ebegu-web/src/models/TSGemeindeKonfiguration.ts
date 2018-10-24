/*
 * AGPL File-Header
 *
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

import {TSEinschulungTyp} from './enums/TSEinschulungTyp';
import {TSGesuchsperiodeStatus} from './enums/TSGesuchsperiodeStatus';
import TSEinstellung from './TSEinstellung';
import TSGesuchsperiode from './TSGesuchsperiode';

export default class TSGemeindeKonfiguration {
    public gesuchsperiodeName: string;
    public gesuchsperiodeId: string;
    public gesuchsperiodeStatus: TSGesuchsperiodeStatus;
    public konfigKontingentierung: boolean; // only on client
    public konfigBeguBisUndMitSchulstufe: TSEinschulungTyp; // only on client
    public editMode: boolean; // only on client
    public konfigurationen: TSEinstellung[];

}
