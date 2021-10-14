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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatTableDataSource} from '@angular/material/table';
import {TranslateService} from '@ngx-translate/core';
import {from, Observable, Subject} from 'rxjs';
import {map, mergeMap, takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSDownloadFile} from '../../../../models/TSDownloadFile';
import {TSLastenausgleich} from '../../../../models/TSLastenausgleich';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {DvNgRemoveDialogComponent} from '../../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../../core/rest-services/applicationPropertyRS.rest';
import {DownloadRS} from '../../../core/service/downloadRS.rest';
import {UploadRS} from '../../../core/service/uploadRS.rest';
import {LastenausgleichRS} from '../../services/lastenausgleichRS.rest';
import {ZemisDialogComponent} from '../zemisDialog/zemis-dialog.component';
import {ZemisDialogDTO} from '../zemisDialog/zemisDialog.interface';

const LOG = LogFactory.createLog('LastenausgleichViewXComponent');

@Component({
  selector: 'dv-lastenausgleich-view-x',
  templateUrl: './lastenausgleich-view-x.component.html',
  styleUrls: ['./lastenausgleich-view-x.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LastenausgleichViewXComponent implements OnInit, OnDestroy {

    public jahr: number;
    public selbstbehaltPro100ProzentPlatz: number;
    public lastenausgleiche: TSLastenausgleich[] = [];
    public readonly TSRoleUtil = TSRoleUtil;
    public datasource: MatTableDataSource<TSLastenausgleich> = new MatTableDataSource<TSLastenausgleich>([]);
    public columndefs: string[] = [];

    @ViewChild(NgForm) private readonly form: NgForm;

    private readonly unsubscribe$ = new Subject<void>();

    public constructor(
        private readonly lastenausgleichRS: LastenausgleichRS,
        private readonly dialog: MatDialog,
        private readonly translate: TranslateService,
        private readonly downloadRS: DownloadRS,
        private readonly uploadRS: UploadRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly errorService: ErrorService,
        private readonly cd: ChangeDetectorRef,
        private readonly applicationPropertyRS: ApplicationPropertyRS
    ) { }

    private static handleDownloadError(err: Error, win: Window): void {
        LOG.error(err);
        win.close();
    }

    public ngOnInit(): void {
        this.getAllLastenausgleiche();
        this.initColumnDefs();
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
    }

    private initColumnDefs(): void {
        this.columndefs = [
            'jahr',
            'lastenausgleichGeneneriert',
            'totalAlleGemeinden',
            'lastenausgleichExcel',
        ];
        if (this.showCSVDownload()) {
            this.columndefs.push('lastenausgleichCsv');
        }
        this.isRemoveAllowed()
            .subscribe(res => {
            if (res) {
                this.columndefs.push('lastenausgleichRemove');
            }
        }, err => LOG.error(err));
    }

    private getAllLastenausgleiche(): void {
        this.lastenausgleichRS.getAllLastenausgleiche().subscribe((response: TSLastenausgleich[]) => {
            this.lastenausgleiche = response;
            this.addToDataSource(response);
        }, err => {
            LOG.error(err);
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
            .pipe(
                mergeMap(() =>
                    this.lastenausgleichRS.createLastenausgleich(this.jahr, this.selbstbehaltPro100ProzentPlatz))
            )
            .subscribe((response: TSLastenausgleich) => {
                this.lastenausgleiche.push(response);
                this.addToDataSource(this.lastenausgleiche);
                }, err => {
                LOG.error(err);
            });
    }

    private addToDataSource(lastenausgleiche: TSLastenausgleich[]): void {
        lastenausgleiche.sort((a, b) => b.jahr - a.jahr);
        this.datasource.data = lastenausgleiche;
        this.cd.markForCheck();
    }

    public downloadZemisExcel(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            upload: false
        };
        this.dialog.open(ZemisDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe((zemisDialogData: ZemisDialogDTO) => {
                if (!zemisDialogData) {
                    return;
                }
                if (!zemisDialogData.jahr) {
                    LOG.error('year undefined');
                    return;
                }
                const win = this.downloadRS.prepareDownloadWindow();
                this.lastenausgleichRS.getZemisExcel(zemisDialogData.jahr)
                    .subscribe((downloadFile: TSDownloadFile) => {
                        this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
                    }, err => {
                        LastenausgleichViewXComponent.handleDownloadError(err, win);
                    });
            }, err => {
                LOG.error(err);
            });
    }

    public uploadZemisExcel(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            upload: true
        };
        this.dialog.open(ZemisDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe((zemisDialogData: ZemisDialogDTO) => {
                if (!zemisDialogData) {
                    return;
                }
                if (!zemisDialogData.file) {
                    LOG.error('file undefined');
                }
                this.uploadRS.uploadZemisExcel(zemisDialogData.file)
                    .then(() => {
                        this.errorService.addMesageAsInfo(this.translate.instant(
                            'ZEMIS_UPLOAD_FINISHED'
                        ));
                    })
                    .catch(err => {
                            LOG.error('Fehler beim Speichern', err);
                        }
                    );
                this.errorService.addMesageAsInfo(this.translate.instant(
                    'ZEMIS_UPLOAD_STARTED'
                ));
            }, err => {
                LOG.error(err);
            });
    }

    public downloadExcel(lastenausgleich: TSLastenausgleich): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.lastenausgleichRS.getLastenausgleichReportExcel(lastenausgleich.id)
            .subscribe((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            }, err => {
                LastenausgleichViewXComponent.handleDownloadError(err, win);
            });
    }

    public downloadCsv(lastenausgleich: TSLastenausgleich): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.lastenausgleichRS.getLastenausgleichReportCSV(lastenausgleich.id)
            .subscribe((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            }, err => {
                LastenausgleichViewXComponent.handleDownloadError(err, win);
            });
    }

    public isRemoveAllowed(): Observable<boolean> {
        return from(this.applicationPropertyRS.isDevMode())
            .pipe(
                map(res => res && this.authServiceRS.isRole(TSRole.SUPER_ADMIN)),
                takeUntil(this.unsubscribe$)
            );
    }

    public removeLastenausgleich(lastenausgleich: TSLastenausgleich): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('LASTENAUSGLEICH_LOESCHEN_DIALOG_TITLE'),
            text: this.translate.instant('LASTENAUSGLEICH_LOESCHEN_DIALOG_TEXT'),
        };

        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .pipe(
                mergeMap(() => this.lastenausgleichRS.removeLastenausgleich(lastenausgleich.id)),
            )
            .subscribe(() => {
                this.getAllLastenausgleiche();
            }, err => {
                LOG.error(err);
            });
    }

    public showLastenausgleich(): boolean {
        return this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getAllRolesForLastenausgleich());
    }

    public showCSVDownload(): boolean {
        return this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getMandantRoles());
    }

    public showActions(): boolean {
        return this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getMandantRoles());
    }
}
