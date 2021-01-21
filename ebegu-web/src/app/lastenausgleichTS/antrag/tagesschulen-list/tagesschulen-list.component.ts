import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {TagesschuleAngabenRS} from '../../services/tagesschule-angaben.service.rest';

@Component({
    selector: 'dv-tagesschulen-list',
    templateUrl: './tagesschulen-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TagesschulenListComponent implements OnInit {

    @Input() public lastenausgleichId: string;

    public data: { institutionName: string; status: string }[];
    public tableColumns = [
        {displayedName: 'Tagesschule', attributeName: 'institutionName'},
        {displayedName: 'STATUS', attributeName: 'status'},
    ];

    public constructor(
        private readonly tagesschuleAngabenService: TagesschuleAngabenRS,
        private readonly cd: ChangeDetectorRef,
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService,
    ) {
    }

    public ngOnInit(): void {
        this.tagesschuleAngabenService.getAllVisibleTagesschulenAngabenForTSLastenausgleich(this.lastenausgleichId)
            .subscribe(data => {
                this.data = data.map(latsInstitutionContainer => {
                        return {
                            institutionName: latsInstitutionContainer.institution.name,
                            status: `LATS_STATUS_${latsInstitutionContainer.status}`,
                        };
                    },
                );
                this.cd.markForCheck();
            }, () => {
                this.translate.get('DATA_RETRIEVAL_ERROR')
                    .subscribe(msg => this.errorService.addMesageAsError(msg),
                        err => console.error('Error loading translation', err));
            });
    }
}
