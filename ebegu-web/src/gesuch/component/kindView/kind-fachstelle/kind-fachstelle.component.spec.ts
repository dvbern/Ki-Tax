import {ComponentFixture, TestBed} from '@angular/core/testing';
import {SharedModule} from '../../../../app/shared/shared.module';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedDirective';
import {TSFachstelle} from '../../../../models/TSFachstelle';
import {TSGesuchsperiode} from '../../../../models/TSGesuchsperiode';
import {TSPensumFachstelle} from '../../../../models/TSPensumFachstelle';
import {TSDateRange} from '../../../../models/types/TSDateRange';
import {GesuchModelManager} from '../../../service/gesuchModelManager';

import {KindFachstelleComponent} from './kind-fachstelle.component';

describe('KindFachstelleComponent', () => {
    let component: KindFachstelleComponent;
    let fixture: ComponentFixture<KindFachstelleComponent>;
    const gesuchModelManagerSpy = jasmine.createSpyObj<GesuchModelManager>(GesuchModelManager.name,
        ['isGesuchReadonly', 'getGesuchsperiode']);
    gesuchModelManagerSpy.getGesuchsperiode.and.returnValue(new TSGesuchsperiode());
    gesuchModelManagerSpy.isGesuchReadonly.and.returnValue(false);
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['principal$']);

    component.pensumFachstelle = new TSPensumFachstelle();
    component.pensumFachstelle.fachstelle = new TSFachstelle();
    component.pensumFachstelle.gueltigkeit = new TSDateRange();

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SharedModule],
            declarations: [KindFachstelleComponent],
            providers: [
                {
                    provide: GesuchModelManager,
                    useValue: gesuchModelManagerSpy,
                },
                {
                    provide: AuthServiceRS,
                    useValue: authServiceSpy,
                },
            ],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(KindFachstelleComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
