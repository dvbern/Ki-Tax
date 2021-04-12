/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {BehaviorSubject, combineLatest, Observable} from 'rxjs';
import {filter, first, map, mergeMap} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSLastenausgleichTagesschuleAngabenGemeindeStatus} from '../../../../../models/enums/TSLastenausgleichTagesschuleAngabenGemeindeStatus';
import {TSRole} from '../../../../../models/enums/TSRole';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {DvNgConfirmDialogComponent} from '../../../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {HTTP_ERROR_CODES} from '../../../../core/constants/CONSTANTS';
import {ErrorService} from '../../../../core/errors/service/ErrorService';
import {LastenausgleichTSService} from '../../../lastenausgleich-ts/services/lastenausgleich-ts.service';

@Component({
    selector: 'dv-freigabe',
    templateUrl: './freigabe.component.html',
    styleUrls: ['./freigabe.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FreigabeComponent implements OnInit {

    private readonly ROUTING_DELAY = 3000; // ms

    @Input() public lastenausgleichID: string;

    public canViewFreigabeButton: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    public canViewGeprueftButton: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

    public constructor(
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService,
        private readonly latsService: LastenausgleichTSService,
        private readonly dialog: MatDialog,
        private readonly $state: StateService,
        private readonly authService: AuthServiceRS,
    ) {
    }

    public ngOnInit(): void {
        combineLatest([
            this.latsService.getLATSAngabenGemeindeContainer(),
            this.authService.principal$,
        ]).subscribe(([container, principal]) => {
            if (principal.hasRole(TSRole.SUPER_ADMIN)) {
                if (container.isAtLeastInBearbeitungKanton()) {
                    this.canViewFreigabeButton.next(false);
                    this.canViewGeprueftButton.next(true);
                } else {
                    this.canViewFreigabeButton.next(true);
                    this.canViewGeprueftButton.next(false);
                }
            }
            if (principal.hasOneOfRoles(TSRoleUtil.getMandantOnlyRoles())) {
                this.canViewFreigabeButton.next(false);
                this.canViewGeprueftButton.next(true);
            }
            if (principal.hasOneOfRoles(TSRoleUtil.getGemeindeOrBGOrTSRoles())) {
                this.canViewFreigabeButton.next(true);
                this.canViewGeprueftButton.next(false);
            }
        }, () => this.errorService.addMesageAsInfo(this.translate.instant('DATA_RETRIEVAL_ERROR')));
    }

    public freigeben(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translate.instant('LATS_FRAGE_GEMEINDE_ANTRAG_FREIGABE'),
        };
        this.dialog.open(DvNgConfirmDialogComponent, dialogConfig)
            .afterClosed()
            .pipe(
                filter(result => !!result),
                mergeMap(() => this.latsService.getLATSAngabenGemeindeContainer().pipe(first())),
                mergeMap(container => this.latsService.latsGemeindeAntragFreigeben(container)),
            )
            .subscribe(() => {
                this.$state.go('gemeindeantraege.view');
            }, error => {
                // tslint:disable-next-line:early-exit
                if (error.status === HTTP_ERROR_CODES.BAD_REQUEST) {
                    if (error.error.includes('angabenDeklaration')) {
                        this.errorService.addMesageAsError(this.translate.instant('LATS_GEMEINDE_ANGABEN_ERROR'));
                        setTimeout(() => this.$state.go('LASTENAUSGLEICH_TAGESSCHULEN.ANGABEN_GEMEINDE',
                            {triggerValidation: true},
                            {}),
                            this.ROUTING_DELAY);
                    } else if (error.error.includes('LastenausgleichAngabenInstitution')) {
                        this.errorService.addMesageAsError(this.translate.instant(
                            'LATS_NICHT_ALLE_INSTITUTIONEN_ABGESCHLOSSEN'));
                        setTimeout(() => this.$state.go('LASTENAUSGLEICH_TAGESSCHULEN.ANGABEN_TAGESSCHULEN.LIST'),
                            this.ROUTING_DELAY);
                    }
                } else {
                    this.errorService.addMesageAsError(this.translate.instant('ERROR_SAVE'));
                }
            });
    }

    public isInBearbeitungGemeinde(): Observable<boolean> {
        return this.latsService.getLATSAngabenGemeindeContainer().pipe(
            map(latsContainer => latsContainer.status ===
                TSLastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE),
        );
    }

    public geprueft(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translate.instant('LATS_FRAGE_GEMEINDE_ANTRAG_FREIGABE_GEPRUEFT'),
        };
        this.dialog.open(DvNgConfirmDialogComponent, dialogConfig)
            .afterClosed()
            .pipe(
                filter(result => !!result),
                mergeMap(() => this.latsService.getLATSAngabenGemeindeContainer().pipe(first())),
            ).subscribe(container => this.latsService.latsGemeindeAntragGeprueft(container),
            () => this.errorService.addMesageAsError(this.translate.instant('SAVE_ERROR')));
    }

    public isInPruefungKanton(): Observable<boolean> {
        return this.latsService.getLATSAngabenGemeindeContainer().pipe(
            map(latsContainer => latsContainer.status ===
                TSLastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON),
        );

    }
}
