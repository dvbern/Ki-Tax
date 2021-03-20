import {ValidatorFn} from '@angular/forms';

export enum ValidationType {
    INTEGER,
    HALF
}

export function numberValidator(type: ValidationType): ValidatorFn {
    return (control): {[key: string]: any} | null => {
        if (!control.value) {
            return null;
        }
        if (type === ValidationType.INTEGER) {
            const err = {isNotInteger: {value: control.value}};
            return isInteger(control.value) ? null : err;
        }
        if (type === ValidationType.HALF) {
            const err = {isNotHalf: {value: control.value}};
            return isHalf(control.value) ? null : err;
        }
        throw new Error('Not implemented');
    };
}

function isInteger(val: any): boolean {
    if (isNaN(val)) {
        return false;
    }
    return Number.isInteger(val * 1);
}

function isHalf(val: any): boolean {
    if (isNaN(val)) {
        return false;
    }
    return val * 2 % 1 === 0;
}
