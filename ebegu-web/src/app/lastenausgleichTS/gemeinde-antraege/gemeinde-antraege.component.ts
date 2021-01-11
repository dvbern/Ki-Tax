import {Component, OnInit, ChangeDetectionStrategy, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, NgForm, Validators} from '@angular/forms';
import {StateService} from '@uirouter/core';
import {Observable} from 'rxjs';
import {map, tap} from 'rxjs/operators';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {GesuchsperiodeRS} from '../../core/service/gesuchsperiodeRS.rest';
import {DVAntragListItem} from '../../shared/interfaces/DVAntragListItem';
import {GemeindeAntragService} from '../services/gemeinde-antrag.service';

@Component({
    selector: 'dv-gemeinde-antraege',
    templateUrl: './gemeinde-antraege.component.html',
    styleUrls: ['./gemeinde-antraege.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeAntraegeComponent implements OnInit {

    @ViewChild(NgForm) public form: NgForm;

    public hiddenDVTableColumns = [
        'fallNummer',
        'familienName',
        'kinder',
        'aenderungsdatum',
        'dokumenteHochgeladen',
        'angebote',
        'institutionen',
        'verantwortlicheTS',
        'verantwortlicheBG',
    ];

    public antragList$: Observable<DVAntragListItem[]>;
    public gesuchsperioden: TSGesuchsperiode[];
    public formGroup: FormGroup;
    public totalItems: number;

    public constructor(
        public readonly gemeindeAntragService: GemeindeAntragService,
        private readonly gesuchsperiodenService: GesuchsperiodeRS,
        private readonly fb: FormBuilder,
        private readonly $state: StateService
    ) {
    }

    public ngOnInit(): void {
        this.loadData();
        this.gesuchsperiodenService.getAllActiveGesuchsperioden().then(result => this.gesuchsperioden = result);
        this.formGroup = this.fb.group({
            periode: ['', Validators.required],
            antragTyp: ['', Validators.required]
        });
    }

    private loadData(): void {
        this.antragList$ = this.gemeindeAntragService.getAllGemeindeAntraege().pipe(
            map(gemeindeAntraege => {
                return gemeindeAntraege.map(antrag => {
                    return {
                        antragId: antrag.id,
                        gemeinde: antrag.gemeinde.name,
                        status: antrag.statusString,
                        periode: antrag.gesuchsperiode.gesuchsperiodeString,
                        antragTyp: antrag.gemeindeAntragTyp,
                    };
                });
            }),
            tap(gemeindeAntraege => this.totalItems = gemeindeAntraege.length)
        );
    }

    public createAntrag(): void {
        if (!this.formGroup.valid) {
           return;
        }
        this.gemeindeAntragService.createAntrag(this.formGroup.value).subscribe(() => {
            this.loadData();
        });
    }

    private navigate(antrag: DVAntragListItem , event: MouseEvent): void {
        const path = 'LASTENAUSGLEICH_TS';
        const navObj = {
            id: antrag.antragId
        };
        if (event.ctrlKey) {
            const url = this.$state.href(path, navObj);
            window.open(url, '_blank');
        } else {
            this.$state.go(path, navObj);
        }
    }
}
