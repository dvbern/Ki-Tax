import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {combineLatest} from 'rxjs';
import {startWith} from 'rxjs/operators';
import {TSFerienbetreuungAngabenContainer} from '../../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {LogFactory} from '../../../../core/logging/LogFactory';
import {FerienbetreuungService} from '../../services/ferienbetreuung.service';
import {TSFerienbetreuungBerechnung} from '../TSFerienbetreuungBerechnung';

const LOG = LogFactory.createLog('FerienbetreuungBerechnungComponent');

@Component({
    selector: 'dv-ferienbetreuung-berechnung',
    templateUrl: './ferienbetreuung-berechnung.component.html',
    styleUrls: ['./ferienbetreuung-berechnung.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungBerechnungComponent implements OnInit {

    @Input()
    private form: FormGroup;

    @Input()
    private container: TSFerienbetreuungAngabenContainer;

    public berechnung: TSFerienbetreuungBerechnung;

    public constructor(
        private readonly ferienbetreuungService: FerienbetreuungService,
        private readonly cd: ChangeDetectorRef
    ) {
    }

    public ngOnInit(): void {
        this.berechnung = new TSFerienbetreuungBerechnung();
        this.ferienbetreuungService.getFerienbetreuungContainer()
            .subscribe(container => {
                this.container = container;
                this.setUpValuesFromContainer();
            }, error => {
                LOG.error(error);
            });
        this.setUpValuesFromForm();
    }

    private setUpValuesFromForm(): void {
        const angaben = this.container?.angabenDeklaration;
        combineLatest([
            this.form.get('personalkosten').valueChanges.pipe(
                startWith(angaben?.kostenEinnahmen.personalkosten),
            ),
            this.form.get('sachkosten').valueChanges.pipe(
                startWith(angaben?.kostenEinnahmen.sachkosten),
            ),
            this.form.get('verpflegungskosten').valueChanges.pipe(
                startWith(angaben?.kostenEinnahmen.verpflegungskosten),
            ),
            this.form.get('weitereKosten').valueChanges.pipe(
                startWith(angaben?.kostenEinnahmen.weitereKosten),
            ),
            this.form.get('elterngebuehren').valueChanges.pipe(
                startWith(angaben?.kostenEinnahmen.elterngebuehren),
            ),
            this.form.get('weitereEinnahmen').valueChanges.pipe(
                startWith(angaben?.kostenEinnahmen.weitereEinnahmen),
            )
        ]).subscribe(formValues => {
            this.berechnung.personalkosten = formValues[0];
            this.berechnung.sachkosten = formValues[1];
            this.berechnung.verpflegungskosten = formValues[2];
            this.berechnung.weitereKosten = formValues[3];
            this.berechnung.einnahmenElterngebuehren = formValues[4];
            this.berechnung.weitereEinnahmen = formValues[5];

            this.calculate();
        });
    }

    private setUpValuesFromContainer(): void {
        const angaben = this.container?.angabenDeklaration;
        this.berechnung.anzahlBetreuungstageKinderBern = angaben?.nutzung?.anzahlBetreuungstageKinderBern;
        this.berechnung.betreuungstageKinderDieserGemeinde = angaben?.nutzung?.betreuungstageKinderDieserGemeinde;
        this.berechnung.betreuungstageKinderDieserGemeindeSonderschueler =
            angaben?.nutzung?.betreuungstageKinderDieserGemeindeSonderschueler;
        this.berechnung.betreuungstageKinderAndererGemeinde =
            angaben?.nutzung?.davonBetreuungstageKinderAndererGemeinden;
        this.berechnung.betreuungstageKinderAndererGemeindenSonderschueler =
            angaben?.nutzung?.davonBetreuungstageKinderAndererGemeindenSonderschueler;

        this.calculate();
    }

    private calculate(): void {
        this.berechnung.calculate();
        this.cd.markForCheck();
    }
}
