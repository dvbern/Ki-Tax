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

import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {MAT_DATE_LOCALE} from '@angular/material/core';
import * as moment from 'moment';
import {of} from 'rxjs';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {BenutzerRSX} from '../../../app/core/service/benutzerRSX.rest';
import {GesuchsperiodeRS} from '../../../app/core/service/gesuchsperiodeRS.rest';
import {I18nServiceRSRest} from '../../../app/i18n/services/i18nServiceRS.rest';
import {SharedModule} from '../../../app/shared/shared.module';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {GesuchRS} from '../../../gesuch/service/gesuchRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedDirective';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TestFaelleRS} from '../../service/testFaelleRS.rest';
import {TestdatenViewComponent} from './testdatenView.component';

describe('testdatenView', () => {
    let component: TestdatenViewComponent;
    let fixture: ComponentFixture<TestdatenViewComponent>;

    beforeEach(waitForAsync(() => {
        const testFaelleRSSpy = jasmine.createSpyObj<TestFaelleRS>(
            TestFaelleRS.name,
            [
                'createTestFall',
                'createTestFallGS',
                'removeFaelleOfGS',
                'mutiereFallHeirat',
                'mutiereFallScheidung',
                'resetSchulungsdaten',
                'deleteSchulungsdaten'
            ]
        );
        const benutzerRSSpy = jasmine.createSpyObj<BenutzerRSX>(
            BenutzerRSX.name,
            ['getAllGesuchsteller']
        );
        benutzerRSSpy.getAllGesuchsteller.and.resolveTo([]);
        const errorServiceSpy = jasmine.createSpyObj<ErrorService>(
            ErrorService.name,
            ['addMesageAsInfo']
        );
        const gesuchsperiodeRSSpy = jasmine.createSpyObj<GesuchsperiodeRS>(
            GesuchsperiodeRS.name,
            ['getAllGesuchsperioden', 'removeGesuchsperiode']
        );
        gesuchsperiodeRSSpy.getAllGesuchsperioden.and.resolveTo([]);
        const applicationPropertyRSSpy =
            jasmine.createSpyObj<ApplicationPropertyRS>(
                ApplicationPropertyRS.name,
                [
                    'isDevMode',
                    'getPublicPropertiesCached',
                    'isEbeguKibonAnfrageTestGuiEnabled'
                ]
            );
        applicationPropertyRSSpy.isDevMode.and.resolveTo(false);
        applicationPropertyRSSpy.isEbeguKibonAnfrageTestGuiEnabled.and.resolveTo(
            false
        );
        applicationPropertyRSSpy.getPublicPropertiesCached.and.resolveTo({
            currentNode: '',
            devmode: false,
            whitelist: '',
            dummyMode: false,
            sentryEnvName: '',
            backgroundColor: '#FFFFFF',
            primaryColor: 'blue',
            primaryColorDark: 'black',
            primaryColorLight: 'white',
            logoFileName: 'test.svg',
            logoFileNameWhite: 'test-white.svg',
            zahlungentestmode: false,
            personenSucheDisabled: true,
            kitaxHost: '',
            kitaxEndpoint: '',
            notverordnungDefaultEinreichefristOeffentlich: '',
            notverordnungDefaultEinreichefristPrivat: '',
            ferienbetreuungAktiv: true,
            lastenausgleichAktiv: true,
            lastenausgleichTagesschulenAktiv: true,
            gemeindeKennzahlenAktiv: true,
            mulitmandantAktiv: false,
            angebotTSActivated: true,
            angebotFIActivated: true,
            angebotTFOActivated: true,
            angebotMittagstischActivated: false,
            infomaZahlungen: true,
            frenchEnabled: true,
            geresEnabledForMandant: true,
            ebeguKibonAnfrageTestGuiEnabled: false,
            steuerschnittstelleAktivAb: moment('2020-01-01'),
            zusatzinformationenInstitution: true,
            activatedDemoFeatures: '',
            checkboxAuszahlungInZukunft: false,
            institutionenDurchGemeindenEinladen: false,
            erlaubenInstitutionenZuWaehlen: false,
            auszahlungAnEltern: true,
            abweichungenEnabled: true,
            gemeindeVereinfachteKonfigAktiv: false,
            testfaelleEnabled: false
        });
        const gemeindeRSSpy = jasmine.createSpyObj<GemeindeRS>(
            GemeindeRS.name,
            ['getAktiveGemeinden']
        );
        gemeindeRSSpy.getAktiveGemeinden.and.resolveTo([]);
        const i18nServiceSpy = jasmine.createSpyObj<I18nServiceRSRest>(
            I18nServiceRSRest.name,
            ['extractPreferredLanguage']
        );
        const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(
            AuthServiceRS.name,
            ['principal$']
        );
        authServiceSpy.principal$ = of(new TSBenutzer());
        const gesuchRSSpy = jasmine.createSpyObj<GesuchRS>(GesuchRS.name, [
            'getSteuerdaten'
        ]);

        TestBed.configureTestingModule({
            imports: [SharedModule],
            providers: [
                {provide: TestFaelleRS, useValue: testFaelleRSSpy},
                {provide: BenutzerRSX, useValue: benutzerRSSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: GesuchsperiodeRS, useValue: gesuchsperiodeRSSpy},
                {
                    provide: ApplicationPropertyRS,
                    useValue: applicationPropertyRSSpy
                },
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
                {provide: MAT_DATE_LOCALE, useValue: 'de-CH'},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: GesuchRS, useValue: gesuchRSSpy}
            ],
            declarations: [TestdatenViewComponent]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
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
