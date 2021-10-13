import {ComponentFixture, TestBed} from '@angular/core/testing';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {StateService} from '@uirouter/angular';
import {of} from 'rxjs';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedComponent';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {SharedModule} from '../../../shared/shared.module';
import {LastenausgleichTSService} from '../services/lastenausgleich-ts.service';

import {VerlaufComponent} from './verlauf.component';

const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['addMesageAsError']);
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
            imports: [
                SharedModule,
                BrowserAnimationsModule
            ],
            providers: [
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: LastenausgleichTSService, useValue: lastenausgleichTSServiceSpy},
                {provide: StateService, useValue: stateServiceSpy},
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
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
