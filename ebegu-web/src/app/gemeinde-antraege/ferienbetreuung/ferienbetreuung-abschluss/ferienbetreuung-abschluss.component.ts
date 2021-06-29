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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component, OnInit, ViewEncapsulation} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {combineLatest, Observable, Subject} from 'rxjs';
import {filter, first, map, mergeMap, takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {FerienbetreuungAngabenStatus} from '../../../../models/enums/FerienbetreuungAngabenStatus';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSWizardStepXTyp} from '../../../../models/enums/TSWizardStepXTyp';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {DvNgConfirmDialogComponent} from '../../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {WizardStepXRS} from '../../../core/service/wizardStepXRS.rest';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

@Component({
    selector: 'dv-ferienbetreuung-abschluss',
    templateUrl: './ferienbetreuung-abschluss.component.html',
    styleUrls: ['./ferienbetreuung-abschluss.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None,
})
export class FerienbetreuungAbschlussComponent implements OnInit {

    public container: TSFerienbetreuungAngabenContainer;

    private readonly WIZARD_TYPE = TSWizardStepXTyp.FERIENBETREUUNG;
    private readonly unsubscribe: Subject<boolean> = new Subject<boolean>();

    public constructor(
        private readonly ferienbetreuungsService: FerienbetreuungService,
        private readonly translate: TranslateService,
        private readonly dialog: MatDialog,
        private readonly errorService: ErrorService,
        private readonly wizardRS: WizardStepXRS,
        private readonly authService: AuthServiceRS,
        private readonly stateService: StateService,
    ) {
    }

    public ngOnInit(): void {
        this.ferienbetreuungsService.getFerienbetreuungContainer()
            .pipe(
                takeUntil(this.unsubscribe),
            )
            .subscribe(container => this.container = container,
                () => this.errorService.addMesageAsError(this.translate.instant('DATA_RETRIEVAL_ERROR')));
    }

    public abschliessenVisible(): Observable<boolean> {
        return combineLatest([
            this.ferienbetreuungsService.getFerienbetreuungContainer().pipe(
                map(latsContainer => latsContainer.status ===
                    FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE),
                takeUntil(this.unsubscribe),
            ), this.authService.principal$,
        ]).pipe(
            map(([inBearbeitungGemeinde, principal]) => {
                return (principal.hasRole(TSRole.SUPER_ADMIN) && inBearbeitungGemeinde) ||
                    (principal.hasOneOfRoles(TSRoleUtil.getFerienbetreuungRoles()) &&
                        !principal.hasOneOfRoles(TSRoleUtil.getMandantRoles()));
            }),
        );
    }

    public geprueftVisible(): Observable<boolean> {
        return combineLatest([
            this.ferienbetreuungsService.getFerienbetreuungContainer().pipe(
                map(latsContainer => latsContainer.isAtLeastInPruefungKanton()),
                takeUntil(this.unsubscribe),
            ), this.authService.principal$,
        ]).pipe(
            map(([alLeastInPruefungKanton, principal]) => {
                return principal.hasOneOfRoles(TSRoleUtil.getMandantRoles()) && alLeastInPruefungKanton;
            }),
        );
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
                mergeMap(() => this.ferienbetreuungsService.getFerienbetreuungContainer().pipe(first())),
                mergeMap(container => this.ferienbetreuungsService.ferienbetreuungAngabenFreigeben(container)),
                takeUntil(this.unsubscribe),
            )
            .subscribe(() => {
                this.wizardRS.updateSteps(this.WIZARD_TYPE, this.container.id);
            }, () => {
                this.errorService.addMesageAsError(this.translate.instant('ERROR_UNEXPECTED'));

            });
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
                mergeMap(() => this.ferienbetreuungsService.getFerienbetreuungContainer().pipe(first())),
                mergeMap(container => this.ferienbetreuungsService.ferienbetreuungAngabenGeprueft(container)),
                takeUntil(this.unsubscribe),
            ).subscribe(() => this.wizardRS.updateSteps(this.WIZARD_TYPE, this.container.id),
            () => this.errorService.addMesageAsError(this.translate.instant('ERROR_UNEXPECTED')));
    }

    public ngOnDestroy(): void {
        this.unsubscribe.next(true);
    }

    public alreadyFreigegeben(): boolean {
        return this.container.status === FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON ||
            this.alreadyGeprueft();
    }

    public alreadyGeprueft(): boolean {
        return this.container?.status === FerienbetreuungAngabenStatus.GEPRUEFT ||
            this.container?.status === FerienbetreuungAngabenStatus.ABGELEHNT ||
            this.container?.status === FerienbetreuungAngabenStatus.VERFUEGT;
    }

    public readyForGeprueft(): boolean {
        return this.container?.angabenKorrektur?.angebot?.isAbgeschlossen() &&
            this.container?.angabenKorrektur?.nutzung?.isAbgeschlossen() &&
            this.container?.angabenKorrektur?.stammdaten?.isAbgeschlossen() &&
            this.container?.angabenKorrektur?.kostenEinnahmen?.isAbgeschlossen();
    }

    public async zurueckAnGemeinde(): Promise<void> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translate.instant('ZURUECK_AN_GEMEINDE'),
        };

        if (!await (this.dialog.open(DvNgConfirmDialogComponent, dialogConfig))
            .afterClosed()
            .toPromise()) {
            return;
        }

        this.ferienbetreuungsService.zurueckAnGemeinde(this.container).subscribe(
            () => this.stateService.go('gemeindeantrage.view'),
            () => this.errorService.addMesageAsError(this.translate.instant('ERROR_UNEXPECTED')));
    }
}
