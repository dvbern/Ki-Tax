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

import {TSEinstellungKey} from './enums/TSEinstellungKey';
import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';
import {TSDateRange} from './types/TSDateRange';

export class TSEinstellung extends TSAbstractDateRangedEntity {
    private _key: TSEinstellungKey;
    private _value: string;
    private _gemeindeId: string = null;
    private _gesuchsperiodeId: string = null;
    private _erklaerung: string = null;

    public constructor(
        gueltigkeit?: TSDateRange,
        key?: TSEinstellungKey,
        value?: string,
        gemeindeId?: string,
        gesuchsperiodeId?: string,
        erklaerung?: string
    ) {
        super(gueltigkeit);
        this._key = key;
        this._value = value;
        this._gemeindeId = gemeindeId;
        this._gesuchsperiodeId = gesuchsperiodeId;
        this._erklaerung = erklaerung;
    }

    public get key(): TSEinstellungKey {
        return this._key;
    }

    public set key(value: TSEinstellungKey) {
        this._key = value;
    }

    public get value(): string {
        return this._value;
    }

    public set value(value: string) {
        this._value = value;
    }

    public get gemeindeId(): string {
        return this._gemeindeId;
    }

    public set gemeindeId(value: string) {
        this._gemeindeId = value;
    }

    public get gesuchsperiodeId(): string {
        return this._gesuchsperiodeId;
    }

    public set gesuchsperiodeId(value: string) {
        this._gesuchsperiodeId = value;
    }

    public getValueAsBoolean(): boolean {
        return 'true' === this._value;
    }

    public set erklaerung(value: string) {
        this._erklaerung = value;
    }
    public get erklaerung(): string {
        return this._erklaerung;
    }
}
