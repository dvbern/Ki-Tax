/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {TSFinanzielleSituationResultateDTO} from '../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSFinanzModel} from '../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {BerechnungsManager} from '../../../service/berechnungsManager';

export interface MassgebendesEinkommenResultate {
    ekvResultate?: TSFinanzielleSituationResultateDTO;
    finSitResultate?: TSFinanzielleSituationResultateDTO;
    veraenderung?: MassgebendesEinkommenVeraenderung;
}

interface MassgebendesEinkommenVeraenderung {
    total: string;
}

@Injectable({
    providedIn: 'root',
})
export class FinanzielleSituationSchwyzService {

    private readonly _massgebendesEinkommenStore: BehaviorSubject<MassgebendesEinkommenResultate> = new BehaviorSubject({
        ekvResultate: null,
        finSitResultate: null,
        veraenderung: null,
    });

    public constructor(
        private readonly berechnungsManager: BerechnungsManager,
    ) {
    }

    public get massgebendesEinkommenStore(): Observable<MassgebendesEinkommenResultate> {
        return this._massgebendesEinkommenStore.asObservable();
    }

    public calculateMassgebendesEinkommen(model: TSFinanzModel): void {
        this.berechnungsManager.calculateFinanzielleSituationTemp(model)
            .then(async result => Promise.all([
                result,
                this.calcVeraenderung(result, this._massgebendesEinkommenStore.value.ekvResultate),
            ]))
            .then(([result, veraenderung]) => {
                const nextValue: MassgebendesEinkommenResultate = {
                    ...this._massgebendesEinkommenStore.value,
                    finSitResultate: result,
                    veraenderung,
                };
                return this._massgebendesEinkommenStore.next(nextValue);
            });
    }

    public calculateEinkommensverschlechterung(model: TSFinanzModel, basisJahrPlus: number): void {
        this.berechnungsManager.calculateEinkommensverschlechterungTemp(model, basisJahrPlus)
            .then(result => this._massgebendesEinkommenStore.next({
                ...this._massgebendesEinkommenStore.value,
                ekvResultate: result,
            }));
    }

    private async calcVeraenderung(
        finSitResultate?: TSFinanzielleSituationResultateDTO,
        ekvResultate?: TSFinanzielleSituationResultateDTO,
    ): Promise<MassgebendesEinkommenVeraenderung | null> {
        if (EbeguUtil.isNullOrUndefined(ekvResultate) || EbeguUtil.isNullOrUndefined(finSitResultate)) {
            return null;
        }
        return {
            total: await this.berechnungsManager.calculateProzentualeDifferenz(finSitResultate.massgebendesEinkVorAbzFamGr,
                ekvResultate.massgebendesEinkVorAbzFamGr),
        };
    }
}
