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
import {HttpClientModule} from '@angular/common/http';
import {TestBed} from '@angular/core/testing';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSGemeindeAntragTyp} from '../../../models/enums/TSGemeindeAntragTyp';
import {TSWizardStepXTyp} from '../../../models/enums/TSWizardStepXTyp';

import {GemeindeAntragService} from './gemeinde-antrag.service';

const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
    ['isOneOfRoles']);

describe('GemeindeAntragService', () => {
    let service: GemeindeAntragService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientModule],
            providers: [
                {provide: AuthServiceRS, useValue: authServiceSpy}
            ]
        });
        service = TestBed.inject(GemeindeAntragService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should ensure wizardStepType is compatible with backend', () => {
      expect(service.gemeindeAntragTypStringToWizardStepTyp('FERIENBETREUUNG'))
          .toEqual(TSWizardStepXTyp.FERIENBETREUUNG);
      expect(service.gemeindeAntragTypStringToWizardStepTyp('LASTENAUSGLEICH_TAGESSCHULEN'))
          .toEqual(TSWizardStepXTyp.LASTENAUSGLEICH_TAGESSCHULEN);
    });
    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should return only Ferienbetreuung', () => {
        authServiceSpy.isOneOfRoles.and.returnValue(false);
        expect(service.getTypesForRole()).toEqual([TSGemeindeAntragTyp.FERIENBETREUUNG]);
    });

    it('should return Ferienbetreuung, GemeindeKennzahlen and Lastenausgleich Tagesschule', () => {
        authServiceSpy.isOneOfRoles.and.returnValue(true);
        expect(service.getTypesForRole()).toEqual(
            [
                TSGemeindeAntragTyp.LASTENAUSGLEICH_TAGESSCHULEN,
                TSGemeindeAntragTyp.FERIENBETREUUNG,
                TSGemeindeAntragTyp.GEMEINDE_KENNZAHLEN,
            ],
        );
    });
});
