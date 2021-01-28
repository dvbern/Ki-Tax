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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
