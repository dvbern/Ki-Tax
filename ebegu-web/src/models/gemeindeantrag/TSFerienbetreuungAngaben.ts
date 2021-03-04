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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {TSAbstractEntity} from '../TSAbstractEntity';
import {TSFerienbetreuungAngabenAngebot} from './TSFerienbetreuungAngabenAngebot';
import {TSFerienbetreuungAngabenKostenEinnahmen} from './TSFerienbetreuungAngabenKostenEinnahmen';
import {TSFerienbetreuungAngabenNutzung} from './TSFerienbetreuungAngabenNutzung';
import {TSFerienbetreuungAngabenStammdaten} from './TSFerienbetreuungAngabenStammdaten';

export class TSFerienbetreuungAngaben extends TSAbstractEntity {

    private _stammdaten: TSFerienbetreuungAngabenStammdaten;
    private _angebot: TSFerienbetreuungAngabenAngebot;
    private _nutzung: TSFerienbetreuungAngabenNutzung;
    private _kostenEinnahmen: TSFerienbetreuungAngabenKostenEinnahmen;
    private _kantonsbeitrag: number;
    private _gemeindebeitrag: number;

    public get stammdaten(): TSFerienbetreuungAngabenStammdaten {
        return this._stammdaten;
    }

    public set stammdaten(value: TSFerienbetreuungAngabenStammdaten) {
        this._stammdaten = value;
    }

    public get angebot(): TSFerienbetreuungAngabenAngebot {
        return this._angebot;
    }

    public set angebot(value: TSFerienbetreuungAngabenAngebot) {
        this._angebot = value;
    }

    public get nutzung(): TSFerienbetreuungAngabenNutzung {
        return this._nutzung;
    }

    public set nutzung(value: TSFerienbetreuungAngabenNutzung) {
        this._nutzung = value;
    }

    public get kostenEinnahmen(): TSFerienbetreuungAngabenKostenEinnahmen {
        return this._kostenEinnahmen;
    }

    public set kostenEinnahmen(value: TSFerienbetreuungAngabenKostenEinnahmen) {
        this._kostenEinnahmen = value;
    }

    public get kantonsbeitrag(): number {
        return this._kantonsbeitrag;
    }

    public set kantonsbeitrag(value: number) {
        this._kantonsbeitrag = value;
    }

    public get gemeindebeitrag(): number {
        return this._gemeindebeitrag;
    }

    public set gemeindebeitrag(value: number) {
        this._gemeindebeitrag = value;
    }
}
