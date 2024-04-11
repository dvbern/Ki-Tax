import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NgForm} from '@angular/forms';
import {ListResourceRS} from '../../app/core/service/listResourceRS.rest';
import {SharedModule} from '../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../hybridTools/mockUpgradedDirective';
import {TSZahlungsinformationen} from '../../models/TSZahlungsinformationen';
import {GesuchModelManager} from '../service/gesuchModelManager';

import {AuszahlungsdatenComponent} from './auszahlungsdaten.component';

describe('AuszahlungsdatenComponent', () => {
    let component: AuszahlungsdatenComponent;
    let fixture: ComponentFixture<AuszahlungsdatenComponent>;
    const gesuchModelManagerSpy =
        jasmine.createSpyObj<GesuchModelManager>(GesuchModelManager.name,
            ['openGesuch', 'isGesuchReadonly', 'isKorrekturModusJugendamt']);
    gesuchModelManagerSpy.isGesuchReadonly.and.returnValue(false);
    gesuchModelManagerSpy.isKorrekturModusJugendamt.and.returnValue(false);
    const listRSSpy = jasmine.createSpyObj<ListResourceRS>(ListResourceRS.name, ['getLaenderList']);
    listRSSpy.getLaenderList.and.resolveTo([]);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SharedModule],
            declarations: [AuszahlungsdatenComponent],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                {provide: ListResourceRS, useValue: listRSSpy},
                {provide: NgForm, useValue: new NgForm([], [])}
            ]
        }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(AuszahlungsdatenComponent);
        component = fixture.componentInstance;
        component.auszahlungsdaten = new TSZahlungsinformationen();
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
