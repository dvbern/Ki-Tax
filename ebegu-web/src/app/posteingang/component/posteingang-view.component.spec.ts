/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {UpgradeModule} from '@angular/upgrade/static';
import {TranslateModule} from '@ngx-translate/core';
import {TransitionService} from '@uirouter/angular';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSMitteilungStatus} from '../../../models/enums/TSMitteilungStatus';
import {TSMitteilungTeilnehmerTyp} from '../../../models/enums/TSMitteilungTeilnehmerTyp';
import {TSRole} from '../../../models/enums/TSRole';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSDossier} from '../../../models/TSDossier';
import {TSFall} from '../../../models/TSFall';
import {TSMitteilung} from '../../../models/TSMitteilung';
import {TSMtteilungSearchresultDTO} from '../../../models/TSMitteilungSearchresultDTO';
import {BenutzerRSX} from '../../core/service/benutzerRSX.rest';
import {MitteilungRS} from '../../core/service/mitteilungRS.rest';
import {MaterialModule} from '../../shared/material.module';
import {StateStoreService} from '../../shared/services/state-store.service';
import {PosteingangViewComponent} from './posteingang-view.component';

describe('PosteingangViewComponent', () => {
    let component: PosteingangViewComponent;
    let fixture: ComponentFixture<PosteingangViewComponent>;
    const authRSSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
        ['isRole', 'isOneOfRoles']);
    const mitteilungRSSpy = jasmine.createSpyObj<MitteilungRS>(MitteilungRS.name, ['searchMitteilungen']);
    const gemeindeRSSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, ['getGemeindenForPrincipal$']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
    const transitionServiceSpy = jasmine.createSpyObj<TransitionService>(TransitionService.name,
        ['onStart']);
    const stateStoreServiceSpy = jasmine.createSpyObj<StateStoreService>(StateStoreService.name,
        ['has', 'get']);
    const uiRouterGlobals = jasmine.createSpyObj<UIRouterGlobals>(UIRouterGlobals.name,
        ['$current']);
    const benutzerSpy = jasmine.createSpyObj<BenutzerRSX>(BenutzerRSX.name, ['getAllBenutzerBgTsOrGemeinde']);
    authRSSpy.principal$ = of(new TSBenutzer());

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [PosteingangViewComponent],
            imports: [MaterialModule, TranslateModule.forRoot(), UpgradeModule, BrowserAnimationsModule],
            providers: [
                {provide: MitteilungRS, useValue: mitteilungRSSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: AuthServiceRS, useValue: authRSSpy},
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
                {provide: TransitionService, useValue: transitionServiceSpy},
                {provide: StateStoreService, useValue: stateStoreServiceSpy},
                {provide: UIRouterGlobals, useValue: uiRouterGlobals},
                {provide: BenutzerRSX, useValue: benutzerSpy}
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA]
        }).compileComponents();
        gemeindeRSSpy.getGemeindenForPrincipal$.and.returnValue(of([]));
        benutzerSpy.getAllBenutzerBgTsOrGemeinde.and.returnValue(Promise.resolve([]));
        mockGetMitteilung();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(PosteingangViewComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    function mockGetMitteilung(): void {
        const mockFall = new TSFall();
        // eslint-disable-next-line no-magic-numbers
        mockFall.fallNummer = 123;
        const mockDossier = new TSDossier();
        mockDossier.fall = mockFall;
        const gesuchsteller = new TSBenutzer();
        gesuchsteller.currentBerechtigung.role = TSRole.GESUCHSTELLER;
        const result = new TSMitteilung(mockDossier,
            undefined,
            undefined,
            TSMitteilungTeilnehmerTyp.GESUCHSTELLER,
            TSMitteilungTeilnehmerTyp.JUGENDAMT,
            gesuchsteller,
            undefined,
            'Frage',
            'Warum ist die Banane krumm?',
            TSMitteilungStatus.NEU,
            undefined);
        const dtoList = [result];
        mitteilungRSSpy.searchMitteilungen.and.returnValue(Promise.resolve(new TSMtteilungSearchresultDTO(dtoList, 1)));
    }
});
