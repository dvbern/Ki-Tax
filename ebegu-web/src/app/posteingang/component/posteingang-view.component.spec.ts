/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {Directive, EventEmitter, Input, Output} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {UpgradeModule} from '@angular/upgrade/static';
import {TranslateModule} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSMitteilungStatus} from '../../../models/enums/TSMitteilungStatus';
import {TSMitteilungTeilnehmerTyp} from '../../../models/enums/TSMitteilungTeilnehmerTyp';
import {TSRole} from '../../../models/enums/TSRole';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSBenutzerNoDetails} from '../../../models/TSBenutzerNoDetails';
import {TSDossier} from '../../../models/TSDossier';
import {TSFall} from '../../../models/TSFall';
import {TSMtteilungSearchresultDTO} from '../../../models/TSMitteilungSearchresultDTO';
import {TSMitteilung} from '../../../models/TSMitteilung';
import {MitteilungRS} from '../../core/service/mitteilungRS.rest';
import {MaterialModule} from '../../shared/material.module';
import {PosteingangViewComponent} from './posteingang-view.component';

@Directive({
    selector: '[dvNewUserSelect]',
})
class MockNewUserSelectDirective {
    @Input()
    public showSelectionAll: boolean;

    @Input()
    public angular2: boolean;

    @Input()
    public inputId: string;

    @Input()
    public dvUsersearch: string;

    @Input()
    public initialAll: boolean;

    @Input()
    public selectedUser: TSBenutzerNoDetails;

    @Input()
    public sachbearbeiterGemeinde: boolean;

    @Input()
    public schulamt: boolean;

    @Output()
    public readonly userChanged: EventEmitter<{ user: TSBenutzerNoDetails }> = new EventEmitter<{ user: TSBenutzerNoDetails }>();
}

describe('PosteingangViewComponent', () => {
    let component: PosteingangViewComponent;
    let fixture: ComponentFixture<PosteingangViewComponent>;
    const authRSSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
        ['isRole', 'isOneOfRoles']);
    const mitteilungRSSpy = jasmine.createSpyObj<MitteilungRS>(MitteilungRS.name, ['searchMitteilungen']);
    const gemeindeRSSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, ['getGemeindenForPrincipal$']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [PosteingangViewComponent, MockNewUserSelectDirective],
            imports: [MaterialModule, TranslateModule.forRoot(), UpgradeModule, BrowserAnimationsModule],
            providers: [
                {provide: MitteilungRS, useValue: mitteilungRSSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: AuthServiceRS, useValue: authRSSpy},
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
            ],
        }).compileComponents();
        gemeindeRSSpy.getGemeindenForPrincipal$.and.returnValue(of([]));
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
        // tslint:disable-next-line:no-magic-numbers
        mockFall.fallNummer = 123;
        const mockDossier = new TSDossier();
        mockDossier.fall = mockFall;
        const gesuchsteller = new TSBenutzer();
        gesuchsteller.currentBerechtigung.role = TSRole.GESUCHSTELLER;
        const result = new TSMitteilung(mockDossier,
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
