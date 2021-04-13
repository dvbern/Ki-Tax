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
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {StateService} from '@uirouter/angular';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {of} from 'rxjs';
import {ErrorService} from '../../../../core/errors/service/ErrorService';
import {WindowRef} from '../../../../core/service/windowRef.service';
import {SharedModule} from '../../../../shared/shared.module';
import {GemeindeAntragService} from '../../../services/gemeinde-antrag.service';

import {TagesschulenListComponent} from './tagesschulen-list.component';

const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name,
    ['go']);

const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, [
    'getErrors',
    'addMesageAsError'
]);
const gemeindeAntragServiceSpy = jasmine.createSpyObj<GemeindeAntragService>(GemeindeAntragService.name, [
    'getAllVisibleTagesschulenAngabenForTSLastenausgleich'
]);
describe('TagesschulenListComponent', () => {
    let component: TagesschulenListComponent;
    let fixture: ComponentFixture<TagesschulenListComponent>;
    gemeindeAntragServiceSpy.getAllVisibleTagesschulenAngabenForTSLastenausgleich.and.returnValue(of([]));

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                HttpClientModule,
                SharedModule,
                BrowserAnimationsModule
            ],
            providers: [
                WindowRef,
                {
                    provide: ErrorService,
                    useValue: errorServiceSpy,
                },
                {
                    provide: StateService,
                    useValue: stateServiceSpy,
                },
                {
                    provide: GemeindeAntragService,
                    useValue: gemeindeAntragServiceSpy
                }
            ],
            declarations: [TagesschulenListComponent],
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(TagesschulenListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
