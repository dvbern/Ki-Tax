import {Injectable} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TransitionService} from '@uirouter/core';
import {Observable, of} from 'rxjs';
import {map, tap} from 'rxjs/operators';
import {DvNgRemoveDialogComponent} from '../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';

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
            return this.checkUnsavedChanges();
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
            .pipe(tap(() => {
                this.unregisterForm();
            }))
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
        return this.dialog.open(DvNgRemoveDialogComponent, dialogConfig)
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
