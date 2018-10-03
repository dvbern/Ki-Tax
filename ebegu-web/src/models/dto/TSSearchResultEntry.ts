/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import TSAbstractAntragDTO from '../TSAbstractAntragDTO';

export default class TSSearchResultEntry {

    private _additionalInformation: string;
    private _antragDTO: TSAbstractAntragDTO;
    private _entity: string;
    private _resultId: string;
    private _gesuchID: string;
    private _fallID: string;
    private _dossierId: string;
    private _text: string;

    public constructor() {
    }

    public get additionalInformation(): string {
        return this._additionalInformation;
    }

    public set additionalInformation(value: string) {
        this._additionalInformation = value;
    }

    public get antragDTO(): TSAbstractAntragDTO {
        return this._antragDTO;
    }

    public set antragDTO(value: TSAbstractAntragDTO) {
        this._antragDTO = value;
    }

    public get entity(): string {
        return this._entity;
    }

    public set entity(value: string) {
        this._entity = value;
    }

    public get resultId(): string {
        return this._resultId;
    }

    public set resultId(value: string) {
        this._resultId = value;
    }

    public get gesuchID(): string {
        return this._gesuchID;
    }

    public set gesuchID(value: string) {
        this._gesuchID = value;
    }

    public get fallID(): string {
        return this._fallID;
    }

    public set fallID(value: string) {
        this._fallID = value;
    }

    public get dossierId(): string {
        return this._dossierId;
    }

    public set dossierId(value: string) {
        this._dossierId = value;
    }

    public get text(): string {
        return this._text;
    }

    public set text(value: string) {
        this._text = value;
    }
}
