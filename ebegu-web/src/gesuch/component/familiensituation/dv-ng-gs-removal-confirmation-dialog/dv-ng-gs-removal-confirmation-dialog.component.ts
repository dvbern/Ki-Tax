import {ChangeDetectionStrategy, Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {DvNgRemoveDialogComponent} from '../../../../app/core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {EbeguUtil} from '../../../../utils/EbeguUtil';

export interface GSRemovalConfirmationDialogData {
    gsFullName: string;
}

@Component({
    selector: 'dv-dv-ng-gs-removal-confirmation-dialog',
    templateUrl: './dv-ng-gs-removal-confirmation-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DvNgGsRemovalConfirmationDialogComponent {
    public readonly text: string;

    public constructor(
        private readonly $translate: TranslateService,
        private readonly dialogRef: MatDialogRef<DvNgRemoveDialogComponent>,
        @Inject(MAT_DIALOG_DATA)
        private readonly data: GSRemovalConfirmationDialogData
    ) {
        if (
            EbeguUtil.isNullOrUndefined(this.data) ||
            EbeguUtil.isNullOrUndefined(data.gsFullName)
        ) {
            throw new Error('Wrong Dialog configuration');
        }

        this.text = this.$translate.instant(
            'FAMILIENSITUATION_WARNING_BESCHREIBUNG',
            {
                gsfullname: this.data.gsFullName
            }
        );
    }

    public ok(): void {
        this.dialogRef.close(true);
    }

    public cancel(): void {
        this.dialogRef.close();
    }
}
