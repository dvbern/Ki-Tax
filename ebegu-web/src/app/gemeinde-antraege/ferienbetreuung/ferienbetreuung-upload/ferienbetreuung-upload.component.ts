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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {NEVER} from 'rxjs';
import {concatMap} from 'rxjs/operators';
import {TSWizardStepXTyp} from '../../../../models/enums/TSWizardStepXTyp';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {FerienbetreuungAngabenStatus} from '../../../../models/enums/FerienbetreuungAngabenStatus';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSFerienbetreuungDokument} from '../../../../models/gemeindeantrag/TSFerienbetreuungDokument';
import {TSDownloadFile} from '../../../../models/TSDownloadFile';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {DvNgRemoveDialogComponent} from '../../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {MAX_FILE_SIZE} from '../../../core/constants/CONSTANTS';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {DownloadRS} from '../../../core/service/downloadRS.rest';
import {UploadRS} from '../../../core/service/uploadRS.rest';
import {WizardStepXRS} from '../../../core/service/wizardStepXRS.rest';
import {FerienbetreuungDokumentService} from '../services/ferienbetreuung-dokument.service';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

const LOG = LogFactory.createLog('FerienbetreuungUploadComponent');

@Component({
    selector: 'dv-ferienbetreuung-upload',
    templateUrl: './ferienbetreuung-upload.component.html',
    styleUrls: ['./ferienbetreuung-upload.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungUploadComponent implements OnInit {

    public dokumente: TSFerienbetreuungDokument[];

    private container: TSFerienbetreuungAngabenContainer;
    public filesTooBig: File[];

    public constructor(
        private readonly ferienbetreuungService: FerienbetreuungService,
        private readonly ferienbetreuungDokumentService: FerienbetreuungDokumentService,
        private readonly uploadRS: UploadRS,
        private readonly errorService: ErrorService,
        private readonly cd: ChangeDetectorRef,
        private readonly translate: TranslateService,
        private readonly dialog: MatDialog,
        private readonly downloadRS: DownloadRS,
        private readonly wizardRS: WizardStepXRS,
        private readonly authService: AuthServiceRS
    ) {
    }

    public ngOnInit(): void {
        this.ferienbetreuungService.getFerienbetreuungContainer()
            .pipe(concatMap(container => {
                this.container = container;
                return this.ferienbetreuungDokumentService.getAllDokumente(container.id);
            }))
            .subscribe(dokumente => {
                this.dokumente = dokumente;
                this.cd.markForCheck();
            }, error => {
            LOG.error(error);
        });
    }

    public download(dokument: TSFerienbetreuungDokument, attachment: boolean): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getAccessTokenFerienbetreuungDokument(dokument.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, attachment, win);
            })
            .catch(() => {
                win.close();
            });
    }

    public onDelete(dokument: TSFerienbetreuungDokument): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('LOESCHEN_DIALOG_TITLE'),
            text: '',
        };
        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .pipe(concatMap(userAccepted => {
                if (!userAccepted) {
                    return NEVER;
                }
                return this.ferienbetreuungDokumentService.deleteDokument(dokument.id);
            }))
            .subscribe(() => {
                    this.dokumente = this.dokumente.filter(d => d.id !== dokument.id);
                    this.wizardRS.updateSteps(TSWizardStepXTyp.FERIENBETREUUNG, this.container.id);
                    this.cd.markForCheck();
                },
                err => {
                    LOG.error(err);
                    this.errorService.addMesageAsError(this.translate.instant('ERROR_UNEXPECTED'));
                });
    }

    public onUpload(event: any): void {
        if (EbeguUtil.isNullOrUndefined(event?.target?.files?.length)) {
            return;
        }
        const files = event.target.files;
        if (this.checkFilesLength(files as File[])) {
            return;
        }
        this.uploadRS.uploadFerienbetreuungDokumente(files, this.container.id)
            .then(dokumente => {
                this.dokumente = this.dokumente.concat(dokumente);
                this.wizardRS.updateSteps(TSWizardStepXTyp.FERIENBETREUUNG, this.container.id);
                this.cd.markForCheck();
            })
            .catch(err => {
                LOG.error(err);
                this.errorService.addMesageAsError(this.translate.instant('ERROR_UNEXPECTED'));
            });

    }

    public isReadonly(): boolean {
        return this.container?.status !== FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE
            || this.authService.isOneOfRoles(TSRoleUtil.getMandantOnlyRoles());
    }

    /**
     * checks if some files are too big and stores them in filesTooBig variable
     */
    private checkFilesLength(files: File[]): boolean {
        this.filesTooBig = [];
        for (const file of files) {
            if (file.size > MAX_FILE_SIZE) {
                this.filesTooBig.push(file);
            }
        }
        return this.filesTooBig.length > 0;
    }
}
