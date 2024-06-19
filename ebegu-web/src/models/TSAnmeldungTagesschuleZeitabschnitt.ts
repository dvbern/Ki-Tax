/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';

export class TSAnmeldungTagesschuleZeitabschnitt extends TSAbstractDateRangedEntity {
    public massgebendesEinkommenInklAbzugFamgr: number;
    public verpflegungskosten: number;
    public betreuungsstundenProWoche: number;
    public betreuungsminutenProWoche: number;
    public gebuehrProStunde: number;
    public totalKostenProWoche: number;
    public pedagogischBetreut: boolean;
}
