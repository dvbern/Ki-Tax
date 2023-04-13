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

import {getTSTaetigkeit, getTSTaetigkeitWithFreiwilligenarbeit, TSTaetigkeit} from '../../../models/enums/TSTaetigkeit';
import {KiBonMandant} from './MANDANTS';
import {MandantVisitor} from './MandantVisitor';

export class TaetigkeitVisitor implements MandantVisitor<ReadonlyArray<TSTaetigkeit>> {
    private readonly _konfigZusaetzlicherAnspruchFreiwilligenarbeitEnabled: boolean;

    public constructor(konfigZusaetzlicherAnspruchFreiwilligenarbeitEnabled: boolean) {
        this._konfigZusaetzlicherAnspruchFreiwilligenarbeitEnabled =
            konfigZusaetzlicherAnspruchFreiwilligenarbeitEnabled;
    }

    public process(mandant: KiBonMandant): any {
        return mandant.accept(this);
    }

    public visitBern(): ReadonlyArray<TSTaetigkeit> {
        return this._konfigZusaetzlicherAnspruchFreiwilligenarbeitEnabled ?
            getTSTaetigkeitWithFreiwilligenarbeit() :
            getTSTaetigkeit();
    }

    public visitAppenzellAusserrhoden(): ReadonlyArray<TSTaetigkeit> {
        return [
            TSTaetigkeit.ANGESTELLT,
            TSTaetigkeit.SELBSTAENDIG,
            TSTaetigkeit.AUSBILDUNG,
            TSTaetigkeit.INTEGRATION_BESCHAEFTIGUNSPROGRAMM,
            TSTaetigkeit.GESUNDHEITLICHE_EINSCHRAENKUNGEN
        ];
    }

    public visitLuzern(): ReadonlyArray<TSTaetigkeit> {
        return this.visitBern();
    }

    public visitSolothurn(): ReadonlyArray<TSTaetigkeit> {
        return this.visitBern();
    }

}
