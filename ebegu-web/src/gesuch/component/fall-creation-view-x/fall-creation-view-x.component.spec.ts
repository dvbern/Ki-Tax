/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NgForm} from '@angular/forms';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import {of} from 'rxjs';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {GesuchsperiodeRS} from '../../../app/core/service/gesuchsperiodeRS.rest';
import {SharedModule} from '../../../app/shared/shared.module';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedDirective';
import {TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSGesuch} from '../../../models/TSGesuch';
import {TSPublicAppConfig} from '../../../models/TSPublicAppConfig';
import {FinanzielleSituationRS} from '../../service/finanzielleSituationRS.rest';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {GesuchRS} from '../../service/gesuchRS.rest';
import {WizardStepManager} from '../../service/wizardStepManager';

import {FallCreationViewXComponent} from './fall-creation-view-x.component';

describe('FallCreationViewXComponent', () => {
    let component: FallCreationViewXComponent;
    let fixture: ComponentFixture<FallCreationViewXComponent>;
    const form = new NgForm([], []);
    form.form.markAsUntouched();

    const gesuchModelManager = jasmine.createSpyObj<GesuchModelManager>(
        'GesuchModelManager',
        [
            'saveGesuchAndFall',
            'getGesuchsperiode',
            'getDossier',
            'getGesuch',
            'isGesuchReadonly',
            'isGesuch',
            'isGesuchSaved'
        ]
    );
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(
        ErrorService.name,
        ['clearAll']
    );
    const wizardStepManagerSpy = jasmine.createSpyObj<WizardStepManager>(
        WizardStepManager.name,
        [
            'setCurrentStep',
            'isNextStepBesucht',
            'isNextStepEnabled',
            'getCurrentStepName'
        ]
    );
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(
        AuthServiceRS.name,
        ['principal$', 'isOneOfRoles']
    );
    const gesuchsperiodeRS = jasmine.createSpyObj<GesuchsperiodeRS>(
        GesuchsperiodeRS.name,
        ['getServiceName']
    );
    const finSitRS = jasmine.createSpyObj<FinanzielleSituationRS>(
        FinanzielleSituationRS.name,
        ['saveFinanzielleSituation']
    );
    const stateService = jasmine.createSpyObj<StateService>(StateService.name, [
        'transition'
    ]);
    const uiRouterGlobals = jasmine.createSpyObj<UIRouterGlobals>(
        UIRouterGlobals.name,
        ['params']
    );
    const einstellungenRS = jasmine.createSpyObj<EinstellungRS>(
        EinstellungRS.name,
        ['findEinstellung']
    );
    const stateServiceSpy = jasmine.createSpyObj<StateService>(
        StateService.name,
        ['go']
    );
    const gesuchRSSpy = jasmine.createSpyObj<GesuchRS>(GesuchRS.name, [
        'getNeustesVerfuegtesGesuchFuerGesuch'
    ]);
    const applicationPropertyRSSpy =
        jasmine.createSpyObj<ApplicationPropertyRS>(
            ApplicationPropertyRS.name,
            [
                'getPublicPropertiesCached',
                'getInstitutionenDurchGemeindenEinladen'
            ]
        );
    applicationPropertyRSSpy.getPublicPropertiesCached.and.returnValue(
        Promise.resolve(new TSPublicAppConfig())
    );
    const gesuch = new TSGesuch();
    gesuch.typ = TSAntragTyp.ERSTGESUCH;
    gesuchModelManager.getGesuch.and.returnValue(gesuch);
    authServiceSpy.isOneOfRoles.and.returnValue(false);
    einstellungenRS.findEinstellung.and.returnValue(of(new TSEinstellung()));

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SharedModule],
            declarations: [FallCreationViewXComponent],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManager},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: WizardStepManager, useValue: wizardStepManagerSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: GesuchsperiodeRS, useValue: gesuchsperiodeRS},
                {provide: FinanzielleSituationRS, useValue: finSitRS},
                {provide: StateService, useValue: stateService},
                {provide: UIRouterGlobals, useValue: uiRouterGlobals},
                {provide: EinstellungRS, useValue: einstellungenRS},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: GesuchRS, useValue: gesuchRSSpy},
                {
                    provide: ApplicationPropertyRS,
                    useValue: applicationPropertyRSSpy
                }
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(FallCreationViewXComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
