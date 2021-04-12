import {Injectable} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TransitionService} from '@uirouter/core';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {DvNgBackDialogComponent} from '../../core/component/dv-ng-back-dialog/dv-ng-back-dialog.component';

@Injectable({
    providedIn: 'root'
})
export class UnsavedChangesService {

    private formGroup: FormGroup;

    public constructor(
        private readonly $transition: TransitionService,
        private readonly dialog: MatDialog,
    ) {
        this.$transition.onStart({}, async() => {
            return this.checkUnsavedChanges().then(userAccepted => {
                if (userAccepted) {
                    this.unregisterForm();
                }
                return userAccepted;
            });
        });
    }

    public registerForm(formGroup: FormGroup): void {
        this.formGroup = formGroup;
    }

    private async checkUnsavedChanges(): Promise<boolean> {
        if (!this.isFormDirty()) {
            return of(true).toPromise();
        }
        return this.openDialog()
            .toPromise();
    }

    private isFormDirty(): boolean {
        if (!this.formGroup) {
            return false;
        }
        return this.formGroup.dirty;
    }

    private openDialog(): Observable<boolean> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'UNSAVED_WARNING',
        };
        return this.dialog.open(DvNgBackDialogComponent, dialogConfig)
            .afterClosed()
            .pipe(map(answer => {
                // answer is undefined, if cancel is pressed. we need a boolean here
                return answer === true;
            }));
    }

    private unregisterForm(): void {
        this.formGroup = undefined;
    }
}
