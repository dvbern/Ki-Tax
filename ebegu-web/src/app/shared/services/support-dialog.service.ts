import {Injectable} from '@angular/core';
import {MatLegacyDialog as MatDialog, MatLegacyDialogRef as MatDialogRef} from '@angular/material/legacy-dialog';
import {DvNgSupportDialogComponent} from '../../../gesuch/dialog/dv-ng-support-dialog.component';

@Injectable({
    providedIn: 'root'
})
export class SupportDialogService {

    private supportDialogRef: MatDialogRef<DvNgSupportDialogComponent>;

    public constructor(
        private readonly supportDialog: MatDialog
    ) {
    }

    public openDialog(): void {
        this.supportDialogRef = this.supportDialog.open(DvNgSupportDialogComponent);
    }

    public closeDialog(): void {
        this.supportDialogRef.close();
    }

    public isDialogOpen(): boolean {
        return this.supportDialogRef.getState() === 0;
    }

    public isDialogClosed(): boolean {
        return this.supportDialogRef.getState() === 2;
    }

}
