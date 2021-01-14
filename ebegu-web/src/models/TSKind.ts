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

import {EbeguUtil} from '../utils/EbeguUtil';
import {TSEinschulungTyp} from './enums/TSEinschulungTyp';
import {TSKinderabzug} from './enums/TSKinderabzug';
import {TSAbstractPersonEntity} from './TSAbstractPersonEntity';
import {TSPensumAusserordentlicherAnspruch} from './TSPensumAusserordentlicherAnspruch';
import {TSPensumFachstelle} from './TSPensumFachstelle';

export class TSKind extends TSAbstractPersonEntity {

    private _kinderabzugErstesHalbjahr: TSKinderabzug;
    private _kinderabzugZweitesHalbjahr: TSKinderabzug;
    private _familienErgaenzendeBetreuung: boolean;
    private _sprichtAmtssprache: boolean;
    private _einschulungTyp: TSEinschulungTyp;
    private _pensumFachstelle: TSPensumFachstelle;
    private _pensumAusserordentlicherAnspruch: TSPensumAusserordentlicherAnspruch;
    private _ausAsylwesen: boolean;
    private _zemisNummer: string; // ZEMIS-Nummer in Format 12345678.9 | 012345678.9 | 012.345.678.9 | 012.345.678-9
    private _zemisNummerStandardFormat: string; // ZEMIS-Nummer in Format 12345678.9
    private _zukunftigeGeburtsdatum: boolean;

    public constructor() {
        super();
    }

    public get kinderabzugErstesHalbjahr(): TSKinderabzug {
        return this._kinderabzugErstesHalbjahr;
    }

    public set kinderabzugErstesHalbjahr(value: TSKinderabzug) {
        this._kinderabzugErstesHalbjahr = value;
    }

    public get kinderabzugZweitesHalbjahr(): TSKinderabzug {
        return this._kinderabzugZweitesHalbjahr;
    }

    public set kinderabzugZweitesHalbjahr(value: TSKinderabzug) {
        this._kinderabzugZweitesHalbjahr = value;
    }

    public get familienErgaenzendeBetreuung(): boolean {
        return this._familienErgaenzendeBetreuung;
    }

    public set familienErgaenzendeBetreuung(value: boolean) {
        this._familienErgaenzendeBetreuung = value;
    }

    public get sprichtAmtssprache(): boolean {
        return this._sprichtAmtssprache;
    }

    public set sprichtAmtssprache(value: boolean) {
        this._sprichtAmtssprache = value;
    }

    public get pensumFachstelle(): TSPensumFachstelle {
        return this._pensumFachstelle;
    }

    public set pensumFachstelle(value: TSPensumFachstelle) {
        this._pensumFachstelle = value;
    }

    public get einschulungTyp(): TSEinschulungTyp {
        return this._einschulungTyp;
    }

    public set einschulungTyp(value: TSEinschulungTyp) {
        this._einschulungTyp = value;
    }

    public get pensumAusserordentlicherAnspruch(): TSPensumAusserordentlicherAnspruch {
        return this._pensumAusserordentlicherAnspruch;
    }

    public set pensumAusserordentlicherAnspruch(value: TSPensumAusserordentlicherAnspruch) {
        this._pensumAusserordentlicherAnspruch = value;
    }

    public get zemisNummer(): string {
        return this._zemisNummer;
    }

    public set zemisNummer(value: string) {
        this._zemisNummer = value;
        this.zemisNummerStandardFormat = EbeguUtil.zemisNummerToStandardZemisNummer(value);
    }

    public get zemisNummerStandardFormat(): string {
        return this._zemisNummerStandardFormat;
    }

    public set zemisNummerStandardFormat(value: string) {
        this._zemisNummerStandardFormat = value;
    }

    public get ausAsylwesen(): boolean {
        return this._ausAsylwesen;
    }

    public set ausAsylwesen(value: boolean) {
        this._ausAsylwesen = value;
    }

    public isGeprueft(): boolean {
        return this._kinderabzugErstesHalbjahr !== null;
    }

    public get zukunftigeGeburtsdatum(): boolean {
        return this._zukunftigeGeburtsdatum;
    }

    public set zukunftigeGeburtsdatum(value: boolean) {
        this._zukunftigeGeburtsdatum = value;
    }
}
