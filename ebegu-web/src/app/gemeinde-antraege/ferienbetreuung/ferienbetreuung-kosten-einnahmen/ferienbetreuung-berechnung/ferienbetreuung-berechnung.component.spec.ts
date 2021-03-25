/* tslint:disable:no-magic-numbers */
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TSFerienbetreuungBerechnung} from '../TSFerienbetreuungBerechnung';

import {FerienbetreuungBerechnungComponent} from './ferienbetreuung-berechnung.component';

describe('FerienbetreuungBerechnungComponent', () => {
    let component: FerienbetreuungBerechnungComponent;
    let fixture: ComponentFixture<FerienbetreuungBerechnungComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FerienbetreuungBerechnungComponent]
        })
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
