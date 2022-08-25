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
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {of} from 'rxjs';
import {BenutzerRSX} from '../../../../app/core/service/benutzerRSX.rest';
import {InstitutionRS} from '../../../../app/core/service/institutionRS.rest';
import {SozialdienstRS} from '../../../../app/core/service/SozialdienstRS.rest';
import {TraegerschaftRS} from '../../../../app/core/service/traegerschaftRS.rest';
import {SharedModule} from '../../../../app/shared/shared.module';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedComponent';

import {BenutzerListXComponent} from './benutzer-list-x.component';

describe('DvBenutzerListXComponent', () => {
    let component: BenutzerListXComponent;
    let fixture: ComponentFixture<BenutzerListXComponent>;

    const authServiceRSSpy = jasmine.createSpyObj(AuthServiceRS.name, [
        'isOneOfRoles',
        'getVisibleRolesForPrincipal'
    ]);
    const institutionRSSpy = jasmine.createSpyObj(InstitutionRS.name, ['getInstitutionenEditableForCurrentBenutzer']);
    const traegerschaftRSSpy = jasmine.createSpyObj(TraegerschaftRS.name, ['getAllTraegerschaften']);
    const sozialdienstRSSpy = jasmine.createSpyObj(SozialdienstRS.name, ['getSozialdienstList']);
    const gemeindeRSSpy = jasmine.createSpyObj(GemeindeRS.name, ['getGemeindenForPrincipal$']);
    const benutzerRSSpy = jasmine.createSpyObj(BenutzerRSX.name, ['searchUsers']);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [BenutzerListXComponent],
            providers: [
                {provide: AuthServiceRS, useValue: authServiceRSSpy},
                {provide: InstitutionRS, useValue: institutionRSSpy},
                {provide: TraegerschaftRS, useValue: traegerschaftRSSpy},
                {provide: SozialdienstRS, useValue: sozialdienstRSSpy},
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
                {provide: BenutzerRSX, useValue: benutzerRSSpy},
            ],
            imports: [
                SharedModule,
                NoopAnimationsModule,
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        benutzerRSSpy.searchUsers.and.returnValue(of([]).toPromise());
        fixture = TestBed.createComponent(BenutzerListXComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
