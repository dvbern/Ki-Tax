/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {waitForAsync, ComponentFixture, TestBed} from '@angular/core/testing';
import {NgForm} from '@angular/forms';
import {By} from '@angular/platform-browser';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSBerechtigung} from '../../../../models/TSBerechtigung';
import {TestDataUtil} from '../../../../utils/TestDataUtil.spec';
import {InstitutionRS} from '../../../core/service/institutionRS.rest';
import {SozialdienstRS} from '../../../core/service/SozialdienstRS.rest';
import {TraegerschaftRS} from '../../../core/service/traegerschaftRS.rest';
import {I18nServiceRSRest} from '../../../i18n/services/i18nServiceRS.rest';
import {SharedModule} from '../../shared.module';

import {BerechtigungComponent} from './berechtigung.component';

describe('BerechtigungComponent', () => {
    let component: BerechtigungComponent;
    let fixture: ComponentFixture<BerechtigungComponent>;

    const insitutionSpy = jasmine.createSpyObj<InstitutionRS>(InstitutionRS.name,
        ['getInstitutionenEditableForCurrentBenutzer']);
    const traegerschaftSpy = jasmine.createSpyObj<TraegerschaftRS>(TraegerschaftRS.name, ['getAllTraegerschaften']);
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
        ['isRole', 'getVisibleRolesForPrincipal', 'principal$']);
    const gemeindeSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, ['getGemeindenForPrincipal$']);
    const i18nServiceSpy = jasmine
        .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);
    const sozialdienstSpy = jasmine.createSpyObj<SozialdienstRS>(SozialdienstRS.name,
        ['getSozialdienstList']);

    const inputSelector = '.dv-input-container-medium';

    beforeEach(waitForAsync(() => {
        const superadmin = TestDataUtil.createSuperadmin();
        authServiceSpy.principal$ = of(superadmin) as any;
        authServiceSpy.getVisibleRolesForPrincipal.and.returnValue([]);
        insitutionSpy.getInstitutionenEditableForCurrentBenutzer.and.resolveTo([]);
        traegerschaftSpy.getAllTraegerschaften.and.resolveTo([]);
        gemeindeSpy.getGemeindenForPrincipal$.and.returnValue(of([]));
        sozialdienstSpy.getSozialdienstList.and.resolveTo([]);

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
            ],
            providers: [
                {provide: InstitutionRS, useValue: insitutionSpy},
                {provide: TraegerschaftRS, useValue: traegerschaftSpy},
                {provide: GemeindeRS, useValue: gemeindeSpy},
                {provide: SozialdienstRS, useValue: sozialdienstSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: NgForm, useValue: new NgForm([], [])},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
            ],
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(BerechtigungComponent);
        component = fixture.componentInstance;
        component.berechtigung = new TSBerechtigung();
    });

    it('should create', () => {
        fixture.detectChanges();
        expect(component).toBeTruthy();
    });

    it('should load institutionen', () => {
        fixture.detectChanges();
        expect(insitutionSpy.getInstitutionenEditableForCurrentBenutzer).toHaveBeenCalled();
    });

    it('should load traegerschaften for role ADMIN_TRAEGERSCHAFT', () => {
        component.berechtigung.role = TSRole.ADMIN_TRAEGERSCHAFT;
        fixture.detectChanges();
        // after detecting changes the element in the html should subscribe to the
        // controller and thus get the list of traegerschaften
        expect(traegerschaftSpy.getAllTraegerschaften).toHaveBeenCalled();
    });

    it('should display gemeinde when gemeinde dependent role', () => {
        component.berechtigung.role = TSRole.ADMIN_GEMEINDE;
        expect(component.berechtigung.hasGemeindeRole()).toBe(true);
        fixture.detectChanges();

        const debugElements = fixture.debugElement.queryAll(By.css(inputSelector));
        expect(debugElements.length).toBe(0);

        expect(fixture.debugElement.query(By.css('dv-gemeinde-multiselect'))).toBeTruthy();
    });

    it('should display institution when institution dependent role', () => {
        component.berechtigung.role = TSRole.SACHBEARBEITER_INSTITUTION;
        expect(component.berechtigung.hasInstitutionRole()).toBe(true);
        fixture.detectChanges();

        const debugElements = fixture.debugElement.queryAll(By.css(inputSelector));
        expect(debugElements.length).toBe(0);

        expect(fixture.debugElement.query(By.css('[id^=institution-]'))).toBeTruthy();
    });

    it('should display institution when institution dependent role', () => {
        component.berechtigung.role = TSRole.SACHBEARBEITER_TRAEGERSCHAFT;
        expect(component.berechtigung.hasTraegerschaftRole()).toBe(true);
        fixture.detectChanges();

        const debugElements = fixture.debugElement.queryAll(By.css(inputSelector));
        expect(debugElements.length).toBe(0);

        expect(fixture.debugElement.query(By.css('[id^=treagerschaft-]'))).toBeTruthy();
    });

    it('should display sozialdienst when sozialdienst dependent role', () => {
        component.berechtigung.role = TSRole.ADMIN_SOZIALDIENST;
        expect(component.berechtigung.hasSozialdienstRole()).toBe(true);
        fixture.detectChanges();

        const debugElements = fixture.debugElement.queryAll(By.css(inputSelector));
        expect(debugElements.length).toBe(0);

        expect(fixture.debugElement.query(By.css('[id^=sozialdienst-]'))).toBeTruthy();

        expect(sozialdienstSpy.getSozialdienstList).toHaveBeenCalled();
    });

    it('should display sozialdienst when sozialdienst dependent role', () => {
        component.berechtigung.role = TSRole.SACHBEARBEITER_SOZIALDIENST;
        expect(component.berechtigung.hasSozialdienstRole()).toBe(true);
        fixture.detectChanges();

        const debugElements = fixture.debugElement.queryAll(By.css(inputSelector));
        expect(debugElements.length).toBe(0);

        expect(fixture.debugElement.query(By.css('[id^=sozialdienst-]'))).toBeTruthy();

        expect(sozialdienstSpy.getSozialdienstList).toHaveBeenCalled();
    });
});
