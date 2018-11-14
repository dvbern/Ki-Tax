import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {StateService, Transition} from '@uirouter/core';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import ErrorService from '../../core/errors/service/ErrorService';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {InstitutionStammdatenRS} from '../../core/service/institutionStammdatenRS.rest';
import {GemeindeModule} from '../../gemeinde/gemeinde.module';
import {MaterialModule} from '../../shared/material.module';
import {SharedModule} from '../../shared/shared.module';

import {EditInstitutionComponent} from './edit-institution.component';

describe('EditInstitutionComponent', () => {

    let component: EditInstitutionComponent;
    let fixture: ComponentFixture<EditInstitutionComponent>;

    const insitutionServiceSpy = jasmine.createSpyObj<InstitutionRS>(InstitutionRS.name,
        ['getInstitutionenForCurrentBenutzer']);
    const stammdatenServiceSpy = jasmine.createSpyObj<InstitutionStammdatenRS>(InstitutionStammdatenRS.name,
        ['getInstitutionStammdatenByInstitution']);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
    const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, ['params', 'from']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isOneOfRoles']);

    beforeEach(async(() => {

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
                MaterialModule,
                GemeindeModule,
            ],
            providers: [
                {provide: Transition, useValue: transitionSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: InstitutionRS, useValue: insitutionServiceSpy},
                {provide: InstitutionStammdatenRS, useValue: stammdatenServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
            ],
            declarations: [EditInstitutionComponent],
        }).compileComponents();

        insitutionServiceSpy.getInstitutionenForCurrentBenutzer.and.returnValue(Promise.resolve([]));
        stammdatenServiceSpy.getInstitutionStammdatenByInstitution.and.returnValue(Promise.resolve([]));
        transitionSpy.params.and.returnValue({});
        transitionSpy.from.and.returnValue({});
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(EditInstitutionComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
