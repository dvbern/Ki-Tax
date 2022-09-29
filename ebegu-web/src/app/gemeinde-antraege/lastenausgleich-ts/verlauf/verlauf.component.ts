import {Location} from '@angular/common';
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import * as moment from 'moment';
import {TSLastenausgleichTagesschulenStatusHistory} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschulenStatusHistory';
import {CONSTANTS} from '../../../core/constants/CONSTANTS';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {DvSimpleTableColumnDefinition} from '../../../shared/component/dv-simple-table/dv-simple-table-column-definition';
import {DvSimpleTableConfig} from '../../../shared/component/dv-simple-table/dv-simple-table-config';
import {LastenausgleichTSService} from '../services/lastenausgleich-ts.service';

const LOG = LogFactory.createLog('VerlaufComponent');

@Component({
    selector: 'dv-verlauf',
    templateUrl: './verlauf.component.html',
    styleUrls: ['./verlauf.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class VerlaufComponent implements OnInit {

    @Input() public lastenausgleichId: string;

    public history: {timestampVon: number; status: string; benutzer: string}[];
    public columns: DvSimpleTableColumnDefinition[] = [
        {
            displayedName: 'DATUM',
            attributeName: 'timestampVon',
            displayFunction: (d: number) => moment(d).format(CONSTANTS.DATE_TIME_FORMAT)
        },
        {
            displayedName: 'AKTION',
            attributeName: 'status'
        },
        {
            displayedName: 'BENUTZER',
            attributeName: 'benutzer'
        }
    ];
    public tableConfig = new DvSimpleTableConfig('timestampVon', 'desc', false);

    public constructor(
        private readonly lastenausgleichTSService: LastenausgleichTSService,
        private readonly errorService: ErrorService,
        private readonly $translate: TranslateService,
        private readonly cd: ChangeDetectorRef,
        private readonly location: Location
    ) {
    }

    public ngOnInit(): void {
        this.lastenausgleichTSService.getVerlauf(this.lastenausgleichId)
            .subscribe(data => {
                console.log(data);
                this.mapHistoryForSimpleTable(data);
                this.cd.markForCheck();
            }, error => {
                LOG.error(error);
            });
    }

    private mapHistoryForSimpleTable(data: TSLastenausgleichTagesschulenStatusHistory[]): void {
        this.history = data.map(d => ({
                timestampVon: d.timestampVon.toDate().getTime(),
                status: d.status,
                benutzer: d.benutzer.getFullName()
            }));
    }

    public navigateBack(): void {
        this.location.back();
    }
}
