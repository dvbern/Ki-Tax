import {ValidatorFn} from '@angular/forms';

export enum ValidationType {
    INTEGER,
    HALF,
    ANY_NUMBER
}

// tslint:disable-next-line:cognitive-complexity
export function numberValidator(type: ValidationType): ValidatorFn {
    return control => {
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
        if (type === ValidationType.ANY_NUMBER) {
            const err = {isNotFloat: {value: control.value}};
            return isNaN(control.value) ? err : null;
        }
        throw new Error('Not implemented');
    };
}

function isInteger(val: any): boolean {
    if (val.toString().endsWith('.')) {
        return false;
    }
    if (isNaN(val)) {
        return false;
    }
    return Number.isInteger(val * 1);
}

function isHalf(val: any): boolean {
    if (val.toString().endsWith('.')) {
        return false;
    }
    if (isNaN(val)) {
        return false;
    }
    return val * 2 % 1 === 0;
}
