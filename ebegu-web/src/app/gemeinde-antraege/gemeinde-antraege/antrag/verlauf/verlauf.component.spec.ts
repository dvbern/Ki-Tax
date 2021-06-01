import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/angular';
import {of} from 'rxjs';
import {ErrorService} from '../../../../core/errors/service/ErrorService';
import {LastenausgleichTSService} from '../../../lastenausgleich-ts/services/lastenausgleich-ts.service';

import {VerlaufComponent} from './verlauf.component';

const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['addMesageAsError']);
const translateServiceSpy = jasmine.createSpyObj<TranslateService>(ErrorService.name, ['instant']);
const lastenausgleichTSServiceSpy = jasmine.createSpyObj<LastenausgleichTSService>(LastenausgleichTSService.name,
    ['getVerlauf']);
const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name,
    ['go']);

describe('VerlaufComponent', () => {
    let component: VerlaufComponent;
    let fixture: ComponentFixture<VerlaufComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [VerlaufComponent],
            providers: [
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: TranslateService, useValue: translateServiceSpy},
                {provide: LastenausgleichTSService, useValue: lastenausgleichTSServiceSpy},
                {provide: StateService, useValue: stateServiceSpy},
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        lastenausgleichTSServiceSpy.getVerlauf.and.returnValue(of([]));
        fixture = TestBed.createComponent(VerlaufComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
