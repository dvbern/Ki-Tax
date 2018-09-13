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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MAT_DATE_LOCALE} from '@angular/material';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import ErrorService from '../../../app/core/errors/service/ErrorService';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import GesuchsperiodeRS from '../../../app/core/service/gesuchsperiodeRS.rest';
import UserRS from '../../../app/core/service/userRS.rest';
import ZahlungRS from '../../../app/core/service/zahlungRS.rest';
import {SharedModule} from '../../../app/shared/shared.module';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import GesuchRS from '../../../gesuch/service/gesuchRS.rest';
import {TestFaelleRS} from '../../service/testFaelleRS.rest';
import {TestdatenViewComponent} from './testdatenView';

describe('testdatenView', () => {

    let component: TestdatenViewComponent;
    let fixture: ComponentFixture<TestdatenViewComponent>;

    beforeEach(async(() => {
        const testFaelleRSSpy = jasmine.createSpyObj<TestFaelleRS>(TestFaelleRS.name,
            ['createTestFall', 'createTestFallGS', 'removeFaelleOfGS', 'mutiereFallHeirat',
                'mutiereFallScheidung', 'resetSchulungsdaten', 'deleteSchulungsdaten']);
        testFaelleRSSpy.createTestFall.and.returnValue('idOfCreatedGesuch');
        const userRSSpy = jasmine.createSpyObj<UserRS>(UserRS.name, ['getAllGesuchsteller']);
        userRSSpy.getAllGesuchsteller.and.returnValue(Promise.resolve(true));
        const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['addMesageAsInfo']);
        const gesuchsperiodeRSSpy = jasmine.createSpyObj<GesuchsperiodeRS>(GesuchsperiodeRS.name,
            ['getAllGesuchsperioden', 'removeGesuchsperiode']);
        gesuchsperiodeRSSpy.getAllGesuchsperioden.and.returnValue(Promise.resolve(true));
        const zahlungRSSpy = jasmine.createSpyObj<ZahlungRS>(ZahlungRS.name,
            ['zahlungenKontrollieren', 'deleteAllZahlungsauftraege']);
        const applicationPropertyRSSpy = jasmine.createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name,
            ['isDevMode']);
        applicationPropertyRSSpy.isDevMode.and.returnValue(Promise.resolve(true));
        const gesuchRSSpy = jasmine.createSpyObj<GesuchRS>(GesuchRS.name, ['gesuchVerfuegen']);
        const gemeindeRSSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, ['getAllGemeinden']);
        gemeindeRSSpy.getAllGemeinden.and.returnValue(Promise.resolve(true));

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
            ],
            providers: [
                {provide: TestFaelleRS, useValue: testFaelleRSSpy},
                {provide: UserRS, useValue: userRSSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: GesuchsperiodeRS, useValue: gesuchsperiodeRSSpy},
                {provide: ZahlungRS, useValue: zahlungRSSpy},
                {provide: ApplicationPropertyRS, useValue: applicationPropertyRSSpy},
                {provide: GesuchRS, useValue: gesuchRSSpy},
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
                {provide: MAT_DATE_LOCALE, useValue: 'de-CH'},
            ],
            declarations: [TestdatenViewComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(TestdatenViewComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
