/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {StateService} from '@uirouter/core';
import {TSBetreuungsstatus} from '../../../../models/enums/betreuung/TSBetreuungsstatus';
import {TSMitteilungStatus} from '../../../../models/enums/TSMitteilungStatus';
import {TSBetreuungsmitteilung} from '../../../../models/TSBetreuungsmitteilung';
import {TSMitteilung} from '../../../../models/TSMitteilung';
import {TSMitteilungVerarbeitungResult} from '../../../../models/TSMitteilungVerarbeitungResult';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {ErrorServiceX} from '../../errors/service/ErrorServiceX';
import {LogFactory} from '../../logging/LogFactory';
import {MitteilungRS} from '../../service/mitteilungRS.rest';

const LOG = LogFactory.createLog('DvNgMitteilungResultDialogComponent');
/**
 * Component fuer den zusammenfassung nach man alle Mutationmitteilungen automatisch bearbeiten hast
 * Es zeigt zwei Liste, eine für die erfolgreiche Mutationsmitteilungen und eine mit die die nicht
 * automatsich bearbeitet werden koennen
 */
@Component({
    selector: 'dv-ng-mitteilung-result-dialog',
    templateUrl: './dv-ng-mitteilung-result-dialog.template.html',
    styleUrls: ['./dv-ng-mitteilung-result-dialog.template.less']
})
export class DvNgMitteilungResultDialogComponent implements OnInit {
    public verarbeitung?: TSMitteilungVerarbeitungResult;
    public constructor(
        private readonly dialogRef: MatDialogRef<DvNgMitteilungResultDialogComponent>,
        private readonly $state: StateService,
        private readonly ebeguUtil: EbeguUtil,
        private readonly mitteilungRS: MitteilungRS,
        private readonly errorService: ErrorServiceX,
        @Inject(MAT_DIALOG_DATA)
        private readonly mitteilungenToProcess: TSBetreuungsmitteilung[]
    ) {}

    public ngOnInit(): void {
        if (EbeguUtil.isEmptyArrayNullOrUndefined(this.mitteilungenToProcess)) {
            LOG.warn('No Mitteilungen to process were provided');
            return;
        }
        this.mitteilungRS
            .applyAlleBetreuungsmitteilungen(this.mitteilungenToProcess)
            .subscribe(
                verarbeitungResult => {
                    this.verarbeitung = verarbeitungResult;
                },
                error => {
                    LOG.error(error);
                }
            );
    }

    public getVerfuegtSuccessItems(): TSBetreuungsmitteilung[] {
        if (EbeguUtil.isNullOrUndefined(this.verarbeitung)) {
            return [];
        }
        return this.verarbeitung.successItems.filter(
            mitteilung =>
                mitteilung.mitteilungStatus === TSMitteilungStatus.ERLEDIGT &&
                mitteilung.betreuung.betreuungsstatus ===
                    TSBetreuungsstatus.VERFUEGT
        );
    }

    public getNotVerfuegtSuccessItems(): TSBetreuungsmitteilung[] {
        if (EbeguUtil.isNullOrUndefined(this.verarbeitung)) {
            return [];
        }
        return this.verarbeitung.successItems.filter(
            mitteilung =>
                mitteilung.mitteilungStatus === TSMitteilungStatus.ERLEDIGT &&
                mitteilung.betreuung.betreuungsstatus !==
                    TSBetreuungsstatus.VERFUEGT
        );
    }

    public ok(): void {
        this.dialogRef.close(true);
    }

    public getBetreuungUrl(mitteilung: TSMitteilung): string {
        const url = this.$state.href('gesuch.betreuung', {
            betreuungNumber: mitteilung.betreuung.betreuungNummer,
            kindNumber: mitteilung.betreuung.kindNummer,
            gesuchId: mitteilung.betreuung.gesuchId
        });
        return url;
    }
}
