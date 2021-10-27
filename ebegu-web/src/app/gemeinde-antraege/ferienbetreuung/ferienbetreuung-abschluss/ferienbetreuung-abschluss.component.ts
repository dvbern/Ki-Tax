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
import {BehaviorSubject, combineLatest, Observable, Subject} from 'rxjs';
import {filter, first, map, mergeMap, takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {FerienbetreuungAngabenStatus} from '../../../../models/enums/FerienbetreuungAngabenStatus';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSSprache} from '../../../../models/enums/TSSprache';
import {TSWizardStepXTyp} from '../../../../models/enums/TSWizardStepXTyp';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {DvNgConfirmDialogComponent} from '../../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {DownloadRS} from '../../../core/service/downloadRS.rest';
import {WizardStepXRS} from '../../../core/service/wizardStepXRS.rest';
import {FerienbetreuungDokumentService} from '../services/ferienbetreuung-dokument.service';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

const LOG = LogFactory.createLog('FerienbetreuungAbschlussComponent');

@Component({
    selector: 'dv-ferienbetreuung-abschluss',
    templateUrl: './ferienbetreuung-abschluss.component.html',
    styleUrls: ['./ferienbetreuung-abschluss.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None,
})
export class FerienbetreuungAbschlussComponent implements OnInit {

    private static readonly FILENAME_DE = 'Verfügung Ferienbetreuung kiBon';
    private static readonly FILENAME_FR = 'Modèle Décisions EJC kibon';

    public container: TSFerienbetreuungAngabenContainer;

    private readonly WIZARD_TYPE = TSWizardStepXTyp.FERIENBETREUUNG;
    private readonly unsubscribe: Subject<boolean> = new Subject<boolean>();
    public downloadingDeFile: BehaviorSubject<boolean> = new BehaviorSubject(false);
    public downloadingFrFile: BehaviorSubject<boolean> = new BehaviorSubject(false);

    public constructor(
        private readonly ferienbetreuungsService: FerienbetreuungService,
        private readonly translate: TranslateService,
        private readonly dialog: MatDialog,
        private readonly errorService: ErrorService,
        private readonly wizardRS: WizardStepXRS,
        private readonly authService: AuthServiceRS,
        private readonly stateService: StateService,
        private readonly ferienbetreuungDokumentService: FerienbetreuungDokumentService,
        private readonly downloadRS: DownloadRS,
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
            this.container?.status === FerienbetreuungAngabenStatus.ABGESCHLOSSEN;
    }

    public readyForGeprueft(): boolean {
        return this.container?.angabenKorrektur?.angebot?.isAbgeschlossen() &&
            this.container?.angabenKorrektur?.nutzung?.isAbgeschlossen() &&
            this.container?.angabenKorrektur?.stammdaten?.isAbgeschlossen() &&
            this.container?.angabenKorrektur?.kostenEinnahmen?.isAbgeschlossen();
    }

    public verfuegungErstellenVisible(): boolean {
        return this.authService.isOneOfRoles(TSRoleUtil.getMandantRoles())
        && this.alreadyGeprueft()
        && !this.container?.isAbgeschlossen();
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

    public createVerfuegungDocumentDe(): void {
        this.downloadingDeFile.next(true);
        this.ferienbetreuungDokumentService.generateVerfuegung(
            this.container,
            TSSprache.DEUTSCH,
        ).subscribe(
            response => {
                this.createDownloadFile(response, TSSprache.DEUTSCH);
                this.downloadingDeFile.next(false);
            },
            async err => {
                LOG.error(err);
                this.errorService.addMesageAsError(err?.translatedMessage || this.translate.instant(
                    'ERROR_UNEXPECTED'));
                this.downloadingDeFile.next(false);
            });
    }

    public createVerfuegungDocumentFr(): void {
        this.downloadingFrFile.next(true);
        this.ferienbetreuungDokumentService.generateVerfuegung(
            this.container,
            TSSprache.FRANZOESISCH,
        ).subscribe(
            response => {
                this.createDownloadFile(response, TSSprache.FRANZOESISCH);
                this.downloadingFrFile.next(false);
            },
            async err => {
                LOG.error(err);
                this.errorService.addMesageAsError(err?.translatedMessage || this.translate.instant(
                    'ERROR_UNEXPECTED'));
                this.downloadingFrFile.next(false);
            });
    }

    private createDownloadFile(response: BlobPart, sprache: TSSprache): void {
        const file = new Blob([response],
            {type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'});
        const filename = this.getFilename(sprache);
        this.downloadRS.openDownload(file, filename);
    }

    private getFilename(sprache: TSSprache): string {
        let filename;
        (sprache === TSSprache.DEUTSCH)
            ? filename = FerienbetreuungAbschlussComponent.FILENAME_DE
            : filename = FerienbetreuungAbschlussComponent.FILENAME_FR;

        return `${filename} ${this.container.gesuchsperiode.gesuchsperiodeString} ${this.container.gemeinde.name}.docx`;
    }

    public async abschliessen(): Promise<void> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translate.instant('FERIENBETREUUNG_ABSCHLIESSEN_FRAGE'),
        };

        if (!await (this.dialog.open(DvNgConfirmDialogComponent, dialogConfig))
            .afterClosed()
            .toPromise()) {
            return;
        }

        this.ferienbetreuungsService.abschliessen(this.container).subscribe(() => {
            this.wizardRS.updateSteps(this.WIZARD_TYPE, this.container.id);
        }, () => this.errorService.addMesageAsError(this.translate.instant('ERROR_UNEXPECTED')));
    }

    public async zurueckAnKanton(): Promise<void> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translate.instant('FERIENBETREUUNG_ZURUECK_AN_KANTON_FRAGE'),
        };

        if (!await (this.dialog.open(DvNgConfirmDialogComponent, dialogConfig))
            .afterClosed()
            .toPromise()) {
            return;
        }

        this.ferienbetreuungsService.zurueckAnKanton(this.container).subscribe(() => {
            this.wizardRS.updateSteps(this.WIZARD_TYPE, this.container.id);
        }, () => this.errorService.addMesageAsError(this.translate.instant('ERROR_UNEXPECTED')));
    }

    public abgeschlossen(): boolean {
        return this.container?.isAbgeschlossen();
    }
}
