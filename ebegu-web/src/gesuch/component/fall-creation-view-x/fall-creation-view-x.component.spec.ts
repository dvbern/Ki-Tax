import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NgForm} from '@angular/forms';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import {of} from 'rxjs';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {GesuchsperiodeRS} from '../../../app/core/service/gesuchsperiodeRS.rest';
import {SharedModule} from '../../../app/shared/shared.module';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedDirective';
import {TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSGesuch} from '../../../models/TSGesuch';
import {FinanzielleSituationRS} from '../../service/finanzielleSituationRS.rest';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';

import {FallCreationViewXComponent} from './fall-creation-view-x.component';

describe('FallCreationViewXComponent', () => {
    let component: FallCreationViewXComponent;
    let fixture: ComponentFixture<FallCreationViewXComponent>;
    const form = new NgForm([], []);
    form.form.markAsUntouched();

    const gesuchModelManager = jasmine.createSpyObj<GesuchModelManager>('GesuchModelManager',
        [
            'saveGesuchAndFall',
            'getGesuchsperiode',
            'getDossier',
            'getGesuch',
            'isGesuchReadonly',
            'isGesuch',
            'isGesuchSaved'
        ]);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['clearAll']);
    const wizardStepManagerSpy = jasmine.createSpyObj<WizardStepManager>(WizardStepManager.name,
        ['setCurrentStep', 'isNextStepBesucht', 'isNextStepEnabled', 'getCurrentStepName']);
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['principal$', 'isOneOfRoles', 'hasMandantAngebotTS']);
    const gesuchsperiodeRS = jasmine.createSpyObj<GesuchsperiodeRS>(GesuchsperiodeRS.name, ['getServiceName']);
    const finSitRS = jasmine.createSpyObj<FinanzielleSituationRS>(FinanzielleSituationRS.name,
        ['saveFinanzielleSituation']);
    const stateService = jasmine.createSpyObj<StateService>(StateService.name, ['transition']);
    const uiRouterGlobals = jasmine.createSpyObj<UIRouterGlobals>(UIRouterGlobals.name, ['params']);
    const einstellungenRS = jasmine.createSpyObj<EinstellungRS>(EinstellungRS.name, ['findEinstellung']);
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
                {provide: EinstellungRS, useValue: einstellungenRS}
            ]
        }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
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
