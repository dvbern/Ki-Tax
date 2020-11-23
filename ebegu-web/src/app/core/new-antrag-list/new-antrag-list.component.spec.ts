import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {UpgradeModule} from '@angular/upgrade/static';
import {TranslateModule} from '@ngx-translate/core';
import {MaterialModule} from '../../shared/material.module';
import {GesuchsperiodeRS} from '../service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../service/institutionRS.rest';

import {NewAntragListComponent} from './new-antrag-list.component';
import {NewUserSelectDirective} from './new-user-select.directive';

describe('NewAntragListComponent', () => {
    let component: NewAntragListComponent;
    let fixture: ComponentFixture<NewAntragListComponent>;
    const insitutionSpy = jasmine.createSpyObj<InstitutionRS>(InstitutionRS.name,
        []);
    const gesuchPeriodeSpy = jasmine.createSpyObj<GesuchsperiodeRS>(GesuchsperiodeRS.name,
        []);

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [NewAntragListComponent, NewUserSelectDirective],
            imports: [MaterialModule, TranslateModule.forRoot(), UpgradeModule],
            providers: [
                {provide: InstitutionRS, useValue: insitutionSpy},
                {provide: GesuchsperiodeRS, useValue: gesuchPeriodeSpy},
            ]
        }).compileComponents();
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
