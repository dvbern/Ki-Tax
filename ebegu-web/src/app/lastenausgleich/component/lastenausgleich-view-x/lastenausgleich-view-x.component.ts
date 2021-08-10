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

import {ChangeDetectionStrategy, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSDownloadFile} from '../../../../models/TSDownloadFile';
import {TSLastenausgleich} from '../../../../models/TSLastenausgleich';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {DvNgRemoveDialogComponent} from '../../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {DownloadRS} from '../../../core/service/downloadRS.rest';
import {UploadRS} from '../../../core/service/uploadRS.rest';
import {LastenausgleichRS} from '../../services/lastenausgleichRS.rest';

const LOG = LogFactory.createLog('LastenausgleichViewXComponent');

@Component({
  selector: 'dv-lastenausgleich-view-x',
  templateUrl: './lastenausgleich-view-x.component.html',
  styleUrls: ['./lastenausgleich-view-x.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LastenausgleichViewXComponent implements OnInit {

    public jahr: number;
    public selbstbehaltPro100ProzentPlatz: number;
    public lastenausgleiche: TSLastenausgleich[] = [];
    public readonly TSRoleUtil = TSRoleUtil;

    @ViewChild(NgForm) private readonly form: NgForm;

    public constructor(
        private readonly lastenausgleichRS: LastenausgleichRS,
        private readonly dialog: MatDialog,
        private readonly translate: TranslateService,
        private readonly downloadRS: DownloadRS,
        private readonly uploadRS: UploadRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly errorService: ErrorService
    ) { }

    public ngOnInit(): void {
        this.getAllLastenausgleiche();
    }

    private getAllLastenausgleiche(): void {
        this.lastenausgleichRS.getAllLastenausgleiche().subscribe((response: TSLastenausgleich[]) => {
            this.lastenausgleiche = response;
        });
    }

    public createLastenausgleich(): void {
        if (!this.form.valid) {
            return;
        }
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('LASTENAUSGLEICH_ERSTELLEN_TITLE'),
            text: this.translate.instant('LASTENAUSGLEICH_ERSTELLEN_INFO'),
        };
        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe(() => {
                this.lastenausgleichRS.createLastenausgleich(this.jahr, this.selbstbehaltPro100ProzentPlatz)
                    .subscribe((response: TSLastenausgleich) => {
                        this.lastenausgleiche.push(response);
                    });
            }, err => LOG.error(err));
    }

     public downloadZemisExcel(): void {
    //     this.dvDialog.showDialog(inputYearDialogTemplate, ZemisDialogController,  {upload: false})
    //         .then((zemisDialogData: ZemisDialogDTO) => {
    //             if (!zemisDialogData) {
    //                 return;
    //             }
    //             if (!zemisDialogData.jahr) {
    //                 LOG.error('year undefined');
    //             }
    //             const win = this.downloadRS.prepareDownloadWindow();
    //             this.lastenausgleichRS.getZemisExcel(zemisDialogData.jahr)
    //                 .subscribe((downloadFile: TSDownloadFile) => {
    //                     this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
    //                 }, err => {
    //                     this.handleDownloadError(err, win);
    //                 });
    //         }, err => {
    //             LOG.error(err);
    //         });
     }

    private handleDownloadError(err: Error, win: Window): void {
        LOG.error(err);
        this.errorService.addMesageAsError('ERROR_UNEXPECED');
        win.close();
    }

    public uploadZemisExcel(): void {

        // this.dvDialog.showDialog(inputYearDialogTemplate, ZemisDialogController, {upload: true})
        //     .then((zemisDialogData: ZemisDialogDTO) => {
        //         if (!zemisDialogData) {
        //             return;
        //         }
        //         if (!zemisDialogData.file) {
        //             LOG.error('file undefined');
        //         }
        //         this.uploadRS.uploadZemisExcel(zemisDialogData.file)
        //             .then(() => {
        //                 this.errorService.addMesageAsInfo(this.translate.instant(
        //                     'ZEMIS_UPLOAD_FINISHED'
        //                 ));
        //             })
        //             .catch(err => {
        //                     LOG.error('Fehler beim Speichern', err);
        //                 }
        //             );
        //         this.errorService.addMesageAsInfo(this.translate.instant(
        //             'ZEMIS_UPLOAD_STARTED'
        //         ));
        //     }, err => {
        //         LOG.error(err);
        //     });
    }

    public downloadExcel(lastenausgleich: TSLastenausgleich): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.lastenausgleichRS.getLastenausgleichReportExcel(lastenausgleich.id)
            .subscribe((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            }, err => {
                this.handleDownloadError(err, win);
            });
    }

    public downloadCsv(lastenausgleich: TSLastenausgleich): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.lastenausgleichRS.getLastenausgleichReportCSV(lastenausgleich.id)
            .subscribe((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            }, err => {
                this.handleDownloadError(err, win);
            });
    }

    public isRemoveAllowed(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public canDownloadCSV(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }

    public removeLastenausgleich(lastenausgleich: TSLastenausgleich): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('LASTENAUSGLEICH_LOESCHEN_DIALOG_TITLE'),
            text: this.translate.instant('LASTENAUSGLEICH_LOESCHEN_DIALOG_TEXT'),
        };

        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe(() => {
                this.lastenausgleichRS.removeLastenausgleich(lastenausgleich.id).subscribe(() => {
                    this.getAllLastenausgleiche();
                }, err => {
                    this.errorService.addMesageAsError(err);
                    LOG.error(err);
                });
            }, err => LOG.error(err));
    }

    public showLastenausgleich(): boolean {
        return this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getAllRolesForLastenausgleich());
    }

    public showActions(): boolean {
        return this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getMandantRoles());
    }
}
