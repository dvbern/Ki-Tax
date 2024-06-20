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
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    Input,
    OnInit,
    ViewEncapsulation
} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSLastenausgleichTagesschuleAngabenInstitutionContainer} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitutionContainer';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {ErrorService} from '../../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../../core/logging/LogFactory';
import {DvSimpleTableColumnDefinition} from '../../../../shared/component/dv-simple-table/dv-simple-table-column-definition';
import {GemeindeAntragService} from '../../../services/gemeinde-antrag.service';
import {LastenausgleichTSService} from '../../services/lastenausgleich-ts.service';

const LOG = LogFactory.createLog('TagesschulenListComponent');

@Component({
    selector: 'dv-tagesschulen-list',
    templateUrl: './tagesschulen-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None
})
export class TagesschulenListComponent implements OnInit {
    @Input() public lastenausgleichId: string;

    public data: {
        institutionName: string;
        status: string;
        kontrollfragenOk: boolean;
    }[];
    public tableColumns: DvSimpleTableColumnDefinition[];

    public constructor(
        private readonly gemeindeAntragService: GemeindeAntragService,
        private readonly lastenausgleichTSService: LastenausgleichTSService,
        private readonly cd: ChangeDetectorRef,
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService,
        private readonly $state: StateService,
        private readonly authService: AuthServiceRS
    ) {}

    private static areKontrollfragenOk(
        latsInstitutionContainer: TSLastenausgleichTagesschuleAngabenInstitutionContainer
    ): boolean | null {
        const latsInstiAngaben =
            latsInstitutionContainer.isAtLeastInBearbeitungGemeinde()
                ? latsInstitutionContainer.angabenKorrektur
                : latsInstitutionContainer.angabenDeklaration;
        return latsInstiAngaben.areKontrollfragenAnswered()
            ? latsInstiAngaben.areKontrollfragenOk()
            : null;
    }

    public ngOnInit(): void {
        this.getAllVisibleTagesschulenAngabenForTSLastenausgleich();
        this.initTableColumns();
    }

    private getAllVisibleTagesschulenAngabenForTSLastenausgleich(): void {
        this.gemeindeAntragService
            .getAllVisibleTagesschulenAngabenForTSLastenausgleich(
                this.lastenausgleichId
            )
            .subscribe(
                data => {
                    this.data = data.map(latsInstitutionContainer => ({
                        id: latsInstitutionContainer.id,
                        institutionName:
                            latsInstitutionContainer.institution.name,
                        status: `LATS_STATUS_${latsInstitutionContainer.status}`,
                        kontrollfragenOk:
                            TagesschulenListComponent.areKontrollfragenOk(
                                latsInstitutionContainer
                            )
                    }));
                    this.cd.markForCheck();
                },
                () => {
                    this.translate.get('DATA_RETRIEVAL_ERROR').subscribe(
                        msg => this.errorService.addMesageAsError(msg),
                        err => console.error('Error loading translation', err)
                    );
                }
            );
    }

    public navigate($event: any): void {
        this.$state.go(
            'LASTENAUSGLEICH_TAGESSCHULEN.ANGABEN_TAGESSCHULEN.DETAIL',
            {institutionId: $event.element.id}
        );
    }

    public createMissingTagesschuleFormulare(): void {
        this.lastenausgleichTSService
            .createMissingTagesschuleFormulare(this.lastenausgleichId)
            .subscribe(
                () => {
                    // since we changed institutions of angabenGemeinde Object, we have to reload store
                    this.lastenausgleichTSService.updateLATSAngabenGemeindeContainerStore(
                        this.lastenausgleichId
                    );
                    this.getAllVisibleTagesschulenAngabenForTSLastenausgleich();
                    this.errorService.addMesageAsInfo(
                        'ALL_TAGESSCHULE_FORMULARE_CREATED'
                    );
                },
                err => {
                    LOG.error(err);
                }
            );
    }

    public isGemeindeOrSuperadmin(): boolean {
        return this.authService.isOneOfRoles(TSRoleUtil.getGemeindeRoles());
    }

    private initTableColumns(): void {
        this.lastenausgleichTSService
            .getLATSAngabenGemeindeContainer()
            .subscribe(
                container => {
                    if (
                        container.isAtLeastInBearbeitungKanton() &&
                        this.authService.isOneOfRoles(
                            TSRoleUtil.getMandantRoles()
                        )
                    ) {
                        this.tableColumns = [
                            {
                                displayedName: 'TAGESSCHULE',
                                attributeName: 'institutionName'
                            },
                            {displayedName: 'STATUS', attributeName: 'status'},
                            {
                                displayedName: 'KONTROLLFRAGEN',
                                attributeName: 'kontrollfragenOk',
                                displayFunction: (isOk: boolean) => {
                                    if (EbeguUtil.isNullOrUndefined(isOk)) {
                                        return '';
                                    }
                                    return isOk
                                        ? '<i class="fa fa-check padding-left-60 green"></i>'
                                        : '<i class="fa fa-close padding-left-60 red"></i>';
                                }
                            }
                        ];
                        return;
                    }
                    this.tableColumns = [
                        {
                            displayedName: 'TAGESSCHULE',
                            attributeName: 'institutionName'
                        },
                        {displayedName: 'STATUS', attributeName: 'status'}
                    ];
                },
                error => console.error(error)
            );
    }

    public isInBearbeitungGemeinde(): Observable<boolean> {
        return this.lastenausgleichTSService
            .getLATSAngabenGemeindeContainer()
            .pipe(map(container => container.isInBearbeitungGemeinde()));
    }
}
