import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {waitForAsync, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {StateService, Transition} from '@uirouter/core';
import * as moment from 'moment';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSDateRange} from '../../../models/types/TSDateRange';
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

    beforeEach(waitForAsync(() => {

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

    describe('hasGueltigkeitDecreased', () => {
        const sep28 = moment('2021-09-28');
        const sep27 = moment('2021-09-27');
        const aug1 = moment('2021-08-01');
        const aug2 = moment('2021-08-02');

        describe('start date to past', () => {
            it('should be false if start date is set to past and end date is set to past', () => {
                const current = new TSDateRange(aug2, sep28);
                const change = new TSDateRange(aug1, sep27);
                expect(EditInstitutionComponent.hasGueltigkeitDecreased(current, change)).toBeTrue();
            });

            it('should be false if start date is set to past and end date stays same', () => {
                const current = new TSDateRange(aug2, sep27);
                const change = new TSDateRange(aug1, sep27);
                expect(EditInstitutionComponent.hasGueltigkeitDecreased(current, change)).toBeFalse();
            });

            it('should be false if start date is set to past and end date is set to future', () => {
                const current = new TSDateRange(aug2, sep27);
                const change = new TSDateRange(aug1, sep28);
                expect(EditInstitutionComponent.hasGueltigkeitDecreased(current, change)).toBeFalse();
            });
        });

        describe('start date stays same', () => {

            it('should be true if start date stays same and end date is set to past', () => {
                const current = new TSDateRange(aug1, sep28);
                const change = new TSDateRange(aug1, sep27);
                expect(EditInstitutionComponent.hasGueltigkeitDecreased(current, change)).toBeTrue();
            });

            it('should be false if start date stays same and end date stays same', () => {
                const current = new TSDateRange(aug1, sep27);
                const change = new TSDateRange(aug1, sep27);
                expect(EditInstitutionComponent.hasGueltigkeitDecreased(current, change)).toBeFalse();
            });

            it('should be false if start date stays same and end date is set to future', () => {
                const current = new TSDateRange(aug1, sep27);
                const change = new TSDateRange(aug1, sep28);
                expect(EditInstitutionComponent.hasGueltigkeitDecreased(current, change)).toBeFalse();
            });
        });

        describe('start date to future', () => {

            it('should be true if start date is set to future and end date is set to past', () => {
                const current = new TSDateRange(aug1, sep28);
                const change = new TSDateRange(aug2, sep27);
                expect(EditInstitutionComponent.hasGueltigkeitDecreased(current, change)).toBeTrue();
            });

            it('should be true if start date is set to future and end date stays same', () => {
                const current = new TSDateRange(aug1, sep27);
                const change = new TSDateRange(aug2, sep27);
                expect(EditInstitutionComponent.hasGueltigkeitDecreased(current, change)).toBeTrue();
            });

            it('should be true if start date is set to future and end date is set to future', () => {
                const current = new TSDateRange(aug1, sep27);
                const change = new TSDateRange(aug2, sep28);
                expect(EditInstitutionComponent.hasGueltigkeitDecreased(current, change)).toBeTrue();
            });
        });

        describe('end date set to null', () => {
            it('should be false if start date is set to past', () => {
                const current = new TSDateRange(aug2, sep27);
                const change = new TSDateRange(aug1, null);
                expect(EditInstitutionComponent.hasGueltigkeitDecreased(current, change)).toBeFalse();
            });

            it('should be false if start date stays same', () => {
                const current = new TSDateRange(aug1, sep27);
                const change = new TSDateRange(aug1, null);
                expect(EditInstitutionComponent.hasGueltigkeitDecreased(current, change)).toBeFalse();
            });

            it('should be true if start date is set to future', () => {
                const current = new TSDateRange(aug1, sep27);
                const change = new TSDateRange(aug2, null);
                expect(EditInstitutionComponent.hasGueltigkeitDecreased(current, change)).toBeTrue();
            });
        });

        describe('end date set from null to date', () => {
            it('should be false if start date is set to past', () => {
                const current = new TSDateRange(aug2, null);
                const change = new TSDateRange(aug1, sep27);
                expect(EditInstitutionComponent.hasGueltigkeitDecreased(current, change)).toBeTrue();
            });

            it('should be false if start date stays same', () => {
                const current = new TSDateRange(aug1, null);
                const change = new TSDateRange(aug1, sep27);
                expect(EditInstitutionComponent.hasGueltigkeitDecreased(current, change)).toBeTrue();
            });

            it('should be false if start date is set to future', () => {
                const current = new TSDateRange(aug1, null);
                const change = new TSDateRange(aug2, sep27);
                expect(EditInstitutionComponent.hasGueltigkeitDecreased(current, change)).toBeTrue();
            });
        });

    });
});
