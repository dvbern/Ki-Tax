/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import {TSOeffnungszeitenTagesschuleTyp} from './TSOeffnungszeitenTagesschuleTyp';

export class TSOeffnungszeitenTagesschule {
    public type: TSOeffnungszeitenTagesschuleTyp;
    public montag: boolean = false;
    public dienstag: boolean = false;
    public mittwoch: boolean = false;
    public donnerstag: boolean = false;
    public freitag: boolean = false;
}
