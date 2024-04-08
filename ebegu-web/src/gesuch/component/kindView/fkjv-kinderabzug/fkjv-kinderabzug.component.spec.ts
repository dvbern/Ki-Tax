/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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
import * as moment from 'moment';
import {of} from 'rxjs';
import {TSGesuchsperiode} from '../../../../models/TSGesuchsperiode';
import {TSKind} from '../../../../models/TSKind';
import {TSKindContainer} from '../../../../models/TSKindContainer';
import {TSDateRange} from '../../../../models/types/TSDateRange';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {KinderabzugExchangeService} from '../service/kinderabzug-exchange.service';

import {FkjvKinderabzugComponent} from './fkjv-kinderabzug.component';

const gesuchModelManagerSpy = jasmine.createSpyObj<GesuchModelManager>(
    GesuchModelManager.name, ['getGesuch', 'getGesuchsperiode']
);
const fkjvExchangeServiceSpy = jasmine.createSpyObj<KinderabzugExchangeService>(
    KinderabzugExchangeService.name,
    ['getFormValidationTriggered$', 'getGeburtsdatumChanged$']
);

describe('FkjvKinderabzugComponent', () => {
    let component: FkjvKinderabzugComponent;
    let fixture: ComponentFixture<FkjvKinderabzugComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FkjvKinderabzugComponent],
            providers: [
                {provide: KinderabzugExchangeService, useValue: fkjvExchangeServiceSpy},
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy}
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fkjvExchangeServiceSpy.getGeburtsdatumChanged$.and.returnValue(of(moment()));
        fkjvExchangeServiceSpy.getFormValidationTriggered$.and.returnValue(of(null));
        const gp = new TSGesuchsperiode();
        gp.gueltigkeit = new TSDateRange();
        gesuchModelManagerSpy.getGesuchsperiode.and.returnValue(gp);
        fixture = TestBed.createComponent(FkjvKinderabzugComponent);
        component = fixture.componentInstance;
        component.kindContainer = new TSKindContainer();
        component.kindContainer.kindJA = new TSKind();
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
