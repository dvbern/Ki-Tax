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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedComponent';
import {TSGemeindeKennzahlen} from '../../../../models/gemeindeantrag/gemeindekennzahlen/TSGemeindeKennzahlen';
import {TSBenutzer} from '../../../../models/TSBenutzer';
import {ErrorServiceX} from '../../../core/errors/service/ErrorServiceX';
import {SharedModule} from '../../../shared/shared.module';
import {GemeindeKennzahlenService} from '../gemeinde-kennzahlen.service';

import {GemeindeKennzahlenFormularComponent} from './gemeinde-kennzahlen-formular.component';

describe('GemeindeKennzahlenFormularComponent', () => {
    let component: GemeindeKennzahlenFormularComponent;
    let fixture: ComponentFixture<GemeindeKennzahlenFormularComponent>;

    const gemeindeKennzahlenServiceSpy = jasmine.createSpyObj<GemeindeKennzahlenService>(GemeindeKennzahlenService.name,
        ['getGemeindeKennzahlenAntrag']);
    gemeindeKennzahlenServiceSpy.getGemeindeKennzahlenAntrag.and.returnValue(of(new TSGemeindeKennzahlen()));

    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['principal$']);
    authServiceSpy.principal$ = of(new TSBenutzer());

    const errorServiceSpy = jasmine.createSpyObj<ErrorServiceX>(ErrorServiceX.name, ['addMesageAsInfo']);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SharedModule, BrowserAnimationsModule],
            declarations: [GemeindeKennzahlenFormularComponent],
            providers: [
                {
                    provide: GemeindeKennzahlenService,
                    useValue: gemeindeKennzahlenServiceSpy,
                },
                {
                    provide: AuthServiceRS,
                    useValue: authServiceSpy,
                },
                {
                    provide: ErrorServiceX,
                    useValue: errorServiceSpy,
                },
            ],
        }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(GemeindeKennzahlenFormularComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
