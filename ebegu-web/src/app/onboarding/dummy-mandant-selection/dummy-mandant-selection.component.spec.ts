import {ComponentFixture, TestBed} from '@angular/core/testing';
import {UIRouterGlobals} from '@uirouter/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {SharedModule} from '../../shared/shared.module';

import {DummyMandantSelectionComponent} from './dummy-mandant-selection.component';

describe('DummyMandantSelectionComponent', () => {
    let component: DummyMandantSelectionComponent;
    let fixture: ComponentFixture<DummyMandantSelectionComponent>;
    const applicationPropertyRSSpy = jasmine.createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name,
        ['isDevMode', 'getPublicPropertiesCached']);
    const uiRouterGlobalsSpy = jasmine.createSpyObj<UIRouterGlobals>(UIRouterGlobals.name,
        ['params']);
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['principal$', 'isOneOfRoles']);

    applicationPropertyRSSpy.getPublicPropertiesCached.and.resolveTo({} as any);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SharedModule],
            declarations: [DummyMandantSelectionComponent],
            providers: [
                {
                    provide: ApplicationPropertyRS,
                    useValue: applicationPropertyRSSpy,
                },
                {
                    provide: UIRouterGlobals,
                    useValue: uiRouterGlobalsSpy,
                },
                {
                    provide: AuthServiceRS,
                    useValue: authServiceSpy
                }
            ],

        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(DummyMandantSelectionComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
