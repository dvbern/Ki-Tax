import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {StateService, Transition} from '@uirouter/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {InstitutionStammdatenRS} from '../../core/service/institutionStammdatenRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import {GemeindeModule} from '../../gemeinde/gemeinde.module';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {MaterialModule} from '../../shared/material.module';
import {SharedModule} from '../../shared/shared.module';

import {EditInstitutionComponent} from './edit-institution.component';

describe('EditInstitutionComponent', () => {

    let component: EditInstitutionComponent;
    let fixture: ComponentFixture<EditInstitutionComponent>;

    const traegerschaftServiceSpy = jasmine.createSpyObj<TraegerschaftRS>(TraegerschaftRS.name,
        ['getAllActiveTraegerschaften']);
    const insitutionServiceSpy = jasmine.createSpyObj<InstitutionRS>(InstitutionRS.name,
        ['getInstitutionenReadableForCurrentBenutzer']);
    const stammdatenServiceSpy = jasmine.createSpyObj<InstitutionStammdatenRS>(InstitutionStammdatenRS.name,
        ['findInstitutionStammdaten']);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
    const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, ['params', 'from']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isOneOfRoles']);
    const i18nServiceSpy = jasmine
        .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);

    beforeEach(async(() => {

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
                MaterialModule,
                GemeindeModule,
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
            providers: [
                {provide: Transition, useValue: transitionSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: TraegerschaftRS, useValue: traegerschaftServiceSpy},
                {provide: InstitutionRS, useValue: insitutionServiceSpy},
                {provide: InstitutionStammdatenRS, useValue: stammdatenServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
            ],
            declarations: [EditInstitutionComponent],
        }).compileComponents();

        traegerschaftServiceSpy.getAllActiveTraegerschaften.and.resolveTo([]);
        insitutionServiceSpy.getInstitutionenReadableForCurrentBenutzer.and.resolveTo([]);
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
