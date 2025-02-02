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

import {TestBed} from '@angular/core/testing';
import {TSFinanzielleSituationResultateDTO} from '../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {BerechnungsManager} from '../../../service/berechnungsManager';

import {FinanzielleSituationLuzernService} from './finanzielle-situation-luzern.service';

describe('FinanzielleSituationLuzernService', () => {
    let service: FinanzielleSituationLuzernService;

    const berechnungsManagerSpy = jasmine.createSpyObj<BerechnungsManager>(
        BerechnungsManager.name,
        ['calculateFinanzielleSituation', 'calculateFinanzielleSituationTemp']
    );
    berechnungsManagerSpy.calculateFinanzielleSituationTemp.and.returnValue(
        Promise.resolve(new TSFinanzielleSituationResultateDTO())
    );

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                {provide: BerechnungsManager, useValue: berechnungsManagerSpy}
            ]
        });
        service = TestBed.inject(FinanzielleSituationLuzernService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
