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
import {ReactiveFormsModule} from '@angular/forms';
import {of} from 'rxjs';
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedDirective';
import {TSFerienbetreuungAngaben} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngaben';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {BenutzerRSX} from '../../../core/service/benutzerRSX.rest';
import {SharedModule} from '../../../shared/shared.module';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';
import {FerienbetreuungKommantarComponent} from './ferienbetreuung-kommantar.component';

const authServiceRSSpy = jasmine.createSpyObj<AuthServiceRS>(
    AuthServiceRS.name,
    ['isOneOfRoles']
);

const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, [
    'addMesageAsError'
]);

const einstellungRSSpy = jasmine.createSpyObj<EinstellungRS>(
    EinstellungRS.name,
    ['getPauschalbetraegeFerienbetreuung']
);

const benuzerRSSpy = jasmine.createSpyObj<BenutzerRSX>(BenutzerRSX.name, [
    'getAllActiveBenutzerMandant'
]);

const fbServiceSpy = jasmine.createSpyObj<FerienbetreuungService>(
    FerienbetreuungService.name,
    ['getFerienbetreuungContainer']
);

const tsFerienbetreuungAngabenContainer: TSFerienbetreuungAngabenContainer =
    new TSFerienbetreuungAngabenContainer();
tsFerienbetreuungAngabenContainer.angabenDeklaration =
    new TSFerienbetreuungAngaben();
tsFerienbetreuungAngabenContainer.angabenKorrektur =
    new TSFerienbetreuungAngaben();
fbServiceSpy.getFerienbetreuungContainer.and.returnValue(
    of(tsFerienbetreuungAngabenContainer)
);

describe('FerienbetreuungKommantarComponent', () => {
    let component: FerienbetreuungKommantarComponent;
    let fixture: ComponentFixture<FerienbetreuungKommantarComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FerienbetreuungKommantarComponent],
            providers: [
                {provide: AuthServiceRS, useValue: authServiceRSSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: EinstellungRS, useValue: einstellungRSSpy},
                {provide: BenutzerRSX, useValue: benuzerRSSpy}
            ],
            imports: [ReactiveFormsModule, SharedModule, HttpClientModule]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(FerienbetreuungKommantarComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
