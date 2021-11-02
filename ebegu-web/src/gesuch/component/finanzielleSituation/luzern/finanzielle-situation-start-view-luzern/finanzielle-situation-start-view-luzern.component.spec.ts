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
import {FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedComponent';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {FinanzielleSituationLuzernService} from '../finanzielle-situation-luzern.service';

import {FinanzielleSituationStartViewLuzernComponent} from './finanzielle-situation-start-view-luzern.component';

const gesuchModelManagerSpy = jasmine.createSpyObj<GesuchModelManager>(
    GesuchModelManager.name, ['areThereOnlyFerieninsel', 'getBasisjahr', 'getBasisjahrPlus']);

FinanzielleSituationLuzernService.finSitNeedsTwoAntragsteller = () => false;

describe('FinanzielleSituationStartViewLuzernComponent', () => {
    let component: FinanzielleSituationStartViewLuzernComponent;
    let fixture: ComponentFixture<FinanzielleSituationStartViewLuzernComponent>;
    const basisjahr = 2020;
    const basisjahrPlus1 = 2021;
    const antragstellerNummer = 1;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FinanzielleSituationStartViewLuzernComponent],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
            ],
            imports: [
                FormsModule,
                ReactiveFormsModule,
                SharedModule
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
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
        setFormValues(component.form, false, true, null, true);

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
        setFormValues(component.form, false, null, true, true);

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
        setFormValues(component.form, false, false, null, null);

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
        setFormValues(component.form, false, null, false, null);

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
        setFormValues(component.form, true, null, null, null);

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
        setFormValues(component.form, true, null, null, null);

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
        setFormValues(component.form, false, true, null, false);

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
        setFormValues(component.form, false, null, true, false);

        expect(component.showSelbstdeklaration()).toBeTrue();
        expect(component.showVeranlagung()).toBeFalse();
        expect(component.gemeinsameStekVisible()).toBeFalse();
        expect(component.alleinigeStekVisible()).toBeTrue();
        expect(component.getYearForDeklaration()).toBe(basisjahr);
        expect(component.isGemeinsam()).toBeFalse();
        expect(component.getAntragstellerNummer()).toBe(antragstellerNummer);
    });

    it('should throw error', () => {
        FinanzielleSituationLuzernService.finSitNeedsTwoAntragsteller = () => false;
        setFormValues(component.form, false, true, true, null);

        expect(() => component.getYearForDeklaration())
            .toThrow(new Error('Dieser Fall ist nicht abgedeckt: ' + JSON.stringify(component.form.value)));
    });

    function setFormValues(
        form: FormGroup,
        quellenbesteuert: boolean,
        gemeinsameStekVorjahr: boolean,
        alleinigeStekVorjahr: boolean,
        veranlagt: boolean
    ): void {
        form.controls.quellenbesteuert.setValue(quellenbesteuert);
        form.controls.gemeinsameStekVorjahr.setValue(gemeinsameStekVorjahr);
        form.controls.alleinigeStekVorjahr.setValue(alleinigeStekVorjahr);
        form.controls.veranlagt.setValue(veranlagt);
    }
});
