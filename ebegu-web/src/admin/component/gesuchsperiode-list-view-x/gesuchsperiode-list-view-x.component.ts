/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnInit
} from '@angular/core';
import {StateService} from '@uirouter/core';
import * as moment from 'moment';
import {CONSTANTS} from '../../../app/core/constants/CONSTANTS';
import {GesuchsperiodeRS} from '../../../app/core/service/gesuchsperiodeRS.rest';
import {DvSimpleTableColumnDefinition} from '../../../app/shared/component/dv-simple-table/dv-simple-table-column-definition';
import {DvSimpleTableConfig} from '../../../app/shared/component/dv-simple-table/dv-simple-table-config';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {AbstractAdminViewX} from '../../abstractAdminViewX';

@Component({
    selector: 'dv-gesuchsperiode-list-view-x',
    templateUrl: './gesuchsperiode-list-view-x.component.html',
    styleUrls: ['./gesuchsperiode-list-view-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GesuchsperiodeListViewXComponent
    extends AbstractAdminViewX
    implements OnInit
{
    public gesuchsperioden: Array<TSGesuchsperiode> = [];
    public jahr: number;

    public simpleTableColumns: DvSimpleTableColumnDefinition[] = [
        {
            displayedName: 'GESUCHSPERIODE_GUELTIG_AB',
            attributeName: 'gueltigAb',
            displayFunction: (d: any) => moment(d).format(CONSTANTS.DATE_FORMAT)
        },
        {
            displayedName: 'GESUCHSPERIODE_GUELTIG_BIS',
            attributeName: 'gueltigBis',
            displayFunction: (d: any) => moment(d).format(CONSTANTS.DATE_FORMAT)
        },
        {displayedName: 'GESUCHSPERIODE_STATUS', attributeName: 'status'}
    ];
    public simpleTableData: GpSimpleTableEntry[];
    public simpleTableConfig: DvSimpleTableConfig = new DvSimpleTableConfig(
        'gueltigAb',
        'desc',
        true
    );

    public constructor(
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly $state: StateService,
        authServiceRS: AuthServiceRS,
        private readonly cd: ChangeDetectorRef
    ) {
        super(authServiceRS);
    }

    public ngOnInit(): void {
        this.initGesuchsperioden();
    }

    private initGesuchsperioden(): void {
        this.gesuchsperiodeRS
            .getAllGesuchsperioden()
            .then((gesuchsperioden: Array<TSGesuchsperiode>) => {
                this.simpleTableData = gesuchsperioden.map(periode =>
                    this.gesuchsperiodeToSimpleTableEntry(periode)
                );
                this.cd.markForCheck();
            });
    }

    private gesuchsperiodeToSimpleTableEntry(
        periode: TSGesuchsperiode
    ): GpSimpleTableEntry {
        return {
            id: periode.id,
            gueltigAb: periode.gueltigkeit.gueltigAb.toDate().getTime(),
            gueltigBis: periode.gueltigkeit.gueltigBis.toDate().getTime(),
            status: periode.status
        };
    }

    public gesuchsperiodeClicked(gesuchsperiodeId: any): void {
        this.$state.go('admin.gesuchsperiode', {
            gesuchsperiodeId
        });
    }

    public createGesuchsperiode(): void {
        this.$state.go('admin.gesuchsperiode', {
            gesuchsperiodeId: undefined
        });
    }
}

interface GpSimpleTableEntry {
    id: string;
    gueltigAb: number;
    gueltigBis: number;
    status: string;
}
