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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {TSDayOfWeek} from './enums/TSDayOfWeek';

export class TSOeffnungstageInstitution {
    private _monday: boolean;
    private _tuesday: boolean;
    private _wednesday: boolean;
    private _thursday: boolean;
    private _friday: boolean;
    private _saturday: boolean;
    private _sunday: boolean;

    public constructor() {
        this._monday = false;
        this._tuesday = false;
        this._wednesday = false;
        this._thursday = false;
        this._friday = false;
        this._saturday = false;
        this._sunday = false;
    }

    public setValueForDay(dayOfWeek: TSDayOfWeek, value: boolean): void {
        switch (dayOfWeek) {
            case TSDayOfWeek.MONDAY:
                this.monday = value;
                break;
            case TSDayOfWeek.TUESDAY:
                this.tuesday = value;
                break;
            case TSDayOfWeek.WEDNESDAY:
                this.wednesday = value;
                break;
            case TSDayOfWeek.THURSDAY:
                this.thursday = value;
                break;
            case TSDayOfWeek.FRIDAY:
                this.friday = value;
                break;
            case TSDayOfWeek.SATURDAY:
                this.saturday = value;
                break;
            case TSDayOfWeek.SUNDAY:
                this.sunday = value;
                break;
            default:
                throw new Error(`unknown day ${dayOfWeek}`);
        }
    }

    /**
     * Gibt eine Liste von allen aktivierten (=true) Wochentagen zurück
     */
    public getActiveDaysAsList(): TSDayOfWeek[] {
        const list: TSDayOfWeek[] = [];
        if (this.monday) {
            list.push(TSDayOfWeek.MONDAY);
        }
        if (this.tuesday) {
            list.push(TSDayOfWeek.TUESDAY);
        }
        if (this.wednesday) {
            list.push(TSDayOfWeek.WEDNESDAY);
        }
        if (this.thursday) {
            list.push(TSDayOfWeek.THURSDAY);
        }
        if (this.friday) {
            list.push(TSDayOfWeek.FRIDAY);
        }
        if (this.saturday) {
            list.push(TSDayOfWeek.SATURDAY);
        }
        if (this.sunday) {
            list.push(TSDayOfWeek.SUNDAY);
        }
        return list;
    }

    public get monday(): boolean {
        return this._monday;
    }

    public set monday(value: boolean) {
        this._monday = value;
    }

    public get tuesday(): boolean {
        return this._tuesday;
    }

    public set tuesday(value: boolean) {
        this._tuesday = value;
    }

    public get wednesday(): boolean {
        return this._wednesday;
    }

    public set wednesday(value: boolean) {
        this._wednesday = value;
    }

    public get thursday(): boolean {
        return this._thursday;
    }

    public set thursday(value: boolean) {
        this._thursday = value;
    }

    public get friday(): boolean {
        return this._friday;
    }

    public set friday(value: boolean) {
        this._friday = value;
    }

    public get saturday(): boolean {
        return this._saturday;
    }

    public set saturday(value: boolean) {
        this._saturday = value;
    }

    public get sunday(): boolean {
        return this._sunday;
    }

    public set sunday(value: boolean) {
        this._sunday = value;
    }
}
