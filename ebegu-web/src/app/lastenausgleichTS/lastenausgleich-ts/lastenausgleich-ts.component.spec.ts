/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {MatToolbarModule} from '@angular/material/toolbar';
import {TranslateModule} from '@ngx-translate/core';
import {UIRouterModule} from '@uirouter/angular';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {WindowRef} from '../../core/service/windowRef.service';
import {SharedModule} from '../../shared/shared.module';
import {LastenausgleichTsKommentarComponent} from '../lastenausgleich-ts-kommentar/lastenausgleich-ts-kommentar.component';
import {LastenausgleichTsSideNavComponent} from '../lastenausgleich-ts-side-nav/lastenausgleich-ts-side-nav.component';
import {LastenausgleichTsToolbarComponent} from '../lastenausgleich-ts-toolbar/lastenausgleich-ts-toolbar.component';

import {LastenausgleichTSComponent} from './lastenausgleich-ts.component';

describe('LastenausgleichTSComponent', () => {
    let component: LastenausgleichTSComponent;
    let fixture: ComponentFixture<LastenausgleichTSComponent>;
    const authServiceRSSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isOneOfRoles']);

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            providers: [
                WindowRef,
                {
                    provide: AuthServiceRS,
                    useValue: authServiceRSSpy,
                },
            ],
            declarations: [
                LastenausgleichTSComponent,
                LastenausgleichTsKommentarComponent,
                LastenausgleichTsSideNavComponent,
                LastenausgleichTsToolbarComponent,
            ],
            imports: [
                SharedModule,
                UIRouterModule.forRoot({useHash: true}),
                TranslateModule,
                MatToolbarModule
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(LastenausgleichTSComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
