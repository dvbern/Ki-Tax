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

import {TSEinschulungTyp} from '../../enums/TSEinschulungTyp';
import {TSAbstractEntity} from '../../TSAbstractEntity';
import {TSGemeinde} from '../../TSGemeinde';
import {TSGesuchsperiode} from '../../TSGesuchsperiode';
import {TSGemeindeKennzahlenStatus} from './TSGemeindeKennzahlenStatus';

export class TSGemeindeKennzahlen extends TSAbstractEntity {
    private _gemeinde: TSGemeinde;
    private _gesuchsperiode: TSGesuchsperiode;
    private _status: TSGemeindeKennzahlenStatus;

    private _nachfrageErfuellt: boolean;
    private _gemeindeKontingentiert: boolean;
    private _nachfrageAnzahl: number;

    private _nachfrageDauer: number;

    private _limitierungTfo: TSEinschulungTyp;

    public set limitierungTfo(value: TSEinschulungTyp) {
        this._limitierungTfo = value;
    }

    public get limitierungTfo(): TSEinschulungTyp {
        return this._limitierungTfo;
    }

    public get nachfrageDauer(): number {
        return this._nachfrageDauer;
    }

    public set nachfrageDauer(value: number) {
        this._nachfrageDauer = value;
    }

    public get nachfrageAnzahl(): number {
        return this._nachfrageAnzahl;
    }

    public set nachfrageAnzahl(value: number) {
        this._nachfrageAnzahl = value;
    }

    public get nachfrageErfuellt(): boolean {
        return this._nachfrageErfuellt;
    }

    public set nachfrageErfuellt(value: boolean) {
        this._nachfrageErfuellt = value;
    }

    public get gemeindeKontingentiert(): boolean {
        return this._gemeindeKontingentiert;
    }

    public set gemeindeKontingentiert(value: boolean) {
        this._gemeindeKontingentiert = value;
    }

    public get status(): TSGemeindeKennzahlenStatus {
        return this._status;
    }

    public set status(value: TSGemeindeKennzahlenStatus) {
        this._status = value;
    }

    public get gesuchsperiode(): TSGesuchsperiode {
        return this._gesuchsperiode;
    }

    public set gesuchsperiode(value: TSGesuchsperiode) {
        this._gesuchsperiode = value;
    }

    public get gemeinde(): TSGemeinde {
        return this._gemeinde;
    }

    public set gemeinde(value: TSGemeinde) {
        this._gemeinde = value;
    }

    public isInBearbeitungGemeinde(): boolean {
        return this.status === TSGemeindeKennzahlenStatus.IN_BEARBEITUNG_GEMEINDE;
    }

    public isAbgeschlossen(): boolean {
        return this.status === TSGemeindeKennzahlenStatus.ABGESCHLOSSEN;
    }
}
