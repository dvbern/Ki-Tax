/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    Input,
    OnChanges,
    OnDestroy,
    SimpleChanges,
} from '@angular/core';
import {ControlContainer, NgForm, ValidationErrors} from '@angular/forms';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {LogFactory} from '../../logging/LogFactory';

const LOG = LogFactory.createLog('ErrorMessagesComponent');

@Component({
    selector: 'dv-error-messages',
    templateUrl: './error-messages.component.html',
    styleUrls: ['./dv-error-messages.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})
export class ErrorMessagesComponent implements OnChanges, OnDestroy {

    @Input() public errorObject: ValidationErrors | null;
    @Input() public inputId: string;

    public error: string = '';

    private readonly unsubscribe$ = new Subject<void>();

    public constructor(
        public readonly form: NgForm,
        public readonly changeDetectorRef: ChangeDetectorRef,
    ) {
        this.form.ngSubmit
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                () => this.changeDetectorRef.markForCheck(),
                err => LOG.error(err),
            );
    }

    public ngOnChanges(changes: SimpleChanges): void {
        // when the errors change we need to update our error
        if (changes && changes.errorObject) {
            this.initError(changes.errorObject.currentValue);
        }
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    private initError(errors: ValidationErrors | null): void {
        this.error = this.findFirstErrorKey(errors);
    }

    private findFirstErrorKey(errors?: ValidationErrors | null): string {
        if (!errors) {
            return '';
        }

        const firstErroneousKey = Object.keys(errors)
            // sort required to the end so more precise errors precede
            .sort((a, b) => {
                if (a === 'required') {
                    return 1;
                }
                if (b === 'required') {
                    return -1;
                }
                return a.localeCompare(b);
            })
            .find(key => !!errors[key]);

        return firstErroneousKey || '';
    }
}
