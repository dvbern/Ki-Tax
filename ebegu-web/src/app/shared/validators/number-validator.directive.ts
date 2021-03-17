import {AbstractControl, ValidatorFn} from '@angular/forms';

export enum ValidationType {
    INTEGER,
    HALF
}

export function numberValidator(type: ValidationType): ValidatorFn {
    return (control: AbstractControl): {[key: string]: any} | null => {
        if (!control.value) {
            return null;
        }
        if (type === ValidationType.INTEGER) {
            return Number.isInteger(control.value) ? null : {isNotInteger: {value: control.value}};
        }
        if (type === ValidationType.HALF) {
            const isHalf = control.value * 2 % 1 === 0;
            return isHalf ? null : {isNotHalf: {value: control.value}};
        }
        throw new Error('Not implemented');
    };
}
