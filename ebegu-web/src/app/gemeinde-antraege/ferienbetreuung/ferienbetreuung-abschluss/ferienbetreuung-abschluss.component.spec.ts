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
import {StateService} from '@uirouter/core';
import {of} from 'rxjs';
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedComponent';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSBenutzer} from '../../../../models/TSBenutzer';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {DownloadRS} from '../../../core/service/downloadRS.rest';
import {SharedModule} from '../../../shared/shared.module';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

import {FerienbetreuungAbschlussComponent} from './ferienbetreuung-abschluss.component';

describe('FerienbetreuungAbschlussComponent', () => {
    let component: FerienbetreuungAbschlussComponent;
    let fixture: ComponentFixture<FerienbetreuungAbschlussComponent>;
    const dummyUser = new TSBenutzer();

    const ferienbetreuungServiceSpy = jasmine.createSpyObj<FerienbetreuungService>(
        FerienbetreuungService.name,
        ['getFerienbetreuungContainer'],
    );
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name,
        ['addMesageAsError', 'addMesageAsInfo']);

    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
        ['principal$', 'isOneOfRoles']);

    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name,
        ['go']);

    const einstellungRSSpy = jasmine.createSpyObj<EinstellungRS>(EinstellungRS.name,
        ['getPauschalbetraegeFerienbetreuung']);

    const downloadRSSpy = jasmine.createSpyObj<DownloadRS>(DownloadRS.name,
        ['openDownload']);

    const container = new TSFerienbetreuungAngabenContainer();
    container.angabenDeklaration = null;
    authServiceSpy.principal$ = of(dummyUser);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SharedModule],
            providers: [
                {provide: FerienbetreuungService, useValue: ferienbetreuungServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: EinstellungRS, useValue: einstellungRSSpy},
                {provide: DownloadRS, useValue: downloadRSSpy},
            ],
            declarations: [FerienbetreuungAbschlussComponent],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(FerienbetreuungAbschlussComponent);
        ferienbetreuungServiceSpy.getFerienbetreuungContainer.and.returnValue(of(container));
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
