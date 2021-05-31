import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {TSLastenausgleichTagesschulenStatusHistory} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschulenStatusHistory';
import {CONSTANTS} from '../../../../core/constants/CONSTANTS';
import {ErrorService} from '../../../../core/errors/service/ErrorService';
import {LastenausgleichTSService} from '../../../lastenausgleich-ts/services/lastenausgleich-ts.service';

@Component({
    selector: 'dv-verlauf',
    templateUrl: './verlauf.component.html',
    styleUrls: ['./verlauf.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class VerlaufComponent implements OnInit {

    @Input() public lastenausgleichId: string;

    public history: {timestampVon: string, status: string, benutzer: string}[];
    public columns = [
        {displayedName: 'DATUM', attributeName: 'timestampVon'},
        {displayedName: 'AKTION', attributeName: 'status'},
        {displayedName: 'BENUTZER', attributeName: 'benutzer'},
    ];

    public constructor(
        private readonly lastenausgleichTSService: LastenausgleichTSService,
        private readonly errorService: ErrorService,
        private readonly $translate: TranslateService,
        private readonly cd: ChangeDetectorRef
    ) {
    }

    public ngOnInit(): void {
        this.lastenausgleichTSService.getVerlauf(this.lastenausgleichId)
            .subscribe(data => {
                console.log(data);
                this.mapHistoryForSimpleTable(data);
                this.cd.markForCheck();
            }, error => {
                this.errorService.addMesageAsError(
                    this.$translate.instant('ERROR_UNEXPECTED')
                );
                console.error(error);
            });
    }

    private mapHistoryForSimpleTable(data: TSLastenausgleichTagesschulenStatusHistory[]): void {
        this.history = data.map(d => {
            return {
                timestampVon: d.timestampVon.format(CONSTANTS.DATE_TIME_FORMAT),
                status: d.status,
                benutzer: d.benutzer.getFullName()
            };
        });
    }
}
