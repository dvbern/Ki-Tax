import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {DVAntragListItem} from '../../shared/interfaces/DVAntragListItem';
import {GemeindeAntragService} from '../services/gemeinde-antrag.service';

@Component({
    selector: 'dv-gemeinde-antraege',
    templateUrl: './gemeinde-antraege.component.html',
    styleUrls: ['./gemeinde-antraege.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeAntraegeComponent implements OnInit {

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

    public constructor(
        private readonly gemeindeAntragService: GemeindeAntragService,
    ) {
    }

    public ngOnInit(): void {
        this.antragList$ = this.gemeindeAntragService.getAllGemeindeAntraege().pipe(
            map(gemeindeAntraege => {
                return gemeindeAntraege.map(antrag => {
                    return {
                        gemeinde: antrag.gemeinde.name,
                        status: antrag.statusString,
                        periode: antrag.gesuchsperiode.gesuchsperiodeString,
                        antragTyp: antrag.gemeindeAntragTyp
                    };
                });
            }),
        )
    }

}
