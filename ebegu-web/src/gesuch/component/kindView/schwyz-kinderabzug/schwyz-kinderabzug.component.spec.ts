import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import {TSFamiliensituation} from '../../../../models/TSFamiliensituation';
import {TSKind} from '../../../../models/TSKind';
import {TSKindContainer} from '../../../../models/TSKindContainer';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {KinderabzugExchangeService} from '../service/kinderabzug-exchange.service';

import {SchwyzKinderabzugComponent} from './schwyz-kinderabzug.component';

const exchangeServiceSpy = jasmine.createSpyObj<KinderabzugExchangeService>(
    KinderabzugExchangeService.name,
    ['getFormValidationTriggered$', 'getFamilienErgaenzendeBetreuungChanged$'],
);
const gesuchModelManagerSpy = jasmine.createSpyObj<GesuchModelManager>(
    GesuchModelManager.name, ['getGesuch', 'getFamiliensituation'],
);
gesuchModelManagerSpy.getFamiliensituation.and
    .returnValue(new TSFamiliensituation());

describe('SchwyzKinderabzugComponent', () => {
    let component: SchwyzKinderabzugComponent;
    let fixture: ComponentFixture<SchwyzKinderabzugComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [SchwyzKinderabzugComponent],
            providers: [
                {provide: KinderabzugExchangeService, useValue: exchangeServiceSpy},
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
            ],
        })
            .compileComponents();
        exchangeServiceSpy.getFormValidationTriggered$.and.returnValue(of(null));
        exchangeServiceSpy.getFamilienErgaenzendeBetreuungChanged$.and.returnValue(of(null));
        fixture = TestBed.createComponent(SchwyzKinderabzugComponent);
        component = fixture.componentInstance;
        component.kindContainer = new TSKindContainer();
        component.kindContainer.kindJA = new TSKind();
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
