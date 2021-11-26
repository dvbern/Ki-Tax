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
import {SharedModule} from '../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedComponent';
import {TSFamiliensituation} from '../../../../../models/TSFamiliensituation';
import {TSFamiliensituationContainer} from '../../../../../models/TSFamiliensituationContainer';
import {TSFinanzielleSituation} from '../../../../../models/TSFinanzielleSituation';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSGesuch} from '../../../../../models/TSGesuch';
import {TSGesuchsteller} from '../../../../../models/TSGesuchsteller';
import {TSGesuchstellerContainer} from '../../../../../models/TSGesuchstellerContainer';
import {FinanzielleSituationRS} from '../../../../service/finanzielleSituationRS.rest';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {FinanzielleSituationLuzernService} from '../finanzielle-situation-luzern.service';

import {FinanzielleSituationStartViewLuzernComponent} from './finanzielle-situation-start-view-luzern.component';

const gesuchModelManagerSpy = jasmine.createSpyObj<GesuchModelManager>(
    GesuchModelManager.name,
    ['areThereOnlyFerieninsel', 'getBasisjahr', 'getBasisjahrPlus', 'getGesuch', 'isGesuchsteller2Required']);
const wizardStepMangerSpy = jasmine.createSpyObj<WizardStepManager>(
    WizardStepManager.name, ['getCurrentStep', 'setCurrentStep']);
const finanzielleSituationRSSpy = jasmine.createSpyObj<FinanzielleSituationRS>(FinanzielleSituationRS.name, []);

FinanzielleSituationLuzernService.finSitNeedsTwoAntragsteller = () => false;

describe('FinanzielleSituationStartViewLuzernComponent', () => {
    let component: FinanzielleSituationStartViewLuzernComponent;
    let fixture: ComponentFixture<FinanzielleSituationStartViewLuzernComponent>;
    const basisjahr = 2020;
    const basisjahrPlus1 = 2021;
    const antragstellerNummer = 1;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [
                FinanzielleSituationStartViewLuzernComponent,
            ],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                {provide: WizardStepManager, useValue: wizardStepMangerSpy},
                {provide: FinanzielleSituationRS, useValue: finanzielleSituationRSSpy},
            ],
            imports: [
                SharedModule,
            ],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        gesuchModelManagerSpy.getGesuch.and.returnValue(createGesuch());
        gesuchModelManagerSpy.isGesuchsteller2Required.and.returnValue(false);
        fixture = TestBed.createComponent(FinanzielleSituationStartViewLuzernComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        gesuchModelManagerSpy.getBasisjahr.and.returnValue(basisjahr);
        gesuchModelManagerSpy.getBasisjahrPlus.and.returnValue(basisjahrPlus1);
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should test "Gemeinsame Veranlagung letztes Jahr"', () => {
        FinanzielleSituationLuzernService.finSitNeedsTwoAntragsteller = () => false;
        setFormValues(false, true, null, true);

        expect(component.showSelbstdeklaration()).toBeFalse();
        expect(component.showVeranlagung()).toBeTrue();
        expect(component.gemeinsameStekVisible()).toBeTrue();
        expect(component.alleinigeStekVisible()).toBeFalse();
        expect(component.getYearForDeklaration()).toBe(basisjahr);
        expect(component.isGemeinsam()).toBeTrue();
        expect(component.getAntragstellerNummer()).toBe(antragstellerNummer);
    });

    it('should test "Alleinige Veranlagung letztes Jahr"', () => {
        FinanzielleSituationLuzernService.finSitNeedsTwoAntragsteller = () => true;
        setFormValues(false, null, true, true);

        expect(component.showSelbstdeklaration()).toBeFalse();
        expect(component.showVeranlagung()).toBeTrue();
        expect(component.gemeinsameStekVisible()).toBeFalse();
        expect(component.alleinigeStekVisible()).toBeTrue();
        expect(component.getYearForDeklaration()).toBe(basisjahr);
        expect(component.isGemeinsam()).toBeFalse();
        expect(component.getAntragstellerNummer()).toBe(antragstellerNummer);
    });

    it('should test "Gemeinsame Selbstdeklaration aktuelles Jahr"', () => {
        FinanzielleSituationLuzernService.finSitNeedsTwoAntragsteller = () => false;
        setFormValues(false, false, null, null);

        expect(component.showSelbstdeklaration()).toBeTrue();
        expect(component.showVeranlagung()).toBeFalse();
        expect(component.gemeinsameStekVisible()).toBeTrue();
        expect(component.alleinigeStekVisible()).toBeFalse();
        expect(component.getYearForDeklaration()).toBe(basisjahrPlus1);
        expect(component.isGemeinsam()).toBeTrue();
        expect(component.getAntragstellerNummer()).toBe(antragstellerNummer);
    });

    it('should test "Alleinige Selbstdeklaration aktuelles Jahr"', () => {
        FinanzielleSituationLuzernService.finSitNeedsTwoAntragsteller = () => true;
        setFormValues(false, null, false, null);

        expect(component.showSelbstdeklaration()).toBeTrue();
        expect(component.showVeranlagung()).toBeFalse();
        expect(component.gemeinsameStekVisible()).toBeFalse();
        expect(component.alleinigeStekVisible()).toBeTrue();
        expect(component.getYearForDeklaration()).toBe(basisjahrPlus1);
        expect(component.isGemeinsam()).toBeFalse();
        expect(component.getAntragstellerNummer()).toBe(antragstellerNummer);
    });

    it('should test "Gemeinsame Selbstdeklaration letztes Jahr (quellenbesteuert)"', () => {
        FinanzielleSituationLuzernService.finSitNeedsTwoAntragsteller = () => false;
        setFormValues(true, null, null, null);

        expect(component.showSelbstdeklaration()).toBeTrue();
        expect(component.showVeranlagung()).toBeFalse();
        expect(component.gemeinsameStekVisible()).toBeFalse();
        expect(component.alleinigeStekVisible()).toBeFalse();
        expect(component.getYearForDeklaration()).toBe(basisjahr);
        expect(component.isGemeinsam()).toBeTrue();
        expect(component.getAntragstellerNummer()).toBe(antragstellerNummer);
    });

    it('should test "Alleinige Selbstdeklaration letztes Jahr (quellenbesteuert)"', () => {
        FinanzielleSituationLuzernService.finSitNeedsTwoAntragsteller = () => true;
        setFormValues(true, null, null, null);

        expect(component.showSelbstdeklaration()).toBeTrue();
        expect(component.showVeranlagung()).toBeFalse();
        expect(component.gemeinsameStekVisible()).toBeFalse();
        expect(component.alleinigeStekVisible()).toBeFalse();
        expect(component.getYearForDeklaration()).toBe(basisjahr);
        expect(component.isGemeinsam()).toBeFalse();
        expect(component.getAntragstellerNummer()).toBe(antragstellerNummer);
    });

    it('should test "Gemeinsame Selbstdeklaration letztes Jahr (nicht veranlagt)"', () => {
        FinanzielleSituationLuzernService.finSitNeedsTwoAntragsteller = () => false;
        setFormValues(false, true, null, false);

        expect(component.showSelbstdeklaration()).toBeTrue();
        expect(component.showVeranlagung()).toBeFalse();
        expect(component.gemeinsameStekVisible()).toBeTrue();
        expect(component.alleinigeStekVisible()).toBeFalse();
        expect(component.getYearForDeklaration()).toBe(basisjahr);
        expect(component.isGemeinsam()).toBeTrue();
        expect(component.getAntragstellerNummer()).toBe(antragstellerNummer);
    });

    it('should test "Alleinige Selbstdeklaration letztes Jahr (nicht veranlagt)"', () => {
        FinanzielleSituationLuzernService.finSitNeedsTwoAntragsteller = () => true;
        setFormValues(false, null, true, false);

        expect(component.showSelbstdeklaration()).toBeTrue();
        expect(component.showVeranlagung()).toBeFalse();
        expect(component.gemeinsameStekVisible()).toBeFalse();
        expect(component.alleinigeStekVisible()).toBeTrue();
        expect(component.getYearForDeklaration()).toBe(basisjahr);
        expect(component.isGemeinsam()).toBeFalse();
        expect(component.getAntragstellerNummer()).toBe(antragstellerNummer);
    });

    it('should return empty antragsteller name', () => {
        FinanzielleSituationLuzernService.finSitNeedsTwoAntragsteller = () => false;
        setFormValues(false, true, true, null);
        expect(component.getYearForDeklaration()).toBe('');
    });

    function setFormValues(
        quellenbesteuert: boolean,
        gemeinsameStekVorjahr: boolean,
        alleinigeStekVorjahr: boolean,
        veranlagt: boolean,
    ): void {
        component.getModel().finanzielleSituationJA.quellenbesteuert = quellenbesteuert;
        component.getModel().finanzielleSituationJA.gemeinsameStekVorjahr = gemeinsameStekVorjahr;
        component.getModel().finanzielleSituationJA.alleinigeStekVorjahr = alleinigeStekVorjahr;
        component.getModel().finanzielleSituationJA.veranlagt = veranlagt;
    }

    function createGesuch(): TSGesuch {
        const gesuch = new TSGesuch();
        gesuch.gesuchsteller1 = new TSGesuchstellerContainer();
        gesuch.gesuchsteller1.gesuchstellerJA = new TSGesuchsteller();
        gesuch.gesuchsteller2 = new TSGesuchstellerContainer();
        gesuch.gesuchsteller2.gesuchstellerJA = new TSGesuchsteller();
        gesuch.gesuchsteller1.finanzielleSituationContainer = new TSFinanzielleSituationContainer();
        gesuch.gesuchsteller1.finanzielleSituationContainer.finanzielleSituationJA = new TSFinanzielleSituation();
        gesuch.gesuchsteller2.finanzielleSituationContainer = new TSFinanzielleSituationContainer();
        gesuch.gesuchsteller2.finanzielleSituationContainer.finanzielleSituationJA = new TSFinanzielleSituation();
        gesuch.familiensituationContainer = new TSFamiliensituationContainer();
        gesuch.familiensituationContainer.familiensituationJA = new TSFamiliensituation();
        return gesuch;
    }
});
