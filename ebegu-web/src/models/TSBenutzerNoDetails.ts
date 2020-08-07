/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
export class TSBenutzerNoDetails {

    public nachname: string;
    public vorname: string;
    public gemeindeIds: Array<string> = [];

    public constructor(
        vorname?: string,
        nachname?: string,
        gemeindeIds?: Array<string>
    ) {
        this.vorname = vorname;
        this.nachname = nachname;
        this.gemeindeIds = gemeindeIds;
    }

    public getFullName(): string {
        return `${this.vorname ? this.vorname : ''} ${this.nachname ? this.nachname : ''}`;
    }
}
