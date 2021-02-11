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

import {Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {BehaviorSubject} from 'rxjs';

import {SavingInfo} from './saving-info.component';

describe('SaveInputInfoComponent', () => {
    let component: TestHostComponent;
    let fixture: ComponentFixture<TestHostComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [SavingInfo, TestHostComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(TestHostComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should show saving', () => {
        expect(fixture.nativeElement.querySelector('span').innerText).toEqual('SAVE');
    });

    it('should show saved', () => {
        component.input.next(false);
        fixture.changeDetectorRef.detectChanges();
        expect(fixture.nativeElement.querySelector('span').innerText).toEqual('SAVED');
    });

    @Component({
        selector: `host-component`,
        template: `<dv-saving-info [saving$]="input"></dv-saving-info>`
    })
    class TestHostComponent {
        public input = new BehaviorSubject(true);
    }
});
