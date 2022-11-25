/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {StateService} from '@uirouter/core';
import {TSBetreuungsstatus} from '../../../../models/enums/TSBetreuungsstatus';
import {TSBetreuungsmitteilung} from '../../../../models/TSBetreuungsmitteilung';
import {TSMitteilung} from '../../../../models/TSMitteilung';
import {EbeguUtil} from '../../../../utils/EbeguUtil';

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
export class DvNgMitteilungResultDialogComponent {

    public betreuungsmitteilungsOkVerfuegt: TSBetreuungsmitteilung[] = [];
    public betreuungsmitteilungsOkNotVerfuegt: TSBetreuungsmitteilung[] = [];
    public betreuungsmitteilungsKo: TSBetreuungsmitteilung[] = [];

    public constructor(
        private readonly dialogRef: MatDialogRef<DvNgMitteilungResultDialogComponent>,
        private readonly $state: StateService,
        private readonly ebeguUtil: EbeguUtil,
        @Inject(MAT_DIALOG_DATA) data: TSBetreuungsmitteilung[]
    ) {
        if (data) {
            this.betreuungsmitteilungsOkVerfuegt = data.filter(betreuungsmitteilung => betreuungsmitteilung.applied
                && TSBetreuungsstatus.VERFUEGT === betreuungsmitteilung.betreuung.betreuungsstatus);
            this.betreuungsmitteilungsOkNotVerfuegt = data.filter(betreuungsmitteilung => betreuungsmitteilung.applied
                && TSBetreuungsstatus.VERFUEGT !== betreuungsmitteilung.betreuung.betreuungsstatus);
            this.betreuungsmitteilungsKo = data.filter(betreuungsmitteilung => !betreuungsmitteilung.applied);
        }
    }

    public ok(): void {
        this.dialogRef.close(true);
    }

    public getRererenceNummer(mitteilung: TSBetreuungsmitteilung): string {
        return this.ebeguUtil.calculateBetreuungsId(mitteilung.betreuung.gesuchsperiode,
            mitteilung.dossier.fall,
            mitteilung.dossier.gemeinde,
            mitteilung.betreuung.kindNummer,
            mitteilung.betreuung.betreuungNummer);
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
