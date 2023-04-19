/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import {Injectable} from '@angular/core';
import {Observable, ReplaySubject, Subject} from 'rxjs';
import {TSFinanzielleSituationResultateDTO} from '../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSFamilienstatus} from '../../../../models/enums/TSFamilienstatus';
import {TSFinanzModel} from '../../../../models/TSFinanzModel';
import {TSGesuch} from '../../../../models/TSGesuch';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {BerechnungsManager} from '../../../service/berechnungsManager';

@Injectable({
    providedIn: 'root'
})
export class FinanzielleSituationAppenzellService {

    private readonly _massgebendesEinkommenStore: Subject<TSFinanzielleSituationResultateDTO> = new ReplaySubject(1);

    public constructor(
        private readonly berechnungsManager: BerechnungsManager
    ) {
    }

    public get massgebendesEinkommenStore(): Observable<TSFinanzielleSituationResultateDTO> {
        return this._massgebendesEinkommenStore.asObservable();
    }

    public calculateMassgebendesEinkommen(finanzModel: TSFinanzModel): void {
        this.berechnungsManager.calculateFinanzielleSituationTemp(finanzModel)
            .then(result => this._massgebendesEinkommenStore.next(result));
    }

    public calculateEinkommensverschlechterung(finanzModel: TSFinanzModel, basisJahrPlus: number): void {
        this.berechnungsManager.calculateEinkommensverschlechterungTemp(finanzModel, basisJahrPlus)
            .then(result => this._massgebendesEinkommenStore.next(result));
    }

    public static finSitNeedsTwoSeparateAntragsteller(gesuch: TSGesuch): boolean {
        if (EbeguUtil.isNullOrUndefined(gesuch)) {
            return false;
        }
        if (EbeguUtil.isNullOrUndefined(gesuch.extractFamiliensituation())) {
            return false;
        }
        const spezialFall = gesuch.extractFamiliensituation().geteilteObhut
            && EbeguUtil.isNotNullAndFalse(gesuch.extractFamiliensituation().gemeinsamerHaushaltMitObhutsberechtigterPerson)
            && gesuch.extractFamiliensituation().gemeinsamerHaushaltMitPartner;
        const spezialFall1 = EbeguUtil.isNotNullAndFalse(gesuch.extractFamiliensituation().geteilteObhut)
            && gesuch.extractFamiliensituation().gemeinsamerHaushaltMitPartner;
        const gesuchHasSecondAntragsteller = EbeguUtil.isNotNullOrUndefined(gesuch.gesuchsteller2);
        const gemeinsameSteuererklaerung = gesuch.extractFamiliensituation().gemeinsameSteuererklaerung;
        return gesuchHasSecondAntragsteller && EbeguUtil.isNotNullAndFalse(gemeinsameSteuererklaerung)
            || (gesuch.extractFamiliensituation().familienstatus === TSFamilienstatus.ALLEINERZIEHEND
                && (spezialFall || spezialFall1));
    }
}
