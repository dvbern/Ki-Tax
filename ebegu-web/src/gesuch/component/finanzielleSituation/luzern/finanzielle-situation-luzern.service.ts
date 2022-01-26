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

import {Injectable} from '@angular/core';
import {Observable, ReplaySubject, Subject} from 'rxjs';
import {TSFinanzielleSituationResultateDTO} from '../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSFamilienstatus} from '../../../../models/enums/TSFamilienstatus';
import {TSFinanzModel} from '../../../../models/TSFinanzModel';
import {BerechnungsManager} from '../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../service/gesuchModelManager';

@Injectable({
    providedIn: 'root',
})
export class FinanzielleSituationLuzernService {

    private readonly _massgebendesEinkommenStore: Subject<TSFinanzielleSituationResultateDTO> = new ReplaySubject(1);

    public constructor(
        private readonly berechnungsManager: BerechnungsManager,
    ) {
    }

    public static finSitNeedsTwoSeparateAntragsteller(gesuchModelManager: GesuchModelManager): boolean {
        // TODO finsit Luzern: get this from server or improve
        const familiensituation = gesuchModelManager.getFamiliensituation().familienstatus;
        return familiensituation === TSFamilienstatus.KONKUBINAT
            || (familiensituation === TSFamilienstatus.KONKUBINAT_KEIN_KIND && this.startKonkubinatSmallerThan2Years());
    }

    private static startKonkubinatSmallerThan2Years(): boolean {
        // TODO finsit Luzern: migrate once Konkubinat time is in configuration
        return true;
    }

    public get massgebendesEinkommenStore(): Observable<TSFinanzielleSituationResultateDTO> {
        return this._massgebendesEinkommenStore.asObservable();
    }

    public calculateMassgebendesEinkommen(model: TSFinanzModel): void {
        this.berechnungsManager.calculateFinanzielleSituationTemp(model)
            .then(result => this._massgebendesEinkommenStore.next(result));
    }

}
