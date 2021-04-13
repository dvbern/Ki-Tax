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
import {StateService} from '@uirouter/core';
import {ErrorService} from '../../../../core/errors/service/ErrorService';
import {TagesschuleAngabenRS} from '../../../lastenausgleich-ts/services/tagesschule-angaben.service.rest';
import {GemeindeAntragService} from '../../../services/gemeinde-antrag.service';

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
        private readonly gemeindeAntragService: GemeindeAntragService,
        private readonly cd: ChangeDetectorRef,
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService,
        private readonly $state: StateService
    ) {
    }

    public ngOnInit(): void {
        this.gemeindeAntragService.getAllVisibleTagesschulenAngabenForTSLastenausgleich(this.lastenausgleichId)
            .subscribe(data => {
                this.data = data.map(latsInstitutionContainer => {
                        return {
                            id: latsInstitutionContainer.id,
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

    public navigate($event: any): void {
        this.$state.go('LASTENAUSGLEICH_TAGESSCHULEN.ANGABEN_TAGESSCHULEN.DETAIL', {institutionId: $event.element.id});
    }
}
