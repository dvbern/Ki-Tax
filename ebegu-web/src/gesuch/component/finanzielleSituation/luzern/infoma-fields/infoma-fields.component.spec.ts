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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormsModule, NgForm} from '@angular/forms';
import {of} from 'rxjs';
import {ListResourceRS} from '../../../../../app/core/service/listResourceRS.rest';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedDirective';
import {TSFamiliensituation} from '../../../../../models/TSFamiliensituation';
import {TSFamiliensituationContainer} from '../../../../../models/TSFamiliensituationContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {TSGesuch} from '../../../../../models/TSGesuch';
import {TSZahlungsinformationen} from '../../../../../models/TSZahlungsinformationen';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

import {InfomaFieldsComponent} from './infoma-fields.component';

const gesuchModelManagerSpy = jasmine.createSpyObj<GesuchModelManager>(GesuchModelManager.name,
    ['getGesuch', 'getFamiliensituation']);
const listResourceRSSpy = jasmine.createSpyObj<ListResourceRS>(ListResourceRS.name,
    ['getLaenderList']);
const authServiceRSSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
    ['isOneOfRoles']);

describe('InfomaFieldsComponent', () => {
    let component: InfomaFieldsComponent;
    let fixture: ComponentFixture<InfomaFieldsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                FormsModule,
                SharedModule
            ],
            declarations: [
                InfomaFieldsComponent
            ],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                {provide: ListResourceRS, useValue: listResourceRSSpy},
                {provide: AuthServiceRS, useValue: authServiceRSSpy},
                {provide: NgForm, useValue: new NgForm([], [])}
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        listResourceRSSpy.getLaenderList.and.returnValue(of([]).toPromise());
        const famSit = new TSFamiliensituation();
        const gesuch = new TSGesuch();
        gesuch.familiensituationContainer = new TSFamiliensituationContainer();
        gesuch.familiensituationContainer.familiensituationGS = famSit;
        gesuchModelManagerSpy.getGesuch.and.returnValue(gesuch);
        gesuchModelManagerSpy.getFamiliensituation.and.returnValue(famSit);

        fixture = TestBed.createComponent(InfomaFieldsComponent);
        component = fixture.componentInstance;
        component.model = new TSFinanzModel(1, false, 1, 2);
        component.model.zahlungsinformationen = new TSZahlungsinformationen();
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
