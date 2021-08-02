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
/* tslint:disable:no-magic-numbers */
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedComponent';
import {SharedModule} from '../../../../shared/shared.module';
import {TSFerienbetreuungBerechnung} from '../TSFerienbetreuungBerechnung';

import {FerienbetreuungBerechnungComponent} from './ferienbetreuung-berechnung.component';

describe('FerienbetreuungBerechnungComponent', () => {
    let component: FerienbetreuungBerechnungComponent;
    let fixture: ComponentFixture<FerienbetreuungBerechnungComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FerienbetreuungBerechnungComponent],
            imports: [
                HttpClientModule,
                SharedModule
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(FerienbetreuungBerechnungComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should calculate 1440 CHF, 960 CHF and \"true\"', () => {
        const berechnung = new TSFerienbetreuungBerechnung();
        berechnung.personalkosten = 2400;
        berechnung.sachkosten = 500;
        berechnung.verpflegungskosten = 500;
        berechnung.weitereKosten = 100;
        berechnung.anzahlBetreuungstageKinderBern = 45;
        berechnung.betreuungstageKinderDieserGemeinde = 30;
        berechnung.betreuungstageKinderDieserGemeindeSonderschueler = 2;
        berechnung.betreuungstageKinderAndererGemeinde = 15;
        berechnung.betreuungstageKinderAndererGemeindenSonderschueler = 1;
        berechnung.einnahmenElterngebuehren = 1200;

        berechnung.calculate();
        expect(berechnung.totalKantonsbeitrag).toEqual(1440);
        expect(berechnung.beitragFuerKinderDerAnbietendenGemeinde).toEqual(960);
        expect(berechnung.beteiligungDurchAnbietendeGemeinde).toEqual(860);
        expect(berechnung.beteiligungZuTief).toBeTrue();
    });

    it('should return 0, throw no error and beteiligungZuTief should be "false" '
        + 'if no values are given', () => {
        const berechnung = new TSFerienbetreuungBerechnung();
        berechnung.calculate();
        expect(berechnung.totalKantonsbeitrag).toEqual(0);
        expect(berechnung.beitragFuerKinderDerAnbietendenGemeinde).toEqual(0);
        expect(berechnung.beteiligungDurchAnbietendeGemeinde).toEqual(0);
        expect(berechnung.beteiligungZuTief).toBeFalse();
    });
});