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

import {HttpClientModule} from '@angular/common/http';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {$q} from '@uirouter/core';
import {of} from 'rxjs';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {SharedModule} from '../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedDirective';
import {TSGesuch} from '../../../models/TSGesuch';
import {GesuchModelManager} from '../../service/gesuchModelManager';

import {InternePendenzenComponent} from './interne-pendenzen.component';
import {InternePendenzenRS} from './internePendenzenRS.rest';

const internePendenzenRSSpy = jasmine.createSpyObj<InternePendenzenRS>(InternePendenzenRS.name,
    ['findInternePendenzenForGesuch']);
const gesuchModelManagerSpy = jasmine.createSpyObj<GesuchModelManager>(GesuchModelManager.name,
    ['getGesuch']);
const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['addMesageAsInfo']);
const applicationPropertyRSSpy = jasmine
    .createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name, ['getActivatedDemoFeatures']);

describe('InternePendenzenComponent', () => {
    let component: InternePendenzenComponent;
    let fixture: ComponentFixture<InternePendenzenComponent>;
    gesuchModelManagerSpy.getGesuch.and.returnValue(createGesuch());
    applicationPropertyRSSpy.getActivatedDemoFeatures.and.returnValue($q.when(''));

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                SharedModule,
                HttpClientModule
            ],
            declarations: [InternePendenzenComponent],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: InternePendenzenRS, useValue: internePendenzenRSSpy},
                {provide: ApplicationPropertyRS, useValue: applicationPropertyRSSpy}
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(InternePendenzenComponent);
        component = fixture.componentInstance;
        internePendenzenRSSpy.findInternePendenzenForGesuch.and.returnValue(of([]));
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    function createGesuch(): TSGesuch {
        const gesuch = new TSGesuch();
        gesuch.markiertFuerKontroll = false;
        return gesuch;
    }
});
