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
import {Directive, EventEmitter, Input, Output} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {UpgradeModule} from '@angular/upgrade/static';
import {TranslateModule} from '@ngx-translate/core';
import {TransitionService} from '@uirouter/angular';
import {$q, UIRouterGlobals} from '@uirouter/core';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {SearchRS} from '../../../gesuch/service/searchRS.rest';
import {TSAntragDTO} from '../../../models/TSAntragDTO';
import {TSBenutzerNoDetails} from '../../../models/TSBenutzerNoDetails';
import {TSAntragSearchresultDTO} from '../../../models/TSAntragSearchresultDTO';
import {TSPublicAppConfig} from '../../../models/TSPublicAppConfig';
import {MaterialModule} from '../../shared/material.module';
import {StateStoreService} from '../../shared/services/state-store.service';
import {ErrorService} from '../errors/service/ErrorService';
import {ApplicationPropertyRS} from '../rest-services/applicationPropertyRS.rest';
import {BenutzerRSX} from '../service/benutzerRSX.rest';
import {GesuchsperiodeRS} from '../service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../service/institutionRS.rest';

import {NewAntragListComponent} from './new-antrag-list.component';
import {DemoFeatureRS} from '../service/demoFeatureRS.rest';

// We mock the user select directive to make the setup easier since these are unit tests
@Directive({
    selector: '[dvNewUserSelect]'
})
class MockNewUserSelectDirective {
    @Input()
    public showSelectionAll: boolean;

    @Input()
    public angular2: boolean;

    @Input()
    public inputId: string;

    @Input()
    public dvUsersearch: string;

    @Input()
    public initialAll: boolean;

    @Input()
    public selectedUser: TSBenutzerNoDetails;

    @Input()
    public sachbearbeiterGemeinde: boolean;

    @Input()
    public schulamt: boolean;

    @Output()
    public readonly userChanged: EventEmitter<{user: TSBenutzerNoDetails}> =
        new EventEmitter<{user: TSBenutzerNoDetails}>();
}

describe('NewAntragListComponent', () => {
    let component: NewAntragListComponent;
    let fixture: ComponentFixture<NewAntragListComponent>;
    const insitutionSpy = jasmine.createSpyObj<InstitutionRS>(
        InstitutionRS.name,
        ['findInstitution', 'getInstitutionenReadableForCurrentBenutzer']
    );
    const gesuchPeriodeSpy = jasmine.createSpyObj<GesuchsperiodeRS>(
        GesuchsperiodeRS.name,
        ['findGesuchsperiode', 'getAllGesuchsperioden']
    );
    const gemeindeRSSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, [
        'getGemeindenForPrincipal$'
    ]);
    const searchRSSpy = jasmine.createSpyObj<SearchRS>(SearchRS.name, [
        'searchAntraege',
        'countAntraege'
    ]);
    const authRSSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, [
        'getPrincipalRole',
        'isOneOfRoles'
    ]);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(
        ErrorService.name,
        ['addMesageAsError']
    );
    const benutzerRSSpy = jasmine.createSpyObj<BenutzerRSX>(BenutzerRSX.name, [
        'getAllBenutzerBgOrGemeinde',
        'getAllBenutzerTsOrGemeinde'
    ]);
    const transitionServiceSpy = jasmine.createSpyObj<TransitionService>(
        TransitionService.name,
        ['onStart']
    );
    const stateStoreServiceSpy = jasmine.createSpyObj<StateStoreService>(
        StateStoreService.name,
        ['has', 'get']
    );
    const uiRouterGlobals = jasmine.createSpyObj<UIRouterGlobals>(
        UIRouterGlobals.name,
        ['$current']
    );
    const appPropRSSpy = jasmine.createSpyObj<ApplicationPropertyRS>(
        ApplicationPropertyRS.name,
        ['getPublicPropertiesCached', 'getActivatedDemoFeatures']
    );
    appPropRSSpy.getPublicPropertiesCached.and.returnValue(
        Promise.resolve(new TSPublicAppConfig())
    );
    appPropRSSpy.getActivatedDemoFeatures.and.returnValue($q.when(''));
    const demofeatureRSSpy = jasmine.createSpyObj<DemoFeatureRS>(
        DemoFeatureRS.name,
        ['isDemoFeatureAllowed']
    );
    demofeatureRSSpy.isDemoFeatureAllowed.and.returnValue(
        Promise.resolve(false)
    );

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [NewAntragListComponent, MockNewUserSelectDirective],
            imports: [
                MaterialModule,
                TranslateModule.forRoot(),
                UpgradeModule,
                BrowserAnimationsModule
            ],
            providers: [
                {provide: InstitutionRS, useValue: insitutionSpy},
                {provide: GesuchsperiodeRS, useValue: gesuchPeriodeSpy},
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
                {provide: AuthServiceRS, useValue: authRSSpy},
                {provide: SearchRS, useValue: searchRSSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: BenutzerRSX, useValue: benutzerRSSpy},
                {provide: TransitionService, useValue: transitionServiceSpy},
                {provide: StateStoreService, useValue: stateStoreServiceSpy},
                {provide: UIRouterGlobals, useValue: uiRouterGlobals},
                {provide: ApplicationPropertyRS, useValue: appPropRSSpy}
            ]
        }).compileComponents();

        insitutionSpy.getInstitutionenReadableForCurrentBenutzer.and.returnValue(
            of([])
        );
        gesuchPeriodeSpy.getAllGesuchsperioden.and.returnValue(
            Promise.resolve([])
        );
        gemeindeRSSpy.getGemeindenForPrincipal$.and.returnValue(of([]));
        authRSSpy.getPrincipalRole.and.returnValue(undefined);
        const dummySearchResult: TSAntragSearchresultDTO = {
            get antragDTOs(): TSAntragDTO[] {
                return [];
            }
        } as any;
        searchRSSpy.searchAntraege.and.returnValue(of(dummySearchResult));
        searchRSSpy.countAntraege.and.returnValue(of(0));
        benutzerRSSpy.getAllBenutzerBgOrGemeinde.and.resolveTo([]);
        benutzerRSSpy.getAllBenutzerTsOrGemeinde.and.resolveTo([]);
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(NewAntragListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
