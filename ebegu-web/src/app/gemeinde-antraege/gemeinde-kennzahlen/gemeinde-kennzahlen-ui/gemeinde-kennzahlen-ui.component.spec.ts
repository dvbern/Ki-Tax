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
import {of} from 'rxjs';
import {TSGemeindeKennzahlen} from '../../../../models/gemeindeantrag/gemeindekennzahlen/TSGemeindeKennzahlen';
import {SharedModule} from '../../../shared/shared.module';
import {GemeindeKennzahlenService} from '../gemeinde-kennzahlen.service';

import {GemeindeKennzahlenUiComponent} from './gemeinde-kennzahlen-ui.component';

describe('GemeindeKennzahlenUiComponent', () => {
    let component: GemeindeKennzahlenUiComponent;
    let fixture: ComponentFixture<GemeindeKennzahlenUiComponent>;

    const gemeindeKennzahlenServiceSpy = jasmine.createSpyObj<GemeindeKennzahlenService>(GemeindeKennzahlenService.name,
        ['getGemeindeKennzahlenAntrag', 'updateGemeindeKennzahlenAntragStore']);
    gemeindeKennzahlenServiceSpy.getGemeindeKennzahlenAntrag.and.returnValue(of(new TSGemeindeKennzahlen()));

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SharedModule],
            declarations: [GemeindeKennzahlenUiComponent],
            providers: [
                {
                    provide: GemeindeKennzahlenService,
                    useValue: gemeindeKennzahlenServiceSpy
                }
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(GemeindeKennzahlenUiComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
